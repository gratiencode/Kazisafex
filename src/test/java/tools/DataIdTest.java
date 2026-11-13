package tools;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DataId — identifiants numériques sans collision pratique")
class DataIdTest {

    @Test
    @DisplayName("generateInt reste positif et couvre bien plus que 3 000 valeurs")
    void generateIntHasWidePositiveRange() {
        Set<Integer> values = new HashSet<>();
        int max = 0;
        for (int i = 0; i < 10_000; i++) {
            int id = DataId.generateInt();
            assertTrue(id > 0, "un identifiant de vente doit rester strictement positif");
            max = Math.max(max, id);
            values.add(id);
        }
        assertTrue(values.size() > 9_990,
                "10 000 tirages sur 31 bits ne doivent pas retomber dans la plage de ~3 144 valeurs historique");
        assertTrue(max > 1_000_000,
                "l'ancien générateur borné à ~3 144 valeurs est remplacé par une plage 31 bits");
    }

    @Test
    @DisplayName("generateLong reste strictement positif sur un large échantillon")
    void generateLongIsStrictlyPositive() {
        for (int i = 0; i < 5_000; i++) {
            assertTrue(DataId.generateLong() > 0L);
        }
    }

    @Test
    @DisplayName("isConcerned ignore les uid nuls, courts ou malformes au lieu de lever")
    void isConcernedIsDefensive() {
        org.junit.jupiter.api.Assertions.assertFalse(DataId.isConcerned((String) null, 24));
        org.junit.jupiter.api.Assertions.assertFalse(DataId.isConcerned("abc", 24));
        org.junit.jupiter.api.Assertions.assertFalse(DataId.isConcerned("z".repeat(32), 24));
        org.junit.jupiter.api.Assertions.assertFalse(
                DataId.isConcerned("00000000000000000000000000000000", 24),
                "un UUID de version 4 n'est pas horodate");
    }

    @Test
    @DisplayName("deterministic produit le meme uid 32 hex pour les memes composants")
    void deterministicIsStableAndDistinct() {
        String a = DataId.deterministic("compter-zero", "inv-1", "prod-1", "lot-1", "Goma");
        String b = DataId.deterministic("compter-zero", "inv-1", "prod-1", "lot-1", "Goma");
        String c = DataId.deterministic("compter-zero", "inv-1", "prod-1", "lot-2", "Goma");
        org.junit.jupiter.api.Assertions.assertEquals(a, b,
                "une cloture rejouee doit viser exactement la meme ligne");
        org.junit.jupiter.api.Assertions.assertNotEquals(a, c);
        assertTrue(a.matches("[0-9a-f]{32}"), "format attendu : 32 caracteres hexadecimaux");
    }
}
