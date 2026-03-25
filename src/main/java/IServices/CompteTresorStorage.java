/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.util.List; import java.time.LocalDateTime;
import java.util.Set;
import data.CompteTresor;
import data.CompteTresor;

/**
 *
 * @author eroot
 */
public interface CompteTresorStorage {
     public CompteTresor createCompteTresor(CompteTresor obj);
    public CompteTresor updateCompteTresor(CompteTresor obj);
    public void deleteCompteTresor(CompteTresor obj);
    public Long getCount();
    public CompteTresor findCompteTresor(String objId);
    public List<CompteTresor> findCompteTresors();
    public List<CompteTresor> findCompteTresors(String region);
    public List<CompteTresor> findCompteTresors(int start,int max);
    public List<CompteTresor> findCompteTresorByNumero(String objId);
     public List<CompteTresor> findCompteTresorByBankName(String bname);
    public boolean isExists(String uid);public boolean isExists(String uid, LocalDateTime atime);
    public List<CompteTresor> findByNumeroCompte(String numeroCompte);
     public List<CompteTresor> mergeSet(Set<CompteTresor> bulk);

    public List<CompteTresor> findUnSyncedCompteTresors(long disconnected_at);
}
