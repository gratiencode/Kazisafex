/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package IServices;

import data.Satisfaire;
import java.util.List; import java.time.LocalDateTime;

/**
 *
 * @author endeleya
 */
public interface SatisfaireStorage {

    public Satisfaire saveSatisfaire(Satisfaire d);

    public Satisfaire updateSatisfaire(Satisfaire d);

    public Satisfaire findSatisfaire(String d);

    public List<Satisfaire> findSatisfaires();

    public void deleteSatisfaire(Satisfaire choosenSatisfaire);
    public boolean isExists(String uid);public boolean isExists(String uid, LocalDateTime atime);
    
}
