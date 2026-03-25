/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package IServices;

import data.Entreposer;
import java.time.LocalDate;
import java.util.List; import java.time.LocalDateTime;

/**
 *
 * @author endeleya
 */
public interface EntreposerStorage {

    public Entreposer saveEntreposer(Entreposer d);

    public Entreposer updateEntreposer(Entreposer d);

    public Entreposer findEntreposer(String d);

    public List<Entreposer> findEntreposers();

    public void deleteEntreposer(Entreposer choosenEntreposer);

    public double sumValueStockMP();

    public List<Entreposer> findEntreposersGroupedByIntrant();

    public List<Entreposer> findEntreposerByLevel(String mp);

    public double findSommeEntree(String uid);

    public List<Entreposer> toFefoOrdering(String matiereId);

    public List<Entreposer> toFifoOrdering(String matiereId);

    public List<Entreposer> toLifoOrdering(String matiereId);
    
     public List<Entreposer> toFefoOrderingProd(String matiereId);

    public List<Entreposer> toFifoOrderingProd(String matiereId);

    public List<Entreposer> toLifoOrderingProd(String matiereId);
    
    public List<Entreposer> toFefoOrdering(String matiereId,String region);

    public List<Entreposer> toFifoOrdering(String matiereId,String region);

    public List<Entreposer> toLifoOrdering(String matiereId,String region);

    public List<Entreposer> findEntreposerByLevel(LocalDate value, LocalDate value0, String MANUFACTURING_LEVEL_RAW_MATERIAL);

    public List<Entreposer> findEntreposersGroupedByIntrant(LocalDate value, LocalDate value0);
    
    public List<Entreposer> findFinishedProduction();

    public List<Entreposer> findProdEntreposers(String uid, String numlot);

    public List<Entreposer> findProdEntreposers(String prouid);

    public List<Entreposer> findEntreposersGroupedByProd(LocalDate value, LocalDate value0);

    public List<Entreposer> findByProduction(String uid);
    
    public boolean isExists(String uid);public boolean isExists(String uid, LocalDateTime atime);
    
}
