# Changelog — remédiation Kazisafex

Format : une entrée par campagne, items classés par finding.
Ce fichier ne remplace pas le CHANGELOG applicatif : il trace la remise à
niveau sécurité/fiabilité demandée en 2026-11.

## Campagne 1 — Sécurité P0/P1 (2026-11-11)

### Ajouts
- `services/utils/SecretStore.java` : magasin de secrets locaux chiffrés
  (AES-GCM), passphrase device (`KAZISAFE_DEVICE_PASSPHRASE` ou
  `~/.kazisafe/device.key` 0600), migration des prefs en clair.
- `data/core/TokenStore.java` : jeton d'authentification chiffré au repos,
  migration paresseuse de la pref `token`.
- `tools/UpdateVerifier.java` : validation des noms de fichiers de mise à
  jour, manifeste signé Ed25519, vérification SHA-256/taille.
- `services/SqlGuard.java` : whitelist centrale des identifiants concaténés
  dans les requêtes JPQL/natives.
- Tests : `SecretStoreTest` (5), `TokenStoreTest` (5), `UpdateVerifierTest`
  (7), `ManagedSessionFactoryDefaultsTest` (7).

### Sécurité
- C1 : clé API Ollama supprimée du code (résolution env/SecretStore).
- C2 : identifiants MySQL sans défaut embarqué ; échec explicite ;
  fail-soft au démarrage.
- C3 : exécution automatique du binaire de mise à jour supprimée.
- C4 : téléchargement `.part` + renommage atomique + vérification
  manifeste ; application au démarrage réservée aux mises à jour
  authentifiées.
- H1/H2/M1 : jeton, mot de passe MySQL et clé API IA chiffrés au repos,
  sites de lecture/écriture migrés (~35 sites).
- H3 : constantes de test Wi-Fi orphelines supprimées.
- H4 : `sslMode` MySQL REQUIRED par défaut, whitelist des modes.
- H5 : code mort SQLCipher supprimé ; secret local fort généré pour les
  nouvelles installations (chiffrement réel = action manuelle).
- M2 : Redis démarré par l'app avec requirepass + AUTH client.
- M3 : identifiants SQL validés partout (SqlGuard).
- M4 : jetons de mots de passe MySQL IA à durée de vie limitée (10 min).
- M5 : allowlist anti-SSRF sur l'outil de consultation web de l'IA.
- M6 : nom de fichier de mise à jour validé avant tout usage.

### Corrections
- `SecretStore.migrateFromPlainPref` ne perd plus le secret migré
  (suppression post-stockage du ciphertext) ; idempotent sur valeur déjà
  chiffrée.
- `TokenStore.saveFromLogin("Bearer   ")` ne renvoie plus `"Bearer"`.
- `pom.xml` : `maven.compiler.release` restauré à 25 (la valeur locale 21
  cassait `--enable-preview` sur JDK 25 — et provoquait l'échec
  `MasterAgentEngineTest` en suite).
- Suppression de `tools/DownloadTask` (code mort non sécurisé).

### Compatibilité / actions requises
- Voir `MANUAL_ACTIONS.md` : révocation clé Ollama, rotation mot de passe
  MySQL, manifeste signé, chiffrement SQLite, Redis système.
- Les mises à jour automatiques sont suspendues jusqu'au manifeste signé.

## Campagne 2 — Couche HTTP / transport (2026-11-11)

### Fiabilité
- Timeouts explicites sur tous les clients OkHttp : connect 300 s → 30 s,
  ajout `callTimeout` (10 min applicatif / 45 s refresh) — plus d'appels
  pouvant rester bloqués indéfiniment.
- Refresh de jeton single-flight global (`TokenRefreshClient`) : verrou +
  fenêtre de dédup 3 s — deux 401 simultanés ne déclenchent plus plusieurs
  refresh réseau (risque de révocation mutuelle des jetons).
- SSE : reprise après 401 — fermeture du flux expiré, refresh différé du
  jeton, relance via hook de reconnexion avec backoff (1 tentative/min) ;
  le flux ne meurt plus définitivement à l'expiration du jeton.
- SSE : le jeton est relu à chaque (re)connexion (`TokenStore`), plus de
  jeton de login capturé une fois ; le callback de refresh relance le flux.

### Sécurité
- `HttpAuthenticator` ne loggait plus la valeur des jetons en INFO.

