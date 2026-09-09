package data.dto;

/**
 * DTO upsync asymetrique CompteTresor : champs scalaires uniquement.
 */
public class CompteTresorUpsertDto {

    private String uid;
    private String intitule;
    private String typeCompte;
    private String numeroCompte;
    private String bankName;
    private String region;
    private Double soldeMinimum;
    private String updatedAt;

    public CompteTresorUpsertDto() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getIntitule() {
        return intitule;
    }

    public void setIntitule(String intitule) {
        this.intitule = intitule;
    }

    public String getTypeCompte() {
        return typeCompte;
    }

    public void setTypeCompte(String typeCompte) {
        this.typeCompte = typeCompte;
    }

    public String getNumeroCompte() {
        return numeroCompte;
    }

    public void setNumeroCompte(String numeroCompte) {
        this.numeroCompte = numeroCompte;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Double getSoldeMinimum() {
        return soldeMinimum;
    }

    public void setSoldeMinimum(Double soldeMinimum) {
        this.soldeMinimum = soldeMinimum;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}