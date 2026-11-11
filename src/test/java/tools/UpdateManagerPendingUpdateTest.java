package tools;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.prefs.Preferences;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Non-regression C3/C4 : un drapeau "verified" persiste ne suffit jamais a
 * appliquer un binaire. La signature/SHA-256 doit etre revalidee au moment de
 * l'application.
 */
class UpdateManagerPendingUpdateTest {

    private static final String PREF_PENDING_UPDATE = "ksf_pending_update";
    private static final String PREF_PENDING_VERSION = "ksf_pending_version";
    private static final String PREF_PENDING_VERIFIED = "ksf_pending_update_verified";

    private final Preferences prefs = Preferences.userNodeForPackage(SyncEngine.class);

    @TempDir
    Path tempDir;

    @AfterEach
    void cleanup() {
        prefs.remove(PREF_PENDING_UPDATE);
        prefs.remove(PREF_PENDING_VERSION);
        prefs.remove(PREF_PENDING_VERIFIED);
    }

    @Test
    void refusesPendingFileWhenSignatureCannotBeRevalidated() throws Exception {
        Path pending = tempDir.resolve("Kazisafe-update.exe");
        Files.writeString(pending, "not-a-signed-update");

        prefs.put(PREF_PENDING_UPDATE, pending.toString());
        prefs.put(PREF_PENDING_VERSION, "9.9.9");
        prefs.putBoolean(PREF_PENDING_VERIFIED, true); // ne doit plus suffire

        UpdateManager manager = new UpdateManager(null);
        assertTrue(manager.hasPendingUpdate());
        assertFalse(manager.applyPendingUpdateAtStartup(),
                "Un drapeau verified ne doit jamais suffire sans revalidation");
        assertFalse(manager.hasPendingUpdate(), "La demande non verifiee doit etre purgee");
    }
}
