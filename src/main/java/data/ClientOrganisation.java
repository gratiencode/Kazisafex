/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data; import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
import jakarta.xml.bind.annotation.XmlTransient;
import java.time.LocalDateTime;
import tools.Tables;


/**
 *
 * @author eroot
 */
@Entity
@Table(name = "client_organisation")
 @XmlRootElement

@NamedQueries({
    @NamedQuery(name = "ClientOrganisation.findAll", query = "SELECT DISTINCT  c FROM ClientOrganisation c")
    , @NamedQuery(name = "ClientOrganisation.findByUid", query = "SELECT DISTINCT  c FROM ClientOrganisation c WHERE c.uid = :uid")
    , @NamedQuery(name = "ClientOrganisation.findByRegion", query = "SELECT DISTINCT  c FROM ClientOrganisation c WHERE c.region = :region")
    , @NamedQuery(name = "ClientOrganisation.findByNomOrganisation", query = "SELECT DISTINCT  c FROM ClientOrganisation c WHERE c.nomOrganisation LIKE :nomOrganisation")
    , @NamedQuery(name = "ClientOrganisation.findByAdresse", query = "SELECT DISTINCT  c FROM ClientOrganisation c WHERE c.adresse = :adresse")
    , @NamedQuery(name = "ClientOrganisation.findByDomaineOrganisation", query = "SELECT DISTINCT  c FROM ClientOrganisation c WHERE c.domaineOrganisation = :domaineOrganisation")
    , @NamedQuery(name = "ClientOrganisation.findByPhoneOrganisation", query = "SELECT DISTINCT  c FROM ClientOrganisation c WHERE c.phoneOrganisation = :phoneOrganisation")
    , @NamedQuery(name = "ClientOrganisation.findByWebsiteOrganisation", query = "SELECT DISTINCT  c FROM ClientOrganisation c WHERE c.websiteOrganisation = :websiteOrganisation")
    , @NamedQuery(name = "ClientOrganisation.findByEmailOrganisation", query = "SELECT DISTINCT  c FROM ClientOrganisation c WHERE c.emailOrganisation = :emailOrganisation")
    , @NamedQuery(name = "ClientOrganisation.findByRccmOrganisation", query = "SELECT DISTINCT  c FROM ClientOrganisation c WHERE c.rccmOrganisation = :rccmOrganisation")
    , @NamedQuery(name = "ClientOrganisation.findByBoitePostalOrganisation", query = "SELECT DISTINCT  c FROM ClientOrganisation c WHERE c.boitePostalOrganisation = :boitePostalOrganisation")})

public class ClientOrganisation extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "uid", updatable = false, nullable = false)
  
    private String uid;
    @Column(name = "region")
    private String region;
    @Column(name = "nom_organisation")
    private String nomOrganisation;
    @Column(name = "adresse")
    private String adresse;
    @Column(name = "domaine_organisation")
    private String domaineOrganisation;
    @Column(name = "phone_organisation")
    private String phoneOrganisation;
    @Column(name = "website_organisation")
    private String websiteOrganisation;
    @Column(name = "email_organisation")
    private String emailOrganisation;
    @Column(name = "rccm_organisation")
    private String rccmOrganisation;
    @Column(name = "boite_postal_organisation")
    private String boitePostalOrganisation;
    
    @OneToMany(mappedBy = "clientOrganisationId")
    private List<ClientAppartenir> clientAppartenirList;
    @Column(name = "deleted_at", columnDefinition = "DATETIME")
     private LocalDateTime deletedAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

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

    

    
    @PrePersist
    @PreUpdate
    protected void onDataOperation(){
        if(this.uid==null){
            this.uid = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
         this.updatedAt = LocalDateTime.now();
    }

   

    public ClientOrganisation() {
        this.type = Tables.CLIENTORGANISATION.name();
    }

    public ClientOrganisation(String uid) {
        this.uid = uid;
         this.type = Tables.CLIENTORGANISATION.name();
    }

    
    
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getNomOrganisation() {
        return nomOrganisation;
    }

    public void setNomOrganisation(String nomOrganisation) {
        this.nomOrganisation = nomOrganisation;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getDomaineOrganisation() {
        return domaineOrganisation;
    }

    public void setDomaineOrganisation(String domaineOrganisation) {
        this.domaineOrganisation = domaineOrganisation;
    }

    public String getPhoneOrganisation() {
        return phoneOrganisation;
    }

    public void setPhoneOrganisation(String phoneOrganisation) {
        this.phoneOrganisation = phoneOrganisation;
    }

    public String getWebsiteOrganisation() {
        return websiteOrganisation;
    }

    public void setWebsiteOrganisation(String websiteOrganisation) {
        this.websiteOrganisation = websiteOrganisation;
    }

    public String getEmailOrganisation() {
        return emailOrganisation;
    }

    public void setEmailOrganisation(String emailOrganisation) {
        this.emailOrganisation = emailOrganisation;
    }

    public String getRccmOrganisation() {
        return rccmOrganisation;
    }

    public void setRccmOrganisation(String rccmOrganisation) {
        this.rccmOrganisation = rccmOrganisation;
    }

    public String getBoitePostalOrganisation() {
        return boitePostalOrganisation;
    }

    public void setBoitePostalOrganisation(String boitePostalOrganisation) {
        this.boitePostalOrganisation = boitePostalOrganisation;
    }

   
     
     public List<ClientAppartenir> getClientAppartenirList() {
        return clientAppartenirList;
    }

    public void setClientAppartenirList(List<ClientAppartenir> clientAppartenirList) {
        this.clientAppartenirList = clientAppartenirList;
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
        if (!(object instanceof ClientOrganisation)) {
            return false;
        }
        ClientOrganisation other = (ClientOrganisation) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.ClientOrganisation[ uid=" + uid + " ]";
    }
    
}
