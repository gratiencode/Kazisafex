/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.util.Date;
import java.util.List;
import data.Client;
import data.LigneVente;
import data.Mesure;
import data.Produit;

/**
 *
 * @author eroot
 */
public class SaleItem {
    private int idVente;
    private double saleAmountUsd;
    private double saleAmountCdf;
    private double saleAmountCredit;
    private Date date;
    private Date dateEcheance;
    private String facture;
    private String libelle;
    private Produit produit;
    private Mesure mesure;
    private double quantite;
    private double unitPrice;
    private double totalCost;
    private Client client;
    private List<LigneVente> items;

    public SaleItem() {
    }

    public SaleItem(Date date, String facture, Produit produit, double quantite, double unitPrice, double totalCost, Client client) {
        this.date = date;
        this.facture = facture;
        this.produit = produit;
        this.quantite = quantite;
        this.unitPrice = unitPrice;
        this.totalCost = totalCost;
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getFacture() {
        return facture;
    }

    public void setFacture(String facture) {
        this.facture = facture;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public int getIdVente() {
        return idVente;
    }

    public void setIdVente(int idVente) {
        this.idVente = idVente;
    }
    
    

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + this.idVente;
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
        final SaleItem other = (SaleItem) obj;
        if (this.idVente != other.idVente) {
            return false;
        }
        return true;
    }

    public Mesure getMesure() {
        return mesure;
    }

    public void setMesure(Mesure mesure) {
        this.mesure = mesure;
    }

    public double getSaleAmountUsd() {
        return saleAmountUsd;
    }

    public void setSaleAmountUsd(double saleAmountUsd) {
        this.saleAmountUsd = saleAmountUsd;
    }

    public double getSaleAmountCdf() {
        return saleAmountCdf;
    }

    public void setSaleAmountCdf(double saleAmountCdf) {
        this.saleAmountCdf = saleAmountCdf;
    }

    public double getSaleAmountCredit() {
        return saleAmountCredit;
    }

    public void setSaleAmountCredit(double saleAmountCredit) {
        this.saleAmountCredit = saleAmountCredit;
    }

    public Date getDateEcheance() {
        return dateEcheance;
    }

    public void setDateEcheance(Date dateEcheance) {
        this.dateEcheance = dateEcheance;
    }

    public List<LigneVente> getItems() {
        return items;
    }

    public void setItems(List<LigneVente> items) {
        this.items = items;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
    
    
    
}
