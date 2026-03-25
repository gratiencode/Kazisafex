/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;
import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;

/**
 *
 * @author eroot
 */
@Entity
@Table(name = "role")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Role.findAll", query = "SELECT DISTINCT  r FROM Role r"),
    @NamedQuery(name = "Role.findByUid", query = "SELECT DISTINCT  r FROM Role r WHERE r.uid = :uid"),
    @NamedQuery(name = "Role.findByRolename", query = "SELECT DISTINCT  r FROM Role r WHERE r.rolename = :rolename")})
public class Role extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "uid")
    private String uid;
    @Column(name = "rolename")
    private String rolename;
     @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "roleId")
    private List<Affecter> affecterList;

    public Role() {
    }

    public Role(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getRolename() {
        return rolename;
    }

    public void setRolename(String rolename) {
        this.rolename = rolename;
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

    
    public List<Affecter> getAffecterList() {
        return affecterList;
    }

    public void setAffecterList(List<Affecter> affecterList) {
        this.affecterList = affecterList;
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
        if (!(object instanceof Role)) {
            return false;
        }
        Role other = (Role) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Role[ uid=" + uid + " ]";
    }

}
