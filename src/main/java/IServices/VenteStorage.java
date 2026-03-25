/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import tools.TopTen;
import java.util.HashMap;
import java.util.List; import java.time.LocalDateTime;
import java.util.Set;
import data.Vente;
import java.time.LocalDate;

/**
 *
 * @author eroot
 */
public interface VenteStorage {

    public Vente createVente(Vente obj);

    public Vente updateVente(Vente obj);

    public void deleteVente(Vente obj);

    public Vente findVente(int objId);

    public List<Vente> findVentes();

    public Long getCount();

    public List<Vente> findVentes(int start, int max);

    public Vente findVenteByRef(String objId);

    public List<Vente> findByRef(String reference, LocalDate date);

    public List<Vente> findAllByDateInterval(LocalDate time, LocalDate date2);

    public List<Vente> findAllByDateInterval(LocalDate time, LocalDate date2, String region);
    
    public double sumCdfSaleOf(LocalDate time, LocalDate date2, String region);
    
    public double sumUsdSaleOf(LocalDate time, LocalDate date2, String region);

    public List<Vente> findCreditSaleByRef(String reference);

    public List<Vente> findDraftedCarts();

    public List<Vente> findCreditSalesFromRegion(String region);

    public List<Vente> findCreditSales();

    public Double sumPayedCredit(String uid, double taux2change);

    public List<Vente> findVentes(String region);

    public List<Vente> findDraftedCarts(String region);

    public List<Vente> findByRef(String ref);

    public double sumVente(LocalDate d1, LocalDate kesho, double taux,String devise);

    public double sumVente(LocalDate d1, LocalDate kesho, String region, double taux);

    public List<TopTen> getTop10ProductDesc();

    public  List<TopTen> getTop10ProductDesc(String region);

    public Double sumCoutAchat(LocalDate date1, LocalDate date2);

    public Double sumCoutAchat(LocalDate date1, LocalDate date2, String region);

    public double sumExpenses(LocalDate date1, LocalDate date2, String region, double taux);

    public double sumCoutAchatArticleVendu(LocalDate date1, LocalDate date2, String region);

    public double sumExpenses(LocalDate date1, LocalDate date2, double taux);

    public List<Vente> mergeSet(Set<Vente> bulk);

    public double getSumVenteFor(String clientId);

    public List<Vente> findUnSyncedVentes(long disconnected_at);
    public boolean isExists(int uid);
    public boolean isExists(int uid,LocalDateTime atime);

}
