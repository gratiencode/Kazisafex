# ARCHITECTURE ET DETTE TECHNIQUE — plan incrémental

**Date** : 11 novembre 2026  
**Principe** : préserver les fonctionnalités commerciales et les contrats IA ; extraire par petits lots testés, jamais par réécriture globale.

---

## 1. État mesuré

| Classe | Taille approximative | Responsabilités actuelles |
|---|---:|---|
| `com.endeleya.ia.GratienTools` | ≈12 400 lignes | outils LangChain4j, stock, ventes, finance, rapports, sync, MySQL admin |
| `com.endeleya.kazisafex.PosController` | ≈8 700 lignes | POS, panier, stock, retours, impression, inventaire, orchestration UI |
| `tools.Util` | ≈7 400 lignes | imports Excel, calculs, formats, helpers divers |
| `services.RecquisitionService` | ≈5 700 lignes | achats, lots, prix, stocks |
| `com.endeleya.kazisafex.MainuiController` | ≈4 200 lignes | navigation, SSE, mises à jour, session |
| `services.RepportService` | ≈? | rapports PDF/Excel |
| `com.endeleya.kazisafex.GoodstorageController` | ≈? | stockage/dépôts |

Volumétrie globale : **504 fichiers Java principaux**, **530 `System.out.println`**, **484 `catch (Exception)`, **101 `printStackTrace`**.

---

## 2. Architecture cible (sans rupture)

```text
Controllers JavaFX
      │
      ▼
Services applicatifs (cas d'usage)
      │
      ├── Repositories / persistance locale SQLite
      ├── Outbox / synchronisation
      └── Clients Retrofit (canal distant unique)
```

Règles :

- Ne pas remplacer Retrofit/OkHttp.
- Ne pas introduire de second canal distant.
- Ne pas casser les noms/signatures des outils LangChain4j.
- Ne pas déplacer massivement une classe sans tests de caractérisation.
- Conserver les écrans et les contrats API.

---

## 3. Découpage recommandé

### 3.1 GratienTools

Extraire, dans cet ordre, des composants sans état UI :

1. `GratienInventoryTools` : comptage, écarts, ajustements.
2. `GratienStockTools` : stock dépôt/magasin, lots.
3. `GratienFinanceTools` : trésorerie, dépenses, comptes.
4. `GratienReportingTools` : exports et rapports.
5. `GratienSyncTools` : diagnostic/backfill.

Chaque outil conserve son annotation `@Tool`, son nom et sa signature publique. Le composant d’origine délègue, puis les méthodes sont déplacées.

### 3.2 PosController

Extraire :

- `PosSaleWorkflow` : validation panier, calculs, création vente/lignes ;
- `PosStockWorkflow` : lots, déclassement, retours ;
- `PosPrintingWorkflow` : impression thermique/Bluetooth ;
- `PosInventoryWorkflow` : comptage/clôture.

Les opérations réseau et les transactions lourdes restent hors du thread JavaFX.

### 3.3 Util

Extraire uniquement les fonctions pures et testables :

- formats de dates/monnaies ;
- imports Excel ;
- calculs de stock ;
- helpers de chaînes.

Ne pas déplacer les méthodes qui dépendent d’un `EntityManager` ou d’un contrôleur.

---

## 4. Journalisation

- Introduire progressivement `SyncLogger` / SLF4J dans les classes touchées.
- Ne pas faire de remplacement textuel massif.
- Distinguer : réseau, métier, persistance, sécurité.
- Ne jamais logger de jeton, mot de passe ou clé.
- Conserver les stack traces utiles, mais avec un niveau et un contexte.

Ordre : nouvelles classes extraites → fichiers déjà modifiés → reste du dépôt.

---

## 5. Accès aux données

- Les contrôleurs ne doivent pas ouvrir de transaction ni d’`EntityManager`.
- Les services exposent des cas d’usage ; les repositories encapsulent les requêtes.
- Les identifiants SQL dynamiques passent par `SqlGuard`.
- Les opérations réseau ne doivent jamais se produire dans une transaction de persistance.

---

## 6. Tests de caractérisation

Avant chaque extraction :

1. capturer le comportement actuel par un test (entrée/sortie) ;
2. extraire sans changer le comportement ;
3. faire déléguer l’ancien emplacement ;
4. exécuter la suite complète ;
5. supprimer l’ancien code seulement après validation.

---

## 7. Ordre d’exécution proposé

| Étape | Cible | Risque |
|---|---|---|
| 1 | Fonctions pures de `Util` | Faible |
| 2 | Outils inventaire de `GratienTools` | Moyen |
| 3 | Workflows stock/vente de `PosController` | Moyen |
| 4 | Outils finance/reporting | Moyen |
| 5 | Contrôleurs restants | Élevé |
| 6 | Nettoyage des imports/classes mortes | Faible |

Aucune étape ne démarre si la suite de tests n’est pas verte.

---

## 8. Statut

- Audit initial : fait.
- Extraction : **non commencée** ; priorité après stabilisation inventaire/sync.
- Aucun déplacement massif effectué.
