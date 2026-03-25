package data;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity
@Table(name = "immobilisation")
public class Immobilisation {
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "uid")
    private String uid;
    private String libelle;
    private String categorie;
    private String region;
    private LocalDate dateAcquisition;
    private Double valeurOrigineUsd = 0d;
    private Double valeurResiduelleUsd = 0d;
    private Integer dureeAmortissementMois = 12;
    private Boolean actif = true;
    private Long updatedAt;
    private Long deletedAt;

    public Immobilisation() {
    }

    public Immobilisation(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public LocalDate getDateAcquisition() {
        return dateAcquisition;
    }

    public void setDateAcquisition(LocalDate dateAcquisition) {
        this.dateAcquisition = dateAcquisition;
    }

    public Double getValeurOrigineUsd() {
        return valeurOrigineUsd;
    }

    public void setValeurOrigineUsd(Double valeurOrigineUsd) {
        this.valeurOrigineUsd = valeurOrigineUsd;
    }

    public Double getValeurResiduelleUsd() {
        return valeurResiduelleUsd;
    }

    public void setValeurResiduelleUsd(Double valeurResiduelleUsd) {
        this.valeurResiduelleUsd = valeurResiduelleUsd;
    }

    public Integer getDureeAmortissementMois() {
        return dureeAmortissementMois;
    }

    public void setDureeAmortissementMois(Integer dureeAmortissementMois) {
        this.dureeAmortissementMois = dureeAmortissementMois;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Long deletedAt) {
        this.deletedAt = deletedAt;
    }

    @PrePersist
    @PreUpdate
    public void updateTimeStamps() {
        updatedAt = System.currentTimeMillis();
    }

    public double dotationMensuelleUsd() {
        double base = Math.max(0d, safe(valeurOrigineUsd) - safe(valeurResiduelleUsd));
        int duree = safeDuration();
        return base / duree;
    }

    public int moisEcoules(LocalDate reference) {
        if (dateAcquisition == null || reference == null) {
            return 0;
        }
        long months = ChronoUnit.MONTHS.between(dateAcquisition.withDayOfMonth(1), reference.withDayOfMonth(1));
        return (int) Math.max(0, Math.min(months + 1, safeDuration()));
    }

    public double amortissementCumulUsd(LocalDate reference) {
        return dotationMensuelleUsd() * moisEcoules(reference);
    }

    public double valeurNetteUsd(LocalDate reference) {
        return Math.max(safe(valeurResiduelleUsd), safe(valeurOrigineUsd) - amortissementCumulUsd(reference));
    }

    private int safeDuration() {
        return (dureeAmortissementMois == null || dureeAmortissementMois <= 0) ? 1 : dureeAmortissementMois;
    }

    private double safe(Double value) {
        return value == null ? 0d : value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Immobilisation other)) {
            return false;
        }
        return Objects.equals(uid, other.uid);
    }
}
