/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package data;import com.fasterxml.jackson.annotation.JsonBackReference;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 *
 * @author endeleya
 */
@Entity
@Table(name = "repartir")
@NamedQueries({
    @NamedQuery(name = "Repartir.findAll", query = "SELECT r FROM Repartir r"),
    @NamedQuery(name = "Repartir.findByUid", query = "SELECT r FROM Repartir r WHERE r.uid = :uid"),
    @NamedQuery(name = "Repartir.findByCoutAchat", query = "SELECT r FROM Repartir r WHERE r.coutAchat = :coutAchat"),
    @NamedQuery(name = "Repartir.findByDevise", query = "SELECT r FROM Repartir r WHERE r.devise = :devise"),
    @NamedQuery(name = "Repartir.findByQuantite", query = "SELECT r FROM Repartir r WHERE r.quantite = :quantite"),
    @NamedQuery(name = "Repartir.findByDate", query = "SELECT r FROM Repartir r WHERE r.date = :date"),
    @NamedQuery(name = "Repartir.findByRegion", query = "SELECT r FROM Repartir r WHERE r.region = :region"),
    @NamedQuery(name = "Repartir.findByNumlot", query = "SELECT r FROM Repartir r WHERE r.numlot = :numlot")})

public class Repartir implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "uid")
    private String uid;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "cout_achat")
    private Double coutAchat;
    @Size(max = 100)
    @Column(name = "devise")
    private String devise;
    @Column(name = "quantite")
    private Double quantite;
    @Column(name = "date_")
    private LocalDateTime date;
    @Size(max = 100)
    @Column(name = "region")
    private String region;
    @Size(max = 100)
    @Column(name = "numlot")
    private String numlot;
    @JoinColumn(name = "matiere_id", referencedColumnName = "uid")
    @ManyToOne(optional = false)
    private Matiere matiereId;
    @JoinColumn(name = "sku_id", referencedColumnName = "uid")
    @ManyToOne
    private MatiereSku skuId;
    @JoinColumn(name = "production_id", referencedColumnName = "uid")
    @ManyToOne(optional = false)
    private Production productionId;
    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    @PrePersist
    @PreUpdate
    protected void onDataOperation() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
        this.updatedAt = LocalDateTime.now();
    }

    public Repartir() {
    }

    public Repartir(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Double getCoutAchat() {
        return coutAchat;
    }

    public void setCoutAchat(Double coutAchat) {
        this.coutAchat = coutAchat;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getNumlot() {
        return numlot;
    }

    public void setNumlot(String numlot) {
        this.numlot = numlot;
    }

    public Matiere getMatiereId() {
        return matiereId;
    }

    public void setMatiereId(Matiere matiereId) {
        this.matiereId = matiereId;
    }

    public MatiereSku getSkuId() {
        return skuId;
    }

    public void setSkuId(MatiereSku skuId) {
        this.skuId = skuId;
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
        if (!(object instanceof Repartir)) {
            return false;
        }
        Repartir other = (Repartir) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ejb.entities.Repartir[ uid=" + uid + " ]";
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
