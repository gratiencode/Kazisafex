package services;

import data.BilanAgregate;
import data.CompteResultatAgregate;
import data.FinancialStatementAgregate;
import data.FluxTresorerieAgregate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import tools.FinancialRowModel;

public class FinancialStatementSyncService {

    public static final String BILAN = "BILAN";
    public static final String COMPTE_RESULTAT = "COMPTE_RESULTAT";
    public static final String FLUX_TRESORERIE = "FLUX_TRESORERIE";

    private static final String[] SYNC_STATEMENT_TYPES = {BILAN, COMPTE_RESULTAT, FLUX_TRESORERIE};
    private static final List<String> MANAGED_LINE_CODES = List.of(
            "CA", "COUT_VENTES", "MARGE_BRUTE", "DEPENSES", "RN",
            "ACT_IMMO", "IMMO_BRUT", "IMMO_AMORT", "IMMO_NET", "ACT_CIRC", "STOCK", "CREANCES",
            "DISPO", "TOTAL_ACTIF", "CAPITAUX", "DETTES_FOURN", "TRESO_PASSIF", "TOTAL_PASSIF",
            "ENCAISSEMENTS", "DECAISSEMENTS", "FLUX_NET", "DISPO_FIN");

    public void synchronizeFiscalYear(EntityManager em, int year, String region) {
        for (LocalDate quarterStart : quarterStartsOfYear(year)) {
            PeriodRange quarter = normalizedQuarterRange(quarterStart);
            if (!quarter.start().isAfter(LocalDate.now())) {
                synchronizeQuarterPeriod(em, quarter.start(), quarter.end(), region);
            }
        }
    }

    public void synchronizeFiscalYears(EntityManager em, List<Integer> years, String region) {
        for (Integer year : years) {
            if (year != null) {
                synchronizeFiscalYear(em, year, region);
            }
        }
    }

    public void synchronizeFiscalYearUsingApplicationEntityManager(int year, String region) {
        runWrite(em -> {
            synchronizeFiscalYear(em, year, region);
            return null;
        });
    }

    public List<FinancialRowModel> synchronizeAndLoadYearPivot(List<Integer> years, String region) {
        List<Integer> orderedYears = years.stream().distinct().sorted().toList();
        runWrite(em -> {
            synchronizeFiscalYears(em, orderedYears, region);
            return null;
        });
        return runRead(em -> loadYearPivot(em, orderedYears, region, List.of(BILAN, COMPTE_RESULTAT, FLUX_TRESORERIE)));
    }

    public List<FinancialRowModel> loadYearPivotUsingApplicationEntityManager(List<Integer> years, String region,
            List<String> statementTypes) {
        List<Integer> orderedYears = years == null ? List.of() : years.stream().distinct().sorted().toList();
        if (orderedYears.isEmpty()) {
            return List.of();
        }
        return runRead(em -> loadYearPivot(em, orderedYears, region, statementTypes));
    }

    public List<Integer> findAvailablePivotYearsUsingApplicationEntityManager(List<Integer> years, String region,
            List<String> statementTypes) {
        List<Integer> orderedYears = years == null ? List.of() : years.stream().distinct().sorted().toList();
        if (orderedYears.isEmpty()) {
            return List.of();
        }
        return runRead(em -> findAvailablePivotYears(em, orderedYears, region, statementTypes));
    }

    public List<Integer> findAvailablePivotYears(EntityManager em, List<Integer> years, String region,
            List<String> statementTypes) {
        List<Integer> orderedYears = years == null ? List.of() : years.stream().distinct().sorted().toList();
        if (orderedYears.isEmpty()) {
            return List.of();
        }
        List<String> usedStatementTypes = statementTypes == null || statementTypes.isEmpty()
                ? List.of(SYNC_STATEMENT_TYPES)
                : statementTypes;
        String yearExpression = "fiscal_year";
        String sourceSql = unionSourceSql(usedStatementTypes);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
                .append(yearExpression)
                .append(" AS pivot_year\n")
                .append("FROM (")
                .append(sourceSql)
                .append(") f\n")
                .append("WHERE region = :region\n")
                .append("  AND ")
                .append(yearExpression)
                .append(" IN (");
        for (int i = 0; i < orderedYears.size(); i++) {
            if (i > 0) {
                sql.append(',');
            }
            sql.append(orderedYears.get(i));
        }
        sql.append(")\n")
                .append("GROUP BY ")
                .append(yearExpression)
                .append("\n")
                .append("HAVING SUM(ABS(COALESCE(amount_usd, 0))) > 0\n")
                .append("ORDER BY pivot_year");

        Query query = em.createNativeQuery(sql.toString())
                .setParameter("region", normalizeRegion(region));
        List<?> rawYears = query.getResultList();
        List<Integer> availableYears = new ArrayList<>();
        for (Object rawYear : rawYears) {
            availableYears.add(asInt(rawYear));
        }
        return availableYears;
    }

