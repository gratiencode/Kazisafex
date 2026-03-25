/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package data;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author endeleya
 */
@Entity
@Table(name = "depot")
@NamedQueries({
    @NamedQuery(name = "Depot.findAll", query = "SELECT d FROM Depot d"),
    @NamedQuery(name = "Depot.findByUid", query = "SELECT d FROM Depot d WHERE d.uid = :uid"),
    @NamedQuery(name = "Depot.findByNomDepot", query = "SELECT d FROM Depot d WHERE d.nomDepot = :nomDepot"),
    @NamedQuery(name = "Depot.findByDimension", query = "SELECT d FROM Depot d WHERE d.dimension = :dimension"),
    @NamedQuery(name = "Depot.findByTypeDepot", query = "SELECT d FROM Depot d WHERE d.typeDepot = :typeDepot"),
    @NamedQuery(name = "Depot.findByRegion", query = "SELECT d FROM Depot d WHERE d.region = :region")})

public class Depot implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "uid")
    private String uid;
    @Size(max = 100)
    @Column(name = "nom_depot")
    private String nomDepot;
    @Size(max = 100)
    @Column(name = "dimension")
    private String dimension;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "type_depot")
    private String typeDepot;
    @Size(max = 100)
    @Column(name = "region")
    private String region;
    @OneToMany(mappedBy = "depotId")
    @JsonBackReference("depot-entrpoz")
    private List<Entreposer> entreposerList;
    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    public Depot() {
    }

    public Depot(String uid) {
        this.uid = uid;
    }

    public Depot(String uid, String typeDepot) {
        this.uid = uid;
        this.typeDepot = typeDepot;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNomDepot() {
        return nomDepot;
    }

    public void setNomDepot(String nomDepot) {
        this.nomDepot = nomDepot;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public String getTypeDepot() {
        return typeDepot;
    }

    public void setTypeDepot(String typeDepot) {
        this.typeDepot = typeDepot;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public List<Entreposer> getEntreposerList() {
        return entreposerList;
    }

    public void setEntreposerList(List<Entreposer> entreposerList) {
        this.entreposerList = entreposerList;
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
        if (!(object instanceof Depot)) {
            return false;
        }
        Depot other = (Depot) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ejb.entities.Depot[ uid=" + uid + " ]";
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
