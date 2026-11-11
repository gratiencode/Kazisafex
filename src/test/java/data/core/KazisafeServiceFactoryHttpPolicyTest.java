package data.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import retrofit2.Retrofit;

/**
 * Non-regression : le client applicatif ne doit jamais rejouer implicitement
 * une requete. Un POST de vente/paiement peut avoir ete traite par le serveur
 * avant une coupure ; le retry doit rester explicite et idempotent.
 */
class KazisafeServiceFactoryHttpPolicyTest {

    @Test
    void applicationClientDoesNotRetryOnConnectionFailure() throws Exception {
        Method factory = KazisafeServiceFactory.class
                .getDeclaredMethod("getRetrofitInstance", String.class);
        factory.setAccessible(true);
        Retrofit retrofit = (Retrofit) factory.invoke(null, "");
        assertNotNull(retrofit);

        OkHttpClient client = (OkHttpClient) retrofit.callFactory();
        Field retry = OkHttpClient.class.getDeclaredField("retryOnConnectionFailure");
        retry.setAccessible(true);
        assertFalse(retry.getBoolean(client),
                "OkHttp ne doit pas rejouer automatiquement une vente apres une coupure");

        assertFalse(client.connectTimeoutMillis() <= 0);
        assertFalse(client.readTimeoutMillis() <= 0);
        assertFalse(client.writeTimeoutMillis() <= 0);
        assertFalse(client.callTimeoutMillis() <= 0);
    }
}
