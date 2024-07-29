/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import java.util.Date;
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
@Table(name = "destocker")
 @XmlRootElement

@NamedQueries({
    @NamedQuery(name = "Destocker.findAll", query = "SELECT DISTINCT  d FROM Destocker d ORDER BY d.dateDestockage DESC"),
    @NamedQuery(name = "Destocker.findByUid", query = "SELECT DISTINCT  d FROM Destocker d WHERE d.uid = :uid"),
    @NamedQuery(name = "Destocker.findByRegion", query = "SELECT DISTINCT  d FROM Destocker d WHERE d.region = :region"),
    @NamedQuery(name = "Destocker.findByDateDestockage", query = "SELECT DISTINCT  d FROM Destocker d WHERE d.dateDestockage = :dateDestockage"),
    @NamedQuery(name = "Destocker.findByCoutAchat", query = "SELECT DISTINCT  d FROM Destocker d WHERE d.coutAchat = :coutAchat"),
    @NamedQuery(name = "Destocker.findByQuantite", query = "SELECT DISTINCT  d FROM Destocker d WHERE d.quantite = :quantite")})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "uid")
public class Destocker extends BaseModel implements Serializable {

    private String region;
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss"
    )
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateDestockage;
    private String reference;
    private String destination;
    private double coutAchat;
    private double quantite;
    private String libelle;
    private String numlot;
    private String observation;

    private static final long serialVersionUID = 1L;
    @Id
   
    private String uid;
    @JoinColumn(name = "mesure_id", referencedColumnName = "uid")
    @ManyToOne
    @JsonBackReference
    private Mesure mesureId;
    @JoinColumn(name = "product_id", referencedColumnName = "uid")
    @ManyToOne(optional = false)
    @JsonBackReference
    private Produit productId;

    
@PrePersist
    @PreUpdate
    protected void onDataOperation(){
        if(this.uid==null){
            this.uid= UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
    }
    public Destocker() {
        this.type = Tables.DESTOCKER.name();
    }

    public Destocker(String uid) {
        this.uid = uid;
        this.type = Tables.DESTOCKER.name();
    }

    public Destocker(String uid, Date dateDestockage, String reference, String destination, double coutAchat, double quantite, String libelle, String observation) {
        this.uid = uid;
        this.dateDestockage = dateDestockage;
        this.reference = reference;
        this.destination = destination;
        this.coutAchat = coutAchat;
        this.quantite = quantite;
        this.libelle = libelle;
        this.observation = observation;
        this.type = Tables.DESTOCKER.name();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getDateDestockage() {
        return dateDestockage;
    }

    public void setDateDestockage(Date dateDestockage) {
        this.dateDestockage = dateDestockage;
    }

    public double getCoutAchat() {
        return coutAchat;
    }

    public void setCoutAchat(double coutAchat) {
        this.coutAchat = coutAchat;
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
        if (!(object instanceof Destocker)) {
            return false;
        }
        Destocker other = (Destocker) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Destocker[ uid=" + uid + " ]";
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getNumlot() {
        return numlot;
    }

    public void setNumlot(String numlot) {
        this.numlot = numlot;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }
}
