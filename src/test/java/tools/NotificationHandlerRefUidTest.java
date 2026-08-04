package tools;

import data.Category;
import data.Mesure;
import data.Produit;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Non-régression : {@link NotificationHandler#refUid} ne doit jamais lever de
 * NPE sur une référence absente d'un payload downsync (ex. une Mesure sans
 * produitId), et doit retourner l'uid d'une référence valide.
 */
@DisplayName("NotificationHandler.refUid — extraction d'uid sans NPE")
class NotificationHandlerRefUidTest {

    @Test
    @DisplayName("Référence null -> null, sans NPE")
    void nullReferenceReturnsNull() {
        assertNull(NotificationHandler.refUid(null));
    }

    @Test
    @DisplayName("Entité avec uid -> uid")
    void validReferenceReturnsUid() {
        assertEquals("prod-1", NotificationHandler.refUid(new Produit("prod-1")));
        assertEquals("mes-1", NotificationHandler.refUid(new Mesure("mes-1")));
        assertEquals("cat-1", NotificationHandler.refUid(new Category("cat-1")));
    }

    @Test
    @DisplayName("Objet sans getUid -> null, sans NPE")
    void objectWithoutGetUidReturnsNull() {
        assertNull(NotificationHandler.refUid(List.of("a")));
    }
}
