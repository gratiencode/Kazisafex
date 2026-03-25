/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data; import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Objects;
import java.util.prefs.Preferences;
import tools.SyncEngine;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class Refresher extends BaseModel {
    private String target;
   

    public Refresher() {
         this.type=Tables.REFRESH.name();
    }
    

    public Refresher(String target) {
        this.target = target;
         this.type=Tables.REFRESH.name();
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.target);
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
        final Refresher other = (Refresher) obj;
        if (!Objects.equals(this.target, other.target)) {
            return false;
        }
        return true;
    }
    
}
