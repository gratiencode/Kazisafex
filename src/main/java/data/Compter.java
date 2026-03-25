/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package data;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.UUID;

/**
 *
 * @author endeleya
 */
@Entity
@Table(name = "compter")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Compter.findAll", query = "SELECT DISTINCT  c FROM Compter c "),
    @NamedQuery(name = "Compter.findByUid", query = "SELECT DISTINCT  c FROM Compter c WHERE c.uid = :uid"),
    @NamedQuery(name = "Compter.findByRegion", query = "SELECT DISTINCT  c FROM Compter c WHERE c.region = :region"),
    @NamedQuery(name = "Compter.findByNumlot", query = "SELECT DISTINCT  c FROM Compter c WHERE c.numlot= :numlot"),
    @NamedQuery(name = "Compter.findByEtat", query = "SELECT DISTINCT  c FROM Compter c WHERE c.timestamp = :timestamp")})

public class Compter extends BaseModel implements Serializable {

    @Id
    @Column(name = "uid")
    private String uid;

    @JoinColumn(name = "inventaire_id", referencedColumnName = "uid")
    @ManyToOne(optional = false)
    private Inventaire inventaireId;
    @Column(name = "date_count", columnDefinition = "TIMESTAMP")
    private LocalDateTime dateCount;
    @JoinColumn(name = "mesure_id", referencedColumnName = "uid")
    @ManyToOne
    private Mesure mesureId;
    @JoinColumn(name = "product_id", referencedColumnName = "uid")
    @ManyToOne(optional = false)
    private Produit productId;
    @Column(name = "region")
    private String region;
    @Column(name = "numlot")
    private String numlot;
    @Column(name = "_timestamp")
    private BigInteger timestamp;
    @Column(name = "quantite")
    private double quantite;
    @Column(name = "cout_achat")
    private double coutAchat;
    @Column(name = "quantite_theorik", columnDefinition = "DOUBLE")
    private Double quantiteTheorik;
    @Column(name = "ecart", columnDefinition = "DOUBLE")
    private Double ecart;
    @Column(name = "observation", columnDefinition = "TEXT")
    private String observation;
    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;
    @Column(name = "date_expiration", columnDefinition = "DATE")
    private LocalDate dateExpiration;

    @PrePersist
    @PreUpdate
    protected void onDataOperation() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
        this.updatedAt = LocalDateTime.now();
    }

    public Compter(String uid) {
        this.uid = uid;
    }

    public Compter() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public LocalDateTime getDateCount() {
        return dateCount;
    }

    public void setDateCount(LocalDateTime dateCount) {
        this.dateCount = dateCount;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getNumlot() {
        return numlot;
    }

    public void setNumlot(String numlot) {
        this.numlot = numlot;
    }

    public BigInteger getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
    }

    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }

    public Inventaire getInventaireId() {
        return inventaireId;
    }

    public void setInventaireId(Inventaire inventaireId) {
        this.inventaireId = inventaireId;
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

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public double getCoutAchat() {
        return coutAchat;
    }

    public void setCoutAchat(double coutAchat) {
        this.coutAchat = coutAchat;
    }

    public Double getQuantiteTheorik() {
        return quantiteTheorik;
    }

    public void setQuantiteTheorik(Double quantiteTheorik) {
        this.quantiteTheorik = quantiteTheorik;
    }

    public Double getEcart() {
        return ecart;
    }

    public void setEcart(Double ecart) {
        this.ecart = ecart;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }
 
}
