package com.hephaitos.maintenance.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EncryptionUtils {

    // Clé AES de 16 octets (128 bits) pour le chiffrement/déchiffrement
    private static final String ALGORITHM = "AES";
    private static final byte[] KEY = "HephaitosKey2026".getBytes(StandardCharsets.UTF_8);

    public static String encrypt(String data) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(KEY, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du chiffrement : " + e.getMessage(), e);
        }
    }

    public static String decrypt(String encryptedData) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(KEY, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du déchiffrement : " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        String email = "tsunada25@gmail.com";
        String pass = "wqxmtkfcctttcpiq";
        System.out.println("Email chiffrement : " + encrypt(email));
        System.out.println("Pass chiffrement : " + encrypt(pass));
    }
}
