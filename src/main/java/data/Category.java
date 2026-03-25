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
import jakarta.persistence.Lob;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.UUID;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;
import tools.Tables;

/**
 *
 * @author eroot
 */
@Entity
@Table(name = "category")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Category.findAll", query = "SELECT DISTINCT  c FROM Category c"),
    @NamedQuery(name = "Category.findByUid", query = "SELECT DISTINCT  c FROM Category c WHERE c.uid = :uid"),
    @NamedQuery(name = "Category.findByDescritption", query = "SELECT DISTINCT  c FROM Category c WHERE c.descritption = :descritption")})

public class Category extends BaseModel implements Serializable {

    @Lob
    @Column(name = "descritption")
    private String descritption;
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "uid", updatable = false, nullable = false)
    private String uid;
    @OneToMany(mappedBy = "categoryId")
    @JsonBackReference("cat-produitx")
    private List<Produit> produitList;
    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;
    @OneToMany(mappedBy = "categoryId")
    @JsonBackReference(value = "cat-saleAg")
    private List<SaleAgregate> saleAgregateList;

    public Category() {
        this.type = Tables.CATEGORY.name();
    }

    @PrePersist
    @PreUpdate
    protected void onDataOperation() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
        if(this.updatedAt==null){
        this.updatedAt = LocalDateTime.now();
        }
    }

    public Category(String uid) {
        this.uid = uid;
        this.type = Tables.CATEGORY.name();
    }

    public Category(String uid, String descritption) {
        this.uid = uid;
        this.descritption = descritption;
        this.type = Tables.CATEGORY.name();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    
    public List<Produit> getProduitList() {
        return produitList;
    }

    public void setProduitList(List<Produit> produitList) {
        this.produitList = produitList;
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
        if (!(object instanceof Category)) {
            return false;
        }
        Category other = (Category) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Category[ uid=" + uid + " ]";
    }

    public String getDescritption() {
        return descritption;
    }

    public void setDescritption(String descritption) {
        this.descritption = descritption;
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

    public List<SaleAgregate> getSaleAgregateList() {
        return saleAgregateList;
    }

    public void setSaleAgregateList(List<SaleAgregate> saleAgregateList) {
        this.saleAgregateList = saleAgregateList;
    }

}
