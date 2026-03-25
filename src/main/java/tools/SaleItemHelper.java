/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tools;

import java.io.Serializable;

/**
 *
 * @author endeleya
 */
public class SaleItemHelper implements Serializable {
    private int venteId;
    private long uid;
    private String productId;
    private String mesureId;
    private double montantUsd;
    private double montantCdf;
    private String clientId;
    private double salePrice;
    private double quantite;
    private String numlot;

    public SaleItemHelper() {
    }

    public SaleItemHelper(int venteId, long uid) {
        this.venteId = venteId;
        this.uid = uid;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public int getVenteId() {
        return venteId;
    }

    public void setVenteId(int venteId) {
        this.venteId = venteId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getMesureId() {
        return mesureId;
    }

    public void setMesureId(String mesureId) {
        this.mesureId = mesureId;
    }

    public double getMontantUsd() {
        return montantUsd;
    }

    public void setMontantUsd(double montantUsd) {
        this.montantUsd = montantUsd;
    }

    public double getMontantCdf() {
        return montantCdf;
    }

    public void setMontantCdf(double montantCdf) {
        this.montantCdf = montantCdf;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(double salePrice) {
        this.salePrice = salePrice;
    }

    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }

    public String getNumlot() {
        return numlot;
    }

    public void setNumlot(String numlot) {
        this.numlot = numlot;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + this.venteId;
        hash = 71 * hash + (int) (this.uid ^ (this.uid >>> 32));
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
        final SaleItemHelper other = (SaleItemHelper) obj;
        if (this.venteId != other.venteId) {
            return false;
        }
        return this.uid == other.uid;
    }
    
    
    
    
    
    
}
