/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;
import java.time.LocalDate;
import java.util.Objects;
import data.Category;
import data.Client;
import data.Mesure;

/**
 *
 * @author eroot
 */
public class VenteReporter {
    private Category category;
    private Client client;
    private LocalDate date;
    private String produit;
    private String codebar;
    private double chiffre;
    private double quantiteVendu;
    private double sommeTotal;
    private Mesure mesure;
    private double stockInitial;
    private double entrees;
    private double stockFinal;
    private double ecart;
    private double retour;
    private double coutAchat;
    private double marge;
  
    public VenteReporter() {
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getProduit() {
        return produit;
    }

    public void setProduit(String produit) {
        this.produit = produit;
    }

    public double getChiffre() {
        return chiffre;
    }

    public void setChiffre(double chiffre) {
        this.chiffre = chiffre;
    }

    public String getCodebar() {
        return codebar;
    }

    public void setCodebar(String codebar) {
        this.codebar = codebar;
    }
    

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.codebar);
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
        final VenteReporter other = (VenteReporter) obj;
        if (!Objects.equals(this.codebar, other.codebar)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "VentePerProduct{" + "date=" + date + ", produit=" + produit + ", codebar=" + codebar + ", chiffre=" + chiffre + '}';
    }

    public double getQuantiteVendu() {
        return quantiteVendu;
    }

    public void setQuantiteVendu(double quantite) {
        this.quantiteVendu = quantite;
    }

    public Mesure getMesure() {
        return mesure;
    }

    public void setMesure(Mesure mesure) {
        this.mesure = mesure;
    }

    public double getSommeTotal() {
        return sommeTotal;
    }

    public void setSommeTotal(double sommeTotal) {
        this.sommeTotal = sommeTotal;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public double getStockInitial() {
        return stockInitial;
    }

    public void setStockInitial(double stockInitial) {
        this.stockInitial = stockInitial;
    }

    public double getEntrees() {
        return entrees;
    }

    public void setEntrees(double entrees) {
        this.entrees = entrees;
    }

    public double getStockFinal() {
        return stockFinal;
    }

    public void setStockFinal(double stockFinal) {
        this.stockFinal = stockFinal;
    }

    public double getEcart() {
        return ecart;
    }

    public void setEcart(double ecart) {
        this.ecart = ecart;
    }

    public double getRetour() {
        return retour;
    }

    public void setRetour(double retour) {
        this.retour = retour;
    }

    public double getCoutAchat() {
        return coutAchat;
    }

    public void setCoutAchat(double coutAchat) {
        this.coutAchat = coutAchat;
    }

    public double getMarge() {
        return marge;
    }

    public void setMarge(double marge) {
        this.marge = marge;
    }
    
    
    
}
