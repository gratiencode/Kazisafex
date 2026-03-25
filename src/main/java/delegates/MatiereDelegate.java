/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package delegates;

import IServices.MatiereStorage;
import data.Matiere;
import java.time.LocalDateTime;
import java.util.List;
import tools.ServiceLocator;
import tools.Tables;

/**
 *
 * @author endeleya
 */
public class MatiereDelegate {

   public static Matiere saveMatiere(Matiere d) {
        return getStorage().saveMatiere(d);
    }

    public static Matiere updateMatiere(Matiere d) {
        return getStorage().updateMatiere(d);
    }
    
    public static Matiere findMatiere(String d) {
        return getStorage().findMatiere(d);
    }
    
    public static List<Matiere> findMatieres() {
        return getStorage().findMatieres();
    }

    public static void removeMatiere(Matiere choosenMatiere) {
        getStorage().deleteMatiere(choosenMatiere);
    }

    public static MatiereStorage getStorage() {
        MatiereStorage cats = (MatiereStorage) ServiceLocator.getInstance().getService(Tables.MATIERE);
        return cats;
    }
    
     public static boolean isExists(String uid, LocalDateTime attime) {
        return getStorage().isExists(uid, attime);
    }

    public static boolean isExists(String uid) {
        return getStorage().isExists(uid);
    }

    
}
