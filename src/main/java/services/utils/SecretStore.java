package services.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;
import java.util.prefs.Preferences;
import tools.SyncEngine;
import tools.SyncLogger;

/**
 * Magasin de secrets locaux pour l'application desktop.
 *
 * <p>Chiffre les valeurs sensibles (jetons, mots de passe, clés d'API) au repos
 * via {@link SecurePreferences} (AES-GCM, PBKDF2) avec une « passphrase
 * device » générée aléatoirement à la première utilisation et conservée
 * <em>hors</em> des {@link Preferences} : un fichier à permissions restreites
 * (0600 sur POSIX) dans le répertoire personnel de l'utilisateur.</p>
 *
 * <p>Modèle de menaces et limites assumées : un attaquant disposant d'un
 * accès administrateur au poste peut lire la passphrase device et les
 * Preferences. L'objectif est d'empêcher la récupération passive des secrets
 * par simple copie du dossier de données / des Preferences XML (sauvegardes,
 * synchronisation de profil, exfiltration non privilégiée).</p>
 *
 * <p>La passphrase device peut être fournie par la variable d'environnement
 * {@code KAZISAFE_DEVICE_PASSPHRASE} (déploiements gérés) ; sinon le fichier
 * {@code ~/.kazisafe/device.key} est utilisé. Son emplacement est surchargable
 * via la propriété système {@code kazisafe.device.key.file} (tests).</p>
 */
public final class SecretStore {

    /** Variable d'environnement de surcharge de la passphrase device. */
    static final String ENV_DEVICE_PASSPHRASE = "KAZISAFE_DEVICE_PASSPHRASE";
    /** Propriété système de surcharge du fichier de passphrase (tests). */
    static final String PROP_DEVICE_KEY_FILE = "kazisafe.device.key.file";

    private static final String DEVICE_KEY_DIR = ".kazisafe";
    private static final String DEVICE_KEY_FILE = "device.key";
    private static final int DEVICE_KEY_BYTES = 32;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static volatile String cachedDevicePassphrase;

    private SecretStore() {
    }

    /**
     * Passphrase device effective : variable d'environnement si définie, sinon
     * fichier local (créé à la première utilisation).
     */
    static String devicePassphrase() {
        String fromEnv = System.getenv(ENV_DEVICE_PASSPHRASE);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        String cached = cachedDevicePassphrase;
        if (cached != null) {
            return cached;
        }
        synchronized (SecretStore.class) {
            if (cachedDevicePassphrase != null) {
                return cachedDevicePassphrase;
            }
            cachedDevicePassphrase = readOrCreateDeviceKeyFile();
            return cachedDevicePassphrase;
        }
    }

    private static Path deviceKeyFile() {
        String override = System.getProperty(PROP_DEVICE_KEY_FILE);
        if (override != null && !override.isBlank()) {
            return Path.of(override);
        }
        String home = System.getProperty("user.home", ".");
        return Path.of(home, DEVICE_KEY_DIR, DEVICE_KEY_FILE);
    }

    private static String readOrCreateDeviceKeyFile() {
        Path file = deviceKeyFile();
        try {
            String existing = readFileIfExists(file);
            if (existing != null) {
                return existing;
            }
            byte[] key = new byte[DEVICE_KEY_BYTES];
            RANDOM.nextBytes(key);
            String value = Base64.getEncoder().encodeToString(key);
            writeDeviceKeyFile(file, value);
            return value;
        } catch (IOException | RuntimeException ex) {
            // Sans passphrase device, aucun secret chiffré ne peut être lu ni
            // écrit : on remonte une erreur explicite plutot que de retomber
            // silencieusement sur du stockage en clair.
            throw new IllegalStateException(
                "Impossible d'initialiser le magasin de secrets local ("
                    + file + ") : " + ex.getMessage(), ex);
        }
    }

    private static String readFileIfExists(Path file) throws IOException {
        if (!Files.isRegularFile(file)) {
            return null;
        }
        String content = Files.readString(file, StandardCharsets.UTF_8).trim();
        return content.isBlank() ? null : content;
    }

