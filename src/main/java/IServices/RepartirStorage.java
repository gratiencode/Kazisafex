/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package IServices;

import data.Repartir;
import java.util.List; import java.time.LocalDateTime;

/**
 *
 * @author endeleya
 */
public interface RepartirStorage {

    public Repartir saveRepartir(Repartir d);

    public Repartir updateRepartir(Repartir d);

    public Repartir findRepartir(String d);

    public List<Repartir> findRepartirs();

    public void deleteRepartir(Repartir choosenRepartir);

    public double findSommeRepartir(String uid);

    public List<Repartir> findForProduction(String uid);
    
    public boolean isExists(String uid);public boolean isExists(String uid, LocalDateTime atime);
    
}
