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
- **Corrigé B5 (partiel)** : verrou réentrant par inventaire dans
  `CompterService` (création/mise à jour/suppression/clôture sérialisées dans
  le processus) ; test de verrouillage ajouté. La protection multi-instances
  reste à faire.
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

## Campagne 7 — Nettoyage du dépôt (2026-11-11)

- Suppression des artefacts générés suivis par Git : `.codex`,
  `compile_output.txt`, `dependency-reduced-pom.xml`, `javac.*.args`,
  `jshell.history`, Javadocs tierces `org/`, `src/main/java/WEB-INF/glassfish-web.xml`.
- Vérification préalable : aucune référence Maven/source à ces chemins ; le
  build et les tests ne les utilisent pas.
- `.gitignore` complété pour éviter leur réintroduction.
- `services/JpaUtil.java` supprimé : ancien chemin MySQL dupliqué, uniquement importé
  (jamais appelé) ; imports retirés de `CommandeService`/`CommandeListService`.

## Campagne 5 bis — Dépendances Maven (2026-11-11)

- Suppression de `controlsfx:8.40.11` (obsolète JavaFX 8) : seul usage
  `Platform.WINDOWS` remplacé par `PlatformUtil.isWindows()`.
- Suppression de `jersey-media-jaxb:4.0.0-M1` et `javax.ws.rs-api:2.0`
  inutilisés ; ajout explicite de `jakarta.ws.rs-api:3.1.0`, réellement requis
  par `module-info.java`.
- `maven-dependency-plugin` passé de 3.5.0 à 3.8.1 ; l'analyse de dépendances
  reste bloquée par le bytecode Java 25 (`Unsupported class file major version 69`).
- Build et suite complète vérifiés après nettoyage.
- `maven-enforcer-plugin` ajouté : Java `[25,26)` et Maven `[3.9,)` obligatoires.
- HikariCP aligné sur la propriété `${hikaricp.version}` (5.1.0) au lieu du 5.0.1 figé.

## Campagne 7 bis — Ressources au shutdown (2026-11-11)

- `ManagedSessionFactory` enregistre un hook d'arrêt après initialisation
  réelle (jamais en mode test) : ferme l'`EntityManagerFactory`, le pool et la
  file d'écriture SQLite.
- Aucun hook n'est enregistré avant l'initialisation de la base, pour éviter
  toute création tardive de base pendant un arrêt.

## Campagne 1 ter — Garde-fou secrets/TLS (2026-11-11)

- Ajout de `scripts/security-regression.sh` : échoue si la clé Ollama
  historique, `Admin*21`, `useSSL=false` ou un contournement TrustAll /
  HostnameVerifier réapparaît dans `src/main/java`.
- Intégré comme première étape de la CI (`build-and-test`).
- Vérifié localement : aucune occurrence interdite.

## Campagne 7 ter — Code mort réseau (2026-11-11)

- `tools/HttpSyncHandler.java` supprimé : 1 576 lignes jamais instanciées,
  uniquement citées dans des commentaires de `SyncEngine`.
- Le chemin réel `SyncEndpoint` / `BackgroundSyncService` n'est pas modifié.
- Build et suite complète vérifiés après suppression.

## Campagne 7 quater — Écritures MySQL d'inventaire (2026-11-11)

- `CompterService.createCompter/updateCompter/deleteCompter` (branche MySQL)
  utilisent `ManagedSessionFactory.executeWrite` au lieu de manipuler
  directement l'`EntityManager` thread-local : la session est fermée après
  commit/rollback.
- Les écritures auxiliaires `createPrixDeVente`, `createRecquisition`,
  `createLigneVente`, `createVente` et la purge `purgeAjustementLigneVente`
  passent également par `executeWrite`.
- `InventaireService.create/update/deleteInventaire` (branche MySQL) passent
  aussi par `executeWrite` : session fermée après commit/rollback.
- Les lectures restent sur `getEntityManager` ; audit à poursuivre.

## Campagne 1 quater — Admin MySQL explicite (2026-11-11)

- Les outils IA de réplication MySQL n'utilisent plus `"root"` par défaut :
  `KAZISAFE_MYSQL_ADMIN_USER` ou la préférence `default_mysql_admin_user`
  est obligatoire.
- Le mot de passe reste fourni par l'utilisateur ; aucune valeur embarquée.
- Erreur explicite si le compte admin n'est pas configuré.

## Rapport d'étape (2026-11-11)

- Création de `FINAL_REPORT.md` : état vérifié, corrections livrées, points
  partiels/non corrigés, actions manuelles et évolutions API serveur.
- Aucune conclusion prématurée : la mission reste active.


## Campagne 1 quinquies — Revalidation des mises à jour (2026-11-11)

- `applyPendingUpdateAtStartup` revalide systématiquement signature + SHA-256 ;
  un drapeau `verified` persisté ne suffit plus.
- Test de non-régression ajouté.


