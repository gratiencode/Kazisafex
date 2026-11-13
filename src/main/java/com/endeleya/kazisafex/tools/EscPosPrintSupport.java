package com.endeleya.kazisafex.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import javax.print.Doc;
import tools.SyncLogger;

/**
 * Utilitaires partages par les PrintService ESC/POS maison (Bluetooth et Serie).
 *
 * <p>Deux problemes distincts sont traites ici :
 * <ul>
 *   <li><b>Extraction du contenu d'un {@link Doc}</b> : escpos-coffee 4.1.0
 *       construit son job avec {@code new SimpleDoc(pipedInputStream,
 *       DocFlavor.INPUT_STREAM.AUTOSENSE, null)}. Selon le {@code PrintService},
 *       le flux ESC/POS se recupere via {@link Doc#getPrintData()},
 *       {@link Doc#getStreamForBytes()} ou {@link Doc#getReaderForText()}.
 *       Si on ne lit que {@code getPrintData()} et qu'il renvoie {@code null},
 *       l'imprimante ne recoit aucun octet et sort du papier vide.</li>
 *   <li><b>Envoi par blocs</b> : les imprimantes thermiques d'entree de gamme
 *       ont un tampon de reception de 512 a 4096 octets. Envoyer le ticket en un
 *       seul bloc (33 Ko avec le logo par defaut) fait deborder le tampon :
 *       l'imprimante jette des octets, se desynchronise du flux ESC/POS et
 *       n'imprime plus rien. D'ou l'envoi par petits blocs avec une pause.</li>
 * </ul>
 */
public final class EscPosPrintSupport {

    private static final int READ_BUFFER = 8192;

    /** Taille d'un bloc envoye a l'imprimante, volontairement petite. */
    public static final int CHUNK_SIZE = 256;

    /** Pause entre deux blocs, le temps que l'imprimante vide son tampon. */
    public static final long CHUNK_DELAY_MS = 12L;

    /** Pause apres le dernier bloc, avant la coupe du papier. */
    public static final long CUT_DELAY_MS = 400L;

    /**
     * Taille maximale du raster du logo, en octets ESC/POS. Au-dela, le tampon
     * de reception d'une imprimante thermique d'entree de gamme (512 a 4096
     * octets) deborde : l'imprimante perd des octets, se desynchronise du flux
     * ESC/POS et le ticket sort vierge.
     */
    public static final int MAX_LOGO_RASTER_BYTES = 8192;

    /** Table CP437 (demi-haute 0x80-0xFF) utilisee par le ticket. */
    private static final String CP437_HIGH
            = "ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜ¢£¥₧ƒ"
            + "áíóúñÑªº¿⌐¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐"
            + "└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀"
            + "αßΓπΣσµτΦΘΩδ∞φε∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■ ";

    private static final Charset ASCII = Charset.forName("US-ASCII");

    private EscPosPrintSupport() {
    }

    /**
     * Recupere l'integralite des octets ESC/POS contenus dans un {@link Doc}.
     *
     * @return les octets a envoyer, jamais {@code null}
     */
    public static byte[] docToBytes(Doc doc) throws IOException {
        if (doc == null) {
            return new byte[0];
        }
        byte[] fromPrintData = readDocObject(doc, true);
        if (fromPrintData != null) {
            return fromPrintData;
        }
        byte[] fromStream = readDocObject(doc, false);
        if (fromStream != null) {
            return fromStream;
        }
        return readDocText(doc);
    }

    private static byte[] readDocObject(Doc doc, boolean viaPrintData) throws IOException {
        Object data;
        try {
            if (viaPrintData) {
                data = doc.getPrintData();
            } else {
                data = doc.getStreamForBytes();
            }
        } catch (IOException | RuntimeException e) {
            SyncLogger.getInstance().log(e, "EscPosPrintSupport.readDocObject");
            return null;
        }
        if (data instanceof byte[]) {
            return ((byte[]) data).clone();
        }
        if (data instanceof InputStream) {
            return drain((InputStream) data);
        }
        if (data instanceof char[]) {
            return new String((char[]) data).getBytes(ASCII);
        }
        if (data instanceof String) {
            return ((String) data).getBytes(ASCII);
        }
        return null;
    }

    private static byte[] readDocText(Doc doc) {
        Reader reader = null;
        try {
            reader = doc.getReaderForText();
        } catch (IOException | RuntimeException e) {
            SyncLogger.getInstance().log(e, "EscPosPrintSupport.readDocText");
        }
        if (reader == null) {
            return new byte[0];
        }
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[1024];
        try {
            int len;
            while ((len = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, len);
            }
        } catch (IOException e) {
            SyncLogger.getInstance().log(e, "EscPosPrintSupport.readDocText");
        } finally {
            try {
                reader.close();
            } catch (IOException ignored) {
            }
        }
        return sb.toString().getBytes(ASCII);
    }

