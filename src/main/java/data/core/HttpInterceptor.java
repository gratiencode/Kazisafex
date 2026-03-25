package data.core;

import java.io.IOException;
import data.network.OnTokenRefreshedListener;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Interceptor.Chain;
import okhttp3.Request.Builder;
import data.helpers.Token;
import data.helpers.TokenRefreshed;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpInterceptor implements Interceptor {

    private static final Logger LOGGER = Logger.getLogger(HttpInterceptor.class.getName());
    private static final String RETRY_HEADER = "X-Kazi-Auth-Retry";

    private final AuthTokenState tokenState;
    private final TokenRefreshClient tokenRefreshClient;
    private OnTokenRefreshedListener onTokenRefreshedListener;

    public HttpInterceptor(AuthTokenState tokenState, TokenRefreshClient tokenRefreshClient) {
        this.tokenState = tokenState;
        this.tokenRefreshClient = tokenRefreshClient;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request rq = chain.request();
        String initialToken = tokenState.getToken();
        Builder builder = rq.newBuilder();
        this.setHeader(builder, initialToken);
        rq = builder.build();
        Response resp = chain.proceed(rq);

        if ((resp.code() == 401 || resp.code() == 403) && rq.header(RETRY_HEADER) == null) {
            LOGGER.log(Level.INFO, "Unauthorized HTTP {0} detected, attempting token refresh", resp.code());
            String newToken;
            synchronized (tokenState) {
                // Double check if another thread refreshed the token while we were waiting
                newToken = tokenState.getToken();
                if (initialToken != null && initialToken.equals(newToken)) {
                    try {
                        newToken = tokenRefreshClient.refreshAccessToken(initialToken);
                        if (newToken != null && !newToken.isBlank()) {
                            tokenState.setToken(newToken);
                            notifyTokenRefresh(newToken);
                            LOGGER.info("Token refresh successful from interceptor. New token length: "
                                    + newToken.length());
                        } else {
                            LOGGER.warning("Token refresh did not return a new token");
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Token refresh from interceptor failed", ex);
                        newToken = null;
                    }
                } else {
                    LOGGER.info("Token already refreshed by another thread, skipping network call");
                }
            }

            if (newToken != null && !newToken.isBlank()) {
                Builder retryBuilder = rq.newBuilder();
                retryBuilder.header(RETRY_HEADER, "1");
                setHeader(retryBuilder, newToken);
                resp.close();
                rq = retryBuilder.build();
                resp = chain.proceed(rq);
            }
        }

        return resp;
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

    public void setHeader(Builder builder, String token) {
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", String.format("Bearer %s", token));
        } else {
            builder.removeHeader("Authorization");
        }
    }
}
