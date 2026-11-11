# BASELINE — Kazisafex 2.0.25-build6

**Date d'établissement** : 11 novembre 2026  
**Campagne** : 0 — Baseline  
**Principe** : cette campagne n'a modifié aucun fichier de production. Elle fige l'état réel du dépôt, du build et des tests avant les corrections.

---

## 1. État Git

| Élément | Valeur |
|---|---|
| Branche courante | `appmod/java-upgrade-20260409124418` |
| HEAD | `a446130` — `feat(gratien/dedup): exports Excel et fusion des doublons clients, fournisseurs, comptes tresor, livraisons et depenses` |
| Remote | `origin` → `git@github.com:gratiencode/Kazisafex.git` |
| Branches suivies | `master` (`cd948a9`), branche courante (`a446130`) |
| Entrées locales | **98** : 78 fichiers suivis modifiés, 18 ajouts non suivis, 2 suppressions suivies |
| Diff cumulé | **80 fichiers**, `+6 929 / −1 222` lignes |
| Stash existant | `stash@{0}` — `java-upgrade-precheck-20260409124418` |
| Fichiers suivis | 682 |

**Décision de préservation** : les modifications locales et le stash préexistent à cette campagne. Ils sont conservés intacts, sans reset, rebase ni checkout destructif. La baseline ne prétend pas qu'ils sont validés ; ils sont traités comme du travail en cours à vérifier et à committer par campagnes séparées.

Les éléments non suivis incluent notamment :

- `docs/remediation/` ;
- `src/main/java/services/SqlGuard.java` ;
- `src/main/java/services/utils/SecretStore.java` ;
- `src/main/java/data/core/TokenStore.java` ;
- `src/main/java/tools/UpdateVerifier.java` ;
- `src/main/java/com/endeleya/kazisafex/agent/` ;
- de nouveaux tests (`UpdateVerifierTest`, `TokenStoreTest`, `SecretStoreTest`, etc.) ;
- des artefacts racine (`wkj.py`, fichiers `.xlsx`, logs, archives) à nettoyer en campagne 7.

---

## 2. Environnement

| Élément | Valeur |
|---|---|
| JDK par défaut | **Temurin 25.0.4+7 LTS** |
| Maven | **3.9.11** |
| `pom.xml` | `source/target/release = 25`, `--enable-preview` |
| JavaFX | 22.0.2 |
| Hibernate | 6.6 |
| HikariCP | 5.1.0 |
| Tests par défaut | `tests.skip=true` ; exécution explicite `-Dtests.skip=false` |
| CI active | **aucune** : `.github/workflows/` absent ; seuls des artefacts `appmod`/`java-upgrade` sont présents |
| Script de lancement | `run.sh` : Temurin 25, `-Xss32m`, fat-jar |

---

## 3. Build et tests — état mesuré

### Build

```text
mvn test -Dtests.skip=false
```

Résultat mesuré le 11/11/2026 :

| Indicateur | Valeur |
|---|---|
| Suites exécutées | **21** |
| Tests exécutés | **177** |
| Réussis | **177** |
| Échecs | **0** |
| Erreurs | **0** |
| Ignorés | **0** |
| Compilation principale et tests | **SUCCÈS** |

Rapports : `target/surefire-reports/TEST-*.xml`.

La baseline antérieure indiquait 153 tests / 1 échec (`MasterAgentEngineTest`) ; ce résultat est **obsolète**. L'échec a disparu après restauration de `maven.compiler.release=25`, cohérent avec le JDK 25 et `--enable-preview`.

### Limites de la validation

- Les 177 tests ne mesurent pas la couverture réelle ; aucun rapport de couverture n'est produit.
- Les tests d'intégration MySQL/SQLite réels restent partiels.
- `tests.skip=true` par défaut signifie qu'un build de production peut réussir sans exécuter les tests : la CI devra imposer `-Dtests.skip=false`.

---

## 4. Volumétrie et dette technique constatée

