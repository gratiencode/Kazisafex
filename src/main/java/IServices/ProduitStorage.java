/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.util.List;
import java.util.Set;
import data.Produit;

/**
 *
 * @author eroot
 */
public interface ProduitStorage {

    public Produit createProduit(Produit obj);

    public Produit updateProduit(Produit obj);

    public void deleteProduit(Produit obj);

    public Produit findByBarcode(String codebar);

    public Produit findProduit(String objId);

    public List<Produit> findProduits();

    public Long getCount();

    public List<Produit> findProduits(int start, int max);

    public List<Produit> findProduitByCategory(String objId);

    public List<Produit> findAllByCodebar(String codebarr);

    public List<Produit> mergeSet(Set<Produit> bulk);

    public List<Produit> findByDescription(String nomProduit, String marque, String modele, String taille);

    public List<Produit> findProduitByName(String query);

}
