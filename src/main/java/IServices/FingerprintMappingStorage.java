package IServices;

import data.FingerprintMapping;
import java.util.List;

public interface FingerprintMappingStorage {
    FingerprintMapping createMapping(FingerprintMapping mapping);

    FingerprintMapping updateMapping(FingerprintMapping mapping);

    void deleteMapping(String agentId);

    FingerprintMapping findByAgentId(String agentId);

    FingerprintMapping findByHash(String hash);

    List<FingerprintMapping> findAll();
}
