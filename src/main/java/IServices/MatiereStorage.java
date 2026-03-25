/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package IServices;

import data.Matiere;
import java.util.List; import java.time.LocalDateTime;

/**
 *
 * @author endeleya
 */
public interface MatiereStorage {

    public Matiere saveMatiere(Matiere d);

    public Matiere updateMatiere(Matiere d);

    public Matiere findMatiere(String d);

    public List<Matiere> findMatieres();

    public void deleteMatiere(Matiere choosenMatiere);
    
    public boolean isExists(String uid);public boolean isExists(String uid, LocalDateTime atime);
    
}
