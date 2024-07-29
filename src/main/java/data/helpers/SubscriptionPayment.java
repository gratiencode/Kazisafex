

package data.helpers;

import java.util.Objects;

public class SubscriptionPayment {
    private String ENTR_RCCM;
    private String module;
    private String typeAbonnement;
    private long durreeAbonnment;
    private double montant;
    private String devise;

    public SubscriptionPayment() {
    }

    public SubscriptionPayment(String entrepriseUid, String module, String typeAbonnement, long durreeAbonnment, double montant, String devise) {
        this.ENTR_RCCM = entrepriseUid;
        this.module = module;
        this.typeAbonnement = typeAbonnement;
        this.durreeAbonnment = durreeAbonnment;
        this.montant = montant;
        this.devise = devise;
    }

    public String getDevise() {
        return this.devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public String getEntreprise() {
        return this.ENTR_RCCM;
    }

    public void setEntreprise(String entrepriseUid) {
        this.ENTR_RCCM = entrepriseUid;
    }

    public String getModule() {
        return this.module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getTypeAbonnement() {
        return this.typeAbonnement;
    }

    public void setTypeAbonnement(String typeAbonnement) {
        this.typeAbonnement = typeAbonnement;
    }

    public long getDurreeAbonnment() {
        return this.durreeAbonnment;
    }

    public void setDurreeAbonnment(long durreeAbonnment) {
        this.durreeAbonnment = durreeAbonnment;
    }

    public double getMontant() {
        return this.montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.ENTR_RCCM);
        return hash;
    }

   

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            SubscriptionPayment other = (SubscriptionPayment)obj;
            return Objects.equals(this.ENTR_RCCM, other.ENTR_RCCM);
        }
    }
}

