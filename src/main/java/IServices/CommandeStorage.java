/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package IServices;

import data.Commande;
import java.util.List; import java.time.LocalDateTime;

/**
 *
 * @author endeleya
 */
public interface CommandeStorage {

    public Commande saveCommande(Commande d);

    public Commande updateCommande(Commande d);

    public Commande findCommande(String d);

    public List<Commande> findCommandes();

    public void deleteCommande(Commande choosenCommande);
    
    public boolean isExists(String uid);public boolean isExists(String uid, LocalDateTime atime);
    
}
