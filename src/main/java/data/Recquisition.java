/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import jakarta.json.bind.annotation.JsonbTransient;

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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.UUID;


 import org.hibernate.annotations.UuidGenerator;
import jakarta.xml.bind.annotation.XmlRootElement;
import tools.Tables;


/**
 *
 * @author eroot
 */
@Entity
@Table(name = "recquisition")
 @XmlRootElement 

@NamedQueries({
    @NamedQuery(name = "Recquisition.findAll", query = "SELECT DISTINCT  r FROM Recquisition r ORDER BY r.date DESC")
    , @NamedQuery(name = "Recquisition.findByUid", query = "SELECT DISTINCT  r FROM Recquisition r WHERE r.uid = :uid")
    , @NamedQuery(name = "Recquisition.findByDate", query = "SELECT DISTINCT  r FROM Recquisition r WHERE r.date = :date")
    , @NamedQuery(name = "Recquisition.findByObservation", query = "SELECT DISTINCT  r FROM Recquisition r WHERE r.observation = :observation")
    , @NamedQuery(name = "Recquisition.findByReference", query = "SELECT DISTINCT  r FROM Recquisition r WHERE r.reference = :reference")
    , @NamedQuery(name = "Recquisition.findByQuantite", query = "SELECT DISTINCT  r FROM Recquisition r WHERE r.quantite = :quantite")
    , @NamedQuery(name = "Recquisition.findByCoutAchat", query = "SELECT DISTINCT  r FROM Recquisition r WHERE r.coutAchat = :coutAchat")
    , @NamedQuery(name = "Recquisition.findByRegion", query = "SELECT DISTINCT  r FROM Recquisition r WHERE r.region = :region")
    , @NamedQuery(name = "Recquisition.findByDateExpiry", query = "SELECT DISTINCT  r FROM Recquisition r WHERE r.dateExpiry = :dateExpiry")
    , @NamedQuery(name = "Recquisition.findByStockAlert", query = "SELECT DISTINCT  r FROM Recquisition r WHERE r.stockAlert = :stockAlert")})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "uid")
public class Recquisition extends BaseModel implements Serializable {

    private String observation;
    private String reference;
    private double quantite;
    private double coutAchat;
    private String region;
    private String numlot;

    private static final long serialVersionUID = 1L;
    @Id
  
    private String uid;
    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss"
    )
  
    @Column(columnDefinition = "TIMESTAMP")
    private Date date;
    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd"
    )
    @Temporal(TemporalType.DATE)
    private Date dateExpiry;
    private Double stockAlert;
    @OneToMany(mappedBy = "recquisitionId", cascade = {CascadeType.PERSIST,CascadeType.REMOVE})
    @JsonIgnore
    @JsonManagedReference
    private List<PrixDeVente> prixDeVenteList;
    @JoinColumn(name = "mesure_id", referencedColumnName = "uid")
    @ManyToOne
    @JsonBackReference
    private Mesure mesureId;
    @JoinColumn(name = "product_id", referencedColumnName = "uid")
    @ManyToOne
    @JsonBackReference
    private Produit productId;
    
    @PrePersist
    @PreUpdate
    protected void onDataOperation(){
        if(this.uid==null){
            this.uid= UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
    }

    public Recquisition() {
        this.type=Tables.RECQUISITION.name();
    }

    public Recquisition(String uid) {
        this.uid = uid;
         this.type=Tables.RECQUISITION.name();
    }

    public Recquisition(String uid, double quantite, double coutAchat) {
        this.uid = uid;
        this.quantite = quantite;
        this.coutAchat = coutAchat;
         this.type=Tables.RECQUISITION.name();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }


    public double getCoutAchat() {
        return coutAchat;
    }

    public void setCoutAchat(double coutAchat) {
        this.coutAchat = coutAchat;
    }


    public Date getDateExpiry() {
        return dateExpiry;
    }

    public void setDateExpiry(Date dateExpiry) {
        this.dateExpiry = dateExpiry;
    }

    public Double getStockAlert() {
        return stockAlert;
    }

    public void setStockAlert(Double stockAlert) {
        this.stockAlert = stockAlert;
    }

    
     @JsonbTransient 
     public List<PrixDeVente> getPrixDeVenteList() {
        return prixDeVenteList;
    }

    public void setPrixDeVenteList(List<PrixDeVente> prixDeVenteList) {
        this.prixDeVenteList = prixDeVenteList;
    }

   
 
    public Mesure getMesureId() {
        return mesureId;
    }

    public void setMesureId(Mesure mesureId) {
        this.mesureId = mesureId;
    }

    public Produit getProductId() {
        return productId;
    }

    public void setProductId(Produit productId) {
        this.productId = productId;
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
        if (!(object instanceof Recquisition)) {
            return false;
        }
        Recquisition other = (Recquisition) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Recquisition[ uid=" + uid + " ]";
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }


    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getNumlot() {
        return numlot;
    }

    public void setNumlot(String numlot) {
        this.numlot = numlot;
    }
    
}
