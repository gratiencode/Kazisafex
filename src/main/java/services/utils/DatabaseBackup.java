package services.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sauvegarde le fichier SQLite avant une ouverture Hibernate susceptible de
 * modifier le schéma ({@code hbm2ddl.auto=update}).
 *
 * <p>La copie est effectuée avant l'ouverture de l'EntityManagerFactory, donc
 * sans écrivain SQLite concurrent. Les fichiers {@code -wal}/{@code -shm}
 * présents sont copiés avec la base pour conserver l'état réel. La sauvegarde
 * est best-effort : un échec est journalisé mais n'empêche pas le démarrage ;
 * l'utilisateur doit pouvoir ouvrir son commerce même si le disque est plein.</p>
 *
 * <p>Rétention : les {@code keep} sauvegardes les plus récentes sont
 * conservées, les plus anciennes sont supprimées.</p>
 */
public final class DatabaseBackup {

    private static final Logger LOGGER = Logger.getLogger(DatabaseBackup.class.getName());
    private static final DateTimeFormatter STAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private DatabaseBackup() {
    }

    /**
     * @return le chemin de la sauvegarde principale, ou {@code null} si la base
     *         n'existe pas ou si la copie a échoué.
     */
    public static Path backupBeforeMigration(String databasePath, int keep) {
        if (databasePath == null || databasePath.isBlank()) {
            return null;
        }
        Path database = Path.of(databasePath);
        if (!Files.isRegularFile(database)) {
            return null;
        }
        Path directory = database.getParent();
        if (directory == null) {
            return null;
        }
        String base = database.getFileName().toString();
        String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        Path existingToday = findTodayBackup(directory, base, today);
        if (existingToday != null) {
            return existingToday;
        }
        Path target = directory.resolve(base + ".bak-" + LocalDateTime.now().format(STAMP));
        try {
            copyIfExists(database, target);
            copyIfExists(Path.of(databasePath + "-wal"), Path.of(target + "-wal"));
            copyIfExists(Path.of(databasePath + "-shm"), Path.of(target + "-shm"));
            DatabaseFileSecurity.harden(target.toString());
            writeChecksum(target);
            prune(directory, base, Math.max(1, keep));
            return target;
        } catch (IOException | RuntimeException ex) {
            LOGGER.log(Level.WARNING,
                    "Sauvegarde SQLite impossible avant migration : " + ex.getMessage());
            return null;
        }
    }

    /**
     * Sauvegarde immédiate, sans throttle quotidien, pour une opération
     * risquée (ex. clôture d'inventaire). Retourne null si la base n'existe pas.
     */
    public static Path backupNow(String databasePath, String label, int keep) {
        if (databasePath == null || databasePath.isBlank()) {
            return null;
        }
        Path database = Path.of(databasePath);
        if (!Files.isRegularFile(database)) {
            return null;
        }
        Path directory = database.getParent();
        if (directory == null) {
            return null;
        }
        String safeLabel = label == null || label.isBlank()
                ? "manual"
                : label.replaceAll("[^A-Za-z0-9._-]", "_");
        String base = database.getFileName().toString();
        Path target = directory.resolve(base + ".bak-" + safeLabel + "-"
                + LocalDateTime.now().format(STAMP));
        try {
            copyIfExists(database, target);
            copyIfExists(Path.of(databasePath + "-wal"), Path.of(target + "-wal"));
            copyIfExists(Path.of(databasePath + "-shm"), Path.of(target + "-shm"));
            DatabaseFileSecurity.harden(target.toString());
            writeChecksum(target);
            prune(directory, base, Math.max(1, keep));
            return target;
        } catch (IOException | RuntimeException ex) {
            LOGGER.log(Level.WARNING,
                    "Sauvegarde SQLite immédiate impossible : " + ex.getMessage());
            return null;
        }
    }

    private static void writeChecksum(Path target) throws IOException {
        String hex = sha256Hex(target);
        Path checksum = Path.of(target.toString() + ".sha256");
        Files.writeString(checksum, hex + "  " + target.getFileName() + System.lineSeparator());
        DatabaseFileSecurity.harden(checksum.toString());
    }

    /**
     * Verifie l'empreinte SHA-256 d'une sauvegarde avant restauration.
     *
     * @return true si la sauvegarde et son empreinte existent et concordent.
     */
    public static boolean verify(Path backup) {
        if (backup == null || !Files.isRegularFile(backup)) {
            return false;
        }
        Path checksumFile = Path.of(backup.toString() + ".sha256");
        if (!Files.isRegularFile(checksumFile)) {
            return false;
        }
        try {
            String expected = Files.readString(checksumFile).trim().split("\\s+")[0];
            String actual = sha256Hex(backup);
            return java.security.MessageDigest.isEqual(
                    expected.toLowerCase(java.util.Locale.ROOT).getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                    actual.toLowerCase(java.util.Locale.ROOT).getBytes(java.nio.charset.StandardCharsets.US_ASCII));
        } catch (IOException | RuntimeException ex) {
            LOGGER.log(Level.WARNING, "Verification de sauvegarde impossible : " + ex.getMessage());
            return false;
        }
    }

    private static String sha256Hex(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (var in = Files.newInputStream(file)) {
                byte[] buffer = new byte[8192];
                int n;
                while ((n = in.read(buffer)) > 0) {
                    digest.update(buffer, 0, n);
                }
            }
            return java.util.HexFormat.of().formatHex(digest.digest());
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IOException("SHA-256 indisponible", ex);
        }
    }

    private static Path findTodayBackup(Path directory, String base, String today) {
        try (var stream = Files.list(directory)) {
            return stream
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        return name.startsWith(base + ".bak-" + today)
                                && !name.endsWith("-wal")
                                && !name.endsWith("-shm")
                                && !name.endsWith(".sha256");
                    })
                    .findFirst()
                    .orElse(null);
        } catch (IOException ex) {
            return null;
        }
    }

    private static void copyIfExists(Path source, Path target) throws IOException {
        if (Files.isRegularFile(source)) {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES);
        }
    }

    private static void prune(Path directory, String base, int keep) {
        try (var stream = Files.list(directory)) {
            List<Path> backups = stream
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        return name.startsWith(base + ".bak-")
                                && !name.endsWith("-wal")
                                && !name.endsWith("-shm")
                                && !name.endsWith(".sha256");
                    })
                    .sorted(Comparator.comparingLong(DatabaseBackup::lastModified).reversed())
                    .toList();
            for (int i = keep; i < backups.size(); i++) {
                Path old = backups.get(i);
                Files.deleteIfExists(old);
                Files.deleteIfExists(Path.of(old.toString() + ".sha256"));
            }
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, "Nettoyage des sauvegardes SQLite ignoré : " + ex.getMessage());
        }
    }

    private static long lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException ex) {
            return 0L;
        }
    }
}
