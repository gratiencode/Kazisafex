/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.util.List;
import data.Client;
import data.Vente;
import data.Produit;
import data.Mesure;
import data.LigneVente;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author eroot
 */
public class SaleItem {

    private int idVente;
    private double saleAmountUsd;
    private double saleAmountCdf;
    private double saleAmountCredit;
    private double quantite;
    private double pu;
    private String mesure;
    private String produit;
    private String facture;
    private String libelle;
    private LocalDate dateDeVente;
    private LocalDateTime dateHeureVente;
    private LocalDate datEcheance;
    private Client client;
    private List<Vente> ventes;
    private List<LigneVente> ligneVenteList;

    private long saleItemUid=0;
    private double unitPrice;
    private double totalCost;
    private Produit productObj;
    private Mesure mesureObj;


    public SaleItem() {
    }

    public SaleItem(double saleAmountUsd, double saleAmountCdf, double saleAmountCredit, String facture, String libelle, LocalDate dateDeVente, List<Vente> ventes) {
        this.saleAmountUsd = saleAmountUsd;
        this.saleAmountCdf = saleAmountCdf;
        this.saleAmountCredit = saleAmountCredit;
        this.facture = facture;
        this.libelle = libelle;
        this.dateDeVente = dateDeVente;
        this.ventes = ventes;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getFacture() {
        return facture;
    }

    public void setFacture(String facture) {
        this.facture = facture;
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

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public LocalDate getDateDeVente() {
        return dateDeVente;
    }

    public void setDateDeVente(LocalDate dateDeVente) {
        this.dateDeVente = dateDeVente;
    }

    public List<Vente> getVentes() {
        return ventes;
    }

    public void setVentes(List<Vente> ventes) {
        this.ventes = ventes;
    }

    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }

    public double getPu() {
        return pu;
    }

    public void setPu(double pu) {
        this.pu = pu;
    }

    public void setProduit(String produit) {
        this.produit = produit;
    }

    public Produit getProduit() {
        return productObj;
    }

    public String getProduitName() {
        return produit;
    }

    public String getMesure() {
        return mesure;
    }

    public Mesure getMesureObj() {
        return mesureObj;
    }

    public void setMesure(String mesure) {
        this.mesure = mesure;
    }

    public void setMesure(Mesure mesure) {
        this.mesureObj = mesure;
        this.mesure = mesure.getDescription();
    }

    public List<LigneVente> getItems() {
        return ligneVenteList;
    }

    public java.util.Date getDate() {
        if (dateHeureVente != null) {
            return java.util.Date.from(dateHeureVente.atZone(java.time.ZoneId.systemDefault()).toInstant());
        }
        if (dateDeVente != null) {
            return java.util.Date.from(dateDeVente.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    public void setItems(List<LigneVente> items) {
        this.ligneVenteList = items;
        if (items != null && !items.isEmpty()) {
            this.pu = items.get(0).getPrixUnit();
            this.quantite = items.stream().mapToDouble(LigneVente::getQuantite).sum();
            this.produit = items.get(0).getProductId().getNomProduit();
            this.mesure = items.get(0).getMesureId().getDescription();
        }
    }

//    public void setDate(LocalDateTime ldt) {
//        if (date != null) {
//            this.dateDeVente = ldt.toLocalDate();
//        }
//    }

    public void setDate(LocalDateTime date) {
        if (date != null) {
            this.dateDeVente = date.toLocalDate();
            this.dateHeureVente = date;
        }
    }


    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.pu = unitPrice;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public Produit getProductObj() {
        return productObj;
    }

    public void setProduit(Produit product) {
        this.productObj = product;
        this.produit = product.getNomProduit();
    }


    public LocalDate getDatEcheance() {
        return datEcheance;
    }

    public void setDatEcheance(LocalDate datEcheance) {
        this.datEcheance = datEcheance;
    }

    public void setDateEcheance(LocalDate datEcheance) {
        this.datEcheance = datEcheance;
    }


    public LocalDateTime getDateHeureVente() {
        return dateHeureVente;
    }

    public void setDateHeureVente(LocalDateTime dateHeureVente) {
        this.dateHeureVente = dateHeureVente;
    }

    public long getSaleItemUid() {
        return saleItemUid;
    }

    public void setSaleItemUid(long saleItemUid) {
        this.saleItemUid = saleItemUid;
    }

}
