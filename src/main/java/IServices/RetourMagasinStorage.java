/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.util.List;
import java.util.Set;
import data.RetourMagasin;

/**
 *
 * @author eroot
 */
public interface RetourMagasinStorage {
     public RetourMagasin createRetourMagasin(RetourMagasin obj);
    public RetourMagasin updateRetourMagasin(RetourMagasin obj);
    public void deleteRetourMagasin(RetourMagasin obj);
    public Long getCount();
    public RetourMagasin findRetourMagasin(String objId);
    public List<RetourMagasin> findRetourMagasins();
    public List<RetourMagasin> findRetourMagasins(int start,int max);
    public long getCountForVente(String uid);

    public List<RetourMagasin> findByLigneVente(Long uid);
     public List<RetourMagasin> mergeSet(Set<RetourMagasin> bulk);
}
