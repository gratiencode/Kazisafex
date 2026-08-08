/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the editor in the template.
 */
package tools;

import data.Destocker;
import java.time.LocalDateTime;

/**
 * Modèle d'affichage du rapport "Sortie en stock par référence" dans
 * l'entrepôt. Les noeuds REFERENCE agrègent les totaux de leurs destocker
 * (feuilles LIGNE).
 *
 * @author eroot
 */
public class DestockItem {

    /**
     * Niveau d'affichage dans le rapport (référence → lignes de déstockage).
     */
    public enum Niveau {
        /**
         * Noeud agrégat : une référence regroupe plusieurs déstockages.
         */
        REFERENCE,
        /**
         * Feuille : un déstockage brut (une instance Destocker).
         */
        LIGNE
    }

    private Niveau niveau = Niveau.LIGNE;

    private Destocker destocker;

    private String reference;
    private String destination;
    private String region;
    private LocalDateTime dateDestockage;
    private String produit;
    private String numlot;

    private int lignes;
    private double quantite;
    private double coutAchat;
    private double coutTotal;

    public DestockItem() {
    }

    public DestockItem(Destocker destocker) {
        this.niveau = Niveau.LIGNE;
        this.destocker = destocker;
    }

    public Niveau getNiveau() {
        return niveau;
    }

    public void setNiveau(Niveau niveau) {
        this.niveau = niveau != null ? niveau : Niveau.LIGNE;
    }

    public Destocker getDestocker() {
        return destocker;
    }

    public void setDestocker(Destocker destocker) {
        this.destocker = destocker;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public LocalDateTime getDateDestockage() {
        return dateDestockage;
    }

    public void setDateDestockage(LocalDateTime dateDestockage) {
        this.dateDestockage = dateDestockage;
    }

    public String getProduit() {
        return produit;
    }

    public void setProduit(String produit) {
        this.produit = produit;
    }

    public String getNumlot() {
        return numlot;
    }

    public void setNumlot(String numlot) {
        this.numlot = numlot;
    }

    public int getLignes() {
        return lignes;
    }

    public void setLignes(int lignes) {
        this.lignes = lignes;
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

}