    /**
     * Lit un flux jusqu'a epuisement. Un flux casse (le producteur s'est arrete
     * prematurely) ne doit pas faire perdre les octets deja recus.
     */
    private static byte[] drain(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[READ_BUFFER];
        try {
            int len;
            while ((len = in.read(buffer)) != -1) {
                if (len > 0) {
                    bos.write(buffer, 0, len);
                }
            }
        } catch (IOException e) {
            if (bos.size() == 0) {
                throw e;
            }
            SyncLogger.getInstance().logMessage("EscPosPrintSupport.drain",
                    "Flux ESC/POS interrompu apres " + bos.size() + " octets : " + e);
        }
        return bos.toByteArray();
    }

    /**
     * Ecrit les donnees par petits blocs en pausing entre chaque, pour ne pas
     * deborder le tampon de l'imprimante.
     *
     * @return nombre d'octets reellement ecrits
     */
    public static long writeChunked(OutputStream out, byte[] data) throws IOException {
        int offset = 0;
        while (offset < data.length) {
            int len = Math.min(CHUNK_SIZE, data.length - offset);
            out.write(data, offset, len);
            out.flush();
            offset += len;
            if (offset < data.length) {
                pause(CHUNK_DELAY_MS);
            }
        }
        return offset;
    }

    public static void pause(long millis) {
        if (millis <= 0L) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Nombre de points imprimables selon la largeur du ticket en caracteres
     * (58mm = 384, 72mm = 512, 80mm = 576). Un logo plus large est rogne par
     * l'imprimante et fait deborder son tampon interne.
     */
    public static int printableDotsOf(int ticketWidth) {
        if (ticketWidth <= 32) {
            return 384;
        }
        if (ticketWidth <= 42) {
            return 512;
        }
        return 576;
    }

    /**
     * Reduit le logo a la largeur imprimable et a une taille de raster
     * raisonnable, en conservant ses proportions. Le raster ESC/POS d'un logo
     * 512x512 pese 32 Ko, ce qui sature le tampon d'une imprimante thermique
     * d'entree de gamme et fait sortir du papier vierge.
     */
    public static BufferedImage scaleLogoToPaper(BufferedImage source, int printableDots) {
        int width = source.getWidth();
        int height = source.getHeight();
        if (width <= 0 || height <= 0) {
            return source;
        }
        int maxWidth = Math.min(width, Math.max(1, printableDots));
        int bytesPerRow = (maxWidth + 7) / 8;
        int maxHeight = Math.max(1, Math.min(height, MAX_LOGO_RASTER_BYTES / Math.max(1, bytesPerRow)));
        if (maxWidth == width && maxHeight == height) {
            return source;
        }
        double factor = Math.min((double) maxWidth / width, (double) maxHeight / height);
        int newWidth = Math.max(1, (int) Math.round(width * factor));
        int newHeight = Math.max(1, (int) Math.round(height * factor));
        BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaled.createGraphics();
        try {
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, newWidth, newHeight);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g2d.drawImage(source, 0, 0, newWidth, newHeight, null);
        } finally {
            g2d.dispose();
        }
        return scaled;
    }

    /**
     * Ecrit le journal complet d'un envoi a l'imprimante : volume, duree et
     * contenu lisible du flux ESC/POS.
     */
    public static void logPayload(String context, String printerName, byte[] data, long sent, long elapsedMs) {
        int expected = data == null ? 0 : data.length;
        StringBuilder sb = new StringBuilder();
        sb.append("Impression ESC/POS vers ").append(printerName).append('\n');
        sb.append("Octets recus du job : ").append(expected).append('\n');
        sb.append("Octets envoyes a l'imprimante : ").append(sent);
        if (expected > 0 && sent != expected) {
            sb.append("  *** INCOMPLET, ").append(expected - sent).append(" octets perdus ***");
        }
        sb.append('\n');
        sb.append("Duree : ").append(elapsedMs).append(" ms\n");
        sb.append("--- Contenu imprime ---\n");
        sb.append(describe(data));
        sb.append("\n--- Fin du contenu imprime ---");
        SyncLogger.getInstance().logMessage(context, sb.toString());
    }

    /**
     * Traduit un flux ESC/POS en texte lisible : les commandes de mise en forme
     * sont remplacees par des marqueurs, le contenu texte est conserve tel quel
     * (decode en CP437) afin que le journal ressemble au papier sorti.
     */
    public static String describe(byte[] data) {
        if (data == null || data.length == 0) {
            return "(flux vide : rien n'a ete envoye a l'imprimante)";
        }
        StringBuilder out = new StringBuilder();
        int i = 0;
        int printable = 0;
        while (i < data.length) {
            int b = data[i] & 0xFF;
            if (b == 0x1D) {
                i = describeGs(data, i, out);
            } else if (b == 0x1B) {
                i = describeEsc(data, i, out);
            } else if (b == 0x0A) {
                out.append('\n');
                i++;
            } else if (b == 0x0D) {
                i++;
            } else if (b == 0x09) {
                out.append("    ");
                i++;
            } else if (b >= 0x20 && b != 0x7F) {
                out.append(decode(b));
                printable++;
                i++;
            } else {
                out.append(String.format("<%02X>", b));
                i++;
            }
        }
        if (printable == 0) {
            out.append("(aucun caractere imprimable dans le flux)\n");
        }
        return out.toString();
    }

