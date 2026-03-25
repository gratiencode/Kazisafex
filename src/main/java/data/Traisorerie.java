/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.util.UUID;
import java.time.LocalDateTime;
import tools.Tables;

/**
 *
 * @author eroot
 */
@Entity
@Table(name = "traisorerie")
@NamedQueries({
    @NamedQuery(name = "Traisorerie.findAll", query = "SELECT DISTINCT  t FROM Traisorerie t ORDER BY t.date DESC"),
    @NamedQuery(name = "Traisorerie.findByUid", query = "SELECT DISTINCT  t FROM Traisorerie t WHERE t.uid = :uid"),
    @NamedQuery(name = "Traisorerie.findByReference", query = "SELECT DISTINCT  t FROM Traisorerie t WHERE t.reference = :reference"),
    @NamedQuery(name = "Traisorerie.findByRegion", query = "SELECT DISTINCT  t FROM Traisorerie t WHERE t.region = :region ORDER BY t.date DESC"),
    @NamedQuery(name = "Traisorerie.findByDate", query = "SELECT DISTINCT  t FROM Traisorerie t WHERE t.date = :date"),
    @NamedQuery(name = "Traisorerie.findByMontantUsd", query = "SELECT DISTINCT  t FROM Traisorerie t WHERE t.montantUsd = :montantUsd"),
    @NamedQuery(name = "Traisorerie.findByMontantCdf", query = "SELECT DISTINCT  t FROM Traisorerie t WHERE t.montantCdf = :montantCdf"),
    @NamedQuery(name = "Traisorerie.findByTypeTresorerie", query = "SELECT DISTINCT  t FROM Traisorerie t WHERE t.typeTresorerie = :typeTresorerie")})

public class Traisorerie extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "uid", updatable = false, nullable = false)
    private String uid;
    private String reference;
    private String region;
    @Column(name = "date", columnDefinition = "DATETIME")
    private LocalDateTime date;
    private String libelle;
    private double montantUsd;
    private double montantCdf;
    @Column(name = "soldeCdf",columnDefinition = "DOUBLE")
    private Double soldeCdf=0d;
    @Column(name = "soldeUsd",columnDefinition = "DOUBLE")
    private Double soldeUsd=0d;
    private String mouvement;
    private String typeTresorerie;
    @JoinColumn(name = "tresor_id", referencedColumnName = "uid")
    @ManyToOne
    private CompteTresor tresorId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "caisseOpId")
    @JsonBackReference(value = "trz-ops")
    private List<Operation> operationList;
    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onDataOperation() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public Traisorerie() {
        this.type = Tables.TRAISORERIE.name();
    }

    public Traisorerie(String uid) {
        this.uid = uid;
        this.type = Tables.TRAISORERIE.name();
    }

    public Traisorerie(String uid, String reference, LocalDateTime date, String libelle, double montantUsd, double montantCdf, String mouvement, String typeTresorerie) {
        this.uid = uid;
        this.reference = reference;
        this.date = date;
        this.libelle = libelle;
        this.montantUsd = montantUsd;
        this.montantCdf = montantCdf;
        this.mouvement = mouvement;
        this.typeTresorerie = typeTresorerie;
        this.type = Tables.TRAISORERIE.name();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public double getMontantUsd() {
        return montantUsd;
    }

    public void setMontantUsd(double montantUsd) {
        this.montantUsd = montantUsd;
    }

    public double getMontantCdf() {
        return montantCdf;
    }

    public void setMontantCdf(double montantCdf) {
        this.montantCdf = montantCdf;
    }

    public String getMouvement() {
        return mouvement;
    }

    public void setMouvement(String mouvement) {
        this.mouvement = mouvement;
    }

    public String getTypeTresorerie() {
        return typeTresorerie;
    }

    public void setTypeTresorerie(String typeTresorerie) {
        this.typeTresorerie = typeTresorerie;
    }

    public CompteTresor getTresorId() {
        return tresorId;
    }

    public void setTresorId(CompteTresor tresorId) {
        this.tresorId = tresorId;
    }

    public List<Operation> getOperationList() {
        return operationList;
    }

    public void setOperationList(List<Operation> operationList) {
        this.operationList = operationList;
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
        if (!(object instanceof Traisorerie)) {
            return false;
        }
        Traisorerie other = (Traisorerie) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Traisorerie[ uid=" + uid + " ]";
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setDeletedAt(LocalDateTime updatedAt) {
        this.deletedAt = updatedAt;
    }

    public Double getSoldeUsd() {
        return soldeUsd;
    }

    public void setSoldeUsd(Double soldeUsd) {
        this.soldeUsd = soldeUsd;
    }

    public Double getSoldeCdf() {
        return soldeCdf;
    }

    public void setSoldeCdf(Double soldeCdf) {
        this.soldeCdf = soldeCdf;
    }
    
    

}
