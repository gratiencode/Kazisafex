package data.dto;

/**
 * DTO upsync asymetrique Recquisition (approvisionnement) : FKs produit/mesure
 * a plat + champs scalaires.
 */
public class RecquisitionUpsertDto {

    private String uid;
    private String produitUid;
    private String mesureUid;
    private String region;
    private String date;
    private String reference;
    private String observation;
    private Double quantite;
    private Double coutAchat;
    private String dateExpiry;
    private Double stockAlert;
    private String numlot;
    private String updatedAt;

    public RecquisitionUpsertDto() {
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public Double getQuantite() {
        return quantite;
    }

    public void setQuantite(Double quantite) {
        this.quantite = quantite;
    }

    public Double getCoutAchat() {
        return coutAchat;
    }

    public void setCoutAchat(Double coutAchat) {
        this.coutAchat = coutAchat;
    }

    public String getDateExpiry() {
        return dateExpiry;
    }

    public void setDateExpiry(String dateExpiry) {
        this.dateExpiry = dateExpiry;
    }

    public Double getStockAlert() {
        return stockAlert;
    }

    public void setStockAlert(Double stockAlert) {
        this.stockAlert = stockAlert;
    }

    public String getNumlot() {
        return numlot;
    }

    public void setNumlot(String numlot) {
        this.numlot = numlot;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}