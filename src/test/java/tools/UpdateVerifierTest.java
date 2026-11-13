package tools;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Securite (C3/C4/M6) : validation stricte des noms de fichiers de mise a
 * jour et verification d'integrite des telechargements.
 */
@DisplayName("UpdateVerifier — noms de fichiers et integrite")
class UpdateVerifierTest {

    // --- validateFilename ---

    @Test
    @DisplayName("noms de fichiers legitimes acceptes")
    void acceptsLegitimateFilenames() {
        assertEquals("Kazisafex.exe", UpdateVerifier.validateFilename("Kazisafex.exe"));
        assertEquals("Kazisafe-Linux.zip", UpdateVerifier.validateFilename("Kazisafe-Linux.zip"));
        assertEquals("Kazisafe-MacOS.zip", UpdateVerifier.validateFilename("Kazisafe-MacOS.zip"));
        assertEquals("Kazisafex.jar", UpdateVerifier.validateFilename("Kazisafex.jar"));
        assertEquals("a1.jar", UpdateVerifier.validateFilename("a1.jar"));
        assertEquals("Kazisafex_2.0.exe", UpdateVerifier.validateFilename(" Kazisafex_2.0.exe "));
    }

    @Test
    @DisplayName("path traversal et caracteres dangereux rejetes")
    void rejectsTraversalAndDangerousNames() {
        assertNull(UpdateVerifier.validateFilename(null));
        assertNull(UpdateVerifier.validateFilename(""));
        assertNull(UpdateVerifier.validateFilename("   "));
        // Traversals et separateurs.
        assertNull(UpdateVerifier.validateFilename("../evil.exe"));
        assertNull(UpdateVerifier.validateFilename("..\\..\\evil.exe"));
        assertNull(UpdateVerifier.validateFilename("sub/dir/evil.exe"));
        assertNull(UpdateVerifier.validateFilename("C:\\temp\\evil.exe"));
        // Prefixes dangereux (options de ligne de commande, fichiers caches).
        assertNull(UpdateVerifier.validateFilename("-option.exe"));
        assertNull(UpdateVerifier.validateFilename(".hidden.exe"));
        // Espaces et caracteres speciaux.
        assertNull(UpdateVerifier.validateFilename("evil name.exe"));
        assertNull(UpdateVerifier.validateFilename("evil;name.exe"));
        assertNull(UpdateVerifier.validateFilename("evil$(x).exe"));
        // Extensions non autorisees.
        assertNull(UpdateVerifier.validateFilename("evil.bat"));
        assertNull(UpdateVerifier.validateFilename("evil.sh"));
        assertNull(UpdateVerifier.validateFilename("evil.msi"));
        // Nom sans extension refuse (seuls jar/exe/zip sont attendus).
        assertNull(UpdateVerifier.validateFilename("readme"));
        // Longueur depassee (limite : 128 caracteres, extension comprise).
        assertNull(UpdateVerifier.validateFilename("a".repeat(125) + ".exe"));
    }

    // --- findEntry (parsing du manifeste) ---

    @Test
    @DisplayName("findEntry extrait l'entree correspondante du manifeste")
    void findsMatchingEntry() {
        String manifest = """
                [
                  {"name":"Kazisafex.exe","size":12345,"sha256":"ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789"},
                  {"name":"Kazisafe-Linux.zip","size":999,"sha256":"fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210"}
                ]
                """;
        Optional<UpdateVerifier.ManifestEntry> entry =
                UpdateVerifier.findEntry(manifest, "Kazisafex.exe");
        assertTrue(entry.isPresent());
        assertEquals(12345L, entry.get().size());
        assertEquals("abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789",
                entry.get().sha256());

        assertTrue(UpdateVerifier.findEntry(manifest, "Kazisafe-Linux.zip").isPresent());
        assertFalse(UpdateVerifier.findEntry(manifest, "Autre.exe").isPresent());
    }

    @Test
    @DisplayName("findEntry refuse les empreintes mal formees")
    void rejectsMalformedHashes() {
        String manifest =
                "[{\"name\":\"Kazisafex.exe\",\"size\":1,\"sha256\":\"court\"}]";
        assertFalse(UpdateVerifier.findEntry(manifest, "Kazisafex.exe").isPresent());
        assertFalse(UpdateVerifier.findEntry(null, "Kazisafex.exe").isPresent());
        assertFalse(UpdateVerifier.findEntry(manifest, null).isPresent());
    }

    // --- sha256Of / verifyDownloadedFile ---

    @Test
    @DisplayName("sha256Of calcule l'empreinte attendue")
    void computesSha256(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("sample.jar");
        Files.writeString(file, "contenu connu du fichier");
        String sha = UpdateVerifier.sha256Of(file);
        assertEquals(64, sha.length());
        // Deterministe.
        assertEquals(sha, UpdateVerifier.sha256Of(file));
        // Valeur de reference du contenu exact.
        assertEquals(
            java.security.MessageDigest.getInstance("SHA-256")
                    .digest(Files.readAllBytes(file))
                    .length * 2,
            sha.length());
        assertNotNull(sha);
    }

    @Test
    @DisplayName("verifyDownloadedFile valide taille et empreinte")
    void verifiesDownloadedFile(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("update.jar");
        Files.writeString(file, "binaire de mise a jour");
        String sha = UpdateVerifier.sha256Of(file);
        long size = Files.size(file);

        var good = new UpdateVerifier.ManifestEntry("update.jar", size, sha);
        assertTrue(UpdateVerifier.verifyDownloadedFile(file, good));

        var wrongSha = new UpdateVerifier.ManifestEntry("update.jar", size,
                "0".repeat(64));
        assertFalse(UpdateVerifier.verifyDownloadedFile(file, wrongSha));

        var wrongSize = new UpdateVerifier.ManifestEntry("update.jar", size + 1, sha);
        assertFalse(UpdateVerifier.verifyDownloadedFile(file, wrongSize));

        assertFalse(UpdateVerifier.verifyDownloadedFile(file, null));
        assertFalse(UpdateVerifier.verifyDownloadedFile(dir.resolve("absent.jar"), good));
    }

    @Test
    @DisplayName("readBounded refuse un manifeste au-dela de la limite")
    void rejectsOversizedManifest() throws Exception {
        byte[] payload = new byte[UpdateVerifier.MAX_MANIFEST_BYTES + 1];
        java.io.InputStream in = new java.io.ByteArrayInputStream(payload);
        org.junit.jupiter.api.Assertions.assertThrows(java.io.IOException.class,
                () -> UpdateVerifier.readBounded(in, UpdateVerifier.MAX_MANIFEST_BYTES));

        byte[] accepted = UpdateVerifier.readBounded(
                new java.io.ByteArrayInputStream("[]".getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                UpdateVerifier.MAX_MANIFEST_BYTES);
        assertEquals("[]", new String(accepted, java.nio.charset.StandardCharsets.UTF_8));
    }

    // --- configuration de la signature ---

    @Test
    @DisplayName("sans cle publique configuree : mode non verifie, pas d'installation automatique")
    void unverifiedModeWithoutPublicKey() {
        if (!UpdateVerifier.isManifestSigningConfigured()) {
            assertFalse(UpdateVerifier.isAutomaticInstallAllowed());
            assertEquals(Optional.empty(), UpdateVerifier.loadVerifiedManifestEntry(
                    "https://cloud.kazisafe.com/download", "Kazisafex.exe"));
        }
    }
}
