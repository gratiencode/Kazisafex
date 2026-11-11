# Actions manuelles requises (hors code)

> Ces actions nécessitent un humain ou un accès à des systèmes externes
> (serveurs, comptes, environnements). Elles sont **obligatoires** pour
> compléter la remédiation. Chaque action indique son urgence, sa procédure
> et son rollback.

## 1. Révoquer et régénérer la clé API Ollama (C1) — URGENT

La clé `a6514fe5e04c41b0af8cfa983c0792be…` est présente dans l'historique
git public/interne (`AiAgents.java` historique). Elle est considérée
compromise.

**Procédure**
1. Révoquer la clé sur https://ollama.com/settings/keys (ou la console
   d'administration du service utilisé).
2. Générer une nouvelle clé.
3. La distribuer aux postes : variable d'environnement
   `KAZISAFE_OLLAMA_API_KEY` (déploiements gérés) **ou** saisie unique dans
   l'UI (Paramètres → assistant Gratien), stockée chiffrée via SecretStore.

**Rollback** : si la clé est partagée par d'autres outils, la révocation peut
casser ces outils — vérifier les usages avant.

## 2. MySQL : rotation du mot de passe root et compte applicatif (C2/H4) — URGENT

Le mot de passe historique `Admin*21` (défaut du code, utilisé par les
déploiements existants dont la machine de développement) est connu de
quiconque a accès au dépôt.

**Procédure**
1. Côté serveur MySQL : `ALTER USER 'root'@'%' IDENTIFIED BY '<nouveau-mot-de-passe-fort>';`
   (et idéalement créer un compte applicatif dédié `kazisafe_app` avec
   droits limités à `ksf_*`).
2. Côté chaque client : Paramètres → saisir le nouveau mot de passe (stocké
   chiffré) **ou** définir `KAZISAFE_DB_PASSWORD` / `KAZISAFE_DB_USER`.
3. Pour exiger la vérification du certificat serveur : préparer le serveur
   avec TLS (certificat valide) puis définir la pref
   `default_mysql_ssl_mode=VERIFY_CA` (ou `VERIFY_IDENTITY`).

**Rollback** : rétablir l'ancien mot de passe côté serveur et ressaisir la
valeur côté client.

> Note machine de développement : la config explicite (user + mot de passe
> legacy) a été écrite dans les prefs pendant la campagne 1 — l'application
> et les tests continuent de fonctionner. La rotation ci-dessus reste à
> faire pour cette machine aussi.

## 3. Publier un manifeste de mise à jour signé (C3/C4) — IMPORTANT

L'installation automatique des mises à jour est désactivée tant que le
serveur ne publie pas de manifeste signé.

