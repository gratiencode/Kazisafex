/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import data.Depense;
import java.util.Date;
import java.util.List; import java.time.LocalDateTime;
import java.util.Set;
import data.Operation;
import data.Operation;
import java.time.LocalDate;

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

    public List<Operation> findOperations(int start, int max);

    public List<Operation> findOperationByCompteTresor(String objId);

    public List<Operation> findOpsByDepense(String depId);

    public List<Operation> findOpsByDepense(String depId, String tresorId);

    public List<Operation> findByDateInterval(LocalDate date, LocalDate addDays);

    public List<Operation> findByDateInterval(LocalDate d1, LocalDate kesho, String region);

    public List<Operation> mergeSet(Set<Operation> bulk);

    public List<Operation> findUnSyncedOperations(long disconnected_at);

    public List<Operation> findOperationByImputation(String DEPT);
    public boolean isExists(String uid);public boolean isExists(String uid, LocalDateTime atime);

    public List<Operation> findByDateInterval(Depense dep, LocalDate date1, LocalDate date2);
}
