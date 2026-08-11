package services.utils;

import data.Permission;
import data.PermitTo;
import delegates.PermissionDelegate;
import java.util.List;
import java.util.prefs.Preferences;
import tools.MainUI;

/**
 * Service centralisé et unifié de gestion des permissions.
 * Combine de manière transparente les rôles à privilèges élevés (ALL_ACCESS, Trader)
 * et les permissions enregistrées en base de données localement.
 * 
 * Exemples d'utilisation :
 *  - PermissionRegistry.has(PermitTo.CREATE_INVENTORY)
 *  - PermissionRegistry.hasAny(PermitTo.CREATE_CLIENT, PermitTo.UPDATE_CLIENT)
 *  - PermissionRegistry.canExecute(PermitTo.DELETE_VENTE, () -> deleteVente())
 *  - PermissionRegistry.checkOrNotify(PermitTo.UPDATE_PRIX, () -> updatePrix())
 */
public final class PermissionRegistry {

    private PermissionRegistry() {
    }

    /**
     * Alias court pour vérifier une permission.
     */
    public static boolean has(PermitTo permit) {
        return hasPermission(permit);
    }

    /**
     * Alias court avec instance spécifique de Preferences.
     */
    public static boolean has(Preferences pref, PermitTo permit) {
        return hasPermission(pref, permit);
    }

    /**
     * Vérifie si l'utilisateur courant possède l'autorisation pour la tâche spécifiée.
     */
    public static boolean hasPermission(PermitTo permit) {
        return hasPermission(null, permit);
    }

    /**
     * Vérifie l'autorisation en utilisant une instance spécifique de Preferences.
     */
    public static boolean hasPermission(Preferences pref, PermitTo permit) {
        if (permit == null) {
            return false;
        }
        if (UserRoleRegistry.hasAllAccess(pref) || UserRoleRegistry.isTrader(pref)) {
            return true;
        }
        return hasExplicitPermission(permit);
    }

    /**
     * Indique si l'utilisateur possède un accès global (ALL_ACCESS ou Trader).
     * Utile pour déterminer la portée régionale sans tester une permission d'action.
     */
    public static boolean hasGlobalAccess() {
        return UserRoleRegistry.hasAllAccess() || UserRoleRegistry.isTrader();
    }

    /**
     * Indique si l'utilisateur possède un accès global (ALL_ACCESS ou Trader)
     * en utilisant une instance spécifique de Preferences.
     */
    public static boolean hasGlobalAccess(java.util.prefs.Preferences pref) {
        return UserRoleRegistry.hasAllAccess(pref) || UserRoleRegistry.isTrader(pref);
    }

    /**
     * Vérifie si l'utilisateur possède au moins UNE des permissions spécifiées (OU logique).
     */
    public static boolean hasAny(PermitTo... permits) {
        if (permits == null || permits.length == 0) {
            return false;
        }
        for (PermitTo permit : permits) {
            if (hasPermission(permit)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vérifie si l'utilisateur possède TOUTES les permissions spécifiées (ET logique).
     */
    public static boolean hasAll(PermitTo... permits) {
        if (permits == null || permits.length == 0) {
            return false;
        }
        for (PermitTo permit : permits) {
            if (!hasPermission(permit)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Vérifie si la permission est explicitement présente en base de données.
     */
    public static boolean hasExplicitPermission(PermitTo permit) {
        if (permit == null) {
            return false;
        }
        try {
            return PermissionDelegate.hasPermission(permit);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Vérification de sécurité par nom exact de la permission.
     */
    public static boolean hasPermissionByName(String permitName) {
        if (permitName == null || permitName.isBlank()) {
            return false;
        }
        if (UserRoleRegistry.hasAllAccess() || UserRoleRegistry.isTrader()) {
            return true;
        }
        try {
            return PermissionDelegate.findPermissionByName(permitName) != null;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Exécute une tâche si l'utilisateur est autorisé.
     * @return true si l'action a été exécutée, false sinon.
     */
    public static boolean canExecute(PermitTo permit, Runnable action) {
        return canExecute(permit, action, null);
    }

    /**
     * Exécute l'action de succès si l'utilisateur a la permission, sinon exécute l'action de refus.
     */
    public static boolean canExecute(PermitTo permit, Runnable actionOnSuccess, Runnable actionOnDenied) {
        if (hasPermission(permit)) {
            if (actionOnSuccess != null) {
                actionOnSuccess.run();
            }
            return true;
        } else {
            if (actionOnDenied != null) {
                actionOnDenied.run();
            }
            return false;
        }
    }

    /**
     * Vérifie si la permission est accordée pour exécuter la tâche.
     * Si l'accès est refusé, affiche automatiquement une alerte utilisateur dans le UI.
     */
    public static boolean checkOrNotify(PermitTo permit, Runnable actionOnSuccess) {
        return checkOrNotify(permit, "effectuer cette opération", actionOnSuccess);
    }

    /**
     * Vérifie si la permission est accordée avec description personnalisée de l'action.
     */
    public static boolean checkOrNotify(PermitTo permit, String actionDescription, Runnable actionOnSuccess) {
        if (hasPermission(permit)) {
            if (actionOnSuccess != null) {
                actionOnSuccess.run();
            }
            return true;
        }
        MainUI.notify(
            null,
            "Accès refusé",
            "Vous n'avez pas les privilèges suffisants pour " + (actionDescription != null ? actionDescription : "effectuer cette opération") + ".",
            4,
            "error"
        );
        return false;
    }

    /**
     * Renouvelle l'ensemble des permissions utilisateur en base de données.
     */
    public static List<Permission> renewPermissions(List<Permission> newPermissions) {
        return PermissionDelegate.renewPermissions(newPermissions);
    }

    /**
     * Récupère la liste des permissions courantes de l'utilisateur.
     */
    public static List<Permission> findPermissions() {
        return PermissionDelegate.findPermissions();
    }
}
