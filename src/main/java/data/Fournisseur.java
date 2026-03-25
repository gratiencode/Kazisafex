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
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.util.UUID;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;
import tools.Tables;

/**
 *
 * @author eroot
 */
@Entity
@Table(name = "fournisseur")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Fournisseur.findAll", query = "SELECT DISTINCT  f FROM Fournisseur f"),
    @NamedQuery(name = "Fournisseur.findByUid", query = "SELECT DISTINCT  f FROM Fournisseur f WHERE f.uid = :uid"),
    @NamedQuery(name = "Fournisseur.findWithPhone", query = "SELECT DISTINCT  f FROM Fournisseur f WHERE f.phone = :phone"),
    @NamedQuery(name = "Fournisseur.findByIdentif", query = "SELECT DISTINCT  f FROM Fournisseur f WHERE f.identification = :ident"),
    @NamedQuery(name = "Fournisseur.findByNameAdresse", query = "SELECT DISTINCT  f FROM Fournisseur f WHERE f.phone = :nomForn AND f.adresse = :adrss"),
    @NamedQuery(name = "Fournisseur.findByName", query = "SELECT DISTINCT  f FROM Fournisseur f WHERE f.nomFourn LIKE :nomFourniss"),
    @NamedQuery(name = "Fournisseur.findByPhone", query = "SELECT DISTINCT  f FROM Fournisseur f WHERE f.phone LIKE :phone")})

public class Fournisseur extends BaseModel implements Serializable {

    @Column(name = "nom_fourn")
    private String nomFourn;
    @Column(name = "adresse")
    private String adresse;
    @Column(name = "identification")
    private String identification;
    // @Pattern(regexp="^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$", message="Invalid phone/fax format, should be as xxx-xxx-xxxx")//if the field contains phone or fax number consider using this annotation to enforce field validation
    @Column(name = "phone")
    private String phone;

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "uid", updatable = false, nullable = false)
    private String uid;
    @OneToMany(mappedBy = "fournId")
    @JsonBackReference(value = "fss-livr")
    private List<Livraison> livraisonList;
    @OneToMany(mappedBy = "fournisseurId")
    @JsonBackReference(value = "fss-cmd")
    private List<Commande> commandeList;
    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onDataOperation() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
        this.updatedAt = LocalDateTime.now();
    }

    public Fournisseur() {
        this.type = Tables.FOURNISSEUR.name();
    }

    public Fournisseur(String uid) {
        this.uid = uid;
        this.type = Tables.FOURNISSEUR.name();
    }

    public Fournisseur(String uid, String nomFourn, String adresse) {
        this.uid = uid;
        this.nomFourn = nomFourn;
        this.adresse = adresse;
        this.type = Tables.FOURNISSEUR.name();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNomFourn() {
        return nomFourn;
    }

    public void setNomFourn(String nomFourn) {
        this.nomFourn = nomFourn;
    }

    public List<Livraison> getLivraisonList() {
        return livraisonList;
    }

    public void setLivraisonList(List<Livraison> livraisonList) {
        this.livraisonList = livraisonList;
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
        if (!(object instanceof Fournisseur)) {
            return false;
        }
        Fournisseur other = (Fournisseur) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Fournisseur[ uid=" + uid + ", nomforun = " + nomFourn + " ]";
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<Commande> getCommandeList() {
        return commandeList;
    }

    public void setCommandeList(List<Commande> commandeList) {
        this.commandeList = commandeList;
    }

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

}
