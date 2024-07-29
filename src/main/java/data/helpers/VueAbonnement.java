package data.helpers;

import java.util.Date;
import java.util.Objects;

public class VueAbonnement {
    private String uid;
    private long recordCount;
    private double subPrice;
    private double totalToPayUsd;
    private double totaltoPayCdf;
    private String month;
    private Date lastSubscription;
    private Date now;
    private String entrepriseName;
    private String modules;

    public VueAbonnement(long recordCount, double subPrice, double totalToPayUsd, double totaltoPayCdf, String month, Date lastSubscription, Date now, String entrepriseName, String modules) {
        this.recordCount = recordCount;
        this.subPrice = subPrice;
        this.totalToPayUsd = totalToPayUsd;
        this.totaltoPayCdf = totaltoPayCdf;
        this.month = month;
        this.lastSubscription = lastSubscription;
        this.now = now;
        this.entrepriseName = entrepriseName;
        this.modules = modules;
    }

    public VueAbonnement() {
    }

    public long getRecordCount() {
        return this.recordCount;
    }

    public void setRecordCount(long recordCount) {
        this.recordCount = recordCount;
    }

    public double getSubPrice() {
        return this.subPrice;
    }

    public void setSubPrice(double subPrice) {
        this.subPrice = subPrice;
    }

    public double getTotalToPayUsd() {
        return this.totalToPayUsd;
    }

    public void setTotalToPayUsd(double totalToPayUsd) {
        this.totalToPayUsd = totalToPayUsd;
    }

    public double getTotaltoPayCdf() {
        return this.totaltoPayCdf;
    }

    public void setTotaltoPayCdf(double totaltoPayCdf) {
        this.totaltoPayCdf = totaltoPayCdf;
    }

    public String getMonth() {
        return this.month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Date getLastSubscription() {
        return this.lastSubscription;
    }

    public void setLastSubscription(Date lastSubscription) {
        this.lastSubscription = lastSubscription;
    }

    public Date getNow() {
        return this.now;
    }

    public void setNow(Date now) {
        this.now = now;
    }

    public String getEntrepriseName() {
        return this.entrepriseName;
    }

    public void setEntrepriseName(String entrepriseName) {
        this.entrepriseName = entrepriseName;
    }

    public String getModules() {
        return this.modules;
    }

    public void setModules(String modules) {
        this.modules = modules;
    }

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.uid);
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
            VueAbonnement other = (VueAbonnement)obj;
            return Objects.equals(this.uid, other.uid);
        }
    }
}

