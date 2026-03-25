/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.PeriodeStorage;
import java.util.List;
import data.Periode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import tools.ServiceLocator;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class PeriodeDelegate {
    
    public static Periode savePeriode(Periode cat) {
        return getPeriodeStorage().createPeriode(cat);
    }

    public static Periode updatePeriode(Periode cat) {
        return getPeriodeStorage().updatePeriode(cat);
    }

    public static void deletePeriode(Periode cat) {
        getPeriodeStorage().deletePeriode(cat);
    }

    public static Periode findPeriode(String objId) {
        return getPeriodeStorage().findPeriode(objId);
    }
    
    
    public static List<Periode> findPeriodes(){
       return getPeriodeStorage().findPeriodes();
    }
    
    public static List<Periode> findPeriodes(int s,int m){
       return getPeriodeStorage().findPeriodes(s,m);
    }
    
    public static List<Periode> findPeriodeByProduit(String prod){
       return getPeriodeStorage().findByProduit(prod);
    }
    
    public static List<Periode> findOpenedPeriodByProduit(String prod,String comment,LocalDate debut, LocalDate fin){
       return getPeriodeStorage().findNowDescByProduit(prod,comment,debut,fin);
    }
    
    public static List<Periode> findPeriodeByProduitInRegion(String prod,String region){
       return getPeriodeStorage().findByProduit(prod,region);
    }
    
    
    public static PeriodeStorage getPeriodeStorage(){
        PeriodeStorage cats=(PeriodeStorage)ServiceLocator.getInstance().getService(Tables.PERIODE);
        return cats;
    }

    public static List<Periode> findByProduit(String uid,LocalDate d1, LocalDate d2) {
       return getPeriodeStorage().findByProduit(uid, d1, d2);
    }

   

    public static Long getCount() {
        return getPeriodeStorage().getCount();
    }

    public static List<Periode> findByLocalDates(LocalDate dd, LocalDate df) {
       return getPeriodeStorage().findPeriodes(dd, df);
    }

    public static List<Periode> findPeriodOpened(String typeriod,LocalDate debut, LocalDate fin) {
    return getPeriodeStorage().findPeriodOpened(typeriod,debut,fin); 
    }

    public static List<Periode> findLastClosedPeriodForProduit(String puid, String cment) {
      return getPeriodeStorage().findLastClosedPeriodForProduit(puid,cment);
    }

    
     public static boolean isExists(String uid, LocalDateTime attime) {
        return getPeriodeStorage().isExists(uid, attime);
    }

    public static boolean isExists(String uid) {
        return getPeriodeStorage().isExists(uid);
    }
    
    
}
