/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.util.Objects;
import data.Destocker;
import data.PrixDeVente;
import data.Produit;
import data.Recquisition;
import data.Stocker;

/**
 *
 * @author eroot
 */
public class DataImporter {
    private Produit product;
    private Stocker stock;
    private Destocker destockage;
    private Recquisition recquisition;
    private PrixDeVente salePrice;
    private String extra;

    public DataImporter() {
    }

    public DataImporter(Produit product, Stocker stock, Destocker destockage, Recquisition recquisition, PrixDeVente salePrice) {
        this.product = product;
        this.stock = stock;
        this.destockage = destockage;
        this.recquisition = recquisition;
        this.salePrice = salePrice;
    }

    public PrixDeVente getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(PrixDeVente salePrice) {
        this.salePrice = salePrice;
    }

    public Produit getProduct() {
        return product;
    }

    public void setProduct(Produit product) {
        this.product = product;
    }

    public Stocker getStock() {
        return stock;
    }

    public void setStock(Stocker stock) {
        this.stock = stock;
    }

    public Destocker getDestockage() {
        return destockage;
    }

    public void setDestockage(Destocker destockage) {
        this.destockage = destockage;
    }

    public Recquisition getRecquisition() {
        return recquisition;
    }

    public void setRecquisition(Recquisition recquisition) {
        this.recquisition = recquisition;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.product);
        hash = 59 * hash + Objects.hashCode(this.salePrice);
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
        final DataImporter other = (DataImporter) obj;
        if (!Objects.equals(this.product, other.product)) {
            return false;
        }
        if (!Objects.equals(this.salePrice, other.salePrice)) {
            return false;
        }
        return true;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
    
    
    
}
