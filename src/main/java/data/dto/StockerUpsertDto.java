package data.dto;

/**
 * DTO upsync asymetrique Stocker : FKs livraison/produit/mesure a plat +
 * champs scalaires.
 */
public class StockerUpsertDto {

    private String uid;
    private String livraisonUid;
    private String produitUid;
    private String mesureUid;
    private String region;
    private String dateStocker;
    private String dateExpir;
    private Double coutAchat;
    private Double reduction;
    private Double stockAlerte;
    private Double quantite;
    private Double prixAchatTotal;
    private Double prixVenteEstime;
    private String numlot;
    private String libelle;
    private String localisation;
    private String observation;
    private String updatedAt;

    public StockerUpsertDto() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getLivraisonUid() {
        return livraisonUid;
    }

    public void setLivraisonUid(String livraisonUid) {
        this.livraisonUid = livraisonUid;
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

    public String getDateStocker() {
        return dateStocker;
    }

    public void setDateStocker(String dateStocker) {
        this.dateStocker = dateStocker;
    }

    public String getDateExpir() {
        return dateExpir;
    }

    public void setDateExpir(String dateExpir) {
        this.dateExpir = dateExpir;
    }

    public Double getCoutAchat() {
        return coutAchat;
    }

    public void setCoutAchat(Double coutAchat) {
        this.coutAchat = coutAchat;
    }

    public Double getReduction() {
        return reduction;
    }

    public void setReduction(Double reduction) {
        this.reduction = reduction;
    }

    public Double getStockAlerte() {
        return stockAlerte;
    }

    public void setStockAlerte(Double stockAlerte) {
        this.stockAlerte = stockAlerte;
    }

    public Double getQuantite() {
        return quantite;
    }

    public void setQuantite(Double quantite) {
        this.quantite = quantite;
    }

    public Double getPrixAchatTotal() {
        return prixAchatTotal;
    }

    public void setPrixAchatTotal(Double prixAchatTotal) {
        this.prixAchatTotal = prixAchatTotal;
    }

    public Double getPrixVenteEstime() {
        return prixVenteEstime;
    }

    public void setPrixVenteEstime(Double prixVenteEstime) {
        this.prixVenteEstime = prixVenteEstime;
    }

    public String getNumlot() {
        return numlot;
    }

    public void setNumlot(String numlot) {
        this.numlot = numlot;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
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