| Indicateur | Valeur |
|---|---|
| Fichiers Java de production | **504** |
| Fichiers Java de test | **22** |
| Lignes Java principales | **≈159 926** |
| Lignes Java de test | **≈4 131** |
| `System.out.println` | **530** |
| `catch (Exception` | **484** |
| `printStackTrace` | **101** |

### God Classes principales

| Fichier | Taille |
|---|---|
| `GratienTools.java` | ≈661 Ko / ≈12 400 lignes |
| `PosController.java` | ≈393 Ko / ≈8 700 lignes |
| `Util.java` | ≈332 Ko / ≈7 400 lignes |
| `RecquisitionService.java` | ≈261 Ko / ≈5 700 lignes |
| `MainuiController.java` | ≈159 Ko / ≈4 200 lignes |

---

## 5. Persistance

### 5.1 SQLite local (mode `embedded_db=true`, défaut)

- PU `SQlitePU` via Hibernate, dialecte `KSQLiteDialect`, `hbm2ddl.auto=update`.
- Driver `org.xerial:sqlite-jdbc` — **SQLite vanilla**, pas SQLCipher.
- Fichier local `kazi_<eUid>.db` en mode WAL, `busy_timeout=120000`, `synchronous=NORMAL`.
- Écritures sérialisées par `WriteQueueManager`.
- `SchemaAutoUpdater` effectue les migrations de colonnes.

### 5.2 Chiffrement au repos : constat critique maintenu

Le mécanisme historique « SQLCipher » est **inopérant** :

1. `SQLiteSessionCustomizer` (EclipseLink) n'était jamais invoqué par Hibernate ; il a été supprimé dans les modifications locales.
2. Le driver xerial standard n'implémente pas les pragmas SQLCipher.

**Conséquence** : la base locale reste en clair sur disque. La clé `local_db_secret` est désormais générée aléatoirement et stockée via `SecretStore`, mais elle n'est pas encore appliquée au fichier SQLite. Le chiffrement réel et la migration des bases existantes sont documentés dans `MANUAL_ACTIONS.md` et restent **non exécutés** (opération à haut risque de perte de données).

### 5.3 MySQL direct (mode `embedded_db=false`)

