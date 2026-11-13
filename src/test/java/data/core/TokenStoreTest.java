package data.core;

import java.util.prefs.Preferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import services.utils.SecretStore;
import tools.SyncEngine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Securite (H1) : le jeton d'authentification est chiffre au repos, et
 * l'ancienne preference en clair ({@code token}) est migree puis supprimee.
 *
 * <p>Les valeurs brutes des preferences reelles (chiffrees) sont
 * sauvegardees puis restaurees autour de chaque test : la machine qui
 * execute la suite peut etre une machine de development ou l'application
 * est installee.</p>
 */
@DisplayName("TokenStore — jeton chiffre et migration legacy")
class TokenStoreTest {

    private static Preferences prefs() {
        return Preferences.userNodeForPackage(SyncEngine.class);
    }

    private String backupSecure;
    private String backupLegacy;

    @BeforeEach
    void backupRealValues() {
        backupSecure = prefs().get("auth_token", null);
        backupLegacy = prefs().get(TokenStore.LEGACY_PREF_TOKEN, null);
    }

    @AfterEach
    void restoreRealValues() {
        if (backupSecure == null) {
            prefs().remove("auth_token");
        } else {
            prefs().put("auth_token", backupSecure);
        }
        if (backupLegacy == null) {
            prefs().remove(TokenStore.LEGACY_PREF_TOKEN);
        } else {
            prefs().put(TokenStore.LEGACY_PREF_TOKEN, backupLegacy);
        }
    }

    @Test
    @DisplayName("store chiffre le jeton et supprime la preference en clair")
    void storeEncryptsAndClearsLegacy() {
        prefs().put(TokenStore.LEGACY_PREF_TOKEN, "jeton-clair-ancien");
        TokenStore.store("nouveau-jeton");
        assertEquals("nouveau-jeton", TokenStore.get());
        assertNull(prefs().get(TokenStore.LEGACY_PREF_TOKEN, null),
                "L'ancienne preference en clair doit etre supprimee");
    }

    @Test
    @DisplayName("get migre paresseusement l'ancien jeton en clair")
    void getMigratesLegacyToken() {
        prefs().put(TokenStore.LEGACY_PREF_TOKEN, "jeton-legacy");
        assertEquals("jeton-legacy", TokenStore.get());
        assertNull(prefs().get(TokenStore.LEGACY_PREF_TOKEN, null));
        // Deuxieme lecture : provient du magasin chiffre.
        assertEquals("jeton-legacy", TokenStore.get());
        assertTrue(SecretStore.has("auth_token"));
    }

    @Test
    @DisplayName("clear efface jeton chiffre et ancien stockage")
    void clearRemovesBothStorages() {
        TokenStore.store("jeton-a-effacer");
        prefs().put(TokenStore.LEGACY_PREF_TOKEN, "residu-clair");
        TokenStore.clear();
        assertNull(TokenStore.get());
        assertFalse(SecretStore.has("auth_token"));
        assertNull(prefs().get(TokenStore.LEGACY_PREF_TOKEN, null));
    }

    @Test
    @DisplayName("saveFromLogin normalise le prefixe Bearer et rejette le vide")
    void saveFromLoginNormalizes() {
        assertEquals("abc123", TokenStore.saveFromLogin("Bearer abc123"));
        assertEquals("abc123", TokenStore.saveFromLogin("  abc123  "));
        assertNull(TokenStore.saveFromLogin(null));
        assertNull(TokenStore.saveFromLogin("   "));
        assertNull(TokenStore.saveFromLogin("Bearer    "));
        assertEquals("abc123", TokenStore.get());
    }

    @Test
    @DisplayName("store d'un jeton vide equivaut a clear")
    void blankStoreClears() {
        TokenStore.store("encore-un-jeton");
        TokenStore.store("   ");
        assertNull(TokenStore.get());
    }
}
