/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tools;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author endeleya
 */
public class WebResult implements Serializable {

    private Set<RequestResult> successResultSet = new HashSet<>();
    private Set<RequestResult> failureResultSet = new HashSet<>();

    public Set<RequestResult> getSuccessResultSet() {
        return successResultSet;
    }

    public Set<RequestResult> getFailureResultSet() {
        return failureResultSet;
    }

    public void setSuccessResultSet(Set<RequestResult> successResultSet) {
        this.successResultSet = successResultSet;
    }

    public void setFailureResultSet(Set<RequestResult> failureResultSet) {
        this.failureResultSet = failureResultSet;
    }

    @JsonIgnore
    public boolean addFailureResult(RequestResult e) {
        return failureResultSet.add(e);
    }

    @JsonIgnore
    public boolean addSuccessResult(RequestResult e) {
        return successResultSet.add(e);
    }

    @JsonIgnore
    public boolean isSuccessResultEmpty() {
        return successResultSet.isEmpty();
    }

    public void clearSuccessResults() {
        successResultSet.clear();
    }

    @JsonIgnore
    public boolean isFailureResultEmpty() {
        return failureResultSet.isEmpty();
    }

    public void clearFailureResult() {
        failureResultSet.clear();
    }

    @JsonIgnore
    public int getSuccessResultSize() {
        return successResultSet.size();
    }

    @JsonIgnore
    public int getFailureResultSize() {
        return failureResultSet.size();
    }

}
