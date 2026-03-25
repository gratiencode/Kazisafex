/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package IServices;

import data.Production;
import data.Produit;
import java.time.LocalDate;
import java.util.List; import java.time.LocalDateTime;

/**
 *
 * @author endeleya
 */
public interface ProductionStorage {

    public Production saveProduction(Production d);

    public Production updateProduction(Production d);

    public Production findProduction(String d);

    public List<Production> findProductions();

    public void deleteProduction(Production choosenProduction);

    public List<Production> findProductionByProduitLot(String lot, String uid);

    public List<Production> findForProduct(Produit p, LocalDate value, LocalDate value0);
    
    public boolean isExists(String uid);public boolean isExists(String uid, LocalDateTime atime);

}
