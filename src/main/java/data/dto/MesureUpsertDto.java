package data.dto;

/**
 * DTO upsync asymetrique Mesure : FK produit a plat + champs scalaires.
 */
public class MesureUpsertDto {

    private String uid;
    private String description;
    private Double quantContenu;
    private String produitUid;
    private String updatedAt;

    public MesureUpsertDto() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getQuantContenu() {
        return quantContenu;
    }

    public void setQuantContenu(Double quantContenu) {
        this.quantContenu = quantContenu;
    }

    public String getProduitUid() {
        return produitUid;
    }

    public void setProduitUid(String produitUid) {
        this.produitUid = produitUid;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}