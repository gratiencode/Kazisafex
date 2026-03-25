/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package IServices;

import data.Compter;
import data.Inventaire;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.time.LocalDateTime;

/**
 *
 * @author endeleya
 */
public interface CompterStorage {

    public Compter createCompter(Compter cat);

    public Compter updateCompter(Compter cat);

    public void deleteCompter(Compter obj);

    public Compter findCompter(String uid);

    public List<Compter> findCompters();

    public Long getCount();

    public List<Compter> findCompters(LocalDate dateDebut, LocalDate dateFin);

    public List<Compter> findComptages(String inventaireId);

    public List<Compter> findComptages(String inventaireId, String region);

    public List<Compter> findCompters(LocalDate dateDebut, LocalDate dateFin, String region);

    public List<Compter> findComptageForProduit(String puid, String inventaireId);

    public List<Compter> findComptageForProduit(String puid, String inventaireId, String lot);

    public List<Compter> findComptageForProduit(String puid, String inventaireId, String lot, String region);

    public List<Compter> findUnSyncedCompter(long disconnected_at);

    public boolean isExists(String uid);

    public boolean isExists(String uid, LocalDateTime atime);

    public Compter findComptageByInventaireProduit(String iuid, String puid);
    
    public void removeNoCountedProducts(Inventaire inv);
}