- PU `kazisafe-jmx`, MySQL Connector/J 8.4, HikariCP.
- `ManagedSessionFactory` ne contient plus de compte `root` ni de mot de passe `Admin*21` par défaut ; `JpaUtil` a été supprimé comme code mort en campagne 7.
- L'utilisateur vient de `KAZISAFE_DB_USER` ou d'une préférence ; le mot de passe vient de `KAZISAFE_DB_PASSWORD` ou de `SecretStore` (migration de l'ancien stockage en clair incluse).
- Absence de configuration : **fail-soft** au démarrage, puis erreur explicite à la connexion (pas de fallback silencieux).
- TLS : `sslMode` par défaut `REQUIRED` ; `VERIFY_CA`/`VERIFY_IDENTITY` configurables.
- Des connexions MySQL directes subsistent dans `GratienTools` / `SchemaAutoUpdater` pour des outils de réplication/migration ; elles contournent l'API REST et restent à traiter en campagne 6.

---

## 6. Architecture HTTP

- Une factory Retrofit principale : `data/core/KazisafeServiceFactory` → `https://cloud.kazisafe.com/v1/`.
- Interface `data.network.Kazisafe` (~328 endpoints).
- OkHttp : connect 30 s, read/write 5 min, call 10 min, `retryOnConnectionFailure(true)`.
- TLS : `TLSSocketFactory` avec trust store système ; pas de TrustAll détecté.
- Auth : `HttpInterceptor` + `HttpAuthenticator` + `TokenRefreshClient` avec single-flight.
- SSE `notification/events` via EventSource, reconnexion après 401.
- Tokens : `TokenStore` chiffré AES-GCM ; migration des anciennes préférences `token`.
- Aucune constante `WIFI_TEST_URL`/`HOTSPOT_TEST_URL` active trouvée (constantes orphelines supprimées).

Détail complet : `docs/remediation/HTTP_ARCHITECTURE.md`.

---

## 7. Synchronisation (état à auditer en campagne 3)

Architecture constatée :

1. Listeners JPA → table `sync_outbox`.
2. `BackgroundSyncService` → `POST sync/adaptive/upsync`.
3. `GET sync/outbox/missed` → matérialisation par phases de dépendance.
4. SSE pour le temps réel.
5. `FullResyncService` / `StateValidationService` pour les divergences.

Faiblesses déjà identifiées dans les documents antérieurs, **à reconfirmer** : écriture outbox non atomique avec l'entité, acquittement de mutations non appliquées, absence de plafond de retry, curseur avancé avant matérialisation, échecs silencieux. Aucun test de reprise réelle (coupure réseau, arrêt brutal) n'est encore automatisé.

---

## 8. Chaîne de mise à jour

- `tools/UpdateVerifier` : validation stricte des noms, manifeste JSON, SHA-256, signature Ed25519.
- Clé publique `ED25519_PUBLIC_KEY_BASE64` **vide** → mode « non vérifié ».
- Conséquence : le téléchargement contrôlé reste possible, mais **l'application automatique au démarrage et l'exécution directe du binaire sont désactivées**.
- `UpdateManager` contient encore des scripts de copie/relance (`ProcessBuilder`) ; ils ne doivent être atteignables qu'après vérification. Ce point doit être confirmé par un test de non-régression en campagne 1/5.
- Actions serveur obligatoires : publier `manifest.json` + `.sig`, puis intégrer la clé publique.

---

## 9. Tests existants et angles morts

Tests présents (22 fichiers) : `SyncOutboxServiceTest`, `ManagedSessionFactoryTransactionTest`, `ManagedSessionFactoryDefaultsTest`, `SchemaAutoUpdaterTest`, `SecretStoreTest`, `TokenStoreTest`, `UpdateVerifierTest`, `ClientServiceTest`, `BackgroundSyncServiceTest`, tests IA, impression, sérialisation, etc.

Angles morts prioritaires :

- inventaire physique (aucun test métier dédié) ;
- reprise après coupure réseau / arrêt brutal ;
- idempotence réelle de la synchronisation ;
- mise à jour signée de bout en bout ;
- migration SQLCipher ;
- packaging Windows/Linux et lancement JavaFX ;
- CI (aucun workflow).

---

## 10. Risques résiduels confirmés

| Risque | Statut |
|---|---|
| Base SQLite non chiffrée | **Ouvert** — migration manuelle |
| Clé Ollama historique compromise dans l'historique Git | **Action externe requise** (révocation) |
| Mot de passe MySQL historique `Admin*21` potentiellement encore utilisé sur des postes | **Action externe requise** (rotation) |
| Installation automatique des mises à jour | **Désactivée** tant que le manifeste signé n'est pas publié |
| SQL dynamique / concaténations JPQL | Partiellement protégé par `SqlGuard` — audit complet à poursuivre |
| `System.out`/`catch(Exception)`/`printStackTrace` massifs | **Ouvert** — campagne 6 |
| Absence de CI | **Ouvert** — campagne 5 |
| God Classes | **Ouvert** — campagne 6 |

---

## 11. Interprétation de la baseline

Le dépôt **compile** et la suite complète passe (**177/177**) dans l'état local actuel. Les modifications locales contiennent déjà une partie substantielle de la remédiation P0/P1, mais elles ne sont **ni commitées ni revues de bout en bout** : la baseline les traite comme du travail à vérifier, pas comme un acquis.

Les campagnes suivantes doivent :

1. vérifier chaque correctif P0 par lecture et test ;
2. committer les correctifs par lots isolés, sans mélanger les 98 entrées locales ;
3. ne jamais réinitialiser une base réelle ;
4. conserver `mvn test -Dtests.skip=false` vert à chaque étape.

---

## 12. Décisions de baseline

- **Aucun reset**, **aucun stash pop**, **aucune suppression** des modifications locales.
- **Aucune migration destructive** de base.
- **Aucun push** sur une branche protégée.
- Le build de référence est celui du 11/11/2026 avec JDK 25, Maven 3.9.11 et 177 tests verts.
- Les documents de remédiation existants sont conservés comme travaux antérieurs ; la présente baseline les remplace lorsqu'ils divergent.
