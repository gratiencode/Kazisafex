package data;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "fingerprint_mapping")
@XmlRootElement
public class FingerprintMapping extends BaseModel implements Serializable {

    @Id
    @Column(name = "agent_id")
    private String agentId;

    @Column(name = "fingerprint_hash")
    private String fingerprintHash;

    @Column(name = "region")
    private String region;

    @Column(name = "entreprise")
    private String entreprise;

    public FingerprintMapping() {
    }

    public FingerprintMapping(String agentId, String fingerprintHash) {
        this.agentId = agentId;
        this.fingerprintHash = fingerprintHash;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
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
}
