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
@Table(name = "stocker")
@XmlRootElement

@NamedQueries({
    @NamedQuery(name = "Stocker.findAll", query = "SELECT DISTINCT  s FROM Stocker s ORDER BY s.dateStocker DESC"),
    @NamedQuery(name = "Stocker.findByUid", query = "SELECT DISTINCT  s FROM Stocker s WHERE s.uid = :uid"),
    @NamedQuery(name = "Stocker.findByRegion", query = "SELECT DISTINCT  s FROM Stocker s WHERE s.region = :region"),
    @NamedQuery(name = "Stocker.findByDateStocker", query = "SELECT DISTINCT  s FROM Stocker s WHERE s.dateStocker = :dateStocker"),
    @NamedQuery(name = "Stocker.findByCoutAchat", query = "SELECT DISTINCT  s FROM Stocker s WHERE s.coutAchat = :coutAchat"),
    @NamedQuery(name = "Stocker.findByReduction", query = "SELECT DISTINCT  s FROM Stocker s WHERE s.reduction = :reduction"),
    @NamedQuery(name = "Stocker.findByDateExpir", query = "SELECT DISTINCT  s FROM Stocker s WHERE s.dateExpir = :dateExpir"),
    @NamedQuery(name = "Stocker.findByStockAlerte", query = "SELECT DISTINCT  s FROM Stocker s WHERE s.stockAlerte = :stockAlerte"),
    @NamedQuery(name = "Stocker.findByQuantite", query = "SELECT DISTINCT  s FROM Stocker s WHERE s.quantite = :quantite"),
    @NamedQuery(name = "Stocker.findByPrixAchatTotal", query = "SELECT DISTINCT  s FROM Stocker s WHERE s.prixAchatTotal = :prixAchatTotal")})
public class Stocker extends BaseModel implements Serializable {

    private String region;
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss"
    )
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateStocker;
    private double coutAchat;
    private double reduction;
    private String numlot;
    private double stockAlerte;
    private double quantite;
    private String libelle;
    private String localisation;
    private double prixAchatTotal;
    private String observation;

    private static final long serialVersionUID = 1L;
    @Id

    private String uid;
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd"
    )
    @Temporal(TemporalType.DATE)
    private Date dateExpir;
    @ManyToOne(optional = false)
    @JsonBackReference
    private Livraison livraisId;
    @JoinColumn(name = "mesure_id", referencedColumnName = "uid")
    @ManyToOne
    @JsonBackReference
    private Mesure mesureId;
    @JoinColumn(name = "product_id", referencedColumnName = "uid")
    @ManyToOne(optional = false)
    @JsonBackReference
    private Produit productId;

    @PrePersist
    @PreUpdate
    protected void onDataOperation() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
    }

    public Stocker() {
        this.type = Tables.STOCKER.name();
    }

    public Stocker(String uid) {
        this.uid = uid;
        this.type = Tables.STOCKER.name();
    }

    public Stocker(String uid, Date dateStocker, double coutAchat, double reduction, double stockAlerte, double quantite, double prixAchatTotal) {
        this.uid = uid;
        this.dateStocker = dateStocker;
        this.coutAchat = coutAchat;
        this.reduction = reduction;
        this.stockAlerte = stockAlerte;
        this.quantite = quantite;
        this.prixAchatTotal = prixAchatTotal;
        this.type = Tables.STOCKER.name();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getDateStocker() {
        return dateStocker;
    }

    public void setDateStocker(Date dateStocker) {
        this.dateStocker = dateStocker;
    }

    public double getCoutAchat() {
        return coutAchat;
    }

    public void setCoutAchat(double coutAchat) {
        this.coutAchat = coutAchat;
    }

    public Date getDateExpir() {
        return dateExpir;
    }

    public void setDateExpir(Date dateExpir) {
        this.dateExpir = dateExpir;
    }

    public double getStockAlerte() {
        return stockAlerte;
    }

    public void setStockAlerte(double stockAlerte) {
        this.stockAlerte = stockAlerte;
    }

    public double getPrixAchatTotal() {
        return prixAchatTotal;
    }

    public void setPrixAchatTotal(double prixAchatTotal) {
        this.prixAchatTotal = prixAchatTotal;
    }

    public Livraison getLivraisId() {
        return livraisId;
    }

    public void setLivraisId(Livraison livraisId) {
        this.livraisId = livraisId;
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (uid != null ? uid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Stocker)) {
            return false;
        }
        Stocker other = (Stocker) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Stocker[ uid=" + uid + " ]";
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public double getReduction() {
        return reduction;
    }

    public void setReduction(double reduction) {
        this.reduction = reduction;
    }

    public String getNumlot() {
        return numlot;
    }

    public void setNumlot(String numlot) {
        this.numlot = numlot;
    }

    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

}
