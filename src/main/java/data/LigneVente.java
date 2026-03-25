/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data; import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import tools.Tables;

/**
 *
 * @author eroot
 */
@Entity
@Table(name = "ligne_vente")
@NamedQueries({
    @NamedQuery(name = "LigneVente.findAll", query = "SELECT DISTINCT  l FROM LigneVente l"),
    @NamedQuery(name = "LigneVente.findByUid", query = "SELECT DISTINCT  l FROM LigneVente l WHERE l.uid = :uid"),
    @NamedQuery(name = "LigneVente.findByClientId", query = "SELECT DISTINCT  l FROM LigneVente l WHERE l.clientId = :clientId"),
    @NamedQuery(name = "LigneVente.findByQuantite", query = "SELECT DISTINCT  l FROM LigneVente l WHERE l.quantite = :quantite"),
    @NamedQuery(name = "LigneVente.findByMontantUsd", query = "SELECT DISTINCT  l FROM LigneVente l WHERE l.montantUsd = :montantUsd"),
    @NamedQuery(name = "LigneVente.findByMontantCdf", query = "SELECT DISTINCT  l FROM LigneVente l WHERE l.montantCdf = :montantCdf"),
    @NamedQuery(name = "LigneVente.findByPrixUnit", query = "SELECT DISTINCT  l FROM LigneVente l WHERE l.prixUnit = :prixUnit")})

public class LigneVente extends BaseModel implements Serializable {

    private String clientId;
    private double quantite;
    private double montantUsd;
    private double montantCdf;
    private String numlot;
    @Column(name = "coutAchat")
    private Double coutAchat;
    private static final long serialVersionUID = 1L;
    @Id
    private Long uid;
    private Double prixUnit;
    @JoinColumn(name = "mesure_id", referencedColumnName = "uid")
    @ManyToOne
    private Mesure mesureId;
    @JoinColumn(name = "product_id", referencedColumnName = "uid")
    @ManyToOne(optional = false)
    private Produit productId;
    @ManyToOne(optional = false)
    @JoinColumn(name = "reference_uid", referencedColumnName = "uid")
    private Vente reference;
    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onDataOperation() {
        if (this.uid == null) {
            this.uid = System.currentTimeMillis() + 101;
        } 
        this.updatedAt=LocalDateTime.now();
    }
    
     
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt=LocalDateTime.now();
    }

    public LigneVente() {
        this.type = Tables.LIGNEVENTE.name();
    }

    public LigneVente(Long uid) {
        this.uid = uid;
        this.type = Tables.LIGNEVENTE.name();
    }

    public LigneVente(Long uid, String clientId, double quantite, double montantUsd, double montantCdf) {
        this.uid = uid;
        this.clientId = clientId;
        this.quantite = quantite;
        this.montantUsd = montantUsd;
        this.montantCdf = montantCdf;
        this.type = Tables.LIGNEVENTE.name();
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
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

    public Double getPrixUnit() {
        return prixUnit;
    }

    public void setPrixUnit(Double prixUnit) {
        this.prixUnit = prixUnit;
    }

    public Mesure getMesureId() {
        return mesureId;
    }

    public void setMesureId(Mesure mesureId) {
        this.mesureId = mesureId;
    }

    public Produit getProductId() {
        return productId;
    }

    public void setProductId(Produit productId) {
        this.productId = productId;
    }

    public Vente getReference() {
        return reference;
    }

    public void setReference(Vente reference) {
        this.reference = reference;
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
        if (!(object instanceof LigneVente)) {
            return false;
        }
        LigneVente other = (LigneVente) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.LigneVente[ uid=" + uid + " produit = " + productId + " \nquantite ="
                + " " + quantite + " mesure = " + mesureId + "\n"
                + " montant usd = " + montantUsd + " montant cdf = " + montantCdf + " prix unit = " + prixUnit + " clientid = " + clientId + "  ]";
    }

    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }

    public String getNumlot() {
        return numlot;
    }

    public void setNumlot(String numlot) {
        this.numlot = numlot;
    }

    public Double getCoutAchat() {
        return coutAchat;
    }

    public void setCoutAchat(Double coutAchat) {
        this.coutAchat = coutAchat;
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
