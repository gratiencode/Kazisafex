/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package data;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author endeleya
 */
@Entity
@Table(name = "periode")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Periode.findAll", query = "SELECT DISTINCT  c FROM Periode c ORDER BY c.dateFin DESC"),
    @NamedQuery(name = "Periode.findByUid", query = "SELECT DISTINCT  c FROM Periode c WHERE c.uid = :uid"),
    @NamedQuery(name = "Periode.findByComment", query = "SELECT DISTINCT  c FROM Periode c WHERE c.comment = :comment"),
    @NamedQuery(name = "Periode.findByMouvement", query = "SELECT DISTINCT  c FROM Periode c WHERE c.mouvement = :mouvement")})

public class Periode extends BaseModel implements Serializable {

    @Id
    @Column(name = "uid")
    private String uid;
    @JoinColumn(name = "product_id", referencedColumnName = "uid")
    @ManyToOne(optional = false)
    private Produit productId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "date_debut")
    private LocalDate dateDebut;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "date_fin")
    private LocalDate dateFin;
    
    @Column(name = "now")
    private LocalDateTime now;
    @Column(name = "comment")
    private String comment;
    @Column(name = "region")
    private String region;
    @Column(name = "mouvement")
    private String mouvement;
    @Column(name = "stock_initial")
    private double stockInitial;
    @Column(name = "stock_final", nullable = true)
    private double stockFinal;
    @Column(name = "ecart", nullable = true)
    private double ecart;
    @JoinColumn(name = "mesure_id", referencedColumnName = "uid")
    @ManyToOne
    private Mesure mesureId;
    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;
    @JoinColumn(name = "previous_period", referencedColumnName = "uid", nullable = true)
    @ManyToOne
    private Periode previousPeriod;

    private List<Periode> nextPeriode;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    public Periode() {
    }

    public Periode(String uid) {
        this.uid = uid;
    }

    @PrePersist
    @PreUpdate
    protected void onDataOperation() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
        this.updatedAt = LocalDateTime.now();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Produit getProductId() {
        return productId;
    }

    public void setProductId(Produit productId) {
        this.productId = productId;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getMouvement() {
        return mouvement;
    }

    public void setMouvement(String mouvement) {
        this.mouvement = mouvement;
    }

    public double getStockInitial() {
        return stockInitial;
    }

    public void setStockInitial(double stockInitial) {
        this.stockInitial = stockInitial;
    }

    public double getStockFinal() {
        return stockFinal;
    }

    public void setStockFinal(double stockFinal) {
        this.stockFinal = stockFinal;
    }

    public double getEcart() {
        return ecart;
    }

    public void setEcart(double ecart) {
        this.ecart = ecart;
    }

    public Mesure getMesureId() {
        return mesureId;
    }

    public void setMesureId(Mesure mesureId) {
        this.mesureId = mesureId;
    }

    public Periode getPreviousPeriod() {
        return previousPeriod;
    }

    public void setPreviousPeriod(Periode previousPeriod) {
        this.previousPeriod = previousPeriod;
    }

    public List<Periode> getNextPeriode() {
        return nextPeriode;
    }

    public void setNextPeriode(List<Periode> nextPeriode) {
        this.nextPeriode = nextPeriode;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.uid);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Periode other = (Periode) obj;
        return Objects.equals(this.uid, other.uid);
    }

    @Override
    public String toString() {
        return "Periode{" + "productId=" + productId + ", dateDebut=" + dateDebut + ", dateFin=" + dateFin + ", comment=" + comment + ", mouvement=" + mouvement + ", stockInitial=" + stockInitial + ", stockFinal=" + stockFinal + ", ecart=" + ecart + ", mesureId=" + mesureId + '}';
    }

    public LocalDateTime getNow() {
        return now;
    }

    public void setNow(LocalDateTime now) {
        this.now = now;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
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
