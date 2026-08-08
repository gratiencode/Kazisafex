package com.endeleya.ia;

import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Random;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Valide ImageAttachment : le MIME annonce doit toujours correspondre aux octets
 * reellement envoyes (une PNG trop lourde est re-encodee en JPEG, donc annoncee
 * image/jpeg), et la compression ne doit jamais renvoyer d'image plus lourde.
 */
public class ImageAttachmentTest {

    @Test
    public void testGrandeImagePngReencodeeEnJpegAvecBonMime() throws Exception {
        File bigPng = Files.createTempFile("invoice-big", ".png").toFile();
        BufferedImage noise = new BufferedImage(1600, 1600, BufferedImage.TYPE_INT_RGB);
        Random random = new Random(42);
        for (int y = 0; y < noise.getHeight(); y++) {
            for (int x = 0; x < noise.getWidth(); x++) {
                noise.setRGB(x, y, random.nextInt(0xFFFFFF));
            }
        }
        ImageIO.write(noise, "png", bigPng);
        assertTrue(bigPng.length() > 700L * 1024L, "le PNG de test doit depasser 700 ko");

        Content content = ImageAttachment.imageContent(bigPng);
        assertNotNull(content);
        assertTrue(content instanceof ImageContent);
        ImageContent imageContent = (ImageContent) content;
        assertEquals("image/jpeg", imageContent.image().mimeType());

        byte[] bytes = Base64.getDecoder().decode(imageContent.image().base64Data());
        assertTrue(bytes.length <= 700L * 1024L);
        assertTrue((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8,
                "les octets envoyes doivent etre du JPEG (magic bytes)");
    }

    @Test
    public void testPetiteImageConserveeAvecSonMimeReel() throws Exception {
        File smallPng = Files.createTempFile("invoice-small", ".png").toFile();
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", smallPng);
        assertTrue(smallPng.length() <= 700L * 1024L);

        Content content = ImageAttachment.imageContent(smallPng);
        assertNotNull(content);
        ImageContent imageContent = (ImageContent) content;
        assertEquals("image/png", imageContent.image().mimeType());

        byte[] sent = Base64.getDecoder().decode(imageContent.image().base64Data());
        byte[] original = Files.readAllBytes(smallPng.toPath());
        assertArrayEquals(original, sent, "une image sous la cible doit etre envoyee telle quelle");
    }

    @Test
    public void testFichierNonImageIgnore() {
        assertNull(ImageAttachment.imageContent(new File("note.txt")));
    }

    @Test
    public void testIsImageAccepteGifCommeLaVue() {
        assertTrue(ImageAttachment.isImage(new File("facture.gif")));
        assertTrue(ImageAttachment.isImage(new File("facture.WEBP")));
        assertFalse(ImageAttachment.isImage(new File("facture.pdf")));
    }
}