**Procédure (côté serveur, https://cloud.kazisafe.com/download/)**
1. Générer une paire de clés Ed25519 (une seule fois, garder la clé privée
   hors du dépôt, dans le secret manager du serveur) :
   ```bash
   openssl genpkey -algorithm ed25519 -out update_signing_key.pem
   openssl pkey -in update_signing_key.pem -pubout -out update_signing_pub.pem
   # Clé publique X509/SPKI en Base64 (une seule ligne) :
   openssl pkey -in update_signing_key.pem -pubout -outform DER | base64 -w0
   ```
2. À chaque publication d'une mise à jour, générer
   `manifest.json` + `manifest.json.sig` :
   ```json
   [
     {"name":"Kazisafex.exe","size":12345678,
      "sha256":"<sha256 hex du fichier>"},
     {"name":"Kazisafe-Linux.zip","size":...,"sha256":"..."},
     {"name":"Kazisafe-MacOS.zip","size":...,"sha256":"..."}
   ]
   ```
   ```bash
   openssl pkeyutl -sign -inkey update_signing_key.pem \
     -rawin -in manifest.json -out manifest.json.sig
   ```
3. Côté client : intégrer la clé publique Base64 dans
   `tools/UpdateVerifier.ED25519_PUBLIC_KEY_BASE64`, reconstruire, publier.
   L'installation automatique au redémarrage se réactive d'elle-même.

**Rollback** : vider la constante pour revenir au mode « non vérifié ».

## 4. Chiffrement au repos de la base SQLite embarquée (H5) — IMPORTANT

Le « SQLCipher » historique était inopérant : **la base locale des postes
n'est pas chiffrée**. Le code mort a été supprimé ; le chiffrement réel
nécessite une migration coordonnée.

**Procédure (par poste, ou à intégrer dans l'installateur)**
1. Ajouter le driver `org.xerial:sqlite-jdbc` remplacé par une build
   SQLCipher (ex. `io.github.ronickg:sqlite-jdbc` ou build custom avec
   l'extension `cipher`).
2. À la première ouverture : `PRAGMA key = '<local_db_secret>'` (la valeur
   est déjà générée/aléatoire et stockée chiffrée via SecretStore sous la
   clé `local_db_secret`) avant toute autre opération.
3. Migrer les bases existantes :
   ```sql
   ATTACH DATABASE 'nouvelle.db' AS encrypted KEY '<secret>';
   SELECT sqlcipher_export('encrypted');
   DETACH DATABASE encrypted;
   ```
4. Tester sur une copie de base avant déploiement général.

**Rollback** : conserver l'ancienne base non chiffrée tant que la migration
n'est pas validée (le driver vanilla continue de fonctionner en lecture sur
une base non chiffrée).

## 5. Redis local système sans AUTH (M2) — MOYEN

Les instances **démarrées par Kazisafex** imposent désormais un mot de passe
aléatoire. Les Redis installés comme **service système** (apt/homebrew)
peuvent rester sans AUTH.

**Procédure** (postes concernés uniquement) :
```bash
# Générer un mot de passe et l'appliquer :
redis-cli CONFIG SET requirepass "<mot-de-passe-aléatoire>"
# Le rendre persistant dans le fichier de config redis (requirepass ...).
```
Puis définir `REDIS_PASSWORD` (env) sur ces postes — `RedisMemoryStore`
l'utilise automatiquement.

**Rollback** : `redis-cli -a '<mot-de-passe>' CONFIG SET requirepass ""`
si d'autres outils locaux dépendent du Redis sans AUTH.

## 6. Passphrase device pour déploiements gérés (M1) — OPTIONNEL

Pour les postes gérés par MDM/Ansible, définir `KAZISAFE_DEVICE_PASSPHRASE`
de façon cohérente si les secrets doivent être lisibles par profil
synchronisé. Sinon, ne rien faire : chaque poste génère sa passphrase dans
`~/.kazisafe/device.key` (0600).

## 7. Divers (suivi campagne 6)

- `TokenBudget` (`tsub`) chiffre un simple plafond de tokens avec une
  passphrase faible (eUid) : faible sensibilité, reprise en campagne 6.
- `GratienTools.mysqlJdbcUrl` construit encore des URL JDBC avec
  `useSSL=false` pour des outils de réplication MySQL pilotés par l'IA :
  reprise en campagne 4/6 avec les mêmes règles que C2/H4.

## 8. Compte administrateur MySQL des outils de réplication (C2/H4) — REQUIS

Les outils IA de configuration/test de réplication MySQL ne se connectent plus
avec `root` implicitement. Avant utilisation :

1. Créer un compte administrateur MySQL dédié avec les privilèges minimaux
   nécessaires à la réplication.
2. Le déclarer sur le poste :
   - `KAZISAFE_MYSQL_ADMIN_USER=<compte>` (variable d'environnement), ou
   - préférence `default_mysql_admin_user`.

Le mot de passe reste demandé explicitement à l'utilisateur lors de l'appel.

Sans cette configuration, l'outil renvoie un message explicite et ne tente
aucune connexion.
