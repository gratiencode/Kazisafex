/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import java.util.List;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;  import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator; import jakarta.xml.bind.annotation.XmlRootElement;
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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "uid")
public class Depense extends BaseModel implements Serializable {

    private String nomDepense;
    @Column(name = "region")
    private String region;
    @JsonManagedReference
    @OneToMany(mappedBy = "depenseId")
    private List<Operation> operationList;

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "uid", updatable = false, nullable = false)
   
    private String uid;

    public Depense() {
        this.type = Tables.DEPENSE.name();
    }

    @PrePersist
    @PreUpdate
    protected void onDataOperation(){
        if(this.uid==null){
            this.uid= UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
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

    @JsonbTransient
    public List<Operation> getOperationList() {
        return operationList;
    }

    public void setOperationList(List<Operation> operationList) {
        this.operationList = operationList;
    }

}
