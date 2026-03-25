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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author endeleya
 */
@Entity
@Table(name = "production")
@NamedQueries({
    @NamedQuery(name = "Production.findAll", query = "SELECT p FROM Production p"),
    @NamedQuery(name = "Production.findByUid", query = "SELECT p FROM Production p WHERE p.uid = :uid"),
    @NamedQuery(name = "Production.findByNumlot", query = "SELECT p FROM Production p WHERE p.numlot = :numlot"),
    @NamedQuery(name = "Production.findByDateDebut", query = "SELECT p FROM Production p WHERE p.dateDebut = :dateDebut"),
    @NamedQuery(name = "Production.findByDateFin", query = "SELECT p FROM Production p WHERE p.dateFin = :dateFin"),
    @NamedQuery(name = "Production.findByDatePeremption", query = "SELECT p FROM Production p WHERE p.datePeremption = :datePeremption"),
    @NamedQuery(name = "Production.findByDateFabrication", query = "SELECT p FROM Production p WHERE p.dateFabrication = :dateFabrication"),
    @NamedQuery(name = "Production.findByQuantitePrevu", query = "SELECT p FROM Production p WHERE p.quantitePrevu = :quantitePrevu"),
    @NamedQuery(name = "Production.findByQualitePrevu", query = "SELECT p FROM Production p WHERE p.qualitePrevu = :qualitePrevu"),
    @NamedQuery(name = "Production.findByComment", query = "SELECT p FROM Production p WHERE p.comment = :comment"),
    @NamedQuery(name = "Production.findByRegion", query = "SELECT p FROM Production p WHERE p.region = :region"),
    @NamedQuery(name = "Production.findByEtat", query = "SELECT p FROM Production p WHERE p.etat = :etat")})

public class Production implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "uid")
    private String uid;

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "numlot")
    private String numlot;
    @Column(name = "date_debut")
    private LocalDate dateDebut;
    @Column(name = "date_fin")
    private LocalDate dateFin;
    @Column(name = "date_peremption")
    private LocalDate datePeremption;
    @Column(name = "date_fabrication")
    private LocalDate dateFabrication;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "quantite_prevu")
    private Double quantitePrevu;
    @Size(max = 100)
    @Column(name = "qualite_prevu")
    private String qualitePrevu;
    @Size(max = 1024)
    @Column(name = "comment")
    private String comment;
    @Size(max = 100)
    @Column(name = "region")
    private String region;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "etat")
    private String etat;
    @OneToMany(mappedBy = "productionId")
    @JsonBackReference(value = "produkt-entrpoz")
    private List<Entreposer> entreposerList;
    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;
    @JoinColumn(name = "mesure_id", referencedColumnName = "uid")
    @ManyToOne
    private Mesure mesureId;
    @JoinColumn(name = "produit_id", referencedColumnName = "uid")
    @ManyToOne
    private Produit produitId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "productionId")
    @JsonBackReference(value = "produkt-repart")
    private List<Repartir> repartirList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "productionId")
    @JsonBackReference(value = "produkt-imputer")
    private List<Imputer> imputerList;

    @PrePersist
    @PreUpdate
    protected void onDataOperation() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
        this.updatedAt = LocalDateTime.now();
    }

    public Production() {
    }

    public Production(String uid) {
        this.uid = uid;
    }

    public Production(String uid, String numlot, String etat) {
        this.uid = uid;
        this.numlot = numlot;
        this.etat = etat;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNumlot() {
        return numlot;
    }

    public void setNumlot(String numlot) {
        this.numlot = numlot;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public LocalDate getDatePeremption() {
        return datePeremption;
    }

    public void setDatePeremption(LocalDate datePeremption) {
        this.datePeremption = datePeremption;
    }

    public LocalDate getDateFabrication() {
        return dateFabrication;
    }

    public void setDateFabrication(LocalDate dateFabrication) {
        this.dateFabrication = dateFabrication;
    }

    public Double getQuantitePrevu() {
        return quantitePrevu;
    }

    public void setQuantitePrevu(Double quantitePrevu) {
        this.quantitePrevu = quantitePrevu;
    }

    public String getQualitePrevu() {
        return qualitePrevu;
    }

    public void setQualitePrevu(String qualitePrevu) {
        this.qualitePrevu = qualitePrevu;
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

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public List<Entreposer> getEntreposerList() {
        return entreposerList;
    }

    public void setEntreposerList(List<Entreposer> entreposerList) {
        this.entreposerList = entreposerList;
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

    public List<Repartir> getRepartirList() {
        return repartirList;
    }

    public void setRepartirList(List<Repartir> repartirList) {
        this.repartirList = repartirList;
    }

    public List<Imputer> getImputerList() {
        return imputerList;
    }

    public void setImputerList(List<Imputer> imputerList) {
        this.imputerList = imputerList;
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
        if (!(object instanceof Production)) {
            return false;
        }
        Production other = (Production) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ejb.entities.Production[ uid=" + uid + " ]";
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
