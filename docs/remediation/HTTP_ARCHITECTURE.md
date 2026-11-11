# Architecture réseau/HTTP — Kazisafex (client desktop)

Document de référence produit après la campagne HTTP (remédiation 2026-11).
Objectif : décrire l'unique canal distant et ses garanties, pour éviter toute
réintroduction d'un canal parallèle.

## 1. Vue d'ensemble

```
UI JavaFX ──> services métier ──> Retrofit/OkHttp ──HTTPS──> cloud.kazisafe.com/v1 (API REST)
                                     │
                                     ├── SSE (launchdarkly EventSource) : notification/events
                                     └── Ollama (API IA, clé via SecretStore, pas de défaut)
Persistance locale : SQLite (Hibernate) — jamais exposée au réseau.
Serveur de réplication IA : MySQL direct (GratienTools) — outillé, pas applicatif.
```

**Règle d'or** : Retrofit/OkHttp est le seul canal applicatif. Tout ajout de
canal (socket brut, autre lib HTTP, driver JDBC distant applicatif) doit être
justifié et passer par la validation architecture (campagne 5).

## 2. Endpoints et constantes

| Usage | URL | Où |
|---|---|---|
| API REST | `https://cloud.kazisafe.com/v1/` | `KazisafeServiceFactory.BASE_URL` |
| SSE notifications | `BASE_URL + notification/events` | `MainuiController.connectSse()` |
| Réplication IA (MySQL direct) | jdbc:mysql (outils Gratien) | `GratienTools.mysqlJdbcUrl()` |
| Ollama | `AiAgents.DEFAULT_OLLAMA_BASE_URL` | clé API via SecretStore (C1) |
| Pages officielles (IA) | `https://endeleya.com/...` | allowlist `GratienTools.FETCH_ALLOWED_HOSTS` (M5) |
| Téléchargement MAJ | `https://cloud.kazisafe.com/download/<fichier>` | vérifié par `UpdateVerifier` (C4/M6) |

La constante `WEBSOCKET` (`wss://cloud.kazisafe.com`) est un vestige non
utilisé (le sync temps réel passe par SSE). À retirer en campagne 6.

## 3. Clients OkHttp

### 3.1 Client applicatif (`KazisafeServiceFactory.getRetrofitInstance`)
- TLS : `TLSSocketFactory` — protocoles limités TLSv1.2/1.3, **trust store
  système** (pas de TrustAll), certificats vérifiés.
- Timeouts : connect **30 s**, read **5 min**, write **5 min**, call
  **10 min** (plafond global). read/write longs car les lots de
  synchronisation peuvent être volumineux sur liaisons lentes.
- `retryOnConnectionFailure(false)` : **aucun retry implicite** d'un POST de
  vente, de paiement ou de régularisation. Une coupure après envoi peut avoir
  exécuté l'opération côté serveur ; le retry est pris en charge
  explicitement par l'outbox/idempotence, jamais par le transport.

### 3.2 Client de refresh (`getInstanceRefresh`)
- Sans intercepteur/authenticator (pas de boucle de refresh).
- Timeouts resserrés : 30 s / 30 s / 30 s, call 45 s.

## 4. Cycle du jeton d'authentification

1. Login → `LoginWebResult.token` → `MainuiController.token` +
   `TokenStore` (chiffré AES-GCM, cf. H1).
2. Chaque requête : `HttpInterceptor` pose `Authorization: Bearer <jeton>`.
3. **401** → `HttpInterceptor` : single-flight (verrou sur l'état + double
   check) → `TokenRefreshClient.refreshAccessToken` → si succès, une seule
   relance (`X-Kazi-Auth-Retry: 1`).
4. Si la relance reçoit encore 401 → `HttpAuthenticator` (OkHttp) :
   dernier recours, max 2 tentatives, puis abandon (`null`).
5. `TokenRefreshClient` : **single-flight global** (verrou statique +
   fenêtre de dédup 3 s) — deux 401 simultanés déclenchent un seul appel
   réseau ; échecs/mises à jour propagés via `TokenStore`.
6. Callback `OnTokenRefreshedListener` (`MainuiController.setLoginResult`)
   : met à jour le champ login, `TokenStore`, puis **relance le flux SSE**
   avec le jeton frais.

Jeton jamais loggé (corrigé campagne 2 — `HttpAuthenticator` loggait
ancien/nouveau jeton en INFO).

## 5. Flux temps réel (SSE)

`MainuiController.connectSse()` (ex-`sseSync` interne) :
- Headers : `Authorization` avec le jeton **courant** (`TokenStore.get()`
  d'abord — toujours frais après un refresh — champ login en repli).
- `EventSource.Builder` : `reconnectTime` 30 s (réseau), `readTimeout` 3 min
  (heartbeat serveur 15 s → 12 battements manqués = connexion morte ; évite
  les « sinks fantômes » côté Quarkus).
- **401** : `NotificationHandler.onError` ferme le flux, puis
  `schedule401Recovery()` : refresh du jeton puis relance via hook
  `setSseReconnectHook` — avec backoff (une tentative programmée max par
  minute, démarrage différé de 30 s). Le flux ne reste plus mort après
  expiration du jeton.
- `sseSync()` (portail) garde le contrôle d'abonnement (`go()`) ; les
  reconnexions ultérieures ne repassent **pas** par `go()` (pas de popup
  d'abonnement en boucle).

## 6. Sérialisation

`KazisafeServiceFactory.mapper()` (Jackson) :
- Dates-heures normalisées **UTC** (sérialiseur/désérialiseur dédiés,
  fractionnaire 0-9), dates locales `yyyy-MM-dd`.
- `FAIL_ON_UNKNOWN_PROPERTIES=false` (tolérance montée de version serveur).
- `NON_NULL` à la sérialisation.

## 7. Historique de la campagne 2 (corrections)

| Problème | Correction |
|---|---|
| `connectTimeout` 300 s (serveur injoignable = 5 min de blocage UI/sync) | 30 s |
| Aucun `callTimeout` (appel infini possible) | 10 min client applicatif, 45 s refresh |
| Refresh concurrent non protégé côté `HttpAuthenticator` (plusieurs 401 → plusieurs refresh réseau, risque de révocation mutuelle) | Single-flight global dans `TokenRefreshClient` (verrou + dédup 3 s) |
| Jetons d'accès loggés en INFO par `HttpAuthenticator` | Log neutre |
| SSE fermé définitivement sur 401 (jeton expiré) sans relance | Refresh + relance via hook, backoff 60 s |
| SSE démarré avec le jeton de login capturé une fois | Lecture du jeton courant à chaque (re)connexion |
