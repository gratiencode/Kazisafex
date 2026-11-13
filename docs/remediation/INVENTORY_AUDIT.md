# AUDIT — Inventaire physique Kazisafex

**Date** : 11 novembre 2026  
**Périmètre** : entités `Inventaire` / `Compter`, services, UI JavaFX, clôture, synchronisation.  
**Méthode** : lecture directe du code, aucun test métier d'inventaire n'existait avant cet audit.  
**Règle** : aucune base réelle n'a été modifiée ; aucune migration destructive n'a été exécutée.

---

## 1. Fonctionnalités réellement présentes

### 1.1 Entités

| Entité | Table | Rôle |
|---|---|---|
| `data.Inventaire` | `inventaire` | Session d'inventaire : `uid`, `code_inventaire`, `etat` (chaîne libre), `date_debut`, `date_fin`, `region`, `comment`, `valeur_total`, `valeur_total_ecart`, `deleted_at`, `updated_at` |
| `data.Compter` | `compter` | Ligne de comptage : inventaire, produit, mesure, lot, quantité physique, quantité théorique, écart, coût d'achat, date de comptage, région, observation, date d'expiration |
| `data.Produit` | `produit` | Produit compté |
| `data.Mesure` | `mesure` | Unité de comptage ; `quantContenu` sert à convertir en unités de base |
| `data.StockAgregate` | `stock_agregate` | Stock théorique par produit/lot/région |

Relations : `Inventaire 1 → N Compter`, `Produit 1 → N Compter`, `Mesure 0..1 → N Compter`.

### 1.2 Cycle actuellement implémenté

1. Création d'un inventaire (`PosController.saveInventaire`, `InventaireService.createInventaire`) avec code, état, dates, région.
2. Sélection d'un inventaire, saisie d'un comptage (`PosController.saveCompter`).
3. `CompterService.createCompter` / `updateCompter` persiste la ligne puis appelle `editStock`.
4. `editStock` purge les anciens ajustements `#INV`, recalcule le stock théorique courant, crée une vente d'ajustement (manquant) ou une réquisition d'ajustement (surplus), puis `rectifyStock`.
5. Clôture (`PosController.cloturerInventaire`) :
   - passe l'état à `Terminé` ;
   - calcule les totaux ;
   - appelle `removeNoCountedProducts` pour créer des comptages à zéro sur les produits/lots non comptés ;
   - propose une annulation avec suppression des comptages créés.
6. Synchronisation HTTP : `createInventaire`, `createCompter`, `syncInventories`, `syncCounts`, endpoints « missed » ; les entités passent aussi par l'outbox JPA.

### 1.3 Fonctionnalités absentes ou incomplètes

| Fonction | État |
|---|---|
| Inventaire total / partiel / par catégorie | Partiel : sélection libre, pas de filtre de périmètre formalisé |
| Photographie figée du stock théorique à l'ouverture | **Absent** |
| Statut de session typé | **Absent** : `etat` est une chaîne libre (`Non commencé`, `En cours...`, `Terminé`) |
| Responsable / validateur / date de validation | **Absent** de l'entité |
| Recompte formalisé | Partiel : mise à jour manuelle d'un comptage ; pas de cycle de recompte |
| Produits non comptés | **Corrigé côté local** : clôture crée des comptages zéro pour tous les lots connus, y compris le lot vide |
| Calcul d'écart et valorisation | Présent, mais calculé sur le stock théorique courant |
| Validation atomique de la clôture | **Corrigée** : régularisation de clôture dans une transaction unique |
| Verrouillage / versionnage | **Absent** : pas de `@Version`, pas de contrainte d'unicité |
| Idempotence de la régularisation | Partielle : purge/réécriture, pas de clé d'idempotence |
| Rapport d'inventaire complet | Partiel : totaux et export, pas de rapport validé/validateur |
| Synchronisation fiable de la clôture | Partielle : dépend de l'outbox et des endpoints existants |

---

## 2. Bugs et risques confirmés

