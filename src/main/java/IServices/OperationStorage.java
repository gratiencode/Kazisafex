/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.util.Date;
import java.util.List;
import java.util.Set;
import data.Operation;
import data.Operation;

/**
 *
 * @author eroot
 */
public interface OperationStorage {
    public Operation createOperation(Operation obj);
    public Operation updateOperation(Operation obj);
    public void deleteOperation(Operation obj);
    public Long getCount();
    public Operation findOperation(String objId);
    public List<Operation> findOperations();
    public List<Operation> findOperations(String region);
    public List<Operation> findOperations(int start,int max);
    public List<Operation> findOperationByCompteTresor(String objId); 
    public List<Operation> findOpsByDepense(String depId);
     public List<Operation> findOpsByDepense(String depId,String tresorId);

    public List<Operation> findByDateInterval(Date date, Date addDays);

    public List<Operation> findByDateInterval(Date d1, Date kesho, String region);
 public List<Operation> mergeSet(Set<Operation> bulk);
}
