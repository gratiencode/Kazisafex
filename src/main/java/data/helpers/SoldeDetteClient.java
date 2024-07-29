/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.helpers;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.Objects;
import data.Vente;

/**
 *
 * @author eroot
 */
public class SoldeDetteClient {
    private String clientName;
    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'H:mm:ss"
    )
    private Date lastUpdateDate;
    private double debtAmount;
    private double payedAmount;
    private double remainSum;
    private String reference;
    private String region;
    private String engager_id;
    private String entreprise_id;
    private Vente vente;

    public SoldeDetteClient() {
    }

    public SoldeDetteClient(String clientName, Date lastUpdateDate, double debtAmount, double payedAmount, double remainSum, String reference, String region, String engager_id, String entreprise_id) {
        this.clientName = clientName;
        this.lastUpdateDate = lastUpdateDate;
        this.debtAmount = debtAmount;
        this.payedAmount = payedAmount;
        this.remainSum = remainSum;
        this.reference = reference;
        this.region = region;
        this.engager_id = engager_id;
        this.entreprise_id = entreprise_id;
    }

    public String getEntreprise_id() {
        return entreprise_id;
    }

    public void setEntreprise_id(String entreprise_id) {
        this.entreprise_id = entreprise_id;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public double getDebtAmount() {
        return debtAmount;
    }

    public void setDebtAmount(double debtAmount) {
        this.debtAmount = debtAmount;
    }

    public double getPayedAmount() {
        return payedAmount;
    }

    public void setPayedAmount(double payedAmount) {
        this.payedAmount = payedAmount;
    }

    public double getRemainSum() {
        return remainSum;
    }

    public void setRemainSum(double remainSum) {
        this.remainSum = remainSum;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getEngager_id() {
        return engager_id;
    }

    public void setEngager_id(String engager_id) {
        this.engager_id = engager_id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.reference);
        hash = 37 * hash + Objects.hashCode(this.region);
        hash = 37 * hash + Objects.hashCode(this.entreprise_id);
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
        final SoldeDetteClient other = (SoldeDetteClient) obj;
        if (!Objects.equals(this.reference, other.reference)) {
            return false;
        }
        if (!Objects.equals(this.region, other.region)) {
            return false;
        }
        if (!Objects.equals(this.entreprise_id, other.entreprise_id)) {
            return false;
        }
        return true;
    }

    public Vente getVente() {
        return vente;
    }

    public void setVente(Vente vente) {
        this.vente = vente;
    }
    
    
    
    
    
}
