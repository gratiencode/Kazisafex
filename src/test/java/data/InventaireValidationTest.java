package data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class InventaireValidationTest {

    @Test
    void validatorFieldsAreLocalOnlyAndNotSentToServer() throws Exception {
        Inventaire inv = new Inventaire();
        inv.setUid("inv-1");
        inv.setCodeInventaire("INV-1");
        inv.setEtat("Terminé");
        inv.setValidePar("Opérateur Test");
        LocalDateTime at = LocalDateTime.of(2026, 11, 11, 12, 30);
        inv.setValideLe(at);

        assertEquals("Opérateur Test", inv.getValidePar());
        assertEquals(at, inv.getValideLe());

        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        String json = mapper.writeValueAsString(inv);
        assertFalse(json.contains("validePar"), "champ local seulement, contrat API préservé");
        assertFalse(json.contains("valideLe"), "champ local seulement, contrat API préservé");
        assertFalse(json.contains("valide_par"));
    }
}
