package tools;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.prefs.Preferences;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Centralise les conversions multi-devise sans casser le stockage historique
 * USD/CDF. Les taux sont exprimes comme suit: 1 USD = N devise.
 */
public final class CurrencyConverter {

    public static final String USD = "USD";
    public static final String CDF = "CDF";
    private static final String PREF_CURRENCIES = "currencies";
    private static final String PREF_MAIN_CURRENCY = "mainCur";
    private static final String PREF_LEGACY_CDF_RATE = "taux2change";
    private static final String PREF_RATE_PREFIX = "currency.rate.";
    private static final double DEFAULT_CDF_RATE = 2000.0;

    private CurrencyConverter() {
    }

    public static Preferences preferences() {
        return Preferences.userNodeForPackage(SyncEngine.class);
    }

    public static String normalize(String currency) {
        if (currency == null || currency.trim().isEmpty()) {
            return USD;
        }
        String code = currency.trim().toUpperCase(Locale.ROOT);
        if ("$".equals(code) || "US$".equals(code)) {
            return USD;
        }
        if ("FC".equals(code) || "CDF".equals(code) || "FRANCS".equals(code)) {
            return CDF;
        }
        if ("€".equals(code)) {
            return "EUR";
        }
        if ("£".equals(code)) {
            return "GBP";
        }
        return code;
    }

    public static String mainCurrency() {
        return normalize(preferences().get(PREF_MAIN_CURRENCY, USD));
    }

    public static void setMainCurrency(String currency) {
        String normalized = normalize(currency);
        preferences().put(PREF_MAIN_CURRENCY, normalized);
        addCurrency(normalized);
    }

    public static ObservableList<String> fxCurrencies() {
        return FXCollections.observableArrayList(supportedCurrencies());
    }

    public static List<String> supportedCurrencies() {
        Preferences pref = preferences();
        Set<String> currencies = new LinkedHashSet<>();
        currencies.add(USD);
        currencies.add(CDF);
        String configured = pref.get(PREF_CURRENCIES, "");
        for (String raw : configured.split(",")) {
            String code = normalize(raw);
            if (!code.isEmpty()) {
                currencies.add(code);
            }
        }
        currencies.add(mainCurrency());
        List<String> list = new ArrayList<>(currencies);
        pref.put(PREF_CURRENCIES, String.join(",", list));
        return list;
    }

    public static void addCurrency(String currency) {
        String code = normalize(currency);
        if (code.isEmpty()) {
            return;
        }
        Set<String> currencies = new LinkedHashSet<>(supportedCurrencies());
        currencies.add(code);
        preferences().put(PREF_CURRENCIES, String.join(",", currencies));
    }

    public static double legacyCdfRate() {
        Preferences pref = preferences();
        double rate = pref.getDouble(PREF_RATE_PREFIX + CDF, pref.getDouble(PREF_LEGACY_CDF_RATE, DEFAULT_CDF_RATE));
        if (rate <= 0) {
            rate = DEFAULT_CDF_RATE;
        }
        pref.putDouble(PREF_LEGACY_CDF_RATE, rate);
        pref.putDouble(PREF_RATE_PREFIX + CDF, rate);
        return rate;
    }

    public static void saveLegacyCdfRate(double rate) {
        if (rate <= 0) {
            throw new IllegalArgumentException("Le taux doit etre superieur a zero.");
        }
        Preferences pref = preferences();
        pref.putDouble(PREF_LEGACY_CDF_RATE, rate);
        pref.putDouble(PREF_RATE_PREFIX + CDF, rate);
        addCurrency(CDF);
    }

    public static double rateFromUsd(String currency) {
        String code = normalize(currency);
        if (USD.equals(code)) {
            return 1.0;
        }
        if (CDF.equals(code)) {
            return legacyCdfRate();
        }
        double rate = preferences().getDouble(PREF_RATE_PREFIX + code, 0.0);
        if (rate <= 0) {
            throw new IllegalStateException("Aucun taux configure pour la devise " + code + ".");
        }
        return rate;
    }

    public static void saveRateFromUsd(String currency, double rate) {
        if (rate <= 0) {
            throw new IllegalArgumentException("Le taux doit etre superieur a zero.");
        }
        String code = normalize(currency);
        if (USD.equals(code)) {
            return;
        }
        preferences().putDouble(PREF_RATE_PREFIX + code, rate);
        if (CDF.equals(code)) {
            preferences().putDouble(PREF_LEGACY_CDF_RATE, rate);
        }
        addCurrency(code);
    }

