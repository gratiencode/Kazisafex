package data.dto;

/**
 * DTO upsync asymetrique Produit : FKs a plat + champs scalaires mutes.
 * L'image BLOB est volontairement exclue (payload leger) ; elle sera poussee
 * a part par le flux d'upload dedie.
 */
public class ProduitUpsertDto {

    private String uid;
    private String codebar;
    private String nomProduit;
    private String marque;
    private String modele;
    private String taille;
    private String couleur;
    private String methodeInventaire;
    private String categoryUid;
    private String dateCreation;
    private String updatedAt;

    public ProduitUpsertDto() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCodebar() {
        return codebar;
    }

    public void setCodebar(String codebar) {
        this.codebar = codebar;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public String getTaille() {
        return taille;
    }

    public void setTaille(String taille) {
        this.taille = taille;
    }

    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    public String getMethodeInventaire() {
        return methodeInventaire;
    }

    public void setMethodeInventaire(String methodeInventaire) {
        this.methodeInventaire = methodeInventaire;
    }

    public String getCategoryUid() {
        return categoryUid;
    }

    public void setCategoryUid(String categoryUid) {
        this.categoryUid = categoryUid;
    }

    public String getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(String dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}