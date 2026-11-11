package services.utils;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.SyncEngine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Securite (H1/H2/M1) : le magasin de secrets locaux chiffre les valeurs au
 * repos et migre les anciennes preferences en clair.
 */
@DisplayName("SecretStore — chiffrement au repos des secrets locaux")
class SecretStoreTest {

    /** Cle de preference en clair, historique. */
    static final String PLAIN_NODE = "SecretStoreTestPlain";

    @TempDir
    static Path tempDir;

    @BeforeAll
    static void isolateDeviceKey() {
        // Passphrase device isolee pour la suite de tests : ne jamais creer
        // ~/.kazisafe/device.key sur la machine du developpeur.
        System.setProperty(SecretStore.PROP_DEVICE_KEY_FILE,
            tempDir.resolve("device.key").toString());
    }

    private String key;

    @AfterEach
    void cleanup() {
        if (key != null) {
            SecretStore.remove(key);
            key = null;
        }
        Preferences.userNodeForPackage(SyncEngine.class).remove(PLAIN_NODE);
    }

    @Test
    @DisplayName("store puis load restitue la valeur en clair")
    void storeLoadRoundTrip() {
        key = "SecretStoreTest.roundtrip." + System.nanoTime();
        SecretStore.store(key, "mot-de-passe-super-secret");
        assertTrue(SecretStore.has(key));
        assertEquals("mot-de-passe-super-secret", SecretStore.load(key));
    }

    @Test
    @DisplayName("la valeur stockee n'est pas lisible en clair dans les Preferences")
    void storedValueIsCiphertext() {
        key = "SecretStoreTest.cipher." + System.nanoTime();
        SecretStore.store(key, "mot-de-passe-super-secret");
        String raw = Preferences.userNodeForPackage(SyncEngine.class)
                .get(key, null);
        assertNotEquals("mot-de-passe-super-secret", raw,
                "La valeur en clair ne doit jamais apparaitre dans les Preferences");
        assertTrue(raw != null && !raw.isBlank());
    }

    @Test
    @DisplayName("remove efface le secret")
    void removeErasesValue() {
        key = "SecretStoreTest.remove." + System.nanoTime();
        SecretStore.store(key, "valeur");
        SecretStore.remove(key);
        assertFalse(SecretStore.has(key));
        assertNull(SecretStore.load(key));
    }

    @Test
    @DisplayName("migrateFromPlainPref chiffre la valeur legacy sous la meme cle")
    void migratesPlainPreference() {
        Preferences plainNode = Preferences.userNodeForPackage(SyncEngine.class);
        plainNode.put(PLAIN_NODE, "  secret-legacy  ");
        key = PLAIN_NODE;

        String migrated = SecretStore.migrateFromPlainPref(PLAIN_NODE);

        assertEquals("secret-legacy", migrated);
        assertTrue(SecretStore.has(PLAIN_NODE));
        // Le ciphertext a remplace le clair sous la meme cle : la valeur
        // survit au redemarrage (et n'est plus lisible en clair).
        assertNotEquals("secret-legacy", plainNode.get(PLAIN_NODE, null),
                "La valeur en clair ne doit plus etre presente dans les Preferences");
        assertEquals("secret-legacy", SecretStore.load(PLAIN_NODE));
        // Re-migration idempotente : la valeur chiffree est reconnue.
        assertEquals("secret-legacy", SecretStore.migrateFromPlainPref(PLAIN_NODE));
    }

    @Test
    @DisplayName("writeDeviceKeyFile accepte un chemin relatif sans parent")
    void writesDeviceKeyForRelativePath() throws Exception {
        Path relative = Path.of("device-key-test-" + System.nanoTime() + ".tmp");
        Method writer = SecretStore.class.getDeclaredMethod(
                "writeDeviceKeyFile", Path.class, String.class);
        writer.setAccessible(true);
        try {
            writer.invoke(null, relative, "test-device-passphrase");
            assertTrue(Files.isRegularFile(relative));
        } finally {
            Files.deleteIfExists(relative);
        }
    }

    @Test
    @DisplayName("load d'une cle absente retourne null sans erreur")
    void loadMissingKey() {
        assertNull(SecretStore.load("SecretStoreTest.absent." + System.nanoTime()));
    }
}
