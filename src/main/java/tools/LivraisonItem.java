/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.util.Objects;
import data.Livraison;
import data.Mesure;
import data.Produit;

/**
 *
 * @author eroot
 */
public class LivraisonItem {
    private int lotcount;
    private Produit produit;
    private double quantite;
    private double sommeTotal;
    private Mesure mesure;
    private Livraison livraison;

    public LivraisonItem() {
    }

    public LivraisonItem(int lotcount, Produit produit, double quantite, Mesure mesure, Livraison livraison) {
        this.lotcount = lotcount;
        this.produit = produit;
        this.quantite = quantite;
        this.mesure = mesure;
        this.livraison = livraison;
    }

    public Livraison getLivraison() {
        return livraison;
    }

    public void setLivraison(Livraison livraison) {
        this.livraison = livraison;
    }

    public int getLotcount() {
        return lotcount;
    }

    public void setLotcount(int lotcount) {
        this.lotcount = lotcount;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }

    public Mesure getMesure() {
        return mesure;
    }
    
    

    public void setMesure(Mesure mesure) {
        this.mesure = mesure;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.produit);
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
        final LivraisonItem other = (LivraisonItem) obj;
        if (!Objects.equals(this.produit, other.produit)) {
            return false;
        }
        return true;
    }   

    public double getSommeTotal() {
        return sommeTotal;
    }

    public void setSommeTotal(double sommeTotal) {
        this.sommeTotal = sommeTotal;
    }
    
    
}
