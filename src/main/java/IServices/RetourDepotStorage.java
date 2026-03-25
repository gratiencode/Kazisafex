/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.util.List; import java.time.LocalDateTime;
import java.util.Set;
import data.RetourDepot;

/**
 *
 * @author eroot
 */
public interface RetourDepotStorage {
    public RetourDepot createRetourDepot(RetourDepot obj);
    public RetourDepot updateRetourDepot(RetourDepot obj);
    public void deleteRetourDepot(RetourDepot obj);
    public Long getCount();
    public RetourDepot findRetourDepot(String objId);
    public List<RetourDepot> findRetourDepots();
    public List<RetourDepot> findRetourDepots(int start,int max);
     public List<RetourDepot> mergeSet(Set<RetourDepot> bulk);

    public List<RetourDepot> findUnSyncedRetourDepots(long disconnected_at);
    public boolean isExists(String uid);public boolean isExists(String uid, LocalDateTime atime);

}
