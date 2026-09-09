package data.dto;

/**
 * DTO upsync asymetrique Traisorerie (mouvement de caisse) : FK tresor a plat
 * + champs scalaires.
 */
public class TraisorerieUpsertDto {

    private String uid;
    private String tresorUid;
    private String reference;
    private String region;
    private String date;
    private String libelle;
    private Double montantUsd;
    private Double montantCdf;
    private Double soldeCdf;
    private Double soldeUsd;
    private String mouvement;
    private String typeTresorerie;
    private String updatedAt;

    public TraisorerieUpsertDto() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTresorUid() {
        return tresorUid;
    }

    public void setTresorUid(String tresorUid) {
        this.tresorUid = tresorUid;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
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

    public Double getSoldeCdf() {
        return soldeCdf;
    }

    public void setSoldeCdf(Double soldeCdf) {
        this.soldeCdf = soldeCdf;
    }

    public Double getSoldeUsd() {
        return soldeUsd;
    }

    public void setSoldeUsd(Double soldeUsd) {
        this.soldeUsd = soldeUsd;
    }

    public String getMouvement() {
        return mouvement;
    }

    public void setMouvement(String mouvement) {
        this.mouvement = mouvement;
    }

    public String getTypeTresorerie() {
        return typeTresorerie;
    }

    public void setTypeTresorerie(String typeTresorerie) {
        this.typeTresorerie = typeTresorerie;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}