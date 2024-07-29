/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.util.List;
import java.util.Set;
import data.PrixDeVente;
import data.Mesure;
import data.PrixDeVente;
import data.Recquisition;

/**
 *
 * @author eroot
 */
public interface PrixDeVenteStorage {
    public PrixDeVente createPrixDeVente(PrixDeVente obj);
    public PrixDeVente updatePrixDeVente(PrixDeVente obj);
    public void deletePrixDeVente(PrixDeVente obj);
    public Long getCount();
    public PrixDeVente findPrixDeVente(String objId);
    public List<PrixDeVente> findPrixDeVentes();
    public List<PrixDeVente> findPrixDeVentes(int start,int max);

    public List<PrixDeVente> findPricesForRecq(String uid);

    public List<PrixDeVente> findSpecificByQuant(Recquisition choosenRecquisition, Mesure choosenmez, double quant);

    public List<PrixDeVente> findDescSortedByRecqWithMesureByPrice(String req, String uid);

    public List<PrixDeVente> findDescOrderdByPriceForRecq(String req);

    public void startTransaction();

    public void commitTransaction();

    public PrixDeVente addToTransaction(PrixDeVente lpv);
     public List<PrixDeVente> mergeSet(Set<PrixDeVente> bulk);

    public List<PrixDeVente> findPrixDeVente(Double qmin, String uid, String uid0);
}
