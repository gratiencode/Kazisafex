package services.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Durcissement des permissions du stockage SQLite local.
 *
 * <p>SQLCipher n'est pas encore actif ; en attendant la migration chiffrée,
 * la base, ses fichiers WAL/SHM et le répertoire de données doivent au moins
 * être inaccessibles aux autres comptes du poste. Sur POSIX : répertoire
 * {@code 0700}, fichiers {@code 0600}. Sur Windows, l'ACL est gérée par le
 * profil utilisateur ; la méthode ne tente pas de forcer un mode POSIX.</p>
 *
 * <p>Le durcissement est best-effort : un échec est journalisé, jamais
 * propagé au démarrage, car il ne doit pas empêcher l'application de
 * fonctionner sur un système de fichiers sans POSIX.</p>
 */
public final class DatabaseFileSecurity {

    private static final Logger LOGGER = Logger.getLogger(DatabaseFileSecurity.class.getName());
    private static final Set<PosixFilePermission> FILE_PERMISSIONS =
            PosixFilePermissions.fromString("rw-------");
    private static final Set<PosixFilePermission> DIRECTORY_PERMISSIONS =
            PosixFilePermissions.fromString("rwx------");

    private DatabaseFileSecurity() {
    }

    /** Applique les permissions minimales au fichier SQLite et à ses annexes. */
    public static void harden(String databasePath) {
        if (databasePath == null || databasePath.isBlank()) {
            return;
        }
        Path database = Path.of(databasePath);
        Path directory = database.getParent();
        if (directory != null) {
            setPermissions(directory, DIRECTORY_PERMISSIONS);
        }
        setPermissions(database, FILE_PERMISSIONS);
        setPermissions(Path.of(databasePath + "-wal"), FILE_PERMISSIONS);
        setPermissions(Path.of(databasePath + "-shm"), FILE_PERMISSIONS);
    }

    private static void setPermissions(Path path, Set<PosixFilePermission> permissions) {
        if (path == null || !Files.exists(path)) {
            return;
        }
        try {
            Files.setPosixFilePermissions(path, permissions);
        } catch (UnsupportedOperationException | IOException | SecurityException ex) {
            LOGGER.log(Level.FINE,
                    "Permissions POSIX non appliquées à " + path + " : " + ex.getMessage());
        }
    }
}
