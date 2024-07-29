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
import jakarta.xml.bind.annotation.XmlRootElement;
import tools.Tables;


/**
 *
 * @author eroot
 */
@Entity
@Table(name = "client_appartenir")
 @XmlRootElement

@NamedQueries({
    @NamedQuery(name = "ClientAppartenir.findAll", query = "SELECT DISTINCT  c FROM ClientAppartenir c")
    , @NamedQuery(name = "ClientAppartenir.findByUid", query = "SELECT DISTINCT  c FROM ClientAppartenir c WHERE c.uid = :uid")
    , @NamedQuery(name = "ClientAppartenir.findByDateAppartenir", query = "SELECT DISTINCT  c FROM ClientAppartenir c WHERE c.dateAppartenir = :dateAppartenir")
    , @NamedQuery(name = "ClientAppartenir.findByRegion", query = "SELECT DISTINCT  c FROM ClientAppartenir c WHERE c.region = :region")})
public class ClientAppartenir extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "uid", updatable = false, nullable = false)
  
    private String uid;
    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd"
    )
    @Column(name = "date_appartenir")
    @Temporal(TemporalType.DATE)
    private Date dateAppartenir;
    @Column(name = "region")
    private String region;
    @JoinColumn(name = "client_id", referencedColumnName = "uid")
    @ManyToOne
    @JsonBackReference
    private Client clientId;
    @JoinColumn(name = "client_organisation_id", referencedColumnName = "uid")
    @ManyToOne
    @JsonBackReference
    private ClientOrganisation clientOrganisationId;
    

    
  
   @PrePersist
    @PreUpdate
    protected void onDataOperation(){
        if(this.uid==null){
            this.uid= UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
    }

    public ClientAppartenir() {
        this.type = Tables.CLIENTAPPARTENIR.name();
    }
    
    

    public ClientAppartenir(String uid) {
        this.uid = uid;
        this.type = Tables.CLIENTAPPARTENIR.name();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getDateAppartenir() {
        return dateAppartenir;
    }

    public void setDateAppartenir(Date dateAppartenir) {
        this.dateAppartenir = dateAppartenir;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Client getClientId() {
        return clientId;
    }

    public void setClientId(Client clientId) {
        this.clientId = clientId;
    }

    public ClientOrganisation getClientOrganisationId() {
        return clientOrganisationId;
    }

    public void setClientOrganisationId(ClientOrganisation clientOrganisationId) {
        this.clientOrganisationId = clientOrganisationId;
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
        if (!(object instanceof ClientAppartenir)) {
            return false;
        }
        ClientAppartenir other = (ClientAppartenir) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.ClientAppartenir[ uid=" + uid + " ]";
    }
    
}
