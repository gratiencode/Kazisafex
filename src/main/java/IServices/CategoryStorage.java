/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.util.List; 
import java.util.Set;
import data.Category;
import java.time.LocalDateTime;

/**
 *
 * @author eroot
 */
public interface CategoryStorage {
    public Category createCategory(Category cat);
    public Category updateCategory(Category cat);
    public void deleteCategory(Category cat);
    public Long getCount();
    public Category findCategory(String catId);
    public List<Category> findCategories();
    public List<Category> findCategories(int start,int max);
    public Category findCategoryByDescription(String catId);
    public List<Category> setCategories(List<Category> c);
    public List<Category> updateCategorySet(Set<Category> lc);
    public List<Category> findCategories(String divers);
    public boolean isExists(String uid);
    public boolean isExists(String uid, LocalDateTime atime);
    public List<Category> findUnSyncedCategories(long disconnected_at);
}
