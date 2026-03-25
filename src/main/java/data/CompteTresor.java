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
@Table(name = "compte_tresor")

@XmlRootElement

@NamedQueries({
    @NamedQuery(name = "CompteTresor.findAll", query = "SELECT DISTINCT  c FROM CompteTresor c"),
    @NamedQuery(name = "CompteTresor.findByUid", query = "SELECT DISTINCT  c FROM CompteTresor c WHERE c.uid = :uid"),
    @NamedQuery(name = "CompteTresor.findByIntitule", query = "SELECT DISTINCT  c FROM CompteTresor c WHERE c.intitule = :intitule"),
    @NamedQuery(name = "CompteTresor.findByTypeCompte", query = "SELECT DISTINCT  c FROM CompteTresor c WHERE c.typeCompte = :typeCompte"),
    @NamedQuery(name = "CompteTresor.findByNumeroCompte", query = "SELECT DISTINCT  c FROM CompteTresor c WHERE c.numeroCompte = :numeroCompte"),
    @NamedQuery(name = "CompteTresor.findByBankName", query = "SELECT DISTINCT  c FROM CompteTresor c WHERE c.bankName = :bankName"),
    @NamedQuery(name = "CompteTresor.findBySoldeMinimum", query = "SELECT DISTINCT  c FROM CompteTresor c WHERE c.soldeMinimum = :soldeMinimum"),
    @NamedQuery(name = "CompteTresor.findByRegion", query = "SELECT DISTINCT  c FROM CompteTresor c WHERE c.region = :region")})

public class CompteTresor extends BaseModel implements Serializable {

    @Column(name = "intitule")
    private String intitule;
    @Column(name = "type_compte")
    private String typeCompte;
    @Column(name = "numero_compte")
    private String numeroCompte;
    @Column(name = "bank_name")
    private String bankName;
    @Column(name = "region")
    private String region;
    @OneToMany(mappedBy = "tresorId")
    @JsonBackReference(value = "cpt-tr")
    private List<Traisorerie> traisorerieList;
    @OneToMany(mappedBy = "tresorId")
    @JsonBackReference(value = "cpt-ops")
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
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "solde_minimum")
    private Double soldeMinimum;

    public CompteTresor() {
        this.type = Tables.COMPTETRESOR.name();
    }

    @PrePersist
    @PreUpdate
    protected void onDataOperation() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
        this.updatedAt = LocalDateTime.now();
    }

    public CompteTresor(String uid) {
        this.uid = uid;
        this.type = Tables.COMPTETRESOR.name();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTypeCompte() {
        return typeCompte;
    }

    public void setTypeCompte(String typeCompte) {
        this.typeCompte = typeCompte;
    }

    public String getNumeroCompte() {
        return numeroCompte;
    }

    public void setNumeroCompte(String numeroCompte) {
        this.numeroCompte = numeroCompte;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public Double getSoldeMinimum() {
        return soldeMinimum;
    }

    public void setSoldeMinimum(Double soldeMinimum) {
        this.soldeMinimum = soldeMinimum;
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
        if (!(object instanceof CompteTresor)) {
            return false;
        }
        CompteTresor other = (CompteTresor) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.CompteTresor[ uid=" + uid + " ]";
    }

    public String getIntitule() {
        return intitule;
    }

    public void setIntitule(String intitule) {
        this.intitule = intitule;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public List<Traisorerie> getTraisorerieList() {
        return traisorerieList;
    }

    public void setTraisorerieList(List<Traisorerie> traisorerieList) {
        this.traisorerieList = traisorerieList;
    }

    public List<Operation> getOperationList() {
        return operationList;
    }

    public void setOperationList(List<Operation> operationList) {
        this.operationList = operationList;
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
