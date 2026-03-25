/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package IServices;

import data.Inventaire;
import java.util.Date;
import java.util.List; import java.time.LocalDateTime;

/**
 *
 * @author endeleya
 */
public interface InventaireStorage {

    public Inventaire createInventaire(Inventaire cat);

    public Inventaire updateInventaire(Inventaire cat);

    public void deleteInventaire(Inventaire obj);

    public Inventaire findInventaire(String uid);

    public List<Inventaire> findInventaires();
    
    public List<Inventaire> findInventaires(String region);

    public Long getCount();

    public List<Inventaire> findInventaires(Date dateDebut, Date dateFin);

    public List<Inventaire> findInventaires(Date dateDebut, Date dateFin,String region);
    
    public List<Inventaire> findUnSyncedInventaires(long disconnected_at);
    
    public boolean isExists(String uid);
    
    public boolean isExists(String uid, LocalDateTime atime);

    public Inventaire findInventaireByCode(String code);

    public Inventaire findLastInventaire();
    
    public List<Inventaire> findNonClosed();

}
