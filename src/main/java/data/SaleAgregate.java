/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package data;
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
import java.io.Serializable;
import java.time.LocalDateTime;

import java.util.UUID;

/**
 *
 * @author endeleya
 */
@Entity
@Table(name = "sale_agregate")
@NamedQueries({
    @NamedQuery(name = "SaleAgregate.findAll", query = "SELECT s FROM SaleAgregate s"),
    @NamedQuery(name = "SaleAgregate.findByUid", query = "SELECT s FROM SaleAgregate s WHERE s.uid = :uid"),
    @NamedQuery(name = "SaleAgregate.findByQuantite", query = "SELECT s FROM SaleAgregate s WHERE s.quantite = :quantite"),
    @NamedQuery(name = "SaleAgregate.findByCoutAchatTotal", query = "SELECT s FROM SaleAgregate s WHERE s.coutAchatTotal = :coutAchatTotal"),
    @NamedQuery(name = "SaleAgregate.findByTotalSaleUsd", query = "SELECT s FROM SaleAgregate s WHERE s.totalSaleUsd = :totalSaleUsd"),
    @NamedQuery(name = "SaleAgregate.findByRegion", query = "SELECT s FROM SaleAgregate s WHERE s.region = :region"),
    @NamedQuery(name = "SaleAgregate.findByDate", query = "SELECT s FROM SaleAgregate s WHERE s.date = :date")})

public class SaleAgregate implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "uid")
    private String uid;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "quantite")
    private Double quantite;
    @Column(name = "cout_achat_total")
    private Double coutAchatTotal;
    @Column(name = "total_sale_usd")
    private Double totalSaleUsd;
    @Size(max = 255)
    @Column(name = "region")
    private String region;
    @Column(name = "date",columnDefinition = "DATETIME")
    private LocalDateTime date;
    @JoinColumn(name = "category_id", referencedColumnName = "uid")
    @ManyToOne
    private Category categoryId;
    @JoinColumn(name = "mesure_id", referencedColumnName = "uid")
    @ManyToOne
    private Mesure mesureId;
    @JoinColumn(name = "product_id", referencedColumnName = "uid")
    @ManyToOne
    private Produit productId;

    public SaleAgregate() {
    }
    
    @PrePersist
    protected void prepersist(){
        this.uid=UUID.randomUUID().toString().toLowerCase().replace("-", "");
    }

    public SaleAgregate(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Double getQuantite() {
        return quantite;
    }

    public void setQuantite(Double quantite) {
        this.quantite = quantite;
    }

    public Double getCoutAchatTotal() {
        return coutAchatTotal;
    }

    public void setCoutAchatTotal(Double coutAchatTotal) {
        this.coutAchatTotal = coutAchatTotal;
    }

    public Double getTotalSaleUsd() {
        return totalSaleUsd;
    }

    public void setTotalSaleUsd(Double totalSaleUsd) {
        this.totalSaleUsd = totalSaleUsd;
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

    public Category getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Category categoryId) {
        this.categoryId = categoryId;
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
        if (!(object instanceof SaleAgregate)) {
            return false;
        }
        SaleAgregate other = (SaleAgregate) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ejb.entities.SaleAgregate[ uid=" + uid + " ]";
    }
    
}
