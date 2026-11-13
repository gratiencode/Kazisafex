package com.endeleya.kazisafex.agent;

import java.util.concurrent.atomic.LongAdder;
import services.utils.SecurePreferences;

/**
 * Budget de tokens du chef d'orchestration.
 *
 * <p>Le « budget d'itérations » de l'orchestrateur est désormais jugé UNIQUEMENT par
 * la somme des tokens consommés (entrée + sortie) rapportée au plafond « tsub » : plus
 * de compteur d'itérations arbitraire. Chaque appel LLM (planificateur, critic,
 * formateur, classifieur sémantique, retentatives directes) entre dans le cumul via
 * {@link TokenCountingChatModel}.</p>
 *
 * <p>Le plafond est lu depuis la préférence applicative {@value #PREF_KEY_TSUB}, qui
 * doit être stockée CHIFFRÉE (AES-GCM via {@link SecurePreferences}) : la valeur est
 * déchiffrée, puis convertie en entier (c'est un nombre). À défaut de valeur lisible,
 * {@link #DEFAULT_TSUB} (1&nbsp;000&nbsp;000) s'applique pour l'essai.</p>
 */
public final class TokenBudget {

    /** Cle de preference chiffree portant le plafond de tokens (« tsub »). */
    public static final String PREF_KEY_TSUB = SecurePreferences.KEY_TSUB;

    /** Plafond par défaut (préférence absente/invalide) : 1 000 000 pour l'essai. */
    public static final int DEFAULT_TSUB = 1_000_000;

    private final LongAdder consumed = new LongAdder();
    private final long cap;

    /**
     * Crée le budget avec le plafond lu (déchiffré) depuis la préférence « tsub ».
     */
    public TokenBudget() {
        this.cap = readCap();
    }

    /**
     * Crée le budget avec un plafond explicite (tests).
     *
     * @param fixedCap plafond de tokens
     */
    public TokenBudget(long fixedCap) {
        this.cap = Math.max(0, fixedCap);
    }

    /** Plafond de tokens autorisés. */
    public long cap() {
        return cap;
    }

    /** Tokens consommés au total (entrée + sortie). */
    public long consumed() {
        return consumed.sum();
    }

    /**
     * Ajoute les tokens d'un appel LLM et renvoie le nouveau total.
     *
     * @param inputTokenCount  tokens d'entrée (prompt)
     * @param outputTokenCount tokens de sortie (réponse)
     * @return total consommé après cumul
     */
    public long add(long inputTokenCount, long outputTokenCount) {
        long delta = Math.max(0, inputTokenCount) + Math.max(0, outputTokenCount);
        consumed.add(delta);
        return consumed.sum();
    }

    /** Tokens restants avant épuisement du plafond. */
    public long remaining() {
        return cap - consumed.sum();
    }

    /**
     * {@code true} dès que la différence (plafond - somme) est {@code <= 0} : le budget
     * de tokens est épuisé et la notification idoine doit s'afficher.
     */
    public boolean exhausted() {
        return remaining() <= 0;
    }

    /**
     * Garantit qu'une valeur chiffrée existe sous la clé « tsub » : si la préférence
     * est absente et qu'une passphrase (eUid) est disponible, le plafond par défaut est
     * stocké chiffré pour l'essai.
     */
    public static void ensurePresent() {
        String passphrase = SecurePreferences.getEntr();
        if (passphrase == null || passphrase.isBlank()) {
            return;
        }
        if (SecurePreferences.hasStoredValue(PREF_KEY_TSUB)) {
            return;
        }
        try {
            SecurePreferences.storeEncryptedInt(PREF_KEY_TSUB, passphrase, DEFAULT_TSUB);
        } catch (Exception ignored) {
            // Repli sur le défaut sans bloquer le démarrage.
        }
    }

    /**
     * Format compact d'un nombre de tokens pour l'affichage (tooltip) : « 90M » pour
     * 90 000 000, « 87.5K » pour 87 500, « 940 » en deça, « 1.2B » au milliard.
     *
     * @param value quantité de tokens
     * @return représentation compacte (1 décimale, « .0 » supprimé)
     */
    public static String formatCompact(long value) {
        long v = Math.max(0, value);
        if (v >= 1_000_000_000) {
            return trimOne(v / 1_000_000_000.0) + "B";
        }
        if (v >= 1_000_000) {
            return trimOne(v / 1_000_000.0) + "M";
        }
        if (v >= 1_000) {
            return trimOne(v / 1_000.0) + "K";
        }
        return Long.toString(v);
    }

    private static String trimOne(double value) {
        String out = String.format(java.util.Locale.ROOT, "%.1f", value);
        return out.endsWith(".0") ? out.substring(0, out.length() - 2) : out;
    }

    /** Lit le plafond depuis la préférence chiffrée « tsub ». */
    private static long readCap() {
        return SecurePreferences.loadDecryptedInt(
                PREF_KEY_TSUB, SecurePreferences.getEntr(), DEFAULT_TSUB);
    }
}