## Campagne 4 bis — Référence d'ouverture de l'inventaire (2026-11-11)

- `CompterService` calcule le théorique à `Inventaire.dateDebut` via `findLotStock` ;
  repli sur le stock courant si aucun agrégat à cette date.
- La clôture des non-comptés utilise la même référence.
- Test de sélection de date ajouté ; suite complète 195/195.

## Campagne 6 bis — Bugs de correction SpotBugs (2026-11-11)

- `Util.findPricesForReq` comparait un `Recquisition` à une `String`
  (filtre toujours vide) ; compare désormais le `uid`.
- Comparaisons d'`Integer` par `==` corrigées en `Objects.equals` dans
  `Util.findLigneVenteForVente`, `Util.findSaleItemsFor` et `Util.getVente`.
- `findSaleItemsFor` protège aussi une référence de vente nulle.
- Tests `UtilTest` ajoutés ; suite complète 198/198.

- PlanStep : identifiant stable sans `Math.abs(Integer.MIN_VALUE)`.
- SecretStore : `writeDeviceKeyFile` accepte un chemin relatif sans parent.
- Tests PlanStep/SecretStore ; suite complète 200/200.

## Campagne 1 sexies — Désérialisation Java non sûre (2026-11-11)

- `util/decoders/ImageProductDecoder` supprimé : `ObjectInputStream.readObject()`
  sur un flux WebSocket, signalé `OBJECT_DESERIALIZATION` par FindSecBugs.
- Il n'était référencé que par le `SyncEndpoint` legacy jamais instancié.
- Aucun autre décodeur n'utilise la désérialisation Java.

## Campagne 6 ter — NPE CompactMode (2026-11-11)

- `CompactMode.printWithThermal` : le libellé client ne déréférence plus
  `client` après un test nul ; noms/téléphones nuls gérés.
- `pos.close()` n'est plus appelé si le flux d'imprimante n'a pas été créé.
- Suite complète 200/200.

## Campagne 6 quater — Corrections SpotBugs supplémentaires (2026-11-11)

- `ImmobilisationDelegate.deleteImmobilisation` refuse désormais une entité
  nulle avant tout accès au stockage ; test ajouté.
- `PVUnit` implémente `Serializable` : le champ `Input.pvus` n'est plus un
  champ non sérialisable signalé par SpotBugs.
- Suite complète 201/201.

## Campagne 6 quinquies — Formats de date thread-safe (2026-11-11)

- `ThreadSafeSimpleDateFormat` synchronise `format`/`parse` tout en conservant
  le type et l'API des champs `Constants`.
- Les champs statiques `DATE_HEURE_FORMAT`, `DATE_ONLY_FORMAT`,
  `YEAR_AND_MONTH_FORMAT`, `YEAR_ONLY_FORMAT`, `TIMESTAMPED_FORMAT` et
  `dateFormater` utilisent cette implémentation.
- Test concurrent ajouté ; suite complète 202/202.

## Campagne 6 sexies — Null-check GratienTools (2026-11-11)

- `prepareAllProductCodesForThermalPrint` vérifie `product == null` avant
  `matchesPrintTarget`/`getNomProduit` ; un produit nul est ignoré proprement.
- Suite complète 202/202.

## Campagne 7 septies — Suppression du canal WebSocket mort (2026-11-11)

- `SyncEndpoint`/`WsClientEndpoint` supprimés : `getInstance()` n'était jamais
  initialisé (constructeur commenté), donc `Util.sync*` n'envoyait rien.
- `BaseModelDecoder` et les 25 encodeurs `util/encoders` supprimés : aucun
  autre consommateur.
- Dépendances `jakarta.websocket-client-api` / `jakarta.websocket-api` et
  `requires jakarta.websocket.client` retirées.
- `Util.sync*` conservés comme API sans envoi réseau ; le canal actif reste
  SSE + outbox Retrofit.
- Suite complète 202/202.

## Campagne 3 bis — Remise en file des dead-letters (2026-11-11)

- Test ajouté vérifiant que `resetForFullResync` inclut bien les mutations
  `DEAD_LETTER`, sinon elles resteraient bloquées après correction.
- Suite complète 203/203.

## Campagne 7 octies — Sauvegarde SQLite avant migration (2026-11-11)

- `DatabaseBackup` copie la base SQLite (+ WAL/SHM) avant l'ouverture Hibernate
  et `hbm2ddl.auto=update`, avec rétention des 5 dernières sauvegardes.
- Protection best-effort : un échec est journalisé sans bloquer le démarrage.
- Tests ajoutés ; suite complète 205/205.

## Campagne 2 ter — Timeouts de téléchargement des mises à jour (2026-11-11)

- `UpdateManager.downloadJar` et le `DownloadTask` de `MainuiController`
  limitent désormais la connexion à 15 s et la lecture à 60 s ; un serveur
  injoignable ne bloque plus le démarrage/la mise à jour indéfiniment.
- Suite complète 205/205.
