package tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.*;
import data.core.KazisafeServiceFactory;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de non-régression couvrant la migration Jackson.
 *
 * Vérifie :
 *  1. JsonUtil.jsonify() produit du JSON valide via Jackson
 *  2. Round-trip sérialisation ↔ désérialisation pour Vente et LigneVente
 *  3. Champs nuls ignorés (NON_NULL) — pas de null dans le JSON wire
 *  4. LocalDate et LocalDateTime au format ISO-8601
 *  5. Sécurité null dans NotificationHandler (les FK nulles ne lèvent pas de NPE)
 *  6. Null input dans jsonify() retourne un objet JSON vide (pas de NPE)
 */
@DisplayName("Jackson Serialization Migration — Non-Regression")
class SerializationMigrationTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = KazisafeServiceFactory.mapper();
    }

    // ── 1. JsonUtil.jsonify() produit un JsonObject valide ─────────────────

    @Test
    @DisplayName("jsonify(Category) retourne un JsonObject non-null avec le uid")
    void testJsonifyCategory() {
        Category cat = new Category();
        cat.setUid("cat-001");
        cat.setDescritption("Électronique");

        JsonObject json = JsonUtil.jsonify(cat);

        assertNotNull(json, "jsonify ne doit pas retourner null");
        assertEquals("cat-001", json.getString("uid"));
    }

    @Test
    @DisplayName("jsonify(Vente) sérialise le uid Integer correctement")
    void testJsonifyVente() {
        Vente vente = new Vente();
        vente.setUid(42);
        vente.setPayment("CASH");
        vente.setMontantUsd(150.0);

        JsonObject json = JsonUtil.jsonify(vente);

        assertNotNull(json);
        // uid Integer → Jackson le sérialise en nombre
        assertTrue(json.containsKey("uid"), "Le champ 'uid' doit être présent");
        assertEquals(42, json.getInt("uid"));
        assertEquals("CASH", json.getString("payment"));
    }

    @Test
    @DisplayName("jsonify(LigneVente) sérialise le uid Long correctement")
    void testJsonifyLigneVente() {
        LigneVente lv = new LigneVente();
        lv.setUid(999999L);
        lv.setQuantite(3.5);
        lv.setMontantUsd(50.0);

        JsonObject json = JsonUtil.jsonify(lv);

        assertNotNull(json);
        assertTrue(json.containsKey("uid"), "Le champ 'uid' doit être présent");
        assertEquals(999999L, json.getJsonNumber("uid").longValue());
    }

    @Test
    @DisplayName("jsonify(null) retourne un JsonObject vide sans exception")
    void testJsonifyNull() {
        assertDoesNotThrow(() -> {
            JsonObject json = JsonUtil.jsonify(null);
            assertNotNull(json);
            assertTrue(json.isEmpty(), "Un objet null doit produire un JsonObject vide {}");
        });
    }

    // ── 2. Round-trip Jackson sérialisation ↔ désérialisation ──────────────

    @Test
    @DisplayName("Round-trip Vente: serialize puis deserialize préserve les champs")
    void testVenteRoundTrip() throws Exception {
        Vente original = new Vente();
        original.setUid(7);
        original.setPayment("CREDIT");
        original.setMontantUsd(200.0);
        original.setMontantCdf(500000.0);
        original.setRegion("Goma");

        String json = mapper.writeValueAsString(original);
        assertNotNull(json);
        assertFalse(json.isBlank());

        Vente deserialized = mapper.readValue(json, Vente.class);
        assertNotNull(deserialized);
        assertEquals(7, deserialized.getUid());
        assertEquals("CREDIT", deserialized.getPayment());
        assertEquals(200.0, deserialized.getMontantUsd(), 0.001);
        assertEquals("Goma", deserialized.getRegion());
    }

    @Test
    @DisplayName("Round-trip LigneVente: serialize puis deserialize préserve uid Long")
    void testLigneVenteRoundTrip() throws Exception {
        LigneVente original = new LigneVente();
        original.setUid(123456789L);
        original.setQuantite(10.0);
        original.setMontantUsd(300.0);

        String json = mapper.writeValueAsString(original);
        LigneVente deserialized = mapper.readValue(json, LigneVente.class);

        assertNotNull(deserialized);
        assertEquals(123456789L, deserialized.getUid());
        assertEquals(10.0, deserialized.getQuantite(), 0.001);
    }

    @Test
    @DisplayName("Round-trip Compter: les FK inventaire, produit et mesure survivent au JSON downsync")
    void testCompterFkRoundTrip() throws Exception {
        Compter c = new Compter();
        c.setUid("comp-1");
        c.setInventaireId(new Inventaire("inv-1"));
        c.setProductId(new Produit("prod-1"));
        c.setMesureId(new Mesure("mes-1"));
        c.setQuantite(12.5);
        c.setNumlot("LOT-A");
        c.setRegion("Goma");

        String json = mapper.writeValueAsString(c);

        assertNotNull(json);
        assertTrue(json.contains("\"inventaireId\""));
        assertTrue(json.contains("\"productId\""));
        assertTrue(json.contains("\"mesureId\""));
        assertTrue(json.contains("\"uid\":\"inv-1\""), "Le JSON doit porter l'uid de l'inventaire");
        assertTrue(json.contains("\"uid\":\"prod-1\""), "Le JSON doit porter l'uid du produit");
        assertTrue(json.contains("\"uid\":\"mes-1\""), "Le JSON doit porter l'uid de la mesure");

        Compter back = mapper.readValue(json, Compter.class);
        assertNotNull(back);
        assertEquals("inv-1", back.getInventaireId().getUid());
        assertEquals("prod-1", back.getProductId().getUid());
        assertEquals("mes-1", back.getMesureId().getUid());
        assertEquals("LOT-A", back.getNumlot());
        assertEquals("Goma", back.getRegion());
    }

    @Test
    @DisplayName("jsonify(Compter) expose les FK inventaire, produit et mesure dans le JSON")
    void testCompterFkJsonify() {
        Compter c = new Compter();
        c.setUid("comp-2");
        c.setInventaireId(new Inventaire("inv-2"));
        c.setProductId(new Produit("prod-2"));
        c.setMesureId(new Mesure("mes-2"));

        JsonObject json = JsonUtil.jsonify(c);

        assertNotNull(json);
        JsonObject inv = json.getJsonObject("inventaireId");
        assertNotNull(inv, "inventaireId doit être présent dans le JSON");
        assertEquals("inv-2", inv.getString("uid"));
        assertEquals("prod-2", json.getJsonObject("productId").getString("uid"));
        assertEquals("mes-2", json.getJsonObject("mesureId").getString("uid"));
    }

    // ── 3. Les champs nuls sont exclus du JSON wire (NON_NULL) ─────────────

    @Test
    @DisplayName("Les champs null ne doivent pas apparaître dans le JSON (NON_NULL)")
    void testNullFieldsExcluded() throws Exception {
        Vente vente = new Vente();
        vente.setUid(1);
        // clientId, libelle, observation, etc. sont null

        String json = mapper.writeValueAsString(vente);

        // Avec NON_NULL, les champs null ne doivent pas être inclus
        assertFalse(json.contains("\"libelle\":null"), "Les champs null ne doivent pas être sérialisés");
        assertFalse(json.contains("\"observation\":null"), "Les champs null ne doivent pas être sérialisés");
    }

    // ── 4. Dates ISO-8601 ───────────────────────────────────────────────────

    @Test
    @DisplayName("LocalDateTime sérialisé en format ISO-8601 (pas de timestamp numérique)")
    void testLocalDateTimeISO8601() throws Exception {
        Vente vente = new Vente();
        vente.setUid(5);
        vente.setDateVente(LocalDateTime.of(2026, 8, 3, 10, 30, 0));

        String json = mapper.writeValueAsString(vente);

        // Doit contenir une chaîne de date et NON un timestamp numérique
        assertTrue(json.contains("2026-08-03"), "La date doit être au format ISO-8601");
        assertFalse(json.matches(".*\"dateVente\":\\d+.*"), "Pas de timestamp Unix pour les dates");
    }

    @Test
    @DisplayName("LocalDate sérialisé en format yyyy-MM-dd")
    void testLocalDateISO() throws Exception {
        Vente vente = new Vente();
        vente.setUid(6);
        vente.setEcheance(LocalDate.of(2026, 9, 15));

        String json = mapper.writeValueAsString(vente);
        assertTrue(json.contains("\"echeance\":\"2026-09-15\""), "LocalDate doit être au format yyyy-MM-dd");
    }

    // ── 5. Désérialisation flexible (champs inconnus ignorés) ───────────────

    @Test
    @DisplayName("Désérialisation tolère les champs inconnus (FAIL_ON_UNKNOWN_PROPERTIES=false)")
    void testUnknownPropertiesIgnored() {
        String jsonWithExtra = "{\"uid\":99,\"payment\":\"CASH\",\"unknownField\":\"someValue\",\"anotherExtra\":42}";

        assertDoesNotThrow(() -> {
            Vente vente = mapper.readValue(jsonWithExtra, Vente.class);
            assertNotNull(vente);
            assertEquals(99, vente.getUid());
            assertEquals("CASH", vente.getPayment());
        }, "Les champs inconnus ne doivent pas lever d'exception");
    }

    // ── 6. Cas limites du jsonify ───────────────────────────────────────────

    @Test
    @DisplayName("jsonify(Mesure) sérialise correctement sans FK nulles")
    void testJsonifyMesureNoFKs() {
        Mesure m = new Mesure();
        m.setUid("mes-001");
        m.setQuantContenu(1.0);
        m.setDescription("Unité de base");

        JsonObject json = JsonUtil.jsonify(m);

        assertNotNull(json);
        assertEquals("mes-001", json.getString("uid"));
        assertEquals("Unité de base", json.getString("description"));
    }

    @Test
    @DisplayName("jsonify(LigneVente avec FK nulles) ne lève pas de NPE")
    void testJsonifyLigneVenteNullFKs() {
        LigneVente lv = new LigneVente();
        lv.setUid(777L);
        // productId, mesureId, reference sont null — ne doit pas planter
        lv.setQuantite(5.0);

        assertDoesNotThrow(() -> {
            JsonObject json = JsonUtil.jsonify(lv);
            assertNotNull(json);
        }, "jsonify avec FK nulles ne doit pas lever de NPE");
    }

    // ── 7. Compatibilité wire format Quarkus ↔ JavaFX ──────────────────────

    @Test
    @DisplayName("Le JSON produit par mapper() est parsable en JsonObject Jakarta JSON-P")
    void testJacksonOutputParsableByJsonP() throws Exception {
        Vente vente = new Vente();
        vente.setUid(10);
        vente.setPayment("CASH");
        vente.setMontantUsd(75.5);

        // Ce que Jackson produit côté Quarkus/JavaFX
        String jacksonJson = mapper.writeValueAsString(vente);

        // Doit être parsable par Jakarta JSON-P (utilisé dans JsonUtil.objectify côté client)
        assertDoesNotThrow(() -> {
            try (jakarta.json.JsonReader reader = jakarta.json.Json.createReader(
                    new java.io.StringReader(jacksonJson))) {
                JsonObject obj = reader.readObject();
                assertNotNull(obj);
                assertEquals(10, obj.getInt("uid"));
            }
        }, "Le JSON Jackson doit être parsable par Jakarta JSON-P");
    }
}
