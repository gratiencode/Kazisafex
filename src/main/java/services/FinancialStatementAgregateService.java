package services;

import data.DepenseAgregate;
import data.FinancialStatementAgregate;
import data.Immobilisation;
import data.ImmobilisationAgregate;
import data.StockAgregate;
import data.Traisorerie;
import data.CreanceAgregate;
import data.DetteFournisseurAgregate;
import data.TresorerieAgregate;
import data.helpers.Mouvment;
import delegates.RepportDelegate;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import tools.Constants;
import tools.FinancialStatementRow;

public class FinancialStatementAgregateService {

    public static final String STATEMENT_BILAN = "BILAN";
    public static final String STATEMENT_COMPTE_RESULTAT = "COMPTE_RESULTAT";
    public static final String STATEMENT_FLUX_TRESORERIE = "FLUX_TRESORERIE";

    public void rebuildStatements(LocalDate start, LocalDate end, String region) {
        if (start == null || end == null) {
            return;
        }
        String usedRegion = normalizeRegion(region);
        Map<String, Double> previousBilan = previousSnapshotValues(STATEMENT_BILAN, start, usedRegion);
        Map<String, Double> previousCash = previousSnapshotValues(STATEMENT_FLUX_TRESORERIE, start, usedRegion);
        CoreMetrics metrics = buildMetrics(start, end, usedRegion, previousBilan, previousCash);
        List<FinancialStatementAgregate> rows = new ArrayList<>();
        rows.addAll(buildCompteResultatRows(start, end, usedRegion, metrics));
        Map<String, Double> resultMap = toValueMap(rows, STATEMENT_COMPTE_RESULTAT);
        rows.addAll(buildBilanRows(start, end, usedRegion, metrics, resultMap));
        Map<String, Double> bilanMap = toValueMap(rows, STATEMENT_BILAN);
        rows.addAll(buildCashFlowRows(start, end, usedRegion, metrics, bilanMap, resultMap, previousBilan, previousCash));
        replaceSnapshot(start, end, usedRegion, rows);
    }

    public void ensureYearlyStatements(int anchorYear, int span, String region) {
        int normalizedSpan = span <= 3 ? 3 : 5;
        for (int year = anchorYear - normalizedSpan + 1; year <= anchorYear; year++) {
            rebuildStatements(yearStart(year), yearEnd(year), region);
        }
    }

    public void ensureQuarterlyStatements(LocalDate anchorDate, int span, String region) {
        int normalizedSpan = span <= 3 ? 3 : 4;
        if (normalizedSpan == 4) {
            for (LocalDate quarterStart : quarterStartsOfYear(anchorDate.getYear())) {
                rebuildStatements(quarterStart, quarterEnd(quarterStart), region);
            }
            return;
        }
        LocalDate current = anchorDate;
        for (int i = 0; i < normalizedSpan; i++) {
            rebuildStatements(quarterStart(current), quarterEnd(current), region);
            current = quarterStart(current).minusDays(1);
        }
    }

    private LocalDate yearStart(int year) {
        return LocalDate.of(year, 1, 1);
    }

    private LocalDate yearEnd(int year) {
        return LocalDate.of(year, 12, 31);
    }

    private LocalDate quarterStart(LocalDate date) {
        int quarter = (date.getMonthValue() - 1) / 3 + 1;
        int startMonth = (quarter - 1) * 3 + 1;
        return LocalDate.of(date.getYear(), startMonth, 1);
    }

    private LocalDate quarterEnd(LocalDate date) {
        int quarter = (date.getMonthValue() - 1) / 3 + 1;
        int endMonth = quarter * 3;
        java.time.YearMonth ym = java.time.YearMonth.of(date.getYear(), endMonth);
        return ym.atEndOfMonth();
    }

    public List<FinancialStatementRow> loadStatementRows(String statementType, LocalDate start, LocalDate end, String region) {
        String usedRegion = normalizeRegion(region);
        List<FinancialStatementAgregate> current = findRows(statementType, start, end, usedRegion);
        if (current.isEmpty()) {
            return Collections.emptyList();
        }
        List<PeriodRange> history = findPreviousPeriods(statementType, start, usedRegion, 3);
        List<Map<String, Double>> previousMaps = new ArrayList<>();
        for (PeriodRange period : history) {
            previousMaps.add(toValueMap(findRows(statementType, period.start(), period.end(), usedRegion), statementType));
        }
        List<FinancialStatementRow> result = new ArrayList<>();
        current.sort(Comparator.comparing(FinancialStatementAgregate::getSortOrder));
        for (FinancialStatementAgregate line : current) {
            result.add(new FinancialStatementRow(
                    line.getLineCode(),
                    line.getRubrique(),
                    line.getNature(),
                    zero(line.getAmountUsd()),
                    getPriorAmount(previousMaps, line.getLineCode(), 0),
                    getPriorAmount(previousMaps, line.getLineCode(), 1),
                    getPriorAmount(previousMaps, line.getLineCode(), 2),
                    0d,
                    Boolean.TRUE.equals(line.getSectionHeader()),
                    Boolean.TRUE.equals(line.getTotalLine())));
        }
        return enrichBilanImmobilisationColumns(statementType, result, end, usedRegion);
    }

