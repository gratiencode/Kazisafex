package services;

import java.util.prefs.Preferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import services.utils.SecretStore;
import tools.SyncEngine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Securite (C2/H4) : plus de compte ni mot de passe par defaut embarque
 * pour MySQL ; TLS REQUIRED par defaut ; les deploiements non configures
 * echouent explicitement plutot que de se connecter en clair avec root.
 *
 * <p>Les valeurs brutes des preferences reelles sont sauvegardees puis
 * restaurees autour de chaque test (la machine d'execution peut etre une
 * machine ou l'application est installee).</p>
 */
@DisplayName("ManagedSessionFactory — identifiants MySQL et TLS")
class ManagedSessionFactoryDefaultsTest {

    private static Preferences pref() {
        return Preferences.userNodeForPackage(SyncEngine.class);
    }

    private String backupUser;
    private String backupSsl;
    private String backupSslMode;
    private String backupPasswordRaw;

    @BeforeEach
    void backupRealValues() {
        Preferences p = pref();
        backupUser = p.get("default_mysql_user", null);
        backupSsl = p.get("default_mysql_ssl", null);
        backupSslMode = p.get("default_mysql_ssl_mode", null);
        backupPasswordRaw = p.get("default_mysql_password", null);
    }

    @AfterEach
    void restoreRealValues() {
        Preferences p = pref();
        restore(p, "default_mysql_user", backupUser);
        restore(p, "default_mysql_ssl", backupSsl);
        restore(p, "default_mysql_ssl_mode", backupSslMode);
        // Restaure l'etat exact d'avant le test (clair legacy ou ciphertext).
        restore(p, "default_mysql_password", backupPasswordRaw);
    }

    private void restore(Preferences p, String key, String backup) {
        if (backup == null) {
            p.remove(key);
        } else {
            p.put(key, backup);
        }
    }

    // --- normalizeSslMode ---

    @Test
    @DisplayName("normalizeSslMode valide et normalise les modes MySQL")
    void normalizesSslModes() {
        assertEquals("REQUIRED", ManagedSessionFactory.normalizeSslMode("REQUIRED"));
        assertEquals("VERIFY_CA", ManagedSessionFactory.normalizeSslMode("verify_ca"));
        assertEquals("VERIFY_IDENTITY", ManagedSessionFactory.normalizeSslMode(" Verify_Identity "));
        assertEquals("DISABLED", ManagedSessionFactory.normalizeSslMode("DISABLED"));
        // Valeur inconnue : repli sur le mode le plus strict.
        assertEquals("REQUIRED", ManagedSessionFactory.normalizeSslMode("n'importe-quoi"));
        assertEquals("REQUIRED", ManagedSessionFactory.normalizeSslMode(null));
    }

    // --- resolveMySqlSslMode ---

    @Test
    @DisplayName("TLS requis par defaut en l'absence de configuration")
    void sslRequiredByDefault() {
        assumeTrue(System.getenv("KAZISAFE_DB_SSL_MODE") == null);
        Preferences p = pref();
        p.remove("default_mysql_ssl_mode");
        p.remove("default_mysql_ssl");

        assertEquals("REQUIRED", ManagedSessionFactory.resolveMySqlSslMode());
    }

    @Test
    @DisplayName("les deploiements legacy explicites sont respectes")
    void respectsLegacyBooleanSslPref() {
        assumeTrue(System.getenv("KAZISAFE_DB_SSL_MODE") == null);
        Preferences p = pref();
        p.remove("default_mysql_ssl_mode");

        p.put("default_mysql_ssl", "true");
        assertEquals("REQUIRED", ManagedSessionFactory.resolveMySqlSslMode());

        p.put("default_mysql_ssl", "false");
        assertEquals("DISABLED", ManagedSessionFactory.resolveMySqlSslMode());

        p.put("default_mysql_ssl_mode", "VERIFY_IDENTITY");
        assertEquals("VERIFY_IDENTITY", ManagedSessionFactory.resolveMySqlSslMode());
    }

    // --- resolveDbUser ---

    @Test
    @DisplayName("aucun utilisateur par defaut : null sans configuration")
    void noDefaultUser() {
        assumeTrue(System.getenv("KAZISAFE_DB_USER") == null);
        Preferences p = pref();
        p.remove("default_mysql_user");

        assertNull(ManagedSessionFactory.resolveDbUser(),
                "Aucun compte par defaut (root) ne doit etre embarque");

        p.put("default_mysql_user", "compte-applicatif");
        assertEquals("compte-applicatif", ManagedSessionFactory.resolveDbUser());
    }

    // --- resolveDbPassword ---

    @Test
    @DisplayName("le mot de passe vient du magasin chiffre, jamais d'un defaut embarque")
    void passwordFromSecretStoreOnly() {
        assumeTrue(System.getenv("KAZISAFE_DB_PASSWORD") == null);
        Preferences p = pref();
        p.remove("default_mysql_password");

        SecretStore.store("default_mysql_password", "motdepasse-chiffre-au-repos");
        assertEquals("motdepasse-chiffre-au-repos",
                ManagedSessionFactory.resolveDbPassword());

        // La valeur sous la cle n'est PAS le mot de passe en clair.
        assertNotEquals("motdepasse-chiffre-au-repos",
                p.get("default_mysql_password", null),
                "Le mot de passe en clair ne doit jamais rester dans les Preferences");
    }

    @Test
    @DisplayName("migrateFromPlainPref chiffre la valeur legacy sous la meme cle")
    void migratesLegacyMysqlPassword() {
        assumeTrue(System.getenv("KAZISAFE_DB_PASSWORD") == null);
        Preferences p = pref();
        p.put("default_mysql_password", "motdepasse-legacy");

        assertEquals("motdepasse-legacy", ManagedSessionFactory.resolveDbPassword());
        // Le ciphertext a remplace le clair : le mot de passe survit au
        // redemarrage (bug de perte de migration corrige).
        assertNotEquals("motdepasse-legacy", p.get("default_mysql_password", null));
        assertEquals("motdepasse-legacy", ManagedSessionFactory.resolveDbPassword());
    }

    @Test
    @DisplayName("sans mot de passe configure : erreur explicite, pas de defaut")
    @DisabledIfEnvironmentVariable(named = "KAZISAFE_DB_PASSWORD", matches = ".+")
    void missingPasswordFailsExplicitly() {
        assumeTrue(System.getenv("KAZISAFE_DB_PASSWORD") == null);
        Preferences p = pref();
        p.remove("default_mysql_password");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                ManagedSessionFactory::resolveDbPassword);
        assertTrue(ex.getMessage().contains("KAZISAFE_DB_PASSWORD"));
    }
}
