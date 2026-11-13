package data.core;

import java.util.prefs.Preferences;
import services.utils.SecretStore;
import tools.SyncEngine;
import tools.SyncLogger;

/**
 * Stockage chiffre du jeton d'authentification Bearer de l'API Kazisafe.
 *
 * <p>Historiquement le jeton etait conserve en clair dans
 * {@code java.util.prefs} sous la cle {@code token}. Cette classe centralise
 * desormais l'acces : la valeur est chiffree au repos via {@link SecretStore}
 * (cle interne {@value #SECURE_KEY}) et les anciens jetons en clair sont
 * migres paresseusement puis supprimes des Preferences.</p>
 *
 * <p>Limites documentees : le jeton reste en memoire pendant la session et
 * transitant par les en-tetes HTTP ; il est revoquable cote serveur. L'objectif
 * est d'empecher sa lecture passive depuis les fichiers de Preferences.</p>
 */
public final class TokenStore {

    /** Ancienne cle de preference (en clair) : conservee uniquement pour la migration. */
    public static final String LEGACY_PREF_TOKEN = "token";

    /** Cle interne du magasin chiffre. */
    private static final String SECURE_KEY = "auth_token";

    private TokenStore() {
    }

    private static Preferences prefs() {
        return Preferences.userNodeForPackage(SyncEngine.class);
    }

    /**
     * Jeton courant : valeur chiffree si presente, sinon migration paresseuse
     * de l'ancienne preference en clair.
     *
     * @return le jeton sans prefixe {@code Bearer}, ou {@code null}
     */
    public static String get() {
        if (SecretStore.has(SECURE_KEY)) {
            String token = SecretStore.load(SECURE_KEY);
            if (token != null && !token.isBlank()) {
                return token.trim();
            }
        }
        String migrated = SecretStore.migrateFromPlainPref(LEGACY_PREF_TOKEN);
        if (migrated == null) {
            return null;
        }
        store(migrated);
        return migrated;
    }

    /** Enregistre (chiffre) le jeton ; supprime l'ancienne valeur en clair. */
    public static void store(String token) {
        if (token == null || token.isBlank()) {
            clear();
            return;
        }
        SecretStore.store(SECURE_KEY, token.trim());
        prefs().remove(LEGACY_PREF_TOKEN);
    }

    /** Supprime le jeton (chiffre et ancien stockage en clair). */
    public static void clear() {
        SecretStore.remove(SECURE_KEY);
        prefs().remove(LEGACY_PREF_TOKEN);
    }

    /**
     * Migre explicitement un jeton fourni par le flux de connexion et le
     * renvoie normalise. Utilise par les points d'ecriture historiques
     * ({@code pref.put("token", ...)}).
     */
    public static String saveFromLogin(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        // Retirer le prefixe "Bearer " AVANT le trim : sinon "Bearer    "
        // devient "Bearer" apres trim et le prefixe n'est plus detectable.
        String normalized = token;
        if (normalized.regionMatches(true, 0, "Bearer ", 0, 7)) {
            normalized = normalized.substring(7);
        }
        normalized = normalized.trim();
        if (normalized.isBlank()) {
            return null;
        }
        try {
            store(normalized);
        } catch (RuntimeException ex) {
            SyncLogger.getInstance().log(ex, "Sauvegarde du jeton d'authentification impossible");
        }
        return normalized;
    }
}
