package services.utils;

import java.util.Locale;
import java.util.prefs.Preferences;

/**
 * Politique TLS commune aux connexions MySQL directes.
 *
 * <p>Le mode est resolu dans cet ordre : variable d'environnement
 * {@code KAZISAFE_DB_SSL_MODE}, preference {@code default_mysql_ssl_mode},
 * ancienne preference booleenne {@code default_mysql_ssl}, puis
 * {@link #DEFAULT_MODE}. Le mode par defaut est {@code REQUIRED} : le
 * transport est chiffre. La verification du certificat serveur s'active via
 * {@code VERIFY_CA} ou {@code VERIFY_IDENTITY} lorsque le serveur possede un
 * certificat valide (voir {@code docs/remediation/MANUAL_ACTIONS.md}).</p>
 *
 * <p>Aucun mode non chiffre n'est choisi silencieusement : {@code DISABLED}
 * doit etre explicitement configure.</p>
 */
public final class MySqlTls {

    public static final String ENV_MODE = "KAZISAFE_DB_SSL_MODE";
    public static final String DEFAULT_MODE = "REQUIRED";

    private MySqlTls() {
    }

    /** Mode TLS effectif pour les connexions MySQL de l'application. */
    public static String resolve(Preferences preferences) {
        String fromEnv = System.getenv(ENV_MODE);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return normalize(fromEnv);
        }
        if (preferences != null) {
            String stored = preferences.get("default_mysql_ssl_mode", null);
            if (stored != null && !stored.isBlank()) {
                return normalize(stored);
            }
            String legacyBoolean = preferences.get("default_mysql_ssl", null);
            if (legacyBoolean != null) {
                return Boolean.parseBoolean(legacyBoolean) ? "REQUIRED" : "DISABLED";
            }
        }
        return DEFAULT_MODE;
    }

    /** Normalise un mode JDBC MySQL ; toute valeur inconnue retombe sur REQUIRED. */
    public static String normalize(String value) {
        String normalized = value == null
                ? ""
                : value.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "DISABLED", "PREFERRED", "REQUIRED", "VERIFY_CA", "VERIFY_IDENTITY" -> normalized;
            default -> DEFAULT_MODE;
        };
    }
}
