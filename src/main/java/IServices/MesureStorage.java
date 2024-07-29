/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.util.List;
import java.util.Set;
import data.Mesure;

/**
 *
 * @author eroot
 */
public interface MesureStorage {
     public Mesure createMesure(Mesure obj);
    public Mesure updateMesure(Mesure obj);
    public void deleteMesure(Mesure obj);
    public Long getCount();
    public Mesure findMesure(String objId);
    public List<Mesure> findMesures();
    public List<Mesure> findMesures(int start,int max);
    public List<Mesure> findByProduit(String prodUid);
    public List<Mesure> findByProduit(String prodUid,String desc);
    public List<Mesure> findAscSortedByQuantWithProduit(String uid);

    public Mesure findMaxMesureByProduit(String uid);

    public Mesure findByProduitAndQuant(String uid, Double quantContenu);
    
     public List<Mesure> mergeSet(Set<Mesure> bulk);
    
}
