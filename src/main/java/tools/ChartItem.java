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
public class ChartItem implements Comparator<ChartItem>{
    private String absices;
    private Date date;
    private double ammount;
    private String serieName;

    public ChartItem() {
    }

    public ChartItem(String absices, Date date, double ammount, String serieName) {
        this.absices = absices;
        this.date = date;
        this.ammount = ammount;
        this.serieName = serieName;
    }

    public String getSerieName() {
        return serieName;
    }

    public void setSerieName(String chartTitle) {
        this.serieName = chartTitle;
    }

    public String getAbsices() {
        return absices;
    }

    public void setAbsices(String absices) {
        this.absices = absices;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getAmmount() {
        return ammount;
    }

    public void setAmmount(double ammount) {
        this.ammount = ammount;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.absices);
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
        final ChartItem other = (ChartItem) obj;
        if (!Objects.equals(this.absices, other.absices)) {
            return false;
        }
        return true;
    }

    @Override
    public int compare(ChartItem o1, ChartItem o2) {
        return o1.date.compareTo(o2.date);
    }
    
    
}
