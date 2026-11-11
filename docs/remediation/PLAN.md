# PLAN OPÉRATIONNEL DE REMISE À NIVEAU

Ce plan découle de la baseline (`BASELINE.md`) et des audits du 10-11/11/2026. Il est exécuté campagne par campagne (§14 du cahier des charges). Statuts : ✅ confirmé par lecture directe du code, ⏳ à vérifier pendant l'implémentation, ❌ infirmé.

## 1. Findings et verdicts

| Finding | Verdict | Fichier(s) | Correction prévue | Campagne |
|---|---|---|---|---|
| C1 — clé Ollama en dur | ✅ | `AiAgents.java:152-153` | Suppression du fallback ; lecture env/prefs ; rotation = action manuelle externe | 1 |
| C2 — `Admin*21` + `root` défaut | ✅ | `ManagedSessionFactory.java:505,513`, `JpaUtil.java:122,130` | Suppression des défauts ; erreur explicite si non configuré ; persistance chiffrée | 1 |
| C3 — exécution binaire téléchargé | ✅ | `MainuiController.java:2456-2461` | Exécution directe supprimée ; ouverture du dossier / lien navigateur ; installation contrôlée uniquement après vérification | 1 |
| C4 — download sans intégrité | ✅ | `MainuiController` (classe interne `DownloadTask`), `UpdateManager.downloadJar`, `tools/DownloadTask` | Validation stricte du nom ; téléchargement vers répertoire contrôlé ; vérif SHA-256 + taille via manifest signé quand disponible ; sinon mode « non vérifié » sans exécution auto | 1 |
| H1 — token en clair prefs | ✅ | 35 sites (5 écritures, 30 lectures) | `TokenStore` chiffré (AES-GCM via SecurePreferences) + passphrase device ; migration lazare des tokens existants | 1 |
| H2 — mot de passe MySQL UI en clair | ⏳ | `ParametreController` (~501, ~908) | Écriture via `SecretStore` (chiffré), lecture dégradée | 1 |
| H3 — URLs HTTP/IPs internes | ✅ (constantes **orphelines**, zéro usage) | `KazisafeServiceFactory.java:55-57` | Suppression pure | 1 |
| H4 — `useSSL=false` défaut | ✅ | `ManagedSessionFactory.java:67`, `JpaUtil.java:77` | Défaut TLS actif (`sslMode` préférence/env : REQUIRED défaut, VERIFY_CA/VERIFY_IDENTITY possible) | 1 |
| H5 — SQLCipher inopérant | ✅ **aggravé** : chiffrement inexistant (customizer EclipseLink jamais invoqué sous Hibernate + driver xerial vanilla) | `ManagedSessionFactory.java:177-195`, `SQLiteSessionCustomizer.java` | Suppression du code mort trompeur ; clé forte générée pour futures installations ; chiffrement réel = procédure documentée (MANUAL_ACTIONS) | 1 |
| M1 — clé IA en clair | ⏳ | `ParametreController` (~866) | `SecretStore` | 1 |
| M2 — Redis sans AUTH | ⏳ | `RedisBootstrap` | Vérif exposition réelle (bind localhost) ; password si possible sans casser le démarrage | 1 |
| M3 — SQL dynamique | ✅ (interne, non exploitable aujourd'hui) | `ProductUidMigrator:238`, `GratienTools:4712`, services sync | Whitelists d'identifiants | 1 |
| M4 — map statique mots de passe | ⏳ | `GratienTools:2439+` | TTL + purge | 1 |
| M5 — SSRF fetchSinglePage | ⏳ | `GratienTools:6260+` | Validation d'URL (schéma https, blocage IPs privées) | 1 |
| Tests : 1 échec préexistant | ✅ | `MasterAgentEngineTest` | `--add-exports` surefire/module-info | 5 |

## 2. Architecture cible (inchangée)

JavaFX → services métier → SQLite (WAL) + outbox → Retrofit/OkHttp → HTTPS → API REST → MySQL serveur. Aucune nouvelle stack de communication. La seule interface ajoutée côté client est un magasin de secrets local (`SecretStore`/`TokenStore`) et un vérificateur de mises à jour (`UpdateVerifier`).

## 3. Ordre d'exécution et dépendances

1. **C1/H3** (indépendants, triviaux) → commit A.
2. `SecretStore` + `TokenStore` (fondation) → commit B. Sans dépendance ascendante.
3. **C2/H4** (ManagedSessionFactory + JpaUtil + ParametreController) dépend de SecretStore → commit C.
4. **H1/M1** (TokenStore généralisé aux 35 sites, clé IA) dépend de TokenStore → commit D.
5. **C3/C4/M6** (chaîne de mise à jour) indépendant de 2-4 → commit E.
6. **H5** (suppression code mort + clé forte nouveaux installs) → commit F.
7. **M2/M3/M4/M5** → commit G.
8. Campagne 2 (HTTP) → docs + timeouts + refresh single-flight.
9. Campagne 3 (sync) → correctifs outbox (statuts, REMOVE null-payload, acquittement conditionnel, plafond retry).
10. Campagne 4 (inventaire) → audit puis correctifs.
11. Campagne 5 (build/tests/CI).
12. Campagnes 6-7 (architecture, nettoyage) — progressives.

## 4. Risques de régression identifiés

| Risque | Mitigation |
|---|---|
| Migration token prefs → chiffré casse une lecture oubliée | Grep exhaustif avant/après (`get("token"` → 0 site restant), compilation, tests |
| Suppression défaut MySQL casse un déploiement non-embedded existant | Message d'erreur explicite + doc ; l'utilisateur peut définir pref/env ; pas de valeurs en dur |
| Changement useSSL défaut casse connexions LAN sans TLS | `sslMode` configurable ; défaut REQUIRED (chiffré, sans vérif certificat) — compatible serveurs auto-signé ; VERIFY_* opt-in documenté |
| Mise à jour : désactivation de l'exécution auto « casse » la MAJ | Comportement dégradé voulu et documenté (mission §5.3) : téléchargement conservé, exécution manuelle guidée ; restauration complète dès manifeste signé déployé côté serveur |
| H5 : suppression customizer/prefs | Vérifié : aucune autre référence (grep) ; customizer jamais invoqué sous Hibernate |
| Recompilation Java 25 preview | `mvn compile` systématique + suite de tests à chaque campagne |

## 5. Tests nécessaires (ajoutés en campagne 1)

- `SecretStoreTest` : génération device key, store/load, migration plaintext→chiffré, permissions fichier (0600).
- `TokenStoreTest` : set/get/migration/suppression plaintext.
- `UpdateVerifierTest` : validation noms de fichiers (refuse `../`, séparateurs, caractères exotiques, contrôle longueur), vérification SHA-256, manifeste signé (Ed25519, clé de test), refus manifeste invalide.
- `ManagedSessionFactoryDefaultsTest` : absence de défaut `root`/`Admin*21`, URL sslMode par défaut.
- Tests d'intégration existants relancés (SyncOutboxServiceTest etc.) — non-régression.

## 6. Évolutions côté serveur nécessaires (documentées, hors périmètre)

1. **Manifeste de mise à jour signé** : `GET download/manifest.json` + `manifest.json.sig` (Ed25519), champs version/plateforme/fichier/taille/sha256. Jusqu'à déploiement, le client reste en mode « non vérifié » (pas d'exécution auto).
2. **Idempotence upsync** : clé d'idempotence par mutation (uid outbox) reconnue serveur (déjà partiellement le cas via `uid` — à confirmer campagne 3).
3. **Suppressions distantes** : payload non null ou champ dédié pour les DELETE dans `sync/outbox/missed`.

