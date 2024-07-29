/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.helpers;

import java.util.Objects;

/**
 *
 * @author eroot
 */
public class Checked {
    private boolean exist;
    private String elementId;
    private String table;
    private String execMessage="Succes";

    public Checked() {
    }

    public Checked(boolean exist, String elementId, String table) {
        this.exist = exist;
        this.elementId = elementId;
        this.table = table;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.elementId);
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
        final Checked other = (Checked) obj;
        if (!Objects.equals(this.elementId, other.elementId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Checked{" + "exist=" + exist + ", elementId=" + elementId + ", table=" + table + '}';
    }

    public String getExecMessage() {
        return execMessage;
    }

    public void setExecMessage(String execMessage) {
        this.execMessage = execMessage;
    }
    
    
}
