/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.LigneVenteStorage;
import java.util.List;
import data.LigneVente;
import tools.ServiceLocator;
import tools.Tables;
import static delegates.LigneVenteDelegate.getStorage;
import java.util.Date;
import java.util.Set;
import data.Recquisition;
import data.Vente;

/**
 *
 * @author eroot
 */
public class LigneVenteDelegate {
    
    public static LigneVente saveLigneVente(LigneVente cat) {
        return getStorage().createLigneVente(cat);
    }

    public static LigneVente updateLigneVente(LigneVente cat) {
        return getStorage().updateLigneVente(cat);
    }

    public static void deleteLigneVente(LigneVente cat) {
        getStorage().deleteLigneVente(cat);
    }

    public static LigneVente findLigneVente(Long objId) {
        return getStorage().findLigneVente(objId);
    }
    
    
    public static List<LigneVente> findLigneVentes(){
       return getStorage().findLigneVentes();
    }
    
    
     public static List<LigneVente> findLigneVentes(int s,int m){
       return getStorage().findLigneVentes(s,m);
    }
    
   public static LigneVenteStorage getStorage(){
        LigneVenteStorage cats=(LigneVenteStorage)ServiceLocator.getInstance().getService(Tables.LIGNEVENTE);
        return cats;
    } 

    public static List<LigneVente> findByProduit(String uid) {
       return getStorage().findByProduit(uid);
    }

    public static List<LigneVente> findByProduitRegion(String uid, String region) {
         return getStorage().findByProduitRegion(uid,region);
    }

    public static List<LigneVente> findByReference(Integer uid) {
         return getStorage().findByReference(uid);
    }

    public static List<LigneVente> findByProduitWithLot(String uid, String numlot) {
        return getStorage().findByProduitWithLot(uid, numlot);
    }

    public static List<LigneVente> findByProduitWithLot(String uid, String numlot, String region) {
        return getStorage().findByProduitWithLot(uid, numlot, region);
    }

    public static double sumByProduitWithLotInUnit(String idpro, String lot) {
       return getStorage().sumByProduitWithLotInUnit(idpro, lot);
    }

    public static Long getCount() {
        return getStorage().getCount();
    }

    public static List<LigneVente> findByProduitWithLot(String uid, String numlot, Date debut, Date fin) {
       return getStorage().findByProduitWithLot(uid,numlot,debut,fin);
    }

    public static List<LigneVente> findByProduitWithLot(String uid, String numlot, Date debut, Date fin, String region) {
      return getStorage().findByProduitWithLot(uid, numlot, debut, fin, region);
    }

    public static double sumByProduit(String uid) {
       return getStorage().sumByProduit(uid);
    }

    public static double sumByProduit(String uid, String region) {
        return getStorage().sumByProduit(uid,region);
    }

    public static List<LigneVente> mergeSet(Set<LigneVente> lvs) {
       return getStorage().mergeSet(lvs);
    }

    public static double sumByProduit(String idpro, Date d1, Date d2) {
        return getStorage().sumByProduit(idpro, d1, d2);
    }

    public static double sumByProduit(String idpro, Date d1, Date d2, String region) {
      return getStorage().sumByProduit(idpro,d1,d2,region);
    }

    public static LigneVente saveLigneVente(LigneVente i, Vente vente4save) {
     return getStorage().saveLigneVente(i,vente4save);
    }

   
}