## 7. Actions manuelles externes (MANUAL_ACTIONS.md)

- Révocation/rotation de la clé Ollama exposée (fournisseur).
- Rotation du mot de passe MySQL si `Admin*21` utilisé en production ; création d'un compte applicatif dédié.
- Génération de la paire de clés Ed25519 de signature des mises à jour et publication du manifeste.
- Décision chiffrement au repos (SQLCipher + migration des bases OU chiffrement disque OS).

---

## 8. Mise à jour après baseline du 11/11/2026

### 8.1 Constats vérifiés

| Point | État vérifié |
|---|---|
| Build | `mvn test -Dtests.skip=false` : **177/177 tests verts**, 21 suites |
| Branche | `appmod/java-upgrade-20260409124418`, HEAD `a446130` |
| Travail local | 98 entrées (78 fichiers modifiés, 18 ajouts, 2 suppressions) + 1 stash **préservés** |
| C1 | Aucune clé API en dur trouvée dans le code courant ; résolution env/`SecretStore` |
| C2 | Aucun défaut `root`/`Admin*21` dans `ManagedSessionFactory`/`JpaUtil` ; fail-soft explicite |
| C3/C4 | `UpdateVerifier` présent ; clé de signature vide → exécution automatique désactivée ; `UpdateManager` à revalider |
| H1/H2/M1 | `TokenStore`/`SecretStore` présents ; migration des anciennes prefs |
| H3 | Constantes de test orphelines absentes |
| H4 | `sslMode=REQUIRED` par défaut, modes vérifiés |
| H5 | **Toujours ouvert** : base SQLite non chiffrée ; migration manuelle |
| CI | **Absente** (`.github/workflows/` inexistant) |
| Couverture | Inconnue ; aucun rapport de couverture |
| Sync | Faiblesses documentées, **non encore corrigées ni testées de bout en bout** |
| Inventaire | **Pas encore audité** (document à produire) |

### 8.2 Ordre d'exécution révisé

1. **P0 — revue et sécurisation finale** : vérifier/committer par lots les correctifs C1/C2/C3/C4/H1-H5/M1-M6 déjà présents ; compléter les tests manquants ; ne pas committer les 98 entrées en bloc.
2. **P1 — HTTP** : valider les timeouts, TLS, refresh single-flight, SSE 401, erreurs réseau ; tests de non-régression.
3. **P1 — synchronisation** : audit outbox/idempotence/reprise ; corriger les défauts confirmés ; tests de coupure et redémarrage.
4. **P1 — inventaire physique** : audit du code et des entités, puis corrections et tests.
5. **P2 — build/CI** : CI GitHub Actions (compile, tests, SpotBugs/FindSecBugs, Gitleaks, dépendances), packaging.
6. **P2 — architecture** : réduction progressive des God Classes, journalisation, code mort.
7. **P3 — performance/nettoyage** : artefacts, dépendances, ressources, mesures avant/après.

### 8.3 Règles de commit

- Un commit par campagne logique, jamais un `git add -A` global.
- Les modifications locales préexistantes ne sont pas attribuées à cette campagne.
- `mvn test -Dtests.skip=false` doit rester vert avant chaque commit.
- Aucun reset, stash pop ou migration destructive.

### 8.4 Tests requis avant de déclarer une campagne terminée

- Compilation principale et tests.
- Tests ciblés des fichiers modifiés.
- Test de non-régression sur `SyncOutboxServiceTest`, `ManagedSessionFactoryTransactionTest`, `TokenStoreTest`, `SecretStoreTest`, `UpdateVerifierTest`.
- Documentation mise à jour dans `docs/remediation/`.
