package services.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.UUID;
import java.util.prefs.Preferences;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MySqlTlsTest {

    private Preferences prefs;

    @BeforeEach
    void setUp() {
        prefs = Preferences.userRoot().node("kazisafe-test-mysql-tls-" + UUID.randomUUID());
    }

    @AfterEach
    void tearDown() throws Exception {
        prefs.removeNode();
    }

    @Test
    void defaultsToRequired() {
        assumeTrue(System.getenv(MySqlTls.ENV_MODE) == null);
        prefs.remove("default_mysql_ssl_mode");
        prefs.remove("default_mysql_ssl");
        assertEquals("REQUIRED", MySqlTls.resolve(prefs));
    }

    @Test
    void acceptsWhitelistedModeCaseInsensitively() {
        assumeTrue(System.getenv(MySqlTls.ENV_MODE) == null);
        prefs.put("default_mysql_ssl_mode", "verify_identity");
        assertEquals("VERIFY_IDENTITY", MySqlTls.resolve(prefs));
    }

    @Test
    void invalidModeFallsBackToRequired() {
        assumeTrue(System.getenv(MySqlTls.ENV_MODE) == null);
        prefs.put("default_mysql_ssl_mode", "something-else");
        assertEquals("REQUIRED", MySqlTls.resolve(prefs));
    }

    @Test
    void legacyBooleanStillHonoured() {
        assumeTrue(System.getenv(MySqlTls.ENV_MODE) == null);
        prefs.remove("default_mysql_ssl_mode");
        prefs.put("default_mysql_ssl", "true");
        assertEquals("REQUIRED", MySqlTls.resolve(prefs));
        prefs.put("default_mysql_ssl", "false");
        assertEquals("DISABLED", MySqlTls.resolve(prefs));
    }

    @Test
    void normalizeRejectsNullAndUnknownValues() {
        assertEquals("REQUIRED", MySqlTls.normalize(null));
        assertEquals("REQUIRED", MySqlTls.normalize(" "));
        assertEquals("DISABLED", MySqlTls.normalize("disabled"));
        assertEquals("PREFERRED", MySqlTls.normalize("PREFERRED"));
    }
}