    public List<FinancialRowModel> loadYearPivot(EntityManager em, List<Integer> years, String region,
            List<String> statementTypes) {
        if (years == null || years.isEmpty()) {
            return List.of();
        }
        List<Integer> orderedYears = years.stream().distinct().sorted().toList();
        List<String> usedStatementTypes = statementTypes == null || statementTypes.isEmpty()
                ? List.of(SYNC_STATEMENT_TYPES)
                : statementTypes;
        String yearExpression = "fiscal_year";
        String sourceSql = unionSourceSql(usedStatementTypes);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT\n")
                .append("    MIN(sort_order) AS sort_order,\n")
                .append("    line_code,\n")
                .append("    statement_type,\n")
                .append("    rubrique,\n")
                .append("    MAX(COALESCE(nature, '')) AS nature,\n")
                .append("    MAX(CASE WHEN total_line = 1 THEN 1 ELSE 0 END) AS is_total,\n")
                .append("    MAX(CASE WHEN section_header = 1 THEN 1 ELSE 0 END) AS is_section\n");
        for (Integer year : orderedYears) {
            sql.append(", SUM(CASE WHEN ")
                    .append(yearExpression)
                    .append(" = ")
                    .append(year)
                    .append(" THEN COALESCE(amount_usd, 0) ELSE 0 END) AS value_")
                    .append(year)
                    .append('\n');
        }
        sql.append("FROM (")
                .append(sourceSql)
                .append(") f\n")
                .append("WHERE region = :region\n")
                .append("  AND ")
                .append(yearExpression)
                .append(" IN (");
        for (int i = 0; i < orderedYears.size(); i++) {
            if (i > 0) {
                sql.append(',');
            }
            sql.append(orderedYears.get(i));
        }
        sql.append(")\n")
                .append("GROUP BY line_code, statement_type, rubrique\n")
                .append("ORDER BY MIN(sort_order), statement_type, line_code");

        Query query = em.createNativeQuery(sql.toString())
                .setParameter("region", normalizeRegion(region));
        List<?> rawRows = query.getResultList();
        List<FinancialRowModel> rows = new ArrayList<>();
        for (Object raw : rawRows) {
            Object[] columns = (Object[]) raw;
            FinancialRowModel row = new FinancialRowModel();
            row.setSortOrder(asInt(columns[0]));
            row.setLineCode(asString(columns[1]));
            row.setStatementType(asString(columns[2]));
            row.setRubrique(asString(columns[3]));
            row.setNature(asString(columns[4]));
            row.setTotal(asBoolean(columns[5]));
            row.setSectionHeader(asBoolean(columns[6]));
            for (int i = 0; i < orderedYears.size(); i++) {
                row.setValueForYear(orderedYears.get(i), asDouble(columns[7 + i]));
            }
            rows.add(row);
        }
        rows.sort(Comparator.comparingInt(FinancialRowModel::getSortOrder)
                .thenComparing(FinancialRowModel::getStatementType)
                .thenComparing(FinancialRowModel::getLineCode));
        return rows;
    }

    public void synchronizePeriod(EntityManager em, LocalDate start, LocalDate end, String region) {
        if (start == null || end == null) {
            return;
        }
        for (PeriodRange quarter : quarterRangesBetween(start, end)) {
            synchronizeQuarterPeriod(em, quarter.start(), quarter.end(), region);
        }
    }

