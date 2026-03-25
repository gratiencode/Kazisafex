/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data; import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;

import jakarta.persistence.Column;
  import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.UUID;

 import jakarta.xml.bind.annotation.XmlRootElement;
import java.time.LocalDate;
import java.time.LocalDateTime;



/**
 *
 * @author eroot
 */
@Entity
@Table(name = "entreprise")
 @XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Entreprise.findAll", query = "SELECT DISTINCT  e FROM Entreprise e")
    , @NamedQuery(name = "Entreprise.findByUid", query = "SELECT DISTINCT  e FROM Entreprise e WHERE e.uid = :uid")
    , @NamedQuery(name = "Entreprise.findByIdentification", query = "SELECT DISTINCT  e FROM Entreprise e WHERE e.identification = :rccm")
    , @NamedQuery(name = "Entreprise.findByWebsite", query = "SELECT DISTINCT  e FROM Entreprise e WHERE e.website = :website")
    , @NamedQuery(name = "Entreprise.findByDateCreation", query = "SELECT DISTINCT  e FROM Entreprise e WHERE e.dateCreation = :dateCreation")
    , @NamedQuery(name = "Entreprise.findByLatitude", query = "SELECT DISTINCT  e FROM Entreprise e WHERE e.latitude = :latitude")
    , @NamedQuery(name = "Entreprise.findByLongitude", query = "SELECT DISTINCT  e FROM Entreprise e WHERE e.longitude = :longitude")
    , @NamedQuery(name = "Entreprise.findByDeletedAt", query = "SELECT DISTINCT  e FROM Entreprise e WHERE e.deletedAt = :deletedAt")})
public class Entreprise extends BaseModel implements Serializable {

    @Column(name = "logo")
    private byte[] logo;
    private String nomEntreprise;
    @Column(name = "identification")
    private String identification;
    private String typeIdentification;
    @Column(name = "adresse")
    private String adresse;
    @Column(name = "website")
    private String website;
  // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Invalid email")//if the field contains email address consider using this annotation to enforce field validation
    @Column(name = "email")
    private String email;
    @Column(name = "category")
    private String category;
    // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Invalid email")//if the field contains email address consider using this annotation to enforce field validation
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss"
    )
    @Column(name = "dateCreation",columnDefinition = "DATETIME")
    private LocalDateTime dateCreation;
    private String idNat;
    private String numeroImpot;
    @Column(name = "phones")
    private String phones;
//    @OneToMany(mappedBy = "entrepriseId")
//    (value = "role-list")
//    private List<Role> roleList;
//    @OneToMany(mappedBy = "entrepriseId")
//    (value = "client-list")
//    private List<Client> clientList;
//    @OneToMany(mappedBy = "entrepriseId")
//    (value = "fournisseur-list")
//    private List<Fournisseur> fournisseurList;
//    @OneToMany(mappedBy = "entrepriseId")
//    (value = "mesure-list")
//    private List<Mesure> mesureList;
    private static final long serialVersionUID = 1L;
    @Id
    
    @Column(name = "uid", updatable = false, nullable = false)
    private String uid;
    @Column(name = "latitude")
    private Double latitude;
    @Column(name = "longitude")
    private Double longitude;
    @Column(name = "deletedAt",columnDefinition = "DATE")
    private LocalDate deletedAt;
