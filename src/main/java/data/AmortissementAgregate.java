package data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "amortissement_agregate")
public class AmortissementAgregate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "uid")
    private String uid;

    @Column(name = "periode", columnDefinition = "DATE")
    private LocalDate periode;

    @Column(name = "dotation_usd")
    private Double dotationUsd;

    @Column(name = "cumul_usd")
    private Double cumulUsd;

    @Column(name = "valeur_comptable_usd")
    private Double valeurComptableUsd;

    @Column(name = "region")
    private String region;

    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;

    @JoinColumn(name = "immobilisation_id", referencedColumnName = "uid")
    @ManyToOne
    private Immobilisation immobilisationId;

    @PrePersist
    protected void onCreate() {
        if (uid == null || uid.isBlank()) {
            uid = UUID.randomUUID().toString().toLowerCase().replace("-", "");
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public LocalDate getPeriode() {
        return periode;
    }

    public void setPeriode(LocalDate periode) {
        this.periode = periode;
    }

    public Double getDotationUsd() {
        return dotationUsd;
    }

    public void setDotationUsd(Double dotationUsd) {
        this.dotationUsd = dotationUsd;
    }

    public Double getCumulUsd() {
        return cumulUsd;
    }

    public void setCumulUsd(Double cumulUsd) {
        this.cumulUsd = cumulUsd;
    }

    public Double getValeurComptableUsd() {
        return valeurComptableUsd;
    }

    public void setValeurComptableUsd(Double valeurComptableUsd) {
        this.valeurComptableUsd = valeurComptableUsd;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Immobilisation getImmobilisationId() {
        return immobilisationId;
    }

    public void setImmobilisationId(Immobilisation immobilisationId) {
        this.immobilisationId = immobilisationId;
    }

}