    private void synchronizeQuarterPeriod(EntityManager em, LocalDate start, LocalDate end, String region) {
        EntityTransaction tx = em.getTransaction();
        boolean started = false;
        if (!tx.isActive()) {
            tx.begin();
            started = true;
        }
        try {
            String usedRegion = normalizeRegion(region);
            deleteExistingRows(em, start, end, usedRegion);
            Map<String, FinancialStatementAgregate> rows = new LinkedHashMap<>();
            addIncomeRows(em, rows, start, end, usedRegion);
            addBalanceRows(em, rows, start, end, usedRegion);
            addCashFlowRows(em, rows, start, end, usedRegion);
            for (FinancialStatementAgregate row : rows.values()) {
                em.merge(toConcrete(row));
            }
            if (started) {
                tx.commit();
            }
        } catch (RuntimeException ex) {
            if (started && tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        }
    }

    private void deleteExistingRows(EntityManager em, LocalDate start, LocalDate end, String region) {
        String code = periodCode(start);
        for (String statementType : SYNC_STATEMENT_TYPES) {
            em.createQuery("""
                    DELETE FROM %s f
                    WHERE f.fiscalYear = :fiscalYear
                      AND f.periodCode = :periodCode
                      AND f.region = :region
                      AND f.lineCode IN :lineCodes
                    """.formatted(entityNameFor(statementType)))
                    .setParameter("fiscalYear", start.getYear())
                    .setParameter("periodCode", code)
                    .setParameter("region", region)
                    .setParameter("lineCodes", MANAGED_LINE_CODES)
                    .executeUpdate();
        }
    }

    private void addIncomeRows(EntityManager em, Map<String, FinancialStatementAgregate> rows,
            LocalDate start, LocalDate end, String region) {
        LocalDateTime startDate = start.atStartOfDay();
        LocalDateTime endDate = end.atTime(23, 59, 59);
        double sales = singleDouble(em, """
                SELECT COALESCE(SUM(s.totalSaleUsd), 0)
                FROM SaleAgregate s
                WHERE s.date BETWEEN :startDate AND :endDate AND s.region LIKE :region
                """, startDate, endDate, region);
        double costOfSales = singleDouble(em, """
                SELECT COALESCE(SUM(s.coutAchatTotal), 0)
                FROM SaleAgregate s
                WHERE s.date BETWEEN :startDate AND :endDate AND s.region LIKE :region
                """, startDate, endDate, region);
        double expenses = singleDouble(em, """
                SELECT COALESCE(SUM(d.montantUsd), 0)
                FROM DepenseAgregate d
                WHERE d.date BETWEEN :startDate AND :endDate AND d.region LIKE :region
                """, startDate, endDate, region);
        put(rows, COMPTE_RESULTAT, 10, "CA", "Chiffre d'affaires", "Ventes agrégées", sales, start, end, region, false, false);
        put(rows, COMPTE_RESULTAT, 20, "COUT_VENTES", "Coût d'achat des ventes", "Coût variable issu de sale_agregate", -costOfSales, start, end, region, false, false);
        put(rows, COMPTE_RESULTAT, 30, "MARGE_BRUTE", "Marge brute", "CA - Coût d'achat", sales - costOfSales, start, end, region, false, true);
        put(rows, COMPTE_RESULTAT, 40, "DEPENSES", "Charges d'exploitation", "Dépenses agrégées", -expenses, start, end, region, false, false);
        put(rows, COMPTE_RESULTAT, 90, "RN", "Résultat net", "Marge brute - Charges", sales - costOfSales - expenses, start, end, region, false, true);
    }

    private void addBalanceRows(EntityManager em, Map<String, FinancialStatementAgregate> rows,
            LocalDate start, LocalDate end, String region) {
        double stock = stockValue(em, end, region);
        double receivables = latestAmount(em, "creance_agregate", end, region);
        double payables = latestAmount(em, "dette_fournisseur_agregate", end, region);
        double cash = cashBalance(em, end, region);
        ImmobilisationAmounts immo = immobilisationAmounts(em, end, region);
        double currentAssets = stock + receivables + Math.max(0d, cash);
        double totalAssets = immo.net + currentAssets;
        double equity = Math.max(0d, totalAssets - payables - Math.max(0d, -cash));
        double totalLiabilities = equity + payables + Math.max(0d, -cash);
        put(rows, BILAN, 100, "ACT_IMMO", "Actif immobilisé net", "Immobilisations nettes", immo.net, start, end, region, true, false);
        put(rows, BILAN, 110, "IMMO_BRUT", "Immobilisations - valeur brute", "Brut immobilisation_agregate", immo.gross, start, end, region, false, false);
        put(rows, BILAN, 120, "IMMO_AMORT", "Amortissements cumulés", "Amortissement immobilisation_agregate", -immo.amortization, start, end, region, false, false);
        put(rows, BILAN, 130, "IMMO_NET", "Immobilisations - valeur nette", "Net immobilisation_agregate", immo.net, start, end, region, false, true);
        put(rows, BILAN, 200, "ACT_CIRC", "Actif circulant", "Stocks + créances + disponibilités", currentAssets, start, end, region, true, false);
        put(rows, BILAN, 210, "STOCK", "Stocks et encours", "stock_final * cout_achat", stock, start, end, region, false, false);
        put(rows, BILAN, 220, "CREANCES", "Créances clients", "creance_agregate", receivables, start, end, region, false, false);
        put(rows, BILAN, 230, "DISPO", "Disponibilités", "Solde trésorerie positif", Math.max(0d, cash), start, end, region, false, false);
        put(rows, BILAN, 290, "TOTAL_ACTIF", "Total actif", "Actif immobilisé + actif circulant", totalAssets, start, end, region, false, true);
        put(rows, BILAN, 300, "CAPITAUX", "Capitaux propres estimés", "Solde de bouclage local", equity, start, end, region, true, false);
        put(rows, BILAN, 410, "DETTES_FOURN", "Dettes fournisseurs", "dette_fournisseur_agregate", payables, start, end, region, false, false);
        put(rows, BILAN, 420, "TRESO_PASSIF", "Trésorerie-passif", "Solde trésorerie négatif", Math.max(0d, -cash), start, end, region, false, false);
        put(rows, BILAN, 490, "TOTAL_PASSIF", "Total passif", "Capitaux + dettes", totalLiabilities, start, end, region, false, true);
    }

    private void addCashFlowRows(EntityManager em, Map<String, FinancialStatementAgregate> rows,
            LocalDate start, LocalDate end, String region) {
        double inflows = treasuryFlow(em, start, end, region, true);
        double outflows = treasuryFlow(em, start, end, region, false);
        double net = inflows - outflows;
        put(rows, FLUX_TRESORERIE, 10, "ENCAISSEMENTS", "Encaissements", "Trésorerie entrante", inflows, start, end, region, false, false);
        put(rows, FLUX_TRESORERIE, 20, "DECAISSEMENTS", "Décaissements", "Trésorerie sortante", -outflows, start, end, region, false, false);
        put(rows, FLUX_TRESORERIE, 90, "FLUX_NET", "Flux net de trésorerie", "Encaissements - décaissements", net, start, end, region, false, true);
        put(rows, FLUX_TRESORERIE, 100, "DISPO_FIN", "Disponibilités fin de période", "Solde cumulé jusqu'à fin période", cashBalance(em, end, region), start, end, region, false, true);
    }

    private double singleDouble(EntityManager em, String jpql, LocalDateTime start, LocalDateTime end, String region) {
        Number number = (Number) em.createQuery(jpql)
                .setParameter("startDate", start)
                .setParameter("endDate", end)
                .setParameter("region", region)
                .getSingleResult();
        return number == null ? 0d : number.doubleValue();
    }

    private double stockValue(EntityManager em, LocalDate end, String region) {
        String sql = """
                SELECT COALESCE(SUM(COALESCE(final_quantity, 0) * COALESCE(cout_achat, 0)), 0)
                FROM stock_agregate
                WHERE date = (SELECT MAX(date) FROM stock_agregate WHERE date <= :endDate AND region LIKE :region)
                  AND region LIKE :region
                """;
        return nativeDouble(em, sql, Map.of("endDate", end, "region", region));
    }

    private double latestAmount(EntityManager em, String table, LocalDate end, String region) {
        String sql = "SELECT COALESCE(SUM(COALESCE(montant_usd, 0)), 0) FROM " + table
                + " WHERE date = (SELECT MAX(date) FROM " + table + " WHERE date <= :endDate AND region LIKE :region)"
                + " AND region LIKE :region";
        return nativeDouble(em, sql, Map.of("endDate", end, "region", region));
    }

    private ImmobilisationAmounts immobilisationAmounts(EntityManager em, LocalDate end, String region) {
        String sql = """
                SELECT
                    COALESCE(SUM(COALESCE(valeur_brutte, 0)), 0),
                    COALESCE(SUM(COALESCE(ammortissement, 0)), 0),
                    COALESCE(SUM(COALESCE(valeur_nette, 0)), 0)
                FROM immobilisation_agregate
                WHERE DATE(date) = (
                    SELECT MAX(DATE(date)) FROM immobilisation_agregate WHERE DATE(date) <= :endDate AND region LIKE :region
                )
                AND region LIKE :region
                """;
        Object[] row = (Object[]) em.createNativeQuery(sql)
                .setParameter("endDate", end)
                .setParameter("region", region)
                .getSingleResult();
        return new ImmobilisationAmounts(asDouble(row[0]), asDouble(row[1]), asDouble(row[2]));
    }

    private double treasuryFlow(EntityManager em, LocalDate start, LocalDate end, String region, boolean incoming) {
        String sql = """
                SELECT COALESCE(SUM(COALESCE(montant_usd, 0)), 0)
                FROM tresorerie_agregate
                WHERE date BETWEEN :startDate AND :endDate
                  AND region LIKE :region
                  AND UPPER(mouvement) IN (:mouvements)
                """;
        List<String> movements = incoming ? List.of("IN", "AUGMENTATION", "CASH", "CREDIT")
                : List.of("OUT", "DIMINUTION");
        Query query = em.createNativeQuery(sql)
                .setParameter("startDate", start)
                .setParameter("endDate", end)
                .setParameter("region", region)
                .setParameter("mouvements", movements);
        return asDouble(query.getSingleResult());
    }

    private double cashBalance(EntityManager em, LocalDate end, String region) {
        String sql = """
                SELECT COALESCE(SUM(CASE
                    WHEN UPPER(mouvement) IN ('IN', 'AUGMENTATION', 'CASH', 'CREDIT') THEN COALESCE(montant_usd, 0)
                    WHEN UPPER(mouvement) IN ('OUT', 'DIMINUTION') THEN -COALESCE(montant_usd, 0)
                    ELSE 0
                END), 0)
                FROM tresorerie_agregate
                WHERE date <= :endDate AND region LIKE :region
                """;
        return nativeDouble(em, sql, Map.of("endDate", end, "region", region));
    }

    private double nativeDouble(EntityManager em, String sql, Map<String, Object> params) {
        Query query = em.createNativeQuery(sql);
        params.forEach(query::setParameter);
        return asDouble(query.getSingleResult());
    }

    private void put(Map<String, FinancialStatementAgregate> rows, String type, int order, String code, String rubrique,
            String nature, double amount, LocalDate start, LocalDate end, String region, boolean section, boolean total) {
        FinancialStatementAgregate row = newRow(type);
        row.setStatementType(type);
        row.setSortOrder(order);
        row.setLineCode(code);
        row.setRubrique(rubrique);
        row.setNature(nature);
        row.setAmountUsd(amount);
        row.setPeriodStart(start);
        row.setPeriodEnd(end);
        row.setFiscalYear(start == null ? null : start.getYear());
        row.setPeriodCode(start == null ? null : periodCode(start));
        row.setRegion(region);
        row.setSectionHeader(section);
        row.setTotalLine(total);
        rows.put(type + "|" + code, row);
    }

    private String unionSourceSql(List<String> statementTypes) {
        List<String> usedTypes = statementTypes == null || statementTypes.isEmpty()
                ? List.of(SYNC_STATEMENT_TYPES)
                : statementTypes;
        StringBuilder sql = new StringBuilder();
        for (String statementType : usedTypes) {
            if (sql.length() > 0) {
                sql.append("\nUNION ALL\n");
            }
            sql.append("SELECT uid, statement_type, line_code, rubrique, nature, amount_usd, ")
                    .append("period_start, period_end, fiscal_year, period_code, region, sort_order, section_header, total_line, updated_at ")
                    .append("FROM ")
                    .append(tableFor(statementType));
        }
        return sql.toString();
    }

    private FinancialStatementAgregate newRow(String statementType) {
        return switch (statementType) {
            case BILAN -> new BilanAgregate();
            case COMPTE_RESULTAT -> new CompteResultatAgregate();
            case FLUX_TRESORERIE -> new FluxTresorerieAgregate();
            default -> throw new IllegalArgumentException("Type d'etat financier inconnu: " + statementType);
        };
    }

    private FinancialStatementAgregate toConcrete(FinancialStatementAgregate row) {
        FinancialStatementAgregate target = newRow(row.getStatementType());
        target.setUid(row.getUid());
        target.setStatementType(row.getStatementType());
        target.setLineCode(row.getLineCode());
        target.setRubrique(row.getRubrique());
        target.setNature(row.getNature());
        target.setAmountUsd(row.getAmountUsd());
        target.setPeriodStart(row.getPeriodStart());
        target.setPeriodEnd(row.getPeriodEnd());
        target.setFiscalYear(row.getFiscalYear());
        target.setPeriodCode(row.getPeriodCode());
        target.setRegion(row.getRegion());
        target.setSortOrder(row.getSortOrder());
        target.setSectionHeader(row.getSectionHeader());
        target.setTotalLine(row.getTotalLine());
        target.setUpdatedAt(row.getUpdatedAt());
        return target;
    }

    private String entityNameFor(String statementType) {
        return switch (statementType) {
            case BILAN -> "BilanAgregate";
            case COMPTE_RESULTAT -> "CompteResultatAgregate";
            case FLUX_TRESORERIE -> "FluxTresorerieAgregate";
            default -> throw new IllegalArgumentException("Type d'etat financier inconnu: " + statementType);
        };
    }

    private String tableFor(String statementType) {
        return switch (statementType) {
            case BILAN -> "bilan_agregate";
            case COMPTE_RESULTAT -> "compte_resultat_agregate";
            case FLUX_TRESORERIE -> "flux_tresorerie_agregate";
            default -> throw new IllegalArgumentException("Type d'etat financier inconnu: " + statementType);
        };
    }

    private <T> T runWrite(java.util.function.Function<EntityManager, T> action) {
        return ManagedSessionFactory.executeWrite(action);
    }

    private <T> T runRead(java.util.function.Function<EntityManager, T> action) {
        return ManagedSessionFactory.executeRead(action);
    }

    private String normalizeRegion(String region) {
        return region == null || region.isBlank() ? "%" : region;
    }

    private int asInt(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private boolean asBoolean(Object value) {
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof Number n) {
            return n.intValue() != 0;
        }
        return "1".equals(String.valueOf(value)) || Boolean.parseBoolean(String.valueOf(value));
    }

    private double asDouble(Object value) {
        if (value == null) {
            return 0d;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().getYear();
        }
        if (value instanceof Date date) {
            return date.toLocalDate().getYear();
        }
        return Double.parseDouble(String.valueOf(value));
    }

    public List<LocalDate[]> quarterPeriods(int year) {
        List<LocalDate[]> periods = new ArrayList<>();
        for (int month : List.of(1, 4, 7, 10)) {
            LocalDate start = LocalDate.of(year, month, 1);
            periods.add(new LocalDate[]{start, YearMonth.from(start).plusMonths(2).atEndOfMonth()});
        }
        return periods;
    }

    private List<LocalDate> quarterStartsOfYear(int year) {
        return List.of(
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 4, 1),
                LocalDate.of(year, 7, 1),
                LocalDate.of(year, 10, 1));
    }

