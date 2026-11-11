# RAPPORT FINAL D'ÉTAPE — Kazisafex

**Date** : 11 novembre 2026  
**Statut global** : **mission en cours**, pas de clôture definitive.  
**Principe** : aucune base réelle modifiée, aucun déploiement, aucun push distant.

---

## 1. Baseline vérifiée

| Élément | Valeur |
|---|---|
| Branche | `appmod/java-upgrade-20260409124418` |
| HEAD initial | `a446130` |
| JDK | Temurin 25.0.4 |
| Maven | 3.9.11, versions imposées par Enforcer |
| Tests initiaux | 177/177 |
| Tests actuels | **194/194** |
| Build package | **SUCCÈS** |
| Entrées locales préexistantes | 98, préservées ; aucun reset/stash pop |

Références : `BASELINE.md`, `BUILD_AND_CI.md`, `TEST_STRATEGY.md`.

---

## 2. Corrections effectivement livrées

### 2.1 Sécurité

- **C1** : clé Ollama absente du code courant ; résolution par `KAZISAFE_OLLAMA_API_KEY` / `SecretStore`. La clé historique reste dans l'historique Git : révocation externe à faire.
- **C2/H4** : `ManagedSessionFactory` sans `root`/`Admin*21` par défaut ; TLS MySQL `REQUIRED` par défaut ; politique centralisée `MySqlTls`.
- **H4 complément** : le dernier `useSSL=false` des outils MySQL directs a été supprimé.
- **Admin MySQL direct** : les outils de réplication exigent désormais un compte explicite (`KAZISAFE_MYSQL_ADMIN_USER`), plus de `root` implicite.
- **C3/C4/M6** : `UpdateVerifier` (nom, taille, SHA-256, signature Ed25519) ; exécution automatique désactivée tant que la clé publique n'est pas déployée.
- **H1/H2/M1** : jetons, mot de passe MySQL et clé IA migrés vers `SecretStore`/`TokenStore` chiffrés.
- **H3** : constantes Wi-Fi/HotSpot orphelines supprimées.
- **M2/M3/M4/M5** : Redis avec AUTH, `SqlGuard`, jetons admin à TTL, allowlist anti-SSRF (voir `SECURITY_REMEDIATION.md`).
- **Permissions SQLite** : répertoire 0700, base/WAL/SHM 0600 (`DatabaseFileSecurity`).
- **Garde-fou CI** : `scripts/security-regression.sh` interdit la réapparition de la clé historique, `Admin*21`, `useSSL=false` ou TrustAll.

### 2.2 HTTP / Retrofit

- Un seul canal applicatif Retrofit/OkHttp, TLS avec trust store système.
- Timeouts connect 30 s, read/write 5 min, call 10 min.
- `retryOnConnectionFailure(false)` : aucune vente/paiement rejoué implicitement.
- Refresh de jeton single-flight, reprise SSE après 401.
- Test de politique HTTP ajouté.

### 2.3 Synchronisation

- S1 : les `REMOVE/DELETE` sans payload ne sont plus supprimés de l'outbox.
- S3 : plafond de 10 retries puis `DEAD_LETTER`, mutation conservée et remise en file via `retryDeadLetters()` / reset global.
- Tests de non-régression.
- Restent ouverts : acquittement par entité au lieu de mutation, `mutationId` serveur, tests de reprise réels.

### 2.4 Inventaire physique

- B1 : attente du commit SQLite avant retour (`join`), test dédié.
- B3 (partiel) : comptages zéro avant passage à `Terminé` ; clôture rejouable.
- B5 (partiel) : verrou réentrant par inventaire (mono-processus).
- B6 (partiel) : `valide_par` / `valide_le` locaux, exclus du payload serveur ; modification d'un inventaire clôturé refusée.
- Sessions MySQL des écritures d'inventaire fermées via `executeWrite`.
- Restent ouverts : photographie figée du théorique, transaction de clôture, protection multi-instances, lots nuls/vides.

