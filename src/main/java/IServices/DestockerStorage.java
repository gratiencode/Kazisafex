/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.time.LocalDate;
import java.util.List; import java.time.LocalDateTime;
import java.util.Set;
import data.Destocker;
import java.time.LocalDateTime;
/**
 *
 * @author eroot
 */
public interface DestockerStorage {
    public Destocker createDestocker(Destocker obj);
    public Destocker updateDestocker(Destocker obj);
    public void deleteDestocker(Destocker obj);
    public Long getCount();
    public Destocker findDestocker(String objId);
    public List<Destocker> findDestockers();
    public List<Destocker> findDestockers(int start,int max);
    public List<Destocker> findDestockerByProduit(String objId);
    public Double sumDestockerByProduit(String prodId);

    public List<Destocker> findDescSortedByDate(String region, int offsetd, int intValue);

    public List<Destocker> findDescSortedByDate(int offsetd, int intValue);

    public List<Destocker> findByDateIntervale(LocalDate date1, LocalDate date2);

    public List<Destocker> findByDateIntervale(LocalDate date1, LocalDate date2, String region);

    public List<Destocker> findDestockerByProduit(String uid, String region);

    public List<Destocker> findByProduitLot(String uid, String nlot);

    public List<Destocker> findByReference(String ref, String region);

    public List<Destocker> findByReference(String ref);

    public List<Destocker> findByReferenceAndProduit(String uid, String ref);

    public List<Destocker> findAscSortedByDate(String uid);
     public List<Destocker> mergeSet(Set<Destocker> bulk);

    public List<Destocker> findByReference(String ref, String uid, String numlot);

    public double sum(String uid);

    public Destocker findCustomised(String uid, String numlot, String ref, LocalDateTime dateStocker);

    public List<Destocker> findUnSyncedDestockers(long disconnected_at);
    public boolean isExists(String uid);public boolean isExists(String uid, LocalDateTime atime);

    public void removeOrphans();

}
