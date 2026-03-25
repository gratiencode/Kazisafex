/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package data; 

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author endeleya
 */
public class VenteHelper implements Serializable {

    private CompteTresor tresor;
    private Vente vente;
    private String transactionId;
    private Client client;
    private List<LigneVente> ligneVentes;

    public CompteTresor getTresor() {
        return tresor;
    }

    public void setTresor(CompteTresor tresor) {
        this.tresor = tresor;
    }

    public Vente getVente() {
        return vente;
    }

    public void setVente(Vente vente) {
        this.vente = vente;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public List<LigneVente> getLigneVentes() {
        return ligneVentes;
    }

    public void setLigneVentes(List<LigneVente> ligneVentes) {
        this.ligneVentes = ligneVentes;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
    

}
