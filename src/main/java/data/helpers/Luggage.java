/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.helpers;


import data.Category;
import data.Destocker;
import data.Vente;
import data.Client;
import data.ClientAppartenir;
import data.Facture;
import data.Depense;
import data.Recquisition;
import data.PrixDeVente;
import data.LigneVente;
import data.Fournisseur;
import data.Operation;
import data.Aretirer;
import data.Mesure;
import data.RetourMagasin;
import data.Traisorerie;
import data.Livraison;
import data.ClientOrganisation;
import data.Stocker;
import data.Produit;
import data.CompteTresor;
import data.RetourDepot;
import java.util.List;

/**
 *
 * @author eroot
 */
public class Luggage {
    private String type;
    private List<Category> categories;
    private List<Produit> produits;
    private List<Mesure> mesures;
    private List<Fournisseur> fournisseurs;
    private List<Livraison> livraisons;
    private List<Stocker> stockers;
    private List<Destocker> destockers;
    private List<Recquisition> recquisitions;
    private List<PrixDeVente> prices;
    private List<Client> clients;
    private List<Vente> ventes;
    private List<LigneVente> saleItems;
    private List<CompteTresor> tresors;
    private List<Depense> depenses;
    private List<Traisorerie> traisories;
    private List<Operation> operations;
    private List<Facture> factures;
    private List<ClientOrganisation> organisations;
    private List<ClientAppartenir> appartenances;
    private List<Aretirer> aretires;
    private List<RetourMagasin> retourMagasins;
    private List<RetourDepot> retourDepots;

    public Luggage() {
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Category> getData() {
        return categories;
    }

    public void setData(List<Category> data) {
        this.categories = data;
    }

    public List<Produit> getProduits() {
        return produits;
    }

    public void setProduits(List<Produit> produits) {
        this.produits = produits;
    }

    public List<Mesure> getMesures() {
        return mesures;
    }

    public void setMesures(List<Mesure> mesures) {
        this.mesures = mesures;
    }

    public List<Fournisseur> getFournisseurs() {
        return fournisseurs;
    }

    public void setFournisseurs(List<Fournisseur> fournisseurs) {
        this.fournisseurs = fournisseurs;
    }

    public List<Livraison> getLivraisons() {
        return livraisons;
    }

    public void setLivraisons(List<Livraison> livraisons) {
        this.livraisons = livraisons;
    }

    public List<Stocker> getStockers() {
        return stockers;
    }

    public void setStockers(List<Stocker> stockers) {
        this.stockers = stockers;
    }

    public List<Destocker> getDestockers() {
        return destockers;
    }

    public void setDestockers(List<Destocker> destockers) {
        this.destockers = destockers;
    }

    public List<Recquisition> getRecquisitions() {
        return recquisitions;
    }

    public void setRecquisitions(List<Recquisition> recquisitions) {
        this.recquisitions = recquisitions;
    }

    public List<PrixDeVente> getPrices() {
        return prices;
    }

    public void setPrices(List<PrixDeVente> prices) {
        this.prices = prices;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public List<Vente> getVentes() {
        return ventes;
    }

    public void setVentes(List<Vente> ventes) {
        this.ventes = ventes;
    }

    public List<LigneVente> getSaleItems() {
        return saleItems;
    }

    public void setSaleItems(List<LigneVente> saleItems) {
        this.saleItems = saleItems;
    }

    public List<CompteTresor> getTresors() {
        return tresors;
    }

    public void setTresors(List<CompteTresor> tresors) {
        this.tresors = tresors;
    }

    public List<Depense> getDepenses() {
        return depenses;
    }

    public void setDepenses(List<Depense> depenses) {
        this.depenses = depenses;
    }

    public List<Traisorerie> getTraisories() {
        return traisories;
    }

    public void setTraisories(List<Traisorerie> traisories) {
        this.traisories = traisories;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }

    public List<Facture> getFactures() {
        return factures;
    }

    public void setFactures(List<Facture> factures) {
        this.factures = factures;
    }

    public List<ClientOrganisation> getOrganisations() {
        return organisations;
    }

    public void setOrganisations(List<ClientOrganisation> organisations) {
        this.organisations = organisations;
    }

    public List<ClientAppartenir> getAppartenances() {
        return appartenances;
    }

    public void setAppartenances(List<ClientAppartenir> appartenances) {
        this.appartenances = appartenances;
    }

    public List<Aretirer> getAretires() {
        return aretires;
    }

    public void setAretires(List<Aretirer> aretires) {
        this.aretires = aretires;
    }

    public List<RetourMagasin> getRetourMagasins() {
        return retourMagasins;
    }

    public void setRetourMagasins(List<RetourMagasin> retourMagasins) {
        this.retourMagasins = retourMagasins;
    }

    public List<RetourDepot> getRetourDepots() {
        return retourDepots;
    }

    public void setRetourDepots(List<RetourDepot> retourDepots) {
        this.retourDepots = retourDepots;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }
    
}
