/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author eroot
 */
public class Transaction implements Comparator<Transaction> {
    private String uid;
    private String reference;
    private LocalDateTime date;
    private String libelle;
    private String region;
    private double debit_usd;
    private double debit_cdf;
    private double credit_usd;
    private double credit_cdf;
    private double solde_usd;
    private double solde_cdf;

    public Transaction() {
    }

    public Transaction(String uid, String reference, LocalDateTime date, String libelle, double debit_usd, double debit_cdf, double credit_usd, double credit_cdf, double solde_usd, double solde_cdf) {
        this.uid = uid;
        this.reference = reference;
        this.date = date;
        this.libelle = libelle;
        this.debit_usd = debit_usd;
        this.debit_cdf = debit_cdf;
        this.credit_usd = credit_usd;
        this.credit_cdf = credit_cdf;
        this.solde_usd = solde_usd;
        this.solde_cdf = solde_cdf;
    }

   

    public double getSolde_cdf() {
        return solde_cdf;
    }

    public void setSolde_cdf(double solde_cdf) {
        this.solde_cdf = solde_cdf;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public double getDebit_usd() {
        return debit_usd;
    }

    public void setDebit_usd(double debit_usd) {
        this.debit_usd = debit_usd;
    }

    public double getDebit_cdf() {
        return debit_cdf;
    }

    public void setDebit_cdf(double debit_cdf) {
        this.debit_cdf = debit_cdf;
    }

    public double getCredit_usd() {
        return credit_usd;
    }

    public void setCredit_usd(double credit_usd) {
        this.credit_usd = credit_usd;
    }

    public double getCredit_cdf() {
        return credit_cdf;
    }

    public void setCredit_cdf(double credit_cdf) {
        this.credit_cdf = credit_cdf;
    }

    public double getSolde_usd() {
        return solde_usd;
    }

    public void setSolde_usd(double solde_usd) {
        this.solde_usd = solde_usd;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.uid);
        hash = 71 * hash + Objects.hashCode(this.reference);
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
        final Transaction other = (Transaction) obj;
        if (!Objects.equals(this.uid, other.uid)) {
            return false;
        }
        if (!Objects.equals(this.reference, other.reference)) {
            return false;
        }
        return true;
    }

    @Override
    public int compare(Transaction o1, Transaction o2) {
        return o1.date.compareTo(o2.date);
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    
    
    
}
