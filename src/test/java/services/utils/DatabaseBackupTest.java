package services.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabaseBackupTest {

    @TempDir
    Path dir;

    @Test
    void copiesDatabaseBeforeMigration() throws Exception {
        Path database = dir.resolve("kazi-test.db");
        Files.writeString(database, "sqlite-content");

        Path backup = DatabaseBackup.backupBeforeMigration(database.toString(), 5);

        assertNotNull(backup);
        assertTrue(Files.isRegularFile(backup));
        assertTrue(Files.readString(backup).contains("sqlite-content"));
        Path second = DatabaseBackup.backupBeforeMigration(database.toString(), 5);
        assertEquals(backup, second, "une seule sauvegarde par jour");
    }

    @Test
    void immediateBackupCarriesOperationLabel() throws Exception {
        Path database = dir.resolve("kazi-test.db");
        Files.writeString(database, "sqlite-content");

        Path backup = DatabaseBackup.backupNow(database.toString(), "inventory-close", 5);

        assertNotNull(backup);
        assertTrue(backup.getFileName().toString().contains("inventory-close"));
        assertTrue(Files.readString(backup).contains("sqlite-content"));
    }

    @Test
    void missingDatabaseHasNoBackup() {
        assertNull(DatabaseBackup.backupBeforeMigration(dir.resolve("absent.db").toString(), 5));
    }
}
