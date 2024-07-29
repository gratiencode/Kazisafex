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

/**
 *
 * @author eroot
 */
public class ListViewItem {
    private Produit produit;
    private Double quantiteRestant;
    private Double salePrice;
    private Double purchasePrice;
    private Double detailPrice;
    private Mesure mesureGros,mesureAchat,mesureDetail;
    private Double coutAchat;
    private Date peremption;
    private String numlot;
    private String achatQuantity;

    public ListViewItem() {
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public Double getQuantiteRestant() {
        return quantiteRestant;
    }

    public void setQuantiteRestant(Double quantiteRestant) {
        this.quantiteRestant = quantiteRestant;
    }

    public Double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Double salePrice) {
        this.salePrice = salePrice;
    }

    public Double getCoutAchat() {
        return coutAchat;
    }

    public void setCoutAchat(Double coutAchat) {
        this.coutAchat = coutAchat;
    }

    public Date getPeremption() {
        return peremption;
    }

    public void setPeremption(Date peremption) {
        this.peremption = peremption;
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
        hash = 43 * hash + Objects.hashCode(this.produit);
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
        final ListViewItem other = (ListViewItem) obj;
        if (!Objects.equals(this.produit, other.produit)) {
            return false;
        }
        return true;
    }

    public Double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(Double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Double getDetailPrice() {
        return detailPrice;
    }

    public void setDetailPrice(Double detailPrice) {
        this.detailPrice = detailPrice;
    }

    public Mesure getMesureGros() {
        return mesureGros;
    }

    public void setMesureGros(Mesure mesureGros) {
        this.mesureGros = mesureGros;
    }

    public Mesure getMesureAchat() {
        return mesureAchat;
    }

    public void setMesureAchat(Mesure mesureAchat) {
        this.mesureAchat = mesureAchat;
    }

    public Mesure getMesureDetail() {
        return mesureDetail;
    }

    public void setMesureDetail(Mesure mesureDetail) {
        this.mesureDetail = mesureDetail;
    }

    public String getAchatQuantity() {
        return achatQuantity;
    }

    public void setAchatQuantity(String achatQuantity) {
        this.achatQuantity = achatQuantity;
    }
    
    
    
}