    public static double toUsd(double amount, String currency) {
        return round(amount / rateFromUsd(currency));
    }

    public static double fromUsd(double usdAmount, String currency) {
        return round(usdAmount * rateFromUsd(currency));
    }

    public static BigDecimal toUsd(BigDecimal amount, String currency) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        }
        BigDecimal rate = BigDecimal.valueOf(rateFromUsd(currency));
        return roundMoney(amount.divide(rate, 8, RoundingMode.HALF_EVEN));
    }

    public static BigDecimal fromUsd(BigDecimal usdAmount, String currency) {
        if (usdAmount == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        }
        BigDecimal rate = BigDecimal.valueOf(rateFromUsd(currency));
        return roundMoney(usdAmount.multiply(rate));
    }

    public static double convert(double amount, String fromCurrency, String toCurrency) {
        String from = normalize(fromCurrency);
        String to = normalize(toCurrency);
        if (from.equals(to)) {
            return round(amount);
        }
        return fromUsd(toUsd(amount, from), to);
    }

    public static BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        String from = normalize(fromCurrency);
        String to = normalize(toCurrency);
        if (from.equals(to)) {
            return roundMoney(amount);
        }
        return fromUsd(toUsd(amount, from), to);
    }

    public static AmountUsdCdf splitForLegacyStorage(double amount, String currency) {
        String code = normalize(currency);
        if (CDF.equals(code)) {
            return new AmountUsdCdf(0.0, round(amount));
        }
        return new AmountUsdCdf(toUsd(amount, code), 0.0);
    }

    public static double amountFromLegacyStorage(double usd, double cdf, String targetCurrency) {
        double totalUsd = usd + toUsd(cdf, CDF);
        return fromUsd(totalUsd, targetCurrency);
    }

    public static String equivalentLabel(double amount, String fromCurrency) {
        String from = normalize(fromCurrency);
        String target = CDF.equals(from) ? USD : CDF;
        return round(convert(amount, from, target)) + " " + symbol(target);
    }

    public static String symbol(String currency) {
        String code = normalize(currency);
        switch (code) {
            case USD:
                return "$";
            case CDF:
                return "Fc";
            case "EUR":
                return "EUR";
            case "GBP":
                return "GBP";
            default:
                return code;
        }
    }

    public static double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }

    public static BigDecimal roundMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        }
        return value.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static String formatAmount(BigDecimal amount, String currency) {
        return roundMoney(amount).toPlainString() + " " + symbol(currency);
    }

    /** Taux CDF actif (1 USD = N CDF), synchronise avec ParametreController. */
    public static double activeRate() {
        return legacyCdfRate();
    }

    /** Prix stocke en USD converti pour affichage dans la devise principale. */
    public static double priceFromStorageUsd(double usdPrice) {
        return fromUsd(usdPrice, mainCurrency());
    }

    /** Montant saisi dans la devise principale vers USD (stockage prix). */
    public static double priceToStorageUsd(double mainCurrencyPrice) {
        return toUsd(mainCurrencyPrice, mainCurrency());
    }

    public static double legacyTotalInMainCurrency(double usd, double cdf) {
        return amountFromLegacyStorage(usd, cdf, mainCurrency());
    }

    public static String formatAmount(double amount, String currency) {
        return round(amount) + " " + symbol(currency);
    }

    public static String formatLegacyTotal(double usd, double cdf) {
        String main = mainCurrency();
        return formatAmount(amountFromLegacyStorage(usd, cdf, main), main);
    }

    public static String formatEquivalent(double amount, String fromCurrency) {
        return equivalentLabel(amount, fromCurrency);
    }

    public static double debtInMainCurrency(double debt, String debtCurrency) {
        if (debt == 0) {
            return 0;
        }
        String code = normalize(debtCurrency == null || debtCurrency.isEmpty() ? USD : debtCurrency);
        return convert(debt, code, mainCurrency());
    }

    /** Arrondi CDF sans decimales (usage panier). */
    public static double roundCdf(double value) {
        return BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_EVEN).doubleValue();
    }

    public static final class AmountUsdCdf {

        private final double usd;
        private final double cdf;

        public AmountUsdCdf(double usd, double cdf) {
            this.usd = usd;
            this.cdf = cdf;
        }

        public double getUsd() {
            return usd;
        }

        public double getCdf() {
            return cdf;
        }
    }
}
