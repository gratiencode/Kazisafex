/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.util.Date;
import java.util.List;
import java.util.Set;
import data.LigneVente;
import data.Vente;

/**
 *
 * @author eroot
 */
public interface LigneVenteStorage {
    public LigneVente createLigneVente(LigneVente obj);
    public LigneVente updateLigneVente(LigneVente obj);
    public void deleteLigneVente(LigneVente obj);
    public Long getCount();
    public LigneVente findLigneVente(Long objId);
    public List<LigneVente> findLigneVentes();
    public List<LigneVente> findLigneVentes(int start,int max);
    public Double sumProductByLot(String prodId,String numlot);

    public List<LigneVente> findByProduit(String uid);

    public List<LigneVente> findByProduitRegion(String uid, String region);

    public List<LigneVente> findByReference(Integer uid);

    public List<LigneVente> findByProduitWithLot(String uid, String numlot);

    public List<LigneVente> findByProduitWithLot(String uid, String numlot, String region);

    public double sumByProduitWithLotInUnit(String idpro, String lot);

    public List<LigneVente> findByProduitWithLot(String uid, String numlot, Date debut, Date fin);

    public List<LigneVente> findByProduitWithLot(String uid, String numlot, Date debut, Date fin, String region);

    public double sumByProduit(String uid);

    public double sumByProduit(String uid, String region);
     public List<LigneVente> mergeSet(Set<LigneVente> bulk);

    public double sumByProduit(String idpro, Date d1, Date d2);

    public double sumByProduit(String idpro, Date d1, Date d2, String region);

    public LigneVente saveLigneVente(LigneVente i, Vente vente4save);


}
