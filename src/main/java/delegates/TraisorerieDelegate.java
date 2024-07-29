/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.TraisorerieStorage;
import static delegates.TraisorerieDelegate.getTraisorerieStorage;
import java.util.List;
import data.Traisorerie;
import tools.ServiceLocator;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class TraisorerieDelegate {
    
    public static Traisorerie saveTraisorerie(Traisorerie cat) {
        return getTraisorerieStorage().createTraisorerie(cat);
    }

    public static Traisorerie updateTraisorerie(Traisorerie cat) {
        return getTraisorerieStorage().updateTraisorerie(cat);
    }

    public static void deleteTraisorerie(Traisorerie cat) {
        getTraisorerieStorage().deleteTraisorerie(cat);
    }

    public static Traisorerie findTraisorerie(String objId) {
        return getTraisorerieStorage().findTraisorerie(objId);
    }
    
    
    public static List<Traisorerie> findTraisoreries(){
       return getTraisorerieStorage().findTraisoreries();
    }
    
    public static List<Traisorerie> findTraisoreries(String region){
       return getTraisorerieStorage().findTraisoreries(region);
    }
     
    public static List<Traisorerie> findTraisorByCompteTresor(String cptId){
       return getTraisorerieStorage().findTraisorerieByCompteTresor(cptId);
    }
    
    public static List<Traisorerie> findTraisoreries(int s,int m){
       return getTraisorerieStorage().findTraisoreries(s,m);
    }
    
    
   public static TraisorerieStorage getTraisorerieStorage(){
        TraisorerieStorage cats=(TraisorerieStorage)ServiceLocator.getInstance().getService(Tables.TRAISORERIE);
        return cats;
    } 

    public static List<Traisorerie> findTraisorByCompteTresor(String cuid, String name) {
        return getTraisorerieStorage().findTraisorerieByCompteTresor(cuid,name);
    }
    
     public static List<Traisorerie> findTraisorByCompteTresOR(String cuid, String name) {
        return getTraisorerieStorage().findTraisorerieByCompteTresOR(cuid,name);
    }

    public static Double sumByReference(String numero, double taux2change) {
       return getTraisorerieStorage().sumByReference(numero, taux2change);
    }

    public static Long getCount() {
        return getTraisorerieStorage().getCount();
    }
}
