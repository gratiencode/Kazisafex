# Prompt pour l'Agent Éditeur de Code Client (JavaFX / Android)

Tu es un agent expert en développement Java (JavaFX / Android). Ta tâche est d'implémenter le module de synchronisation locale (SQLite/H2) pour consommer l'architecture de synchronisation bidirectionnelle du backend Kazisafe.

Voici les spécifications détaillées de l'architecture backend, des politiques de synchronisation, et du comportement que le client doit implémenter.

---

## 1. Architecture Globale de Synchronisation

La synchronisation repose sur deux flux distincts :
1. **Upsync (Client → Serveur)** : Envoi par lots (batches) hétérogènes de mutations locales via des requêtes HTTP POST adaptatives.
2. **Downsync (Serveur → Client)** : Réception en temps réel des mutations distantes via des canaux asynchrones (SSE pour Android, WebSocket pour JavaFX), avec un mécanisme de rattrapage (missed events) par polling HTTP lors des reconnexions.

---

## 2. Spécification de l'Upsync (Client → Serveur)

Le client doit maintenir une table locale nommée **`local_outbox`** contenant toutes les mutations en attente d'envoi. Chaque ligne contient :
* `entityId` (UUID de l'entité)
* `entityType` (ex: CATEGORY, PRODUIT, VENTE, LIGNEVENTE...)
* `payload` (JSON plat de l'entité)
* `updatedAt` (Timestamp local de modification)
* `mutationType` (INSERT | UPDATE | DELETE)

### A. Tri Hiérarchique Impératif avant Envoi
Avant d'envoyer un lot de mutations, le client **DOIT** les trier localement selon l'ordre de dépendance des tables pour éviter les violations de clés étrangères sur le serveur PostgreSQL.
Voici les niveaux de dépendance (`DEPENDENCY_LEVEL`) à respecter (les niveaux inférieurs d'abord) :
* **Niveau 0** : `ENTREPRISE`, `USER`, `TAXE`, `MODULE`, `REFRESH`
* **Niveau 1** : `CATEGORY`, `FOURNISSEUR`, `CLIENT`, `COMPTETRESOR`, `DEPOT`, `MATIERE`, `PERMISSION`, `ROLE`, `DEPENSE`, `PRESENCE`, `USER_PREFERENCES`
* **Niveau 2** : `PRODUIT`, `MATIERESKU`, `ENGAGER`, `ABONNEMENT`, `BULKMODEL`
* **Niveau 3** : `MESURE`, `COMMANDE`
* **Niveau 4** : `LIVRAISON`, `PRODUCTION`, `RECQUISITION`, `VENTE`, `AFFECTER`, `CLIENTORGANISATION`, `CLIENTAPPARTENIR`, `INVENTORY`
* **Niveau 5** : `STOCKER`, `DESTOCKER`, `LIGNEVENTE`, `TRAISORERIE`, `ENTREPOSER`, `REPARTIR`, `TAXER`, `COMPTER`, `COMMANDELISTE`, `ARETIRER`, `RETOURMAGASIN`, `RETOURDEPOT`, `PRIXDEVENTE`
* **Niveau 6** : `OPERATION`, `IMPUTER`, `FACTURE`
* **Niveau 7** : `INDUSTRIAL_STOCK_AGREGATE`

*Règle de tri* : Trier par `DEPENDENCY_LEVEL` croissant, puis par `updatedAt` croissant pour préserver la chronologie.

### B. Contrôle Adaptatif de Charge et Backpressure (HTTP 429)
Le client doit envoyer ses mutations à l'endpoint :
`POST /api/sync/adaptive/upsync`

Le serveur calcule dynamiquement ses limites en fonction de sa mémoire JVM libre. Le client doit s'y adapter de la façon suivante :
1. **Traitement du HTTP 429 (Too Many Requests)** :
   * Si rejeté avec un header `Retry-With-Batch-Size : <size>`, réduire immédiatement la taille maximale de lot à cette valeur pour les prochains envois.
   * Si rejeté en raison de requêtes parallèles trop nombreuses, suspendre temporairement les envois parallèles (attendre un délai exponentiel avant réessai).
2. **Lecture des Headers HTTP de Succès (HTTP 200 / 202)** :
   * Lire le header `Sync-Max-Parallel` : Ajuster dynamiquement le nombre maximal de requêtes HTTP parallèles envoyées simultanément par le client.
   * Lire le header `Sync-Mode` (`HIGH` | `MEDIUM` | `SURVIVAL`) pour adapter la fréquence d'envoi local.

### C. Gestion des Résultats de l'Upsync
L'endpoint retourne un objet JSON de type `BatchResult` :
```json
{
  "successes": [
    { "entityId": "uuid", "entityType": "PRODUIT" }
  ],
  "failures": [
    { "entityId": "uuid", "entityType": "VENTE", "reason": "Détail de l'erreur" }
  ]
}
```
*   **Pour chaque succès** : Supprimer la mutation de la table `local_outbox`.
*   **Pour chaque échec** : Marquer la ligne locale comme "bloquée" ou avec une incrémentation du compteur de tentatives (`retryCount`). Ne pas bloquer indéfiniment la file si l'erreur est fatale (ex: violation de contrainte métier) ; journaliser l'erreur pour analyse ou intervention de l'utilisateur.

---

## 3. Spécification du Downsync (Serveur → Client)

Le serveur écrit automatiquement chaque modification (INSERT, UPDATE, DELETE) effectuée sur le serveur dans une table `sync_outbox` (via un JPA Listener) et la diffuse en temps réel.

### A. Protocoles de Streaming
*   **Android (Java)** : S'abonner à l'endpoint SSE :
    `GET /v1/sync/outbox/stream`
    *   Implémenter un heartbeat (ping du serveur reçu toutes les 15 secondes).
    *   Gérer la reconnexion automatique avec un backoff exponentiel en cas de déconnexion.
*   **JavaFX (Java)** : Se connecter à l'endpoint WebSocket :
    `ws://<host>/sync/{engagerId}`
    *   Permet d'envoyer des événements en local vers le serveur et de recevoir instantanément les mises à jour des autres clients.
    *   Gérer les pings/pongs réguliers et la reconnexion automatique.

### B. Traitement des Payloads Reçus
Chaque événement reçu (SSE ou WebSocket) contient une mutation :
```json
{
  "entityType": "PRODUIT",
  "entityId": "uuid",
  "mutationType": "INSERT|UPDATE|DELETE",
  "payload": "JSON de l'entité",
  "entrepriseId": "euid",
  "region": "region",
  "mutationTs": 1719544000000
}
```
1. **Éviter les Boucles Infinies (Echo Loop)** :
   * Si le client reçoit une mutation concernant un `entityId` qu'il a lui-même généré/modifié récemment et qui est toujours en cours de traitement local, il doit l'ignorer pour éviter d'écraser des données locales plus récentes.
2. **Écriture Locale Multi-Threadée fluide** :
   * Désérialiser le payload. Les clés étrangères dans le payload sont sous forme de références simplifiées `{ "uid": "..." }`. Le client doit résoudre ces références lors de l'insertion dans sa base locale.
   * Effectuer l'écriture locale dans un thread d'arrière-plan dédié pour ne pas bloquer l'interface utilisateur.
   * **Pour JavaFX** : Une fois la persistance locale confirmée, notifier le Thread d'Application JavaFX (`Platform.runLater()`) pour mettre à jour la vue.

### C. Rattrapage lors de la Reconnexion (Missed Events)
Le client doit enregistrer localement dans ses préférences le timestamp de la dernière mutation appliquée (`lastAppliedMutationTs`).
Lors d'une reconnexion (après une perte de réseau) ou au démarrage :
1. Envoyer une requête HTTP :
   `GET /v1/sync/outbox/missed?since={lastAppliedMutationTs}`
2. Le serveur renvoie la liste ordonnée des mutations manquées.
3. Appliquer ces mutations localement de manière séquentielle dans l'ordre chronologique (`mutationTs` croissant).
4. Seulement après avoir traité tout le lot de rattrapage, activer le flux de streaming temps réel (SSE / WebSocket).

---

## 4. Politique de Validation Globale (Anti-Dérive)

Pour s'assurer que les données locales n'ont pas divergé de la base de données centrale (perte d'événements, crash client, etc.) :
1. **Endpoint de hashage** :
   `GET /api/sync/validation/hash`
   Ce endpoint retourne un hash calculé côté serveur représentant l'état des données pour l'entreprise et la région de l'utilisateur.
2. **Comportement du Client** :
   * Calculer périodiquement (ex: toutes les heures ou à chaque démarrage) le hash équivalent sur les données locales.
   * Si les hashs ne correspondent pas :
     * Interroger `GET /api/sync/validation/counts` pour comparer le nombre brut de catégories et de produits.
     * Si une anomalie majeure est détectée, déclencher une procédure de resynchronisation complète (téléchargement de tous les enregistrements depuis les ressources REST d'entités spécifiques, puis remplacement des tables locales).

---

## Directives d'Implémentation pour l'Agent

1. **Robustesse et Threads** : Sépare strictement les appels réseau et les accès DB locaux des threads UI (JavaFX et Android). Utilise des pools de threads (ou Executors) dédiés.
2. **Gestion de la base locale** :
   * Gère les relations entre entités en extrayant l'UID des objets imbriqués.
   * Implémente le mécanisme d'upsert local (insérer ou mettre à jour si existe déjà en vérifiant le timestamp de modification).
3. **Journalisation et Diagnostic** : Fournis des logs détaillés avec des tags clairs (ex: `[SYNC-UPSYNC]`, `[SYNC-STREAM]`, `[SYNC-CATCHUP]`) pour faciliter le débogage en production.
4. **Offline First** : Le client doit continuer de fonctionner pleinement en mode déconnecté. L'outbox locale doit être persistante et survivre aux redémarrages de l'application.
