package data.network.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchResultDto {
    // Ignore the "success" boolean from server, we don't need it
    // Only map the actual lists: "successes" and "failures"
    @JsonProperty("successes")
    public List<EntryResult> successes;
    
    @JsonProperty("failures")
    public List<EntryResult> failures;

    public BatchResultDto() {}

    public static class EntryResult {
        public String entityId;
        public String entityType;
        public String reason;

        public EntryResult() {}
        public EntryResult(String entityId, String entityType, String reason) {
            this.entityId = entityId;
            this.entityType = entityType;
            this.reason = reason;
        }
    }
}
