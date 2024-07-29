

package data.helpers;

import java.util.Date;
import java.util.Objects;

public class VuStock {
    private double quantEntree = 0.0D;
    private double quantSortie = 0.0D;
    private double quantStock = 0.0D;
    private double coutUnitEntree;
    private double coutUnitSortie;
    private double coutUnitStock;
    private double totalEntree;
    private double totalSortie;
    private double totalStock;
    private String nomProduit;
    private Date date;
    private String libelle;
    private String codeBar;
    private double stockAlert = 0.0D;

    public VuStock() {
    }

    public VuStock(String nomProduit, Date date, String libelle, String codeBar) {
        this.nomProduit = nomProduit;
        this.date = date;
        this.libelle = libelle;
        this.codeBar = codeBar;
    }

    public VuStock(double coutUnitEntree, double coutUnitSortie, double coutUnitStock, double totalEntree, double totalSortie, double totalStock, String nomProduit, Date date, String libelle, String codeBar) {
        this.coutUnitEntree = coutUnitEntree;
        this.coutUnitSortie = coutUnitSortie;
        this.coutUnitStock = coutUnitStock;
        this.totalEntree = totalEntree;
        this.totalSortie = totalSortie;
        this.totalStock = totalStock;
        this.nomProduit = nomProduit;
        this.date = date;
        this.libelle = libelle;
        this.codeBar = codeBar;
    }

    public String getCodeBar() {
        return this.codeBar;
    }

    public void setCodeBar(String codeBar) {
        this.codeBar = codeBar;
    }

    public String getNomProduit() {
        return this.nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLibelle() {
        return this.libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.codeBar);
        return hash;
    }

   

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            VuStock other = (VuStock)obj;
            return Objects.equals(this.codeBar, other.codeBar);
        }
    }

    public double getStockAlert() {
        return this.stockAlert;
    }

    public void setStockAlert(double stockAlert) {
        this.stockAlert = stockAlert;
    }

    public double getCoutUnitEntree() {
        return this.coutUnitEntree;
    }

    public void setCoutUnitEntree(double coutUnitEntree) {
        this.coutUnitEntree = coutUnitEntree;
    }

    public double getCoutUnitSortie() {
        return this.coutUnitSortie;
    }

    public void setCoutUnitSortie(double coutUnitSortie) {
        this.coutUnitSortie = coutUnitSortie;
    }

    public double getCoutUnitStock() {
        return this.coutUnitStock;
    }

    public void setCoutUnitStock(double coutUnitStock) {
        this.coutUnitStock = coutUnitStock;
    }

    public double getTotalEntree() {
        return this.totalEntree;
    }

    public void setTotalEntree(double totalEntree) {
        this.totalEntree = totalEntree;
    }

    public double getTotalSortie() {
        return this.totalSortie;
    }

    public void setTotalSortie(double totalSortie) {
        this.totalSortie = totalSortie;
    }

    public double getTotalStock() {
        return this.totalStock;
    }

    public void setTotalStock(double totalStock) {
        this.totalStock = totalStock;
    }

    public double getQuantEntree() {
        return this.quantEntree;
    }

    public void setQuantEntree(double quantEntree) {
        this.quantEntree = quantEntree;
    }

    public double getQuantSortie() {
        return this.quantSortie;
    }

    public void setQuantSortie(double quantSortie) {
        this.quantSortie = quantSortie;
    }

    public double getQuantStock() {
        return this.quantStock;
    }

    public void setQuantStock(double quantStock) {
        this.quantStock = quantStock;
    }
}

