/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.util.Date;
import java.util.Objects;
import data.Mesure;
import data.Produit;
import java.time.LocalDate;

/**
 *
 * @author eroot
 */
public class InventoryMagasin {
    private Produit produit;
    private Mesure mesure;
    private double quantEntree;
    private double quantSortie;
    private double quantStock;
    private double valeurStock;
    private double coutAchat;
    private String lot;
    private String localisation;
    private String prixDeVente;
    private double alerte;
    private Date expiry;

    public InventoryMagasin(Produit produit, Mesure mesure, double quantEntree, double quantSortie, double quantStock) {
        this.produit = produit;
        this.mesure = mesure;
        this.quantEntree = quantEntree;
        this.quantSortie = quantSortie;
        this.quantStock = quantStock;
    }

    public InventoryMagasin() {
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public Mesure getMesure() {
        return mesure;
    }

    public void setMesure(Mesure mesure) {
        this.mesure = mesure;
    }

    public double getQuantEntree() {
        return quantEntree;
    }
    
    public void setQuantEntree(double quantEntree) {
        this.quantEntree = quantEntree;
    }

    public double getQuantSortie() {
        return quantSortie;
    }

    public void setQuantSortie(double quantSortie) {
        this.quantSortie = quantSortie;
    }

    public double getQuantStock() {
        return quantStock;
    }

    public void setQuantStock(double quantStock) {
        this.quantStock = quantStock;
    }
    
    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.produit);
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
        final InventoryMagasin other = (InventoryMagasin) obj;
        if (!Objects.equals(this.produit, other.produit)) {
            return false;
        }
        return true;
    }

    public double getAlerte() {
        return alerte;
    }

    public void setAlerte(double alerte) {
        this.alerte = alerte;
    }

    public Date getExpiry() {
        return expiry;
    }

    public void setExpiry(Date expiry) {
        this.expiry = expiry;
    }

    public double getValeurStock() {
        return valeurStock;
    }

    public void setValeurStock(double valeurStock) {
        this.valeurStock = valeurStock;
    } 

    public String getLot() {
      return lot;
    }

    public double getCoutAchat() {
        return this.coutAchat;
    }

    public void setLot(String lot) {
        this.lot = lot;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

  
   
    public void setCoutAchat(double coutAchat) {
        this.coutAchat = coutAchat;
    }


    public String getLocalisation() {
        return localisation;
    }

    public String getPrixDeVente() {
        return prixDeVente;
    }

    public void setPrixDeVente(String prixDeVente) {
        this.prixDeVente = prixDeVente;
    }

  

}
