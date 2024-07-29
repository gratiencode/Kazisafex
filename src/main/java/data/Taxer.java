/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.fasterxml.jackson.annotation.JsonBackReference;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;


import jakarta.xml.bind.annotation.XmlRootElement;


/**
 *
 * @author eroot
 */
@Entity
@Table(name = "taxer")
 @XmlRootElement

@NamedQueries({
    @NamedQuery(name = "Taxer.findAll", query = "SELECT DISTINCT  t FROM Taxer t")
    , @NamedQuery(name = "Taxer.findByUid", query = "SELECT DISTINCT  t FROM Taxer t WHERE t.uid = :uid")
    , @NamedQuery(name = "Taxer.findByMontantPercu", query = "SELECT DISTINCT  t FROM Taxer t WHERE t.montantPercu = :montantPercu")})
public class Taxer extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "uid", updatable = false, nullable = false)
    private String uid;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "montant_percu")
    private Double montantPercu;
   
    @JoinColumn(name = "taxe_id", referencedColumnName = "uid")
    @ManyToOne
    @JsonBackReference
    private Taxe taxeId;
    @JoinColumn(name = "vente_reference", referencedColumnName = "uid")
    @ManyToOne
    @JsonBackReference
    private Vente venteReference;
    
    

    public Taxer() {
    }

    public Taxer(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Double getMontantPercu() {
        return montantPercu;
    }

    public void setMontantPercu(Double montantPercu) {
        this.montantPercu = montantPercu;
    }

    public Taxe getTaxeId() {
        return taxeId;
    }

    public void setTaxeId(Taxe taxeId) {
        this.taxeId = taxeId;
    }

    public Vente getVenteReference() {
        return venteReference;
    }

    public void setVenteReference(Vente venteReference) {
        this.venteReference = venteReference;
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
        if (!(object instanceof Taxer)) {
            return false;
        }
        Taxer other = (Taxer) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Taxer[ uid=" + uid + " ]";
    }
    
}
