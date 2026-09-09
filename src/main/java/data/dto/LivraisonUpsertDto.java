package data.dto;

/**
 * DTO upsync asymetrique Livraison : FK fournisseur a plat + champs scalaires.
 */
public class LivraisonUpsertDto {

    private String uid;
    private String fournisseurUid;
    private String numPiece;
    private String dateLivr;
    private String reference;
    private String libelle;
    private String region;
    private String observation;
    private Double reduction;
    private Double topay;
    private Double payed;
    private Double remained;
    private Double toreceive;
    private String updatedAt;

    public LivraisonUpsertDto() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFournisseurUid() {
        return fournisseurUid;
    }

    public void setFournisseurUid(String fournisseurUid) {
        this.fournisseurUid = fournisseurUid;
    }

    public String getNumPiece() {
        return numPiece;
    }

    public void setNumPiece(String numPiece) {
        this.numPiece = numPiece;
    }

    public String getDateLivr() {
        return dateLivr;
    }

    public void setDateLivr(String dateLivr) {
        this.dateLivr = dateLivr;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public Double getReduction() {
        return reduction;
    }

    public void setReduction(Double reduction) {
        this.reduction = reduction;
    }

    public Double getTopay() {
        return topay;
    }

    public void setTopay(Double topay) {
        this.topay = topay;
    }

    public Double getPayed() {
        return payed;
    }

    public void setPayed(Double payed) {
        this.payed = payed;
    }

    public Double getRemained() {
        return remained;
    }

    public void setRemained(Double remained) {
        this.remained = remained;
    }

    public Double getToreceive() {
        return toreceive;
    }

    public void setToreceive(Double toreceive) {
        this.toreceive = toreceive;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}