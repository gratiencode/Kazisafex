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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author endeleya
 */
@Entity
@Table(name = "immobilisation_agregate")
public class ImmobilisationAgregate implements Serializable {

    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "uid")
    private String uid;

    @Column(name = "date", columnDefinition = "DATETIME")
    private LocalDateTime date;

    @Column(name = "valeur_brutte")
    private Double valeurBrutte;
    
    @Column(name = "ammortissement")
    private Double ammortissement;
    
    @Column(name = "valeur_nette")
    private Double valeurNette;
    
    @Column(name = "region")
    private String region;

    @JoinColumn(name = "immobilisation_id", referencedColumnName = "uid")
    @ManyToOne
    private Immobilisation immobilisationId;

    public ImmobilisationAgregate() {
    }
    
     @PrePersist
    private void prepersist() {
        if (uid == null) {
            uid = UUID.randomUUID().toString().toLowerCase().replace("-", "");
        }
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Double getValeurBrutte() {
        return valeurBrutte;
    }

    public void setValeurBrutte(Double valeurBrutte) {
        this.valeurBrutte = valeurBrutte;
    }

    public Double getAmmortissement() {
        return ammortissement;
    }

    public void setAmmortissement(Double ammortissement) {
        this.ammortissement = ammortissement;
    }

    public Double getValeurNette() {
        return valeurNette;
    }

    public void setValeurNette(Double valeurNette) {
        this.valeurNette = valeurNette;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Immobilisation getImmobilisationId() {
        return immobilisationId;
    }

    public void setImmobilisationId(Immobilisation immobilisationId) {
        this.immobilisationId = immobilisationId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.uid);
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
        final ImmobilisationAgregate other = (ImmobilisationAgregate) obj;
        return Objects.equals(this.uid, other.uid);
    }

    @Override
    public String toString() {
        return "ImmobilisationAgregate{" + "uid=" + uid + ", date=" + date + ", valeurBrutte=" + valeurBrutte + ", ammortissement=" + ammortissement + ", valeurNette=" + valeurNette + ", region=" + region + ", immobilisationId=" + immobilisationId + '}';
    }
    
    
}
