/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package IServices;

import data.MatiereSku;
import java.util.List; import java.time.LocalDateTime;

/**
 *
 * @author endeleya
 */
public interface MatiereSkuStorage {

    public MatiereSku saveMatiereSku(MatiereSku d);

    public MatiereSku updateMatiereSku(MatiereSku d);

    public MatiereSku findMatiereSku(String d);

    public List<MatiereSku> findMatiereSkus();
    
    public boolean isExists(String uid);public boolean isExists(String uid, LocalDateTime atime);

    public void deleteMatiereSku(MatiereSku choosenMatiereSku);

    public List<MatiereSku> findMatiereSkuFor(String uid);

    public MatiereSku findMatiereSku(String name, double q, String uid);

    public MatiereSku findMatiereSku(String txt, String uid);
    
}
