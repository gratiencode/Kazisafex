/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data; import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.List;

/**
 *
 * @author eroot
 */
public class BulkModel extends BaseModel{
    private List<Object> models;
    private boolean forAllUsers;

   

    public BulkModel(List<Object> models) {
        this.models = models;
    }

    public BulkModel() {
    }

    public List<Object> getModels() {
        return models;
    }

    public void setModels(List<Object> models) {
        this.models = models;
    }

    public boolean isForAllUsers() {
        return forAllUsers;
    }

    public void setForAllUsers(boolean forAllUsers) {
        this.forAllUsers = forAllUsers;
    }
    
}
