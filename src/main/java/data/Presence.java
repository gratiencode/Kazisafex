package data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlRootElement;
import tools.Tables;

@Entity
@Table(name = "presence")
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "Presence.findAll", query = "SELECT p FROM Presence p"),
        @NamedQuery(name = "Presence.findByUid", query = "SELECT p FROM Presence p WHERE p.uid = :uid"),
        @NamedQuery(name = "Presence.findByAgentId", query = "SELECT p FROM Presence p WHERE p.agentId = :agentId"),
        @NamedQuery(name = "Presence.findByRegion", query = "SELECT p FROM Presence p WHERE p.region = :region"),
        @NamedQuery(name = "Presence.findByPeriod", query = "SELECT p FROM Presence p WHERE p.timestamp >= :start AND p.timestamp <= :end")
})
public class Presence extends BaseModel implements Serializable {

    @Id
    @Basic(optional = false)
    @Column(name = "uid", updatable = false, nullable = false)
    private String uid;

    @Column(name = "agent_id")
    private String agentId;

    @Column(name = "agent_nom")
    private String agentNom;

    @Column(name = "agent_prenom")
    private String agentPrenom;

    @Column(name = "timestamp", columnDefinition = "DATETIME")
    private LocalDateTime timestamp;

    @Column(name = "type")
    private String typePresence; // CHECK_IN, CHECK_OUT

    @Column(name = "fingerprint_hash")
    private String fingerprintHash;

    @Column(name = "region")
    private String region;

    @Column(name = "entreprise")
    private String entreprise;

    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    public Presence() {
        this.type = Tables.PRESENCE.name();
    }

    @PrePersist
    protected void onDataOperation() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Presence(String uid, String agentId, String agentNom, String agentPrenom, LocalDateTime timestamp,
            String type, String fingerprintHash, String region, String entreprise) {
        this();
        this.uid = uid;
        this.agentId = agentId;
        this.agentNom = agentNom;
        this.agentPrenom = agentPrenom;
        this.timestamp = timestamp;
        this.typePresence = type;
        this.fingerprintHash = fingerprintHash;
        this.region = region;
        this.entreprise = entreprise;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getAgentNom() {
        return agentNom;
    }

    public void setAgentNom(String agentNom) {
        this.agentNom = agentNom;
    }

    public String getAgentPrenom() {
        return agentPrenom;
    }

    public void setAgentPrenom(String agentPrenom) {
        this.agentPrenom = agentPrenom;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getTypePresence() {
        return typePresence;
    }

    public void setTypePresence(String typePresence) {
        this.typePresence = typePresence;
    }

    public String getFingerprintHash() {
        return fingerprintHash;
    }

    public void setFingerprintHash(String fingerprintHash) {
        this.fingerprintHash = fingerprintHash;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getEntreprise() {
        return entreprise;
    }

    public void setEntreprise(String entreprise) {
        this.entreprise = entreprise;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.uid);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Presence other = (Presence) obj;
        return Objects.equals(this.uid, other.uid);
    }

    @Override
    public String toString() {
        return "Presence{" + "uid=" + uid + ", agentId=" + agentId + ", agentNom=" + agentNom + ", timestamp="
                + timestamp + ", type=" + typePresence + '}';
    }
}
