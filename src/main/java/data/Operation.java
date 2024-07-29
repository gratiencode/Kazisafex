/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.UUID;

 import jakarta.xml.bind.annotation.XmlRootElement;
import tools.Tables;

/**
 *
 * @author eroot
 */
@Entity
@Table(name = "operation")
 @XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Operation.findAll", query = "SELECT DISTINCT  o FROM Operation o ORDER BY o.date DESC"),
    @NamedQuery(name = "Operation.findByUid", query = "SELECT DISTINCT  o FROM Operation o WHERE o.uid = :uid"),
    @NamedQuery(name = "Operation.findByReferenceOp", query = "SELECT DISTINCT  o FROM Operation o WHERE o.referenceOp = :referenceOp"),
    @NamedQuery(name = "Operation.findByRegion", query = "SELECT DISTINCT  o FROM Operation o WHERE o.region = :region"),
    @NamedQuery(name = "Operation.findByMontantUsd", query = "SELECT DISTINCT  o FROM Operation o WHERE o.montantUsd = :montantUsd"),
    @NamedQuery(name = "Operation.findByMontantCdf", query = "SELECT DISTINCT  o FROM Operation o WHERE o.montantCdf = :montantCdf"),
    @NamedQuery(name = "Operation.findByDate", query = "SELECT DISTINCT  o FROM Operation o WHERE o.date = :date"),
    @NamedQuery(name = "Operation.findSumUSDByDateIntervalRegion", query = "SELECT DISTINCT  SUM(o.montantUsd) FROM Operation o WHERE o.date BETWEEN :date1 AND :date2 AND o.region = :region"),
    @NamedQuery(name = "Operation.findSumCDFByDateIntervalRegion", query = "SELECT DISTINCT  SUM(o.montantCdf) FROM Operation o WHERE o.date BETWEEN :date1 AND :date2 AND o.region = :region"),
    @NamedQuery(name = "Operation.findSumUSDByDateInterval", query = "SELECT DISTINCT  SUM(o.montantUsd) FROM Operation o WHERE o.date BETWEEN :date1 AND :date2"),
    @NamedQuery(name = "Operation.findSumCDFByDateInterval", query = "SELECT DISTINCT  SUM(o.montantCdf) FROM Operation o WHERE o.date BETWEEN :date1 AND :date2"),
    @NamedQuery(name = "Operation.findByMouvement", query = "SELECT DISTINCT  o FROM Operation o WHERE o.mouvement = :mouvement"),
    @NamedQuery(name = "Operation.findByImputation", query = "SELECT DISTINCT  o FROM Operation o WHERE o.imputation = :imputation")})

public class Operation extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
   
    private String uid;
    private String referenceOp;
    private String region;
    private String libelle;
    private Double montantUsd;
    private Double montantCdf;
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss"
    )
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    private String mouvement;
    private String imputation;
    private String observation;
    @JoinColumn(name = "tresor_id", referencedColumnName = "uid")
    @ManyToOne
    @JsonBackReference
    private CompteTresor tresorId;
    @JoinColumn(name = "depense_id", referencedColumnName = "uid")
    @ManyToOne
    @JsonBackReference
    private Depense depenseId;
    @JsonBackReference
    @ManyToOne(optional = false)
    private Traisorerie caisseOpId;

    @PrePersist
    @PreUpdate
    protected void onDataOperation(){
        if(this.uid==null){
            this.uid= UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
    }

    public Operation() {
         this.type=Tables.OPERATION.name();
    }

    public Operation(String uid) {
        this.uid = uid;
        this.type=Tables.OPERATION.name();
    }

    public Operation(String uid, String referenceOp, String libelle) {
        this.uid = uid;
        this.referenceOp = referenceOp;
        this.libelle = libelle;
         this.type=Tables.OPERATION.name();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getReferenceOp() {
        return referenceOp;
    }

    public void setReferenceOp(String referenceOp) {
        this.referenceOp = referenceOp;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public Double getMontantUsd() {
        return montantUsd;
    }

    public void setMontantUsd(Double montantUsd) {
        this.montantUsd = montantUsd;
    }

    public Double getMontantCdf() {
        return montantCdf;
    }

    public void setMontantCdf(Double montantCdf) {
        this.montantCdf = montantCdf;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMouvement() {
        return mouvement;
    }

    public void setMouvement(String mouvement) {
        this.mouvement = mouvement;
    }

    public String getImputation() {
        return imputation;
    }

    public void setImputation(String imputation) {
        this.imputation = imputation;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public CompteTresor getTresorId() {
        return tresorId;
    }

    public void setTresorId(CompteTresor tresorId) {
        this.tresorId = tresorId;
    }

    public Depense getDepenseId() {
        return depenseId;
    }

    public void setDepenseId(Depense depenseId) {
        this.depenseId = depenseId;
    }

    public Traisorerie getCaisseOpId() {
        return caisseOpId;
    }

    public void setCaisseOpId(Traisorerie caisseOpId) {
        this.caisseOpId = caisseOpId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (uid != null ? uid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Operation)) {
            return false;
        }
        Operation other = (Operation) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Operation[ uid=" + uid + " ]";
    }

}
