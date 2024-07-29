/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import data.Mesure;

/**
 *
 * @author eroot
 */
public class FicheItem implements Comparator<FicheItem>{
    private String uidRef;
    private String uidProduit;
    private String destination;
    private String libelles;
    private Mesure mesure;
    private Date date;
    private double quantiteEntree;
    private double quantiteSortie;
    private double quantiteRestant;
    private double prixUnitEntree;
    private double coutUnitaireSortie;
    private double coutTotalEntree;
    private double coutTotalSortie;
    private double coutUnitRestant;
    private double coutTotalRestant;
    

    public FicheItem() {
    }

    public FicheItem(String uidRef, String uidProduit, Date date) {
        this.uidRef = uidRef;
        this.uidProduit = uidProduit;
        this.date = date;
    }

    public FicheItem(String uidRef, String uidProduit, Date date, double quantiteEntree, double quantiteSortie, double quantiteRestant, double prixUnitEntree, double cump) {
        this.uidRef = uidRef;
        this.uidProduit = uidProduit;
        this.date = date;
        this.quantiteEntree = quantiteEntree;
        this.quantiteSortie = quantiteSortie;
        this.quantiteRestant = quantiteRestant;
        this.prixUnitEntree = prixUnitEntree;
        this.coutUnitaireSortie = cump;
    }
    
    
    

    public String getUidRef() {
        return uidRef;
    }

    public void setUidRef(String uidRef) {
        this.uidRef = uidRef;
    }

    public String getUidProduit() {
        return uidProduit;
    }

    public void setUidProduit(String uidProduit) {
        this.uidProduit = uidProduit;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getQuantiteEntree() {
        return quantiteEntree;
    }

    public void setQuantiteEntree(double quantiteEntree) {
        this.quantiteEntree = quantiteEntree;
    }

    public double getQuantiteSortie() {
        return quantiteSortie;
    }

    public void setQuantiteSortie(double quantiteSortie) {
        this.quantiteSortie = quantiteSortie;
    }

    public double getQuantiteRestant() {
        return quantiteRestant;
    }

    public void setQuantiteRestant(double quantiteRestant) {
        this.quantiteRestant = quantiteRestant;
    }

    public double getPrixUnitEntree() {
        return prixUnitEntree;
    }

    public void setPrixUnitEntree(double prixUnitEntree) {
        this.prixUnitEntree = prixUnitEntree;
    }

    public double getCoutUnitaireSortie() {
        return coutUnitaireSortie;
    }

    public void setCoutUnitaireSortie(double cump) {
        this.coutUnitaireSortie = cump;
    }

   

    public Mesure getMesure() {
        return mesure;
    }

    public void setMesure(Mesure mesure) {
        this.mesure = mesure;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getLibelles() {
        return libelles;
    }

    public void setLibelles(String libelles) {
        this.libelles = libelles;
    }

    public double getCoutTotalEntree() {
        return coutTotalEntree;
    }

    public void setCoutTotalEntree(double coutTotalEntree) {
        this.coutTotalEntree = coutTotalEntree;
    }

    public double getCoutTotalSortie() {
        return coutTotalSortie;
    }

    public void setCoutTotalSortie(double coutTotalSortie) {
        this.coutTotalSortie = coutTotalSortie;
    }

    public double getCoutUnitRestant() {
        return coutUnitRestant;
    }

    public void setCoutUnitRestant(double coutUnitRestant) {
        this.coutUnitRestant = coutUnitRestant;
    }

    public double getCoutTotalRestant() {
        return coutTotalRestant;
    }

    public void setCoutTotalRestant(double coutTotalRestant) {
        this.coutTotalRestant = coutTotalRestant;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.date);
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
        final FicheItem other = (FicheItem) obj;
        if (!Objects.equals(this.date, other.date)) {
            return false;
        }
        return true;
    }

    @Override
    public int compare(FicheItem o1, FicheItem o2) {
        return o1.date.compareTo(o2.date);
    }
    
    
    
    
    
}
