/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.UniqueConstraint;
 import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author eroot
 */
@Entity
@Table(name = "journal", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"stringuid","intuid","longuid","actionname","tablename"})
})
 @XmlRootElement
public class Journal implements Serializable {

    @Id
    @Column(name = "uid", updatable = false, nullable = false)
    private long uid;
    @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
    @Column(name = "syncTime")
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss"
    )
    private Date syncTime;
    @Column(name = "stringuid")
    private String stringUid="SAVED";
    @Column(name = "intuid")
    private int intUid = 0;
    @Column(name = "longuid")
    private long longUid = 0;
    @Column(name = "tablename")
    private String tableName;
    @Column(name = "actionname")
    private String actionName;
    @Column(name = "synced")
    private Boolean synced;
    
    @PrePersist
    @PreUpdate
    protected void onDataOperation(){
        if(this.uid==0){
            this.uid= System.currentTimeMillis()+103;
            this.stringUid=stringUid.replaceAll("-", "");
        }
        
    }

    public Journal(long uid) {
        this.uid = uid;
    }

    public Journal() {
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public Date getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(Date syncTime) {
        this.syncTime = syncTime;
    }

    public String getStringUid() {
        return stringUid;
    }

    public void setStringUid(String stringUid) {
        this.stringUid = stringUid;
    }

    public int getIntUid() {
        return intUid;
    }

    public void setIntUid(int intUid) {
        this.intUid = intUid;
    }

    public long getLongUid() {
        return longUid;
    }

    public void setLongUid(long longUid) {
        this.longUid = longUid;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public Boolean getSynced() {
        return synced;
    }

    public void setSynced(Boolean synced) {
        this.synced = synced;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + (int) (this.uid ^ (this.uid >>> 32));
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
        final Journal other = (Journal) obj;
        if (this.uid != other.uid) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "{" + "\"uid\":" + uid + ",\"syncTime\":\"" + syncTime + "\", \"stringUid\":\"" + stringUid + "\",\"intUid\":" + intUid + ", \"longUid\":" + longUid + ",\" tableName\":\"" + tableName + "\",\" actionName\":\"" + actionName + "\",\" synced\":" + synced + '}';
    }

}
