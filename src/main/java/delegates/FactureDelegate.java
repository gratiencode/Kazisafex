/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.FactureStorage;
import static delegates.FactureDelegate.getFactureStorage;
import java.util.Date;
import java.util.List;
import data.Facture;
import tools.ServiceLocator;
import tools.Tables;
import utilities.Relevee;


/**
 *
 * @author eroot
 */
public class FactureDelegate {
    
    public static Facture saveFacture(Facture cat) {
        return getFactureStorage().createFacture(cat);
    }

    public static Facture updateFacture(Facture cat) {
        return getFactureStorage().updateFacture(cat);
    }

    public static void deleteFacture(Facture cat) {
        getFactureStorage().deleteFacture(cat);
    }

    public static Facture findFacture(String objId) {
        return getFactureStorage().findFacture(objId);
    }
    
    
    public static List<Facture> findFactures(){
       return getFactureStorage().findFactures();
    }
    
     public static List<Facture> findFactures(int s,int m){
       return getFactureStorage().findFactures(s,m);
    }
    
   public static FactureStorage getFactureStorage(){
        FactureStorage cats=(FactureStorage)ServiceLocator.getInstance().getService(Tables.FACTURE);
        return cats;
    } 

    public static List<Relevee> findReleveeInterval(String uid, Date toUtilDate, Date toUtilDate0) {
       return getFactureStorage().findReleveeInterval(uid, toUtilDate, toUtilDate0);
    }

    public static List<Facture> findOrgaBills(String uid) {
        return getFactureStorage().findOrgaBills(uid);
    }

    public static List<Facture> findUpaidBillsFor(String uid) {
       return getFactureStorage().findUpaidBillsFor(uid);
    }

    public static List<Facture> findBillingInInterval(Date toUtilDate, Date toUtilDate0) {
        return getFactureStorage().findBillingInInterval(toUtilDate, toUtilDate0);
    }

   public static Long getCount() {
        return getFactureStorage().getCount();
    }
}
