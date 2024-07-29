/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.util.List;
import java.util.Set;
import data.Traisorerie;

/**
 *
 * @author eroot
 */
public interface TraisorerieStorage {
    public Traisorerie createTraisorerie(Traisorerie obj);
    public Traisorerie updateTraisorerie(Traisorerie obj);
    public void deleteTraisorerie(Traisorerie obj);
    public Long getCount();
    public Traisorerie findTraisorerie(String objId);
    public List<Traisorerie> findTraisoreries();
    public List<Traisorerie> findTraisoreries(String region);
    public List<Traisorerie> findTraisoreries(int start,int max);
    public List<Traisorerie> findTraisorerieByCompteTresor(String objId); 
    public List<Traisorerie> findTraisorerieByCompteTresor(String objId,String typeCpte); 
    public List<Traisorerie> findTraisorerieByCompteTresOR(String objId,String typeCpte); 
    public List<Traisorerie> findByReference(String ref);
    public Double sumByReference(String ref,double taux);
     public List<Traisorerie> mergeSet(Set<Traisorerie> bulk);
}
