package services;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

/**
 * Assure les ajustements minimaux de schema au demarrage pour rester coherent
 * avec les entites JPA, meme quand le dialecte ne supporte pas les ALTER.
 */
public final class SchemaAutoUpdater {

    private SchemaAutoUpdater() {
    }

    public static void ensureCoreSchema(boolean embedded, String dbUrl, String dbUser, String dbPassword) {
        if (dbUrl == null || dbUrl.isBlank()) {
            throw new IllegalArgumentException("URL de base de donnees introuvable.");
        }

        try (Connection connection = openConnection(embedded, dbUrl, dbUser, dbPassword)) {
            ensureStockAgregateSchema(connection);
        } catch (SQLException ex) {
            throw new IllegalStateException("Echec de mise a jour automatique du schema: " + ex.getMessage(), ex);
        }
    }

    private static Connection openConnection(boolean embedded, String dbUrl, String dbUser, String dbPassword)
            throws SQLException {
        if (embedded) {
            return DriverManager.getConnection(dbUrl);
        }
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    private static void ensureStockAgregateSchema(Connection connection) throws SQLException {
        if (!tableExists(connection, "stock_agregate")) {
            return;
        }
        addColumnIfMissing(connection, "stock_agregate", "num_lot", "VARCHAR(100)");
        addColumnIfMissing(connection, "stock_agregate", "date_expiration", "DATE");
    }

    private static void addColumnIfMissing(Connection connection, String table, String column, String type)
            throws SQLException {
        if (columnExists(connection, table, column)) {
            return;
        }
        String sql = "ALTER TABLE " + table + " ADD COLUMN " + column + " " + type;
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private static boolean tableExists(Connection connection, String table) throws SQLException {
        if (isSQLite(connection)) {
            String sql = "SELECT 1 FROM sqlite_master WHERE type='table' AND lower(name)=lower(?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, table);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        }

        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();
        for (String candidate : variants(table)) {
            try (ResultSet rs = metaData.getTables(catalog, null, candidate, new String[]{"TABLE"})) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean columnExists(Connection connection, String table, String column) throws SQLException {
        if (isSQLite(connection)) {
            String sql = "PRAGMA table_info(" + table + ")";
            try (Statement statement = connection.createStatement();
                    ResultSet rs = statement.executeQuery(sql)) {
                while (rs.next()) {
                    String existing = rs.getString("name");
                    if (existing != null && existing.equalsIgnoreCase(column)) {
                        return true;
                    }
                }
            }
            return false;
        }

        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();
        for (String tableCandidate : variants(table)) {
            for (String columnCandidate : variants(column)) {
                try (ResultSet rs = metaData.getColumns(catalog, null, tableCandidate, columnCandidate)) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isSQLite(Connection connection) throws SQLException {
        String url = connection.getMetaData().getURL();
        return url != null && url.toLowerCase(Locale.ROOT).contains(":sqlite:");
    }

    private static String[] variants(String value) {
        return new String[]{value, value.toLowerCase(Locale.ROOT), value.toUpperCase(Locale.ROOT)};
    }
}
