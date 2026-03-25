/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.UUID;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;
import tools.Tables;

/**
 *
 * @author eroot
 */
@Entity
@Table(name = "abonnement")
@XmlRootElement

@NamedQueries({
    @NamedQuery(name = "Abonnement.findAll", query = "SELECT DISTINCT  a FROM Abonnement a"),
    @NamedQuery(name = "Abonnement.findByUid", query = "SELECT DISTINCT  a FROM Abonnement a WHERE a.uid = :uid"),
    @NamedQuery(name = "Abonnement.findByNombreOperation", query = "SELECT DISTINCT  a FROM Abonnement a WHERE a.nombreOperation = :nombreOperation"),
    @NamedQuery(name = "Abonnement.findByDateAbonnement", query = "SELECT DISTINCT  a FROM Abonnement a WHERE a.dateAbonnement = :dateAbonnement"),
    @NamedQuery(name = "Abonnement.findByMontant", query = "SELECT DISTINCT  a FROM Abonnement a WHERE a.montant = :montant"),
    @NamedQuery(name = "Abonnement.findByEtat", query = "SELECT DISTINCT  a FROM Abonnement a WHERE a.etat = :etat")})
public class Abonnement extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id

    @Column(name = "uid", updatable = false, nullable = false)
    private String uid;
    private String typeAbonnement;
    private double nombreOperation;
    private LocalDateTime dateAbonnement;
    private double montant;
    private String devise;
    private String agent;
    private String etat;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;
    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    @PrePersist
    @PreUpdate
    protected void onDataOperation() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
    }

    public Abonnement() {
        type = Tables.ABONNEMENT.name();
    }

    public Abonnement(String uid) {
        this.uid = uid;
        type = Tables.ABONNEMENT.name();
    }

    public Abonnement(String uid, String typeAbonnement, double nombreOperation, LocalDateTime dateAbonnement, double montant, String devise, String agent) {
        this.uid = uid;
        this.typeAbonnement = typeAbonnement;
        this.nombreOperation = nombreOperation;
        this.dateAbonnement = dateAbonnement;
        this.montant = montant;
        this.devise = devise;
        this.agent = agent;
        type = Tables.ABONNEMENT.name();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTypeAbonnement() {
        return typeAbonnement;
    }

    public void setTypeAbonnement(String typeAbonnement) {
        this.typeAbonnement = typeAbonnement;
    }

    public double getNombreOperation() {
        return nombreOperation;
    }

    public void setNombreOperation(double nombreOperation) {
        this.nombreOperation = nombreOperation;
    }

    public LocalDateTime getDateAbonnement() {
        return dateAbonnement;
    }

    public void setDateAbonnement(LocalDateTime dateAbonnement) {
        this.dateAbonnement = dateAbonnement;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
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
        if (!(object instanceof Abonnement)) {
            return false;
        }
        Abonnement other = (Abonnement) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

}
