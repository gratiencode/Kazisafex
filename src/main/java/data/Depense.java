/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.io.Serializable;
import java.util.List;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.util.UUID;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;
import tools.Tables;

/**
 *
 * @author eroot
 */
@Entity
@Table(name = "depense")
@XmlRootElement

@NamedQueries({
    @NamedQuery(name = "Depense.findAll", query = "SELECT DISTINCT  d FROM Depense d"),
    @NamedQuery(name = "Depense.findByUid", query = "SELECT DISTINCT  d FROM Depense d WHERE d.uid = :uid"),
    @NamedQuery(name = "Depense.findByNomDepense", query = "SELECT DISTINCT  d FROM Depense d WHERE d.nomDepense = :nomDepense"),
    @NamedQuery(name = "Depense.findByRegion", query = "SELECT DISTINCT  d FROM Depense d WHERE d.region = :region")})

public class Depense extends BaseModel implements Serializable {

    private String nomDepense;
    @Column(name = "frequence")
    private String frequence;
    @Column(name = "devise")
    private String devise;
    @Column(name = "montant")
    private Double montant = 0d;
    @Column(name = "region")
    private String region;
    @JsonBackReference("dep-ops")
    @OneToMany(mappedBy = "depenseId")
    private List<Operation> operationList;
    @Column(name = "deleted_at", columnDefinition = "DATETIME")
     private LocalDateTime deletedAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "uid", updatable = false, nullable = false)
    private String uid;

    public Depense() {
        this.type = Tables.DEPENSE.name();
    }

    @PrePersist
    protected void onDataOperation() {
        if (this.uid == null) {
             this.uid = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
        this.updatedAt = LocalDateTime.now();
    }
 
    @PreUpdate
    protected void onUpdate() {
       this.updatedAt = LocalDateTime.now();
    }

    public Depense(String uid) {
        this.uid = uid;
        this.type = Tables.DEPENSE.name();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNomDepense() {
        return nomDepense;
    }

    public void setNomDepense(String nomDepense) {
        this.nomDepense = nomDepense;
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
        if (!(object instanceof Depense)) {
            return false;
        }
        Depense other = (Depense) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Depense[ uid=" + uid + " ]";
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    
    public List<Operation> getOperationList() {
        return operationList;
    }

    public void setOperationList(List<Operation> operationList) {
        this.operationList = operationList;
    }

    public String getFrequence() {
        return frequence;
    }

    public void setFrequence(String frequence) {
        this.frequence = frequence;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
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
