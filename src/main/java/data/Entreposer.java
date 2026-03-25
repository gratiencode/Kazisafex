/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package data;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 *
 * @author endeleya
 */
@Entity
@Table(name = "entreposer")
@NamedQueries({
    @NamedQuery(name = "Entreposer.findAll", query = "SELECT e FROM Entreposer e"),
    @NamedQuery(name = "Entreposer.findByUid", query = "SELECT e FROM Entreposer e WHERE e.uid = :uid"),
    @NamedQuery(name = "Entreposer.findByDate", query = "SELECT e FROM Entreposer e WHERE e.date = :date"),
    @NamedQuery(name = "Entreposer.findByExpiryDate", query = "SELECT e FROM Entreposer e WHERE e.expiryDate = :expiryDate"),
    @NamedQuery(name = "Entreposer.findByNumlot", query = "SELECT e FROM Entreposer e WHERE e.numlot = :numlot"),
    @NamedQuery(name = "Entreposer.findByQuantite", query = "SELECT e FROM Entreposer e WHERE e.quantite = :quantite"),
    @NamedQuery(name = "Entreposer.findByComment", query = "SELECT e FROM Entreposer e WHERE e.comment = :comment"),
    @NamedQuery(name = "Entreposer.findByRegion", query = "SELECT e FROM Entreposer e WHERE e.region = :region"),
    @NamedQuery(name = "Entreposer.findByQualite", query = "SELECT e FROM Entreposer e WHERE e.qualite = :qualite"),
    @NamedQuery(name = "Entreposer.findByCout", query = "SELECT e FROM Entreposer e WHERE e.cout = :cout"),
    @NamedQuery(name = "Entreposer.findByDevise", query = "SELECT e FROM Entreposer e WHERE e.devise = :devise"),
    @NamedQuery(name = "Entreposer.findByNiveauFabrication", query = "SELECT e FROM Entreposer e WHERE e.niveauFabrication = :niveauFabrication")})

public class Entreposer implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "uid")
    private String uid;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_")
    private LocalDateTime date;
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    @Size(max = 100)
    @Column(name = "numlot")
    private String numlot;
    @Basic(optional = false)
    @NotNull
    @Column(name = "quantite")
    private double quantite;
    @Size(max = 100)
    @Column(name = "comment")
    private String comment;
    @Size(max = 100)
    @Column(name = "region")
    private String region;
    @Size(max = 100)
    @Column(name = "qualite")
    private String qualite;
    @Basic(optional = false)
    @NotNull
    @Column(name = "cout")
    private double cout;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "devise")
    private String devise;
    @Size(max = 100)
    @Column(name = "niveau_fabrication")
    private String niveauFabrication;
    @JoinColumn(name = "depot_id", referencedColumnName = "uid")
    @ManyToOne
    private Depot depotId;
    @JoinColumn(name = "livraison_id", referencedColumnName = "uid")
    @ManyToOne
    private Livraison livraisonId;
    @JoinColumn(name = "matiere_id", referencedColumnName = "uid")
    @ManyToOne
    private Matiere matiereId;
    @JoinColumn(name = "sku_id", referencedColumnName = "uid")
    @ManyToOne
    private MatiereSku skuId;
    @JoinColumn(name = "mesure_id", referencedColumnName = "uid")
    @ManyToOne
    private Mesure mesureId;
    @JoinColumn(name = "production_id", referencedColumnName = "uid")
    @ManyToOne
    private Production productionId;
    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    public Entreposer() {
    }
    
    @PrePersist
    @PreUpdate
    public void presave(){
        if(this.uid==null){
            this.uid=UUID.randomUUID().toString().replace("-","").toLowerCase();
        }
        this.updatedAt=LocalDateTime.now();
    }

    public Entreposer(String uid) {
        this.uid = uid;
    }

    public Entreposer(String uid, LocalDateTime date, double quantite, double cout, String devise) {
        this.uid = uid;
        this.date = date;
        this.quantite = quantite;
        this.cout = cout;
        this.devise = devise;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getQualite() {
        return qualite;
    }

    public void setQualite(String qualite) {
        this.qualite = qualite;
    }

    public double getCout() {
        return cout;
    }

    public void setCout(double cout) {
        this.cout = cout;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public String getNiveauFabrication() {
        return niveauFabrication;
    }

    public void setNiveauFabrication(String niveauFabrication) {
        this.niveauFabrication = niveauFabrication;
    }

    public Depot getDepotId() {
        return depotId;
    }

    public void setDepotId(Depot depotId) {
        this.depotId = depotId;
    }

    public Livraison getLivraisonId() {
        return livraisonId;
    }

    public void setLivraisonId(Livraison livraisonId) {
        this.livraisonId = livraisonId;
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

    public Mesure getMesureId() {
        return mesureId;
    }

    public void setMesureId(Mesure mesureId) {
        this.mesureId = mesureId;
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
        if (!(object instanceof Entreposer)) {
            return false;
        }
        Entreposer other = (Entreposer) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ejb.entities.Entreposer[ uid=" + uid + " ]";
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
