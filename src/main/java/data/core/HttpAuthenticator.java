
package data.core;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import data.network.OnTokenRefreshedListener;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import data.helpers.Token;

public class HttpAuthenticator implements Authenticator {

    private static final Logger LOGGER = Logger.getLogger(HttpAuthenticator.class.getName());
    private static final int MAX_AUTH_ATTEMPTS = 2;
    private final AuthTokenState tokenState;
    private final TokenRefreshClient tokenRefreshClient;
    private OnTokenRefreshedListener onTokenRefreshedListener;

    public HttpAuthenticator(AuthTokenState tokenState, TokenRefreshClient tokenRefreshClient) {
        this.tokenState = tokenState;
        this.tokenRefreshClient = tokenRefreshClient;
    }

    private void notifyTokenRefresh(String tokenValue) {
        if (this.onTokenRefreshedListener != null) {
            Token tkn = new Token();
            tkn.setToken(tokenValue);
            this.onTokenRefreshedListener.onTokenRefreshed(tkn);
        }
    }

    public void setOnTokenRefreshedListener(OnTokenRefreshedListener onTokenRefreshedListener) {
        this.onTokenRefreshedListener = onTokenRefreshedListener;
    }

    @Override
    public Request authenticate(Route route, Response rspns) throws IOException {
        String htkn = rspns.request().header("Authorization");
        if (htkn == null || !htkn.startsWith("Bearer ") || responseCount(rspns) > MAX_AUTH_ATTEMPTS) {
            return null;
        }
        if (rspns.request().url().encodedPath().endsWith("/auth/refresh")) {
            return null;
        }

        try {
            String accessToken = tokenRefreshClient.refreshAccessToken(tokenState.getToken());
            if (accessToken != null && !accessToken.isBlank()) {
                LOGGER.info("Affichage authenticator old = "+htkn+" new = "+accessToken);
                tokenState.setToken(accessToken);
                notifyTokenRefresh(accessToken);
                return rspns.request().newBuilder()
                        .header("Authorization", "Bearer " + accessToken)
                        .build();
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Token refresh failed", ex);
        }

        return null;
    }

    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }
}
