package data.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.helpers.Token;
import data.helpers.TokenRefreshed;
import data.network.Kazisafe;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.ResponseBody;
import retrofit2.Response;

public final class TokenRefreshClient {

    private static final Logger LOGGER = Logger.getLogger(TokenRefreshClient.class.getName());
    private static final ObjectMapper MAPPER = KazisafeServiceFactory.mapper();
    private static final long REFRESH_DEDUPE_MS = 3_000L;
    private static final Object REFRESH_LOCK = new Object();
    private static volatile String lastSourceToken;
    private static volatile String lastFreshToken;
    private static volatile long lastRefreshAtMs;

    public String refreshAccessToken(String currentToken) {
        String normalized = normalize(currentToken);
        if (normalized == null || normalized.isBlank()) {
            return null;
        }
        synchronized (REFRESH_LOCK) {
            if (normalized.equals(lastSourceToken)
                    && System.currentTimeMillis() - lastRefreshAtMs < REFRESH_DEDUPE_MS) {
                LOGGER.fine("Reusing token refresh result from concurrent call");
                return lastFreshToken;
            }
            String fresh = doRefresh(normalized);
            lastSourceToken = normalized;
            lastFreshToken = fresh;
            lastRefreshAtMs = System.currentTimeMillis();
            return fresh;
        }
    }

    private String doRefresh(String normalized) {
        try {
            Kazisafe apiService = KazisafeServiceFactory.getInstanceRefresh().create(Kazisafe.class);
            Token token = new Token();
            token.setToken(normalized);
            LOGGER.log(Level.INFO, "Refreshing access token after unauthorized response");

            Response<TokenRefreshed> response = apiService.refreshTokenWithHeader("Bearer " + normalized, token).execute();
            if (!response.isSuccessful()) {
                // Fallback for backends that only read token from the request body
                response = apiService.refreshToken(token).execute();
            }
            if (!response.isSuccessful()) {
                System.out.println("reponse refresh non 200 "+response);
                LOGGER.log(Level.WARNING, "Token refresh rejected: HTTP {0}", response.code());
                return null;
            }
            TokenRefreshed body = response.body();
            if (body == null) {
                LOGGER.warning("Token refresh returned empty body");
                return null;
            }
            String freshToken = extractToken(body);
            if (freshToken != null && !freshToken.isBlank()) {
                TokenStore.store(freshToken);
                return freshToken;
            }
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, "Token refresh network failure", ex);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Token refresh failure", ex);
        }
        return null;
    }

    private String normalize(String token) {
        if (token == null) {
            return null;
        }
        String t = token.trim();
        if (t.regionMatches(true, 0, "Bearer ", 0, 7)) {
            t = t.substring(7).trim();
        }
        return t.isBlank() ? null : t;
    }

    private String extractToken(TokenRefreshed payload) {
        if (payload == null) {
            return null;
        }
        return payload.getAccessToken();
    }
}
