/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import data.Vente;

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
    public List<Vente> findVentes(int start,int max);
    public Vente findVenteByRef(String objId); 
    public List<Vente> findByRef(String reference, Date date);
    public List<Vente> findAllByDateInterval(Date time, Date date2);
    public List<Vente> findAllByDateInterval(Date time, Date date2,String region);
    public List<Vente> findCreditSaleByRef(String reference);
    public List<Vente> findDraftedCarts();
    public List<Vente> findCreditSalesFromRegion(String region);
    public List<Vente> findCreditSales();
    public Double sumPayedCredit(String uid, double taux2change);
    public List<Vente> findVentes(String region);
    public List<Vente> findDraftedCarts(String region); 
    public List<Vente> findByRef(String ref);
    public double sumVente(Date d1, Date kesho, double taux);
    public double sumVente(Date d1, Date kesho, String region, double taux);

    public HashMap<Long, String> getTop10ProductDesc();

    public HashMap<Long, String> getTop10ProductDesc(String region);

    public double sumExpenses(Date date1, Date date2, String region, double taux);
    public double sumCoutAchatArticleVendu(Date date1, Date date2, String region);
    public double sumExpenses(Date date1, Date date2, double taux);
     public List<Vente> mergeSet(Set<Vente> bulk);

}
