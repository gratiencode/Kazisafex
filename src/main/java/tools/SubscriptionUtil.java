/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tools;

import data.Abonnement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 *
 * @author endeleya
 */
public class SubscriptionUtil {

    /**
     * Durée totale de l’abonnement
     */
    public static Duration totalDuration(Abonnement ab) {
        return Duration.ofMillis(
                Double.valueOf(ab.getNombreOperation()).longValue());
    }

    /**
     * Temps consommé
     */
    public static Duration consumedDuration(Abonnement ab) {
        long start = ab.getDateAbonnement()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        long now = System.currentTimeMillis();
        return Duration.ofMillis(Math.max(0, now - start));
    }

    public static long nextSubscriptionMillis(Abonnement ab) {
        return ab.getDateAbonnement()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .plusMillis(Double.valueOf(ab.getNombreOperation()).longValue())
                .toEpochMilli();
    }

    public static LocalDateTime nextSubscriptionDay(Abonnement ab) {
        long startMillis = ab.getDateAbonnement()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        long endMillis = startMillis + Double.valueOf(ab.getNombreOperation()).longValue();
        return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(endMillis),
                ZoneId.systemDefault());
    }

    /**
     * Temps restant
     */
    public static Duration remainingDuration(Abonnement ab) {
        Duration remaining = totalDuration(ab).minus(consumedDuration(ab));
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    /**
     * État calculé dynamiquement
     */
    public static String computeStatus(Abonnement ab) {
        if (ab.getNombreOperation() <= 0) {
            return Constants.ETAT_SUBSCRIPTION_EXPIRY;
        }
        if (!ab.getTypeAbonnement().equals(Constants.SUBSCRIPTION_PRO)) {
            Duration remaining = remainingDuration(ab);
            if (remaining.isZero()) {
                return Constants.ETAT_SUBSCRIPTION_EXPIRY;
            } else {
                return Constants.ETAT_SUBSCRIPTION_EN_CONSOMMATION;
            }
        } else {
            return Constants.ETAT_SUBSCRIPTION_VALID;
        }
    }

}
