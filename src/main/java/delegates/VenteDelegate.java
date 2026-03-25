/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.VenteStorage;
import java.util.List;
import data.Vente;
import tools.TopTen;
import tools.ServiceLocator;
import tools.Tables;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import services.VenteService;

/**
 *
 * @author eroot
 */
public class VenteDelegate {

    public static Vente saveVente(Vente cat) {
        return getStorage().createVente(cat);
    }

    public static Vente updateVente(Vente cat) {
        return getStorage().updateVente(cat);
    }

    public static void deleteVente(Vente cat) {
        getStorage().deleteVente(cat);
    }

    public static Vente findVente(int objId) {
        return getStorage().findVente(objId);
    }

    public static List<Vente> findVentes() {
        return getStorage().findVentes();
    }

    public static List<Vente> findVentes(int s, int m) {
        return getStorage().findVentes(s, m);
    }

    public static VenteStorage getStorage() {
        VenteStorage cats = (VenteStorage) ServiceLocator.getInstance().getService(Tables.VENTE);
        return cats;
    }

    public static List<Vente> findByRef(String reference, LocalDate date) {
        return getStorage().findByRef(reference, date);
    }

    public static List<Vente> findAllByDateInterval(LocalDate time, LocalDate date2) {
        return getStorage().findAllByDateInterval(time, date2);
    }

    public static List<Vente> findCreditSaleByRef(String reference) {
        return getStorage().findCreditSaleByRef(reference);
    }

    public static List<Vente> findDraftedCarts() {
        return getStorage().findDraftedCarts();
    }

    public static List<Vente> findCreditSalesFromRegion(String region) {
        return getStorage().findCreditSalesFromRegion(region);
    }

    public static List<Vente> findCreditSales() {
        return getStorage().findCreditSales();
    }

    public static Double sumPayedCredit(String uid, double taux2change) {
        return getStorage().sumPayedCredit(uid, taux2change);
    }

    public static List<Vente> findVentes(String region) {
        return getStorage().findVentes(region);
    }

    public static List<Vente> findAllByDateInterval(LocalDate toUtilDate, LocalDate toUtilDate0, String region) {
        return getStorage().findAllByDateInterval(toUtilDate, toUtilDate0, region);
    }

    public static Long getCount() {
        return getStorage().getCount();
    }

    public static List<Vente> findDraftedCarts(String region) {
        return getStorage().findDraftedCarts(region);
    }

    public static List<Vente> findByRef(String ref) {
        return getStorage().findByRef(ref);
    }

    public static double sumVente(LocalDate d1, LocalDate kesho, double taux, String devise) {
        return getStorage().sumVente(d1, kesho, taux, devise);
    }

    public static double sumVente(LocalDate d1, LocalDate kesho, String region, double taux) {
        return getStorage().sumVente(d1, kesho, region, taux);// Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public static List<TopTen> getTop10ProductDesc() {
        return getStorage().getTop10ProductDesc();
    }

    public static List<TopTen> getTop10ProductDesc(String region) {
        return getStorage().getTop10ProductDesc(region);
    }

    public static double sumExpenses(LocalDate date1, LocalDate date2, String region, double taux) {
        return getStorage().sumExpenses(date1, date2, region, taux);
    }

    public static double sumCoutAchatArticleVendu(LocalDate date1, LocalDate date2, String region) {
        return getStorage().sumCoutAchat(date1, date2, region);
    }

    public static double sumCoutAchatArticleVendu(LocalDate date1, LocalDate date2) {
        return getStorage().sumCoutAchat(date1, date2);
    }

    public static double sumExpenses(LocalDate date1, LocalDate date2, double taux) {
        return getStorage().sumExpenses(date1, date2, taux);
    }

    public static List<Vente> mergeSet(Set<Vente> ventes) {
        return getStorage().mergeSet(ventes);
    }

    public static double getVenteFor(String clientId) {
        return getStorage().getSumVenteFor(clientId);
    }

    public static List<Vente> getVentes() {
        return VenteService.getVentes();
    }

    public static List<Vente> findUnSyncedVentes(long disconnected_at) {
        return getStorage().findUnSyncedVentes(disconnected_at);
    }

    public static boolean isExists(int uid, LocalDateTime attime) {
        return getStorage().isExists(uid, attime);
    }

    public static boolean isExists(int uid) {
        return getStorage().isExists(uid);
    }

    public static double sumUsdSaleOf(LocalDate time, LocalDate date2, String region){
        return getStorage().sumUsdSaleOf(time, date2, region);
    }
    
    public static double sumCdfSaleOf(LocalDate time, LocalDate date2, String region){
        return getStorage().sumCdfSaleOf(time, date2, region);
    }

}
