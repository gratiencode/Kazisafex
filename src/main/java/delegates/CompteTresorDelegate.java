/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.CompteTresorStorage;
import static delegates.CompteTresorDelegate.getCompteTresorStorage;
import java.util.List;
import data.CompteTresor;
import tools.ServiceLocator;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class CompteTresorDelegate {
    
    public static CompteTresor saveCompteTresor(CompteTresor cat) {
        return getCompteTresorStorage().createCompteTresor(cat);
    }

    public static CompteTresor updateCompteTresor(CompteTresor cat) {
        return getCompteTresorStorage().updateCompteTresor(cat);
    }

    public static void deleteCompteTresor(CompteTresor cat) {
        getCompteTresorStorage().deleteCompteTresor(cat);
    }

    public static CompteTresor findCompteTresor(String objId) {
        return getCompteTresorStorage().findCompteTresor(objId);
    }
    
    
    public static List<CompteTresor> findCompteTresors(){
       return getCompteTresorStorage().findCompteTresors();
    }
    
    public static List<CompteTresor> findCompteTresors(String region){
       return getCompteTresorStorage().findCompteTresors(region);
    }
    
    public static List<CompteTresor> findCompteTresors(int s,int m){
       return getCompteTresorStorage().findCompteTresors(s,m);
    }
    
  public static CompteTresorStorage getCompteTresorStorage(){
        CompteTresorStorage cats=(CompteTresorStorage)ServiceLocator.getInstance().getService(Tables.COMPTETRESOR);
        return cats;
    }  

   public static Long getCount() {
        return getCompteTresorStorage().getCount();
    }

    public static List<CompteTresor> findByNumeroCompte(String numeroCompte) {
        return getCompteTresorStorage().findByNumeroCompte(numeroCompte);
    }
}
