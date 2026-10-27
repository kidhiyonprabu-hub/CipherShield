package com.ciphershield.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import com.ciphershield.exception.EncryptionException;
import com.ciphershield.exception.DecryptionException;
import com.ciphershield.exception.ValidationException;

/**
 * Core cryptographic engine providing AES-256-GCM authenticated encryption.
 * Uses PBKDF2WithHmacSHA256 for password-based key derivation.
 *
 * CO3: Exception handling for crypto operations.
 * CO4: String manipulation (Base64 encoding/decoding).
 */
public class CryptoEngine {

    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int SALT_LENGTH = 16;
    private static final int NONCE_LENGTH = 12;
    private static final int DEFAULT_ITERATIONS = 310000;
    private static final int KEY_LENGTH = 256;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Encrypts plaintext using AES-256-GCM with a password-derived key.
     *
     * @param plaintext The text to encrypt (must not be null or empty).
     * @param password  The password for key derivation (must not be null or empty).
     * @return Base64-encoded string containing salt + nonce + ciphertext+tag.
     * @throws EncryptionException if encryption fails.
     * @throws ValidationException if input validation fails.
     */
    public String encryptText(String plaintext, char[] password)
            throws EncryptionException, ValidationException {
        validateTextInput(plaintext, password);
        try {
            byte[] plaintextBytes = plaintext.getBytes("UTF-8");
            byte[] encrypted = encryptBytes(plaintextBytes, password);
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new EncryptionException("Text encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypts Base64-encoded ciphertext using AES-256-GCM with a password-derived key.
     *
     * @param encryptedBase64 The Base64-encoded encrypted data.
     * @param password        The password used during encryption.
     * @return The original plaintext string.
     * @throws DecryptionException if decryption fails (wrong password, corrupted data).
     * @throws ValidationException if input validation fails.
     */
    public String decryptText(String encryptedBase64, char[] password)
            throws DecryptionException, ValidationException {
        if (encryptedBase64 == null || encryptedBase64.trim().isEmpty()) {
            throw new ValidationException("Encrypted text cannot be empty.");
        }
        if (password == null || password.length == 0) {
            throw new ValidationException("Password cannot be empty.");
        }
        try {
            byte[] encrypted = Base64.getDecoder().decode(encryptedBase64.trim());
            byte[] decrypted = decryptBytes(encrypted, password);
            return new String(decrypted, "UTF-8");
        } catch (ValidationException e) {
            throw e;
        } catch (DecryptionException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new DecryptionException("Invalid encrypted data format (not valid Base64).", e);
        } catch (Exception e) {
            throw new DecryptionException("Text decryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Encrypts raw bytes using AES-256-GCM.
     * Output format: [16-byte salt][12-byte nonce][ciphertext+GCM tag]
     *
     * @param data     The data to encrypt.
     * @param password The password for key derivation.
     * @return Encrypted bytes with salt and nonce prepended.
     * @throws EncryptionException if encryption fails.
     * @throws ValidationException if inputs are invalid.
     */
    public byte[] encryptBytes(byte[] data, char[] password)
            throws EncryptionException, ValidationException {
        if (data == null) {
            throw new ValidationException("Data to encrypt cannot be null.");
        }
        if (password == null || password.length == 0) {
            throw new ValidationException("Password cannot be empty.");
        }

        try {
            // Generate fresh random salt and nonce
            byte[] salt = new byte[SALT_LENGTH];
            byte[] nonce = new byte[NONCE_LENGTH];
            secureRandom.nextBytes(salt);
            secureRandom.nextBytes(nonce);

            // Derive key from password
            SecretKey key = KeyDerivation.deriveKey(password, salt, DEFAULT_ITERATIONS, KEY_LENGTH);
            SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");

            // Encrypt with AES-GCM
            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_BITS, nonce);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            byte[] ciphertext = cipher.doFinal(data);

            // Combine: salt + nonce + ciphertext (includes GCM tag)
            byte[] result = new byte[SALT_LENGTH + NONCE_LENGTH + ciphertext.length];
            System.arraycopy(salt, 0, result, 0, SALT_LENGTH);
            System.arraycopy(nonce, 0, result, SALT_LENGTH, NONCE_LENGTH);
            System.arraycopy(ciphertext, 0, result, SALT_LENGTH + NONCE_LENGTH, ciphertext.length);

            return result;
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new EncryptionException("Encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypts bytes that were encrypted with encryptBytes().
     * Input format: [16-byte salt][12-byte nonce][ciphertext+GCM tag]
     *
     * @param data     The encrypted data with salt and nonce prepended.
     * @param password The password used during encryption.
     * @return The original plaintext bytes.
     * @throws DecryptionException if decryption fails.
     * @throws ValidationException if inputs are invalid.
     */
    public byte[] decryptBytes(byte[] data, char[] password)
            throws DecryptionException, ValidationException {
        if (data == null) {
            throw new ValidationException("Encrypted data cannot be null.");
        }
        if (password == null || password.length == 0) {
            throw new ValidationException("Password cannot be empty.");
        }
        int minLength = SALT_LENGTH + NONCE_LENGTH + GCM_TAG_BITS / 8;
        if (data.length < minLength) {
            throw new DecryptionException("Encrypted data is too short or corrupted.");
        }

        try {
            // Extract salt, nonce, and ciphertext
            byte[] salt = Arrays.copyOfRange(data, 0, SALT_LENGTH);
            byte[] nonce = Arrays.copyOfRange(data, SALT_LENGTH, SALT_LENGTH + NONCE_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(data, SALT_LENGTH + NONCE_LENGTH, data.length);

            // Derive key from password
            SecretKey key = KeyDerivation.deriveKey(password, salt, DEFAULT_ITERATIONS, KEY_LENGTH);
            SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");

            // Decrypt with AES-GCM (authentication is verified automatically)
            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_BITS, nonce);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            return cipher.doFinal(ciphertext);

        } catch (javax.crypto.AEADBadTagException e) {
            throw new DecryptionException(
                "Authentication failed. Wrong password or data has been tampered with.", e);
        } catch (javax.crypto.BadPaddingException e) {
            throw new DecryptionException(
                "Decryption failed. Wrong password or corrupted data.", e);
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new DecryptionException("Decryption failed: " + e.getMessage(), e);
        }
    }

    private void validateTextInput(String text, char[] password) throws ValidationException {
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException("Input text cannot be empty.");
        }
        if (password == null || password.length == 0) {
            throw new ValidationException("Password cannot be empty.");
        }
    }
}
