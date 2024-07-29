

package data.helpers;

import java.util.Objects;

public class NetCash {
    private String accountType;
    private double amountCdf;
    private double amountUsd;
    private String region;
    private String period;

    public NetCash() {
    }

    public NetCash(String accountType, double amountCdf, double amountUsd, String region, String period) {
        this.accountType = accountType;
        this.amountCdf = amountCdf;
        this.amountUsd = amountUsd;
        this.region = region;
        this.period = period;
    }

    public String getPeriod() {
        return this.period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getAccountType() {
        return this.accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public double getAmountCdf() {
        return this.amountCdf;
    }

    public void setAmountCdf(double amountCdf) {
        this.amountCdf = amountCdf;
    }

    public double getAmountUsd() {
        return this.amountUsd;
    }

    public void setAmountUsd(double amountUsd) {
        this.amountUsd = amountUsd;
    }

    public String getRegion() {
        return this.region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.accountType);
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
            NetCash other = (NetCash)obj;
            return Objects.equals(this.accountType, other.accountType);
        }
    }

    public String toString() {
        return "NetCash{accountType=" + this.accountType + ", amountCdf=" + this.amountCdf + ", amountUsd=" + this.amountUsd + ", region=" + this.region + ", period=" + this.period + '}';
    }
}

