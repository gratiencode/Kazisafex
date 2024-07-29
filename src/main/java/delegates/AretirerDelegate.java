/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.AretirerStorage;
import java.util.List;
import data.Aretirer;
import tools.ServiceLocator;
import tools.Tables;

/**
 * 
 * @author eroot
 */
public class AretirerDelegate {
    
    public static Aretirer saveAretirer(Aretirer cat) {
        return getStorage().createAretirer(cat);
    }

    public static Aretirer updateAretirer(Aretirer cat) {
        return getStorage().updateAretirer(cat);
    }

    public static void deleteAretirer(Aretirer cat) {
        getStorage().deleteAretirer(cat);
    }

    public static Aretirer findAretirer(String objId) {
        return getStorage().findAretirer(objId);
    }
    
    
    public static List<Aretirer> findAretirers(){
       return getStorage().findAretirer();
    }
    
    public static List<Aretirer> findAretirers(int stat,int max){
       return getStorage().findAretirer(stat,max);
    }
    
   public static AretirerStorage getStorage(){
        AretirerStorage cats=(AretirerStorage)ServiceLocator.getInstance().getService(Tables.ARETIRER);
        return cats;
    } 

   public static Long getCount() {
        return getStorage().getCount();
    }
}
