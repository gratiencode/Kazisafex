/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data; import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Objects;

/**
 *
 * @author gratien
 */
public class PVUnit {
    private String devise;
    private String uid;
    private Double qMin;
    private Double qMax;
    private Double prixUnitaire;
    private String mesureId;
    private String recquisitionId;

    public PVUnit() {
    }

    public PVUnit(String uid) {
        this.uid = uid;
    }

    public PVUnit(String devise, String uid, Double qMin, Double qMax, Double prixUnitaire, String mesureId, String recquisitionId) {
        this.devise = devise;
        this.uid = uid;
        this.qMin = qMin;
        this.qMax = qMax;
        this.prixUnitaire = prixUnitaire;
        this.mesureId = mesureId;
        this.recquisitionId = recquisitionId;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Double getqMin() {
        return qMin;
    }

    public void setqMin(Double qMin) {
        this.qMin = qMin;
    }

    public Double getqMax() {
        return qMax;
    }

    public void setqMax(Double qMax) {
        this.qMax = qMax;
    }

    public Double getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(Double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public String getMesureId() {
        return mesureId;
    }

    public void setMesureId(String mesureId) {
        this.mesureId = mesureId;
    }

    public String getRecquisitionId() {
        return recquisitionId;
    }

    public void setRecquisitionId(String recquisitionId) {
        this.recquisitionId = recquisitionId;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.uid);
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
        final PVUnit other = (PVUnit) obj;
        if (!Objects.equals(this.uid, other.uid)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PVUnit{" + "devise=" + devise + ", uid=" + uid + ", qMin=" + qMin + ", qMax=" + qMax + ", prixUnitaire=" + prixUnitaire + ", mesureId=" + mesureId + ", recquisitionId=" + recquisitionId + '}';
    }
    
    
    
}
