/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services.utils;

/**
 *
 * @author endeleya
 */
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.prefs.Preferences;
import tools.SyncEngine;

public final class SecurePreferences {

    private static final String PREF_KEY_NAME = "enc_db_key"; // emplacement dans Preferences

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SALT_LEN = 16;     // bytes
    private static final int IV_LEN = 12;       // bytes for GCM
    private static final int PBKDF2_ITER = 200_000; // itérations PBKDF2 (ajuste selon perf)
    private static final int KEY_LEN = 256;     // bits
    private static final int GCM_TAG_LEN = 128; // bits

    private SecurePreferences() {
    }

    // Dérive une clé AES depuis une passphrase et un salt
    private static SecretKey deriveKey(char[] passphrase, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(passphrase, salt, PBKDF2_ITER, KEY_LEN);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = skf.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static String getEntr() {
        return Preferences.userNodeForPackage(SyncEngine.class).get("eUid", null);
    }

   
    // Chiffre la valeur et la sauvegarde dans Preferences
    public static void storeEncryptedValue(String masterPassphrase, String plainValue) throws Exception {
        byte[] salt = new byte[SALT_LEN];
        RANDOM.nextBytes(salt);

        SecretKey aesKey = deriveKey(masterPassphrase.toCharArray(), salt);

        byte[] iv = new byte[IV_LEN];
        RANDOM.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LEN, iv);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec);

        byte[] cipherText = cipher.doFinal(plainValue.getBytes("UTF-8"));

        // Concatener salt|iv|ciphertext et encoder en Base64
        byte[] out = new byte[salt.length + iv.length + cipherText.length];
        System.arraycopy(salt, 0, out, 0, salt.length);
        System.arraycopy(iv, 0, out, salt.length, iv.length);
        System.arraycopy(cipherText, 0, out, salt.length + iv.length, cipherText.length);

        String b64 = Base64.getEncoder().encodeToString(out);
        Preferences.userNodeForPackage(SyncEngine.class).put(PREF_KEY_NAME, b64);
        System.out.println("CHIF-" + b64);
    }

    // Récupère la valeur chiffrée depuis Preferences et la déchiffre
    public static String loadDecryptedValue(String masterPassphrase) throws Exception {
        String b64 = Preferences.userNodeForPackage(SyncEngine.class).get(PREF_KEY_NAME, null);
        if (b64 == null) {
            return null;
        }

        byte[] all = Base64.getDecoder().decode(b64);
        if (all.length < SALT_LEN + IV_LEN + 1) {
            throw new IllegalStateException("Données corrompues dans Preferences");
        }

        byte[] salt = new byte[SALT_LEN];
        byte[] iv = new byte[IV_LEN];
        byte[] cipherText = new byte[all.length - SALT_LEN - IV_LEN];

        System.arraycopy(all, 0, salt, 0, SALT_LEN);
        System.arraycopy(all, SALT_LEN, iv, 0, IV_LEN);
        System.arraycopy(all, SALT_LEN + IV_LEN, cipherText, 0, cipherText.length);

        SecretKey aesKey = deriveKey(masterPassphrase.toCharArray(), salt);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LEN, iv);
        cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec);

        byte[] plain = cipher.doFinal(cipherText);
        return new String(plain, "UTF-8");
    }

    public static boolean hasStoredValue() {
        return Preferences.userNodeForPackage(SyncEngine.class).get(PREF_KEY_NAME, null) != null;
    }
}
