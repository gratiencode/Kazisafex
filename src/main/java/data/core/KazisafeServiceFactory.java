package data.core;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import data.network.Kazisafe;
import data.network.OnTokenRefreshedListener;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import data.helpers.Token;
import javax.net.ssl.SSLSession;

public class KazisafeServiceFactory {

    public static final String BASE_URL = 
//        "https://192.168.43.184:8080/v1/";
            "https://kazisaf2.5y5.uk:10027/v1/";
//            "https://app.kazisafe.com/v1/";
    public static final String WEBSOCKET =
//      "wss://kazisaf2.5y5.uk:10027";
    "ws://192.168.43.184:8080";
    //"wss://app.kazisafe.com:10365";
    public static final String WIFI_TEST_URL = "http://172.20.10.6:8020/erp/v1/";
    public static final String WIFI2_TEST_URL = "http://192.168.88.243/erp/v1/";
    public static final String HOTSPOT_TEST_URL = "http://192.168.43.120:8020/erp/v1/";
    private static Retrofit retrofitRefresh = null;
    private static Retrofit retrofit = null;
    private static HttpAuthenticator auth = null;
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
                .readTimeout(5L, TimeUnit.MINUTES);
        builder.hostnameVerifier((String hostname, SSLSession session) -> true);

        if (token != null) {
            try {

                HttpInterceptor intercep = new HttpInterceptor(token);
                builder.addInterceptor(intercep);
                TLSSocketFactory f = new TLSSocketFactory();
                builder.sslSocketFactory(TLSSocketFactory.trustAllCerts(), f.getTrustManager());
                Token t = new Token();
                t.setToken(token);
                intercep.setOnTokenRefreshedListener(KazisafeServiceFactory::notifytoken);
            } catch (KeyStoreException | KeyManagementException | NoSuchAlgorithmException ex) {
            }
        }

        try {
            TLSSocketFactory ssl = new TLSSocketFactory();
            if (ssl.getTrustManager() != null) {
                builder.sslSocketFactory(TLSSocketFactory.trustAllCerts(), ssl.getTrustManager());
            }
        } catch (KeyStoreException ex) {
        } catch (KeyManagementException ex) {
        } catch (NoSuchAlgorithmException ex) {
        }
        OkHttpClient client = builder.build();
        retrofit = (new retrofit2.Retrofit.Builder()).baseUrl(BASE_URL)
                .client(client).addConverterFactory(JsonbConverterFactory.create()).build();
        return retrofit;
    }

    public static Kazisafe createService(String token) {
        return (Kazisafe) getRetrofitInstance(token).create(Kazisafe.class);
    }

    public static Retrofit getInstanceRefresh() {
        try {
            Builder httpClient = new Builder();
            TLSSocketFactory ssl = new TLSSocketFactory();
            if(ssl.getTrustManager()!=null){
//            httpClient.hostnameVerifier((String hostname, SSLSession session) -> true);
            httpClient.sslSocketFactory(TLSSocketFactory.trustAllCerts(), ssl.getTrustManager());
            }
            retrofitRefresh = (new retrofit2.Retrofit.Builder())
                    .baseUrl(BASE_URL)
                    .addConverterFactory(JsonbConverterFactory.create())
                    .client(httpClient.build()).build();
//            return retrofitRefresh;
        } catch (KeyStoreException ex) {
        } catch (KeyManagementException ex) {
        } catch (NoSuchAlgorithmException ex) {
        }
        return retrofitRefresh;
    }
}
