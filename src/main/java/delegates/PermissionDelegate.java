/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package delegates;

import IServices.PermissionStorage;
import data.Permission;
import data.PermitTo;
import java.util.List;
import tools.ServiceLocator;
import tools.Tables;

/**
 *
 * @author endeleya
 */
public class PermissionDelegate {

    public static PermissionStorage getStorage() {
        PermissionStorage cats = (PermissionStorage) ServiceLocator.getInstance().getService(Tables.PERMISSION);
        return cats;
    }

    public static Permission savePermission(Permission e) {
        return getStorage().savePermission(e);
    }

    public static List<Permission> savePermissions(List<Permission> l) {
        return getStorage().savePermissions(l);
    }

    public static List<Permission> renewPermissions(List<Permission> l) {
        List<Permission> lst = findPermissions();
        if (!lst.isEmpty()) {
            deletePermissions(lst);
        }
        return getStorage().savePermissions(l);
    }

    public static List<Permission> findPermissions() {
        return getStorage().findPermissions();
    }

    public static Permission updatePermission(Permission e) {
        return getStorage().updatePermission(e);
    }

    public static boolean hasPermission(PermitTo p) {
        return getStorage().isExists(p.name());
    }

    public static void deletePermission(Permission e) {
        getStorage().deletePermission(e);
    }

    public static void deletePermissions(List<Permission> items) {
        getStorage().deletePermissions(items);
    }

    public static Permission findPermission(String uid) {
        return getStorage().findPermission(uid);
    }

    public static Permission findPermissionByName(String name) {
        return getStorage().findPermissionByName(name);
    }
}
