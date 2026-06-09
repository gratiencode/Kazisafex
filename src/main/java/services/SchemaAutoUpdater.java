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
            ensureAggregateSchema(connection);
            ensureStockAgregateSchema(connection);
            ensureFinancialStatementSchema(connection);
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

    private static void ensureAggregateSchema(Connection connection) throws SQLException {
        createTableIfMissing(connection, "creance_agregate", """
                CREATE TABLE IF NOT EXISTS creance_agregate (
                    uid VARCHAR(64) NOT NULL PRIMARY KEY,
                    date DATE,
                    region VARCHAR(120),
                    montant_usd DOUBLE,
                    updated_at DATETIME,
                    deleted_at DATETIME
                )
                """);
        createTableIfMissing(connection, "dette_fournisseur_agregate", """
                CREATE TABLE IF NOT EXISTS dette_fournisseur_agregate (
                    uid VARCHAR(64) NOT NULL PRIMARY KEY,
                    date DATE,
                    region VARCHAR(120),
                    montant_usd DOUBLE,
                    fournisseur_id VARCHAR(64),
                    updated_at DATETIME,
                    deleted_at DATETIME
                )
                """);
        createTableIfMissing(connection, "tresorerie_agregate", """
                CREATE TABLE IF NOT EXISTS tresorerie_agregate (
                    uid VARCHAR(64) NOT NULL PRIMARY KEY,
                    date DATE,
                    region VARCHAR(120),
                    mouvement VARCHAR(120),
                    categorie VARCHAR(120),
                    montant_usd DOUBLE,
                    montant_cdf DOUBLE,
                    updated_at DATETIME,
                    deleted_at DATETIME
                )
                """);
        createTableIfMissing(connection, "immobilisation_agregate", """
                CREATE TABLE IF NOT EXISTS immobilisation_agregate (
                    uid VARCHAR(64) NOT NULL PRIMARY KEY,
                    date DATETIME,
                    valeur_brutte DOUBLE,
                    ammortissement DOUBLE,
                    valeur_nette DOUBLE,
                    region VARCHAR(120),
                    immobilisation_id VARCHAR(64),
                    updated_at DATETIME,
                    deleted_at DATETIME
                )
                """);
        createTableIfMissing(connection, "amortissement_agregate", """
                CREATE TABLE IF NOT EXISTS amortissement_agregate (
                    uid VARCHAR(64) NOT NULL PRIMARY KEY,
                    periode DATE,
                    dotation_usd DOUBLE,
                    cumul_usd DOUBLE,
                    valeur_comptable_usd DOUBLE,
                    region VARCHAR(120),
                    immobilisation_id VARCHAR(64),
                    updated_at DATETIME,
                    deleted_at DATETIME
                )
                """);
        createTableIfMissing(connection, "stock_depot_agregate", """
                CREATE TABLE IF NOT EXISTS stock_depot_agregate (
                    uid VARCHAR(64) NOT NULL PRIMARY KEY,
                    date_record DATE,
                    region VARCHAR(120),
                    quantite DOUBLE,
                    coutAchat DOUBLE,
                    valeurStock DOUBLE,
                    num_lot VARCHAR(120),
                    date_expiration DATE,
                    product_id VARCHAR(64),
                    mesure_id VARCHAR(64),
                    updated_at DATETIME,
                    deleted_at DATETIME
                )
                """);
        createIndexIfMissing(connection, "idx_sale_agregate_date_region",
                "sale_agregate", "date, region");
        createIndexIfMissing(connection, "idx_stock_agregate_date_region",
                "stock_agregate", "date, region");
        createIndexIfMissing(connection, "idx_stock_depot_agregate_date_region",
                "stock_depot_agregate", "date_record, region");
        createIndexIfMissing(connection, "idx_depense_agregate_date_region",
                "depense_agregate", "date, region");
        createIndexIfMissing(connection, "idx_dette_fournisseur_agregate_date_region",
                "dette_fournisseur_agregate", "date, region");
        createIndexIfMissing(connection, "idx_tresorerie_agregate_date_region",
                "tresorerie_agregate", "date, region");
        createIndexIfMissing(connection, "idx_immobilisation_agregate_date_region",
                "immobilisation_agregate", "date, region");
        createIndexIfMissing(connection, "idx_amortissement_agregate_period_region",
                "amortissement_agregate", "periode, region");
    }

    private static void ensureFinancialStatementSchema(Connection connection) throws SQLException {
        for (String table : new String[]{"bilan_agregate", "compte_resultat_agregate", "flux_tresorerie_agregate"}) {
            createFinancialStatementTableIfMissing(connection, table);
            addColumnIfMissing(connection, table, "uid", "VARCHAR(64)");
            addColumnIfMissing(connection, table, "statement_type", "VARCHAR(80)");
            addColumnIfMissing(connection, table, "line_code", "VARCHAR(80)");
            addColumnIfMissing(connection, table, "rubrique", "VARCHAR(255)");
            addColumnIfMissing(connection, table, "nature", "TEXT");
            addColumnIfMissing(connection, table, "amount_usd", "DOUBLE");
            addColumnIfMissing(connection, table, "period_start", "DATE");
            addColumnIfMissing(connection, table, "period_end", "DATE");
            addColumnIfMissing(connection, table, "fiscal_year", "INTEGER");
            addColumnIfMissing(connection, table, "period_code", "VARCHAR(20)");
            addColumnIfMissing(connection, table, "region", "VARCHAR(120)");
            addColumnIfMissing(connection, table, "sort_order", "INTEGER");
            addColumnIfMissing(connection, table, "section_header", "BOOLEAN");
            addColumnIfMissing(connection, table, "total_line", "BOOLEAN");
            addColumnIfMissing(connection, table, "updated_at", "DATETIME");
            createIndexIfMissing(connection, "idx_" + table + "_period", table, "region, period_start, period_end");
            createIndexIfMissing(connection, "idx_" + table + "_fiscal_period", table, "region, fiscal_year, period_code");
            createIndexIfMissing(connection, "idx_" + table + "_line", table, "line_code, sort_order");
        }
    }

    private static void createFinancialStatementTableIfMissing(Connection connection, String table) throws SQLException {
        createTableIfMissing(connection, table, """
                CREATE TABLE IF NOT EXISTS %s (
                    uid VARCHAR(64) NOT NULL PRIMARY KEY,
                    statement_type VARCHAR(80),
                    line_code VARCHAR(80),
                    rubrique VARCHAR(255),
                    nature TEXT,
                    amount_usd DOUBLE,
                    period_start DATE,
                    period_end DATE,
                    fiscal_year INTEGER,
                    period_code VARCHAR(20),
                    region VARCHAR(120),
                    sort_order INTEGER,
                    section_header BOOLEAN,
                    total_line BOOLEAN,
                    updated_at DATETIME
                )
                """.formatted(table));
    }

    private static void createTableIfMissing(Connection connection, String table, String sql) throws SQLException {
        if (tableExists(connection, table)) {
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
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

    private static void createIndexIfMissing(Connection connection, String indexName, String table, String columns)
            throws SQLException {
        if (!tableExists(connection, table)) {
            return;
        }
        if (indexExists(connection, table, indexName)) {
            return;
        }
        String sql = "CREATE INDEX " + indexName + " ON " + table + " (" + columns + ")";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private static boolean indexExists(Connection connection, String table, String indexName) throws SQLException {
        if (isSQLite(connection)) {
            String sql = "SELECT 1 FROM sqlite_master WHERE type='index' AND lower(name)=lower(?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, indexName);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        }

        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();
        for (String tableCandidate : variants(table)) {
            try (ResultSet rs = metaData.getIndexInfo(catalog, null, tableCandidate, false, false)) {
                while (rs.next()) {
                    String existing = rs.getString("INDEX_NAME");
                    if (existing != null && existing.equalsIgnoreCase(indexName)) {
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
