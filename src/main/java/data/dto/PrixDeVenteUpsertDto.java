package data.dto;

/**
 * DTO upsync asymetrique PrixDeVente : FKs mesure/recquisition a plat + champs
 * scalaires (barreme de prix).
 */
public class PrixDeVenteUpsertDto {

    private String uid;
    private String mesureUid;
    private String recquisitionUid;
    private Double qmin;
    private Double qmax;
    private Double prixUnitaire;
    private String devise;
    private Double pourcentParCunit;
    private String updatedAt;

    public PrixDeVenteUpsertDto() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMesureUid() {
        return mesureUid;
    }

    public void setMesureUid(String mesureUid) {
        this.mesureUid = mesureUid;
    }

    public String getRecquisitionUid() {
        return recquisitionUid;
    }

    public void setRecquisitionUid(String recquisitionUid) {
        this.recquisitionUid = recquisitionUid;
    }

    public Double getQmin() {
        return qmin;
    }

    public void setQmin(Double qmin) {
        this.qmin = qmin;
    }

    public Double getQmax() {
        return qmax;
    }

    public void setQmax(Double qmax) {
        this.qmax = qmax;
    }

    public Double getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(Double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public Double getPourcentParCunit() {
        return pourcentParCunit;
    }

    public void setPourcentParCunit(Double pourcentParCunit) {
        this.pourcentParCunit = pourcentParCunit;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}