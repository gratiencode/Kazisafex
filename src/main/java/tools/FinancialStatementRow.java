package tools;

public class FinancialStatementRow {

    private final String code;
    private final String rubrique;
    private final String nature;
    private final Double amountN;
    private final Double amountN1;
    private final Double amountN2;
    private final Double amountN3;
    private final Double amountN4;
    private final Double grossAmount;
    private final Double amortizationAmount;
    private final Double netAmount;
    private final boolean sectionHeader;
    private final boolean totalLine;

    public FinancialStatementRow(String code, String rubrique, String nature, Double amountN, Double amountN1,
            Double amountN2, Double amountN3, Double amountN4, boolean sectionHeader, boolean totalLine) {
        this.code = code;
        this.rubrique = rubrique;
        this.nature = nature;
        this.amountN = amountN;
        this.amountN1 = amountN1;
        this.amountN2 = amountN2;
        this.amountN3 = amountN3;
        this.amountN4 = amountN4;
        this.grossAmount = null;
        this.amortizationAmount = null;
        this.netAmount = null;
        this.sectionHeader = sectionHeader;
        this.totalLine = totalLine;
    }

    public FinancialStatementRow(String code, String rubrique, String nature, Double amountN, Double amountN1,
            Double amountN2, Double amountN3, Double amountN4, Double grossAmount, Double amortizationAmount,
            Double netAmount, boolean sectionHeader, boolean totalLine) {
        this.code = code;
        this.rubrique = rubrique;
        this.nature = nature;
        this.amountN = amountN;
        this.amountN1 = amountN1;
        this.amountN2 = amountN2;
        this.amountN3 = amountN3;
        this.amountN4 = amountN4;
        this.grossAmount = grossAmount;
        this.amortizationAmount = amortizationAmount;
        this.netAmount = netAmount;
        this.sectionHeader = sectionHeader;
        this.totalLine = totalLine;
    }

    public String getCode() {
        return code;
    }

    public String getRubrique() {
        return rubrique;
    }

    public String getNature() {
        return nature;
    }

    public Double getAmountN() {
        return amountN;
    }

    public Double getAmountN1() {
        return amountN1;
    }

    public Double getAmountN2() {
        return amountN2;
    }

    public Double getAmountN3() {
        return amountN3;
    }

    public Double getAmountN4() {
        return amountN4;
    }

    public Double getGrossAmount() {
        return grossAmount;
    }

    public Double getAmortizationAmount() {
        return amortizationAmount;
    }

    public Double getNetAmount() {
        return netAmount;
    }

    public boolean isSectionHeader() {
        return sectionHeader;
    }

    public boolean isTotalLine() {
        return totalLine;
    }
}