    private static void writeDeviceKeyFile(Path file, String value) throws IOException {
        Path dir = file.getParent();
        if (dir != null) {
            Files.createDirectories(dir);
        }
        Path tmp;
        if (dir != null) {
            tmp = Files.createTempFile(dir, "device-", ".tmp");
        } else {
            // Un chemin relatif peut ne pas avoir de parent : createTempFile
            // exige un répertoire non nul.
            tmp = Files.createTempFile("device-", ".tmp");
        }
        Files.writeString(tmp, value, StandardCharsets.UTF_8);
        restrictPermissions(tmp);
        try {
            Files.move(tmp, file,
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException ex) {
            Files.move(tmp, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        restrictPermissions(file);
    }

    /**
     * Restreint le fichier a son proprietaire (0600) sur les systemes POSIX.
     * Sans effet sur Windows : la protection repose alors sur les ACL du
     * repertoire du profil utilisateur (limite documentee).
     */
    private static void restrictPermissions(Path file) {
        try {
            if (file.getFileSystem().supportedFileAttributeViews()
                    .contains("posix")) {
                Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
                Files.setPosixFilePermissions(file, perms);
            }
        } catch (IOException | RuntimeException ignored) {
            // Permissions best-effort : ne pas faire echouer l'initialisation.
        }
    }

    private static Preferences prefs() {
        return Preferences.userNodeForPackage(SyncEngine.class);
    }

    /** Vrai si une valeur chiffree existe sous cette cle. */
    public static boolean has(String key) {
        return prefs().get(key, null) != null;
    }

    /**
     * Stocke une valeur chiffree sous la cle donnee.
     *
     * @throws IllegalStateException si le magasin local est indisponible
     */
    public static void store(String key, String plainValue) {
        try {
            SecurePreferences.storeEncryptedValue(key, devicePassphrase(), plainValue);
        } catch (Exception ex) {
            throw new IllegalStateException(
                "Impossible de stocker le secret '" + key + "' : " + ex.getMessage(), ex);
        }
    }

    /**
     * Lit et dechiffre la valeur sous la cle donnee.
     *
     * @return la valeur en clair, ou {@code null} si absente ou illisible
     *         (corruption, passphrase perdue : le secret est alors considere
     *         perdu, l'appelant doit reauthentifier l'utilisateur)
     */
    public static String load(String key) {
        if (!has(key)) {
            return null;
        }
        try {
            return SecurePreferences.loadDecryptedValue(key, devicePassphrase());
        } catch (Exception ex) {
            SyncLogger.getInstance().log(ex,
                "Secret '" + key + "' illisible (corruption ou passphrase perdue)");
            return null;
        }
    }

    /** Supprime la valeur chiffree sous la cle donnee. */
    public static void remove(String key) {
        prefs().remove(key);
    }

    /**
     * Migre une ancienne preference en clair vers le magasin chiffre.
     *
     * <p>Le ciphertext remplace la valeur en clair sous la meme cle (il ne
     * faut PAS la supprimer ensuite : ce serait detruire la valeur migree).
     * Une valeur deja chiffree (migration precedente interrompue avant le
     * re-stockage sous la cle definitive) est dechiffree et retournee
     * telle quelle.</p>
     *
     * @return la valeur migree (ou dechiffree), ou {@code null} si la
     *         preference d'origine etait absente
     */
    public static String migrateFromPlainPref(String prefKey) {
        String plain = prefs().get(prefKey, null);
        if (plain == null || plain.isBlank()) {
            return null;
        }
        // Valeur deja chiffree (GCM verifie l'authenticite : un mot de passe
        // en clair ne se decrypte pas par accident) : la retourner dechiffree.
        try {
            String decrypted = SecurePreferences.loadDecryptedValue(prefKey, devicePassphrase());
            if (decrypted != null) {
                return decrypted;
            }
        } catch (Exception alreadyPlain) {
            // Echec de decryptage : la valeur est en clair, on migre.
        }
        try {
            store(prefKey, plain.trim());
            return plain.trim();
        } catch (RuntimeException ex) {
            // Echec de migration : on conserve la valeur en clair existante
            // pour ne pas deconnecter l'utilisateur ; l'appelant retentera.
            SyncLogger.getInstance().log(ex,
                "Migration du secret '" + prefKey + "' impossible, ancien stockage conserve");
            return plain.trim();
        }
    }
}
