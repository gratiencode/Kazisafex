package data.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import data.network.Kazisafe;
import data.network.OnTokenRefreshedListener;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import retrofit2.Retrofit;
import data.helpers.Token;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Objects;
import java.util.prefs.Preferences;
import retrofit2.converter.jackson.JacksonConverterFactory;
import tools.SyncEngine;

public class KazisafeServiceFactory {

    public static final String BASE_URL
            = //        "https://192.168.43.184:8080/v1/";
            //            "https://192.168.88.245:8010/v1/";
            //            "https://localhost:8181/v1/";
            "https://cloud.kazisafe.com/v1/";
    public static final String WEBSOCKET
            = //            "wss:////192.168.88.245:8010";
            //            "wss://localhost:8181";
            //    "ws://192.168.43.184:8080";
            "wss://cloud.kazisafe.com";
    public static final String WIFI_TEST_URL = "http://172.20.10.6:8020/erp/v1/";
    public static final String WIFI2_TEST_URL = "http://192.168.88.243/erp/v1/";
    public static final String HOTSPOT_TEST_URL = "http://192.168.43.120:8020/erp/v1/";
    private static Retrofit retrofitRefresh = null;
    private static Retrofit retrofit = null;
    private static final Preferences PREFS = Preferences.userNodeForPackage(SyncEngine.class);
    private static volatile String currentAccessToken;

//    private static HttpAuthenticator auth = null;
    private static OnTokenRefreshedListener onTokenRefreshedListener;

    public KazisafeServiceFactory() {
    }

    private static void notifytoken(Token token) {
        if (token != null && token.getToken() != null && !token.getToken().isBlank()) {
            currentAccessToken = normalizeToken(token.getToken());
            PREFS.put("token", currentAccessToken);
        }
        if (onTokenRefreshedListener != null) {
            onTokenRefreshedListener.onTokenRefreshed(token);
        }

    }

    public static void setOnTokenRefreshCallback(OnTokenRefreshedListener otrl) {
        onTokenRefreshedListener = otrl;
    }

    private static Retrofit getRetrofitInstance(String token) {
        String effectiveToken = effectiveToken(token);
        Builder builder = new Builder();
        builder.connectTimeout(300L, TimeUnit.SECONDS)
                .writeTimeout(5L, TimeUnit.MINUTES)
                .retryOnConnectionFailure(true)
                .readTimeout(5L, TimeUnit.MINUTES);
//        builder.addInterceptor(chain -> {
//            Response response = chain.proceed(chain.request());
//            String rawBody = response.peekBody(Long.MAX_VALUE).string();
//            System.out.println(">>> Réponse brute: " + rawBody);
//            return response;
//        });
        if (effectiveToken != null && !effectiveToken.isBlank()) {
            try {
                AuthTokenState tokenState = new AuthTokenState(effectiveToken);
                TokenRefreshClient tokenRefreshClient = new TokenRefreshClient();
                HttpInterceptor intercep = new HttpInterceptor(tokenState, tokenRefreshClient);
                HttpAuthenticator authenticator = new HttpAuthenticator(tokenState, tokenRefreshClient);
                builder.addInterceptor(intercep);
                builder.authenticator(authenticator);
                TLSSocketFactory f = new TLSSocketFactory();
                builder.sslSocketFactory(f, f.getTrustManager());
                intercep.setOnTokenRefreshedListener(KazisafeServiceFactory::notifytoken);
                authenticator.setOnTokenRefreshedListener(KazisafeServiceFactory::notifytoken);
            } catch (KeyStoreException | KeyManagementException | NoSuchAlgorithmException ex) {
                throw new IllegalStateException("Unable to configure TLS client", ex);
            }
        }

        try {
            TLSSocketFactory ssl = new TLSSocketFactory();
            if (ssl.getTrustManager() != null) {
                builder.sslSocketFactory(ssl, ssl.getTrustManager());
            }
        } catch (KeyStoreException | KeyManagementException | NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Unable to initialize TLS", ex);
        }

        OkHttpClient client = builder.build();
//        initNotificationService(client);
        retrofit = (new retrofit2.Retrofit.Builder()).baseUrl(BASE_URL)
                .client(client).addConverterFactory(JacksonConverterFactory.create(mapper())).build();
        return retrofit;
    }
    
    static DateTimeFormatter flexibleFormatter = new DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
        .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
        .toFormatter();

    public static LocalDateTime toUtc(LocalDateTime local) {
        if (local == null) return null;
        return local.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    public static LocalDateTime fromUtc(LocalDateTime utc) {
        if (utc == null) return null;
        return utc.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    static class UtcLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            gen.writeString(toUtc(value).format(flexibleFormatter));
        }
    }

    static class UtcLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.currentToken() == JsonToken.VALUE_NULL) {
                return null;
            }
            String s = p.getValueAsString();
            if (s == null || s.isBlank()) {
                return null;
            }
            return fromUtc(LocalDateTime.parse(s, flexibleFormatter));
        }
    }

    public static ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Allow Jackson to access public fields directly
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        // Ignore unknown properties globally for safety
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        module.addSerializer(LocalDateTime.class, new UtcLocalDateTimeSerializer());
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        module.addDeserializer(LocalDateTime.class, new UtcLocalDateTimeDeserializer());
        mapper.registerModule(module);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    public static Kazisafe createService(String token) {
        if ((currentAccessToken == null || currentAccessToken.isBlank()) && token != null && !token.isBlank()) {
            currentAccessToken = normalizeToken(token);
            PREFS.put("token", currentAccessToken);
        }
        return (Kazisafe) getRetrofitInstance(effectiveToken(token)).create(Kazisafe.class);
    }

    public static Retrofit getInstanceRefresh() {
        if (Objects.nonNull(retrofitRefresh)) {
            return retrofitRefresh;
        }
        synchronized (KazisafeServiceFactory.class) {
            if (Objects.nonNull(retrofitRefresh)) {
                return retrofitRefresh;
            }
            try {
                Builder httpClient = new Builder()
                        .connectTimeout(30L, TimeUnit.SECONDS)
                        .writeTimeout(30L, TimeUnit.SECONDS)
                        .readTimeout(30L, TimeUnit.SECONDS);
                TLSSocketFactory ssl = new TLSSocketFactory();
                if (ssl.getTrustManager() != null) {
                    httpClient.sslSocketFactory(ssl, ssl.getTrustManager());
                }
                retrofitRefresh = (new retrofit2.Retrofit.Builder())
                        .baseUrl(BASE_URL)
                        .addConverterFactory(JacksonConverterFactory.create(mapper()))
                        .client(httpClient.build()).build();
            } catch (KeyStoreException | KeyManagementException | NoSuchAlgorithmException ex) {
                throw new IllegalStateException("Unable to initialize refresh client", ex);
            }
            return retrofitRefresh;
        }
    }

    private static String effectiveToken(String token) {
        String normalized = normalizeToken(token);
        if (currentAccessToken != null && !currentAccessToken.isBlank()) {
            return currentAccessToken;
        }
        if (normalized != null && !normalized.isBlank()) {
            currentAccessToken = normalized;
            return normalized;
        }
        String stored = normalizeToken(PREFS.get("token", null));
        if (stored != null && !stored.isBlank()) {
            currentAccessToken = stored;
        }
        return currentAccessToken;
    }

    private static String normalizeToken(String token) {
        if (token == null) {
            return null;
        }
        String normalized = token.trim();
        if (normalized.regionMatches(true, 0, "Bearer ", 0, 7)) {
            normalized = normalized.substring(7).trim();
        }
        return normalized.isBlank() ? null : normalized;
    }
}