//    @OneToMany(mappedBy = "entrepriseId")
//    (value = "produit-list")
//    private List<Produit> produitList;
//    @OneToMany(mappedBy = "entrepriseId")
//    (value = "stock-list")
//    private List<Stocker> stockerList;
//    (value = "eng-list")
//    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entrepriseId")
//    private List<Engager> engagerList;
//    (value = "traisorerie-list")
//    @OneToMany(mappedBy = "entrepriseId")
//    private List<Traisorerie> traisorerieList;
//    (value = "destocker-list")
//    @OneToMany(mappedBy = "entrepriseId")
//    private List<Destocker> destockerList;
//    (value = "livraison-list")
//    @OneToMany(mappedBy = "entrepriseId")
//    private List<Livraison> livraisonList;
//    (value = "requisition-list")
//    @OneToMany(mappedBy = "entrepriseId")
//    private List<Recquisition> recquisitionList;
//    (value = "category-list")
//    @OneToMany(mappedBy = "entrepriseId")
//    private List<Category> categoryList;
//    (value = "operation-list")
//    @OneToMany(mappedBy = "entrepriseId")
//    private List<Operation> operationList;
//    (value = "vente-list")
//    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entrepriseId")
//    private List<Vente> venteList;

    @PrePersist
    @PreUpdate
    protected void onDataOperation(){
        if(this.uid==null){
            this.uid = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
    }

    public Entreprise() {
    }

    public Entreprise(String uid) {
        this.uid = uid;
    }

    public Entreprise(String uid, String nomEntreprise, String identification, String typeIdentification,
            String adresse, String category, LocalDateTime dateCreation) {
        this.uid = uid;
        this.nomEntreprise = nomEntreprise;
        this.identification = identification;
        this.typeIdentification = typeIdentification;
        this.adresse = adresse;
        this.category = category;
        this.dateCreation = dateCreation;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNomEntreprise() {
        return nomEntreprise;
    }

    public void setNomEntreprise(String nomEntreprise) {
        this.nomEntreprise = nomEntreprise;
    }

    public String getTypeIdentification() {
        return typeIdentification;
    }

    public void setTypeIdentification(String typeIdentification) {
        this.typeIdentification = typeIdentification;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public LocalDate getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDate deletedAt) {
        this.deletedAt = deletedAt;
    }

//    
//    public List<Produit> getProduitList() {
//        return produitList;
//    }
//
//    public void setProduitList(List<Produit> produitList) {
//        this.produitList = produitList;
//    }
//
//    
//    public List<Stocker> getStockerList() {
//        return stockerList;
//    }
//
//    public void setStockerList(List<Stocker> stockerList) {
//        this.stockerList = stockerList;
//    }

//    
//    public List<Engager> getEngagerList() {
//        return engagerList;
//    }
//
//    public void setEngagerList(List<Engager> engagerList) {
//        this.engagerList = engagerList;
//    }
//
//    
//    public List<Traisorerie> getTraisorerieList() {
//        return traisorerieList;
//    }
//
//    public void setTraisorerieList(List<Traisorerie> traisorerieList) {
//        this.traisorerieList = traisorerieList;
//    }

//    
//    public List<Destocker> getDestockerList() {
//        return destockerList;
//    }
//
//    public void setDestockerList(List<Destocker> destockerList) {
//        this.destockerList = destockerList;
//    }
//
//    
//    public List<Livraison> getLivraisonList() {
//        return livraisonList;
//    }
//
//    public void setLivraisonList(List<Livraison> livraisonList) {
//        this.livraisonList = livraisonList;
//    }

//    
//    public List<Recquisition> getRecquisitionList() {
//        return recquisitionList;
//    }
//
//    public void setRecquisitionList(List<Recquisition> recquisitionList) {
//        this.recquisitionList = recquisitionList;
//    }
//
//    
//    public List<Category> getCategoryList() {
//        return categoryList;
//    }

//    public void setCategoryList(List<Category> categoryList) {
//        this.categoryList = categoryList;
//    }
//
//    
//    public List<Operation> getOperationList() {
//        return operationList;
//    }
//
//    public void setOperationList(List<Operation> operationList) {
//        this.operationList = operationList;
//    }
//
//    
//    public List<Vente> getVenteList() {
//        return venteList;
//    }
//
//    public void setVenteList(List<Vente> venteList) {
//        this.venteList = venteList;
//    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (uid != null ? uid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Entreprise)) {
            return false;
        }
        Entreprise other = (Entreprise) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Entreprise[ uid=" + uid + " ]";
    }

    public String getIdNat() {
        return idNat;
    }

    public void setIdNat(String idNat) {
        this.idNat = idNat;
    }

    public String getNumeroImpot() {
        return numeroImpot;
    }

    public void setNumeroImpot(String numeroImpot) {
        this.numeroImpot = numeroImpot;
    }

//    
//    public List<Mesure> getMesureList() {
//        return mesureList;
//    }
//
//    public void setMesureList(List<Mesure> mesureList) {
//        this.mesureList = mesureList;
//    }
//
//    
//    public List<Client> getClientList() {
//        return clientList;
//    }

//    public void setClientList(List<Client> clientList) {
//        this.clientList = clientList;
//    }
//
//    
//    public List<Fournisseur> getFournisseurList() {
//        return fournisseurList;
//    }
//
//    public void setFournisseurList(List<Fournisseur> fournisseurList) {
//        this.fournisseurList = fournisseurList;
//    }
//
//    
//    public List<Role> getRoleList() {
//        return roleList;
//    }
//
//    public void setRoleList(List<Role> roleList) {
//        this.roleList = roleList;
//    }

    public byte[] getLogo() {
        return logo;
    }

    public void setLogo(byte[] logo) {
        this.logo = logo;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPhones() {
        return phones;
    }

    public void setPhones(String phones) {
        this.phones = phones;
    }
}
