/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.FournisseurStorage;
import java.util.List;
import data.Fournisseur;
import tools.ServiceLocator;
import tools.Tables;
import static delegates.FournisseurDelegate.getStorage;
import java.util.Set;

/**
 *
 * @author eroot
 */
public class FournisseurDelegate {
    public static Fournisseur saveFournisseur(Fournisseur cat) {
        return getStorage().createFournisseur(cat);
    }

    public static Fournisseur updateFournisseur(Fournisseur cat) {
        return getStorage().updateFournisseur(cat);
    }

    public static void deleteFournisseur(Fournisseur cat) {
        getStorage().deleteFournisseur(cat);
    }

    public static Fournisseur findFournisseur(String objId) {
        return getStorage().findFournisseur(objId);
    }
    
    
    public static List<Fournisseur> findFournisseurs(){
       return getStorage().findFournisseurs();
    }
    
      public static List<Fournisseur> findFournisseurs(int s,int m){
       return getStorage().findFournisseurs(s,m);
    }
    
    public static FournisseurStorage getStorage(){
        FournisseurStorage cats=(FournisseurStorage)ServiceLocator.getInstance().getService(Tables.FOURNISSEUR);
        return cats;
    }

    public static List<Fournisseur> findByPhone(String text) {
       return getStorage().findByPhone(text);
    }

    public static Long getCount() {
        return getStorage().getCount();
    }

    public static List<Fournisseur> mergeSet(Set<Fournisseur> fs) {
       return getStorage().mergeSet(fs);
    }
}