### B1 — Écritures locales non attendues (corrigé dans cette campagne)

`InventaireService.createInventaire`, `updateInventaire` et `deleteInventaire` utilisaient
`submitWrite(...).thenAccept(...)` puis rendaient immédiatement la main. Un appelant
pouvait donc synchroniser ou relire un inventaire non encore commité.

**Correction appliquée** : `.join()` avant retour, comme `CompterService`. Le commit local
est terminé quand la méthode rend la main.  
**Test** : `InventaireServiceTest.createWaitsForLocalFuture` vérifie explicitement
l'appel à `join()` via un `CompletableFuture` espionné.

### B2 — Pas de photographie du stock théorique à l'ouverture

`editStock` calcule `stockTheorik` à partir du stock **courant** après purge des
ajustements. Les ventes/achats/retours effectués pendant le comptage modifient donc la
référence. Le cahier des charges exige une photographie figée à l'ouverture et une
stratégie explicite pour les mouvements postérieurs.

**Correction appliquée** : la photographie théorique est persistée dans
`compter.theoretical_snapshot` à la création du comptage, puis utilisée par
`editStock`, `revertCompterAdjustments` et la clôture. La colonne est ajoutée
par une migration idempotente (`SchemaAutoUpdater.ensureCompterSchema`) et
exclue du payload serveur (`@JsonIgnore`). Les anciennes lignes sans snapshot
retombent sur la référence à la date d'ouverture, puis sur le stock courant.

**Limites** : les comptages zéro créés par une clôture utilisent le stock à la
date de référence ; la réconciliation des très anciens comptages reste à faire.

**Tests** : `openingTheoreticalStockUsesInventoryOpeningDate`,
`CompterServiceInventoryGuardTest` (snapshot prioritaire, repli ancien) et
`SchemaAutoUpdaterTest` (migration SQLite réelle).

**Statut** : **corrigé** (avec repli historique).

### B3 — Clôture non atomique

Ancien ordre :

1. `etat = Terminé` et sauvegarde ;
2. synchronisation ;
3. comptages zéro des produits non comptés ;
4. totaux.

Une interruption entre 1 et 3 laissait un inventaire « Terminé » sans
régularisation complète.

**Correction appliquée** : l'ordre est maintenant :

1. création idempotente des comptages zéro ;
2. contrôle d'annulation ;
3. calcul des totaux ;
4. `etat = Terminé`, `date_fin`, sauvegarde ;
5. synchronisation.

