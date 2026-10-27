package com.ciphershield.crypto;

import com.ciphershield.exception.CipherShieldException;
import com.ciphershield.exception.ValidationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * SHA-256 file fingerprinting and integrity verification engine.
 * CO4: File I/O and data hashing.
 */
public class HashEngine {

    public interface HashProgressCallback {
        void onProgress(int percentage);
        boolean isCancelled();
    }

    /**
     * Calculates the SHA-256 fingerprint of a file in 64KB chunks.
     */
    public static String calculateFileSHA256(File file, HashProgressCallback callback) throws CipherShieldException {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new ValidationException("File does not exist or is not readable.");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            long totalBytes = file.length();
            long readBytes = 0;

            byte[] buffer = new byte[65536]; // 64KB buffer
            try (InputStream is = new FileInputStream(file)) {
                int n;
                while ((n = is.read(buffer)) != -1) {
                    if (callback != null && callback.isCancelled()) {
                        throw new CipherShieldException("Hash calculation was cancelled.");
                    }
                    digest.update(buffer, 0, n);
                    readBytes += n;
                    if (callback != null && totalBytes > 0) {
                        int pct = (int) ((readBytes * 100) / totalBytes);
                        callback.onProgress(pct);
                    }
                }
            }

            byte[] hashBytes = digest.digest();
            return bytesToHex(hashBytes);

        } catch (CipherShieldException e) {
            throw e;
        } catch (Exception e) {
            throw new CipherShieldException("Failed to calculate SHA-256 hash: " + e.getMessage(), e);
        }
    }

    public static String calculateStringSHA256(String text) throws CipherShieldException {
        if (text == null) {
            throw new ValidationException("Input string cannot be null.");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes("UTF-8"));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new CipherShieldException("Hashing failed: " + e.getMessage(), e);
        }
    }

    public static boolean compareHashes(String hash1, String hash2) {
        if (hash1 == null || hash2 == null) return false;
        String h1 = hash1.trim().toLowerCase();
        String h2 = hash2.trim().toLowerCase();
        return h1.equals(h2);
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