### Documentation
- `docs/remediation/HTTP_ARCHITECTURE.md` : description du canal unique
  Retrofit/OkHttp, endpoints, timeouts, cycle du jeton, SSE,
  sérialisation UTC.

## Campagne 1 bis — Revue P0 (2026-11-11)

- Politique TLS MySQL centralisée dans `services/utils/MySqlTls.java` :
  `REQUIRED` par défaut, compatibilité `default_mysql_ssl_mode` et ancienne
  préférence booléenne.
- `GratienTools.mysqlJdbcUrl` ne construit plus d'URL `useSSL=false`.
- `ManagedSessionFactory` et `JpaUtil` délèguent à la politique commune.
- Tests : `MySqlTlsTest` (5 cas). Suite complète après campagne : **183/183**.
- Baseline actualisée : 177 tests au démarrage, 98 entrées locales préservées.
- **H5 (atténuation)** : `DatabaseFileSecurity` applique 0700 au répertoire et
  0600 à la base SQLite/WAL/SHM. Le chiffrement SQLCipher reste manuel.

## Campagne 3 — Synchronisation (audit + première correction, 2026-11-11)

- **Corrigé S1** : `SyncOutboxService.deleteNullPayloadRecords` ne supprime
  plus les `REMOVE`/`DELETE` sans payload ; leur payload null est légitime et
  la matérialisation les traite par identifiant.
- **Corrigé S3** : plafond `MAX_RETRY_ATTEMPTS = 10` puis état
  `DEAD_LETTER` ; la mutation est conservée, n'est plus renvoyée
  automatiquement, et `retryDeadLetters()` permet la remise en file.
- Tests de non-régression ajoutés dans `SyncOutboxServiceTest`.
- Audit complet : `SYNC_RELIABILITY.md`.
- Restent ouverts : acquittement par entité au lieu de mutation, absence de
  tests de reprise réels.

## Campagne 4 — Inventaire physique (audit + première correction, 2026-11-11)

- **Corrigé B1** : `InventaireService.create/update/deleteInventaire` attendent
  la fin de l'écriture SQLite (`.join()`) avant de rendre la main.
- **Corrigé B6 (partiel)** : `CompterService.updateCompter` refuse la
  modification d'un comptage dont l'inventaire est `Terminé` ; garde testée.
- **Ajout B6** : `Inventaire.valide_par` / `valide_le` renseignés à la clôture,
  exclus du payload serveur (`@JsonIgnore`) tant que l'API ne les accepte pas.
- **Corrigé B3 (partiel)** : la clôture crée d'abord les comptages zéro, puis
  marque l'inventaire `Terminé`. Une interruption laisse l'inventaire ouvert
  et la clôture reste rejouable ; la transaction unique reste à faire.
- Audit complet : `INVENTORY_AUDIT.md`.
- Constats ouverts : pas de photographie figée du théorique, clôture non
  atomique, comptages concurrents non protégés, validateur absent, lots
  nuls/vides ignorés à la clôture.

## Campagne 5 — Build et CI (2026-11-11)

- `.github/workflows/ci.yml` : JDK 25, `mvn -Dtests.skip=false test`, package,
  rapports Surefire.
- SpotBugs + FindSecBugs (non bloquant initial) et OWASP Dependency-Check.
- Gitleaks sur l'historique complet.
- Documentation : `BUILD_AND_CI.md`, `TEST_STRATEGY.md`.
- Limites : pas de runner Windows/Linux, pas de MySQL de test, pas de seuil de
  couverture.

## Campagne 2 bis — Retries HTTP (2026-11-11)

- `KazisafeServiceFactory` : `retryOnConnectionFailure(false)` sur le client
  applicatif. Aucun POST de vente, paiement ou régularisation n'est rejoué
  implicitement après une coupure ; les retries restent explicites via
  l'outbox/idempotence.
- Test ajouté : `KazisafeServiceFactoryHttpPolicyTest` (vérifie la politique
  OkHttp et les timeouts).
- `HTTP_ARCHITECTURE.md` mis à jour.

## Campagne 6 — Architecture (cadrage, 2026-11-11)

- Audit des God Classes et plan d'extraction progressive :
  `ARCHITECTURE_REMEDIATION.md`.
- Aucun déplacement massif effectué : les extractions démarreront après la
  stabilisation inventaire/synchronisation, avec tests de caractérisation.
