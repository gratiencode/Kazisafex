
package data.helpers;

import java.util.Date;
import data.Entreprise;

public class Resultat {
    private Date dateDebut;
    private Date dateFin;
    private double chiffreAffaire;
    private double depenses;
    private double marge;
    private Entreprise entreprise;

    public Resultat() {
    }

    public Resultat(Date dateDebut, Date dateFin, double chiffreAffaire, double depenses, double marge) {
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.chiffreAffaire = chiffreAffaire;
        this.depenses = depenses;
        this.marge = marge;
    }

    public double getChiffreAffaire() {
        return this.chiffreAffaire;
    }

    public void setChiffreAffaire(double chiffreAffaire) {
        this.chiffreAffaire = chiffreAffaire;
    }

    public double getDepenses() {
        return this.depenses;
    }

    public void setDepenses(double depenses) {
        this.depenses = depenses;
    }

    public Date getDateFin() {
        return this.dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }

    public Date getDateDebut() {
        return this.dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public double getMarge() {
        return this.marge;
    }

    public void setMarge(double marge) {
        this.marge = marge;
    }

    public Entreprise getEntreprise() {
        return this.entreprise;
    }

    public void setEntreprise(Entreprise entreprise) {
        this.entreprise = entreprise;
    }
}

