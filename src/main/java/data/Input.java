/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;


import java.io.Serializable;
import java.util.List;
import jakarta.json.bind.annotation.JsonbTransient;
 import org.hibernate.annotations.UuidGenerator; import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author eroot
 */
 @XmlRootElement 
public class Input extends BaseModel implements Serializable{
    private Produit produit;
    private Stocker stock;
    private Destocker destocker;
    private Recquisition recquisition;
    private Livraison livraison;
    private List<PrixDeVente> prixdevents;
    private List<PVUnit> pvus;
    public Input() {
        
    }

     @JsonbTransient 
     public List<PVUnit> getPvus() {
        return pvus;
    }

    public void setPvus(List<PVUnit> pvus) {
        this.pvus = pvus;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public Stocker getStock() {
        return stock;
    }

    public void setStock(Stocker stock) {
        this.stock = stock;
    }

     @JsonbTransient public List<PrixDeVente> getPrixdevents() {
        return prixdevents;
    }

     @JsonbTransient public List<PVUnit> getReturnedPrices() {
        return pvus;
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

    public Livraison getLivraison() {
        return livraison;
    }

    public void setLivraison(Livraison livraison) {
        this.livraison = livraison;
    }

    public void setPrixdevents(List<PrixDeVente> prixdevents) {
        this.prixdevents = prixdevents;
    }
    
    
}
