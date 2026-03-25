/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.util.Objects;
import data.Mesure;
import data.Produit;

/**
 *
 * @author eroot
 */
public class Rupture {
    private Produit produit;
    private double quant;
    private double unitprice;
    private String sale_price;
    private String localisation;
    private String date;
    private Mesure mesure;
    private String region;
    private double alert;
    private boolean select;

    public Rupture() {
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public double getQuant() {
        return quant;
    }

    public void setQuant(double quant) {
        this.quant = quant;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Mesure getMesure() {
        return mesure;
    }

    public void setMesure(Mesure mesure) {
        this.mesure = mesure;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public double getAlert() {
        return alert;
    }

    public void setAlert(double alert) {
        this.alert = alert;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.produit);
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
        final Rupture other = (Rupture) obj;
        if (!Objects.equals(this.produit, other.produit)) {
            return false;
        }
        return true;
    }

    public double getUnitprice() {
        return unitprice;
    }

    public void setUnitprice(double unitprice) {
        this.unitprice = unitprice;
    }

    

    public String getSalePrice() {
        return sale_price;
    }

    public void setSalePrice(String sale_price) {
        this.sale_price = sale_price;
    }
    
    
    
}
