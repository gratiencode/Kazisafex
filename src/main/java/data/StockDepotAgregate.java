/*
 * Stock Depot Aggregate Table
 * Stores daily aggregated stock data per product per region
 */
package data;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 *
 * @author eroot
 */
@Entity
@Table(name = "stock_depot_agregate")
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "StockDepotAgregate.findAll", query = "SELECT s FROM StockDepotAgregate s ORDER BY s.date DESC"),
        @NamedQuery(name = "StockDepotAgregate.findByUid", query = "SELECT s FROM StockDepotAgregate s WHERE s.uid = :uid"),
        @NamedQuery(name = "StockDepotAgregate.findByDate", query = "SELECT s FROM StockDepotAgregate s WHERE s.date = :date"),
        @NamedQuery(name = "StockDepotAgregate.findByRegion", query = "SELECT s FROM StockDepotAgregate s WHERE s.region = :region"),
        @NamedQuery(name = "StockDepotAgregate.findByProduit", query = "SELECT s FROM StockDepotAgregate s WHERE s.productId.uid = :productId"),
        @NamedQuery(name = "StockDepotAgregate.findByProduitAndRegion", query = "SELECT s FROM StockDepotAgregate s WHERE s.productId.uid = :productId AND s.region = :region"),
        @NamedQuery(name = "StockDepotAgregate.findByProduitRegionDate", query = "SELECT s FROM StockDepotAgregate s WHERE s.productId.uid = :productId AND s.region = :region AND s.date = :date"),
        @NamedQuery(name = "StockDepotAgregate.findLatestByProduitAndRegion", query = "SELECT s FROM StockDepotAgregate s WHERE s.productId.uid = :productId AND s.region = :region ORDER BY s.date DESC") })

public class StockDepotAgregate extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String uid;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "date_record")
    private LocalDate date;

    private String region;

    private double quantite;

    private double coutAchat;

    private double valeurStock;

    @Column(name = "num_lot")
    private String numlot;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "date_expiration")
    private LocalDate dateExpiration;

    @JoinColumn(name = "product_id", referencedColumnName = "uid")
    @ManyToOne(optional = false)
    private Produit productId;

    @JoinColumn(name = "mesure_id", referencedColumnName = "uid")
    @ManyToOne
    private Mesure mesureId;

    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;

    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    public StockDepotAgregate() {
        this.type = "STOCKDEPOTAGREGATE";
    }

    public StockDepotAgregate(String uid) {
        this.uid = uid;
        this.type = "STOCKDEPOTAGREGATE";
    }

    @PrePersist
    @PreUpdate
    protected void onDataOperation() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
        this.updatedAt = LocalDateTime.now();
        // Calculate stock value
        this.valeurStock = this.quantite * this.coutAchat;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }

    public double getCoutAchat() {
        return coutAchat;
    }

    public void setCoutAchat(double coutAchat) {
        this.coutAchat = coutAchat;
    }

    public double getValeurStock() {
        return valeurStock;
    }

    public void setValeurStock(double valeurStock) {
        this.valeurStock = valeurStock;
    }

    public String getNumlot() {
        return numlot;
    }

    public void setNumlot(String numlot) {
        this.numlot = numlot;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public Produit getProductId() {
        return productId;
    }

    public void setProductId(Produit productId) {
        this.productId = productId;
    }

    public Mesure getMesureId() {
        return mesureId;
    }

    public void setMesureId(Mesure mesureId) {
        this.mesureId = mesureId;
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (uid != null ? uid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof StockDepotAgregate)) {
            return false;
        }
        StockDepotAgregate other = (StockDepotAgregate) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "data.StockDepotAgregate[ uid=" + uid + " ]";
    }
}
