/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.CategoryStorage;
import java.util.List;
import java.util.Set;
import data.Category;
import java.time.LocalDateTime;
import tools.ServiceLocator;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class CategoryDelegate {
    
    public static Category saveCategory(Category cat){
       return getCategoryStorage().createCategory(cat);
    }
    
     public static Category updateCategory(Category cat) {
        return getCategoryStorage().updateCategory(cat);
    }

    public static void deleteCategory(Category cat) {
        getCategoryStorage().deleteCategory(cat);
    }

    public static Category findCategory(String objId) {
        return getCategoryStorage().findCategory(objId);
    }
    
    
    public static List<Category> findCategories(){
       return getCategoryStorage().findCategories();
    }
    
    public static List<Category> findCategories(int s,int m){
       return getCategoryStorage().findCategories(s,m);
    }
    
    public static Category findCategoryByDescription(String catId){
       return getCategoryStorage().findCategoryByDescription(catId);
    }
    
    
    public static CategoryStorage getCategoryStorage(){
        CategoryStorage cats=(CategoryStorage)ServiceLocator.getInstance().getService(Tables.CATEGORY);
        return cats;
    }
    
    public static List<Category> setCategories(List<Category> c){
        return getCategoryStorage().setCategories(c);
    }
    
     public static List<Category> updateCategories(Set<Category> c){
        return getCategoryStorage().updateCategorySet(c);
    }

    public static Long findCount() {
      return getCategoryStorage().getCount();
    }

    public static List<Category> findCategories(String divers) {
        return getCategoryStorage().findCategories(divers);
    }

    public static List<Category> findUnSyncedCategories(long disconnected_at) {
         return getCategoryStorage().findUnSyncedCategories(disconnected_at);
    }
    
     public static boolean isExists(String uid, LocalDateTime attime) {
        return getCategoryStorage().isExists(uid, attime);
    }

    public static boolean isExists(String uid) {
        return getCategoryStorage().isExists(uid);
    }

}
