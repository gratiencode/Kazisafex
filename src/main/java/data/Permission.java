/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data; import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 *
 * @author eroot
 */
@Entity
@Table(name = "permission")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Permission.findAll", query = "SELECT DISTINCT  p FROM Permission p"),
    @NamedQuery(name = "Permission.findByUid", query = "SELECT DISTINCT  p FROM Permission p WHERE p.uid = :uid"),
    @NamedQuery(name = "Permission.findByPermissionname", query = "SELECT DISTINCT  p FROM Permission p WHERE p.permissionname = :permissionname")})

public class Permission extends BaseModel implements Serializable {

    @Column(name = "permissionname")
    private String permissionname;

    @Column(name = "tablename")
    private String tablename;
    
     @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "uid")
    private String uid;
    @OneToMany(mappedBy = "permissionId")
    private List<Affecter> affecterList;

    public Permission() {
    }

    public Permission(String uid) {
        this.uid = uid;
    }
    
    @PrePersist
    public void uuidify(){
        this.uid=UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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
        if (!(object instanceof Permission)) {
            return false;
        }
        Permission other = (Permission) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Permission[ uid=" + uid + " ]";
    }

    public String getPermissionname() {
        return permissionname;
    }

    public void setPermissionname(String permissionname) {
        this.permissionname = permissionname;
    }

    public String getTablename() {
        return tablename;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename;
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
