package data.network.dto;

import jakarta.persistence.Column;
import java.time.LocalDateTime;
import java.util.List;

public class BatchMutationDto {
    /** Uid de la ligne outbox : cle d'idempotence si le serveur l'echo. */
    public String mutationId;
    public String entityId;
    public String entityType;
    public String payload;
    @Column(name = "updateAt", columnDefinition = "DATETIME")
    public LocalDateTime updatedAt;
    public String mutationType;
    public String entrepriseId;
    public String region;

    public BatchMutationDto() {}

    public BatchMutationDto(String mutationId, String entityId, String entityType, String payload, LocalDateTime updatedAt, String mutationType, String entrepriseId, String region) {
        this.mutationId = mutationId;
        this.entityId = entityId;
        this.entityType = entityType;
        this.payload = payload;
        this.updatedAt = updatedAt;
        this.mutationType = mutationType;
        this.entrepriseId = entrepriseId;
        this.region = region;
    }
}
