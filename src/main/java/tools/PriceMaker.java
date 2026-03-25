/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tools;

import data.LigneVente;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 *
 * @author endeleya
 */
public class PriceMaker {

    private String mainCurrency;
    private double prix;
    private double taux;

    public PriceMaker() {
    }

    public String getMainCurrency() {
        return mainCurrency;
    }

    public void setMainCurrency(String mainCurrency) {
        this.mainCurrency = mainCurrency;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public double getTaux() {
        return taux;
    }

    public void setTaux(double taux) {
        this.taux = taux;
    }

    public double toCdf() {
        if (this.mainCurrency.equals("USD")) {
            return prix;
        } else {
            return prix * taux;
        }
    }

    public double sumFacture(List<LigneVente> lvs) {
        if (lvs == null) {
            return 0;
        }
        double rst = 0;
        for (LigneVente lv : lvs) {
            if (this.mainCurrency.equals("USD")) {
                rst += lv.getMontantUsd();
            } else {
                rst += lv.getMontantCdf();
            }
        }
        return rst;
    }

    public double sumInverseFacture(List<LigneVente> lvs) {
        if (lvs == null) {
            return 0;
        }
        double rst = 0;
        for (LigneVente lv : lvs) {
            if (this.mainCurrency.equals("CDF")) {
                rst += lv.getMontantUsd();
            } else {
                rst += lv.getMontantCdf();
            }
        }
        return rst;
    }

    public double usdToCdf(double prixUsdField) {
        return BigDecimal.valueOf(prixUsdField * taux).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }

    public boolean isUsd() {
        return this.mainCurrency.equals("USD");
    }

    public boolean isCdf() {
        return this.mainCurrency.equals("CDF");
    }

    public double cdfToUsd(double prixCdfField) {
        return BigDecimal.valueOf((prixCdfField / taux)).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }

    public double toUsd() {
        if (this.mainCurrency.equals("CDF")) {
            return prix;
        } else {
            return BigDecimal.valueOf((prix / taux)).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        }
    }

    public String getInverseCurrencyCode() {
        if (this.mainCurrency.equals("USD")) {
            return "CDF";
        } else {
            return "USD";
        }
    }

    public String getInverseCurrencySymbol() {
        if (this.mainCurrency.equals("USD")) {
            return "Fc";
        } else {
            return "$";
        }
    }
}
