package tools;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import tools.SyncLogger;

/**
 * Verification de securite de la chaine de mise a jour.
 *
 * <p>Trois etapes :</p>
 * <ol>
 *   <li>validation stricte du nom de fichier (anti path-traversal,
 *       anti caracteres dangereux) ;</li>
 *   <li>recuperation d'un manifeste distant
 *       {@code download/manifest.json} signe (Ed25519) par la cle publique
 *       embarquee {@value #ED25519_PUBLIC_KEY_BASE64} ;</li>
 *   <li>verification du fichier telecharge (taille + SHA-256) contre
 *       l'entree correspondante du manifeste.</li>
 * </ol>
 *
 * <p><b>Etat actuel</b> : la cle publique de signature n'est pas encore
 * configuree (constante vide). Tant qu'elle ne l'est pas — et que le serveur
 * ne publie pas de manifeste signe — les mises a jour sont traitees en
 * « mode non verifie » : le telechargement reste possible (nom valide,
 * repertoire controle), mais l'application automatique au demarrage et
 * l'execution directe du binaire restent desactivees. Procedure de
 * deploiement cote serveur : voir docs/remediation/MANUAL_ACTIONS.md.</p>
 *
 * <p>SHA-256 seul ne suffit pas : c'est la signature Ed25519 du manifeste
 * qui authentifie l'empreinte.</p>
 */
public final class UpdateVerifier {

    /** Cle publique Ed25519 (X509/SPKI, Base64) du signataire des mises a jour. */
    static final String ED25519_PUBLIC_KEY_BASE64 = "";

    /** Extension du manifeste distant. */
    static final String MANIFEST_NAME = "manifest.json";

    /** Extensions de fichiers de mise a jour autorisees. */
    private static final Pattern SAFE_FILENAME = Pattern.compile(
        "^[A-Za-z0-9][A-Za-z0-9._-]{0,127}$");

    private static final int MAX_FILENAME_LENGTH = 128;

    /** Taille maximale acceptee pour le manifeste et sa signature (1 Mio). */
    static final int MAX_MANIFEST_BYTES = 1_048_576;

    private UpdateVerifier() {
    }

    /** Vrai si la verification par manifeste signe est operationnelle. */
    public static boolean isManifestSigningConfigured() {
        return ED25519_PUBLIC_KEY_BASE64 != null && !ED25519_PUBLIC_KEY_BASE64.isBlank();
    }

    /**
     * Validation stricte d'un nom de fichier de mise a jour fourni par le
     * serveur : caracteurs alphanumeriques, points, tirets et underscores
     * uniquement, premier caractere alphanumerique, longueur bornee, pas de
     * separateur de chemin, pas de nom de repertoire parent.
     *
     * @return le nom valide, ou {@code null} si invalide
     */
    public static String validateFilename(String candidate) {
        if (candidate == null) {
            return null;
        }
        String trimmed = candidate.trim();
        if (trimmed.isEmpty()
                || trimmed.length() > MAX_FILENAME_LENGTH
                || trimmed.contains("/")
                || trimmed.contains("\\")
                || trimmed.contains("..")
                || trimmed.startsWith(".")
                || trimmed.startsWith("-")
                || !SAFE_FILENAME.matcher(trimmed).matches()) {
            return null;
        }
        String lower = trimmed.toLowerCase(Locale.ROOT);
        boolean allowedExtension = lower.endsWith(".jar")
                || lower.endsWith(".exe")
                || lower.endsWith(".zip");
        if (!allowedExtension) {
            return null;
        }
        return trimmed;
    }

    /**
     * Entree de manifeste : empreinte et taille attendues d'un fichier de
     * mise a jour.
     */
    public record ManifestEntry(String filename, long size, String sha256) {
    }

    /**
     * Recupere et authentifie le manifeste distant.
     *
     * @return l'entree du manifeste correspondant au fichier demande, ou
     *         {@link Optional#empty()} si le manifeste est absent, illisible
     *         ou non authentifie
     */
    public static Optional<ManifestEntry> loadVerifiedManifestEntry(
            String updateBaseUrl, String filename) {
        if (!isManifestSigningConfigured()) {
            return Optional.empty();
        }
        try {
            byte[] manifest = fetch(updateBaseUrl + "/" + MANIFEST_NAME);
            byte[] signature = fetch(updateBaseUrl + "/" + MANIFEST_NAME + ".sig");
            if (!verifyEd25519Signature(manifest, signature)) {
                SyncLogger.getInstance().log(
                    new SecurityException("Signature du manifeste de mise a jour invalide"),
                    "UpdateVerifier.loadVerifiedManifestEntry");
                return Optional.empty();
            }
            return findEntry(new String(manifest, StandardCharsets.UTF_8), filename);
        } catch (Exception ex) {
            SyncLogger.getInstance().log(ex, "UpdateVerifier.loadVerifiedManifestEntry");
            return Optional.empty();
        }
    }

