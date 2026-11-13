/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author eroot
 */
public class DataId {

    private static final java.security.SecureRandom SECURE_RANDOM = new java.security.SecureRandom();

    public static String generate() {
        UUID uuid = UUID.randomUUID();//UuidCreator.getTimeBased();
        return uuid.toString().replace("-", "").toLowerCase();
    }

    /**
     * Identifiant deterministe (32 hex) derive des composants fournis. Utilise
     * pour les operations qui doivent etre idempotentes : deux executions
     * concurrentes produisent le meme identifiant et la contrainte de cle
     * primaire empeche le doublon.
     */
    public static String deterministic(String... parts) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            if (parts != null) {
                for (String part : parts) {
                    digest.update(String.valueOf(part).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    digest.update((byte) 0x1f);
                }
            }
            byte[] hash = digest.digest();
            StringBuilder sb = new StringBuilder(32);
            for (int i = 0; i < 16; i++) {
                sb.append(String.format("%02x", hash[i]));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 indisponible", ex);
        }
    }

    public static int from(LocalDate date) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMM");
        if (date == null) {
            date = LocalDate.now();
        }
        String dae = date.format(format);
        int io = Integer.parseInt(dae);
        return io;
    }

    /**
     * Identifiant entier positif sur 31 bits. L'ancien generateur
     * ({@code Math.random() * ~3144}) ne produisait qu'environ 3 000 valeurs
     * distinctes : les ventes pouvaient entrer en collision sur la cle
     * primaire. Un tirage sur 31 bits rend la collision negligeable.
     */
    public static int generateInt() {
        return SECURE_RANDOM.nextInt(Integer.MAX_VALUE) + 1;
    }

    /**
     * Identifiant long positif sur 63 bits. Utilise pour les lignes dont
     * l'identifiant est numerique (lignes de vente, mouvements) : la plage
     * historique de {@code 1e13} rendait les collisions mesurables sur une
     * base de plusieurs millions de lignes.
     */
    public static long generateLong() {
        long candidate;
        do {
            candidate = SECURE_RANDOM.nextLong();
        } while (candidate <= 0);
        return candidate;
    }

    public static boolean isConcerned(String uid, int period) {
        if (uid == null) {
            return false;
        }
        String hex = uid.replace("-", "").trim();
        if (hex.length() < 32) {
            return false;
        }
        String id = hex.substring(0, 8) + "-" + hex.substring(8, 12) + "-"
                + hex.substring(12, 16) + "-" + hex.substring(16, 20) + "-"
                + hex.substring(20, 32);
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            return false;
        }
        if (uuid.version() == 1) {
            long uuidTimestamp = uuid.getMostSignificantBits();
            long millisecond = uuidTimestamp / 1000000;
            Instant instant = Instant.ofEpochMilli(millisecond);
            LocalDateTime datetime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            LocalDate uuidate = datetime.toLocalDate();
            LocalDate leo = LocalDate.now();
            long interv = ChronoUnit.HOURS.between(uuidate, leo);
            return interv < period;
        }
        return false;
    }

    public static boolean isConcerned(Date date, long period) {
        long uuidTimestamp = date.getTime() / 1000;
        long currentTimeMillis = System.currentTimeMillis() / 1000;
        long diff = currentTimeMillis - uuidTimestamp;
        return diff <= period;
    }

}
