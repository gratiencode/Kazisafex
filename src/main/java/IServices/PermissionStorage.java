/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package IServices;

import data.Permission;
import java.util.List; import java.time.LocalDateTime;

/**
 *
 * @author endeleya
 */
public interface PermissionStorage {

    public Permission savePermission(Permission d);

    public List<Permission> savePermissions(List<Permission> perms);

    public Permission updatePermission(Permission d);

    public Permission findPermission(String ui);

    public Permission findPermissionByName(String name);

    public List<Permission> findPermissions();

    public void deletePermission(Permission choosenPermission);

    public void deletePermissions(List<Permission> choosenPermission);
    
    public boolean isExists(String uid);

}