### 2.5 Build, CI et dépôt

- Java 25 / Maven 3.9 imposés par Enforcer.
- ControlsFX 8 et Jersey inutilisés supprimés ; JAX-RS Jakarta explicite.
- HikariCP aligné sur la propriété 5.1.0.
- `HttpSyncHandler` (1576 lignes mortes), `JpaUtil`, artefacts générés `org/`, `.codex`, etc. supprimés.
- Hook d'arrêt fermant l'`EntityManagerFactory`, le pool et la file d'écriture.
- CI GitHub Actions : build/tests, garde-fou secrets, SpotBugs/FindSecBugs (non bloquant, incompatible Java 25), Dependency-Check, Gitleaks.
- SpotBugs 4.8.6 ne sait pas analyser Java 25 : limitation documentée, outil à remplacer.

---

## 3. Tests

```text
mvn -q clean test -Dtests.skip=false
```

- 194 tests, 0 échec, 0 erreur, 22 suites.
- Tests ajoutés : MySQL TLS, politique HTTP, REMOVE outbox, dead-letter, inventaire (attente locale, garde clôture, verrou), permissions SQLite.

Angles morts : inventaire réel SQLite/MySQL, reprise sync après coupure, mise à jour signée de bout en bout, packaging Windows/Linux, couverture JavaFX.

---

## 4. Problèmes non corrigés / partiels

| Problème | Statut |
|---|---|
| Chiffrement SQLite SQLCipher | **Non corrigé** — migration manuelle, base en clair |
| Clôture d'inventaire atomique | **Partiel** — ordre corrigé, pas de transaction unique |
| Concurrence inventaire multi-instances | **Partiel** — verrou local seulement |
| Photographie du théorique à l'ouverture | **Non corrigé** |
| Lots nuls/vides à la clôture | **Non corrigé** |
| Acquittement sync par mutation | **Non corrigé** — API serveur requise |
| Tests de reprise sync | **Non corrigés** |
| Manifeste de mise à jour signé | **Non déployé** — serveur/clé requis |
| Rotation clé Ollama / mot de passe MySQL | **Action externe requise** |
| SpotBugs Java 25 | **Non opérationnel** |
| Packaging Windows/Linux | **Non automatisé** |
| God Classes / journalisation | **Partiel** — plan seulement |

---

## 5. Actions manuelles obligatoires

Voir `MANUAL_ACTIONS.md` :

1. révoquer/régénérer la clé Ollama compromise ;
2. faire tourner le mot de passe MySQL historique et créer un compte applicatif ;
3. configurer `KAZISAFE_MYSQL_ADMIN_USER` pour les outils de réplication ;
4. publier `manifest.json` + signature Ed25519 et intégrer la clé publique ;
5. décider et tester la migration SQLCipher (ou chiffrement disque OS) ;
6. sécuriser les Redis système préexistants sans AUTH.

---

## 6. Évolutions API serveur nécessaires

1. `mutationId` dans `BatchMutationDto` et les résultats, pour un acquittement exact.
2. Contrat explicite des `REMOVE` sans payload.
3. Champs `valide_par` / `valide_le` dans le DTO d'inventaire.
4. Endpoint de clôture idempotent ou clé d'idempotence.
5. Manifeste de mise à jour signé.

---

## 7. Conclusion

Le dépôt est plus sûr et plus fiable qu'à la baseline : secrets retirés, TLS durci, retries non idempotents désactivés, suppressions sync préservées, poison pills bornées, inventaire mieux ordonné et verrouillé, ressources mieux fermées, code mort supprimé, CI et garde-fous ajoutés. La suite de tests est verte.

La mission **n'est pas terminée** : le chiffrement réel de la base, la clôture transactionnelle, l'idempotence sync par mutation et les actions externes restent indispensables avant de considérer Kazisafex comme durablement sécurisé.
