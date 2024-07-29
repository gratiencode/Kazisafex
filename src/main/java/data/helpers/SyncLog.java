/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.helpers;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
 import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author eroot
 */
 @XmlRootElement
public class SyncLog implements Serializable {
    private long sequence;
    private String tableName;
    private String uidElement;
    private String action;
    private String entrepriseUid;
    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss"
    )
    private Date syncDate;
    private boolean synced;
    private long upcount;
    private long downcount;
    private String region;

    public SyncLog() {
    }

    public SyncLog(long sequence, String tableName, String uidElement, String entrepriseUid, boolean synced, String region) {
        this.sequence = sequence;
        this.tableName = tableName;
        this.uidElement = uidElement;
        this.entrepriseUid = entrepriseUid;
        this.synced = synced;
        this.region = region;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getUidElement() {
        return uidElement;
    }

    public void setUidElement(String uidElement) {
        this.uidElement = uidElement;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntrepriseUid() {
        return entrepriseUid;
    }

    public void setEntrepriseUid(String entrepriseUid) {
        this.entrepriseUid = entrepriseUid;
    }

    public Date getSyncDate() {
        return syncDate;
    }

    public void setSyncDate(Date syncDate) {
        this.syncDate = syncDate;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public long getUpcount() {
        return upcount;
    }

    public void setUpcount(long upcount) {
        this.upcount = upcount;
    }

    public long getDowncount() {
        return downcount;
    }

    public void setDowncount(long downcount) {
        this.downcount = downcount;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (int) (this.sequence ^ (this.sequence >>> 32));
        hash = 97 * hash + Objects.hashCode(this.tableName);
        hash = 97 * hash + Objects.hashCode(this.uidElement);
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
        final SyncLog other = (SyncLog) obj;
        if (this.sequence != other.sequence) {
            return false;
        }
        if (!Objects.equals(this.tableName, other.tableName)) {
            return false;
        }
        if (!Objects.equals(this.uidElement, other.uidElement)) {
            return false;
        }
        return true;
    }

    
    
    

}
