# RAPPORT FINAL D'ÉTAPE — Kazisafex

**Date** : 11 novembre 2026  
**Statut global** : **mission en cours**, pas de clôture définitive.  
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
| Tests actuels | **218/218** |
| Build package | **SUCCÈS** |
| Entrées locales préexistantes | préservées ; aucun reset/stash pop |

Références : `BASELINE.md`, `BUILD_AND_CI.md`, `TEST_STRATEGY.md`.

---

## 2. Corrections effectivement livrées

### 2.1 Sécurité

- **C1** : clé Ollama absente du code courant ; résolution par
  `KAZISAFE_OLLAMA_API_KEY` / `SecretStore`. La clé historique reste dans
  l'historique Git : révocation externe à faire.
- **C2/H4** : `ManagedSessionFactory` sans `root`/`Admin*21` par défaut ; TLS
  MySQL `REQUIRED` par défaut ; politique centralisée `MySqlTls`.
- **Admin MySQL direct** : compte explicite
  (`KAZISAFE_MYSQL_ADMIN_USER`), plus de `root` implicite.
- **C3/C4/M6** : `UpdateVerifier` (nom, taille, SHA-256, signature Ed25519) ;
  vérification revalidée à chaque application ; exécution automatique
  désactivée tant que la clé publique n'est pas déployée.
- **H1/H2/M1** : jetons, mot de passe MySQL et clé IA dans
  `SecretStore`/`TokenStore` chiffrés.
- **H3** : constantes Wi-Fi/HotSpot orphelines supprimées.
- **M2–M5** : Redis avec AUTH, `SqlGuard`, jetons admin à TTL, allowlist
  anti-SSRF.
- **Permissions SQLite** : répertoire 0700, base/WAL/SHM 0600.
- **Désérialisation** : `ImageProductDecoder` supprimé ; garde-fou CI contre
  `new ObjectInputStream` / `XMLDecoder`.
- **Garde-fou CI** : `scripts/security-regression.sh` interdit la
  réapparition de la clé historique, `Admin*21`, `useSSL=false`, TrustAll et
  désérialisation Java non sûre.

### 2.2 HTTP / Retrofit

- Un seul canal applicatif Retrofit/OkHttp, TLS avec trust store système.
- Timeouts connect 30 s, read/write 5 min, call 10 min.
- `retryOnConnectionFailure(false)` : aucune vente/paiement rejoué
  implicitement.
- Refresh de jeton single-flight, reprise SSE après 401.
- Timeouts ajoutés aux téléchargements de mise à jour (`URLConnection`).
- Tests de politique HTTP ajoutés.

### 2.3 Persistance offline-first et synchronisation

- **Outbox atomique** : `SyncOutboxListener` écrit désormais dans
  l'EntityManager de la transaction métier courante ; le commit métier et
  l'outbox ne peuvent plus diverger.
- Repli asynchrone conservé uniquement hors transaction managée.
- `S1` : les `REMOVE/DELETE` sans payload ne sont plus supprimés de
  l'outbox.
- `S3` : plafond de 10 retries puis `DEAD_LETTER`, mutation conservée,
  remise en file via `retryDeadLetters()` / reset global.
- Coalescence des mutations **strictement identiques** avant envoi ;
  acquittement des copies uniquement si la mutation conservée a réussi.
- Sauvegarde SQLite avant migration Hibernate (une par jour) et avant
  clôture d'inventaire, avec empreinte SHA-256, vérification d'intégrité et
  script de restauration manuel protégé par confirmation.
- Tests : atomicité outbox, coalescence, dead-letter, REMOVE, transaction.

Restent ouverts : acquittement par mutation (API serveur), `mutationId`,
tests de reprise après coupure réelle.

### 2.4 Inventaire physique

- `B1` : attente du commit SQLite avant retour (`join`), test dédié.
- `B3` : comptages zéro avant passage à `Terminé` ; clôture rejouable ;
  sauvegarde immédiate avant ajustements (la clôture reste une saga non
  transactionnelle).
- `B5` : verrou réentrant par inventaire, mono-processus.
- `B6` : `valide_par` / `valide_le` locaux, exclus du payload serveur ;
  modification d'un inventaire clôturé refusée.
- Validation des quantités : refus des valeurs négatives, NaN/infinies et
  des produits/inventaires absents avant écriture.
- Référence de stock à la date d'ouverture (`theoreticalStockAtOpening`) et
  fallback explicite.
- Sessions MySQL des écritures d'inventaire fermées via `executeWrite`.

Restent ouverts : transaction unique de clôture, protection multi-instances,
lots nuls/vides, photographie figée complète du théorique.

### 2.5 Build, CI et dette technique

