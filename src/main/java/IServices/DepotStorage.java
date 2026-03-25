/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package IServices;

import data.Depot;
import java.util.List; import java.time.LocalDateTime;

/**
 *
 * @author endeleya
 */
public interface DepotStorage {

    public Depot saveDepot(Depot d);

    public Depot updateDepot(Depot d);

    public void deleteDepot(Depot choosenDepot);

    public Depot findDepot(String d);

    public List<Depot> findDepots();
    
    public boolean isExists(String uid);public boolean isExists(String uid, LocalDateTime atime);
    
}
