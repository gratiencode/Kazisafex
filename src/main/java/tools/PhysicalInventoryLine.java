/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.time.LocalDate;
import java.util.Date;

/**
 *
 * @author eroot
 */
public class PhysicalInventoryLine {
    private int ligne;
    private String codebarr;
    private String nomProduit;
    private String marqueProduit;
    private String modeleProduit;
    private String tailleProduit;
    private String numlot;
    private String mesure;
    private String devise;
    private double entrees=0;
    private double sorties=0;
    private double stockTheorique=0;
    private double stockPhysique=0;
    private double stockAlerte=0;
    private double coutAchat=0;
    private double totalTheorique=0;
    private double totalPhysique=0;
    private boolean multiBatch=false;
    private double ecart=0;
    private String localisation;
    private String commentaire;
    private LocalDate dateExpiration;
    private String prixDeVente;
    private String region;

    public PhysicalInventoryLine() {
    }

    public String getCodebarr() {
        return codebarr;
    }

    public void setCodebarr(String codebarr) {
        this.codebarr = codebarr;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    public String getMarqueProduit() {
        return marqueProduit;
    }

    public void setMarqueProduit(String marqueProduit) {
        this.marqueProduit = marqueProduit;
    }

    public String getModeleProduit() {
        return modeleProduit;
    }

    public void setModeleProduit(String modeleProduit) {
        this.modeleProduit = modeleProduit;
    }

    public String getTailleProduit() {
        return tailleProduit;
    }

    public void setTailleProduit(String tailleProduit) {
        this.tailleProduit = tailleProduit;
    }

    public String getNumlot() {
        return numlot;
    }

    public void setNumlot(String numlot) {
        this.numlot = numlot;
    }

    public String getMesure() {
        return mesure;
    }

    public void setMesure(String mesure) {
        this.mesure = mesure;
    }

    public double getEntrees() {
        return entrees;
    }

    public void setEntrees(double entrees) {
        this.entrees = entrees;
    }

    public double getSorties() {
        return sorties;
    }

    public void setSorties(double sorties) {
        this.sorties = sorties;
    }

    public double getStockTheorique() {
        return stockTheorique;
    }

    public void setStockTheorique(double stockTheorique) {
        this.stockTheorique = stockTheorique;
    }

    public double getStockPhysique() {
        return stockPhysique;
    }

    public void setStockPhysique(double stockPhysique) {
        this.stockPhysique = stockPhysique;
    }

    public double getStockAlerte() {
        return stockAlerte;
    }

    public void setStockAlerte(double stockAlerte) {
        this.stockAlerte = stockAlerte;
    }

    public double getCoutAchat() {
        return coutAchat;
    }

    public void setCoutAchat(double coutAchat) {
        this.coutAchat = coutAchat;
    }

    public double getTotalTheorique() {
        return totalTheorique;
    }

    public void setTotalTheorique(double totalTheorique) {
        this.totalTheorique = totalTheorique;
    }

    public double getTotalPhysique() {
        return totalPhysique;
    }

    public void setTotalPhysique(double totalPhysique) {
        this.totalPhysique = totalPhysique;
    }

    public double getEcart() {
        return ecart;
    }

    public void setEcart(double ecart) {
        this.ecart = ecart;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public String getPrixDeVente() {
        return prixDeVente;
    }

    public void setPrixDeVente(String prixDeVente) {
        this.prixDeVente = prixDeVente;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public PhysicalInventoryLine(int ligne) {
        this.ligne = ligne;
    }

    public int getLigne() {
        return ligne;
    }

    public void setLigne(int ligne) {
        this.ligne = ligne;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.ligne;
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
        final PhysicalInventoryLine other = (PhysicalInventoryLine) obj;
        if (this.ligne != other.ligne) {
            return false;
        }
        return true;
    }

    public boolean isMultiBatch() {
        return multiBatch;
    }

    public void setMultiBatch(boolean multiBatch) {
        this.multiBatch = multiBatch;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }
    
}
