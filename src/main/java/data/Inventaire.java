/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package data;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
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
@Table(name = "inventaire")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Inventaire.findAll", query = "SELECT DISTINCT  i FROM Inventaire i ORDER BY i.dateFin DESC"),
    @NamedQuery(name = "Inventaire.findByUid", query = "SELECT DISTINCT  i FROM Inventaire i WHERE i.uid = :uid"),
    @NamedQuery(name = "Inventaire.findByRegion", query = "SELECT DISTINCT  i FROM Inventaire i WHERE i.region = :region"),
    @NamedQuery(name = "Inventaire.findByComment", query = "SELECT DISTINCT  i FROM Inventaire i WHERE i.comment = :comment"),
    @NamedQuery(name = "Inventaire.findByCodeInventaire", query = "SELECT DISTINCT  i FROM Inventaire i WHERE i.codeInventaire= :codeInventaire"),
    @NamedQuery(name = "Inventaire.findByEtat", query = "SELECT DISTINCT  i FROM Inventaire i WHERE i.etat = :etat")})

public class Inventaire extends BaseModel implements Serializable {

    @Id
    @Column(name = "uid")
    private String uid;
    @Column(name = "etat")
    private String etat;
    @Column(name = "comment")
    private String comment;
    @Column(name = "code_inventaire")
    private String codeInventaire;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "date_fin", columnDefinition = "DATE")
    private LocalDate dateFin;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "date_debut", columnDefinition = "DATE")
    private LocalDate dateDebut;
    @Column(name = "region")
    private String region;
    @OneToMany(mappedBy = "inventaireId")
    @JsonBackReference(value = "inv-cnt")
    private List<Compter> compterList;
    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;
    @Column(name = "valeur_total")
    private double valeurTotal=0;
    @Column(name = "valeur_total_ecart")
    private Double valeurTotalEcart=0d;

    public Inventaire() {
    }

    @PrePersist
    @PreUpdate
    protected void onDataOperation() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
        this.updatedAt = LocalDateTime.now();
    }

    public Inventaire(String uid, String etat, String comment, String codeInventaire, LocalDate dateFin, LocalDate dateDebut, String region) {
        this.uid = uid;
        this.etat = etat;
        this.comment = comment;
        this.codeInventaire = codeInventaire;
        this.dateFin = dateFin;
        this.dateDebut = dateDebut;
        this.region = region;
    }

    public Inventaire(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCodeInventaire() {
        return codeInventaire;
    }

    public void setCodeInventaire(String codeInventaire) {
        this.codeInventaire = codeInventaire;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    
    public List<Compter> getCompterList() {
        return compterList;
    }

    public void setCompterList(List<Compter> compterList) {
        this.compterList = compterList;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.uid);
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
        final Inventaire other = (Inventaire) obj;
        return Objects.equals(this.uid, other.uid);
    }

    @Override
    public String toString() {
        return "Inventaire{" + "uid=" + uid + ", etat=" + etat + ", comment=" + comment + ", codeInventaire=" + codeInventaire + ", dateFin=" + dateFin + ", dateDebut=" + dateDebut + ", region=" + region + '}';
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

    public double getValeurTotal() {
        return valeurTotal;
    }

    public void setValeurTotal(double valeurTotal) {
        this.valeurTotal = valeurTotal;
    }

    public Double getValeurTotalEcart() {
        return valeurTotalEcart;
    }

    public void setValeurTotalEcart(Double valeurTotalEcart) {
        this.valeurTotalEcart = valeurTotalEcart;
    }

    
}
