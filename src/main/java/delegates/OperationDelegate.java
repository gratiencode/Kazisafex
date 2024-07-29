/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.OperationStorage;
import java.util.Date;
import java.util.List;
import data.Operation;
import tools.ServiceLocator;
import tools.Tables;
import static delegates.OperationDelegate.getStorage;

/**
 *
 * @author eroot
 */
public class OperationDelegate {
    
    public static Operation saveOperation(Operation cat) {
        return getStorage().createOperation(cat);
    }

    public static Operation updateOperation(Operation cat) {
        return getStorage().updateOperation(cat);
    }

    public static void deleteOperation(Operation cat) {
        getStorage().deleteOperation(cat);
    }

    public static Operation findOperation(String objId) {
        return getStorage().findOperation(objId);
    }
    
    
    public static List<Operation> findOperations(){
       return getStorage().findOperations();
    }
    
    
    public static List<Operation> findOperations(int s,int m){
       return getStorage().findOperations(s,m);
    }
    
   public static OperationStorage getStorage(){
        OperationStorage cats=(OperationStorage)ServiceLocator.getInstance().getService(Tables.OPERATION);
        return cats;
    } 

    public static List<Operation> findByDateInterval(Date date, Date addDays) {
        return getStorage().findByDateInterval(date,addDays);
    }

    public static Long getCount() {
        return getStorage().getCount();
    }

    public static List<Operation> findOperations(String region) {
      return getStorage().findOperations(region);
    }

    public static List<Operation> findByDateInterval(Date d1, Date kesho, String region) {
               return getStorage().findByDateInterval(d1,kesho,region);
    }
}
