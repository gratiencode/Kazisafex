/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.helpers;

/**
 *
 * @author eroot
 */
public class Register {
    private long timestamp;
    private String uidInfo;
    private String action;
    private String status;
    private String table;

    public Register() {
    }

    public Register(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public Register(long timestamp, String uidInfo, String action, String status, String table) {
        this.timestamp = timestamp;
        this.uidInfo = uidInfo;
        this.action = action;
        this.status = status;
        this.table = table;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUidInfo() {
        return uidInfo;
    }

    public void setUidInfo(String uidInfo) {
        this.uidInfo = uidInfo;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (int) (this.timestamp ^ (this.timestamp >>> 32));
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
        final Register other = (Register) obj;
        return this.timestamp == other.timestamp;
    }
    
    
    
}
