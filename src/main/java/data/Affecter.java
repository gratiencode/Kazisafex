/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.UUID;


import org.hibernate.annotations.UuidGenerator; 
import jakarta.xml.bind.annotation.XmlRootElement;
import tools.Tables;

/**
 *
 * @author eroot
 */
@Entity
@Table(name = "affecter")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Affecter.findAll", query = "SELECT DISTINCT  a FROM Affecter a"),
    @NamedQuery(name = "Affecter.findByUid", query = "SELECT DISTINCT  a FROM Affecter a WHERE a.uid = :uid"),
    @NamedQuery(name = "Affecter.findByRegion", query = "SELECT DISTINCT  a FROM Affecter a WHERE a.region = :region"),
    @NamedQuery(name = "Affecter.findByDateAffect", query = "SELECT DISTINCT  a FROM Affecter a WHERE a.dateAffect = :dateAffect")})
public class Affecter extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "uid", updatable = false, nullable = false)
    private String uid;
    private String region;
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss"
    )
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateAffect;
    @JsonBackReference
    @ManyToOne
    private Permission permissionId;
    @ManyToOne
    @JsonBackReference
    private Role roleId;

    public Affecter() {
        
    }

    @PrePersist
    @PreUpdate
    protected void onDataOperation(){
        if(this.uid==null){
            this.uid= UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
        }
    }

    public Affecter(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Date getDateAffect() {
        return dateAffect;
    }

    public void setDateAffect(Date dateAffect) {
        this.dateAffect = dateAffect;
    }

    public Permission getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Permission permissionId) {
        this.permissionId = permissionId;
    }

    public Role getRoleId() {
        return roleId;
    }

    public void setRoleId(Role roleId) {
        this.roleId = roleId;
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
        if (!(object instanceof Affecter)) {
            return false;
        }
        Affecter other = (Affecter) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Affecter[ uid=" + uid + " ]";
    }

}
