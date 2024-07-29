/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author eroot
 */
public class StockerComparator implements Comparator<StockerComparator>  {
    private Date stockDate;
    private String uid;
    private String mesureId;
    private String productId;

    public StockerComparator() {
    }

    public StockerComparator(Date stockDate, String uid, String mesureId, String productId) {
        this.stockDate = stockDate;
        this.uid = uid;
        this.mesureId = mesureId;
        this.productId = productId;
    }
    
    

    @Override
    public int compare(StockerComparator o1, StockerComparator o2) {
        return o1.stockDate.compareTo(o2.stockDate);
    }

    public Date getStockDate() {
        return stockDate;
    }

    public void setStockDate(Date stockDate) {
        this.stockDate = stockDate;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMesureId() {
        return mesureId;
    }

    public void setMesureId(String mesureId) {
        this.mesureId = mesureId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.uid);
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
        final StockerComparator other = (StockerComparator) obj;
        if (!Objects.equals(this.uid, other.uid)) {
            return false;
        }
        return true;
    }
    
    
}
