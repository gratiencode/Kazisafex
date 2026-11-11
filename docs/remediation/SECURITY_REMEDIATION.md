# Remédiation sécurité — Campagne 1 (P0/P1)

> Statut : **TERMINÉE** — 2026-11-11. Suite de tests : 177/177 verts
> (`mvn test -Dtests.skip=false`). L'échec préexistant
> `MasterAgentEngineTest.adaptsAnnotatedLegacyObjectVerbatim`
> (InaccessibleObjectException) a disparu après restauration de
> `maven.compiler.release=25` dans `pom.xml` (la valeur `21` locale était
> incompatible avec `--enable-preview` sur JDK 25).

## Résumé des correctifs

| # | Gravité | Correction | Fichiers |
|---|---------|-----------|----------|
| C1 | Critique | Clé API Ollama supprimée du code (compromise dans l'historique git). Résolution : env `KAZISAFE_OLLAMA_API_KEY` → `SecretStore("ia_api_key")` → migration legacy. Aucun défaut embarqué. | `com/endeleya/ia/AiAgents.java` |
| C2 | Critique | Identifiants MySQL : plus de défaut `root`/`Admin*21` (code et prefs). User : env `KAZISAFE_DB_USER` ou pref. Password : env `KAZISAFE_DB_PASSWORD` ou `SecretStore("default_mysql_password")` (migration de l'ancien stockage clair incluse), sinon échec explicite — fail-soft au démarrage (l'app démarre, les connexions échouent avec un message clair). | `services/ManagedSessionFactory.java`, `services/JpaUtil.java` |
| C3 | Critique | Plus d'exécution automatique du binaire téléchargé sur aucune plateforme (`ProcessBuilder` retiré de `installUpdate`) : ouverture du dossier + message, l'utilisateur lance l'installateur. | `com/endeleya/kazisafex/MainuiController.java` |
| C4 | Critique | **Revalidation à chaque application** : le drapeau `verified` persisté n'est plus suffisant, signature/SHA-256 revalidées au démarrage. Chaîne de mise à jour : nouveau `tools/UpdateVerifier` — validation stricte du nom de fichier (anti path-traversal), manifeste distant signé Ed25519 (`manifest.json` + `.sig`), vérification taille + SHA-256, téléchargement en `.part` puis renommage atomique. Tant que la clé publique de signature n'est pas configurée côté serveur : mode « non vérifié » = téléchargement possible, **application automatique au démarrage désactivée** (`applyPendingUpdateAtStartup` refuse et purge la demande en attente). | `tools/UpdateVerifier.java`, `tools/UpdateManager.java`, `MainuiController.java` (interne `DownloadTask`) |
| H1 | Haute | Jeton d'authentification chiffré au repos : `data/core/TokenStore` (clé `auth_token`), migration paresseuse de l'ancienne pref `token`, tous les sites de lecture/écriture migrés (~35 sites). | `TokenStore.java`, `KazisafeServiceFactory.java`, `TokenRefreshClient.java`, `EntryPointController.java`, `MainuiController.java`, `RecqController.java`, `PaymentController.java`, … |
| H2 | Haute | UI Paramètres : mot de passe MySQL et clé API IA écrits via `SecretStore` (chiffrés), plus jamais en clair dans les prefs. | `ParametreController.java` |
| H3 | Haute | Constantes de test Wi-Fi orphelines (`WIFI_TEST_URL`, etc.) supprimées. | `KazisafeServiceFactory.java` |
| H4 | Haute | MySQL TLS : `sslMode` dans l'URL JDBC (whitelist DISABLED/PREFERRED/REQUIRED/VERIFY_CA/VERIFY_IDENTITY), **REQUIRED par défaut** (était `useSSL=false`). La pref legacy booléenne `default_mysql_ssl` est respectée. | `ManagedSessionFactory.java`, `JpaUtil.java` |
| H5 | Haute | « SQLCipher » était inopérant (customizer EclipseLink jamais invoqué sous Hibernate + driver xerial vanilla = base locale non chiffrée). Code mort supprimé (`SQLiteSessionCustomizer.java`). `local_db_secret` : génération aléatoire forte 32 octets pour nouvelles installations, migration de la valeur legacy, stockage chiffré `SecretStore`. **Durcissement ajouté** : permissions POSIX 0700/0600 sur le répertoire, la base et les fichiers WAL/SHM (`DatabaseFileSecurity`). **Le chiffrement réel au repos reste une action manuelle** (voir MANUAL_ACTIONS.md). | `ManagedSessionFactory.java`, `services/utils/SQLiteSessionCustomizer.java` (supprimé), `DatabaseFileSecurity.java` |
| M1 | Moyenne | Secrets locaux : nouveau `services/utils/SecretStore` — AES-GCM via `SecurePreferences`, « passphrase device » générée aléatoirement (env `KAZISAFE_DEVICE_PASSPHRASE` ou `~/.kazisafe/device.key`, permissions 0600, override tests `-Dkazisafe.device.key.file`). | `SecretStore.java` + tous les sites ci-dessus |
| M2 | Moyenne | Redis local : l'instance démarrée par Kazisafex impose `--requirepass` (mot de passe aléatoire 24 octets, stocké chiffré) + `--protected-mode yes` ; le client RESP envoie AUTH sur chaque commande. Les instances système préexistantes sans AUTH ne sont pas modifiées (voir MANUAL_ACTIONS.md). | `RedisBootstrap.java`, `RedisMemoryStore.java` |
| M3 | Moyenne | Nouveau `services/SqlGuard` : tout identifiant concaténé dans une requête JPQL/native est validé (format strict + appartenance au modèle JPA + champ existant). Appliqué à `ProductUidMigrator` (UPDATE/PRAGMA), `GratienTools` (repoint*), `SyncOutboxService` (3 sites), `StateValidationService`, `FullResyncService`, `FinancialStatementSyncService` (whitelist tables natives). | `SqlGuard.java` + les fichiers cités |
| M4 | Moyenne | Jetons de mots de passe root MySQL (IA Gratien) : durée de vie limitée (10 min) + usage unique, purge à l'enregistrement. | `GratienTools.java` |
| M5 | Moyenne | Anti-SSRF `fetchSinglePage` : allowlist stricte (HTTPS, hôtes `endeleya.com`/`www.endeleya.com`, pas d'userinfo, port 443). | `GratienTools.java` |
| M6 | Moyenne | Nom de fichier de mise à jour (`Module.nomModule`, fourni par le serveur) validé strictement avant usage réseau/disque ; repli sur le nom par défaut de la plateforme. | `UpdateVerifier.validateFilename`, `MainuiController.resolveUpdateFilename`, `DownloadTask` |

## Corrections annexes découvertes pendant la campagne

- **Bug de migration des secrets** (introduit puis corrigé pendant la campagne) :
  `SecretStore.migrateFromPlainPref` stockait le ciphertext sous la même clé
  puis la **supprimait** — le secret était perdu au redémarrage (seul
  `TokenStore` s'en sortait par re-stockage). Corrigé : le ciphertext remplace
  le clair, et une valeur déjà chiffrée (migration interrompue) est
  déchiffrée et retournée plutôt que re-migrée en garbage.
- **`TokenStore.saveFromLogin("Bearer    ")`** retournait `"Bearer"` (le
  `trim()` prématuré masquait le préfixe) : le préfixe est maintenant retiré
  avant le trim.
- **Machine de développement** : elle tournait en mode MySQL (`embedded_db=false`)
  **sans user ni mot de passe stockés** — elle vivait sur les défauts du code
  (`root`/`Admin*21`). Une config explicite a été écrite une fois dans les
  prefs (`default_mysql_user=root`, mot de passe legacy en clair, chiffré
  paresseusement au premier accès par l'application). **Ce mot de passe reste
  le mot de passe faible historique : sa rotation côté serveur est requise**
  (voir MANUAL_ACTIONS.md).
- **Fail-soft au démarrage** : si le mot de passe MySQL est absent en mode
  serveur, l'application démarre quand même (log SEVERE) et les connexions
  échouent explicitement — l'utilisateur peut le saisir dans Paramètres
  (l'ancien comportement aurait été un crash au démarrage, chicken-and-egg
  avec l'UI de configuration).
- **`tools/DownloadTask`** (code mort, hôte différent, aucune validation)
  supprimé ; import inutilisé retiré de `ProduitsController`.

## Nouveaux tests (24)

- `services/utils/SecretStoreTest` (5) — chiffrement au repos, ciphertext ≠
  clair, migration legacy (idempotente), suppression.
- `data/core/TokenStoreTest` (5) — stockage chiffré, migration paresseuse de
  la pref `token`, normalisation `Bearer`, clear.
- `tools/UpdateVerifierTest` (7) — validation des noms (traversal, options,
  extensions, longueur), parsing du manifeste, SHA-256, vérification
  taille+empreinte, mode non-vérifié.
- `services/ManagedSessionFactoryDefaultsTest` (7) — modes TLS (whitelist,
  défaut REQUIRED, prefs legacy), user sans défaut, mot de passe depuis le
  magasin chiffré uniquement, migration legacy, échec explicite sans config.

## Ce qui reste volontairement en mode dégradé

- **Mises à jour automatiques** : désactivées tant que le serveur ne publie pas
  de manifeste signé (Ed25519) et que la clé publique n'est pas embarquée
  (`UpdateVerifier.ED25519_PUBLIC_KEY_BASE64`). Comportement : téléchargement
  contrôlé + installation manuelle par l'utilisateur.
- **Chiffrement au repos de la base SQLite** : procédure documentée, non
  exécutée sans validation (règle §3.3 du plan).
- **Redis système préexistant** (service système sans AUTH) : non modifié.

---

## Campagne 1 bis — Revue P0 après baseline du 11/11/2026

### Vérifications effectuées dans le code courant

| Finding | Vérification | Statut |
|---|---|---|
| C1 — clé Ollama | Aucune occurrence de la clé historique ni de fallback en dur dans les sources ; résolution `KAZISAFE_OLLAMA_API_KEY` → `SecretStore` | Corrigé dans l'arbre local, à committer |
| C2 — identifiants MySQL applicatifs | `ManagedSessionFactory` et `JpaUtil` n'ont plus de `root`/`Admin*21` par défaut ; erreur explicite si absent | Corrigé dans l'arbre local |
| C3 — exécution binaire | `installUpdate` ouvre le dossier, ne lance plus l'installateur ; application auto conditionnée à la vérification | Corrigé |
| C4 — intégrité mise à jour | `UpdateVerifier` valide nom, taille, SHA-256 et signature Ed25519 ; clé publique vide = mode non vérifié, exécution auto refusée | Partiel : dépend du manifeste serveur |
| H1 — token | `TokenStore` chiffré AES-GCM + migration legacy | Corrigé |
| H2/M1 — secrets UI | `SecretStore` utilisé pour le mot de passe MySQL et la clé IA | Corrigé |
| H3 — URLs de test | Aucune constante `WIFI_TEST_URL`/`HOTSPOT_TEST_URL` active | Corrigé |
| H4 — TLS MySQL | `sslMode=REQUIRED` par défaut dans la persistance applicative **et désormais dans les outils MySQL directs** (`GratienTools`) | Corrigé dans cette campagne |
| H5 — SQLCipher | Toujours inopérant ; base locale en clair. Permissions POSIX 0700/0600 appliquées (atténuation, pas chiffrement). | **Partiel** — migration manuelle requise |

### Correction apportée dans cette campagne

- Création de `services/utils/MySqlTls.java` : politique TLS unique
  (`KAZISAFE_DB_SSL_MODE` → préférence `default_mysql_ssl_mode` → ancienne
  préférence booléenne → `REQUIRED`).
- `ManagedSessionFactory` et `JpaUtil` délèguent à cette politique.
- `GratienTools.mysqlJdbcUrl` ne construit plus d'URL avec `useSSL=false` ;
  il utilise `sslMode=REQUIRED` (ou le mode explicitement configuré).
- Test ajouté : `services/utils/MySqlTlsTest` (5 cas : défaut, whitelist,
  valeur invalide, compatibilité legacy, normalisation).

### Risques résiduels P0

1. **SQLCipher** : le chiffrement au repos n'est pas actif. La procédure de
   migration est documentée mais volontairement non exécutée (risque de perte
   de données). Décision à prendre : driver SQLCipher + migration, ou
   chiffrement disque OS.
2. **Clé Ollama historique** : présente dans l'historique Git ; sa révocation
   est une action externe (MANUAL_ACTIONS).
3. **MySQL admin direct** : `GratienTools` utilise encore le compte `root`
   pour des opérations de réplication explicitement demandées par
   l'utilisateur. Le mot de passe n'est jamais embarqué ; un compte
   administrateur dédié reste recommandé. Ce choix doit être confirmé ou
   remplacé en campagne 6.
4. **Mises à jour signées** : le mode automatique reste désactivé tant que la
   clé publique n'est pas intégrée et que le serveur ne publie pas
   `manifest.json` + `.sig`.



### Compte admin MySQL direct (outils IA)

Les outils de réplication MySQL de `GratienTools` n'utilisent plus `root` par défaut : `KAZISAFE_MYSQL_ADMIN_USER` ou `default_mysql_admin_user` est requis, avec mot de passe fourni par l'utilisateur. Voir MANUAL_ACTIONS.md §8.


### Mise à jour — revalidation au démarrage (2026-11-11)

`UpdateManager.applyPendingUpdateAtStartup` ne fait plus confiance au drapeau `ksf_pending_update_verified` : le manifeste signé, la taille et le SHA-256 sont revalidés juste avant l'application. Test : `UpdateManagerPendingUpdateTest`.


### Désérialisation Java WebSocket (2026-11-11)

`ImageProductDecoder` (`ObjectInputStream.readObject`) supprimé ; il n'était utilisé que par le `SyncEndpoint` legacy. Aucun flux réseau ne doit désérialiser du Java arbitraire.
