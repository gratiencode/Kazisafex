package com.endeleya.kazisafex.agent;

import java.util.prefs.Preferences;

/**
 * Enterprise identity ({@code eUid}) and engagement contract carried by the
 * orchestration runtime.
 *
 * <p>The application stores two identifiers at login in the {@link Preferences} node
 * of {@code tools.SyncEngine} (same convention as every controller in the app):
 * <ul>
 *   <li>{@code eUid}      — the connected enterprise ({@code LoginWebResult.getEntrepriseId()});</li>
 *   <li>{@code ucontract} — the user contract / engagement
 *       ({@code LoginWebResult.getUserContract()}).</li>
 * </ul>
 * The legacy layer derives the database/session exclusively from those preference
 * values, never from user text; this helper makes the same source of truth available
 * to the orchestration layer so the whole pipeline is attributed to one enterprise
 * and its engagement and can never be redirected by a user request.</p>
 *
 * @param enterpriseUid the {@code eUid} of the enterprise
 * @param engagementId  the engagement/contract identifier ({@code ucontract})
 */
public record EnterpriseIdentity(String enterpriseUid, String engagementId) {

    /** Preference key holding the enterprise UID. */
    public static final String KEY_EUID = "eUid";

    /** Preference key holding the user contract / engagement id. */
    public static final String KEY_ENGAGEMENT = "ucontract";

    /**
     * Canonical constructor that normalizes blank inputs.
     *
     * @param enterpriseUid the {@code eUid} of the enterprise
     * @param engagementId  the engagement/contract identifier
     */
    public EnterpriseIdentity {
        enterpriseUid = enterpriseUid == null ? "" : enterpriseUid.strip();
        engagementId = engagementId == null ? "" : engagementId.strip();
    }

    /**
     * Reads the identity stored in the application preferences.
     *
     * @return the current {@code (eUid, engagementId)} pair, with empty values when
     *         absent/unreadable
     */
    public static EnterpriseIdentity current() {
        try {
            Preferences pref = Preferences.userNodeForPackage(tools.SyncEngine.class);
            return new EnterpriseIdentity(pref.get(KEY_EUID, ""), pref.get(KEY_ENGAGEMENT, ""));
        } catch (Exception ex) {
            return new EnterpriseIdentity("", "");
        }
    }

    /** Whether at least one identity value is present. */
    public boolean present() {
        return !enterpriseUid.isBlank() || !engagementId.isBlank();
    }

    /** Compact attribution label, e.g. {@code euid#contract}. */
    public String attribution() {
        return enterpriseUid + "#" + engagementId;
    }
}