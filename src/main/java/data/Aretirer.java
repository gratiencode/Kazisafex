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
@Table(name = "aretirer")
@XmlRootElement

@NamedQueries({
    @NamedQuery(name = "Aretirer.findAll", query = "SELECT DISTINCT  a FROM Aretirer a")
    , @NamedQuery(name = "Aretirer.findByUid", query = "SELECT DISTINCT  a FROM Aretirer a WHERE a.uid = :uid")
    , @NamedQuery(name = "Aretirer.findByNumlot", query = "SELECT DISTINCT  a FROM Aretirer a WHERE a.numlot = :numlot")
    , @NamedQuery(name = "Aretirer.findByPrixVente", query = "SELECT DISTINCT  a FROM Aretirer a WHERE a.prixVente = :prixVente")
    , @NamedQuery(name = "Aretirer.findByQuantite", query = "SELECT DISTINCT  a FROM Aretirer a WHERE a.quantite = :quantite")
    , @NamedQuery(name = "Aretirer.findByDate", query = "SELECT DISTINCT  a FROM Aretirer a WHERE a.date = :date")
    , @NamedQuery(name = "Aretirer.findByReferenceVente", query = "SELECT DISTINCT  a FROM Aretirer a WHERE a.referenceVente = :referenceVente")})
public class Aretirer extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "uid", updatable = false, nullable = false)
    private String uid;
    @Column(name = "region")
    private String region;
    @Column(name = "numlot")
    private String numlot;
    @Column(name = "status")
    private String status;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "prix_vente")
    private Double prixVente;
    @Column(name = "quantite")
    private Double quantite;
    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss"
    )
    @Column(name = "date_")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    @Column(name = "reference_vente")
    private String referenceVente;
    @JoinColumn(name = "client_id", referencedColumnName = "uid")
    @ManyToOne(optional = true)
    @JsonBackReference
    private Client clientId;
    
  
    
    @JoinColumn(name = "ligne_vente_id", referencedColumnName = "uid")
    @ManyToOne
    @JsonBackReference
    private LigneVente ligneVenteId;
    @JoinColumn(name = "mesure_id", referencedColumnName = "uid")
    @ManyToOne(optional = false)
    @JsonBackReference
    private Mesure mesureId;

    public Aretirer() {
        this.type=Tables.ARETIRER.name();
    }

    @PrePersist
    @PreUpdate
    protected void onDataOperation(){
        if(this.uid==null){
            this.uid= UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
    }
    
    public Aretirer(String uid) {
        this.uid = uid;
        this.type=Tables.ARETIRER.name();
    }

    public Aretirer(String uid, String referenceVente) {
        this.uid = uid;
        this.referenceVente = referenceVente;
        this.type=Tables.ARETIRER.name();
    }

     

    
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNumlot() {
        return numlot;
    }

    public void setNumlot(String numlot) {
        this.numlot = numlot;
    }

    public Double getPrixVente() {
        return prixVente;
    }

    public void setPrixVente(Double prixVente) {
        this.prixVente = prixVente;
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

    public String getReferenceVente() {
        return referenceVente;
    }

    public void setReferenceVente(String referenceVente) {
        this.referenceVente = referenceVente;
    }

    public Client getClientId() {
        return clientId;
    }

    public void setClientId(Client clientId) {
        this.clientId = clientId;
    }

   
       
    

   
        
    

   

    public LigneVente getLigneVenteId() {
        return ligneVenteId;
    }

    public void setLigneVenteId(LigneVente ligneVenteId) {
        this.ligneVenteId = ligneVenteId;
    }

    public Mesure getMesureId() {
        return mesureId;
    }

    public void setMesureId(Mesure mesureId) {
        this.mesureId = mesureId;
    }
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
        if (!(object instanceof Aretirer)) {
            return false;
        }
        Aretirer other = (Aretirer) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Aretirer[ uid=" + uid + " ]";
    }
     public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
    
}
