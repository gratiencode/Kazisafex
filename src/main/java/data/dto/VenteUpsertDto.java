package data.dto;

/**
 * DTO upsync asymetrique Vente : uid a plat (String), FK client a plat +
 * champs scalaires. Les lignes voyagent dans LigneVenteUpsertDto.
 */
public class VenteUpsertDto {

    private String uid;
    private String clientUid;
    private String reference;
    private String region;
    private String dateVente;
    private Double montantUsd;
    private Double montantCdf;
    private Double montantDette;
    private String deviseDette;
    private String payment;
    private String libelle;
    private String observation;
    private String echeance;
    private Double latitude;
    private Double longitude;
    private String transactionId;
    private String updatedAt;

    public VenteUpsertDto() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getClientUid() {
        return clientUid;
    }

    public void setClientUid(String clientUid) {
        this.clientUid = clientUid;
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

    public String getDateVente() {
        return dateVente;
    }

    public void setDateVente(String dateVente) {
        this.dateVente = dateVente;
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

    public Double getMontantDette() {
        return montantDette;
    }

    public void setMontantDette(Double montantDette) {
        this.montantDette = montantDette;
    }

    public String getDeviseDette() {
        return deviseDette;
    }

    public void setDeviseDette(String deviseDette) {
        this.deviseDette = deviseDette;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getEcheance() {
        return echeance;
    }

    public void setEcheance(String echeance) {
        this.echeance = echeance;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}