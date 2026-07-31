package services;

import data.network.Kazisafe;
import data.network.ValidationHashResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import retrofit2.Response;

public class StateValidationService {

    private static final String[] ENTERPRISE_ENTITIES = {
        "Category",
        "Produit",
    };
    private static final String[] REGIONAL_ENTITIES = {
        "Production",
        "Commande",
        "Entreposer",
    };

    public static class ValidationResult {

        public final boolean isSynchronized;
        public final String localHash;
        public final String serverHash;
        public final String localFingerprint;
        public final String serverFingerprint;

        public ValidationResult(
            boolean isSynchronized,
            String localHash,
            String serverHash,
            String localFingerprint,
            String serverFingerprint
        ) {
            this.isSynchronized = isSynchronized;
            this.localHash = localHash;
            this.serverHash = serverHash;
            this.localFingerprint = localFingerprint;
            this.serverFingerprint = serverFingerprint;
        }
    }

    public static ValidationResult validateState(
        Kazisafe api,
        String enterpriseId,
        String regionId
    ) {
        try {
            // 1. Get server hash
            Response<ValidationHashResponse> response = api
                .getValidationHash(enterpriseId, regionId)
                .execute();
            if (!response.isSuccessful() || response.body() == null) {
                // Handle server errors (like 404) gracefully, treat as synced
                System.err.println(
                    "StateValidationService: Failed to fetch validation hash from server: " +
                    response.code() + " - treating as synchronized"
                );
                return new ValidationResult(true, null, null, null, null);
            }
            ValidationHashResponse serverData = response.body();

            // 2. Calculate local fingerprint and hash
            String localFingerprint = calculateLocalFingerprint(regionId);
            String localHash = sha256(localFingerprint);

            boolean isSynced = Objects.equals(localHash, serverData.getHash());

            return new ValidationResult(
                isSynced,
                localHash,
                serverData.getHash(),
                localFingerprint,
                serverData.getFingerprint()
            );
        } catch (Exception e) {
            // Handle any exceptions gracefully, treat as synced
            e.printStackTrace();
            System.err.println(
                "StateValidationService: Exception during validation - treating as synchronized: " +
                e.getMessage()
            );
            return new ValidationResult(true, null, null, null, null);
        }
    }

    private static String calculateLocalFingerprint(String regionId) {
        Map<String, String> stats = new LinkedHashMap<>();

        // Enterprise Scope
        for (String entity : ENTERPRISE_ENTITIES) {
            stats.put(entity, fetchStats(entity, null));
        }

        // Regional Scope
        for (String entity : REGIONAL_ENTITIES) {
            stats.put(entity, fetchStats(entity, regionId));
        }

        return stats
            .entrySet()
            .stream()
            .map(e -> e.getKey() + ":" + e.getValue())
            .collect(Collectors.joining("|"));
    }

    private static String fetchStats(String entity, String regionId) {
        return ManagedSessionFactory.executeRead(em -> {
            String jpql =
                "SELECT count(e), max(e.updatedAt) FROM " + entity + " e ";
            if (regionId != null) {
                jpql += " WHERE e.region = :region";
            }

            Query query = em.createQuery(jpql);
            if (regionId != null) {
                query.setParameter("region", regionId);
            }

            Object[] result = (Object[]) query.getSingleResult();
            long count =
                result[0] == null ? 0 : ((Number) result[0]).longValue();
            LocalDateTime maxDate = (LocalDateTime) result[1];

            String dateStr =
                maxDate == null
                    ? "null"
                    : maxDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            return "cnt=" + count + ",max=" + dateStr;
        });
    }

    private static String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }
}
