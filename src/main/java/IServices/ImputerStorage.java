/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package IServices;

import data.Depense;
import data.Imputer;
import java.time.LocalDate;
import java.util.List; import java.time.LocalDateTime;

/**
 *
 * @author endeleya
 */
public interface ImputerStorage {

    public Imputer saveImputer(Imputer d);

    public Imputer updateImputer(Imputer d);

    public Imputer findImputer(String d);

    public List<Imputer> findImputers();

    public void deleteImputer(Imputer choosenImputer);

    public List<Imputer> findForProduction(String uid);

    public List<Imputer> findByDateInterval(Depense value, LocalDate value0, LocalDate value1);
    
    public boolean isExists(String uid);public boolean isExists(String uid, LocalDateTime atime);
    
}
