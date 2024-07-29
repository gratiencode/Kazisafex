/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import data.Destocker;
import data.Produit;
import data.Stocker;

/**
 *
 * @author eroot
 */
public class InventoryItem {
    private Produit produit;
    private List<Stocker> stocker;
    private List<Destocker> destocker;
    private Stocker lastStocker;
    private Destocker lastDestocker;
    String localisation;
    Date datexpir;
    private String periode;
    private String quantEntree;
    private String quantSortie;
    private String quantRest;
    private String stockAlerte;
    private String valeurStock;

    public InventoryItem() {
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public List<Stocker> getStocker() {
        return stocker;
    }

    public void setStocker(List<Stocker> stocker) {
        this.stocker = stocker;
    }

    public List<Destocker> getDestocker() {
        return destocker;
    }

    public void setDestocker(List<Destocker> destocker) {
        this.destocker = destocker;
    }

    public Stocker getLastStocker() {
        return lastStocker;
    }

    public void setLastStocker(Stocker lastStocker) {
        this.lastStocker = lastStocker;
    }

    public Destocker getLastDestocker() {
        return lastDestocker;
    }

    public void setLastDestocker(Destocker lastDestocker) {
        this.lastDestocker = lastDestocker;
    }

    public String getPeriode() {
        return periode;
    }

    public void setPeriode(String periode) {
        this.periode = periode;
    }

    public String getQuantEntree() {
        return quantEntree;
    }

    public void setQuantEntree(String quantEntree) {
        this.quantEntree = quantEntree;
    }

    public String getQuantSortie() {
        return quantSortie;
    }

    public void setQuantSortie(String quantSortie) {
        this.quantSortie = quantSortie;
    }

    public String getQuantRest() {
        return quantRest;
    }

    public void setQuantRest(String quantRest) {
        this.quantRest = quantRest;
    }

    public String getStockAlerte() {
        return stockAlerte;
    }

    public void setStockAlerte(String stockAlerte) {
        this.stockAlerte = stockAlerte;
    }

    public String getValeurStock() {
        return valeurStock;
    }

    public void setValeurStock(String valeurStock) {
        this.valeurStock = valeurStock;
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
        final InventoryItem other = (InventoryItem) obj;
        if (!Objects.equals(this.produit, other.produit)) {
            return false;
        }
        return true;
    }

   
    
    
}
