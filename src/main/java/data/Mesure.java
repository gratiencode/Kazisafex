/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.util.UUID;

 
import org.hibernate.annotations.UuidGenerator; import jakarta.xml.bind.annotation.XmlRootElement;
import tools.Tables;

/**
 *
 * @author eroot
 */
@Entity
@Table(name = "mesure")
 @XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Mesure.findAll", query = "SELECT DISTINCT  m FROM Mesure m"),
    @NamedQuery(name = "Mesure.findByUid", query = "SELECT DISTINCT  m FROM Mesure m WHERE m.uid = :uid"),
    @NamedQuery(name = "Mesure.findByDescription", query = "SELECT DISTINCT  m FROM Mesure m WHERE m.description = :description"),
    @NamedQuery(name = "Mesure.findByQuantContenu", query = "SELECT DISTINCT  m FROM Mesure m WHERE m.quantContenu = :quantContenu")})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "uid")
public class Mesure extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
   
    private String uid;
    private Double quantContenu;
    private String description;
    @OneToMany(mappedBy = "mesureId")
    @JsonManagedReference
    private List<PrixDeVente> prixDeVenteList;
    @JoinColumn(name = "produit_id", referencedColumnName = "uid")
    @ManyToOne
    @JsonBackReference
    private Produit produitId;
    @JsonManagedReference
    @OneToMany(mappedBy = "mesureId")
    private List<Stocker> stockerList;
    @OneToMany(mappedBy = "mesureId")
    @JsonManagedReference
    private List<LigneVente> ligneVenteList;
    @OneToMany(mappedBy = "mesureId")
    @JsonManagedReference
    private List<Destocker> destockerList;
    @JsonManagedReference
    @OneToMany(mappedBy = "mesureId")
    private List<Recquisition> recquisitionList;
    

    @PrePersist
    @PreUpdate
    protected void onDataOperation(){
        if(this.uid==null){
            this.uid= UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
    }

    public Mesure() {
         this.type=Tables.MESURE.name();
    }

    public Mesure(String uid) {
        this.uid = uid;
         this.type=Tables.MESURE.name();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Double getQuantContenu() {
        return quantContenu;
    }

    public void setQuantContenu(Double quantContenu) {
        this.quantContenu = quantContenu;
    }

    @JsonbTransient
    public List<PrixDeVente> getPrixDeVenteList() {
        return prixDeVenteList;
    }

    public void setPrixDeVenteList(List<PrixDeVente> prixDeVenteList) {
        this.prixDeVenteList = prixDeVenteList;
    }

    public Produit getProduitId() {
        return produitId;
    }

    public void setProduitId(Produit produitId) {
        this.produitId = produitId;
    }

    @JsonbTransient
    public List<Stocker> getStockerList() {
        return stockerList;
    }

    public void setStockerList(List<Stocker> stockerList) {
        this.stockerList = stockerList;
    }

    @JsonbTransient
    public List<LigneVente> getLigneVenteList() {
        return ligneVenteList;
    }

    public void setLigneVenteList(List<LigneVente> ligneVenteList) {
        this.ligneVenteList = ligneVenteList;
    }

    @JsonbTransient
    public List<Destocker> getDestockerList() {
        return destockerList;
    }

    public void setDestockerList(List<Destocker> destockerList) {
        this.destockerList = destockerList;
    }

    @JsonbTransient
    public List<Recquisition> getRecquisitionList() {
        return recquisitionList;
    }

    public void setRecquisitionList(List<Recquisition> recquisitionList) {
        this.recquisitionList = recquisitionList;
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
        if (!(object instanceof Mesure)) {
            return false;
        }
        Mesure other = (Mesure) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Mesure[ uid=" + uid + " ]";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
