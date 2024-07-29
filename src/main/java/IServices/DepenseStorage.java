/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.util.List;
import java.util.Set;
import data.Depense;
import data.Depense;

/**
 *
 * @author eroot
 */
public interface DepenseStorage {
     public Depense createDepense(Depense obj);
    public Depense updateDepense(Depense obj);
    public void deleteDepense(Depense obj);
    public Long getCount();
    public Depense findDepense(String objId);
    public List<Depense> findDepenses();
    public List<Depense> findDepenses(int start ,int max);
    public List<Depense> findDepenseByDescription(String objId);
    public List<Depense> findDepenses(String region);
     public List<Depense> mergeSet(Set<Depense> bulk);
}
