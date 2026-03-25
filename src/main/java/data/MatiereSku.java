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
import jakarta.persistence.JoinColumns;
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
@Table(name = "matiere_sku")
@NamedQueries({
    @NamedQuery(name = "MatiereSku.findAll", query = "SELECT m FROM MatiereSku m"),
    @NamedQuery(name = "MatiereSku.findByUid", query = "SELECT m FROM MatiereSku m WHERE m.uid = :uid"),
    @NamedQuery(name = "MatiereSku.findByNomSku", query = "SELECT m FROM MatiereSku m WHERE m.nomSku = :nomSku"),
    @NamedQuery(name = "MatiereSku.findByQuantContenuSku", query = "SELECT m FROM MatiereSku m WHERE m.quantContenuSku = :quantContenuSku"),
    @NamedQuery(name = "MatiereSku.findByRegion", query = "SELECT m FROM MatiereSku m WHERE m.region = :region")})

public class MatiereSku implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "uid")
    private String uid;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "nom_sku")
    private String nomSku;
    @Basic(optional = false)
    @NotNull
    @Column(name = "quant_contenu_sku")
    private double quantContenuSku;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "region")
    private String region;
    @OneToMany(mappedBy = "skuId")
     @JsonBackReference(value = "matsku-cmdl")
    private List<CommandeLister> commandeListerList;
    @OneToMany(mappedBy = "skuId")
     @JsonBackReference(value = "matsku-entrpoz")
    private List<Entreposer> entreposerList;
    @JoinColumns({
    @JoinColumn(name = "matiere_id", referencedColumnName = "uid")})
    @ManyToOne
    private Matiere matiere;
    @OneToMany(mappedBy = "skuId")
     @JsonBackReference(value = "matsku-repartir")
    private List<Repartir> repartirList;
    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    public MatiereSku() {
    }

    public MatiereSku(String uid) {
        this.uid = uid;
    }

    public MatiereSku(String uid, String nomSku, double quantContenuSku, String region) {
        this.uid = uid;
        this.nomSku = nomSku;
        this.quantContenuSku = quantContenuSku;
        this.region = region;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNomSku() {
        return nomSku;
    }

    public void setNomSku(String nomSku) {
        this.nomSku = nomSku;
    }

    public double getQuantContenuSku() {
        return quantContenuSku;
    }

    public void setQuantContenuSku(double quantContenuSku) {
        this.quantContenuSku = quantContenuSku;
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

    public Matiere getMatiere() {
        return matiere;
    }

    public void setMatiere(Matiere matiere) {
        this.matiere = matiere;
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
        if (!(object instanceof MatiereSku)) {
            return false;
        }
        MatiereSku other = (MatiereSku) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ejb.entities.MatiereSku[ uid=" + uid + " ]";
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
