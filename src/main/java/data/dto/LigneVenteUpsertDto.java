package data.dto;

/**
 * DTO upsync asymetrique LigneVente : uid a plat (String), FKs vente/produit/
 * mesure a plat + champs scalaires. Permet de rejouer une ligne apres healing
 * du parent.
 */
public class LigneVenteUpsertDto {

    private String uid;
    private String venteUid;
    private String clientUid;
    private String produitUid;
    private String mesureUid;
    private Double quantite;
    private Double montantUsd;
    private Double montantCdf;
    private Double prixUnit;
    private Double coutAchat;
    private String numlot;
    private String updatedAt;

    public LigneVenteUpsertDto() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getVenteUid() {
        return venteUid;
    }

    public void setVenteUid(String venteUid) {
        this.venteUid = venteUid;
    }

    public String getClientUid() {
        return clientUid;
    }

    public void setClientUid(String clientUid) {
        this.clientUid = clientUid;
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

    public Double getQuantite() {
        return quantite;
    }

    public void setQuantite(Double quantite) {
        this.quantite = quantite;
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

    public Double getPrixUnit() {
        return prixUnit;
    }

    public void setPrixUnit(Double prixUnit) {
        this.prixUnit = prixUnit;
    }

    public Double getCoutAchat() {
        return coutAchat;
    }

    public void setCoutAchat(Double coutAchat) {
        this.coutAchat = coutAchat;
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