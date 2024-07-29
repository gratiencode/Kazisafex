/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.util.Date;
import java.util.Objects;
import data.Mesure;

/**
 *
 * @author eroot
 */
public class Relevee {
    private Date date;
    private String numeroBon;
    private String nomClient;
    private String nomProduit;
    private double quantite;
    private double prixunitaire;
    private double montant;
    private String parent;
    private String observation;
    private Mesure mesure;

    public Relevee() {
    }

    public Relevee(String numeroBon) {
        this.numeroBon = numeroBon;
    }

    public String getNumeroBon() {
        return numeroBon;
    }

    public void setNumeroBon(String numeroBon) {
        this.numeroBon = numeroBon;
    }
    

    
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
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

    public double getPrixunitaire() {
        return prixunitaire;
    }

    public void setPrixunitaire(double prixunitaire) {
        this.prixunitaire = prixunitaire;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.numeroBon);
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
        final Relevee other = (Relevee) obj;
        if (!Objects.equals(this.numeroBon, other.numeroBon)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "{" + "date:" + date + ", numeroBon:" + numeroBon + ", nomClient:" + nomClient + ", nomProduit:" + nomProduit + ", quantite:" + quantite + ", prixunitaire:" + prixunitaire + ", parent:" + parent + ", observation:" + observation + "}";
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public Mesure getMesure() {
        return mesure;
    }

    public void setMesure(Mesure mesure) {
        this.mesure = mesure;
    }
    
    
    
}
