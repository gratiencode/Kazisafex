/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tools;

import java.time.LocalDate;
import java.time.LocalDateTime;
import data.Mesure;
import data.Produit;
import java.util.Objects;


/**
 *
 * @author endeleya
 */
public class ComptageItem {
    private Produit produit;
    private LocalDateTime dateCompter;
    private String codeInventaire;
    private String etatInventaire;
    private LocalDate debutInventair;
    private String nomProduit;
    private double quantite;
    private double coutAchat;
    private double coutTotal;
    private double quantiteTheorik;
    private double ecart;
    private double valeurEcart;
    private String observation;
    private String numlot;
    private LocalDate dateExpiration;
    private Mesure mesure;
    

    public ComptageItem() {
    }

    public LocalDateTime getDateCompter() {
        return dateCompter;
    }

    public void setDateCompter(LocalDateTime dateCompter) {
        this.dateCompter = dateCompter;
    }

    public String getCodeInventaire() {
        return codeInventaire;
    }

    public void setCodeInventaire(String codeInventaire) {
        this.codeInventaire = codeInventaire;
    }

    public String getEtatInventaire() {
        return etatInventaire;
    }

    public void setEtatInventaire(String etatInventaire) {
        this.etatInventaire = etatInventaire;
    }

    public LocalDate getDebutInventair() {
        return debutInventair;
    }

    public void setDebutInventair(LocalDate debutInventair) {
        this.debutInventair = debutInventair;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
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

    public double getCoutTotal() {
        return coutTotal;
    }

    public void setCoutTotal(double coutTotal) {
        this.coutTotal = coutTotal;
    }

    public String getNumlot() {
        return numlot;
    }

    public void setNumlot(String numlot) {
        this.numlot = numlot;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public Mesure getMesure() {
        return mesure;
    }

    public void setMesure(Mesure mesure) {
        this.mesure = mesure;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }
    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.produit);
        hash = 67 * hash + Objects.hashCode(this.codeInventaire);
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
        final ComptageItem other = (ComptageItem) obj;
        if (!Objects.equals(this.codeInventaire, other.codeInventaire)) {
            return false;
        }
        return Objects.equals(this.produit, other.produit);
    }

    public double getQuantiteTheorik() {
        return quantiteTheorik;
    }

    public void setQuantiteTheorik(double quantiteTheorik) {
        this.quantiteTheorik = quantiteTheorik;
    }

    public double getEcart() {
        return ecart;
    }

    public void setEcart(double ecart) {
        this.ecart = ecart;
    }

    public double getValeurEcart() {
        return valeurEcart;
    }

    public void setValeurEcart(double valeurEcart) {
        this.valeurEcart = valeurEcart;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }
    
    
}
