package data.dto;

/**
 * DTO upsync asymetrique Destocker (sortie de stock) : FKs produit/mesure a
 * plat + champs scalaires.
 */
public class DestockerUpsertDto {

    private String uid;
    private String produitUid;
    private String mesureUid;
    private String region;
    private String dateDestockage;
    private String reference;
    private String destination;
    private Double coutAchat;
    private Double quantite;
    private String libelle;
    private String numlot;
    private String observation;
    private String updatedAt;

    public DestockerUpsertDto() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getProduitUid() {
        return produitUid;
    }

    public void setProduitUid(String produitUid) {
        this.produitUid = produitUid;
    }

    public String getMesureUid() {
        return mesureUid;
    }

    public void setMesureUid(String mesureUid) {
        this.mesureUid = mesureUid;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDateDestockage() {
        return dateDestockage;
    }

    public void setDateDestockage(String dateDestockage) {
        this.dateDestockage = dateDestockage;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Double getCoutAchat() {
        return coutAchat;
    }

    public void setCoutAchat(Double coutAchat) {
        this.coutAchat = coutAchat;
    }

    public Double getQuantite() {
        return quantite;
    }

    public void setQuantite(Double quantite) {
        this.quantite = quantite;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getNumlot() {
        return numlot;
    }

    public void setNumlot(String numlot) {
        this.numlot = numlot;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}