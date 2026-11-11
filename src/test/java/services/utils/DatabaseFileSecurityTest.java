package services.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabaseFileSecurityTest {

    @TempDir
    Path directory;

    @Test
    void restrictsDatabaseAndDirectoryToOwner() throws Exception {
        assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));
        Path database = directory.resolve("kazi-test.db");
        Files.createFile(database);

        DatabaseFileSecurity.harden(database.toString());

        assertEquals(PosixFilePermissions.fromString("rw-------"),
                Files.getPosixFilePermissions(database));
        assertEquals(PosixFilePermissions.fromString("rwx------"),
                Files.getPosixFilePermissions(directory));
    }

    @Test
    void ignoresMissingAnnexFiles() throws Exception {
        assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));
        Path database = directory.resolve("kazi-test.db");
        Files.createFile(database);

        DatabaseFileSecurity.harden(database.toString());

        Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(database);
        assertEquals(2, permissions.size(), "lecture + écriture propriétaire uniquement");
    }
}
