# STRATÉGIE DE TESTS — Kazisafex

**Date** : 13 novembre 2026  
**Baseline** : 250 tests verts (dont inventaire, outbox atomique, sauvegardes, dates thread-safe).

---

## 1. Principe

Le ratio « lignes de test / lignes de production » n'est pas une mesure de
qualité. La stratégie cible les **risques métier** :

1. sécurité des secrets et des mises à jour ;
2. persistance locale SQLite et transactions ;
3. synchronisation sans perte ni doublon ;
4. inventaire physique et régularisation ;
5. calculs financiers (ventes, retours, trésorerie) ;
6. non-régression de l'interface JavaFX.

## 2. Tests existants (état réel)

| Domaine | Fichiers principaux | Couverture |
|---|---|---|
| Secrets | `SecretStoreTest`, `TokenStoreTest` | Bonne (chiffrement, migration, suppression) |
| Mise à jour | `UpdateVerifierTest` | Bonne (noms, SHA-256, manifeste, lecture bornée) |
| MySQL/TLS | `ManagedSessionFactoryDefaultsTest`, `ManagedSessionFactoryTransactionTest`, `MySqlTlsTest` | Bonne (modes TLS, défauts, transactions) |
| Sync | `SyncOutboxServiceTest`, `BackgroundSyncServiceTest`, `SyncOutboxListenerAtomicTest`, `SyncAcknowledgementsTest` | Bonne sur UPSERT/ordre/REMOVE, dead-letter, atomicité, coalescence, acquittement ; reprise réelle incomplète |
| Inventaire | `CompterServiceInventoryGuardTest`, `CompterServiceValidationTest`, `InventaireServiceTest` | Bonne sur garde/validation/attente locale/clôture idempotente/photographie ; intégration SQLite réelle limitée |
| Ventes | `GratienSaleWorkflowTest`, `ClientServiceTest` | Partielle |
| IA | `GratienMemoryCompactionTest`, `GratienGenericToolsTest`, `OllamaModelFallbackTest` | Partielle |
| Impression | `PaymentControllerThermalPrintTest`, `SerializationMigrationTest` | Partielle |
| UI JavaFX | `PosListViewDisplayTest` | Faible |
| Migration | `SchemaAutoUpdaterTest`, `DataIdTest` | Bonne (migration SQLite réelle, identifiants déterministes) |

## 3. Tests prioritaires à ajouter

### 3.1 Inventaire (priorité absolue)

- SQLite temporaire réel, pas H2 :
  - création d'un inventaire et attente du commit ;
  - comptage, écart, ajustement;
  - produit non compté ;
  - clôture et double clôture ;
  - interruption de clôture ;
  - deux comptages concurrents.
- Scénarios de vente/achat/retour pendant le comptage.

### 3.2 Synchronisation

- Outbox non vide au démarrage.
- Coupure entre écriture métier et outbox.
- Réponse HTTP perdue après envoi.
- Deux workers concurrents.
- Rejeu d'une mutation déjà appliquée (idempotence).
- Matérialisation partielle puis reprise.
- `REMOVE` sans payload préservé (test ajouté).

### 3.3 Sécurité

- UpdateVerifier avec manifeste réellement signé (clé de test).
- Absence de secrets dans les logs.
- `SecretStore` permissions 0600 sur POSIX.
- Migration legacy TokenStore/SecretStore.

### 3.4 Transactions et ressources

- Fuite d'EntityManager (leak cleaner).
- Pool MySQL sous erreurs.
- Fermeture des connexions après transaction.
- Timeouts HTTP et annulation.

## 4. Tests d'intégration

- **SQLite** : driver réel, fichier temporaire, WAL, `SchemaAutoUpdater`.
- **MySQL** : conteneur/testcontainers si l'infrastructure l'autorise ;
  ne pas utiliser H2 pour les comportements MySQL.
- **HTTP** : MockWebServer pour Retrofit/OkHttp, erreurs 401/429/500,
  timeouts, réponse perdue.
- **JavaFX** : TestFX uniquement pour les parcours critiques (POS, clôture
  inventaire), avec plateforme headless si possible.

## 5. CI

La CI exécute `mvn -Dtests.skip=false test` et publie les rapports Surefire.
Les tests qui dépendent d'Ollama ou d'un MySQL externe doivent être marqués
et exclus par profil, jamais silencieusement ignorés.

## 6. Règles

- Un bug corrigé sans test de régression est rouvert.
- Un test qui échoue n'est jamais désactivé pour faire passer le build.
- Les tests d'intégration utilisent des bases temporaires, jamais une base
  cliente réelle.
- Les secrets de test sont générés à la volée, jamais commités.

## 7. Objectif progressif

| Campagne | Objectif |
|---|---|
| 4 | Tests inventaire SQLite + clôture |
| 3 | Tests reprise sync et idempotence |
| 5 | CI bloquante build/tests |
| 6 | SpotBugs bloquant par catégorie |
| 7 | Couverture mesurée et seuil progressif |
