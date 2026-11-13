package services;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaAutoUpdaterTest {

    @Test
    void duplicateIndexError_reconnaitLeCodeMySql1061() {
        assertTrue(SchemaAutoUpdater.isDuplicateIndexError(
                new SQLException("Duplicate key name 'idx_sale_agregate_date_region'", "42000", 1061)));
    }

    @Test
    void migrationAjouteLaPhotographieTheoriqueSansToucherAuxDonnees(@TempDir Path dir) throws Exception {
        String url = "jdbc:sqlite:" + dir.resolve("kazisafe-schema.db");
        try (Connection c = DriverManager.getConnection(url);
                Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE compter (uid VARCHAR(64) PRIMARY KEY, quantite DOUBLE)");
            st.executeUpdate("INSERT INTO compter(uid, quantite) VALUES ('c1', 3)");
        }

        SchemaAutoUpdater.ensureCoreSchema(true, url, null, null);
        // Deuxieme passage : la migration doit etre idempotente.
        SchemaAutoUpdater.ensureCoreSchema(true, url, null, null);

        try (Connection c = DriverManager.getConnection(url);
                ResultSet rs = c.getMetaData().getColumns(null, null, "compter", "theoretical_snapshot")) {
            assertTrue(rs.next(), "la colonne theoretical_snapshot doit exister");
        }
        try (Connection c = DriverManager.getConnection(url);
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM compter")) {
            assertTrue(rs.next());
            assertTrue(rs.getInt(1) == 1, "les lignes existantes ne doivent pas etre perdues");
        }
    }

    @Test
    void duplicateIndexError_reconnaitLeMessageEtNonUnAutreEchec() {
        assertTrue(SchemaAutoUpdater.isDuplicateIndexError(
                new SQLException("Index idx_x already exists")));
        assertFalse(SchemaAutoUpdater.isDuplicateIndexError(
                new SQLException("Table 'foo' doesn't exist", "42S02", 1146)));
    }
}
