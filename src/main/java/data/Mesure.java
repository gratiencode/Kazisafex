/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import jakarta.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;
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
//
public class Mesure extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    private String uid;
    private Double quantContenu;
    private String description;
    @JsonBackReference(value = "mez-pv")
    @OneToMany(mappedBy = "mesureId")
    private List<PrixDeVente> prixDeVenteList;
    @JoinColumn(name = "produit_id", referencedColumnName = "uid")
    @ManyToOne(optional = false)
    private Produit produitId;
    @OneToMany(mappedBy = "mesureId")
    @JsonBackReference(value = "mez-stock")
    private List<Stocker> stockerList;
    @OneToMany(mappedBy = "mesureId")
    @JsonBackReference(value = "mez-period")
    private List<Periode> periodeList;
    @OneToMany(mappedBy = "mesureId")
    @JsonBackReference(value = "mez-stokAg")
    private List<StockAgregate> stockAgregateList;
    @OneToMany(mappedBy = "mesureId")
    @JsonBackReference(value = "mez-saleAg")
    private List<SaleAgregate> saleAgregateList;
    @OneToMany(mappedBy = "mesureId")
    @JsonBackReference(value = "mez-compter")
    private List<Compter> compterList;
    @OneToMany(mappedBy = "mesureId")
    @JsonBackReference(value = "mez-ligv")
    private List<LigneVente> ligneVenteList;
    @OneToMany(mappedBy = "mesureId")
    @JsonBackReference(value = "mez-destok")
    private List<Destocker> destockerList;
    @OneToMany(mappedBy = "mesureId")
    @JsonBackReference(value = "mez-req")
    private List<Recquisition> recquisitionList;
    @OneToMany(mappedBy = "mesureId")
    @JsonBackReference(value = "mez-cmdl")
    private List<CommandeLister> commandeListerList;
    @OneToMany(mappedBy = "mesureId")
    @JsonBackReference(value = "mez-entrpoz")
    private List<Entreposer> entreposerList;
    @OneToMany(mappedBy = "mesureId")
    @JsonBackReference(value = "mez-produkt")
    private List<Production> productionList;
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

    public Mesure() {
        this.type = Tables.MESURE.name();
    }

    public Mesure(String uid) {
        this.uid = uid;
        this.type = Tables.MESURE.name();
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

    public List<Stocker> getStockerList() {
        return stockerList;
    }

    public void setStockerList(List<Stocker> stockerList) {
        this.stockerList = stockerList;
    }

    public List<LigneVente> getLigneVenteList() {
        return ligneVenteList;
    }

    public void setLigneVenteList(List<LigneVente> ligneVenteList) {
        this.ligneVenteList = ligneVenteList;
    }

    public List<Destocker> getDestockerList() {
        return destockerList;
    }

    public void setDestockerList(List<Destocker> destockerList) {
        this.destockerList = destockerList;
    }

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
        return "entities.Mesure[ uid=" + uid + ", description =" + this.description + ", quantcontenu =" + this.quantContenu + "]";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public List<CommandeLister> getCommandeListerList() {
        return commandeListerList;
    }

    public void setCommandeListerList(List<CommandeLister> commandeListerList) {
        this.commandeListerList = commandeListerList;
    }

    public List<Entreposer> getEntreposerList() {
        return entreposerList;
    }

    public void setEntreposerList(List<Entreposer> entreposerList) {
        this.entreposerList = entreposerList;
    }

    public List<Production> getProductionList() {
        return productionList;
    }

    public void setProductionList(List<Production> productionList) {
        this.productionList = productionList;
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
