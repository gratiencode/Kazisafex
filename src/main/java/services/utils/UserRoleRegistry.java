package services.utils;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import tools.SyncEngine;

/**
 * Service centralisé de gestion du rôle (privilège) utilisateur.
 * Garantit un accès sécurisé et cohérent à la clé de préférence "priv" sans risque de NullPointerException.
 */
public final class UserRoleRegistry {

    private static final Logger LOG = Logger.getLogger(UserRoleRegistry.class.getName());
    public static final String PRIV_PREF = "priv";
    public static final String DEFAULT_ROLE = "Trader";
    public static final String ALL_ACCESS = "ALL_ACCESS";

    private UserRoleRegistry() {
    }

    private static Preferences getDefaultPref() {
        return Preferences.userNodeForPackage(SyncEngine.class);
    }

    /**
     * Récupère le rôle courant depuis les préférences utilisateur par défaut.
     * Garantit un retour non-null (DEFAULT_ROLE = "Trader" par défaut).
     */
    public static String getRole() {
        return getRole(getDefaultPref());
    }

    /**
     * Récupère le rôle courant depuis une instance spécifique de Preferences.
     * Garantit un retour non-null.
     */
    public static String getRole(Preferences pref) {
        if (pref == null) {
            return DEFAULT_ROLE;
        }
        String role = pref.get(PRIV_PREF, null);
        if (role == null || role.isBlank()) {
            return DEFAULT_ROLE;
        }
        return role.trim();
    }

    /**
     * Sauvegarde le rôle dans les préférences utilisateur par défaut.
     */
    public static void saveRole(String role) {
        saveRole(getDefaultPref(), role);
    }

    /**
     * Sauvegarde le rôle dans l'instance de Preferences fournie.
     */
    public static void saveRole(Preferences pref, String role) {
        if (pref == null) {
            return;
        }
        String targetRole = (role != null && !role.isBlank()) ? role.trim() : DEFAULT_ROLE;
        pref.put(PRIV_PREF, targetRole);
        try {
            pref.flush();
        } catch (BackingStoreException ex) {
            LOG.log(Level.SEVERE, "Erreur lors de la sauvegarde du rôle dans les préférences", ex);
        }
    }

    /**
     * Vérifie de manière sécurisée si le rôle contient un privilège spécifique.
     */
    public static boolean hasRole(String roleName) {
        return hasRole(getDefaultPref(), roleName);
    }

    /**
     * Vérifie de manière sécurisée si le rôle (sur les préférences spécifiées) contient un privilège.
     */
    public static boolean hasRole(Preferences pref, String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return false;
        }
        String currentRole = getRole(pref);
        return currentRole.equalsIgnoreCase(roleName.trim()) || currentRole.contains(roleName.trim());
    }

    /**
     * Indique si l'utilisateur possède l'accès total (ALL_ACCESS).
     */
    public static boolean hasAllAccess() {
        return hasAllAccess(getDefaultPref());
    }

    /**
     * Indique si l'utilisateur possède l'accès total (ALL_ACCESS).
     */
    public static boolean hasAllAccess(Preferences pref) {
        return getRole(pref).contains(ALL_ACCESS);
    }

    /**
     * Indique si l'utilisateur est un Commerçant / Trader.
     */
    public static boolean isTrader() {
        return isTrader(getDefaultPref());
    }

    /**
     * Indique si l'utilisateur est un Commerçant / Trader.
     */
    public static boolean isTrader(Preferences pref) {
        return DEFAULT_ROLE.equalsIgnoreCase(getRole(pref));
    }
}