    public List<FinancialStatementRow> loadStatementRows(String statementType, int anchorYear, int span, String region) {
        int normalizedSpan = span <= 3 ? 3 : 5;
        String usedRegion = normalizeRegion(region);
        List<FinancialStatementAgregate> current = findRows(statementType, yearStart(anchorYear), yearEnd(anchorYear), usedRegion);
        if (current.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Integer, Map<String, Double>> yearlyMaps = new LinkedHashMap<>();
        for (int year = anchorYear; year >= anchorYear - normalizedSpan + 1; year--) {
            yearlyMaps.put(year, toValueMap(findRows(statementType, yearStart(year), yearEnd(year), usedRegion), statementType));
        }
        List<FinancialStatementRow> result = new ArrayList<>();
        current.sort(Comparator.comparing(FinancialStatementAgregate::getSortOrder));
        for (FinancialStatementAgregate line : current) {
            Map<String, Double> currentYear = yearlyMaps.get(anchorYear);
            Map<String, Double> year1 = yearlyMaps.get(anchorYear - 1);
            Map<String, Double> year2 = yearlyMaps.get(anchorYear - 2);
            Map<String, Double> year3 = yearlyMaps.get(anchorYear - 3);
            Map<String, Double> year4 = yearlyMaps.get(anchorYear - 4);
            result.add(new FinancialStatementRow(
                    line.getLineCode(),
                    line.getRubrique(),
                    line.getNature(),
                    amountOf(currentYear, line.getLineCode()),
                    amountOf(year1, line.getLineCode()),
                    amountOf(year2, line.getLineCode()),
                    normalizedSpan == 5 ? amountOf(year3, line.getLineCode()) : 0d,
                    normalizedSpan == 5 ? amountOf(year4, line.getLineCode()) : 0d,
                    Boolean.TRUE.equals(line.getSectionHeader()),
                    Boolean.TRUE.equals(line.getTotalLine())));
        }
        return enrichBilanImmobilisationColumns(statementType, result, yearEnd(anchorYear), usedRegion);
    }

    public List<FinancialStatementRow> loadStatementRowsQuarterly(String statementType, LocalDate anchorDate, int span, String region) {
        int normalizedSpan = span <= 3 ? 3 : 4;
        String usedRegion = normalizeRegion(region);
        
        List<PeriodRange> periods = quarterlyPeriods(anchorDate, normalizedSpan);
        PeriodRange firstPeriod = periods.get(0);
        LocalDate qEnd = periods.get(periods.size() - 1).end();
        
        List<FinancialStatementAgregate> current = findRows(statementType, firstPeriod.start(), firstPeriod.end(), usedRegion);
        if (current.isEmpty()) {
            return Collections.emptyList();
        }
        
        Map<Integer, Map<String, Double>> quarterlyMaps = new LinkedHashMap<>();
        for (int i = 0; i < periods.size(); i++) {
            PeriodRange period = periods.get(i);
            quarterlyMaps.put(i, toValueMap(findRows(statementType, period.start(), period.end(), usedRegion), statementType));
        }
        
        List<FinancialStatementRow> result = new ArrayList<>();
        current.sort(Comparator.comparing(FinancialStatementAgregate::getSortOrder));
        for (FinancialStatementAgregate line : current) {
            Map<String, Double> currentQ = quarterlyMaps.get(0);
            Map<String, Double> q1 = quarterlyMaps.get(1);
            Map<String, Double> q2 = quarterlyMaps.get(2);
            Map<String, Double> q3 = quarterlyMaps.get(3);
            
            result.add(new FinancialStatementRow(
                    line.getLineCode(),
                    line.getRubrique(),
                    line.getNature(),
                    amountOf(currentQ, line.getLineCode()),
                    amountOf(q1, line.getLineCode()),
                    amountOf(q2, line.getLineCode()),
                    normalizedSpan == 4 ? amountOf(q3, line.getLineCode()) : 0d,
                    0d,
                    Boolean.TRUE.equals(line.getSectionHeader()),
                    Boolean.TRUE.equals(line.getTotalLine())));
        }
        return enrichBilanImmobilisationColumns(statementType, result, qEnd, usedRegion);
    }

    private List<PeriodRange> quarterlyPeriods(LocalDate anchorDate, int span) {
        int normalizedSpan = span <= 3 ? 3 : 4;
        List<PeriodRange> periods = new ArrayList<>();
        if (normalizedSpan == 4) {
            for (LocalDate start : quarterStartsOfYear(anchorDate.getYear())) {
                periods.add(new PeriodRange(start, quarterEnd(start)));
            }
            return periods;
        }
        LocalDate current = anchorDate;
        for (int i = 0; i < normalizedSpan; i++) {
            LocalDate start = quarterStart(current);
            periods.add(new PeriodRange(start, quarterEnd(start)));
            current = start.minusDays(1);
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

    private List<FinancialStatementRow> enrichBilanImmobilisationColumns(String statementType,
            List<FinancialStatementRow> rows, LocalDate end, String region) {
        if (!STATEMENT_BILAN.equals(statementType) || rows.isEmpty()) {
            return rows;
        }
        Map<String, ImmobilisationAmounts> amountsByLine = immobilisationAmountsByBilanLine(end, region);
        if (amountsByLine.isEmpty()) {
            return rows;
        }
        List<FinancialStatementRow> enriched = new ArrayList<>(rows.size());
        for (FinancialStatementRow row : rows) {
            ImmobilisationAmounts amounts = amountsByLine.get(row.getCode());
            if (amounts == null) {
                enriched.add(row);
                continue;
            }
            enriched.add(new FinancialStatementRow(
                    row.getCode(),
                    row.getRubrique(),
                    row.getNature(),
                    row.getAmountN(),
                    row.getAmountN1(),
                    row.getAmountN2(),
                    row.getAmountN3(),
                    row.getAmountN4(),
                    scale(amounts.gross()),
                    scale(amounts.amortization()),
                    scale(amounts.net()),
                    row.isSectionHeader(),
                    row.isTotalLine()));
        }
        return enriched;
    }

    private Double getPriorAmount(List<Map<String, Double>> previousMaps, String lineCode, int index) {
        if (index >= previousMaps.size()) {
            return 0d;
        }
        return amountOf(previousMaps.get(index), lineCode);
    }

    private double amountOf(Map<String, Double> values, String lineCode) {
        return values == null ? 0d : zero(values.get(lineCode));
    }

    private double zero(Double value) {
        return value == null ? 0d : value;
    }

    private Map<String, Double> toValueMap(List<FinancialStatementAgregate> rows, String statementType) {
        Map<String, Double> values = new HashMap<>();
        for (FinancialStatementAgregate row : rows) {
            if (!statementType.equals(row.getStatementType())) {
                continue;
            }
            values.put(row.getLineCode(), row.getAmountUsd());
        }
        return values;
    }

    private List<FinancialStatementAgregate> buildBilanRows(LocalDate start, LocalDate end, String region,
            CoreMetrics metrics, Map<String, Double> resultMap) {
        List<FinancialStatementAgregate> rows = new ArrayList<>();
        double resultNet = safe(resultMap.get("XJ"));
        double previousCapital = safe(previousSnapshotValue(STATEMENT_BILAN, "10", start, region));
        double previousReserves = safe(previousSnapshotValue(STATEMENT_BILAN, "11", start, region));
        double previousReport = safe(previousSnapshotValue(STATEMENT_BILAN, "12", start, region));
        double previousResult = safe(previousSnapshotValue(STATEMENT_BILAN, "13", start, region));

        double capital = previousCapital > 0d ? previousCapital + metrics.capitalIncreaseInPeriod : metrics.capitalInflowsToDate;
        double reserves = previousReserves > 0d ? previousReserves : Math.max(0d, metrics.retainedResultsToDate - metrics.dividendsToDate);
        double reportANouveau = Math.max(0d, previousReport + previousResult - metrics.dividendsPaidInPeriod);

        double actifImmobilise = metrics.immobIncorp + metrics.immobCorp + metrics.immobFin;
        double actifCirculant = metrics.stockClose + metrics.clientDebt + metrics.otherReceivables;
        double tresorerieActif = Math.max(0d, metrics.totalTreasuryClose);
        double totalActif = actifImmobilise + actifCirculant + tresorerieActif;

        double capitauxPropres = capital + reserves + reportANouveau + resultNet;
        double dettesFinancieres = Math.max(0d, metrics.longTermDebtClose);
        double passifCirculantBase = metrics.supplierDebt + metrics.taxAndSocialDebt;
        double tresoreriePassif = Math.max(0d, -metrics.totalTreasuryClose) + Math.max(0d, metrics.shortTermBankFunding);
        double autresDettes = Math.max(0d, totalActif - (capitauxPropres + dettesFinancieres + passifCirculantBase + tresoreriePassif));
        double passifCirculant = passifCirculantBase + autresDettes;

        addRow(rows, STATEMENT_BILAN, 1, "ACT", "ACTIF IMMOBILISÉ", "Moyens durables de production",
                actifImmobilise, start, end, region, true, false);
        addRow(rows, STATEMENT_BILAN, 2, "3", "Immobilisations incorporelles", "Actifs immatériels",
                metrics.immobIncorp, start, end, region, false, false);
        addRow(rows, STATEMENT_BILAN, 3, "4", "Immobilisations corporelles",
                "Matériels, équipements, constructions", metrics.immobCorp, start, end, region, false, false);
        addRow(rows, STATEMENT_BILAN, 4, "-", "dont Terrains et Bâtiments",
                "Sûretés réelles / Hypothèques bancaires", metrics.immobLandBuildings, start, end, region, false,
                false);
        addRow(rows, STATEMENT_BILAN, 5, "5", "Immobilisations financières",
                "Titres de participation & Prêts accordés", metrics.immobFin, start, end, region, false, false);

        addRow(rows, STATEMENT_BILAN, 6, "ACT_C", "ACTIF CIRCULANT", "Cycle d'exploitation court terme",
                actifCirculant, start, end, region, true, false);
        addRow(rows, STATEMENT_BILAN, 7, "6", "Stocks et encours",
                "Marchandises, matières premières, encours", metrics.stockClose, start, end, region, false, false);
        addRow(rows, STATEMENT_BILAN, 8, "-", "dont Marchandises / Matières premières",
                "Indicateur de rotation des stocks", metrics.stockClose, start, end, region, false, false);
        addRow(rows, STATEMENT_BILAN, 9, "7", "Créances clients et comptes rattachés",
                "Délai de paiement accordé (DPC)", metrics.clientDebt, start, end, region, false, false);
        addRow(rows, STATEMENT_BILAN, 10, "-", "dont Créances d'exploitation / douteuses",
                "Risque d'impayés et qualité du portefeuille", metrics.doubtfulReceivables, start, end, region, false,
                false);
        addRow(rows, STATEMENT_BILAN, 11, "8", "Autres créances circulantes",
                "Créances fiscales, sociales ou diverses", metrics.otherReceivables, start, end, region, false,
                false);
        addRow(rows, STATEMENT_BILAN, 12, "TA", "TRÉSORERIE-ACTIF", "Liquidités immédiatement mobilisables",
                tresorerieActif, start, end, region, true, false);
        addRow(rows, STATEMENT_BILAN, 13, "9", "Banques, chèques postaux et caisse",
                "Disponibilités au jour le jour", tresorerieActif, start, end, region, false, false);
        addRow(rows, STATEMENT_BILAN, 14, "TOT_A", "TOTAL GÉNÉRAL DE L'ACTIF", "", totalActif, start, end, region,
                false, true);

        addRow(rows, STATEMENT_BILAN, 15, "PAS", "CAPITAUX PROPRES",
                "Fonds propres et couverture de sécurité", capitauxPropres, start, end, region, true, false);
        addRow(rows, STATEMENT_BILAN, 16, "10", "Capital social ou individuel",
                "Ressources initiales stables des associés", capital, start, end, region, false, false);
        addRow(rows, STATEMENT_BILAN, 17, "11", "Réserves (Légales, statutaires)",
                "Bénéfices capitalisés non distribués", reserves, start, end, region, false, false);
        addRow(rows, STATEMENT_BILAN, 18, "12", "Report à nouveau (Solde débiteur ou créditeur)",
                "Résultats des exercices antérieurs non affectés", reportANouveau, start, end, region, false, false);
        addRow(rows, STATEMENT_BILAN, 19, "13", "Résultat net de l'exercice (Bénéfice ou Perte)",
                "Performance nette distribuable ou réinvestie", resultNet, start, end, region, false, false);
        addRow(rows, STATEMENT_BILAN, 20, "PAS_D", "DETTES FINANCIÈRES",
                "Ressources stables empruntées à long/moyen terme", dettesFinancieres, start, end, region, true,
                false);
        addRow(rows, STATEMENT_BILAN, 21, "14", "Emprunts et dettes financières assimilées",
                "Endettement structurel de l'entreprise", dettesFinancieres, start, end, region, false, false);
        addRow(rows, STATEMENT_BILAN, 22, "-", "dont Établissements de crédit (M/L terme)",
                "Dettes d'investissement auprès des banques", dettesFinancieres, start, end, region, false, false);
        addRow(rows, STATEMENT_BILAN, 23, "PAS_C", "PASSIF CIRCULANT", "Dettes à court terme liées au cycle",
                passifCirculant, start, end, region, true, false);
        addRow(rows, STATEMENT_BILAN, 24, "15", "Dettes fournisseurs et comptes rattachés",
                "Crédit inter-entreprises d'exploitation", metrics.supplierDebt, start, end, region, false, false);
        addRow(rows, STATEMENT_BILAN, 25, "16", "Dettes fiscales et sociales",
                "Dettes État, Personnel, Organismes sociaux", metrics.taxAndSocialDebt, start, end, region, false,
                false);
        addRow(rows, STATEMENT_BILAN, 26, "-", "dont État, Organismes sociaux (Prioritaires)",
                "Risques de privilèges légaux ou blocages", metrics.taxAndSocialDebt, start, end, region, false,
                false);
        addRow(rows, STATEMENT_BILAN, 27, "17", "Autres dettes circulantes", "Dettes diverses à court terme",
                autresDettes, start, end, region, false, false);
        addRow(rows, STATEMENT_BILAN, 28, "TP", "TRÉSORERIE-PASSIF",
                "Financements bancaires de très court terme", tresoreriePassif, start, end, region, true, false);
        addRow(rows, STATEMENT_BILAN, 29, "18", "Banques, crédits d'escompte et de trésorerie",
                "Concours bancaires courants et découverts", tresoreriePassif, start, end, region, false, false);
        addRow(rows, STATEMENT_BILAN, 30, "-", "dont Découverts bancaires / facilités de caisse",
                "Dépendance financière à court terme", tresoreriePassif, start, end, region, false, false);
        addRow(rows, STATEMENT_BILAN, 31, "TOT_P", "TOTAL GÉNÉRAL DU PASSIF", "", totalActif, start, end, region,
                false, true);
        return rows;
    }

    private List<FinancialStatementAgregate> buildCompteResultatRows(LocalDate start, LocalDate end, String region,
            CoreMetrics metrics) {
        List<FinancialStatementAgregate> rows = new ArrayList<>();

        double margeCommerciale = metrics.sales - metrics.costOfSales - metrics.stockVariation;
        double productionExercice = metrics.manufacturedSales + metrics.servicesSales + metrics.accessoryIncome
                - metrics.finishedGoodsVariation + metrics.selfConstructedProduction;
        double valeurAjoutee = margeCommerciale + productionExercice - metrics.rawMaterialPurchases
                - metrics.rawMaterialVariation - metrics.otherPurchases - metrics.otherPurchasesVariation
                - metrics.transportCharges - metrics.externalServices;
        double ebe = valeurAjoutee + metrics.operatingSubsidies - metrics.taxesAndDuties - metrics.personnelCharges;
        double resultatExploitation = ebe + metrics.otherOperatingIncome - metrics.otherOperatingCharges
                - metrics.amortizationExpense + metrics.reversalProvisionIncome;
        double resultatFinancier = metrics.financialIncome - metrics.financialCharges;
        double resultatOrdinaire = resultatExploitation + resultatFinancier;
        double resultatHAO = metrics.haoIncome - metrics.haoCharges;
        double taxableResult = resultatOrdinaire + resultatHAO - metrics.workerParticipation;
        double impotResultat = metrics.incomeTaxCharge > 0 ? metrics.incomeTaxCharge : Math.max(0d, taxableResult * 0.3d);
        double resultatNet = taxableResult - impotResultat;

        addRow(rows, STATEMENT_COMPTE_RESULTAT, 1, "TA", "Ventes de marchandises (+)",
                "Chiffre d'affaires négoce", metrics.sales, start, end, region, false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 2, "RA", "Achats de marchandises (-)",
                "Coût d'achat brut des marchandises", metrics.costOfSales, start, end, region, false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 3, "RB", "Variation de stocks de marchandises (-/+)",
                "Stock Initial - Stock Final", metrics.stockVariation, start, end, region, false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 4, "XA", "MARGE COMMERCIALE", "", margeCommerciale, start, end,
                region, false, true);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 5, "TB", "Ventes de produits fabriqués (+)",
                "Production vendue de biens", metrics.manufacturedSales, start, end, region, false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 6, "TC", "Travaux et services produits (+)",
                "Production vendue de services", metrics.servicesSales, start, end, region, false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 7, "TD", "Produits accessoires (+)",
                "Locations, redevances, etc.", metrics.accessoryIncome, start, end, region, false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 8, "RC", "Variation de stocks de produits (-/+)",
                "Stock Initial - Stock Final", metrics.finishedGoodsVariation, start, end, region, false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 9, "TE", "Production immobilisée (+)",
                "Travaux faits par l'entité pour elle-même", metrics.selfConstructedProduction, start, end, region,
                false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 10, "XB", "PRODUCTION DE L'EXERCICE", "", productionExercice, start,
                end, region, false, true);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 11, "RD",
                "Achats de matières premières et fournitures (-)", "Approvisionnements usine",
                metrics.rawMaterialPurchases, start, end, region, false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 12, "RE",
                "Variation de stocks (Matières et fournitures) (-/+)", "Stock Initial - Stock Final",
                metrics.rawMaterialVariation, start, end, region, false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 13, "RF", "Autres achats (-)",
                "Eau, électricité, fournitures bureau", metrics.otherPurchases, start, end, region, false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 14, "RG", "Variation de stocks des autres achats (-/+)",
                "Stock Initial - Stock Final", metrics.otherPurchasesVariation, start, end, region, false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 15, "RH", "Transports (-)",
                "Marchandises, personnel, etc.", metrics.transportCharges, start, end, region, false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 16, "RI", "Services extérieurs (-)",
                "Loyers, assurances, honoraires", metrics.externalServices, start, end, region, false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 17, "XC", "VALEUR AJOUTÉE", "", valeurAjoutee, start, end, region,
                false, true);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 18, "TF", "Subventions d'exploitation (+)",
                "Aides publiques d'exploitation", metrics.operatingSubsidies, start, end, region, false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 19, "RJ", "Impôts et taxes (-)",
                "Impôts d'exploitation hors IS", metrics.taxesAndDuties, start, end, region, false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 20, "RK", "Charges de personnel (-)",
                "Salaires bruts + charges sociales", metrics.personnelCharges, start, end, region, false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 21, "XD", "EXCÉDENT BRUT D'EXPLOITATION (EBE)", "", ebe, start, end,
                region, false, true);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 22, "TG", "Autres produits d'exploitation (+)",
                "Plus-values courantes, etc.", metrics.otherOperatingIncome, start, end, region, false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 23, "RL", "Autres charges d'exploitation (-)",
                "Moins-values courantes, créances irrécouvrables", metrics.otherOperatingCharges, start, end, region,
                false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 24, "RM", "Dotations aux amortissements et aux provisions (-)",
                "Pertes de valeur de l'actif immobilisé", metrics.amortizationExpense, start, end, region, false,
                false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 25, "TH", "Reprises d'amortissements et de provisions (+)",
                "Annulations de dépréciations antérieures", metrics.reversalProvisionIncome, start, end, region,
                false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 26, "XE", "RÉSULTAT D'EXPLOITATION", "", resultatExploitation, start,
                end, region, false, true);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 27, "TI", "Produits financiers (+)",
                "Dividendes, intérêts, gains de change", metrics.financialIncome, start, end, region, false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 28, "RN", "Charges financières (-)",
                "Intérêts des emprunts, agios, pertes change", metrics.financialCharges, start, end, region, false,
                false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 29, "XF", "RÉSULTAT FINANCIER", "", resultatFinancier, start, end,
                region, false, true);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 30, "XG", "RÉSULTAT DES ACTIVITÉS ORDINAIRES", "",
                resultatOrdinaire, start, end, region, false, true);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 31, "TK", "Produits Hors Activités Ordinaires (HAO) (+)",
                "Prix de cession des actifs vendus", metrics.haoIncome, start, end, region, false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 32, "RO", "Charges Hors Activités Ordinaires (HAO) (-)",
                "Valeur comptable des actifs cédés, amendes", metrics.haoCharges, start, end, region, false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 33, "XH", "RÉSULTAT HORS ACTIVITÉS ORDINAIRES", "", resultatHAO,
                start, end, region, false, true);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 34, "RP", "Participation des travailleurs (-)",
                "Quote-part des bénéfices légaux", metrics.workerParticipation, start, end, region, false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 35, "RQ", "Impôts sur le résultat (-)",
                "Impôt sur les Sociétés (IS)", impotResultat, start, end, region, false, false);
        addRow(rows, STATEMENT_COMPTE_RESULTAT, 36, "XJ", "RÉSULTAT NET DE L'EXERCICE", "", resultatNet, start, end,
                region, false, true);
        return rows;
    }

    private List<FinancialStatementAgregate> buildCashFlowRows(LocalDate start, LocalDate end, String region,
            CoreMetrics metrics, Map<String, Double> bilanMap, Map<String, Double> resultMap,
            Map<String, Double> previousBilan, Map<String, Double> previousCash) {
        List<FinancialStatementAgregate> rows = new ArrayList<>();

        double zA = safe(resultMap.get("XJ")) + safe(resultMap.get("RM")) - safe(resultMap.get("TH"));
        double zB = safe(previousBilan.get("6")) - safe(bilanMap.get("6"));
        double zC = safe(previousBilan.get("7")) - safe(bilanMap.get("7"));
        double zD = safe(bilanMap.get("15")) - safe(previousBilan.get("15"));
        double zE = (safe(previousBilan.get("8")) - safe(bilanMap.get("8")))
                + ((safe(bilanMap.get("16")) + safe(bilanMap.get("17")))
                        - (safe(previousBilan.get("16")) + safe(previousBilan.get("17"))));
        double zF = zB + zC + zD + zE;
        double flowA = zA + zF;

        double flowB = -metrics.investmentAcquisitions + metrics.assetDisposals + metrics.financialAssetReductions;
        double flowC = metrics.capitalIncreaseInPeriod + metrics.newBorrowingsInPeriod - metrics.borrowingRepaymentsInPeriod
                - metrics.dividendsPaidInPeriod;
        double netVariation = flowA + flowB + flowC;
        double openingTreasury = previousCash.containsKey("ZP") ? safe(previousCash.get("ZP"))
                : metrics.totalTreasuryOpen;
        double fxGap = metrics.totalTreasuryClose - (openingTreasury + netVariation);
        double closingTreasury = openingTreasury + netVariation + fxGap;

        addRow(rows, STATEMENT_FLUX_TRESORERIE, 1, "I",
                "FLUX DE TRÉSORERIE LIÉS AUX ACTIVITÉS D'EXPLOITATION", "Flux d'exploitation", null, start, end,
                region, true, false);
        addRow(rows, STATEMENT_FLUX_TRESORERIE, 2, "ZA", "Capacité d'Autofinancement Globale (CAFG)",
                "EBE + Produits encaissables - Charges décaissables", zA, start, end, region, false, false);
        addRow(rows, STATEMENT_FLUX_TRESORERIE, 3, "ZB", "(-) Variation des stocks d'exploitation",
                "Stock Initial (N) - Stock Final (N)", zB, start, end, region, false, false);
        addRow(rows, STATEMENT_FLUX_TRESORERIE, 4, "ZC", "(-) Variation des créances d'exploitation",
                "Créances Initiales - Créances Finales", zC, start, end, region, false, false);
        addRow(rows, STATEMENT_FLUX_TRESORERIE, 5, "ZD", "(+) Variation des dettes fournisseurs d'exploitation",
                "Dettes Finales - Dettes Initiales", zD, start, end, region, false, false);
        addRow(rows, STATEMENT_FLUX_TRESORERIE, 6, "ZE", "(-/+) Variation des autres créances et dettes d'expl.",
                "Ajustements nets du passif/actif circulant d'expl.", zE, start, end, region, false, false);
        addRow(rows, STATEMENT_FLUX_TRESORERIE, 7, "ZF",
                "Variation du Besoin en Fonds de Roulement d'Expl. (BFRE)", "", zF, start, end, region, false,
                true);
        addRow(rows, STATEMENT_FLUX_TRESORERIE, 8, "A",
                "FLUX DE TRÉSORERIE NET DES ACTIVITÉS D'EXPLOITATION", "", flowA, start, end, region, false, true);

        addRow(rows, STATEMENT_FLUX_TRESORERIE, 9, "II",
                "FLUX DE TRÉSORERIE LIÉS AUX ACTIVITÉS D'INVESTISSEMENT", "Flux d'investissement", null, start, end,
                region, true, false);
        addRow(rows, STATEMENT_FLUX_TRESORERIE, 10, "ZG",
                "(-) Décaissements liés aux acquisitions d'immobilisations",
                "Flux de sortie pour investissements physiques/financiers", metrics.investmentAcquisitions, start, end,
                region, false, false);
        addRow(rows, STATEMENT_FLUX_TRESORERIE, 11, "ZH",
                "(+) Encaissements liés aux cessions d'immobilisations",
                "Prix de vente perçu suite à cession d'actifs", metrics.assetDisposals, start, end, region, false,
                false);
        addRow(rows, STATEMENT_FLUX_TRESORERIE, 12, "ZI", "(+) Réduction des immobilisations financières",
                "Désinvestissements financiers (prêts/titres)", metrics.financialAssetReductions, start, end, region,
                false, false);
        addRow(rows, STATEMENT_FLUX_TRESORERIE, 13, "B",
                "FLUX DE TRÉSORERIE NET DES ACTIVITÉS D'INVESTISSEMENT", "", flowB, start, end, region, false, true);

        addRow(rows, STATEMENT_FLUX_TRESORERIE, 14, "III",
                "FLUX DE TRÉSORERIE LIÉS AUX ACTIVITÉS DE FINANCEMENT", "Flux de financement", null, start, end,
                region, true, false);
        addRow(rows, STATEMENT_FLUX_TRESORERIE, 15, "ZJ", "(+) Augmentation de capital par apports nouveaux",
                "Apports en numéraire des actionnaires", metrics.capitalIncreaseInPeriod, start, end, region, false,
                false);
        addRow(rows, STATEMENT_FLUX_TRESORERIE, 16, "ZK", "(+) Encaissements liés aux nouveaux emprunts",
                "Fonds de crédit à long/moyen terme débloqués", metrics.newBorrowingsInPeriod, start, end, region,
                false, false);
        addRow(rows, STATEMENT_FLUX_TRESORERIE, 17, "ZL", "(-) Remboursements d'emprunts et dettes assimilées",
                "Amortissements du principal des crédits", metrics.borrowingRepaymentsInPeriod, start, end, region,
                false, false);
        addRow(rows, STATEMENT_FLUX_TRESORERIE, 18, "ZM", "(-) Dividendes versés aux actionnaires",
                "Distributions effectives de dividendes payées", metrics.dividendsPaidInPeriod, start, end, region,
                false, false);
        addRow(rows, STATEMENT_FLUX_TRESORERIE, 19, "C",
                "FLUX DE TRÉSORERIE NET DES ACTIVITÉS DE FINANCEMENT", "", flowC, start, end, region, false, true);

        addRow(rows, STATEMENT_FLUX_TRESORERIE, 20, "IV", "CONCILIATION ET VARIATION DE LA TRÉSORERIE",
                "Synthèse générale", null, start, end, region, true, false);
        addRow(rows, STATEMENT_FLUX_TRESORERIE, 21, "Z_D", "VARIATION NETTE DE LA TRÉSORERIE DE L'EXERCICE", "",
                netVariation, start, end, region, false, true);
        addRow(rows, STATEMENT_FLUX_TRESORERIE, 22, "ZN", "(+) Trésorerie d'ouverture (au 1er Janvier)",
                "Trésorerie Actif Initial - Trésorerie Passif Initial", openingTreasury, start, end, region, false,
                false);
        addRow(rows, STATEMENT_FLUX_TRESORERIE, 23, "ZO", "(+/-) Écarts de conversion sur trésorerie",
                "Impact des variations de taux de devises", fxGap, start, end, region, false, false);
        addRow(rows, STATEMENT_FLUX_TRESORERIE, 24, "ZP", "TRÉSORERIE DE CLÔTURE (au 31 Décembre)", "",
                closingTreasury, start, end, region, false, true);
        return rows;
    }

    private CoreMetrics buildMetrics(LocalDate start, LocalDate end, String region, Map<String, Double> previousBilan,
            Map<String, Double> previousCash) {
        CoreMetrics metrics = new CoreMetrics();
        metrics.sales = scale(RepportDelegate.chiffreDaffaire(start, end, region));
        metrics.costOfSales = scale(RepportDelegate.chargeVariable(start, end, region));
        metrics.stockClose = scale(sumLatestStockValue(end, region));
        metrics.stockOpen = previousBilan.containsKey("6") ? safe(previousBilan.get("6"))
                : scale(sumLatestStockValue(start.minusDays(1), region));
        metrics.stockVariation = scale(metrics.stockOpen - metrics.stockClose);

        metrics.clientDebt = scale(sumClientDebtAt(end, region));
        metrics.supplierDebt = scale(sumSupplierDebtAt(end, region));
        metrics.doubtfulReceivables = scale(metrics.clientDebt * 0.1d);
        metrics.otherReceivables = 0d;

        metrics.totalTreasuryClose = scale(sumTreasuryBalanceUntil(end, region));
        metrics.totalTreasuryOpen = previousCash.containsKey("ZP") ? safe(previousCash.get("ZP"))
                : scale(sumTreasuryBalanceUntil(start.minusDays(1), region));
        metrics.shortTermBankFunding = Math.max(0d, scale(-sumBankBalanceUntil(end, region)));
        metrics.longTermDebtClose = scale(sumBorrowingFlowsUntil(end, region));
        metrics.capitalInflowsToDate = scale(sumTreasuryByKeywordsUntil(end, region, true, KEYWORDS_CAPITAL));
        metrics.retainedResultsToDate = scale(sumPastNetResults(start, region));
        metrics.dividendsToDate = scale(sumTreasuryByKeywordsUntil(end, region, false, KEYWORDS_DIVIDENDS));
        metrics.dividendsPaidInPeriod = scale(sumTreasuryByKeywords(start, end, region, false, KEYWORDS_DIVIDENDS));

        classifyExpenses(start, end, region, metrics);
        classifyTreasuryFlows(start, end, region, metrics);
        classifyImmobilisations(end, region, metrics);
        metrics.amortizationExpense = scale(sumAmortizationExpense(start, end, region));
        metrics.taxAndSocialDebt = scale(metrics.taxesAndDuties + Math.max(0d, metrics.personnelCharges * 0.15d));
        metrics.incomeTaxCharge = scale(sumTreasuryByKeywords(start, end, region, false, KEYWORDS_INCOME_TAX));
        return metrics;
    }

    private void classifyImmobilisations(LocalDate end, String region, CoreMetrics metrics) {
        Map<String, ImmobilisationSnapshot> snapshots = latestImmobilisationSnapshots(end, region);
        if (snapshots.isEmpty()) {
            for (Immobilisation immobilisation : findImmobilisations(region)) {
                double net = immobilisation.valeurNetteUsd(end);
                String combined = normalize(immobilisation.getCategorie()) + " " + normalize(immobilisation.getLibelle());
                if (containsAny(combined, KEYWORDS_IMMO_INCORP)) {
                    metrics.immobIncorp += net;
                } else if (containsAny(combined, KEYWORDS_IMMO_FIN)) {
                    metrics.immobFin += net;
                } else {
                    metrics.immobCorp += net;
                    if (containsAny(combined, KEYWORDS_LAND_BUILDINGS)) {
                        metrics.immobLandBuildings += net;
                    }
                }
            }
            metrics.immobIncorp = scale(metrics.immobIncorp);
            metrics.immobCorp = scale(metrics.immobCorp);
            metrics.immobFin = scale(metrics.immobFin);
            metrics.immobLandBuildings = scale(metrics.immobLandBuildings);
            return;
        }
        for (ImmobilisationSnapshot snapshot : snapshots.values()) {
            String combined = normalize(snapshot.categorie()) + " " + normalize(snapshot.libelle());
            if (containsAny(combined, KEYWORDS_IMMO_INCORP)) {
                metrics.immobIncorp += snapshot.netUsd();
            } else if (containsAny(combined, KEYWORDS_IMMO_FIN)) {
                metrics.immobFin += snapshot.netUsd();
            } else {
                metrics.immobCorp += snapshot.netUsd();
                if (containsAny(combined, KEYWORDS_LAND_BUILDINGS)) {
                    metrics.immobLandBuildings += snapshot.netUsd();
                }
            }
        }
        metrics.immobIncorp = scale(metrics.immobIncorp);
        metrics.immobCorp = scale(metrics.immobCorp);
        metrics.immobFin = scale(metrics.immobFin);
        metrics.immobLandBuildings = scale(metrics.immobLandBuildings);
    }

    private Map<String, ImmobilisationAmounts> immobilisationAmountsByBilanLine(LocalDate end, String region) {
        Map<String, ImmobilisationAmounts> byLine = new LinkedHashMap<>();
        Map<String, ImmobilisationSnapshot> snapshots = latestImmobilisationSnapshots(end, region);
        if (snapshots.isEmpty()) {
            for (Immobilisation immobilisation : findImmobilisations(region)) {
                double gross = safe(immobilisation.getValeurOrigineUsd());
                double amortization = immobilisation.amortissementCumulUsd(end);
                double net = immobilisation.valeurNetteUsd(end);
                addImmobilisationAmount(byLine, immobilisation.getCategorie(), immobilisation.getLibelle(),
                        gross, amortization, net);
            }
        } else {
            for (ImmobilisationSnapshot snapshot : snapshots.values()) {
                addImmobilisationAmount(byLine, snapshot.categorie(), snapshot.libelle(),
                        snapshot.grossUsd(), snapshot.amortizationUsd(), snapshot.netUsd());
            }
        }
        ImmobilisationAmounts total = byLine.values().stream()
                .reduce(new ImmobilisationAmounts(0d, 0d, 0d), ImmobilisationAmounts::plus);
        if (total.hasValue()) {
            byLine.put("ACT", total);
        }
        return byLine;
    }

    private void addImmobilisationAmount(Map<String, ImmobilisationAmounts> byLine, String categorie, String libelle,
            double gross, double amortization, double net) {
        String combined = normalize(categorie) + " " + normalize(libelle);
        mergeImmobilisationAmount(byLine, containsAny(combined, KEYWORDS_IMMO_INCORP) ? "3"
                : containsAny(combined, KEYWORDS_IMMO_FIN) ? "5" : "4", gross, amortization, net);
    }

    private void mergeImmobilisationAmount(Map<String, ImmobilisationAmounts> byLine, String lineCode,
            double gross, double amortization, double net) {
        byLine.merge(lineCode, new ImmobilisationAmounts(gross, amortization, net), ImmobilisationAmounts::plus);
    }

    private void classifyExpenses(LocalDate start, LocalDate end, String region, CoreMetrics metrics) {
        for (DepenseAgregate depense : findDepenseAgregates(start, end, region)) {
            double amount = scale(depense.getMontantUsd());
            String label = normalize(depense.getImputation()) + " "
                    + normalize(depense.getDepenseId() == null ? null : depense.getDepenseId().getNomDepense());
            if (containsAny(label, KEYWORDS_TAXES)) {
                metrics.taxesAndDuties += amount;
            } else if (containsAny(label, KEYWORDS_PERSONNEL)) {
                metrics.personnelCharges += amount;
            } else if (containsAny(label, KEYWORDS_TRANSPORT)) {
                metrics.transportCharges += amount;
            } else if (containsAny(label, KEYWORDS_SERVICES)) {
                metrics.externalServices += amount;
            } else if (Constants.DEPT_PRODUCTION.equalsIgnoreCase(depense.getImputation())
                    || Constants.DEPT_APPROVISIONEMENT.equalsIgnoreCase(depense.getImputation())
                    || containsAny(label, KEYWORDS_RAW_MATERIALS)) {
                metrics.rawMaterialPurchases += amount;
            } else {
                metrics.otherOperatingCharges += amount;
                metrics.otherPurchases += amount;
            }
        }
        metrics.rawMaterialPurchases = scale(metrics.rawMaterialPurchases);
        metrics.otherPurchases = scale(metrics.otherPurchases);
        metrics.transportCharges = scale(metrics.transportCharges);
        metrics.externalServices = scale(metrics.externalServices);
        metrics.taxesAndDuties = scale(metrics.taxesAndDuties);
        metrics.personnelCharges = scale(metrics.personnelCharges);
        metrics.otherOperatingCharges = Math.max(0d, scale(metrics.otherOperatingCharges - metrics.otherPurchases));
    }

    private void classifyTreasuryFlows(LocalDate start, LocalDate end, String region, CoreMetrics metrics) {
        metrics.investmentAcquisitions = scale(sumTreasuryByKeywords(start, end, region, false, KEYWORDS_ACQUISITIONS));
        metrics.assetDisposals = scale(sumTreasuryByKeywords(start, end, region, true, KEYWORDS_ASSET_DISPOSALS));
        metrics.financialAssetReductions = scale(sumTreasuryByKeywords(start, end, region, true, KEYWORDS_FINANCIAL_ASSET_REDUCTIONS));
        metrics.capitalIncreaseInPeriod = scale(sumTreasuryByKeywords(start, end, region, true, KEYWORDS_CAPITAL));
        metrics.newBorrowingsInPeriod = scale(sumTreasuryByKeywords(start, end, region, true, KEYWORDS_BORROWINGS));
        metrics.borrowingRepaymentsInPeriod = scale(sumTreasuryByKeywords(start, end, region, false, KEYWORDS_BORROWINGS));
        metrics.operatingSubsidies = scale(sumTreasuryByKeywords(start, end, region, true, KEYWORDS_SUBSIDIES));
        metrics.otherOperatingIncome = scale(sumTreasuryByKeywords(start, end, region, true, KEYWORDS_OTHER_OPERATING_INCOME));
        metrics.reversalProvisionIncome = scale(sumTreasuryByKeywords(start, end, region, true, KEYWORDS_REVERSALS));
        metrics.financialIncome = scale(sumTreasuryByKeywords(start, end, region, true, KEYWORDS_FINANCIAL_INCOME));
        metrics.financialCharges = scale(sumTreasuryByKeywords(start, end, region, false, KEYWORDS_FINANCIAL_CHARGES));
        metrics.haoIncome = scale(sumTreasuryByKeywords(start, end, region, true, KEYWORDS_HAO_INCOME));
        metrics.haoCharges = scale(sumTreasuryByKeywords(start, end, region, false, KEYWORDS_HAO_CHARGES));
        metrics.workerParticipation = scale(sumTreasuryByKeywords(start, end, region, false, KEYWORDS_PARTICIPATION));
    }

    private List<FinancialStatementAgregate> findRows(String statementType, LocalDate start, LocalDate end, String region) {
        String jpql = """
                SELECT f FROM FinancialStatementAgregate f
                WHERE f.statementType = :statementType
                  AND f.periodStart = :periodStart
                  AND f.periodEnd = :periodEnd
                  AND f.region = :region
                ORDER BY f.sortOrder ASC
                """;
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, FinancialStatementAgregate.class)
                    .setParameter("statementType", statementType)
                    .setParameter("periodStart", start)
                    .setParameter("periodEnd", end)
                    .setParameter("region", normalizeRegion(region))
                    .getResultList());
        }
        return ManagedSessionFactory.getEntityManager().createQuery(jpql, FinancialStatementAgregate.class)
                .setParameter("statementType", statementType)
                .setParameter("periodStart", start)
                .setParameter("periodEnd", end)
                .setParameter("region", normalizeRegion(region))
                .getResultList();
    }

    private List<PeriodRange> findPreviousPeriods(String statementType, LocalDate beforeStart, String region, int limit) {
        String sql = """
                SELECT DISTINCT period_start, period_end
                FROM financial_statement_agregate
                WHERE statement_type = ?
                  AND region = ?
                  AND period_end < ?
                ORDER BY period_end DESC
                """;
        List<Object[]> rows;
        if (ManagedSessionFactory.isEmbedded()) {
            rows = ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sql);
                query.setParameter(1, statementType);
                query.setParameter(2, normalizeRegion(region));
                query.setParameter(3, beforeStart);
                query.setMaxResults(limit);
                return query.getResultList();
            });
        } else {
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sql);
            query.setParameter(1, statementType);
            query.setParameter(2, normalizeRegion(region));
            query.setParameter(3, beforeStart);
            query.setMaxResults(limit);
            rows = query.getResultList();
        }
        List<PeriodRange> periods = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (Object[] row : rows) {
            LocalDate start = toLocalDate(row[0]);
            LocalDate end = toLocalDate(row[1]);
            if (start == null || end == null) {
                continue;
            }
            String key = start + "|" + end;
            if (seen.add(key)) {
                periods.add(new PeriodRange(start, end));
            }
        }
        return periods;
    }

    private void replaceSnapshot(LocalDate start, LocalDate end, String region, List<FinancialStatementAgregate> rows) {
        String deleteSql = """
                DELETE FROM FinancialStatementAgregate f
                WHERE f.periodStart = :periodStart
                  AND f.periodEnd = :periodEnd
                  AND f.region = :region
                """;
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.createQuery(deleteSql)
                        .setParameter("periodStart", start)
                        .setParameter("periodEnd", end)
                        .setParameter("region", normalizeRegion(region))
                        .executeUpdate();
                for (FinancialStatementAgregate row : rows) {
                    em.persist(row);
                }
                return null;
            }).join();
            return;
        }
        EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        ManagedSessionFactory.getEntityManager().createQuery(deleteSql)
                .setParameter("periodStart", start)
                .setParameter("periodEnd", end)
                .setParameter("region", normalizeRegion(region))
                .executeUpdate();
        for (FinancialStatementAgregate row : rows) {
            ManagedSessionFactory.getEntityManager().persist(row);
        }
        tx.commit();
    }

    private void addRow(List<FinancialStatementAgregate> rows, String statementType, int order, String code, String rubrique,
            String nature, Double amount, LocalDate start, LocalDate end, String region, boolean sectionHeader,
            boolean totalLine) {
        FinancialStatementAgregate row = new FinancialStatementAgregate();
        row.setStatementType(statementType);
        row.setSortOrder(order);
        row.setLineCode(code);
        row.setRubrique(rubrique);
        row.setNature(nature);
        row.setAmountUsd(amount == null ? null : scale(amount));
        row.setPeriodStart(start);
        row.setPeriodEnd(end);
        row.setRegion(normalizeRegion(region));
        row.setSectionHeader(sectionHeader);
        row.setTotalLine(totalLine);
        rows.add(row);
    }

    private double sumLatestStockValue(LocalDate atDate, String region) {
        if (atDate == null) {
            return 0d;
        }
        List<StockAgregate> rows = RepportDelegate.findLatestStockAgregates(region, atDate);
        double total = 0d;
        for (StockAgregate row : rows) {
            total += safe(row.getCoutAchat()) * safe(row.getFinalQuantity());
        }
        return scale(total);
    }

    private double sumClientDebtAt(LocalDate atDate, String region) {
        String usedRegion = normalizeRegion(region);
        String jpqlCheck = "SELECT c FROM CreanceAgregate c WHERE c.date = :atDate AND c.region = :region";
        List<CreanceAgregate> list = ManagedSessionFactory.isEmbedded()
                ? ManagedSessionFactory.executeRead(em -> em.createQuery(jpqlCheck, CreanceAgregate.class)
                        .setParameter("atDate", atDate).setParameter("region", usedRegion).getResultList())
                : ManagedSessionFactory.getEntityManager().createQuery(jpqlCheck, CreanceAgregate.class)
                        .setParameter("atDate", atDate).setParameter("region", usedRegion).getResultList();
        if (!list.isEmpty()) {
            return safe(list.get(0).getMontantUsd());
        }

        String jpql = """
                SELECT SUM(COALESCE(v.montantDette,0))
                FROM Vente v
                WHERE v.region LIKE :region
                  AND v.dateVente <= :atDateTime
                  AND (v.observation IS NULL OR v.observation <> 'Annulée')
                """;
        Double result;
        java.time.LocalDateTime atDateTime = atDate.atTime(23, 59, 59);
        if (ManagedSessionFactory.isEmbedded()) {
            result = ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, Double.class)
                    .setParameter("region", usedRegion)
                    .setParameter("atDateTime", atDateTime)
                    .getSingleResult());
        } else {
            result = ManagedSessionFactory.getEntityManager().createQuery(jpql, Double.class)
                    .setParameter("region", usedRegion)
                    .setParameter("atDateTime", atDateTime)
                    .getSingleResult();
        }
        double amount = result == null ? 0d : scale(result);

        CreanceAgregate agg = new CreanceAgregate();
        agg.setDate(atDate);
        agg.setRegion(usedRegion);
        agg.setMontantUsd(amount);
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(agg);
                return null;
            }).join();
        } else {
            EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
            if (!tx.isActive()) tx.begin();
            ManagedSessionFactory.getEntityManager().persist(agg);
            tx.commit();
        }
        return amount;
    }

    private double sumSupplierDebtAt(LocalDate atDate, String region) {
        String usedRegion = normalizeRegion(region);
        String jpqlCheck = "SELECT d FROM DetteFournisseurAgregate d WHERE d.date = :atDate AND d.region = :region";
        List<DetteFournisseurAgregate> list = ManagedSessionFactory.isEmbedded()
                ? ManagedSessionFactory.executeRead(em -> em.createQuery(jpqlCheck, DetteFournisseurAgregate.class)
                        .setParameter("atDate", atDate).setParameter("region", usedRegion).getResultList())
                : ManagedSessionFactory.getEntityManager().createQuery(jpqlCheck, DetteFournisseurAgregate.class)
                        .setParameter("atDate", atDate).setParameter("region", usedRegion).getResultList();
        if (!list.isEmpty()) {
            return safe(list.get(0).getMontantUsd());
        }

        String jpql = """
                SELECT SUM(COALESCE(l.remained,0))
                FROM Livraison l
                WHERE l.region LIKE :region
                  AND l.dateLivr <= :atDate
                  AND (l.observation IS NULL OR l.observation <> 'Annulée')
                """;
        Double result;
        if (ManagedSessionFactory.isEmbedded()) {
            result = ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, Double.class)
                    .setParameter("region", usedRegion)
                    .setParameter("atDate", atDate)
                    .getSingleResult());
        } else {
            result = ManagedSessionFactory.getEntityManager().createQuery(jpql, Double.class)
                    .setParameter("region", usedRegion)
                    .setParameter("atDate", atDate)
                    .getSingleResult();
        }
        double amount = result == null ? 0d : scale(result);

        DetteFournisseurAgregate agg = new DetteFournisseurAgregate();
        agg.setDate(atDate);
        agg.setRegion(usedRegion);
        agg.setMontantUsd(amount);
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(agg);
                return null;
            }).join();
        } else {
            EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
            if (!tx.isActive()) tx.begin();
            ManagedSessionFactory.getEntityManager().persist(agg);
            tx.commit();
        }
        return amount;
    }

    private double sumTreasuryBalanceUntil(LocalDate atDate, String region) {
        if (atDate == null) {
            return 0d;
        }
        String usedRegion = normalizeRegion(region);
        String jpqlCheck = "SELECT t FROM TresorerieAgregate t WHERE t.date = :atDate AND t.region = :region AND t.mouvement = 'BALANCE' AND t.categorie = 'TOTAL'";
        List<TresorerieAgregate> list = ManagedSessionFactory.isEmbedded()
                ? ManagedSessionFactory.executeRead(em -> em.createQuery(jpqlCheck, TresorerieAgregate.class)
                        .setParameter("atDate", atDate).setParameter("region", usedRegion).getResultList())
                : ManagedSessionFactory.getEntityManager().createQuery(jpqlCheck, TresorerieAgregate.class)
                        .setParameter("atDate", atDate).setParameter("region", usedRegion).getResultList();
        if (!list.isEmpty()) {
            return safe(list.get(0).getMontantUsd());
        }

        String sql = """
                SELECT SUM(
                    CASE
                        WHEN t.mouvement = 'AUGMENTATION' THEN COALESCE(t.montantUsd,0)
                        WHEN t.mouvement = 'DIMINUTION' THEN -COALESCE(t.montantUsd,0)
                        ELSE 0
                    END
                )
                FROM traisorerie t
                WHERE t.date <= ?
                  AND t.region LIKE ?
                """;
        Object raw = singleNativeResult(sql, atDate.atTime(23, 59, 59), usedRegion);
        double amount = raw == null ? 0d : scale(((Number) raw).doubleValue());

        TresorerieAgregate agg = new TresorerieAgregate();
        agg.setDate(atDate);
        agg.setRegion(usedRegion);
        agg.setMouvement("BALANCE");
        agg.setCategorie("TOTAL");
        agg.setMontantUsd(amount);
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> { em.persist(agg); return null; }).join();
        } else {
            EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
            if (!tx.isActive()) tx.begin();
            ManagedSessionFactory.getEntityManager().persist(agg);
            tx.commit();
        }
        return amount;
    }

    private double sumBankBalanceUntil(LocalDate atDate, String region) {
        if (atDate == null) {
            return 0d;
        }
        String usedRegion = normalizeRegion(region);
        String jpqlCheck = "SELECT t FROM TresorerieAgregate t WHERE t.date = :atDate AND t.region = :region AND t.mouvement = 'BALANCE' AND t.categorie = 'BANQUE'";
        List<TresorerieAgregate> list = ManagedSessionFactory.isEmbedded()
                ? ManagedSessionFactory.executeRead(em -> em.createQuery(jpqlCheck, TresorerieAgregate.class)
                        .setParameter("atDate", atDate).setParameter("region", usedRegion).getResultList())
                : ManagedSessionFactory.getEntityManager().createQuery(jpqlCheck, TresorerieAgregate.class)
                        .setParameter("atDate", atDate).setParameter("region", usedRegion).getResultList();
        if (!list.isEmpty()) {
            return safe(list.get(0).getMontantUsd());
        }

        String sql = """
                SELECT SUM(
                    CASE
                        WHEN t.mouvement = 'AUGMENTATION' THEN COALESCE(t.montantUsd,0)
                        WHEN t.mouvement = 'DIMINUTION' THEN -COALESCE(t.montantUsd,0)
                        ELSE 0
                    END
                )
                FROM traisorerie t
                INNER JOIN compte_tresor c ON c.uid = t.tresor_id
                WHERE t.date <= ?
                  AND t.region LIKE ?
                  AND LOWER(COALESCE(c.type_compte,'')) = 'banque'
                """;
        Object raw = singleNativeResult(sql, atDate.atTime(23, 59, 59), usedRegion);
        double amount = raw == null ? 0d : scale(((Number) raw).doubleValue());

        TresorerieAgregate agg = new TresorerieAgregate();
        agg.setDate(atDate);
        agg.setRegion(usedRegion);
        agg.setMouvement("BALANCE");
        agg.setCategorie("BANQUE");
        agg.setMontantUsd(amount);
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> { em.persist(agg); return null; }).join();
        } else {
            EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
            if (!tx.isActive()) tx.begin();
            ManagedSessionFactory.getEntityManager().persist(agg);
            tx.commit();
        }
        return amount;
    }

    private double sumBorrowingFlowsUntil(LocalDate atDate, String region) {
        if (atDate == null) {
            return 0d;
        }
        double incoming = sumTreasuryByKeywords(LocalDate.of(2000, 1, 1), atDate, region, true, KEYWORDS_BORROWINGS);
        double outgoing = sumTreasuryByKeywords(LocalDate.of(2000, 1, 1), atDate, region, false, KEYWORDS_BORROWINGS);
        return Math.max(0d, scale(incoming - outgoing));
    }

    private double sumPastNetResults(LocalDate beforeStart, String region) {
        String jpql = """
                SELECT SUM(COALESCE(f.amountUsd,0))
                FROM FinancialStatementAgregate f
                WHERE f.statementType = :statementType
                  AND f.lineCode = :lineCode
                  AND f.region = :region
                  AND f.periodEnd < :beforeStart
                """;
        if (ManagedSessionFactory.isEmbedded()) {
            Double result = ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, Double.class)
                    .setParameter("statementType", STATEMENT_COMPTE_RESULTAT)
                    .setParameter("lineCode", "XJ")
                    .setParameter("region", normalizeRegion(region))
                    .setParameter("beforeStart", beforeStart)
                    .getSingleResult());
            return result == null ? 0d : scale(result);
        }
        Double result = ManagedSessionFactory.getEntityManager().createQuery(jpql, Double.class)
                .setParameter("statementType", STATEMENT_COMPTE_RESULTAT)
                .setParameter("lineCode", "XJ")
                .setParameter("region", normalizeRegion(region))
                .setParameter("beforeStart", beforeStart)
                .getSingleResult();
        return result == null ? 0d : scale(result);
    }

    private double previousSnapshotValue(String statementType, String lineCode, LocalDate beforeStart, String region) {
        String jpql = """
                SELECT f FROM FinancialStatementAgregate f
                WHERE f.statementType = :statementType
                  AND f.lineCode = :lineCode
                  AND f.region = :region
                  AND f.periodEnd < :beforeStart
                ORDER BY f.periodEnd DESC
                """;
        List<FinancialStatementAgregate> rows;
        if (ManagedSessionFactory.isEmbedded()) {
            rows = ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, FinancialStatementAgregate.class)
                    .setParameter("statementType", statementType)
                    .setParameter("lineCode", lineCode)
                    .setParameter("region", normalizeRegion(region))
                    .setParameter("beforeStart", beforeStart)
                    .setMaxResults(1)
                    .getResultList());
        } else {
            rows = ManagedSessionFactory.getEntityManager().createQuery(jpql, FinancialStatementAgregate.class)
                    .setParameter("statementType", statementType)
                    .setParameter("lineCode", lineCode)
                    .setParameter("region", normalizeRegion(region))
                    .setParameter("beforeStart", beforeStart)
                    .setMaxResults(1)
                    .getResultList();
        }
        return rows.isEmpty() ? 0d : safe(rows.get(0).getAmountUsd());
    }

    private Map<String, Double> previousSnapshotValues(String statementType, LocalDate beforeStart, String region) {
        List<PeriodRange> periods = findPreviousPeriods(statementType, beforeStart, region, 1);
        if (periods.isEmpty()) {
            return Collections.emptyMap();
        }
        PeriodRange period = periods.get(0);
        List<FinancialStatementAgregate> rows = findRows(statementType, period.start(), period.end(), region);
        Map<String, Double> values = new HashMap<>();
        for (FinancialStatementAgregate row : rows) {
            values.put(row.getLineCode(), row.getAmountUsd());
        }
        return values;
    }

    private List<DepenseAgregate> findDepenseAgregates(LocalDate start, LocalDate end, String region) {
        String jpql = """
                SELECT d FROM DepenseAgregate d
                LEFT JOIN FETCH d.depenseId
                WHERE d.date BETWEEN :startDate AND :endDate
                  AND d.region LIKE :region
                ORDER BY d.date ASC
                """;
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, DepenseAgregate.class)
                    .setParameter("startDate", start.atStartOfDay())
                    .setParameter("endDate", end.atTime(23, 59, 59))
                    .setParameter("region", normalizeRegion(region))
                    .getResultList());
        }
        return ManagedSessionFactory.getEntityManager().createQuery(jpql, DepenseAgregate.class)
                .setParameter("startDate", start.atStartOfDay())
                .setParameter("endDate", end.atTime(23, 59, 59))
                .setParameter("region", normalizeRegion(region))
                .getResultList();
    }

    private double sumAmortizationExpense(LocalDate start, LocalDate end, String region) {
        Map<String, Double> endValues = latestAmortizationByImmobilisation(end, region);
        Map<String, Double> startValues = latestAmortizationByImmobilisation(start.minusDays(1), region);
        if (endValues.isEmpty()) {
            double fallback = 0d;
            for (Immobilisation immobilisation : findImmobilisations(region)) {
                double endCumul = immobilisation.amortissementCumulUsd(end);
                double startCumul = start.minusDays(1).isBefore(LocalDate.of(1900, 1, 1))
                        ? 0d
                        : immobilisation.amortissementCumulUsd(start.minusDays(1));
                fallback += Math.max(0d, endCumul - startCumul);
            }
            return scale(fallback);
        }
        double total = 0d;
        for (Map.Entry<String, Double> entry : endValues.entrySet()) {
            total += Math.max(0d, safe(entry.getValue()) - safe(startValues.get(entry.getKey())));
        }
        return scale(total);
    }

    private Map<String, Double> latestAmortizationByImmobilisation(LocalDate atDate, String region) {
        if (atDate == null) {
            return Collections.emptyMap();
        }
        String jpql = """
                SELECT i FROM ImmobilisationAgregate i
                LEFT JOIN FETCH i.immobilisationId
                WHERE i.region LIKE :region
                  AND i.date <= :atDate
                ORDER BY i.date DESC
                """;
        List<ImmobilisationAgregate> rows;
        if (ManagedSessionFactory.isEmbedded()) {
            rows = ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, ImmobilisationAgregate.class)
                    .setParameter("region", normalizeRegion(region))
                    .setParameter("atDate", atDate.atTime(23, 59, 59))
                    .getResultList());
        } else {
            rows = ManagedSessionFactory.getEntityManager().createQuery(jpql, ImmobilisationAgregate.class)
                    .setParameter("region", normalizeRegion(region))
                    .setParameter("atDate", atDate.atTime(23, 59, 59))
                    .getResultList();
        }
        Map<String, Double> result = new LinkedHashMap<>();
        for (ImmobilisationAgregate row : rows) {
            if (row.getImmobilisationId() == null || row.getImmobilisationId().getUid() == null) {
                continue;
            }
            result.putIfAbsent(row.getImmobilisationId().getUid(), safe(row.getAmmortissement()));
        }
        return result;
    }

    private Map<String, ImmobilisationSnapshot> latestImmobilisationSnapshots(LocalDate atDate, String region) {
        if (atDate == null) {
            return Collections.emptyMap();
        }
        String jpql = """
                SELECT i FROM ImmobilisationAgregate i
                LEFT JOIN FETCH i.immobilisationId
                WHERE i.region LIKE :region
                  AND i.date <= :atDate
                ORDER BY i.date DESC
                """;
        List<ImmobilisationAgregate> rows;
        if (ManagedSessionFactory.isEmbedded()) {
            rows = ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, ImmobilisationAgregate.class)
                    .setParameter("region", normalizeRegion(region))
                    .setParameter("atDate", atDate.atTime(23, 59, 59))
                    .getResultList());
        } else {
            rows = ManagedSessionFactory.getEntityManager().createQuery(jpql, ImmobilisationAgregate.class)
                    .setParameter("region", normalizeRegion(region))
                    .setParameter("atDate", atDate.atTime(23, 59, 59))
                    .getResultList();
        }
        Map<String, ImmobilisationSnapshot> snapshots = new LinkedHashMap<>();
        for (ImmobilisationAgregate row : rows) {
            if (row.getImmobilisationId() == null || row.getImmobilisationId().getUid() == null) {
                continue;
            }
            snapshots.putIfAbsent(row.getImmobilisationId().getUid(),
                    new ImmobilisationSnapshot(
                            row.getImmobilisationId().getUid(),
                            row.getImmobilisationId().getLibelle(),
                            row.getImmobilisationId().getCategorie(),
                            safe(row.getValeurBrutte()),
                            safe(row.getAmmortissement()),
                            safe(row.getValeurNette())));
        }
        return snapshots;
    }

    private List<Immobilisation> findImmobilisations(String region) {
        String jpql = "SELECT i FROM Immobilisation i WHERE i.region LIKE :region";
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(
                    em -> em.createQuery(jpql, Immobilisation.class)
                            .setParameter("region", normalizeRegion(region))
                            .getResultList());
        }
        return ManagedSessionFactory.getEntityManager().createQuery(jpql, Immobilisation.class)
                .setParameter("region", normalizeRegion(region))
                .getResultList();
    }

    private double sumTreasuryByKeywords(LocalDate start, LocalDate end, String region, boolean incoming, String[] keywords) {
        if (start == null || end == null) {
            return 0d;
        }
        String usedRegion = normalizeRegion(region);
        String mvt = incoming ? "IN" : "OUT";
        String catRaw = String.join("_", keywords);
        if (catRaw.length() > 220) {
            catRaw = catRaw.substring(0, 220);
        }
        final String cat = catRaw;
        String periodCat = start + "|" + end + "|" + cat;
        String jpqlCheck = "SELECT t FROM TresorerieAgregate t WHERE t.date = :endDate AND t.region = :region AND t.mouvement = :mvt AND t.categorie = :cat";
        List<TresorerieAgregate> list = ManagedSessionFactory.isEmbedded()
                ? ManagedSessionFactory.executeRead(em -> em.createQuery(jpqlCheck, TresorerieAgregate.class)
                        .setParameter("endDate", end).setParameter("region", usedRegion)
                        .setParameter("mvt", mvt).setParameter("cat", periodCat).getResultList())
                : ManagedSessionFactory.getEntityManager().createQuery(jpqlCheck, TresorerieAgregate.class)
                        .setParameter("endDate", end).setParameter("region", usedRegion)
                        .setParameter("mvt", mvt).setParameter("cat", periodCat).getResultList();
        if (!list.isEmpty()) {
            return safe(list.get(0).getMontantUsd());
        }

        double total = 0d;
        for (Traisorerie row : findTreasuryEntries(start, end, usedRegion)) {
            if (incoming && !Mouvment.AUGMENTATION.name().equalsIgnoreCase(row.getMouvement())) {
                continue;
            }
            if (!incoming && !Mouvment.DIMINUTION.name().equalsIgnoreCase(row.getMouvement())) {
                continue;
            }
            String label = normalize(row.getLibelle()) + " " + normalize(row.getReference());
            if (containsAny(label, keywords)) {
                total += safe(row.getMontantUsd());
            }
        }
        double amount = scale(total);

        TresorerieAgregate agg = new TresorerieAgregate();
        agg.setDate(end);
        agg.setRegion(usedRegion);
        agg.setMouvement(mvt);
        agg.setCategorie(periodCat);
        agg.setMontantUsd(amount);
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> { em.persist(agg); return null; }).join();
        } else {
            EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
            if (!tx.isActive()) tx.begin();
            ManagedSessionFactory.getEntityManager().persist(agg);
            tx.commit();
        }
        return amount;
    }

    private double sumTreasuryByKeywordsUntil(LocalDate end, String region, boolean incoming, String[] keywords) {
        return sumTreasuryByKeywords(LocalDate.of(2000, 1, 1), end, region, incoming, keywords);
    }

    private List<Traisorerie> findTreasuryEntries(LocalDate start, LocalDate end, String region) {
        String jpql = """
                SELECT t FROM Traisorerie t
                LEFT JOIN FETCH t.tresorId
                WHERE t.date BETWEEN :startDate AND :endDate
                  AND t.region LIKE :region
                ORDER BY t.date ASC
                """;
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.createQuery(jpql, Traisorerie.class)
                    .setParameter("startDate", start.atStartOfDay())
                    .setParameter("endDate", end.atTime(23, 59, 59))
                    .setParameter("region", normalizeRegion(region))
                    .getResultList());
        }
        return ManagedSessionFactory.getEntityManager().createQuery(jpql, Traisorerie.class)
                .setParameter("startDate", start.atStartOfDay())
                .setParameter("endDate", end.atTime(23, 59, 59))
                .setParameter("region", normalizeRegion(region))
                .getResultList();
    }

    private Object singleNativeResult(String sql, Object... params) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sql);
                for (int i = 0; i < params.length; i++) {
                    query.setParameter(i + 1, params[i]);
                }
                return query.getSingleResult();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sql);
        for (int i = 0; i < params.length; i++) {
            query.setParameter(i + 1, params[i]);
        }
        return query.getSingleResult();
    }

    private boolean containsAny(String value, String[] keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private LocalDate toLocalDate(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof java.sql.Date date) {
            return date.toLocalDate();
        }
        if (raw instanceof LocalDate date) {
            return date;
        }
        return LocalDate.parse(String.valueOf(raw));
    }

    private double safe(Double value) {
        return value == null ? 0d : value;
    }

    private double scale(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private String normalizeRegion(String region) {
        return (region == null || region.isBlank()) ? "%" : region;
    }

    private static final String[] KEYWORDS_IMMO_INCORP = {"logiciel", "licence", "brevet", "marque", "fonds commercial", "incorp"};
    private static final String[] KEYWORDS_IMMO_FIN = {"participation", "titre", "pret", "financier", "caution"};
    private static final String[] KEYWORDS_LAND_BUILDINGS = {"terrain", "batiment", "immeuble", "construction"};
    private static final String[] KEYWORDS_RAW_MATERIALS = {"matiere", "intrant", "emballage", "approvision", "fourniture de production"};
    private static final String[] KEYWORDS_TAXES = {"impot", "taxe", "fisc", "cnss", "dgi", "etat"};
    private static final String[] KEYWORDS_PERSONNEL = {"salaire", "personnel", "paie", "prime", "social"};
    private static final String[] KEYWORDS_TRANSPORT = {"transport", "carburant", "fuel", "logistique"};
    private static final String[] KEYWORDS_SERVICES = {"loyer", "assurance", "honoraire", "maintenance", "internet",
            "telephone", "électricité", "electricite", "eau", "consult", "service", "commission", "reparation"};
    private static final String[] KEYWORDS_CAPITAL = {"capital", "apport", "actionnaire", "associe", "associé"};
    private static final String[] KEYWORDS_DIVIDENDS = {"dividende"};
    private static final String[] KEYWORDS_BORROWINGS = {"emprunt", "prêt", "pret", "crédit", "credit", "agios", "dette bancaire"};
    private static final String[] KEYWORDS_SUBSIDIES = {"subvention", "aide", "dotation"};
    private static final String[] KEYWORDS_OTHER_OPERATING_INCOME = {"commission recue", "redevance", "location", "plus value courante"};
    private static final String[] KEYWORDS_REVERSALS = {"reprise", "annulation provision"};
    private static final String[] KEYWORDS_FINANCIAL_INCOME = {"interet recu", "intérêt reçu", "dividende recu", "gain de change"};
    private static final String[] KEYWORDS_FINANCIAL_CHARGES = {"interet", "intérêt", "agio", "perte de change", "frais bancaire"};
    private static final String[] KEYWORDS_HAO_INCOME = {"cession", "vente immobilisation", "produit exceptionnel"};
    private static final String[] KEYWORDS_HAO_CHARGES = {"amende", "pénalité", "penalite", "charge exceptionnelle"};
    private static final String[] KEYWORDS_PARTICIPATION = {"participation travailleurs", "quote part", "quote-part"};
    private static final String[] KEYWORDS_INCOME_TAX = {"impot sur le resultat", "is", "impôt sur les sociétés"};
    private static final String[] KEYWORDS_ACQUISITIONS = {"acquisition", "achat immobilisation", "investissement",
            "machine", "equipement", "équipement", "terrain", "batiment", "bâtiment", "vehicule", "véhicule"};
    private static final String[] KEYWORDS_ASSET_DISPOSALS = {"cession", "vente immobilisation", "vente actif"};
    private static final String[] KEYWORDS_FINANCIAL_ASSET_REDUCTIONS = {"remboursement pret", "cession titre", "désinvestissement", "desinvestissement"};

    private record PeriodRange(LocalDate start, LocalDate end) {
    }

    private record ImmobilisationSnapshot(String uid, String libelle, String categorie, double grossUsd,
            double amortizationUsd, double netUsd) {
    }

    private record ImmobilisationAmounts(double gross, double amortization, double net) {

        private ImmobilisationAmounts plus(ImmobilisationAmounts other) {
            return new ImmobilisationAmounts(gross + other.gross, amortization + other.amortization,
                    net + other.net);
        }

        private boolean hasValue() {
            return Math.abs(gross) > 0.001 || Math.abs(amortization) > 0.001 || Math.abs(net) > 0.001;
        }
    }

    private static final class CoreMetrics {
        double sales;
        double costOfSales;
        double stockOpen;
        double stockClose;
        double stockVariation;
        double clientDebt;
        double doubtfulReceivables;
        double otherReceivables;
        double supplierDebt;
        double taxAndSocialDebt;
        double totalTreasuryOpen;
        double totalTreasuryClose;
        double shortTermBankFunding;
        double longTermDebtClose;
        double capitalInflowsToDate;
        double retainedResultsToDate;
        double dividendsToDate;
        double dividendsPaidInPeriod;
        double rawMaterialPurchases;
        double rawMaterialVariation;
        double otherPurchases;
        double otherPurchasesVariation;
        double transportCharges;
        double externalServices;
        double taxesAndDuties;
        double personnelCharges;
        double otherOperatingCharges;
        double operatingSubsidies;
        double otherOperatingIncome;
        double amortizationExpense;
        double reversalProvisionIncome;
        double financialIncome;
        double financialCharges;
        double haoIncome;
        double haoCharges;
        double workerParticipation;
        double incomeTaxCharge;
        double manufacturedSales;
        double servicesSales;
        double accessoryIncome;
        double finishedGoodsVariation;
        double selfConstructedProduction;
        double immobIncorp;
        double immobCorp;
        double immobLandBuildings;
        double immobFin;
        double investmentAcquisitions;
        double assetDisposals;
        double financialAssetReductions;
        double capitalIncreaseInPeriod;
        double newBorrowingsInPeriod;
        double borrowingRepaymentsInPeriod;
    }
}
