package data.network.dto;

public class SyncOutboxDto {
    public String entityId;
    public String entityType;
    public String payload;
    public String mutationType;
    public long mutationTs;

    public SyncOutboxDto() {}
}
