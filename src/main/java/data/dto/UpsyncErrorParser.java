package data.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.core.KazisafeServiceFactory;
import tools.SyncLogger;

/**
 * Parse le corps d'erreur HTTP (errorBody) d'un upsync en
 * {@link SyncErrorResponse}. Le parsing est tolerant : tout corps non-JSON ou
 * vide aboutit a null (retry classique sans healing).
 */
public final class UpsyncErrorParser {

    private static final ObjectMapper MAPPER = KazisafeServiceFactory.mapper();

    private UpsyncErrorParser() {
    }

    public static SyncErrorResponse parse(String errorBody) {
        if (errorBody == null || errorBody.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(errorBody, SyncErrorResponse.class);
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "UpsyncErrorParser.parse");
            return null;
        }
    }
}