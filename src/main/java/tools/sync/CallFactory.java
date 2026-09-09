package tools.sync;

import java.util.Set;
import retrofit2.Call;

/**
 * Fabrique de requete Retrofit re-jouable : chaque nouvelle invocation du
 * {@link #newCall()} reconstruit un Call identique (un Call Retrofit ne peut
 * etre execute qu'une seule fois).
 */
@FunctionalInterface
public interface CallFactory<T> {

    Call<T> newCall();

    /**
     * Codes HTTP recouvrables par un healing de parent manquant.
     * 417 client, 412 tresor, 460 produit, 461 mesure (convention serveur
     * historique) + 409/422 formates en SyncErrorResponse (nouvelle convention).
     */
    int MISSING_CLIENT = 417;
    int MISSING_TRESOR = 412;
    int MISSING_PRODUIT = 460;
    int MISSING_MESURE = 461;
    int CONFLICT = 409;
    int UNPROCESSABLE = 422;

    Set<Integer> HEALABLE_CODES = Set.of(MISSING_CLIENT, MISSING_TRESOR,
            MISSING_PRODUIT, MISSING_MESURE, CONFLICT, UNPROCESSABLE);

    static boolean isHealable(int httpCode) {
        return HEALABLE_CODES.contains(httpCode);
    }
}