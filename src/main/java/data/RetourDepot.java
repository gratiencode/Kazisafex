/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.fasterxml.jackson.annotation.JsonBackReference;
import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;  import jakarta.persistence.Entity;
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
@Table(name = "retour_depot")
 @XmlRootElement

@NamedQueries({
    @NamedQuery(name = "RetourDepot.findAll", query = "SELECT DISTINCT  r FROM RetourDepot r")
    , @NamedQuery(name = "RetourDepot.findByUid", query = "SELECT DISTINCT  r FROM RetourDepot r WHERE r.uid = :uid")
    , @NamedQuery(name = "RetourDepot.findByRegionProv", query = "SELECT DISTINCT  r FROM RetourDepot r WHERE r.regionProv = :regionProv")
    , @NamedQuery(name = "RetourDepot.findByCoutAchat", query = "SELECT DISTINCT  r FROM RetourDepot r WHERE r.coutAchat = :coutAchat")
    , @NamedQuery(name = "RetourDepot.findByQuantite", query = "SELECT DISTINCT  r FROM RetourDepot r WHERE r.quantite = :quantite")
    , @NamedQuery(name = "RetourDepot.findByDate", query = "SELECT DISTINCT  r FROM RetourDepot r WHERE r.date = :date")
    , @NamedQuery(name = "RetourDepot.findByRegion", query = "SELECT DISTINCT  r FROM RetourDepot r WHERE r.region = :region")
    , @NamedQuery(name = "RetourDepot.findByMotif", query = "SELECT DISTINCT  r FROM RetourDepot r WHERE r.motif = :motif")
    , @NamedQuery(name = "RetourDepot.findByNumlot", query = "SELECT DISTINCT  r FROM RetourDepot r WHERE r.numlot = :numlot")
    , @NamedQuery(name = "RetourDepot.findByLocalisation", query = "SELECT DISTINCT  r FROM RetourDepot r WHERE r.localisation = :localisation")
    , @NamedQuery(name = "RetourDepot.findByRegionDest", query = "SELECT DISTINCT  r FROM RetourDepot r WHERE r.regionDest = :regionDest")})

public class RetourDepot extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "uid", updatable = false, nullable = false)
  
    private String uid;
    @Basic(optional = false)
    @Column(name = "region_prov")
    private String regionProv;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "cout_achat")
    private Double coutAchat;
    @Column(name = "quantite")
    private Double quantite;
    @Column(name = "date_")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    @Column(name = "region")
    private String region;
    @Column(name = "motif")
    private String motif;
    @Column(name = "numlot")
    private String numlot;
    @Column(name = "localisation")
    private String localisation;
    @Column(name = "region_dest")
    private String regionDest;
    @JoinColumn(name = "destocker_id", referencedColumnName = "uid")
    @ManyToOne
    @JsonBackReference
    private Destocker destockerId;
    
   
    
    
   
    @JoinColumn(name = "mesure_id", referencedColumnName = "uid")
    @ManyToOne(optional = false)
    @JsonBackReference
    private Mesure mesureId;
    @JoinColumn(name = "recquisition_id", referencedColumnName = "uid")
    @ManyToOne(optional = false)
    @JsonBackReference
    private Recquisition recquisitionId;

    public RetourDepot() {
         this.type=Tables.RETOURDEPOT.name();
    }

    @PrePersist
    @PreUpdate
    protected void onDataOperation(){
        if(this.uid==null){
            this.uid= UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
    }
    
    public RetourDepot(String uid) {
        this.uid = uid;
          this.type=Tables.RETOURDEPOT.name();
    }

    public RetourDepot(String uid, String regionProv) {
        this.uid = uid;
        this.regionProv = regionProv;
           this.type=Tables.RETOURDEPOT.name();
    }

    
    
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getRegionProv() {
        return regionProv;
    }

    public void setRegionProv(String regionProv) {
        this.regionProv = regionProv;
    }

    public Double getCoutAchat() {
        return coutAchat;
    }

    public void setCoutAchat(Double coutAchat) {
        this.coutAchat = coutAchat;
    }

    public Double getQuantite() {
        return quantite;
    }

    public void setQuantite(Double quantite) {
        this.quantite = quantite;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getNumlot() {
        return numlot;
    }

    public void setNumlot(String numlot) {
        this.numlot = numlot;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public String getRegionDest() {
        return regionDest;
    }

    public void setRegionDest(String regionDest) {
        this.regionDest = regionDest;
    }

    public Destocker getDestockerId() {
        return destockerId;
    }

    public void setDestockerId(Destocker destockerId) {
        this.destockerId = destockerId;
    }

   
 
   

    public Mesure getMesureId() {
        return mesureId;
    }

    public void setMesureId(Mesure mesureId) {
        this.mesureId = mesureId;
    }

    public Recquisition getRecquisitionId() {
        return recquisitionId;
    }

    public void setRecquisitionId(Recquisition recquisitionId) {
        this.recquisitionId = recquisitionId;
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
        if (!(object instanceof RetourDepot)) {
            return false;
        }
        RetourDepot other = (RetourDepot) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.RetourDepot[ uid=" + uid + " ]";
    }
    
}
