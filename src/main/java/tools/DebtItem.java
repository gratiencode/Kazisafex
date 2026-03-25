/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Date;

/**
 *
 * @author eroot
 */
public class DebtItem implements Comparator<DebtItem> {
    private LocalDate date;
    private String nomClient;
    private String phoneClient;
    private String facture;
    private double montantDette;
    private double montantPaye;
    private double montantRestant;

    public DebtItem() {
    }

    public DebtItem(LocalDate date, String nomClient, String phoneClient, String facture, double montantDette, double montantPaye, double montantRestant) {
        this.date = date;
        this.nomClient = nomClient;
        this.phoneClient = phoneClient;
        this.facture = facture;
        this.montantDette = montantDette;
        this.montantPaye = montantPaye;
        this.montantRestant = montantRestant;
    }
    
    

    @Override
    public int compare(DebtItem o1, DebtItem o2) {
      return o1.date.compareTo(o2.date);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    public String getPhoneClient() {
        return phoneClient;
    }

    public void setPhoneClient(String phoneClient) {
        this.phoneClient = phoneClient;
    }

    public String getFacture() {
        return facture;
    }

    public void setFacture(String facture) {
        this.facture = facture;
    }

    public double getMontantDette() {
        return montantDette;
    }

    public void setMontantDette(double montantDette) {
        this.montantDette = montantDette;
    }

    public double getMontantPaye() {
        return montantPaye;
    }

    public void setMontantPaye(double montantPaye) {
        this.montantPaye = montantPaye;
    }

    public double getMontantRestant() {
        return montantRestant;
    }

    public void setMontantRestant(double montantRestant) {
        this.montantRestant = montantRestant;
    }
    
}
