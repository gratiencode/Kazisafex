/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;  import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator; import jakarta.xml.bind.annotation.XmlRootElement;
import tools.Tables;



/**
 *
 * @author eroot
 */
@Entity
@Table(name = "facture")
 
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Facture.findAll", query = "SELECT DISTINCT  f FROM Facture f")
    , @NamedQuery(name = "Facture.findByUid", query = "SELECT DISTINCT  f FROM Facture f WHERE f.uid = :uid")
    , @NamedQuery(name = "Facture.findByNumero", query = "SELECT DISTINCT  f FROM Facture f WHERE f.numero = :numero")
    , @NamedQuery(name = "Facture.findByStartDate", query = "SELECT DISTINCT  f FROM Facture f WHERE f.startDate = :startDate")
    , @NamedQuery(name = "Facture.findByEndDate", query = "SELECT DISTINCT  f FROM Facture f WHERE f.endDate = :endDate")
    , @NamedQuery(name = "Facture.findByStatus", query = "SELECT DISTINCT  f FROM Facture f WHERE f.status = :status")
    , @NamedQuery(name = "Facture.findByTotalamount", query = "SELECT DISTINCT  f FROM Facture f WHERE f.totalamount = :totalamount")
    , @NamedQuery(name = "Facture.findByPayedamount", query = "SELECT DISTINCT  f FROM Facture f WHERE f.payedamount = :payedamount")
    , @NamedQuery(name = "Facture.findByRegion", query = "SELECT DISTINCT  f FROM Facture f WHERE f.region = :region")})
public class Facture extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "uid", updatable = false, nullable = false)
   
    private  String uid;
    @Column(name = "numero")
    private String numero;
    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd"
    )
    @Column(name = "start_date")
    @Temporal(TemporalType.DATE)
    private Date startDate;
    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd"
    )
    @Column(name = "end_date")
    @Temporal(TemporalType.DATE)
    private Date endDate;
    @Column(name = "status")
    private String status;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "totalamount")
    private Double totalamount;
    @Column(name = "payedamount")
    private Double payedamount;
    @Column(name = "region")
    private String region;
    @JoinColumn(name = "organis_id", referencedColumnName = "uid")
    @ManyToOne
    @JsonBackReference
    private ClientOrganisation organisId;
    
    
@PrePersist
    @PreUpdate
    protected void onDataOperation(){
        if(this.uid==null){
            this.uid= UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
    }
    public Facture() {
        this.type=Tables.FACTURE.name();
    }

    public Facture(String uid) {
        this.uid = uid;
        this.type=Tables.FACTURE.name();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getTotalamount() {
        return totalamount;
    }

    public void setTotalamount(Double totalamount) {
        this.totalamount = totalamount;
    }

    public Double getPayedamount() {
        return payedamount;
    }

    public void setPayedamount(Double payedamount) {
        this.payedamount = payedamount;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public ClientOrganisation getOrganisId() {
        return organisId;
    }

    public void setOrganisId(ClientOrganisation organisId) {
        this.organisId = organisId;
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
        if (!(object instanceof Facture)) {
            return false;
        }
        Facture other = (Facture) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Facture[ uid=" + uid + " ]";
    }
    
}
