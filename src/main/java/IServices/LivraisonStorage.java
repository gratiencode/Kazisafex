/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.util.List; import java.time.LocalDateTime;
import java.util.Set;
import data.Livraison;
import data.Livraison;

/**
 *
 * @author eroot
 */
public interface LivraisonStorage {
     public Livraison createLivraison(Livraison obj);
    public Livraison updateLivraison(Livraison obj);
    public void deleteLivraison(Livraison obj);
    
    public Livraison findLivraison(String objId);
    public List<Livraison> findLivraisons();
    public Long getCount();
    public List<Livraison> findLivraisons(int start,int max);
    public List<Livraison> findLivraisonBySupplier(String objId);
    public boolean isExists(String uid);public boolean isExists(String uid, LocalDateTime atime);
    
    public List<Livraison> findLivraisonByReference(String ref);

    public List<Livraison> findDescSortedByDate(String region, int offset, int intValue);

    public List<Livraison> findDescSortedByDate(int offset, int intValue);

    public List<Livraison> findDescSortedByDate();

    public List<Livraison> findDescSortedByDate(String region);

    public Double sumBySupplier(String uid);
     public List<Livraison> mergeSet(Set<Livraison> bulk);

    public List<Livraison> findUnSyncedLivraisons(long disconnected_at);
    
}
