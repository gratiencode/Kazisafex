/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.util.Objects;
import data.Mesure;
import data.PrixDeVente;
import data.Produit;

/**
 *
 * @author eroot
 */
public class CartItem {

    private Produit produit;
    private double quantite;
    private PrixDeVente prixDeVente;
    private Mesure mesure;
    private double prixTotal;

    public CartItem() {
    }

    public CartItem(Produit produit, double quantite, PrixDeVente prixDeVente, Mesure mesure, double prixTotal) {
        this.produit = produit;
        this.quantite = quantite;
        this.prixDeVente = prixDeVente;
        this.mesure = mesure;
        this.prixTotal = prixTotal;
    }

    public double getPrixTotal() {
        return prixTotal;
    }

    public void setPrixTotal(double prixTotal) {
        this.prixTotal = prixTotal;
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

    public PrixDeVente getPrixDeVente() {
        return prixDeVente;
    }

    public void setPrixDeVente(PrixDeVente prixDeVente) {
        this.prixDeVente = prixDeVente;
    }

    public Mesure getMesure() {
        return mesure;
    }

    public void setMesure(Mesure mesure) {
        this.mesure = mesure;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.produit);
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
        final CartItem other = (CartItem) obj;
        if (!Objects.equals(this.produit, other.produit)) {
            return false;
        }
        return true;
    }
    
    

   
}
