/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package data.helpers;

import data.Entreposer;
import data.Production;
import data.Produit;
import java.util.Objects;

/**
 *
 * @author endeleya
 */
public class EntreposerHelper {
    private Entreposer entreposer;
    private Production production;
    private Produit produit;
    private double quotePart=0;

    public Entreposer getEntreposer() {
        return entreposer;
    }

    public void setEntreposer(Entreposer entreposer) {
        this.entreposer = entreposer;
    }

    public Production getProduction() {
        return production;
    }

    public void setProduction(Production production) {
        this.production = production;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public double getQuotePart() {
        return quotePart;
    }

    public void setQuotePart(double quotePart) {
        this.quotePart = quotePart;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.produit);
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
        final EntreposerHelper other = (EntreposerHelper) obj;
        return Objects.equals(this.produit, other.produit);
    }
    
    
}
