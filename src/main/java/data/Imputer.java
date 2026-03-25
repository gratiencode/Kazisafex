/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package data;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 *
 * @author endeleya
 */
@Entity
@Table(name = "imputer")
@NamedQueries({
    @NamedQuery(name = "Imputer.findAll", query = "SELECT i FROM Imputer i"),
    @NamedQuery(name = "Imputer.findByUid", query = "SELECT i FROM Imputer i WHERE i.uid = :uid"),
    @NamedQuery(name = "Imputer.findByDate", query = "SELECT i FROM Imputer i WHERE i.date = :date"),
    @NamedQuery(name = "Imputer.findByMontant", query = "SELECT i FROM Imputer i WHERE i.montant = :montant"),
    @NamedQuery(name = "Imputer.findByDevise", query = "SELECT i FROM Imputer i WHERE i.devise = :devise"),
    @NamedQuery(name = "Imputer.findByPercent", query = "SELECT i FROM Imputer i WHERE i.percent = :percent"),
    @NamedQuery(name = "Imputer.findByRegion", query = "SELECT i FROM Imputer i WHERE i.region = :region")})

public class Imputer implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "uid")
    private String uid;
    @Column(name = "date_")
    private LocalDate date;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "montant")
    private Double montant;
    @Size(max = 100)
    @Column(name = "devise")
    private String devise;
    @Column(name = "percent")
    private Double percent;
    @Size(max = 100)
    @Column(name = "region")
    private String region;
    @JoinColumn(name = "operation_id", referencedColumnName = "uid")
    @ManyToOne(optional = false)
    private Operation operationId;
    @JoinColumn(name = "production_id", referencedColumnName = "uid")
    @ManyToOne(optional = false)
    private Production productionId;
    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    public Imputer() {
    }

    public Imputer(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Operation getOperationId() {
        return operationId;
    }

    public void setOperationId(Operation operationId) {
        this.operationId = operationId;
    }

    public Production getProductionId() {
        return productionId;
    }

    public void setProductionId(Production productionId) {
        this.productionId = productionId;
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
        if (!(object instanceof Imputer)) {
            return false;
        }
        Imputer other = (Imputer) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ejb.entities.Imputer[ uid=" + uid + " ]";
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
