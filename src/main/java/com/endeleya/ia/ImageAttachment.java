package com.endeleya.ia;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Locale;
import javax.imageio.ImageIO;

/**
 * Utilitaire commun pour joindre des images aux modeles multimodaux.
 *
 * Les workflows (facture, vente, depense, image-produits) et le chat vision
 * passent tous ici afin que chaque image soit compressee sous ~700 ko avant
 * l'envoi. Sans cette compression, le serveur Ollama distant repond HTTP 413
 * (Request Entity Too Large) des que le corps de la requete depasse sa limite,
 * et l'extraction de la facture echoue avec "Je n'ai pas pu lire ...".
 */
public final class ImageAttachment {

    private static final long IMAGE_TARGET_BYTES = 700L * 1024L;
    private static final int MIN_IMAGE_DIMENSION = 800;

    private ImageAttachment() {
    }

    /** Vrai si le fichier semble etre une image d'apres son extension. */
    public static boolean isImage(File file) {
        String name = file == null ? "" : file.getName().toLowerCase(Locale.ROOT);
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")
                || name.endsWith(".gif") || name.endsWith(".webp") || name.endsWith(".bmp");
    }

    /**
     * Transforme un fichier image en contenu multimodal compresse. Renvoie null
     * pour les fichiers non-images ou illisibles afin que les workflows puissent
     * ignorer proprement les pieces jointes non exploitables.
     *
     * Le MIME est deduit des octets reellement envoyes (et non de l'extension du
     * fichier d'origine): quand la compression re-encode en JPEG une image
     * PNG/WebP/BMP trop lourde, l'annoter image/png ou image/webp avec des octets
     * JPEG fait echouer le decodeur d'image du serveur Ollama, qui ne voit alors
     * aucune image et l'extraction de facture echoue ("Je n'ai pas pu lire...").
     */
    public static Content imageContent(File file) {
        if (!isImage(file)) {
            return null;
        }
        try {
            byte[] bytes = compressToTarget(Files.readAllBytes(file.toPath()));
            return ImageContent.from(Image.builder()
                    .base64Data(Base64.getEncoder().encodeToString(bytes))
                    .mimeType(sniffMime(bytes))
                    .build());
        } catch (IOException | RuntimeException ex) {
            return null;
        }
    }

    /**
     * Detecte le type MIME reel depuis les octets de signature (magic bytes)
     * afin que le type annonce corresponde toujours aux donnees envoyees.
     */
    private static String sniffMime(byte[] bytes) {
        if (bytes != null && bytes.length >= 4) {
            if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8) {
                return "image/jpeg";
            }
            if ((bytes[0] & 0xFF) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G') {
                return "image/png";
            }
            if (bytes[0] == 'B' && bytes[1] == 'M') {
                return "image/bmp";
            }
            if (bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F') {
                return "image/webp";
            }
        }
        return "image/jpeg";
    }

    /**
     * Comprime les octets d'une image sous la taille cible en reduisant d'abord
     * la qualite JPEG a resolution pleine (lisibilite des ecrits), puis en
     * redimensionnant en dernier recours sans descendre sous 800 px. On renvoie
     * toujours le plus petit encodage obtenu: si la taille cible n'est pas
     * atteignable, on ne renvoie jamais une image plus lourde que ce que le
     * decodeur a pu produire (sinon le serveur Ollama repondrait HTTP 413).
     * En cas de decodage impossible, l'original est conserve plutot que de bloquer.
     */
    private static byte[] compressToTarget(byte[] original) throws IOException {
        if (original.length <= IMAGE_TARGET_BYTES) {
            return original;
        }
        BufferedImage source = ImageIO.read(new java.io.ByteArrayInputStream(original));
        if (source == null) {
            return original;
        }
        BufferedImage current = source;
        byte[] best = original;
        while (true) {
            for (float quality = 0.9f; quality >= 0.4f; quality -= 0.1f) {
                byte[] encoded = encodeJpeg(current, quality);
                if (encoded == null) {
                    continue;
                }
                if (encoded.length <= IMAGE_TARGET_BYTES) {
                    return encoded;
                }
                if (encoded.length < best.length) {
                    best = encoded;
                }
            }
            if (current.getWidth() <= MIN_IMAGE_DIMENSION
                    && current.getHeight() <= MIN_IMAGE_DIMENSION) {
                break;
            }
            current = resize(current,
                    Math.max(1, current.getWidth() * 3 / 4),
                    Math.max(1, current.getHeight() * 3 / 4));
        }
        return best;
    }

    private static BufferedImage resize(BufferedImage source, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }

    private static byte[] encodeJpeg(BufferedImage image, float quality) {
        try {
            java.util.Iterator<javax.imageio.ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
            if (!writers.hasNext()) {
                return null;
            }
            javax.imageio.ImageWriter writer = writers.next();
            javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (javax.imageio.stream.ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
                writer.setOutput(ios);
                writer.write(image);
            }
            writer.dispose();
            return out.toByteArray();
        } catch (IOException | RuntimeException ex) {
            return null;
        }
    }
}
