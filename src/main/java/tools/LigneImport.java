/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.io.Serializable;
import java.util.List;
import data.Destocker;
import data.Mesure;
import data.PrixDeVente;
import data.Produit;
import data.Recquisition;
import data.Stocker;

/**
 *
 * @author eroot
 */
public class LigneImport implements Serializable{
    private Produit produit;
    private Stocker stocker;
    private Destocker destocker;
    private Recquisition recquisition;
    private Mesure mesure;
    private List<PrixDeVente> salesPrices;

    public LigneImport() {
    }

    public Stocker getStocker() {
        return stocker;
    }

    public void setStocker(Stocker stocker) {
        this.stocker = stocker;
    }

    public Destocker getDestocker() {
        return destocker;
    }

    public void setDestocker(Destocker destocker) {
        this.destocker = destocker;
    }

    public Recquisition getRecquisition() {
        return recquisition;
    }

    public void setRecquisition(Recquisition recquisition) {
        this.recquisition = recquisition;
    }

    public List<PrixDeVente> getSalesPrices() {
        return salesPrices;
    }

    public void setSalesPrices(List<PrixDeVente> sellPrices) {
        this.salesPrices = sellPrices;
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
    
    
    
}