Une interruption pendant l'étape 1 laisse l'inventaire ouvert ; une nouvelle
clôture est idempotente (les comptages zéro déjà présents sont ignorés, et
`removeNoCountedProducts` ne fait rien si l'inventaire est déjà `Terminé`).
Les comptages zéro utilisent un uid déterministe
(`DataId.deterministic("compter-zero", inventaire, produit, lot, région)`) :
eux clôtures concurrentes visent la même ligne et la clé primaire empêche le
double ajustement ; un doublon de clé est journalisé puis ignoré.
La régularisation de clôture (comptages zéro, purges et ajustements de stock)
est désormais exécutée dans **une transaction unique** : `ManagedSessionFactory`
réutilise l'`EntityManager` de la transaction courante pour les écritures
imbriquées, et `removeNoCountedProducts` enveloppe tout le traitement dans
`executeWrite`. Une erreur ou une annulation au milieu de la clôture annule
donc l'ensemble des ajustements. La sauvegarde immédiate reste prise avant la
transaction.

**Statut** : **corrigé côté poste local** — idempotence, concurrence et
atomicité de la régularisation traitées ; une saga multi-instances (verrou
serveur, clé d'idempotence distante) reste nécessaire pour plusieurs postes.

### B4 — Produits sans lot soldés à la clôture

`removeNoCountedProducts` parcourt les lots renvoyés par `findAllDistinctLots`.
Cette méthode renvoie maintenant `COALESCE(numlot, '')` /
`COALESCE(num_lot, '')` et n’exclut plus les lots nuls ou vides. La clôture
normalise chaque lot avec `tools.LotKeys.normalize` (`null`/espaces → `""`)
et ne saute plus le lot vide.

Les lectures et écritures concernées (`findLotStock`, `findComptageForProduit`,
`sommeComptage`, `updateCompterMeta`, purges `#INV`, `rectifyStock`,
`findClosedStockByLot`, agrégats `stock_agregate`) comparent désormais avec
`COALESCE(colonne, '')`, ce qui couvre aussi les lignes historiques en
`NULL`.

**Risque résiduel** : un lot vide sans aucune réquisition ni agrégat reste
invisible ; il est recommandé de valider le scénario sur une base réelle.

**Statut** : **corrigé côté poste local** — les lots `NULL`/vides sont soldés
comme les autres ; un test d’intégration SQLite/MySQL réel reste souhaitable.

### B5 — Comptages concurrents non protégés

Aucune contrainte d'unicité sur `(inventaire_id, product_id, numlot, region)` et aucun
`@Version`. Deux utilisateurs peuvent créer ou modifier le même comptage ; les
purges et recalculs `#INV` peuvent se chevaucher.

**Correction partielle** : `CompterService` sérialise désormais tous les
ajustements/clôtures d'un même inventaire via un verrou réentrant par
`inventaire.uid`. Les autres inventaires restent parallèles.

**Limite** : ce verrou est local au processus. Deux instances de
l'application sur la même base, ou deux nœuds, ne sont pas protégés ; une
contrainte d'unicité et/ou `@Version` reste nécessaire.

**Statut** : **partiellement corrigé** (mono-processus).

### B6 — Absence de validateur et de trace de validation

`Inventaire` ne contient ni `validated_by`, ni `validated_at`, ni version. L'audit ne peut
pas prouver qui a clôturé ni empêcher une modification silencieuse après clôture.

**Corrections appliquées** :

- `CompterService.updateCompter` refuse toute modification si l'inventaire est
  `Terminé` (`requireInventoryOpen`), même si l'appel ne passe pas par l'UI ;
- `Inventaire` porte désormais `valide_par` et `valide_le`, renseignés à la
  clôture depuis l'opérateur connecté ;
- ces champs sont **exclus du payload Retrofit** (`@JsonIgnore`) tant que l'API
  serveur ne les accepte pas : le contrat distant est préservé.

**Limite** : le validateur n'est pas encore synchronisé côté serveur ; une
évolution API est nécessaire. La version d'inventaire (`@Version`) reste
absente.

**Statut** : **partiellement corrigé** (traçabilité locale).

### B7 — Calcul de l'écart dépendant du stock courant

Même sans mouvement, `editStock` purge puis recalcule à partir du stock agrégé. La
quantité théorique stockée sur `Compter` est réécrite. Il n'existe pas de champ
`theoretical_at_open` ni de date de référence.

**Statut** : **ouvert** (lié à B2).

### B8 — Synchronisation de l'inventaire non testée de bout en bout

Les endpoints `syncInventories` / `syncCounts` existent, mais aucun test ne couvre :

- coupure pendant la clôture ;
- réponse HTTP perdue ;
- reprise après redémarrage ;
- double envoi d'une régularisation ;
- ordre inventaire → comptage.

**Statut** : **ouvert**.

---

## 3. Flux de données actuel

```text
JavaFX (PosController)
  ├─ saveInventaire ──> InventaireDelegate ──> InventaireService (SQLite/MySQL)
  ├─ saveCompter ────> CompterDelegate ─────> CompterService
  │                                           ├─ persiste Compter
  │                                           └─ editStock
  │                                                ├─ purge #INV
  │                                                ├─ calcule stock théorique courant
  │                                                ├─ Vente/LigneVente (manquant)
  │                                                └─ Recquisition (surplus)
  └─ cloturerInventaire
        ├─ Inventaire.etat = Terminé
        ├─ removeNoCountedProducts (comptages zéro)
        └─ sync HTTP / outbox
```

---

## 4. Scénarios métier à couvrir

1. Vente pendant le comptage.
2. Achat pendant le comptage.
3. Retour client.
4. Sortie de stock.
5. Produit compté plusieurs fois.
6. Produit non compté.
7. Inventaire interrompu.
8. Deux utilisateurs comptant simultanément.
9. Inventaire hors ligne.
10. Synchronisation après reconnexion.
11. Interruption HTTP pendant la validation.
12. Réponse HTTP perdue.
13. Double régularisation.
14. Modification après clôture.

---

## 5. Corrections recommandées, par ordre de sécurité

### Étape 1 — Peu risquée, à faire maintenant

- Conserver B1 (attente des écritures locales).
- Ajouter un **verrou de clôture** applicatif (état `CLOSING`) et refuser toute nouvelle
  saisie dès le début de la clôture.
- Ajouter une **contrainte d'unicité** `(inventaire_id, product_id, numlot, region)` avec
  migration non destructive (dédoublonnage préalable).
- Ajouter `validated_by`, `validated_at`, `version` à `Inventaire` (colonnes nullables).
- Refuser toute modification si `etat = Terminé` (déjà partiel côté UI ; à imposer côté
  service).

### Étape 2 — Photographie du théorique

- Ajouter à `Inventaire` une date de référence (`opened_at`) et, à `Compter`, une
  quantité théorique figée (`quantite_theorique_ouverture`).
- Au premier comptage d'un produit/lot, enregistrer la quantité théorique du moment.
- Décider et documenter la stratégie des mouvements postérieurs :
  - soit figer le théorique et produire un écart « à date d'ouverture » ;
  - soit recalculer explicitement le théorique à la clôture en affichant les mouvements
    intermédiaires.
- Ne pas modifier silencieusement la référence.

### Étape 3 — Clôture atomique

- Déplacer la clôture dans `CompterService`/`InventaireService` avec une transaction
  unique (ou une saga idempotente) : état → comptages zéro → totaux → marqueur de clôture.
- Utiliser une clé d'idempotence `inventory_close:<uid>`.
- En cas d'échec, ne pas laisser `Terminé`.

### Étape 4 — Synchronisation

- Vérifier que l'ordre inventaire → comptages → clôture est respecté.
- Ajouter un test de réponse HTTP perdue et de reprise.
- Documenter les évolutions API nécessaires (validation, idempotence, validateur).

---

## 6. Tests à créer

| Test | Type |
|---|---|
| `InventaireServiceTest` avec SQLite temporaire réel | intégration |
| Attente du commit local avant retour | intégration |
| Produit non compté avec lot null/vide | unité ajoutée (`CompterServiceLotNormalizationTest`) ; intégration base réelle à faire |
| Double clôture | unité |
| Comptage concurrent | intégration |
| Écart avec vente pendant comptage | intégration |
| Reprise après interruption de clôture | intégration |
| Synchronisation inventaire/comptage | intégration HTTP mockée |
| Migration des nouvelles colonnes | intégration SQLite |

---

## 7. Évolutions API serveur nécessaires

1. Endpoint de clôture idempotent (ou clé d'idempotence sur l'upsert).
2. Champs `validated_by`, `validated_at`, `version` dans le DTO d'inventaire.
3. Contrat explicite pour les comptages zéro (produits non comptés).
4. Réponse de synchronisation indiquant les doublons/ignorés.

---

## 8. Conclusion de l'audit

Le module existe réellement et couvre la création, la saisie, l'ajustement et la clôture.
Il n'est cependant **pas encore un inventaire physique professionnel au sens
multi-postes** : la référence théorique et l'atomicité locale de la clôture sont
corrigées, mais les comptages concurrents entre plusieurs postes ne sont pas
protégés par le serveur et la traçabilité du validateur reste à compléter.

La correction B1 est appliquée. Les autres corrections nécessitent des migrations de
schéma et des tests d'intégration réels ; elles ne doivent pas être improvisées sur une
base de production.
