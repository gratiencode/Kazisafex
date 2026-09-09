package tools.sync;

import data.dto.SyncErrorResponse;
import data.dto.UpsyncErrorParser;
import data.network.Kazisafe;
import java.io.IOException;
import retrofit2.Response;
import tools.SyncLogger;

/**
 * Wrapper d'upsync resilient : execute une mutation, et si le serveur refuse
 * parce qu'un parent manque, declenche l'auto-healing
 * ({@link MissingParentHealer}) puis rejoue la meme mutation dans le meme flux
 * (max {@value #DEFAULT_MAX_ATTEMPTS} essais de healing).
 */
public final class UpsyncEngine {

    public static final int DEFAULT_MAX_ATTEMPTS = 3;

    private final MissingParentHealer healer;

    public UpsyncEngine() {
        this(MissingParentHealer.getInstance());
    }

    public UpsyncEngine(MissingParentHealer healer) {
        this.healer = healer;
    }

    public <T> Response<T> execute(Kazisafe kazisafe, CallFactory<T> factory, String context) throws IOException {
        return execute(kazisafe, factory, context, DEFAULT_MAX_ATTEMPTS);
    }

    public <T> Response<T> execute(Kazisafe kazisafe, CallFactory<T> factory, String context, int maxAttempts)
            throws IOException {
        if (kazisafe == null || factory == null) {
            return null;
        }
        Response<T> rep = factory.newCall().execute();
        for (int attempt = 0; attempt < maxAttempts
                && rep != null && !rep.isSuccessful()
                && CallFactory.isHealable(rep.code()); attempt++) {
            SyncErrorResponse err = UpsyncErrorParser.parse(bodyOf(rep));
            boolean healed = err != null && healer.heal(kazisafe, err);
            SyncLogger.getInstance().logMessage(context,
                    "upsync http=" + rep.code() + " attempt=" + attempt + " body=" + bodyOf(rep)
                            + " parse=" + (err != null) + " healed=" + healed);
            if (!healed) {
                break;
            }
            SyncLogger.getInstance().logMessage(context,
                    "healing execute (http " + rep.code() + "), rejeu de la mutation");
            rep = factory.newCall().execute();
        }
        return rep;
    }

    private static String bodyOf(Response<?> rep) {
        try {
            return rep.errorBody() != null ? rep.errorBody().string() : "";
        } catch (IOException e) {
            return "";
        }
    }
}