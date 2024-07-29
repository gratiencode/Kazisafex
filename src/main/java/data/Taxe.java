/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.io.Serializable;
import java.util.List;
import jakarta.json.bind.annotation.JsonbTransient;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;


import org.hibernate.annotations.UuidGenerator;
import jakarta.xml.bind.annotation.XmlRootElement;


/**
 *
 * @author eroot
 */
@Entity
@Table(name = "taxe")
 @XmlRootElement 

@NamedQueries({
    @NamedQuery(name = "Taxe.findAll", query = "SELECT DISTINCT  t FROM Taxe t")
    , @NamedQuery(name = "Taxe.findByUid", query = "SELECT DISTINCT  t FROM Taxe t WHERE t.uid = :uid")
    , @NamedQuery(name = "Taxe.findByDescription", query = "SELECT DISTINCT  t FROM Taxe t WHERE t.description = :description")
    , @NamedQuery(name = "Taxe.findByTaux", query = "SELECT DISTINCT  t FROM Taxe t WHERE t.taux = :taux")
    , @NamedQuery(name = "Taxe.findByResumee", query = "SELECT DISTINCT  t FROM Taxe t WHERE t.resumee = :resumee")})
public class Taxe extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
  
    private String uid;
    private String description;
    private double taux;
    private String resumee;
    @OneToMany(mappedBy = "taxeId")
    @JsonManagedReference
    private List<Taxer> taxerList;
    
     

    public Taxe() {
    }

    public Taxe(String uid) {
        this.uid = uid;
    }

    public Taxe(String uid, String description, double taux) {
        this.uid = uid;
        this.description = description;
        this.taux = taux;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getTaux() {
        return taux;
    }

    public void setTaux(double taux) {
        this.taux = taux;
    }

    public String getResumee() {
        return resumee;
    }

    public void setResumee(String resumee) {
        this.resumee = resumee;
    }

    
     @JsonbTransient public List<Taxer> getTaxerList() {
        return taxerList;
    }

    public void setTaxerList(List<Taxer> taxerList) {
        this.taxerList = taxerList;
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
        if (!(object instanceof Taxe)) {
            return false;
        }
        Taxe other = (Taxe) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Taxe[ uid=" + uid + " ]";
    }
    
}
