/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tools;

import data.BaseModel;
import java.io.Serializable;


/**
 *
 * @author endeleya
 */
public class RequestResult  extends BaseModel implements Serializable {

    private String tableName;
    private String stringInstanceId="";
    private int intInstanceId;
    private long longInstanceId;
    private boolean successResult;

    public RequestResult() {
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getStringInstanceId() {
        return stringInstanceId;
    }

    public void setStringInstanceId(String stringInstanceId) {
        this.stringInstanceId = stringInstanceId;
    }

    public int getIntInstanceId() {
        return intInstanceId;
    }

    public void setIntInstanceId(int intInstanceId) {
        this.intInstanceId = intInstanceId;
    }

    public long getLongInstanceId() {
        return longInstanceId;
    }

    public void setLongInstanceId(long longInstanceId) {
        this.longInstanceId = longInstanceId;
    }

    public boolean isSuccessResult() {
        return successResult;
    }

    public void setSuccessResult(boolean successResult) {
        this.successResult = successResult;
    }

}
