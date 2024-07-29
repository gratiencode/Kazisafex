/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import java.util.List;
import jakarta.json.bind.annotation.JsonbTransient;
//
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;
import jakarta.xml.bind.annotation.XmlRootElement;
import tools.Tables;

/**
 *
 * @author eroot
 */
@Entity
@Table(name = "client")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Client.findAll", query = "SELECT DISTINCT  c FROM Client c"),
    @NamedQuery(name = "Client.findByUid", query = "SELECT DISTINCT  c FROM Client c WHERE c.uid = :uid"),
    @NamedQuery(name = "Client.findByPhone", query = "SELECT DISTINCT  c FROM Client c WHERE c.phone = :phone"),
    @NamedQuery(name = "Client.findByNomClient", query = "SELECT DISTINCT  c FROM Client c WHERE c.nomClient = :nomClient"),
    @NamedQuery(name = "Client.findByPhoneName", query = "SELECT DISTINCT  c FROM Client c WHERE c.phone = :phone AND c.nomClient = :nom"),
    @NamedQuery(name = "Client.findByTypeClient", query = "SELECT DISTINCT  c FROM Client c WHERE c.typeClient = :typeClient")})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "uid")
public class Client extends BaseModel implements Serializable {
    // @Pattern(regexp="^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$", message="Invalid phone/fax format, should be as xxx-xxx-xxxx")//if the field contains phone or fax number consider using this annotation to enforce field validation

    @Column(name = "phone")
    private String phone;
    // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Invalid email")//if the field contains email address consider using this annotation to enforce field validation
    @Lob
    @Column(name = "email")
    private String email;
    @Lob
    @Column(name = "adresse")
    private String adresse;
    @Column(name = "nom_client")
    private String nomClient;
    @Column(name = "type_client")
    private String typeClient;
    @JsonManagedReference
    @OneToMany(mappedBy = "clientId")
    private List<RetourMagasin> retourMagasinList;
    @OneToMany(mappedBy = "clientId")
    @JsonManagedReference
    private List<ClientAppartenir> clientAppartenirList;
    @OneToMany(fetch = FetchType.LAZY,mappedBy = "clientId")
    @JsonIgnore
    private List<Aretirer> aretirerList;
    @JoinColumn(name = "parent_id", referencedColumnName = "uid")
    @ManyToOne
    private Client parentId;

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "uid", updatable = false, nullable = false)
    private String uid;
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "clientId")
    private List<Vente> venteList;

    @PrePersist
    @PreUpdate
    protected void onDataOperation() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
    }

    public Client() {
        this.type = Tables.CLIENT.name();
    }

    public Client(String uid) {
        this.uid = uid;
        this.type = Tables.CLIENT.name();
    }

    public Client(String uid, String nomClient) {
        this.uid = uid;
        this.nomClient = nomClient;
        this.type = Tables.CLIENT.name();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    public String getTypeClient() {
        return typeClient;
    }

    public void setTypeClient(String typeClient) {
        this.typeClient = typeClient;
    }

    @JsonbTransient
    public List<Vente> getVenteList() {
        return venteList;
    }

    public void setVenteList(List<Vente> venteList) {
        this.venteList = venteList;
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
        if (!(object instanceof Client)) {
            return false;
        }
        Client other = (Client) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Client[ uid=" + uid + " ]";
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    @JsonbTransient
    public List<RetourMagasin> getRetourMagasinList() {
        return retourMagasinList;
    }

    public void setRetourMagasinList(List<RetourMagasin> retourMagasinList) {
        this.retourMagasinList = retourMagasinList;
    }

    @JsonbTransient
    public List<ClientAppartenir> getClientAppartenirList() {
        return clientAppartenirList;
    }

    public void setClientAppartenirList(List<ClientAppartenir> clientAppartenirList) {
        this.clientAppartenirList = clientAppartenirList;
    }

    @JsonbTransient
    public List<Aretirer> getAretirerList() {
        return aretirerList;
    }

    public void setAretirerList(List<Aretirer> aretirerList) {
        this.aretirerList = aretirerList;
    }

    public Client getParentId() {
        return parentId;
    }

    public void setParentId(Client parentId) {
        this.parentId = parentId;
    }

}
