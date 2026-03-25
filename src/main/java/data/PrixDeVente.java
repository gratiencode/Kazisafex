/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.util.UUID;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;
import tools.Tables;

/**
 *
 * @author eroot
 */
@Entity
@Table(name = "prix_de_vente")
@XmlRootElement

@NamedQueries({
    @NamedQuery(name = "PrixDeVente.findAll", query = "SELECT DISTINCT  p FROM PrixDeVente p"),
    @NamedQuery(name = "PrixDeVente.findByUid", query = "SELECT DISTINCT  p FROM PrixDeVente p WHERE p.uid = :uid"),
    @NamedQuery(name = "PrixDeVente.findByQMin", query = "SELECT DISTINCT  p FROM PrixDeVente p WHERE p.qmin = :qMin"),
    @NamedQuery(name = "PrixDeVente.findByQMax", query = "SELECT DISTINCT  p FROM PrixDeVente p WHERE p.qmax = :qMax"),
    @NamedQuery(name = "PrixDeVente.findByPrixUnitaire", query = "SELECT DISTINCT  p FROM PrixDeVente p WHERE p.prixUnitaire = :prixUnitaire"),
    @NamedQuery(name = "PrixDeVente.findByQuantiteInterval", query = "SELECT DISTINCT  p FROM PrixDeVente p WHERE p.qmin <= :quantiteInterval AND p.qmax >= :quantiteInterval"),
    @NamedQuery(name = "PrixDeVente.findByDevise", query = "SELECT DISTINCT  p FROM PrixDeVente p WHERE p.devise = :devise"),
    @NamedQuery(name = "PrixDeVente.findByPourcentParCunit", query = "SELECT DISTINCT  p FROM PrixDeVente p WHERE p.pourcentParCunit = :pourcentParCunit")})

public class PrixDeVente extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "uid", updatable = false, nullable = false)
    private String uid;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "q_min")
    private double qmin;
    @Column(name = "q_max")
    private double qmax;
    @Column(name = "prix_unitaire")
    private double prixUnitaire;
    @Column(name = "devise")
    private String devise;
    @Column(name = "pourcent_par_cunit")
    private Double pourcentParCunit;
    @JoinColumn(name = "mesureid_uid", referencedColumnName = "uid")
    @ManyToOne
    private Mesure mesureId;
    @JoinColumn(name = "recquisition_id", referencedColumnName = "uid")
    @ManyToOne(optional = false)
    private Recquisition recquisitionId;
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

    public PrixDeVente() {
        this.type = Tables.PRIXDEVENTE.name();
    }

    public PrixDeVente(String uid) {
        this.uid = uid;
        this.type = Tables.PRIXDEVENTE.name();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public double getQmin() {
        return this.qmin;
    }

    public void setQmin(double qMin) {
        this.qmin = qMin;
    }

    public double getQmax() {
        return this.qmax;
    }

    public void setQmax(double qMax) {
        this.qmax = qMax;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public Double getPourcentParCunit() {
        return pourcentParCunit;
    }

    public void setPourcentParCunit(Double pourcentParCunit) {
        this.pourcentParCunit = pourcentParCunit;
    }

    public Mesure getMesureId() {
        return mesureId;
    }

    public void setMesureId(Mesure mesureId) {
        this.mesureId = mesureId;
    }

    public Recquisition getRecquisitionId() {
        return recquisitionId;
    }

    public void setRecquisitionId(Recquisition recquisitionId) {
        this.recquisitionId = recquisitionId;
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
        if (!(object instanceof PrixDeVente)) {
            return false;
        }
        PrixDeVente other = (PrixDeVente) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.PrixDeVente[ uid=" + uid + " ]";
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
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

}
