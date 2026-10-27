package com.ciphershield.crypto;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.ciphershield.exception.ValidationException;

/**
 * PBKDF2WithHmacSHA256 password-based key derivation.
 *
 * CO3: Exception handling for cryptographic operations.
 * CO4: String and data manipulation for key derivation.
 */
public class KeyDerivation {

    public static final int DEFAULT_ITERATIONS = 310000;
    public static final int DEFAULT_KEY_LENGTH = 256;
    private static final int MIN_ITERATIONS = 100000;
    private static final int MAX_ITERATIONS = 1000000;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    /**
     * Derives a cryptographic key from a password using PBKDF2WithHmacSHA256.
     *
     * @param password   The password to derive from (must not be null or empty).
     * @param salt       Random salt bytes (must not be null or empty).
     * @param iterations Number of PBKDF2 iterations (100,000 – 1,000,000).
     * @param keyLength  Desired key length in bits (e.g. 256).
     * @return A SecretKey suitable for AES encryption.
     * @throws ValidationException if parameters are invalid.
     */
    public static SecretKey deriveKey(char[] password, byte[] salt,
                                      int iterations, int keyLength)
            throws ValidationException {
        if (password == null || password.length == 0) {
            throw new ValidationException("Password cannot be null or empty.");
        }
        if (salt == null || salt.length == 0) {
            throw new ValidationException("Salt cannot be null or empty.");
        }
        if (iterations < MIN_ITERATIONS || iterations > MAX_ITERATIONS) {
            throw new ValidationException(
                "Iteration count must be between " + MIN_ITERATIONS +
                " and " + MAX_ITERATIONS + ". Got: " + iterations);
        }
        if (keyLength != 128 && keyLength != 192 && keyLength != 256) {
            throw new ValidationException("Key length must be 128, 192, or 256 bits.");
        }

        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            SecretKey tmp = factory.generateSecret(spec);
            spec.clearPassword();
            return new SecretKeySpec(tmp.getEncoded(), "AES");
        } catch (Exception e) {
            throw new ValidationException("Key derivation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Derives a key using default iterations (310,000) and key length (256 bits).
     */
    public static SecretKey deriveKey(char[] password, byte[] salt)
            throws ValidationException {
        return deriveKey(password, salt, DEFAULT_ITERATIONS, DEFAULT_KEY_LENGTH);
    }
}
