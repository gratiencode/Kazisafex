/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.util.Objects;

/**
 *
 * @author eroot
 */
public class ResultStatementItem {
    private String periode;
    private String description;
    private double montantRevenu;
    private double quantite;
    private double montantDepense;
    private double montantMarge;

    public ResultStatementItem() {
    }

    public String getPeriode() {
        return periode;
    }

    public void setPeriode(String periode) {
        this.periode = periode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getMontantRevenu() {
        return montantRevenu;
    }

    public void setMontantRevenu(double montantRevenu) {
        this.montantRevenu = montantRevenu;
    }

    public double getMontantDepense() {
        return montantDepense;
    }

    public void setMontantDepense(double montantDepense) {
        this.montantDepense = montantDepense;
    }

    public double getMontantMarge() {
        return montantMarge;
    }

    public void setMontantMarge(double montantMarge) {
        this.montantMarge = montantMarge;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.periode);
        hash = 97 * hash + Objects.hashCode(this.description);
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
        final ResultStatementItem other = (ResultStatementItem) obj;
        if (!Objects.equals(this.periode, other.periode)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ResultStatementItem{" + "periode=" + periode + ", description=" + description + ", montantRevenu=" + montantRevenu + ", montantDepense=" + montantDepense + ", montantMarge=" + montantMarge + '}';
    }

    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }
    
    
    
}
