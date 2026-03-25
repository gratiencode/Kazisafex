/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data; import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.util.UUID;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import tools.Tables;

/**
 *
 * @author eroot
 */
@Entity
@Table(name = "livraison")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Livraison.findAll", query = "SELECT DISTINCT  l FROM Livraison l ORDER BY l.dateLivr DESC"),
    @NamedQuery(name = "Livraison.findByUid", query = "SELECT DISTINCT  l FROM Livraison l WHERE l.uid = :uid"),
    @NamedQuery(name = "Livraison.findByDateLivr", query = "SELECT DISTINCT  l FROM Livraison l WHERE l.dateLivr = :dateLivr"),
    @NamedQuery(name = "Livraison.findByReference", query = "SELECT DISTINCT  l FROM Livraison l WHERE l.reference = :reference")})

public class Livraison extends BaseModel implements Serializable {

    private String numPiece;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    private LocalDate dateLivr;
    private String reference;
    private String libelle;
    private String region;
    private String observation;
    private Double reduction;
    private Double topay;
    private Double payed;
    private Double remained = 0d;
    private Double toreceive = 0d;
    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    private static final long serialVersionUID = 1L;
    @Id
    private String uid;
    @OneToMany(mappedBy = "livraisId")
     @JsonBackReference(value = "liv-stk")
    private List<Stocker> stockerList;
     @JsonBackReference(value = "liv-entrpoz")
    @OneToMany(mappedBy = "livraisonId")
    private List<Entreposer> entreposerList;
     @JsonBackReference(value = "liv-satisf")
    @OneToMany(mappedBy = "livraisonId")
    private List<Satisfaire> satisfaireList;
    @ManyToOne(optional = false)
    
    private Fournisseur fournId;

    @PrePersist
    @PreUpdate
    protected void onDataOperation() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
        this.updatedAt = LocalDateTime.now();
    }

    public Livraison() {
        this.type = Tables.LIVRAISON.name();
    }

    public Livraison(String uid) {
        this.uid = uid;
        this.type = Tables.LIVRAISON.name();
    }

    public Livraison(String uid, String numPiece, LocalDate dateLivr) {
        this.uid = uid;
        this.numPiece = numPiece;
        this.dateLivr = dateLivr;
        this.type = Tables.LIVRAISON.name();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNumPiece() {
        return numPiece;
    }

    public void setNumPiece(String numPiece) {
        this.numPiece = numPiece;
    }

    public LocalDate getDateLivr() {
        return dateLivr;
    }

    public void setDateLivr(LocalDate dateLivr) {
        this.dateLivr = dateLivr;
    }

    
    public List<Stocker> getStockerList() {
        return stockerList;
    }

    public void setStockerList(List<Stocker> stockerList) {
        this.stockerList = stockerList;
    }

    public Fournisseur getFournId() {
        return fournId;
    }

    public void setFournId(Fournisseur fournId) {
        this.fournId = fournId;
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
        if (!(object instanceof Livraison)) {
            return false;
        }
        Livraison other = (Livraison) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Livraison[ uid=" + uid + " ]";
    }

    public Double getTopay() {
        return topay;
    }

    public void setTopay(Double topay) {
        this.topay = topay;
    }

    public Double getPayed() {
        return payed;
    }

    public void setPayed(Double payed) {
        this.payed = payed;
    }

    public Double getRemained() {
        return remained;
    }

    public void setRemained(Double remained) {
        this.remained = remained;
    }

    public Double getToreceive() {
        return toreceive;
    }

    public void setToreceive(Double toreceive) {
        this.toreceive = toreceive;
    }

    public Double getReduction() {
        return reduction;
    }

    public void setReduction(Double reduction) {
        this.reduction = reduction;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    
    public List<Entreposer> getEntreposerList() {
        return entreposerList;
    }

    public void setEntreposerList(List<Entreposer> entreposerList) {
        this.entreposerList = entreposerList;
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
