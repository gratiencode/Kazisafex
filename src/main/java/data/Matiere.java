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
import jakarta.persistence.CascadeType;
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
@Table(name = "matiere")
@NamedQueries({
    @NamedQuery(name = "Matiere.findAll", query = "SELECT m FROM Matiere m"),
    @NamedQuery(name = "Matiere.findByUid", query = "SELECT m FROM Matiere m WHERE m.uid = :uid"),
    @NamedQuery(name = "Matiere.findByMatiereName", query = "SELECT m FROM Matiere m WHERE m.matiereName = :matiereName"),
    @NamedQuery(name = "Matiere.findByTypeMatiere", query = "SELECT m FROM Matiere m WHERE m.typeMatiere = :typeMatiere"),
    @NamedQuery(name = "Matiere.findByPerissable", query = "SELECT m FROM Matiere m WHERE m.perissable = :perissable"),
    @NamedQuery(name = "Matiere.findByRegion", query = "SELECT m FROM Matiere m WHERE m.region = :region")})

public class Matiere implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "uid")
    private String uid;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "matiere_name")
    private String matiereName;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "type_matiere")
    private String typeMatiere;
    @Basic(optional = false)
    @NotNull
    @Column(name = "perissable")
    private boolean perissable;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "region")
    private String region;
    @OneToMany(mappedBy = "matiereId")
    @JsonBackReference(value = "mat-cmdl")
    private List<CommandeLister> commandeListerList;
    @OneToMany(mappedBy = "matiereId")
    @JsonBackReference(value = "mat-entrpoz")
    private List<Entreposer> entreposerList;
    @OneToMany(mappedBy = "matiere")
    @JsonBackReference(value = "mat-matsku")
    private List<MatiereSku> matiereSkuList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "matiereId")
    @JsonBackReference(value = "mat-repartir")
    private List<Repartir> repartirList;
    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    public Matiere() {
    }

    public Matiere(String uid) {
        this.uid = uid;
    }

    public Matiere(String uid, String matiereName, String typeMatiere, boolean perissable, String region) {
        this.uid = uid;
        this.matiereName = matiereName;
        this.typeMatiere = typeMatiere;
        this.perissable = perissable;
        this.region = region;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMatiereName() {
        return matiereName;
    }

    public void setMatiereName(String matiereName) {
        this.matiereName = matiereName;
    }

    public String getTypeMatiere() {
        return typeMatiere;
    }

    public void setTypeMatiere(String typeMatiere) {
        this.typeMatiere = typeMatiere;
    }

    public boolean getPerissable() {
        return perissable;
    }

    public void setPerissable(boolean perissable) {
        this.perissable = perissable;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public List<CommandeLister> getCommandeListerList() {
        return commandeListerList;
    }

    public void setCommandeListerList(List<CommandeLister> commandeListerList) {
        this.commandeListerList = commandeListerList;
    }

    public List<Entreposer> getEntreposerList() {
        return entreposerList;
    }

    public void setEntreposerList(List<Entreposer> entreposerList) {
        this.entreposerList = entreposerList;
    }

    public List<MatiereSku> getMatiereSkuList() {
        return matiereSkuList;
    }

    public void setMatiereSkuList(List<MatiereSku> matiereSkuList) {
        this.matiereSkuList = matiereSkuList;
    }

    public List<Repartir> getRepartirList() {
        return repartirList;
    }

    public void setRepartirList(List<Repartir> repartirList) {
        this.repartirList = repartirList;
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
        if (!(object instanceof Matiere)) {
            return false;
        }
        Matiere other = (Matiere) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ejb.entities.Matiere[ uid=" + uid + " ]";
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
