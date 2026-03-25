/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.util.Date;
import java.util.List; import java.time.LocalDateTime;
import java.util.Set;
import data.Facture;
import data.Facture;
import utilities.Relevee;
import utilities.Relevee;

/**
 *
 * @author eroot
 */
public interface FactureStorage {
     public Facture createFacture(Facture obj);
    public Facture updateFacture(Facture obj);
    public void deleteFacture(Facture obj);
    public Long getCount();
    public Facture findFacture(String objId);
    public List<Facture> findFactures();
    public List<Facture> findFactures(int start , int max);
    public boolean isExists(String uid);public boolean isExists(String uid, LocalDateTime atime);

    public List<Relevee> findReleveeInterval(String uid, Date toUtilDate, Date toUtilDate0);

    public List<Facture> findOrgaBills(String uid);

    public List<Facture> findUpaidBillsFor(String uid);

    public List<Facture> findBillingInInterval(Date toUtilDate, Date toUtilDate0);
     public List<Facture> mergeSet(Set<Facture> bulk);

    public List<Facture> findUnSyncedFactures(long disconnected_at);
    
}