    private PeriodRange normalizedQuarterRange(LocalDate anyDateInQuarter) {
        LocalDate start = quarterStart(anyDateInQuarter);
        LocalDate end = quarterEnd(start);
        LocalDate today = LocalDate.now();
        if (!start.isAfter(today) && !end.isBefore(today)) {
            end = today;
        }
        return new PeriodRange(start, end);
    }

    private List<PeriodRange> quarterRangesBetween(LocalDate start, LocalDate end) {
        LocalDate safeStart = start.isAfter(end) ? end : start;
        LocalDate safeEnd = end.isBefore(start) ? start : end;
        LocalDate today = LocalDate.now();
        List<PeriodRange> ranges = new ArrayList<>();
        LocalDate cursor = quarterStart(safeStart);
        while (!cursor.isAfter(safeEnd) && !cursor.isAfter(today)) {
            LocalDate qEnd = quarterEnd(cursor);
            LocalDate effectiveEnd = minDate(qEnd, safeEnd);
            if (!cursor.isAfter(today) && !qEnd.isBefore(today)) {
                effectiveEnd = minDate(effectiveEnd, today);
            }
            ranges.add(new PeriodRange(cursor, effectiveEnd));
            cursor = qEnd.plusDays(1);
        }
        return ranges;
    }

    private LocalDate quarterStart(LocalDate date) {
        int quarter = quarterNumber(date);
        int startMonth = (quarter - 1) * 3 + 1;
        return LocalDate.of(date.getYear(), startMonth, 1);
    }

    private LocalDate quarterEnd(LocalDate date) {
        int quarter = quarterNumber(date);
        int endMonth = quarter * 3;
        return YearMonth.of(date.getYear(), endMonth).atEndOfMonth();
    }

    private int quarterNumber(LocalDate date) {
        return ((date.getMonthValue() - 1) / 3) + 1;
    }

    private String periodCode(LocalDate quarterStart) {
        return "T" + quarterNumber(quarterStart) + "-" + quarterStart.getYear();
    }

    private LocalDate minDate(LocalDate a, LocalDate b) {
        return a.isBefore(b) ? a : b;
    }

    private record ImmobilisationAmounts(double gross, double amortization, double net) {
    }

    private record PeriodRange(LocalDate start, LocalDate end) {
    }
}
