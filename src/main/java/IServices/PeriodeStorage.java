/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package IServices;

import data.Periode;
import java.time.LocalDate;
import java.util.Date;
import java.util.List; import java.time.LocalDateTime;

/**
 *
 * @author endeleya
 */
public interface PeriodeStorage {
    public Periode createPeriode(Periode obj);
    public Periode updatePeriode(Periode obj);
    public void deletePeriode(Periode obj);
    public Long getCount();
    public Periode findPeriode(String objId);
    public List<Periode> findPeriodes();
    public List<Periode> findPeriodes(int start,int max);
    public List<Periode> findByProduit(String prodUid);
    public List<Periode> findNowDescByProduit(String prodUid,String comment,LocalDate debut, LocalDate fin);
    public List<Periode> findByProduit(String prodUid,String mesId);
    public List<Periode> findByProduit(String prodUid,LocalDate dateDebut,LocalDate dateFin);
    public List<Periode> findPeriodes(LocalDate dateDebut,LocalDate dateFin);
    public List<Periode> findPeriodOpened(String typeriod, LocalDate debut, LocalDate fin);
    public List<Periode> findLastClosedPeriodForProduit(String puid, String cment);
    
    public boolean isExists(String uid);public boolean isExists(String uid, LocalDateTime atime);
    
}
