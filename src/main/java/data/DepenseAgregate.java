package data;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "depense_agregate")
public class DepenseAgregate implements Serializable {

    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "uid")
    private String uid;

    @Column(name = "date", columnDefinition = "DATETIME")
    private LocalDateTime date;

    @Column(name = "montant_usd")
    private Double montantUsd;

    @Column(name = "montant_cdf")
    private Double montantCdf;

    @Column(name = "region")
    private String region;

    @Column(name = "imputation")
    private String imputation;

    @JoinColumn(name = "depense_id", referencedColumnName = "uid")
    @ManyToOne
    private Depense depenseId;

    @PrePersist
    private void prepersist() {
        if (uid == null) {
            uid = UUID.randomUUID().toString().toLowerCase().replace("-", "");
        }
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Double getMontantUsd() {
        return montantUsd;
    }

    public void setMontantUsd(Double montantUsd) {
        this.montantUsd = montantUsd;
    }

    public Double getMontantCdf() {
        return montantCdf;
    }

    public void setMontantCdf(Double montantCdf) {
        this.montantCdf = montantCdf;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getImputation() {
        return imputation;
    }

    public void setImputation(String imputation) {
        this.imputation = imputation;
    }

    public Depense getDepenseId() {
        return depenseId;
    }

    public void setDepenseId(Depense depenseId) {
        this.depenseId = depenseId;
    }
}
