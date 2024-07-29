/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  util.Employee
 */
package data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
 import org.hibernate.annotations.UuidGenerator; import jakarta.xml.bind.annotation.XmlRootElement;
import tools.Tables;

 @XmlRootElement 
public class Employee
extends BaseModel implements Serializable {
    private String userId;
    private String engagementId;
    private String region;
    private String poste;
    private String entreprise;
    private String nom;
    private String prenom;
    private String phone;
    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss"
    )
    private Date engagementDate;
    private boolean revoquee;

    public Employee() {
    }

    public Employee(String userId, String engagementId, String region, String poste, String entreprise, String nom, String prenom, String phone, Date engagementDate, boolean actif) {
        this.userId = userId;
        this.engagementId = engagementId;
        this.region = region;
        this.poste = poste;
        this.entreprise = entreprise;
        this.nom = nom;
        this.prenom = prenom;
        this.phone = phone;
        this.engagementDate = engagementDate;
        this.revoquee = actif;
    }

    public boolean isRevoquee() {
        return this.revoquee;
    }

    public void setRevoquee(boolean actif) {
        this.revoquee = actif;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEngagementId() {
        return this.engagementId;
    }

    public void setEngagementId(String engagementId) {
        this.engagementId = engagementId;
    }

    public String getRegion() {
        return this.region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPoste() {
        return this.poste;
    }

    public void setPoste(String poste) {
        this.poste = poste;
    }

    public String getEntreprise() {
        return this.entreprise;
    }

    public void setEntreprise(String entreprise) {
        this.entreprise = entreprise;
    }

    public String getNom() {
        return this.nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return this.prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getEngagementDate() {
        return this.engagementDate;
    }

    public void setEngagementDate(Date engagementDate) {
        this.engagementDate = engagementDate;
    }

    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.userId);
        hash = 47 * hash + Objects.hashCode(this.engagementId);
        hash = 47 * hash + Objects.hashCode(this.region);
        return hash;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Employee other = (Employee)obj;
        if (!Objects.equals(this.userId, other.userId)) {
            return false;
        }
        if (!Objects.equals(this.engagementId, other.engagementId)) {
            return false;
        }
        if (!Objects.equals(this.region, other.region)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "Employee{userId=" + this.userId + ", engagementId=" + this.engagementId + ", region=" + this.region + ", poste=" + this.poste + ", entreprise=" + this.entreprise + ", nom=" + this.nom + ", prenom=" + this.prenom + ", phone=" + this.phone + ", engagementDate=" + this.engagementDate + ", actif=" + this.revoquee + '}';
    }
}

