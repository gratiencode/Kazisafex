/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import data.DepenseAgregate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 *
 * @author eroot
 */
public interface DepenseAgregateStorage {
    
    public DepenseAgregate createDepenseAgregate(DepenseAgregate cat);

    public DepenseAgregate updateDepenseAgregate(DepenseAgregate cat);

    public void deleteDepenseAgregate(DepenseAgregate cat);

    public DepenseAgregate findDepenseAgregate(String catId);

    public List<DepenseAgregate> findDepenseAgregates();

    public List<DepenseAgregate> findDepenseAgregates(int start, int max);

    public List<DepenseAgregate> findDepenseAgregates(String region);
    
    public List<DepenseAgregate> findDepenseAgregates(LocalDateTime date, String imputation);

    public Long getCount();

    public List<DepenseAgregate> mergeSet(Set<DepenseAgregate> bulk);

    public List<DepenseAgregate> findUnSyncedDepenseAgregates(long disconnected_at);

    public boolean isExists(String uid, LocalDateTime atime);

    public boolean isExists(String uid);
}
