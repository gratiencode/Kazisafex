package data.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Objects;
import retrofit2.converter.jackson.JacksonConverterFactory;

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

//    private static HttpAuthenticator auth = null;
    private static OnTokenRefreshedListener onTokenRefreshedListener;

    public KazisafeServiceFactory() {
    }

    private static void notifytoken(Token token) {
        if (onTokenRefreshedListener != null) {
            onTokenRefreshedListener.onTokenRefreshed(token);
        }

    }

    public static void setOnTokenRefreshCallback(OnTokenRefreshedListener otrl) {
        onTokenRefreshedListener = otrl;
    }

    private static Retrofit getRetrofitInstance(String token) {
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
        if (token != null && !token.isBlank()) {
            try {
                AuthTokenState tokenState = new AuthTokenState(token);
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


    public static ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(flexibleFormatter));
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(flexibleFormatter));
        mapper.registerModule(module);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    public static Kazisafe createService(String token) {
        return (Kazisafe) getRetrofitInstance(token).create(Kazisafe.class);
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
}
