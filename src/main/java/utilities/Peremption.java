/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author eroot
 */
public class Peremption {
    private String produitUid;
    private String codebar;
    private String produit;
    private String lot;
    private String localisation;
    private String mesure;
    private String region;
    private double quantite;
    private double coutAchat;
    private double valeur;
    private LocalDate dateExpiry;

    public Peremption() {
    }

    public Peremption(String codebar, String produit, String lot, String localisation, String region, double quantite, double coutAchat, double valeur, LocalDate dateExpiry) {
        this.codebar = codebar;
        this.produit = produit;
        this.lot = lot;
        this.localisation = localisation;
        this.region = region;
        this.quantite = quantite;
        this.coutAchat = coutAchat;
        this.valeur = valeur;
        this.dateExpiry = dateExpiry;
    }

    public Peremption(String codebar, String lot) {
        this.codebar = codebar;
        this.lot = lot;
    }

    public String getLot() {
        return lot;
    }

    public void setLot(String lot) {
        this.lot = lot;
    }

    public String getCodebar() {
        return codebar;
    }

    public void setCodebar(String codebar) {
        this.codebar = codebar;
    }

    public String getProduit() {
        return produit;
    }

    public void setProduit(String produit) {
        this.produit = produit;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }

    public double getCoutAchat() {
        return coutAchat;
    }

    public void setCoutAchat(double coutAchat) {
        this.coutAchat = coutAchat;
    }

    public double getValeur() {
        return valeur;
    }

    public void setValeur(double valeur) {
        this.valeur = valeur;
    }

    public LocalDate getDateExpiry() {
        return dateExpiry;
    }

    public void setDateExpiry(LocalDate dateExpiry) {
        this.dateExpiry = dateExpiry;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.codebar);
        hash = 73 * hash + Objects.hashCode(this.lot);
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
        final Peremption other = (Peremption) obj;
        if (!Objects.equals(this.codebar, other.codebar)) {
            return false;
        }
        if (!Objects.equals(this.lot, other.lot)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Peremption{" + "codebar=" + codebar + ", produit=" + produit + ", lot=" + lot + ", localisation=" + localisation + ", region=" + region + ", quantite=" + quantite + ", coutAchat=" + coutAchat + ", valeur=" + valeur + ", dateExpiry=" + dateExpiry + '}';
    }

    public String getMesure() {
        return mesure;
    }

    public void setMesure(String mesure) {
        this.mesure = mesure;
    }

    public String getProduitUid() {
        return produitUid;
    }

    public void setProduitUid(String produitUid) {
        this.produitUid = produitUid;
    }
    
    
    
}
