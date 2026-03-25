/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package data; import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.List;

/**
 *
 * @author endeleya
 */
@Entity
@Table(name = "expedition")
@NamedQueries({
    @NamedQuery(name = "Expedition.findAll", query = "SELECT e FROM Expedition e"),
    @NamedQuery(name = "Expedition.findByUid", query = "SELECT e FROM Expedition e WHERE e.uid = :uid"),
    @NamedQuery(name = "Expedition.findByNumero", query = "SELECT e FROM Expedition e WHERE e.numero = :numero"),
    @NamedQuery(name = "Expedition.findByDateExpedition", query = "SELECT e FROM Expedition e WHERE e.dateExpedition = :dateExpedition"),
    @NamedQuery(name = "Expedition.findByDestination", query = "SELECT e FROM Expedition e WHERE e.destination = :destination"),
    @NamedQuery(name = "Expedition.findByRegion", query = "SELECT e FROM Expedition e WHERE e.region = :region")})

public class Expedition implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "uid")
    private String uid;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "numero")
    private String numero;
    @Column(name = "date_expedition")
    private LocalDate dateExpedition;
    @Size(max = 100)
    @Column(name = "destination")
    private String destination;
    @Size(max = 100)
    @Column(name = "region")
    private String region;
    @OneToMany(mappedBy = "expeditionId")
    private List<Satisfaire> satisfaireList;
    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    public Expedition() {
    }

    public Expedition(String uid) {
        this.uid = uid;
    }

    public Expedition(String uid, String numero) {
        this.uid = uid;
        this.numero = numero;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public LocalDate getDateExpedition() {
        return dateExpedition;
    }

    public void setDateExpedition(LocalDate dateExpedition) {
        this.dateExpedition = dateExpedition;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public List<Satisfaire> getSatisfaireList() {
        return satisfaireList;
    }

    public void setSatisfaireList(List<Satisfaire> satisfaireList) {
        this.satisfaireList = satisfaireList;
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
        if (!(object instanceof Expedition)) {
            return false;
        }
        Expedition other = (Expedition) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ejb.entities.Expedition[ uid=" + uid + " ]";
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
