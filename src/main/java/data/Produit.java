package data;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import jakarta.json.bind.annotation.JsonbTransient;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;  import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import java.util.UUID;

 import org.hibernate.annotations.UuidGenerator; import jakarta.xml.bind.annotation.XmlRootElement;
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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "uid")

public class Produit extends BaseModel implements Serializable {

    private String codebar;
    private byte[] image;
    private String nomProduit;
    private String marque;
    private String modele;
    private String taille;
    private String couleur;
    private String methodeInventaire;
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss"
    )
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreation;
    private static final long serialVersionUID = 1;
    @Id
   @Column(name="uid",updatable=false, nullable=false)
    private String uid;
    @ManyToOne(optional = false)
    @JsonBackReference("catsprods")
    private Category categoryId;  
    @JsonManagedReference(value = "prodsmesur")
    @OneToMany(mappedBy = "produitId")
    private List<Mesure> mesureList;
    @OneToMany( mappedBy = "productId")
    @JsonManagedReference(value = "prodsstock")
    private List<Stocker> stockerList;
    @OneToMany(mappedBy = "productId")
    @JsonManagedReference(value = "prodslvente")
    private List<LigneVente> ligneVenteList;
    @JsonManagedReference(value = "prodsdestockage")
    @OneToMany( mappedBy = "productId")
    private List<Destocker> destockerList;
    @JsonManagedReference(value = "prodsrecq")
    @OneToMany(mappedBy = "productId")
    private List<Recquisition> recquisitionList;
  

    public Produit() {
        this.type=Tables.PRODUIT.name();
    }

    public Produit(String uid) {
        this.uid = uid;
        this.type=Tables.PRODUIT.name();
    }

    @PrePersist
    @PreUpdate
    protected void onDataOperation(){
        if(this.uid==null){
            this.uid= UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
    }
    

    public Produit(String nomProduit, String marque, String modele, String taille, String couleur, String methodeInventaire, Date dateCreation, String uid, String codebar) {
        this.nomProduit = nomProduit;
        this.marque = marque;
        this.modele = modele;
        this.taille = taille;
        this.couleur = couleur;
        this.methodeInventaire = methodeInventaire;
        this.dateCreation = dateCreation;
        this.uid = uid;
        this.codebar = codebar;
        this.type=Tables.PRODUIT.name();
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

    public Date getDateCreation() {
        return this.dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Category getCategoryId() {
        return this.categoryId;
    }

    public void setCategoryId(Category categoryId) {
        this.categoryId = categoryId;
    }

    @JsonbTransient
    public List<Mesure> getMesureList() {
        return this.mesureList;
    }

    public void setMesureList(List<Mesure> mesureList) {
        this.mesureList = mesureList;
    }

    @JsonbTransient
    public List<Stocker> getStockerList() {
        return this.stockerList;
    }

    public void setStockerList(List<Stocker> stockerList) {
        this.stockerList = stockerList;
    }

    @JsonbTransient
    public List<LigneVente> getLigneVenteList() {
        return this.ligneVenteList;
    }

    public void setLigneVenteList(List<LigneVente> ligneVenteList) {
        this.ligneVenteList = ligneVenteList;
    }

    @JsonbTransient
    public List<Destocker> getDestockerList() {
        return this.destockerList;
    }

    public void setDestockerList(List<Destocker> destockerList) {
        this.destockerList = destockerList;
    }

    @JsonbTransient
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
        return "entities.Produit[ uid =" + this.uid + " ]";
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

}
