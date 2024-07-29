/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.util.List;
import java.util.Set;
import data.Fournisseur;
import data.Fournisseur;

/**
 *
 * @author eroot
 */
public interface FournisseurStorage {

    public Fournisseur createFournisseur(Fournisseur obj);

    public Fournisseur updateFournisseur(Fournisseur obj);

    public void deleteFournisseur(Fournisseur obj);

    public Long getCount();

    public Fournisseur findFournisseur(String objId);

    public List<Fournisseur> findFournisseurs();

    public List<Fournisseur> findFournisseurs(int start, int max);

    public List<Fournisseur> findByPhone(String text);
     public List<Fournisseur> mergeSet(Set<Fournisseur> bulk);
}
