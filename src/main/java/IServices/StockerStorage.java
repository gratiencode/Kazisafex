/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Set;
import data.Stocker;

/**
 *
 * @author eroot
 */
public interface StockerStorage {
     public Stocker createStocker(Stocker obj);
    public Stocker updateStocker(Stocker obj);
    public void deleteStocker(Stocker obj);
    public Long getCount();
    public Stocker findStocker(String objId);
    public List<Stocker> findStockers();
    public List<Stocker> findStockers(int start,int max);
     public List<Stocker> findStockerByProduit(String objId);
     public List<Stocker> findStockerByProduitLot(String objId,String lot);
     public Double sumByProduit(String idPro);

    public List<Stocker> findStockerByLivraison(String uid);

    public List<Stocker> findByDateIntervale(LocalDate date1, LocalDate date2);

    public List<Stocker> findByDateIntervale(LocalDate date1, LocalDate date2, String region);

    public List<Stocker> findAscSortedByDateExpir(String uid);

    public List<Stocker> findAscSortedByDateExpir(String uid, String region);

    public List<Stocker> findDescSortedByDateStock(String prouid);

    public List<Stocker> findAscSortedByDateStock(String prouid);

    public List<Stocker> findStockerByLivrAndProduit(String livuid, String prouid0);

    public List<Stocker> findByDateExpInterval(Date time, Date darg);

    public List<Stocker> findStockerByProduit(String pid, String region);

    public List<Stocker> findStockers(String region, int s, int m);

    public List<Stocker> findStockers(String region);

    public List<Stocker> findDescSortedByDateStock(String uid, String region);
    
     public List<Stocker> mergeSet(Set<Stocker> bulk);

    public List<Stocker> toFefoOrdering(String uid);

    public List<Stocker> toFifoOrdering(String uid);

    public List<Stocker> toLifoOrdering(String uid);

    public double sum(String uid);

}
