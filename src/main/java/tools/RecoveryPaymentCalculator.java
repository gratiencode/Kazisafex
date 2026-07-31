package tools;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calcul bidirectionnel USD/CDF pour paiements et recouvrements.
 * Pivot interne en USD ; arrondi monetaire HALF_EVEN a 2 decimales.
 */
public final class RecoveryPaymentCalculator {

    private static final int SCALE = 2;
    private static final int DIV_SCALE = 10;

    private RecoveryPaymentCalculator() {
    }

    public static final class Result {

        private final BigDecimal remainingUsd;
        private final BigDecimal remainingCdf;
        private final BigDecimal changeUsd;
        private final BigDecimal changeCdf;
        private final BigDecimal storedUsd;
        private final BigDecimal storedCdf;
        private final BigDecimal debtUsd;

        public Result(BigDecimal remainingUsd, BigDecimal remainingCdf,
                BigDecimal changeUsd, BigDecimal changeCdf,
                BigDecimal storedUsd, BigDecimal storedCdf, BigDecimal debtUsd) {
            this.remainingUsd = remainingUsd;
            this.remainingCdf = remainingCdf;
            this.changeUsd = changeUsd;
            this.changeCdf = changeCdf;
            this.storedUsd = storedUsd;
            this.storedCdf = storedCdf;
            this.debtUsd = debtUsd;
        }

        public double getRemainingUsd() {
            return remainingUsd.doubleValue();
        }

        public double getRemainingCdf() {
            return remainingCdf.doubleValue();
        }

        public double getChangeUsd() {
            return changeUsd.doubleValue();
        }

        public double getChangeCdf() {
            return changeCdf.doubleValue();
        }

        public double getStoredUsd() {
            return storedUsd.doubleValue();
        }

        public double getStoredCdf() {
            return storedCdf.doubleValue();
        }

        public double getDebtUsd() {
            return debtUsd.doubleValue();
        }

        public BigDecimal getRemainingUsdBd() {
            return remainingUsd;
        }

        public BigDecimal getRemainingCdfBd() {
            return remainingCdf;
        }

        public BigDecimal getChangeUsdBd() {
            return changeUsd;
        }

        public BigDecimal getChangeCdfBd() {
            return changeCdf;
        }

        public BigDecimal getStoredUsdBd() {
            return storedUsd;
        }

        public BigDecimal getStoredCdfBd() {
            return storedCdf;
        }

        public BigDecimal getDebtUsdBd() {
            return debtUsd;
        }
    }

    /**
     * Calcule reste, monnaie a rendre et montants a persister.
     *
     * @param debtUsd    dette initiale en USD
     * @param debtCdf    dette initiale en CDF
     * @param inputUsd   montant saisi USD (0 si vide)
     * @param inputCdf   montant saisi CDF (0 si vide)
     * @param rate       taux 1 USD = rate CDF
     */
    public static Result compute(double debtUsd, double debtCdf,
            double inputUsd, double inputCdf, double rate) {
        return compute(BigDecimal.valueOf(debtUsd), BigDecimal.valueOf(debtCdf),
                BigDecimal.valueOf(inputUsd), BigDecimal.valueOf(inputCdf),
                BigDecimal.valueOf(rate));
    }