    private static int describeEsc(byte[] data, int i, StringBuilder out) {
        if (i + 1 >= data.length) {
            out.append("<ESC tronque>");
            return data.length;
        }
        int cmd = data[i + 1] & 0xFF;
        switch (cmd) {
            case 0x40:
                out.append("[init]");
                return i + 2;
            case 0x45:
                return escOneParam(data, i, out, "gras", (data[i + 2] & 1) != 0 ? "on" : "off");
            case 0x2D:
                return escOneParam(data, i, out, "souligne", (data[i + 2] & 0xFF) == 48 ? "off" : "on");
            case 0x61:
                return escOneParam(data, i, out, "alignement", alignName(data[i + 2] & 0xFF));
            case 0x21:
                return escOneParam(data, i, out, "mode impression", printModeName(data[i + 2] & 0xFF));
            case 0x74:
                return escOneParam(data, i, out, "table de caracteres", (data[i + 2] & 0xFF) == 0 ? "CP437" : String.valueOf(data[i + 2] & 0xFF));
            case 0x64: {
                int n = data[i + 2] & 0xFF;
                out.append("[saut de ligne x").append(n).append("]");
                return i + 3;
            }
            case 0x4D:
                return escOneParam(data, i, out, "mode impression", printModeName(data[i + 2] & 0xFF));
            case 0x41:
            case 0x42:
            case 0x32:
            case 0x33:
                out.append("[police ").append((char) cmd).append(']');
                return i + 2;
            case 0x2A:
                return i + 4;
            default:
                out.append(String.format("<ESC %02X>", cmd));
                return i + 2;
        }
    }

    private static int escOneParam(byte[] data, int i, StringBuilder out, String label, String value) {
        if (i + 2 >= data.length) {
            out.append("<ESC ").append(label).append(" tronque>");
            return data.length;
        }
        out.append('[').append(label).append(' ').append(value).append(']');
        return i + 3;
    }

    private static String alignName(int value) {
        switch (value) {
            case 0:
                return "gauche";
            case 1:
                return "centre";
            case 2:
                return "droite";
            default:
                return String.valueOf(value);
        }
    }

    private static String printModeName(int value) {
        switch (value) {
            case 48:
                return "standard";
            case 49:
                return "police A compressee";
            case 50:
                return "police B compressee";
            default:
                return String.valueOf(value);
        }
    }

    private static int describeGs(byte[] data, int i, StringBuilder out) {
        if (i + 1 >= data.length) {
            out.append("<GS tronque>");
            return data.length;
        }
        int cmd = data[i + 1] & 0xFF;
        switch (cmd) {
            case 0x21:
                if (i + 2 < data.length) {
                    out.append("[taille ").append(data[i + 2] & 0xFF).append(']');
                    return i + 3;
                }
                return i + 2;
            case 0x42:
                if (i + 2 < data.length) {
                    out.append("[hauteur de caractere ").append(data[i + 2] & 0xFF).append(']');
                    return i + 3;
                }
                return i + 2;
            case 0x56: {
                int mode = (i + 2 < data.length) ? (data[i + 2] & 0xFF) : 0;
                out.append((mode == 0 || mode == 48) ? "[COUPE]" : "[coupe partielle]");
                return i + 3;
            }
            case 0x28: {
                // GS ( k : blocs image / QR code, longueur donnee par pL/pH.
                if (i + 5 < data.length && (data[i + 2] & 0xFF) == 0x6B) {
                    int len = (data[i + 3] & 0xFF) | ((data[i + 4] & 0xFF) << 8);
                    out.append("[image/QR code ").append(len).append(" octets]");
                    return Math.min(data.length, i + 5 + len);
                }
                out.append("[bloc image]");
                return i + 2;
            }
            case 0x76: {
                // GS v 0 : image matricielle, entete de 8 octets puis les donnees.
                if (i + 9 < data.length && (data[i + 2] & 0xFF) == 0x30) {
                    int widthBytes = (data[i + 4] & 0xFF) | ((data[i + 5] & 0xFF) << 8);
                    int height = (data[i + 6] & 0xFF) | ((data[i + 7] & 0xFF) << 8);
                    int len = widthBytes * height;
                    out.append("[LOGO matriciel ").append(widthBytes * 8).append('x')
                            .append(height).append(" pixels, ").append(len).append(" octets]");
                    return Math.min(data.length, i + 8 + len);
                }
                out.append("[image matricielle]");
                return i + 2;
            }
            default:
                out.append(String.format("<GS %02X>", cmd));
                return i + 2;
        }
    }

    private static char decode(int b) {
        if (b < 0x80) {
            return (char) b;
        }
        return CP437_HIGH.charAt(b - 0x80);
    }
}
