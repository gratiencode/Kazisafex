/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package data;
import com.fasterxml.jackson.annotation.JsonBackReference;
 import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 *
 * @author endeleya
 */
@Entity
@Table(name = "stock_agregate")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "StockAgregate.findAll", query = "SELECT s FROM StockAgregate s"),
    @NamedQuery(name = "StockAgregate.findByUid", query = "SELECT s FROM StockAgregate s WHERE s.uid = :uid"),
    @NamedQuery(name = "StockAgregate.findByInitialQuantity", query = "SELECT s FROM StockAgregate s WHERE s.initialQuantity = :initialQuantity"),
    @NamedQuery(name = "StockAgregate.findByEntrees", query = "SELECT s FROM StockAgregate s WHERE s.entrees = :entrees"),
    @NamedQuery(name = "StockAgregate.findBySorties", query = "SELECT s FROM StockAgregate s WHERE s.sorties = :sorties"),
    @NamedQuery(name = "StockAgregate.findByFinalQuantity", query = "SELECT s FROM StockAgregate s WHERE s.finalQuantity = :finalQuantity"),
    @NamedQuery(name = "StockAgregate.findByExpiree", query = "SELECT s FROM StockAgregate s WHERE s.expiree = :expiree"),
    @NamedQuery(name = "StockAgregate.findByCoutAchat", query = "SELECT s FROM StockAgregate s WHERE s.coutAchat = :coutAchat"),
    @NamedQuery(name = "StockAgregate.findByRegion", query = "SELECT s FROM StockAgregate s WHERE s.region = :region"),
    @NamedQuery(name = "StockAgregate.findByDate", query = "SELECT s FROM StockAgregate s WHERE s.date = :date")})
public class StockAgregate implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "uid")
    private String uid;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "initial_quantity")
    private Double initialQuantity;
    @Column(name = "entrees")
    private Double entrees;
    @Column(name = "sorties")
    private Double sorties;
    @Column(name = "final_quantity")
    private Double finalQuantity;
    @Column(name = "expiree")
    private Double expiree;
    @Column(name = "cout_achat")
    private Double coutAchat;
    @Size(max = 100)
    @Column(name = "region")
    private String region;
    @Size(max = 200)
    @Column(name = "context")
    private String context;
    @Column(name = "date", columnDefinition = "DATETIME")
    private LocalDateTime date;
    @JoinColumn(name = "mesure_id", referencedColumnName = "uid")
    @ManyToOne
    private Mesure mesureId;
    @JoinColumn(name = "product_id", referencedColumnName = "uid")
    @ManyToOne
    private Produit productId;

    public StockAgregate() {
    }
    @PrePersist
    private void prepersist(){
        this.uid=UUID.randomUUID().toString().toLowerCase().replace("-", "");
    }

    public StockAgregate(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Double getInitialQuantity() {
        return initialQuantity;
    }

    public void setInitialQuantity(Double initialQuantity) {
        this.initialQuantity = initialQuantity;
    }

    public Double getEntrees() {
        return entrees;
    }

    public void setEntrees(Double entrees) {
        this.entrees = entrees;
    }

    public Double getSorties() {
        return sorties;
    }

    public void setSorties(Double sorties) {
        this.sorties = sorties;
    }

    public Double getFinalQuantity() {
        return finalQuantity;
    }

    public void setFinalQuantity(Double finalQuantity) {
        this.finalQuantity = finalQuantity;
    }

    public Double getExpiree() {
        return expiree;
    }

    public void setExpiree(Double expiree) {
        this.expiree = expiree;
    }

    public Double getCoutAchat() {
        return coutAchat;
    }

    public void setCoutAchat(Double coutAchat) {
        this.coutAchat = coutAchat;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Mesure getMesureId() {
        return mesureId;
    }

    public void setMesureId(Mesure mesureId) {
        this.mesureId = mesureId;
    }

    public Produit getProductId() {
        return productId;
    }

    public void setProductId(Produit productId) {
        this.productId = productId;
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
        if (!(object instanceof StockAgregate)) {
            return false;
        }
        StockAgregate other = (StockAgregate) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ejb.entities.StockAgregate[ uid=" + uid + " ]";
    }

    public String getContext() {
        return context;
    }

    public void setContext(String clotureType) {
        this.context = clotureType;
    }
    
}
