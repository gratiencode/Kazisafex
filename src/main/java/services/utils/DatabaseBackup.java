package services.utils;

import java.io.IOException;
import java.nio.file.Files;
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
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

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
            prune(directory, base, Math.max(1, keep));
            return target;
        } catch (IOException | RuntimeException ex) {
            LOGGER.log(Level.WARNING,
                    "Sauvegarde SQLite impossible avant migration : " + ex.getMessage());
            return null;
        }
    }

    private static Path findTodayBackup(Path directory, String base, String today) {
        try (var stream = Files.list(directory)) {
            return stream
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        return name.startsWith(base + ".bak-" + today)
                                && !name.endsWith("-wal")
                                && !name.endsWith("-shm");
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
                                && !name.endsWith("-shm");
                    })
                    .sorted(Comparator.comparingLong(DatabaseBackup::lastModified).reversed())
                    .toList();
            for (int i = keep; i < backups.size(); i++) {
                Files.deleteIfExists(backups.get(i));
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
