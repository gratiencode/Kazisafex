/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package delegates;

import IServices.InventaireStorage;
import data.Inventaire;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import tools.ServiceLocator;
import tools.Tables;

/**
 *
 * @author endeleya
 */
public class InventaireDelegate {

    public static InventaireStorage getStorage() {
        InventaireStorage cats = (InventaireStorage) ServiceLocator.getInstance().getService(Tables.INVENTORY);
        return cats;
    }

    public static Inventaire createInventaire(Inventaire c) {
        return getStorage().createInventaire(c);
    }

    public static Inventaire updateInventaire(Inventaire c) {
        return getStorage().updateInventaire(c);
    }

    public static Inventaire findInventaire(String uid) {
        return getStorage().findInventaire(uid);
    }

     public static Inventaire findInventaireByCode(String code) {
        return getStorage().findInventaireByCode(code);
    }

    
    public static void deleteInventaire(Inventaire c) {
        getStorage().deleteInventaire(c);
    }

    public static List<Inventaire> findInventaires() {
        return getStorage().findInventaires();
    }
    
     public static List<Inventaire> findInventaires(String region) {
        return getStorage().findInventaires(region);
    }

    public static List<Inventaire> findInventaires(Date dateDebut, Date dateFin) {
        return getStorage().findInventaires(dateDebut, dateFin);
    }

    public static List<Inventaire> findInventaires(Date dateDebut, Date dateFin, String region) {
        return getStorage().findInventaires(dateDebut, dateFin, region);
    }

    public static List<Inventaire> findUnSyncedInventories(long lastSession) {
        return getStorage().findUnSyncedInventaires(lastSession);
    }

    public static boolean isExists(String uid, LocalDateTime updatedAt) {
        return getStorage().isExists(uid, updatedAt);
    }

    public static boolean isExists(String uid) {
        return getStorage().isExists(uid);
    }

    public static Inventaire findLastInventory() {
        return getStorage().findLastInventaire();
    }
    
    public static List<Inventaire> findNonClosed(){
        return getStorage().findNonClosed();
    }

}
