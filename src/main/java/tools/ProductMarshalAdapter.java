/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tools;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author endeleya
 */
public class ProductMarshalAdapter implements Serializable{
    private String image;
    private String nomProduit;
    private String marque;
    private String modele;
    private String taille;
    private String couleur;
    private String methodeInventaire;
    private String codebar;
    private String uid;
    private String categoryId;

    public ProductMarshalAdapter() {
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public String getCodebar() {
        return codebar;
    }

    public void setCodebar(String codebar) {
        this.codebar = codebar;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.uid);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProductMarshalAdapter other = (ProductMarshalAdapter) obj;
        return Objects.equals(this.uid, other.uid);
    }

    @Override
    public String toString() {
        return "ProductMarshalAdapter{" + "image=" + image + ", nomProduit=" + nomProduit + ", marque=" + marque + ", modele=" + modele + ", taille=" + taille + ", couleur=" + couleur + ", methodeInventaire=" + methodeInventaire + ", codebar=" + codebar + ", uid=" + uid + ", categoryId=" + categoryId + '}';
    }
    
    
    
}
