/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.time.LocalDate;
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

    // Additional fields for StockDepotAgregate integration
    private String productId;
    private String productName;
    private double quantite;
    private double coutAchat;
    private String region;
    private LocalDate date;
    private double stockInitial;
    private double quantEntreeValue;
    private double quantSortieValue;
    private double quantRestValue;
    private double valeurStockValue;
    private double stockAlerteValue;
    private String mesureLabel;
    private double quantContenu = 1d;
    private LocalDate expiryDate;

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

    // Additional getters/setters for StockDepot integration
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }

    public double getCoutAchat() {
        return coutAchat;
    }

    public void setCoutAchat(double coutAchat) {
        this.coutAchat = coutAchat;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getStockInitial() {
        return stockInitial;
    }

    public void setStockInitial(double stockInitial) {
        this.stockInitial = stockInitial;
    }

    public double getQuantEntreeValue() {
        return quantEntreeValue;
    }

    public void setQuantEntreeValue(double quantEntreeValue) {
        this.quantEntreeValue = quantEntreeValue;
    }

    public double getQuantSortieValue() {
        return quantSortieValue;
    }

    public void setQuantSortieValue(double quantSortieValue) {
        this.quantSortieValue = quantSortieValue;
    }

    public double getQuantRestValue() {
        return quantRestValue;
    }

    public void setQuantRestValue(double quantRestValue) {
        this.quantRestValue = quantRestValue;
    }

    public double getValeurStockValue() {
        return valeurStockValue;
    }

    public void setValeurStockValue(double valeurStockValue) {
        this.valeurStockValue = valeurStockValue;
    }

    public double getStockAlerteValue() {
        return stockAlerteValue;
    }

    public void setStockAlerteValue(double stockAlerteValue) {
        this.stockAlerteValue = stockAlerteValue;
    }

    public String getMesureLabel() {
        return mesureLabel;
    }

    public void setMesureLabel(String mesureLabel) {
        this.mesureLabel = mesureLabel;
    }

    public double getQuantContenu() {
        return quantContenu;
    }

    public void setQuantContenu(double quantContenu) {
        this.quantContenu = quantContenu;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    // ── Lot-aware display fields ──────────────────────────────────────────────
    private String numlot;
    private String dateExpirStr;

    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }

    public String getNumlot() { return numlot; }
    public void setNumlot(String numlot) { this.numlot = numlot; }

    public String getDateExpir() { return dateExpirStr; }
    public void setDateExpir(String dateExpirStr) { this.dateExpirStr = dateExpirStr; }

    @Override
    public int hashCode() {
        return Objects.hash(this.produit, this.numlot, this.region);
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
        return Objects.equals(this.produit, other.produit)
                && Objects.equals(this.numlot, other.numlot)
                && Objects.equals(this.region, other.region);
    }

}
