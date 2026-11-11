# FIABILITÉ DE LA SYNCHRONISATION — Kazisafex

**Date** : 11 novembre 2026  
**Périmètre** : outbox locale, upsync adaptatif, downsync, SSE, reprise, idempotence.  
**Méthode** : lecture directe du code + exécution des tests existants. Aucune base réelle n'a été modifiée.

---

## 1. Architecture constatée

```text
Écriture métier JPA
   └─ SyncOutboxListener
        └─ sync_outbox (PENDING / UNSYNCED / FAILED / APPLIED / DOWNSYNCED)
              ├─ BackgroundSyncService (cycle ~30 s)
              │     └─ POST sync/adaptive/upsync (lots 5..200)
              └─ GET sync/outbox/missed
                    └─ DOWNSYNCED
                          └─ materializeDownsyncRecords()
                                ├─ phases de dépendance 0..5
                                ├─ merge local (UPSERT par uid)
                                └─ REMOVE par find + remove
SSE notification/events ──> NotificationHandler (matérialisation temps réel)
Ventes ──> canal dédié VenteDtoSyncer
Divergence ──> StateValidationService / FullResyncService
```

### États de l'outbox

| État | Signification |
|---|---|
| `PENDING` | Mutation locale à envoyer |
| `UNSYNCED` | Envoi tenté, à réessayer ; `retryCount` informatif |
| `FAILED` | Résidu historique ; re-inclus dans les envois |
| `APPLIED` | Acquittée par le serveur |
| `DOWNSYNCED` | Mutation provenant du serveur, à matérialiser localement |

---

## 2. Défauts confirmés

### S1 — Suppressions distantes `REMOVE` sans payload perdues (corrigé)

`SyncOutboxService.deleteNullPayloadRecords()` supprimait **toutes** les lignes sans payload, y compris `REMOVE`/`DELETE`, alors que `applyDownsyncMutation` sait traiter un `REMOVE` sans payload par identifiant. Une suppression distante pouvait donc disparaître avant matérialisation.

**Correction appliquée** : la purge exclut désormais `REMOVE`/`DELETE`.

**Test ajouté** : `SyncOutboxServiceTest.testDeleteNullPayloadKeepsRemoveMutations`.

### S2 — Acquittement par entité, pas par mutation

`processUpsyncChunk` marque `APPLIED` toutes les lignes dont `(entityId, tableName)` figurent dans les succès du serveur. Si le même lot contient deux mutations successives de la même entité, elles peuvent être acquittées ensemble alors qu'une seule a été appliquée.

**Risque** : mutation locale perdue silencieusement.  
**Cause** : le DTO `BatchMutationDto` ne transporte pas l'UID d'outbox.  
**Statut** : **ouvert** — évolution API serveur nécessaire (identifiant de mutation).

### S3 — `FAILED` sans plafond ni dead-letter

`fetchAllPendingOutbox` inclut `PENDING`, `UNSYNCED` et `FAILED` sans limite d'âge ou de tentatives. Une mutation définitivement invalide (poison pill) reste éligible indéfiniment ; le backoff évite la boucle serrée mais pas l'envoi perpétuel.

**Risque** : bruit réseau, impossibilité de distinguer un rejet définitif d'une panne temporaire.  
**Statut** : **ouvert** — prévoir un état `DEAD_LETTER` avec action manuelle de rejeu, sans supprimer la donnée.

### S4 — Atomicité écriture métier / outbox

Le listener JPA doit écrire l'outbox dans la même transaction que l'entité. Ce point doit être vérifié sur chaque type d'écriture (surtout SQLite `WriteQueueManager` et MySQL `submitWrite`).

**Statut** : **à confirmer par test d'intégration** ; pas de test de coupure entre l'écriture métier et l'écriture outbox.

### S5 — Reprise après interruption non testée de bout en bout

Aucun test automatisé ne couvre :

- arrêt brutal entre l'écriture métier et l'outbox ;
- coupure réseau après envoi mais avant réponse ;
- redémarrage avec outbox non vide ;
- réponse HTTP perdue ;
- deux workers concurrents ;
- matérialisation partielle et reprise.

**Statut** : **ouvert**.

### S6 — SSE et curseur

Le flux SSE et le curseur `lastAppliedMutationTs` existent ; des correctifs antérieurs concernent le 401 et la reprise. Aucun test de coupure SSE prolongée + reconnexion + rattrapage n'est présent.

**Statut** : **partiel**.

---

## 3. Points positifs vérifiés

- Ordre de matérialisation parent → enfant (phases 0..5) testé.
- Idempotence par UPSERT (`merge`) testée.
- `REMOVE` sans payload est appliqué par `find` + `remove` (test existant).
- Payload sans uid ignoré pour éviter les doublons.
- Upsync par lots adaptatifs, gestion 429, backoff, verrou de cycle.
- Les tests `SyncOutboxServiceTest` couvrent l'UPSERT et la matérialisation.

---

## 4. Tests exécutés

```text
mvn test -Dtests.skip=false -Dtest=SyncOutboxServiceTest,MySqlTlsTest
```

Résultat : **succès**. Le test de préservation des `REMOVE` est vert.

---

## 5. Corrections recommandées

### Priorité 1 — sans changement serveur

1. Ajouter un état `DEAD_LETTER` (ou `FAILED_PERMANENT`) après un nombre borné de tentatives, avec conservation de la mutation et bouton de rejeu.
2. Ajouter des tests de reprise avec une base SQLite temporaire réelle :
   - outbox non vide au démarrage ;
   - coupure entre écriture métier et outbox ;
   - relance après réponse perdue.
3. Vérifier l'atomicité de l'écriture métier + outbox pour chaque listener.

### Priorité 2 — nécessite le serveur

4. Ajouter un `mutationId` (UID outbox) dans `BatchMutationDto` et dans les succès/échecs serveur ; acquitter par mutation, pas par entité.
5. Rendre l'upsync idempotent côté serveur sur ce `mutationId`.
6. Confirmer le contrat des suppressions distantes (payload null autorisé).

### Priorité 3 — observabilité

7. Exposer des compteurs : outbox par statut, âge de la plus ancienne mutation, retries, dead-letter.
8. Alerter au-delà d'un seuil d'ancienneté.

---

## 6. Évolutions API serveur nécessaires

| Évolution | Raison |
|---|---|
| `mutationId` dans le lot et dans les résultats | acquittement exact, idempotence |
| Statut/raison d'échec par mutation | éviter les rejets silencieux |
| Contrat explicite `REMOVE` sans payload | suppressions fiables |
| Endpoint de rejeu dead-letter (optionnel) | exploitation support |

---

## 7. Conclusion

La synchronisation possède une base solide (outbox, phases, UPSERT, tests). La correction S1 supprime une perte réelle de suppression distante. Les risques majeurs restants sont l'acquittement par entité (S2), l'absence de dead-letter (S3) et l'absence de tests de reprise réels (S5) ; ils nécessitent des évolutions serveur ou des tests d'intégration supplémentaires et ne doivent pas être corrigés par une suppression de données.