    private static byte[] fetch(String url) throws IOException {
        URLConnection connection = new URL(url).openConnection();
        connection.setConnectTimeout(15_000);
        connection.setReadTimeout(60_000);
        try (var is = connection.getInputStream()) {
            return readBounded(is, MAX_MANIFEST_BYTES);
        }
    }

    /**
     * Lecture bornee : un manifeste/signature hostile ne doit pas provoquer
     * une consommation memoire non maitrisee (readAllBytes illimite).
     */
    static byte[] readBounded(java.io.InputStream in, int maxBytes) throws IOException {
        if (in == null || maxBytes <= 0) {
            throw new IllegalArgumentException("Flux ou limite invalide");
        }
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int total = 0;
        int n;
        while ((n = in.read(buffer)) > 0) {
            total += n;
            if (total > maxBytes) {
                throw new IOException("Manifeste de mise a jour trop volumineux (> " + maxBytes + " octets)");
            }
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }

    private static boolean verifyEd25519Signature(byte[] manifest, byte[] signature) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(ED25519_PUBLIC_KEY_BASE64);
            PublicKey publicKey = KeyFactory.getInstance("Ed25519")
                    .generatePublic(new X509EncodedKeySpec(keyBytes));
            Signature verifier = Signature.getInstance("Ed25519");
            verifier.initVerify(publicKey);
            verifier.update(manifest);
            return verifier.verify(signature);
        } catch (Exception ex) {
            SyncLogger.getInstance().log(ex, "UpdateVerifier.verifyEd25519Signature");
            return false;
        }
    }

    /**
     * Extrait l'entree du manifeste pour un nom de fichier donne. Le
     * manifeste est attendu sous la forme d'un tableau JSON :
     * {@code [{"name":"Kazisafex.exe","size":123,"sha256":"..."}, ...]}.
     * Parsing volontairement conservateur (regexp sur les champs).
     */
    static Optional<ManifestEntry> findEntry(String manifestJson, String filename) {
        if (manifestJson == null || manifestJson.isBlank() || filename == null) {
            return Optional.empty();
        }
        String normalized = manifestJson.replaceAll("\\s+", "");
        for (String rawEntry : normalized.split("\\},\\{")) {
            String entry = rawEntry.replace("[", "").replace("]", "")
                    .replace("{", "").replace("}", "");
            if (!entry.contains("\"name\"")) {
                continue;
            }
            String name = extractJsonStringField(entry, "name");
            if (!filename.equals(name)) {
                continue;
            }
            Long size = extractJsonLongField(entry, "size");
            String sha256 = extractJsonStringField(entry, "sha256");
            if (sha256 == null || !sha256.matches("[0-9a-fA-F]{64}")) {
                return Optional.empty();
            }
            return Optional.of(new ManifestEntry(filename,
                    size == null ? -1L : size, sha256.toLowerCase(Locale.ROOT)));
        }
        return Optional.empty();
    }

    private static String extractJsonStringField(String entry, String field) {
        Pattern p = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"");
        var m = p.matcher(entry);
        return m.find() ? m.group(1) : null;
    }

    private static Long extractJsonLongField(String entry, String field) {
        Pattern p = Pattern.compile("\"" + field + "\"\\s*:\\s*([0-9]+)");
        var m = p.matcher(entry);
        if (m.find()) {
            try {
                return Long.parseLong(m.group(1));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    /**
     * Calcule le SHA-256 d'un fichier.
     */
    public static String sha256Of(Path file) throws IOException {
        try (var in = Files.newInputStream(file)) {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) > 0) {
                digest.update(buffer, 0, n);
            }
            StringBuilder sb = new StringBuilder();
            for (byte b : digest.digest()) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 indisponible", ex);
        }
    }

    /**
     * Verifie un fichier telecharge contre l'entree du manifeste
     * (taille + SHA-256).
     *
     * @return vrai si le fichier correspond exactement a l'entree signee
     */
    public static boolean verifyDownloadedFile(Path file, ManifestEntry entry) {
        if (entry == null || !Files.isRegularFile(file)) {
            return false;
        }
        try {
            if (entry.size() >= 0 && Files.size(file) != entry.size()) {
                return false;
            }
            return entry.sha256().equals(sha256Of(file));
        } catch (IOException | RuntimeException ex) {
            SyncLogger.getInstance().log(ex, "UpdateVerifier.verifyDownloadedFile");
            return false;
        }
    }

    /** Telechargement verifie possible uniquement si manifeste signe configure. */
    public static boolean isAutomaticInstallAllowed() {
        return isManifestSigningConfigured();
    }

    static URI normalizeDownloadUri(String baseUrl, String encodedName) {
        return URI.create(baseUrl + "/" + encodedName);
    }
}
