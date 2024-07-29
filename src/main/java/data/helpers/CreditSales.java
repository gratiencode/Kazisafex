

package data.helpers;

import java.util.Objects;

public class CreditSales {
    private String reference;
    private double volumeDebt;
    private double recoveredDebt;
    private double remainDebt;
    private String period;
    private String clientName;
    private String clientPhone;

    public CreditSales() {
    }

    public CreditSales(String reference, double volumeDebt, double recoveredDebt, double remainDebt, String period, String clientName, String clientPhone) {
        this.reference = reference;
        this.volumeDebt = volumeDebt;
        this.recoveredDebt = recoveredDebt;
        this.remainDebt = remainDebt;
        this.period = period;
        this.clientName = clientName;
        this.clientPhone = clientPhone;
    }

    public String getClientPhone() {
        return this.clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public String getReference() {
        return this.reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public double getVolumeDebt() {
        return this.volumeDebt;
    }

    public void setVolumeDebt(double volumeDebt) {
        this.volumeDebt = volumeDebt;
    }

    public double getRecoveredDebt() {
        return this.recoveredDebt;
    }

    public void setRecoveredDebt(double recoveredDebt) {
        this.recoveredDebt = recoveredDebt;
    }

    public double getRemainDebt() {
        return this.remainDebt;
    }

    public void setRemainDebt(double remainDebt) {
        this.remainDebt = remainDebt;
    }

    public String getPeriod() {
        return this.period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getClientName() {
        return this.clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.reference);
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
            CreditSales other = (CreditSales)obj;
            return Objects.equals(this.reference, other.reference);
        }
    }

    public String toString() {
        return "CreditSales{reference=" + this.reference + ", volumeDebt=" + this.volumeDebt + ", recoveredDebt=" + this.recoveredDebt + ", remainDebt=" + this.remainDebt + ", period=" + this.period + ", clientName=" + this.clientName + ", clientPhone=" + this.clientPhone + '}';
    }
}

