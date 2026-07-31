package data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "sync_outbox")
@EntityListeners(data.SyncOutboxListener.class)
public class SyncOutbox implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "uid", updatable = false, nullable = false)
    private String uid;

    @Column(name = "table_name", nullable = false)
    private String tableName;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(name = "action", nullable = false)
    private String action; // "PERSIST", "UPDATE", "REMOVE", "APPLIED"

    @Lob
    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "created_at", columnDefinition = "DATETIME")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    @Column(name = "entreprise_id")
    private String entrepriseId;

    @Column(name = "region")
    private String region;

    public SyncOutbox() {}

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getEntrepriseId() {
        return entrepriseId;
    }

    public void setEntrepriseId(String entrepriseId) {
        this.entrepriseId = entrepriseId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getRetryCount() {
        return retryCount == null ? 0 : retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount == null ? 0 : retryCount;
    }
}
