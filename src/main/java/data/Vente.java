/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.List;
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

import jakarta.xml.bind.annotation.XmlRootElement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import tools.Tables;

/**
 *
 * @author eroot
 */
@Entity
@Table(name = "vente")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Vente.findSet", query = "SELECT DISTINCT  v FROM Vente v ORDER BY v.dateVente DESC "),
    @NamedQuery(name = "Vente.findAll", query = "SELECT DISTINCT  v FROM Vente v WHERE v.observation != :draft ORDER BY v.dateVente DESC "),
    @NamedQuery(name = "Vente.findByUid", query = "SELECT DISTINCT  v FROM Vente v WHERE v.uid = :uid"),
    @NamedQuery(name = "Vente.findByReference", query = "SELECT DISTINCT  v FROM Vente v WHERE v.reference = :reference"),
    @NamedQuery(name = "Vente.findByRegion", query = "SELECT DISTINCT  v FROM Vente v WHERE v.region = :region"),
    @NamedQuery(name = "Vente.findByDateVente", query = "SELECT DISTINCT  v FROM Vente v WHERE v.dateVente = :dateVente"),
    @NamedQuery(name = "Vente.findByDateVenteInterval", query = "SELECT DISTINCT  v FROM Vente v WHERE v.dateVente BETWEEN :date1 AND :date2"),
    @NamedQuery(name = "Vente.findBySumUSD", query = "SELECT DISTINCT  SUM(v.montantUsd) FROM Vente v WHERE v.dateVente BETWEEN :date1 AND :date2"),
    @NamedQuery(name = "Vente.findBySumCDF", query = "SELECT DISTINCT  SUM(v.montantCdf) FROM Vente v WHERE v.dateVente BETWEEN :date1 AND :date2"),
    @NamedQuery(name = "Vente.findBySumDebt", query = "SELECT DISTINCT  SUM(v.montantDette) FROM Vente v WHERE v.dateVente BETWEEN :date1 AND :date2"),
    @NamedQuery(name = "Vente.findByMontantUsd", query = "SELECT DISTINCT  v FROM Vente v WHERE v.montantUsd = :montantUsd"),
    @NamedQuery(name = "Vente.findByMontantCdf", query = "SELECT DISTINCT  v FROM Vente v WHERE v.montantCdf = :montantCdf"),
    @NamedQuery(name = "Vente.findBySumUSDRegioError writing JSON-B serialized object.n", query = "SELECT DISTINCT  SUM(v.montantUsd) FROM Vente v WHERE v.dateVente BETWEEN :date1 AND :date2 AND v.region = :region"),
    @NamedQuery(name = "Vente.findBySumCDFRegion", query = "SELECT DISTINCT  SUM(v.montantCdf) FROM Vente v WHERE v.dateVente BETWEEN :date1 AND :date2 AND v.region = :region"),
    @NamedQuery(name = "Vente.findBySumDebtRegion", query = "SELECT DISTINCT  SUM(v.montantDette) FROM Vente v WHERE v.dateVente BETWEEN :date1 AND :date2 AND v.region = :region"),
    @NamedQuery(name = "Vente.findByEcheance", query = "SELECT DISTINCT  v FROM Vente v WHERE v.echeance = :echeance"),
    @NamedQuery(name = "Vente.findByPayment", query = "SELECT DISTINCT  v FROM Vente v WHERE v.payment = :payment"),
    @NamedQuery(name = "Vente.findByLatitude", query = "SELECT DISTINCT  v FROM Vente v WHERE v.latitude = :latitude"),
    @NamedQuery(name = "Vente.findByLongitude", query = "SELECT DISTINCT  v FROM Vente v WHERE v.longitude = :longitude"),
    @NamedQuery(name = "Vente.findByMontantDette", query = "SELECT DISTINCT  v FROM Vente v WHERE v.montantDette = :montantDette"),
    @NamedQuery(name = "Vente.findByDeviseDette", query = "SELECT DISTINCT  v FROM Vente v WHERE v.deviseDette = :deviseDette")})

public class Vente extends BaseModel implements Serializable {

    @Column(name = "reference")
    private String reference;
    @Column(name = "region")
    private String region;
    @Column(name = "dateVente", columnDefinition = "DATETIME")
    private LocalDateTime dateVente;
    private double montantUsd;
    private double montantCdf;
    private String payment;
    private String libelle;
    private String observation;
    private String deviseDette;

    private static final long serialVersionUID = 1L;
    @Id
    private Integer uid;
    @Column(name = "echeance", columnDefinition = "DATE")
    private LocalDate echeance;
    private Double latitude = 0d;
    private Double longitude = 0d;
    private Double montantDette = 0d;
    @OneToMany(mappedBy = "venteReference")
    @JsonBackReference(value = "vnt-tx")
    private List<Taxer> taxerList;
    @OneToMany(mappedBy = "reference")
    @JsonBackReference(value = "vnt-ligv")
    private List<LigneVente> ligneVenteList;
    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;
    @JoinColumn(name = "clientId", referencedColumnName = "uid")
    @ManyToOne
    private Client clientId;

    @PrePersist
    @PreUpdate
    protected void doBeforeUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Vente() {
        this.type = Tables.VENTE.name();
    }

    public Vente(Integer uid) {
        this.uid = uid;
        this.type = Tables.VENTE.name();
    }

    public Vente(Integer uid, String reference, LocalDateTime dateVente, double montantUsd, double montantCdf, String payment, String libelle, String observation) {
        this.uid = uid;
        this.reference = reference;
        this.dateVente = dateVente;
        this.montantUsd = montantUsd;
        this.montantCdf = montantCdf;
        this.payment = payment;
        this.libelle = libelle;
        this.observation = observation;
        this.type = Tables.VENTE.name();
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public LocalDateTime getDateVente() {
        return dateVente;
    }

    public void setDateVente(LocalDateTime dateVente) {
        this.dateVente = dateVente;
    }

    public double getMontantUsd() {
        return montantUsd;
    }

    public void setMontantUsd(double montantUsd) {
        this.montantUsd = montantUsd;
    }

    public double getMontantCdf() {
        return montantCdf;
    }

    public void setMontantCdf(double montantCdf) {
        this.montantCdf = montantCdf;
    }

    public LocalDate getEcheance() {
        return echeance;
    }

    public void setEcheance(LocalDate echeance) {
        this.echeance = echeance;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude == null ? 0 : latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude == null ? 0 : longitude;
    }

    public Double getMontantDette() {
        return montantDette;
    }

    public void setMontantDette(Double montantDette) {
        this.montantDette = montantDette;
    }

    public String getDeviseDette() {
        return deviseDette;
    }

    public void setDeviseDette(String deviseDette) {
        this.deviseDette = deviseDette;
    }

    public List<Taxer> getTaxerList() {
        return taxerList;
    }

    public void setTaxerList(List<Taxer> taxerList) {
        this.taxerList = taxerList;
    }

    public List<LigneVente> getLigneVenteList() {
        return ligneVenteList;
    }

    public void setLigneVenteList(List<LigneVente> ligneVenteList) {
        this.ligneVenteList = ligneVenteList;
    }

    public Client getClientId() {
        return clientId;
    }

    public void setClientId(Client clientId) {
        this.clientId = clientId;
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
        if (!(object instanceof Vente)) {
            return false;
        }
        Vente other = (Vente) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Vente[ uid=" + uid + " ]";
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setDeletedAt(LocalDateTime updatedAt) {
        this.deletedAt = updatedAt;
    }

}
