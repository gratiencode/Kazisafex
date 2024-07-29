/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import data.Mesure;
import data.PrixDeVente;
import data.Produit;

/**
 *
 * @author eroot
 */
public class RequestHelper implements Comparator<RequestHelper>{
    private String requid;
    private Produit productId;
    private Date dateReq;
    private double stockAlerte;
    private Date dateExpiry;
    private double coutAchat;
    private double quantite;
    private Mesure mesureId;
    
    private List<PrixDeVente> prices;

    public RequestHelper() {
    }
    
    @Override
    public int compare(RequestHelper o1, RequestHelper o2) {
        return o1.dateReq.compareTo(o2.dateReq);
    }

    public String getRequid() {
        return requid;
    }

    public void setRequid(String requid) {
        this.requid = requid;
    }

    public Produit getProductId() {
        return productId;
    }

    public void setProductId(Produit productId) {
        this.productId = productId;
    }

    public Date getDateReq() {
        return dateReq;
    }

    public void setDateReq(Date dateReq) {
        this.dateReq = dateReq;
    }

    public double getCoutAchat() {
        return coutAchat;
    }

    public void setCoutAchat(double coutAchat) {
        this.coutAchat = coutAchat;
    }

    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }

    public Mesure getMesureId() {
        return mesureId;
    }

    public void setMesureId(Mesure mesureId) {
        this.mesureId = mesureId;
    }

    public List<PrixDeVente> getPrices() {
        return prices;
    }

    public void setPrices(List<PrixDeVente> prices) {
        this.prices = prices;
    }
    
    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.requid);
        hash = 17 * hash + Objects.hashCode(this.productId);
        hash = 17 * hash + Objects.hashCode(this.mesureId);
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
        final RequestHelper other = (RequestHelper) obj;
        if (!Objects.equals(this.requid, other.requid)) {
            return false;
        }
        if (!Objects.equals(this.productId, other.productId)) {
            return false;
        }
        if (!Objects.equals(this.mesureId, other.mesureId)) {
            return false;
        }
        return true;
    }

    public double getStockAlerte() {
        return stockAlerte;
    }

    public void setStockAlerte(double stockAlerte) {
        this.stockAlerte = stockAlerte;
    }

    public Date getDateExpiry() {
        return dateExpiry;
    }

    public void setDateExpiry(Date dateExpiry) {
        this.dateExpiry = dateExpiry;
    }

    
}
