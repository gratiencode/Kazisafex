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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * @author endeleya
 */
@Entity
@Table(name = "commande_lister")
@NamedQueries({
    @NamedQuery(name = "CommandeLister.findAll", query = "SELECT c FROM CommandeLister c"),
    @NamedQuery(name = "CommandeLister.findByUid", query = "SELECT c FROM CommandeLister c WHERE c.uid = :uid"),
    @NamedQuery(name = "CommandeLister.findByQuantite", query = "SELECT c FROM CommandeLister c WHERE c.quantite = :quantite"),
    @NamedQuery(name = "CommandeLister.findByCoutAchat", query = "SELECT c FROM CommandeLister c WHERE c.coutAchat = :coutAchat"),
    @NamedQuery(name = "CommandeLister.findByDevise", query = "SELECT c FROM CommandeLister c WHERE c.devise = :devise"),
    @NamedQuery(name = "CommandeLister.findByRegion", query = "SELECT c FROM CommandeLister c WHERE c.region = :region")})

public class CommandeLister implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "uid")
    private String uid;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "quantite")
    private Double quantite;
    @Column(name = "cout_achat")
    private Double coutAchat;
    @Size(max = 100)
    @Column(name = "devise")
    private String devise;
    @Size(max = 100)
    @Column(name = "region")
    private String region;
    @JoinColumn(name = "commande_id", referencedColumnName = "uid")
    @ManyToOne(optional = false)
    private Commande commandeId;
    @JoinColumn(name = "matiere_id", referencedColumnName = "uid")
    @ManyToOne
    private Matiere matiereId;
    @JoinColumn(name = "sku_id", referencedColumnName = "uid")
    @ManyToOne
    private MatiereSku skuId;
    @JoinColumn(name = "mesure_id", referencedColumnName = "uid")
    @ManyToOne
    private Mesure mesureId;
    @JoinColumn(name = "produit_id", referencedColumnName = "uid")
    @ManyToOne
    private Produit produitId;
    @Column(name = "deleted_at", columnDefinition = "DATETIME")
     private LocalDateTime deletedAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

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


    public CommandeLister() {
    }

    public CommandeLister(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Double getQuantite() {
        return quantite;
    }

    public void setQuantite(Double quantite) {
        this.quantite = quantite;
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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Commande getCommandeId() {
        return commandeId;
    }

    public void setCommandeId(Commande commandeId) {
        this.commandeId = commandeId;
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

    public Produit getProduitId() {
        return produitId;
    }

    public void setProduitId(Produit produitId) {
        this.produitId = produitId;
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
        if (!(object instanceof CommandeLister)) {
            return false;
        }
        CommandeLister other = (CommandeLister) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ejb.entities.CommandeLister[ uid=" + uid + " ]";
    }
    
}