    public static Result compute(BigDecimal debtUsd, BigDecimal debtCdf,
            BigDecimal inputUsd, BigDecimal inputCdf, BigDecimal rate) {
        BigDecimal rateBd = rateBd(rate);
        BigDecimal usdBd = bd(debtUsd);
        BigDecimal cdfBd = bd(debtCdf);
        BigDecimal inUsd = bd(inputUsd);
        BigDecimal inCdf = bd(inputCdf);

        boolean usdEmpty = inUsd.compareTo(BigDecimal.ZERO) == 0;
        boolean cdfEmpty = inCdf.compareTo(BigDecimal.ZERO) == 0;

        if (usdEmpty && cdfEmpty) {
            return new Result(
                    roundBd(usdBd), roundBd(cdfBd),
                    zero(), zero(),
                    zero(), zero(),
                    roundBd(usdBd));
        }

        if (!usdEmpty && cdfEmpty) {
            BigDecimal restUsd = usdBd.subtract(inUsd);
            if (restUsd.compareTo(BigDecimal.ZERO) >= 0) {
                BigDecimal restCdf = restUsd.multiply(rateBd);
                return new Result(
                        roundBd(restUsd), roundBd(restCdf),
                        zero(), zero(),
                        roundBd(inUsd), zero(),
                        roundBd(restUsd));
            }
            BigDecimal retour = restUsd.abs();
            BigDecimal storedUsd = inUsd.subtract(retour);
            return new Result(
                    zero(), zero(),
                    roundBd(retour), roundBd(retour.multiply(rateBd)),
                    roundBd(storedUsd), zero(),
                    zero());
        }

        if (usdEmpty && !cdfEmpty) {
            BigDecimal restCdf = cdfBd.subtract(inCdf);
            if (restCdf.compareTo(BigDecimal.ZERO) >= 0) {
                BigDecimal restUsd = restCdf.divide(rateBd, DIV_SCALE, RoundingMode.HALF_EVEN);
                return new Result(
                        roundBd(restUsd), roundBd(restCdf),
                        zero(), zero(),
                        zero(), roundBd(inCdf),
                        roundBd(restUsd));
            }
            BigDecimal retour = restCdf.abs();
            BigDecimal storedCdf = inCdf.subtract(retour);
            return new Result(
                    zero(), zero(),
                    roundBd(retour.divide(rateBd, DIV_SCALE, RoundingMode.HALF_EVEN)),
                    roundBd(retour),
                    zero(), roundBd(storedCdf),
                    zero());
        }

        BigDecimal converted = inCdf.divide(rateBd, DIV_SCALE, RoundingMode.HALF_EVEN);
        BigDecimal paidUsd = inUsd.add(converted);
        BigDecimal restUsd = usdBd.subtract(paidUsd);
        if (restUsd.compareTo(BigDecimal.ZERO) >= 0) {
            BigDecimal restCdf = restUsd.multiply(rateBd);
            return new Result(
                    roundBd(restUsd), roundBd(restCdf),
                    zero(), zero(),
                    roundBd(inUsd), roundBd(inCdf),
                    roundBd(restUsd));
        }
        BigDecimal retour = restUsd.abs();
        BigDecimal storedUsd = paidUsd.subtract(retour);
        return new Result(
                zero(), zero(),
                roundBd(retour), roundBd(retour.multiply(rateBd)),
                roundBd(storedUsd), zero(),
                zero());
    }

    public static double parseInput(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public static double[] initialDebtAmounts(double amount, String amountCurrency, double rate) {
        BigDecimal[] amounts = initialDebtAmounts(BigDecimal.valueOf(amount), amountCurrency, BigDecimal.valueOf(rate));
        return new double[]{amounts[0].doubleValue(), amounts[1].doubleValue()};
    }

    public static BigDecimal[] initialDebtAmounts(BigDecimal amount, String amountCurrency, BigDecimal rate) {
        String cur = CurrencyConverter.normalize(amountCurrency);
        if (CurrencyConverter.CDF.equals(cur)) {
            BigDecimal cdf = CurrencyConverter.roundMoney(amount);
            BigDecimal usd = CurrencyConverter.convert(amount, CurrencyConverter.CDF, CurrencyConverter.USD);
            return new BigDecimal[]{usd, cdf};
        }
        BigDecimal usd = CurrencyConverter.roundMoney(amount);
        BigDecimal cdf = CurrencyConverter.convert(amount, CurrencyConverter.USD, CurrencyConverter.CDF);
        return new BigDecimal[]{usd, cdf};
    }

    private static BigDecimal rateBd(double rate) {
        return BigDecimal.valueOf(rate <= 0 ? CurrencyConverter.legacyCdfRate() : rate);
    }

    private static BigDecimal rateBd(BigDecimal rate) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.valueOf(CurrencyConverter.legacyCdfRate());
        }
        return rate;
    }

    private static BigDecimal bd(double value) {
        return BigDecimal.valueOf(value);
    }

    private static BigDecimal bd(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static BigDecimal roundBd(BigDecimal value) {
        return bd(value).setScale(SCALE, RoundingMode.HALF_EVEN);
    }

    private static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_EVEN);
    }
}
