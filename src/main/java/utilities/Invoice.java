/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities; 

import java.util.Objects;

/**
 *
 * @author eroot
 */
public class Invoice {
    private String numero;
    private String periode;
    private String etat;
    private double montantPaye;
    private double montantTotal;

    public Invoice() {
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getPeriode() {
        return periode;
    }

    public void setPeriode(String periode) {
        this.periode = periode;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public double getMontantPaye() {
        return montantPaye;
    }

    public void setMontantPaye(double montantPaye) {
        this.montantPaye = montantPaye;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.numero);
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
        final Invoice other = (Invoice) obj;
        if (!Objects.equals(this.numero, other.numero)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Invoice{" + "numero=" + numero + ", periode=" + periode + ", etat=" + etat + ", montantPaye=" + montantPaye + ", montantTotal=" + montantTotal + '}';
    }
    
}
