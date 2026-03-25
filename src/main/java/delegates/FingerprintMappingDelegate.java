package delegates;

import IServices.FingerprintMappingStorage;
import data.FingerprintMapping;
import java.util.List;
import tools.ServiceLocator;
import tools.Tables;

public class FingerprintMappingDelegate {

    private static FingerprintMappingStorage getStorage() {
        return (FingerprintMappingStorage) ServiceLocator.getInstance().getService(Tables.FINGERPRINTMAPPING);
    }

    public static FingerprintMapping createMapping(FingerprintMapping mapping) {
        return getStorage().createMapping(mapping);
    }

    public static FingerprintMapping updateMapping(FingerprintMapping mapping) {
        return getStorage().updateMapping(mapping);
    }

    public static void deleteMapping(String agentId) {
        getStorage().deleteMapping(agentId);
    }

    public static FingerprintMapping findByAgentId(String agentId) {
        return getStorage().findByAgentId(agentId);
    }

    public static FingerprintMapping findByHash(String hash) {
        return getStorage().findByHash(hash);
    }

    public static List<FingerprintMapping> findAll() {
        return getStorage().findAll();
    }

    public static boolean isExists(String agentId) {
        return findByAgentId(agentId) != null;
    }
}
