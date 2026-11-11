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
| Produits non comptés | Partiel : clôture crée des comptages zéro, mais uniquement pour les lots connus |
| Calcul d'écart et valorisation | Présent, mais calculé sur le stock théorique courant |
| Validation atomique de la clôture | **Absent** : enchaînement d'écritures séparées |
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

**Correction partielle** : `editStock` et `removeNoCountedProducts` utilisent désormais la date d'ouverture (`dateDebut`) pour interroger le stock théorique, avec repli explicite sur le stock courant si aucun agrégat n'existe. Les mouvements postérieurs à l'ouverture ne modifient plus la référence.

**Limites** : pas de snapshot immuable dédié ; dépend de la présence d'un agrégat à la date d'ouverture ; les anciens inventaires sans date gardent le comportement courant.

**Test** : `openingTheoreticalStockUsesInventoryOpeningDate` vérifie que la date transmise est bien `dateDebut` et que la quantité d'ouverture est utilisée.

**Statut** : **partiellement corrigé**.

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
clôture est idempotente (les comptages zéro déjà présents sont ignorés).
La clôture reste une suite d'écritures séparées, pas une transaction unique.

**Statut** : **partiellement corrigé** — reste à encapsuler dans une transaction
ou une saga idempotente avec verrou.

### B4 — Produits sans lot ignorés à la clôture

`removeNoCountedProducts` parcourt les lots renvoyés par `findAllDistinctLots`
(`recquisition.numlot` et `stock_agregate.num_lot`) et ignore les lots nuls/vides :

```java
if (lot == null || lot.isBlank()) continue;
```

Un produit sans lot (ou dont le stock est porté par un lot vide) n'est donc pas mis à
zéro, malgré le message « les non comptés seront mis à ZERO ».

**Risque** : stock résiduel non inventorié.  
**Statut** : **ouvert, désormais signalé** — les lots `NULL`/vides restent
non soldés, mais la clôture journalise un avertissement explicite par produit
concerné ; la normalisation des requêtes et un test sur base réelle restent
requis.

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
| Produit non compté avec lot null/vide | unité/integration |
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
Il n'est cependant **pas encore un inventaire physique professionnel** : la référence
théorique n'est pas figée, la clôture n'est pas atomique, les comptages concurrents ne
sont pas protégés et la traçabilité du validateur est absente.

La correction B1 est appliquée. Les autres corrections nécessitent des migrations de
schéma et des tests d'intégration réels ; elles ne doivent pas être improvisées sur une
base de production.
