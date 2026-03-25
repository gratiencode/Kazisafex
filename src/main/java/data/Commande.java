/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package data;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author endeleya
 */
@Entity
@Table(name = "commande")
@NamedQueries({
    @NamedQuery(name = "Commande.findAll", query = "SELECT c FROM Commande c"),
    @NamedQuery(name = "Commande.findByUid", query = "SELECT c FROM Commande c WHERE c.uid = :uid"),
    @NamedQuery(name = "Commande.findByTypeCommande", query = "SELECT c FROM Commande c WHERE c.typeCommande = :typeCommande"),
    @NamedQuery(name = "Commande.findByNumero", query = "SELECT c FROM Commande c WHERE c.numero = :numero"),
    @NamedQuery(name = "Commande.findByAddresseLivraison", query = "SELECT c FROM Commande c WHERE c.addresseLivraison = :addresseLivraison"),
    @NamedQuery(name = "Commande.findByPersonneReference", query = "SELECT c FROM Commande c WHERE c.personneReference = :personneReference"),
    @NamedQuery(name = "Commande.findByPhonePersonneReference", query = "SELECT c FROM Commande c WHERE c.phonePersonneReference = :phonePersonneReference"),
    @NamedQuery(name = "Commande.findByDate", query = "SELECT c FROM Commande c WHERE c.date = :date"),
    @NamedQuery(name = "Commande.findByRegion", query = "SELECT c FROM Commande c WHERE c.region = :region")})

public class Commande implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "uid")
    private String uid;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "type_commande")
    private String typeCommande;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "numero")
    private String numero;
    @Size(max = 1024)
    @Column(name = "addresse_livraison")
    private String addresseLivraison;
    @Size(max = 100)
    @Column(name = "personne_reference")
    private String personneReference;
    @Size(max = 100)
    @Column(name = "phone_personne_reference")
    private String phonePersonneReference;
    @Column(name = "date_")
    private LocalDate date;
    @Size(max = 100)
    @Column(name = "region")
    private String region;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "commandeId")
    private List<CommandeLister> commandeListerList;
    @JoinColumn(name = "client_id", referencedColumnName = "uid")
    @ManyToOne
    private Client clientId;
    @JoinColumn(name = "fournisseur_id", referencedColumnName = "uid")
    @ManyToOne
    private Fournisseur fournisseurId;
    @OneToMany(mappedBy = "commandeId")
    private List<Satisfaire> satisfaireList;
    @Column(name = "deleted_at", columnDefinition = "DATETIME")
     private LocalDateTime deletedAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    public Commande() {
    }

    public Commande(String uid) {
        this.uid = uid;
    }

    public Commande(String uid, String typeCommande, String numero) {
        this.uid = uid;
        this.typeCommande = typeCommande;
        this.numero = numero;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTypeCommande() {
        return typeCommande;
    }

    public void setTypeCommande(String typeCommande) {
        this.typeCommande = typeCommande;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getAddresseLivraison() {
        return addresseLivraison;
    }

    public void setAddresseLivraison(String addresseLivraison) {
        this.addresseLivraison = addresseLivraison;
    }

    public String getPersonneReference() {
        return personneReference;
    }

    public void setPersonneReference(String personneReference) {
        this.personneReference = personneReference;
    }

    public String getPhonePersonneReference() {
        return phonePersonneReference;
    }

    public void setPhonePersonneReference(String phonePersonneReference) {
        this.phonePersonneReference = phonePersonneReference;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public List<CommandeLister> getCommandeListerList() {
        return commandeListerList;
    }

    public void setCommandeListerList(List<CommandeLister> commandeListerList) {
        this.commandeListerList = commandeListerList;
    }

    public Client getClientId() {
        return clientId;
    }

    public void setClientId(Client clientId) {
        this.clientId = clientId;
    }

    public Fournisseur getFournisseurId() {
        return fournisseurId;
    }

    public void setFournisseurId(Fournisseur fournisseurId) {
        this.fournisseurId = fournisseurId;
    }

    public List<Satisfaire> getSatisfaireList() {
        return satisfaireList;
    }

    public void setSatisfaireList(List<Satisfaire> satisfaireList) {
        this.satisfaireList = satisfaireList;
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
        if (!(object instanceof Commande)) {
            return false;
        }
        Commande other = (Commande) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ejb.entities.Commande[ uid=" + uid + " ]";
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