- Java 25 / Maven 3.9 imposés par Enforcer.
- ControlsFX 8 et Jersey inutilisés supprimés ; JAX-RS Jakarta explicite.
- HikariCP aligné sur la propriété 5.1.0.
- Canal WebSocket mort supprimé : `SyncEndpoint`, `WsClientEndpoint`,
  `BaseModelDecoder`, 25 encodeurs et dépendances `jakarta.websocket`.
- `HttpSyncHandler` (1576 lignes mortes), `JpaUtil`, artefacts générés
  supprimés.
- `ThreadSafeSimpleDateFormat` : formateurs partagés synchronisés, typés
  explicitement dans `Constants`.
- Hook d'arrêt fermant l'`EntityManagerFactory`, le pool et la file
  d'écriture.
- CI GitHub Actions : build/tests, garde-fou secrets, SpotBugs 4.10.5
  (`effort=Default`, `maxHeap=2048`, non bloquant), Dependency-Check,
  Gitleaks.
- Rapport SpotBugs produit : environ 2 771 findings, dont la majorité relève
  du style/performance ; les bugs de correction réels identifiés ont été
  corrigés.

---

## 3. Tests et vérifications manuelles

```text
mvn -q clean test -Dtests.skip=false
```

- **218 tests, 0 échec, 0 erreur, 22 suites.**
- Tests ajoutés : MySQL TLS, politique HTTP, REMOVE outbox, dead-letter,
  atomicité outbox, coalescence, inventaire (attente locale, garde clôture,
  verrou, validation), permissions SQLite, sauvegardes, dates thread-safe.

**Test JavaFX réel** :

- lancement avec base SQLite isolée et données factices
  (`Entreprise Factice`, `Client Factice`) ;
- dashboard et POS chargés ;
- smoke test après suppression du WebSocket ;
- nouveau smoke test après atomicité outbox avec préférences utilisateur
  isolées (`Entreprise Atomique`) ;
- bypass temporaire supprimé, bases/préférences de test supprimées, jar final
  reconstruit sans bypass.

Angles morts : inventaire réel de bout en bout SQLite/MySQL, reprise sync
après coupure, mise à jour signée de bout en bout, packaging Windows/Linux,
couverture JavaFX.

---

## 4. Problèmes non corrigés / partiels

| Problème | Statut |
|---|---|
| Chiffrement SQLite SQLCipher | **Non corrigé** — migration manuelle, base en clair |
| Clôture d'inventaire atomique | **Partiel** — saga ordonnée + sauvegarde, pas de transaction unique |
| Concurrence inventaire multi-instances | **Partiel** — verrou local seulement |
| Photographie figée du théorique | **Partiel** — référence date d'ouverture, pas de snapshot immuable |
| Lots nuls/vides à la clôture | **Non corrigé** |
| Acquittement sync par mutation | **Non corrigé** — API serveur requise |
| Tests de reprise sync réels | **Non corrigés** |
| Manifeste de mise à jour signé | **Non déployé** — serveur/clé requis |
| Rotation clé Ollama / mot de passe MySQL | **Action externe requise** |
| SpotBugs | **Opérationnel**, findings historiques à trier |
| Packaging Windows/Linux | **Image Linux testée** ; Windows non testé |
| God Classes / journalisation | **Partiel** — plan seulement |

---

## 5. Actions manuelles obligatoires

Voir `MANUAL_ACTIONS.md` :

1. révoquer/régénérer la clé Ollama compromise ;
2. faire tourner le mot de passe MySQL historique et créer un compte
   applicatif ;
3. configurer `KAZISAFE_MYSQL_ADMIN_USER` pour les outils de réplication ;
4. publier `manifest.json` + signature Ed25519 et intégrer la clé publique ;
5. décider et tester la migration SQLCipher (ou chiffrement disque OS) ;
6. sécuriser les Redis système préexistants sans AUTH.

---

## 6. Évolutions API serveur nécessaires

1. `mutationId` dans `BatchMutationDto` et les résultats, pour un
   acquittement exact par mutation.
2. Contrat explicite des `REMOVE` sans payload.
3. Champs `valide_par` / `valide_le` dans le DTO d'inventaire.
4. Endpoint de clôture idempotent ou clé d'idempotence.
5. Manifeste de mise à jour signé.

---

## 7. Conclusion

Le dépôt est plus sûr et plus fiable qu'à la baseline : secrets retirés, TLS
durci, retries non idempotents désactivés, outbox atomique avec le métier,
doublons maîtrisés, suppressions sync préservées, poison pills bornées,
inventaire mieux ordonné/validé/verrouillé et sauvegardé avant clôture,
ressources mieux fermées, code mort WebSocket supprimé, CI et garde-fous
ajoutés. La suite de tests est verte.

La mission **n'est pas terminée** : le chiffrement réel de la base, la clôture
transactionnelle, la photographie immuable du stock, l'idempotence sync par
mutation et les actions externes restent indispensables avant de considérer
Kazisafex comme durablement sécurisé.
