/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package data;
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
@Table(name = "satisfaire")
@NamedQueries({
    @NamedQuery(name = "Satisfaire.findAll", query = "SELECT s FROM Satisfaire s"),
    @NamedQuery(name = "Satisfaire.findByUid", query = "SELECT s FROM Satisfaire s WHERE s.uid = :uid"),
    @NamedQuery(name = "Satisfaire.findByDate", query = "SELECT s FROM Satisfaire s WHERE s.date = :date"),
    @NamedQuery(name = "Satisfaire.findByEtatCommande", query = "SELECT s FROM Satisfaire s WHERE s.etatCommande = :etatCommande"),
    @NamedQuery(name = "Satisfaire.findByComment", query = "SELECT s FROM Satisfaire s WHERE s.comment = :comment"),
    @NamedQuery(name = "Satisfaire.findByRegion", query = "SELECT s FROM Satisfaire s WHERE s.region = :region")})

public class Satisfaire implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "uid")
    private String uid;
    @Column(name = "date_")
    private LocalDate date;
    @Size(max = 100)
    @Column(name = "etat_commande")
    private String etatCommande;
    @Size(max = 1024)
    @Column(name = "comment")
    private String comment;
    @Size(max = 100)
    @Column(name = "region")
    private String region;
    @JoinColumn(name = "commande_id", referencedColumnName = "uid")
    @ManyToOne
    private Commande commandeId;
    @JoinColumn(name = "expedition_id", referencedColumnName = "uid")
    @ManyToOne
    private Expedition expeditionId;
    @JoinColumn(name = "livraison_id", referencedColumnName = "uid")
    @ManyToOne
    private Livraison livraisonId;
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

    public Satisfaire() {
    }

    public Satisfaire(String uid) {
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

    public String getEtatCommande() {
        return etatCommande;
    }

    public void setEtatCommande(String etatCommande) {
        this.etatCommande = etatCommande;
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

    public Commande getCommandeId() {
        return commandeId;
    }

    public void setCommandeId(Commande commandeId) {
        this.commandeId = commandeId;
    }

    public Expedition getExpeditionId() {
        return expeditionId;
    }

    public void setExpeditionId(Expedition expeditionId) {
        this.expeditionId = expeditionId;
    }

    public Livraison getLivraisonId() {
        return livraisonId;
    }

    public void setLivraisonId(Livraison livraisonId) {
        this.livraisonId = livraisonId;
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
        if (!(object instanceof Satisfaire)) {
            return false;
        }
        Satisfaire other = (Satisfaire) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ejb.entities.Satisfaire[ uid=" + uid + " ]";
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
