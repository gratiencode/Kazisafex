/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data; import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import jakarta.persistence.Basic;
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
@Table(name = "retour_magasin")
@XmlRootElement

@NamedQueries({
    @NamedQuery(name = "RetourMagasin.findAll", query = "SELECT DISTINCT  r FROM RetourMagasin r"),
    @NamedQuery(name = "RetourMagasin.findByUid", query = "SELECT DISTINCT  r FROM RetourMagasin r WHERE r.uid = :uid"),
    @NamedQuery(name = "RetourMagasin.findByPrixVente", query = "SELECT DISTINCT  r FROM RetourMagasin r WHERE r.prixVente = :prixVente"),
    @NamedQuery(name = "RetourMagasin.findByQuantite", query = "SELECT DISTINCT  r FROM RetourMagasin r WHERE r.quantite = :quantite"),
    @NamedQuery(name = "RetourMagasin.findByDate", query = "SELECT DISTINCT  r FROM RetourMagasin r WHERE r.date = :date"),
    @NamedQuery(name = "RetourMagasin.findByReferenceVente", query = "SELECT DISTINCT  r FROM RetourMagasin r WHERE r.referenceVente = :referenceVente"),
    @NamedQuery(name = "RetourMagasin.findByMotif", query = "SELECT DISTINCT  r FROM RetourMagasin r WHERE r.motif = :motif")})

public class RetourMagasin extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "uid", updatable = false, nullable = false)

    private String uid;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "prix_vente")
    private Double prixVente;
    @Column(name = "quantite")
    private Double quantite;
    @Column(name = "date_")
    
    private LocalDateTime date;
    @Basic(optional = false)
    @Column(name = "reference_vente")
    private String referenceVente;
    @Column(name = "motif")
    private String motif;
    @Column(name = "region")
    private String region;
    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    @JoinColumn(name = "client_id", referencedColumnName = "uid")
    @ManyToOne(optional = false)
    
    private Client clientId;
    @JoinColumn(name = "ligne_vente_id", referencedColumnName = "uid")
    @ManyToOne
    
    private LigneVente ligneVenteId;
    @JoinColumn(name = "mesure_id", referencedColumnName = "uid")
    @ManyToOne(optional = false)
    
    private Mesure mesureId;

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public RetourMagasin() {
        this.type = Tables.RETOURMAGASIN.name();
    }

    @PrePersist
    @PreUpdate
    protected void onDataOperation() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
        this.updatedAt = LocalDateTime.now();
    }

    public RetourMagasin(String uid) {
        this.uid = uid;
        this.type = Tables.RETOURMAGASIN.name();
    }

    public RetourMagasin(String uid, String referenceVente) {
        this.uid = uid;
        this.referenceVente = referenceVente;
        this.type = Tables.RETOURMAGASIN.name();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Double getPrixVente() {
        return prixVente;
    }

    public void setPrixVente(Double prixVente) {
        this.prixVente = prixVente;
    }

    public Double getQuantite() {
        return quantite;
    }

    public void setQuantite(Double quantite) {
        this.quantite = quantite;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getReferenceVente() {
        return referenceVente;
    }

    public void setReferenceVente(String referenceVente) {
        this.referenceVente = referenceVente;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public Client getClientId() {
        return clientId;
    }

    public void setClientId(Client clientId) {
        this.clientId = clientId;
    }

    public LigneVente getLigneVenteId() {
        return ligneVenteId;
    }

    public void setLigneVenteId(LigneVente ligneVenteId) {
        this.ligneVenteId = ligneVenteId;
    }

    public Mesure getMesureId() {
        return mesureId;
    }

    public void setMesureId(Mesure mesureId) {
        this.mesureId = mesureId;
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
        if (!(object instanceof RetourMagasin)) {
            return false;
        }
        RetourMagasin other = (RetourMagasin) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.RetourMagasin[ uid=" + uid + " ]";
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
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
