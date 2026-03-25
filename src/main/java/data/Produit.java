package data;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Basic;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
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

@Entity
@Table(name = "produit")
@XmlRootElement
@NamedQueries(value = {
    @NamedQuery(name = "Produit.findAll", query = "SELECT DISTINCT  p FROM Produit p ORDER BY p.dateCreation DESC"),
    @NamedQuery(name = "Produit.findByUid", query = "SELECT DISTINCT  p FROM Produit p WHERE p.uid = :uid"),
    @NamedQuery(name = "Produit.findByCodeBar", query = "SELECT DISTINCT  p FROM Produit p WHERE p.codebar = :codeBar"),
    @NamedQuery(name = "Produit.findByModele", query = "SELECT DISTINCT  p FROM Produit p WHERE p.modele = :modele"),
    @NamedQuery(name = "Produit.findByTaille", query = "SELECT DISTINCT  p FROM Produit p WHERE p.taille = :taille"),
    @NamedQuery(name = "Produit.findByCouleur", query = "SELECT DISTINCT  p FROM Produit p WHERE p.couleur = :couleur"),
    @NamedQuery(name = "Produit.findByMethodeInventaire", query = "SELECT DISTINCT  p FROM Produit p WHERE p.methodeInventaire = :methodeInventaire"),
    @NamedQuery(name = "Produit.findByDateCreation", query = "SELECT DISTINCT  p FROM Produit p WHERE p.dateCreation = :dateCreation")})
public class Produit extends BaseModel implements Serializable {

    private String codebar;
    
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "image", columnDefinition = "BLOB")
    @JsonIgnore
    private byte[] image;
    private String nomProduit;
    private String marque;
    private String modele;
    private String taille;
    private String couleur;
    private String methodeInventaire;
   
    @Column(name = "dateCreation", columnDefinition = "DATETIME")
    private LocalDateTime dateCreation;
    private static final long serialVersionUID = 1;
    @Id
    @Column(name = "uid", updatable = false, nullable = false)
    private String uid;
    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;
    @ManyToOne(optional = false)
    private Category categoryId;
    @OneToMany(mappedBy = "productId")
    @JsonBackReference(value = "prod-stockAg")
    private List<StockAgregate> stockAgregateList;
    @OneToMany(mappedBy = "productId")
    @JsonBackReference(value = "prod-saleAg")
    private List<SaleAgregate> saleAgregateList;
    @OneToMany(mappedBy = "produitId")
    @JsonBackReference("prod-mez")
    private List<Mesure> mesureList;
    @JsonBackReference(value = "prod-compter")
    @OneToMany(mappedBy = "productId")

    private List<Compter> compterList;
    @JsonBackReference(value = "prod-period")
    @OneToMany(mappedBy = "productId")

    private List<Periode> periodeList;
    @JsonBackReference(value = "prod-stock")
    @OneToMany(mappedBy = "productId")

    private List<Stocker> stockerList;
    @OneToMany(mappedBy = "productId")
    @JsonBackReference(value = "prod-ligv")
    private List<LigneVente> ligneVenteList;
    @OneToMany(mappedBy = "productId")
    @JsonBackReference(value = "prod-destok")

    private List<Destocker> destockerList;
    @OneToMany(mappedBy = "productId")
    @JsonBackReference(value = "prod-req")

    private List<Recquisition> recquisitionList;
    @JsonBackReference(value = "prod-cmdl")
    @OneToMany(mappedBy = "produitId")

    private List<CommandeLister> commandeListerList;
    @JsonBackReference(value = "prod-produkt")
    @OneToMany(mappedBy = "produitId")

    private List<Production> productionList;

    public List<CommandeLister> getCommandeListerList() {
        return commandeListerList;
    }

    public void setCommandeListerList(List<CommandeLister> commandeListerList) {
        this.commandeListerList = commandeListerList;
    }

    public Produit() {
        this.type = Tables.PRODUIT.name();
    }

    public Produit(String uid) {
        this.uid = uid;
        this.type = Tables.PRODUIT.name();
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    @PrePersist
    @PreUpdate
    protected void onDataOperation() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
        this.updatedAt = LocalDateTime.now();
    }

    public Produit(String nomProduit, String marque, String modele, String taille, String couleur, String methodeInventaire, LocalDateTime dateCreation, String uid, String codebar) {
        this.nomProduit = nomProduit;
        this.marque = marque;
        this.modele = modele;
        this.taille = taille;
        this.couleur = couleur;
        this.methodeInventaire = methodeInventaire;
        this.dateCreation = dateCreation;
        this.uid = uid;
        this.codebar = codebar;
        this.type = Tables.PRODUIT.name();
    }

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNomProduit() {
        return this.nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    public String getMethodeInventaire() {
        return this.methodeInventaire;
    }

    public void setMethodeInventaire(String methodeInventaire) {
        this.methodeInventaire = methodeInventaire;
    }

    public LocalDateTime getDateCreation() {
        return this.dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Category getCategoryId() {
        return this.categoryId;
    }

    public void setCategoryId(Category categoryId) {
        this.categoryId = categoryId;
    }

    public List<Mesure> getMesureList() {
        return this.mesureList;
    }

    public void setMesureList(List<Mesure> mesureList) {
        this.mesureList = mesureList;
    }

    public List<Stocker> getStockerList() {
        return this.stockerList;
    }

    public void setStockerList(List<Stocker> stockerList) {
        this.stockerList = stockerList;
    }

    public List<LigneVente> getLigneVenteList() {
        return this.ligneVenteList;
    }

    public void setLigneVenteList(List<LigneVente> ligneVenteList) {
        this.ligneVenteList = ligneVenteList;
    }

    public List<Destocker> getDestockerList() {
        return this.destockerList;
    }

    public void setDestockerList(List<Destocker> destockerList) {
        this.destockerList = destockerList;
    }

    public List<Recquisition> getRecquisitionList() {
        return this.recquisitionList;
    }

    public void setRecquisitionList(List<Recquisition> recquisitionList) {
        this.recquisitionList = recquisitionList;
    }

    public int hashCode() {
        int hash = 0;
        return hash += this.uid != null ? this.uid.hashCode() : 0;
    }

    public boolean equals(Object object) {
        if (!(object instanceof Produit)) {
            return false;
        }
        Produit other = (Produit) object;
        if (this.uid == null && other.uid != null || this.uid != null && !this.uid.equals(other.uid)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "entities.Produit[ uid =" + this.uid + " nomproduit =" + this.nomProduit + "]";
    }

    public String getCodebar() {
        return codebar;
    }

    public void setCodebar(String codebar) {
        this.codebar = codebar;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public String getTaille() {
        return taille;
    }

    public void setTaille(String taille) {
        this.taille = taille;
    }

    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    public List<Periode> getPeriodeList() {
        return periodeList;
    }

    public void setPeriodeList(List<Periode> periodeList) {
        this.periodeList = periodeList;
    }

    public List<Compter> getCompterList() {
        return compterList;
    }

    public void setCompterList(List<Compter> compterList) {
        this.compterList = compterList;
    }

    public List<Production> getProductionList() {
        return productionList;
    }

    public void setProductionList(List<Production> productionList) {
        this.productionList = productionList;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<StockAgregate> getStockAgregateList() {
        return stockAgregateList;
    }

    public void setStockAgregateList(List<StockAgregate> stockAgregateList) {
        this.stockAgregateList = stockAgregateList;
    }

    public List<SaleAgregate> getSaleAgregateList() {
        return saleAgregateList;
    }

    public void setSaleAgregateList(List<SaleAgregate> saleAgregateList) {
        this.saleAgregateList = saleAgregateList;
    }

}
