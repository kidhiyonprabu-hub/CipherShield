package com.ciphershield.crypto;

import com.ciphershield.exception.DecryptionException;
import com.ciphershield.exception.EncryptionException;
import com.ciphershield.exception.FileFormatException;
import com.ciphershield.exception.ValidationException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.FileAlreadyExistsException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Handles secure file encryption and decryption using AES-256-GCM.
 * Enforces file format validation, overwrite protection, and atomic output writing.
 *
 * Header Specification:
 * - Magic Bytes: "CSHD" (4 bytes: 0x43, 0x53, 0x48, 0x44)
 * - Version: 2 bytes (short = 1)
 * - KDF Iterations: 4 bytes (int = 310,000)
 * - Salt Length: 1 byte (byte = 16)
 * - Salt: 16 bytes
 * - Nonce Length: 1 byte (byte = 12)
 * - Nonce: 12 bytes
 * - Authenticated Payload: Ciphertext + 16-byte GCM Tag
 */
public class SecureFileHandler {

    public static final byte[] MAGIC_HEADER = new byte[]{'C', 'S', 'H', 'D'};
    public static final short CURRENT_VERSION = 1;
    public static final String ENCRYPTED_EXTENSION = ".senc";
    public static final long MAX_FILE_SIZE = 500L * 1024 * 1024; // 500 MB limit
    private static final int GCM_TAG_BITS = 128;
    private static final int SALT_LENGTH = 16;
    private static final int NONCE_LENGTH = 12;
    private static final int DEFAULT_ITERATIONS = 310000;
    private static final int MIN_ITERATIONS = 100000;
    private static final int MAX_ITERATIONS = 1000000;

    public interface ProgressCallback {
        void onProgress(int percentage, String status);
        boolean isCancelled();
    }

    public static File getAvailableOutputFile(File outputDirectory, String requestedName) {
        File candidate = new File(outputDirectory, requestedName);
        if (!candidate.exists()) {
            return candidate;
        }

        int extensionIndex = requestedName.lastIndexOf('.');
        String baseName = extensionIndex > 0 ? requestedName.substring(0, extensionIndex) : requestedName;
        String extension = extensionIndex > 0 ? requestedName.substring(extensionIndex) : "";
        int suffix = 1;
        do {
            candidate = new File(outputDirectory, baseName + " (" + suffix++ + ")" + extension);
        } while (candidate.exists());
        return candidate;
    }

    /**
     * Encrypts a file using AES-256-GCM and writes to outputFile.
     */
    public static void encryptFile(File inputFile, File outputFile, char[] password, ProgressCallback callback)
            throws EncryptionException, ValidationException {
        if (inputFile == null || !inputFile.exists() || !inputFile.isFile()) {
            throw new ValidationException("Input file does not exist or is not a valid file.");
        }
        if (inputFile.length() > MAX_FILE_SIZE) {
            throw new ValidationException("File size exceeds maximum supported limit of 500 MB.");
        }
        if (outputFile == null) {
            throw new ValidationException("Output file path must be specified.");
        }
        if (outputFile.exists()) {
            throw new ValidationException("Output file already exists: " + outputFile.getName());
        }
        if (password == null || password.length == 0) {
            throw new ValidationException("Password cannot be empty.");
        }

        File tempFile = new File(outputFile.getAbsolutePath() + ".tmp." + System.currentTimeMillis());
        try {
            if (callback != null) callback.onProgress(5, "Generating cryptographic parameters...");

            SecureRandom secureRandom = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            byte[] nonce = new byte[NONCE_LENGTH];
            secureRandom.nextBytes(salt);
            secureRandom.nextBytes(nonce);

            if (callback != null && callback.isCancelled()) {
                throw new EncryptionException("Operation was cancelled by user.");
            }

            if (callback != null) callback.onProgress(15, "Deriving encryption key via PBKDF2 (310,000 rounds)...");
            SecretKey key = KeyDerivation.deriveKey(password, salt, DEFAULT_ITERATIONS, 256);
            SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");

            if (callback != null && callback.isCancelled()) {
                throw new EncryptionException("Operation was cancelled by user.");
            }

            if (callback != null) callback.onProgress(35, "Reading input file into memory...");
            byte[] plaintextBytes = new byte[(int) inputFile.length()];
            try (FileInputStream fis = new FileInputStream(inputFile);
                 DataInputStream dis = new DataInputStream(fis)) {
                dis.readFully(plaintextBytes);
            }

            if (callback != null && callback.isCancelled()) {
                throw new EncryptionException("Operation was cancelled by user.");
            }

            if (callback != null) callback.onProgress(50, "Encrypting with AES-256-GCM...");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_BITS, nonce);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            byte[] ciphertext = cipher.doFinal(plaintextBytes);

            if (callback != null && callback.isCancelled()) {
                throw new EncryptionException("Operation was cancelled by user.");
            }

            if (callback != null) callback.onProgress(80, "Writing encrypted container...");
            try (FileOutputStream fos = new FileOutputStream(tempFile);
                 DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos))) {
                // Header: Magic (4 bytes)
                dos.write(MAGIC_HEADER);
                // Version (2 bytes)
                dos.writeShort(CURRENT_VERSION);
                // Iterations (4 bytes)
                dos.writeInt(DEFAULT_ITERATIONS);
                // Salt length (1 byte) + Salt
                dos.writeByte(salt.length);
                dos.write(salt);
                // Nonce length (1 byte) + Nonce
                dos.writeByte(nonce.length);
                dos.write(nonce);
                // Ciphertext + Auth Tag
                dos.write(ciphertext);
                dos.flush();
            }

            if (callback != null && callback.isCancelled()) {
                throw new EncryptionException("Operation was cancelled by user.");
            }

            if (callback != null) callback.onProgress(95, "Finalizing output file...");
            // Atomic rename
            commitOutputFile(tempFile, outputFile);

            if (callback != null) callback.onProgress(100, "File encryption complete!");

        } catch (ValidationException e) {
            deleteQuietly(tempFile);
            throw e;
        } catch (EncryptionException e) {
            deleteQuietly(tempFile);
            throw e;
        } catch (Exception e) {
            deleteQuietly(tempFile);
            throw new EncryptionException("Encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypts a .senc file and writes to outputFile.
     */
    public static void decryptFile(File inputFile, File outputFile, char[] password, ProgressCallback callback)
            throws DecryptionException, ValidationException, FileFormatException {
        if (inputFile == null || !inputFile.exists() || !inputFile.isFile()) {
            throw new ValidationException("Input file does not exist or is not a valid file.");
        }
        if (outputFile == null) {
            throw new ValidationException("Output file path must be specified.");
        }
        if (outputFile.exists()) {
            throw new ValidationException("Output file already exists: " + outputFile.getName());
        }
        if (password == null || password.length == 0) {
            throw new ValidationException("Password cannot be empty.");
        }

        File tempFile = new File(outputFile.getAbsolutePath() + ".tmp." + System.currentTimeMillis());
        try {
            if (callback != null) callback.onProgress(10, "Parsing and validating container header...");

            byte[] salt;
            byte[] nonce;
            int iterations;
            byte[] ciphertext;

            try (FileInputStream fis = new FileInputStream(inputFile);
                 DataInputStream dis = new DataInputStream(new BufferedInputStream(fis))) {

                // Read and check Magic Header
                byte[] magic = new byte[4];
                dis.readFully(magic);
                if (!Arrays.equals(magic, MAGIC_HEADER)) {
                    throw new FileFormatException("Invalid file format. Magic header does not match CipherShield signature.");
                }

                // Read Version
                short version = dis.readShort();
                if (version != CURRENT_VERSION) {
                    throw new FileFormatException("Unsupported file version: " + version + " (expected: " + CURRENT_VERSION + ")");
                }

                // Read Iterations
                iterations = dis.readInt();
                if (iterations < MIN_ITERATIONS || iterations > MAX_ITERATIONS) {
                    throw new FileFormatException("Unreasonable KDF iteration count: " + iterations);
                }

                // Read Salt
                int saltLen = dis.readByte() & 0xFF;
                if (saltLen != SALT_LENGTH) {
                    throw new FileFormatException("Invalid salt length in header: " + saltLen);
                }
                salt = new byte[saltLen];
                dis.readFully(salt);

                // Read Nonce
                int nonceLen = dis.readByte() & 0xFF;
                if (nonceLen != NONCE_LENGTH) {
                    throw new FileFormatException("Invalid nonce length in header: " + nonceLen);
                }
                nonce = new byte[nonceLen];
                dis.readFully(nonce);

                // Read Remaining Payload (Ciphertext + Tag)
                int headerSize = 4 + 2 + 4 + 1 + saltLen + 1 + nonceLen;
                long payloadSize = inputFile.length() - headerSize;
                if (payloadSize < (GCM_TAG_BITS / 8)) {
                    throw new FileFormatException("Corrupted file: Ciphertext payload is too small.");
                }
                if (payloadSize > MAX_FILE_SIZE) {
                    throw new FileFormatException("Ciphertext payload exceeds max file limit.");
                }
                ciphertext = new byte[(int) payloadSize];
                dis.readFully(ciphertext);
            }

            if (callback != null && callback.isCancelled()) {
                throw new DecryptionException("Operation was cancelled by user.");
            }

            if (callback != null) callback.onProgress(30, "Deriving key with PBKDF2 (" + iterations + " rounds)...");
            SecretKey key = KeyDerivation.deriveKey(password, salt, iterations, 256);
            SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");

            if (callback != null && callback.isCancelled()) {
                throw new DecryptionException("Operation was cancelled by user.");
            }

            if (callback != null) callback.onProgress(60, "Authenticating and decrypting payload...");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_BITS, nonce);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            byte[] plaintext = cipher.doFinal(ciphertext);

            if (callback != null && callback.isCancelled()) {
                throw new DecryptionException("Operation was cancelled by user.");
            }

            if (callback != null) callback.onProgress(85, "Writing decrypted file to temporary location...");
            try (FileOutputStream fos = new FileOutputStream(tempFile);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                bos.write(plaintext);
                bos.flush();
            }

            if (callback != null && callback.isCancelled()) {
                throw new DecryptionException("Operation was cancelled by user.");
            }

            if (callback != null) callback.onProgress(95, "Finalizing decrypted file...");
            commitOutputFile(tempFile, outputFile);

            if (callback != null) callback.onProgress(100, "Decryption and authentication successful!");

        } catch (javax.crypto.AEADBadTagException e) {
            deleteQuietly(tempFile);
            throw new DecryptionException("Authentication failed: Incorrect password or file corrupted/tampered.", e);
        } catch (FileFormatException | ValidationException | DecryptionException e) {
            deleteQuietly(tempFile);
            throw e;
        } catch (Exception e) {
            deleteQuietly(tempFile);
            throw new DecryptionException("Decryption failed: " + e.getMessage(), e);
        }
    }

    private static void deleteQuietly(File file) {
        if (file != null && file.exists()) {
            try {
                file.delete();
            } catch (Exception ignored) {}
        }
    }

    private static void commitOutputFile(File tempFile, File outputFile) throws IOException {
        try {
            Files.createLink(outputFile.toPath(), tempFile.toPath());
        } catch (FileAlreadyExistsException e) {
            throw e;
        } catch (UnsupportedOperationException e) {
            Files.move(tempFile.toPath(), outputFile.toPath());
            return;
        } catch (IOException linkError) {
            if (Files.exists(outputFile.toPath())) {
                throw linkError;
            }
            try {
                Files.move(tempFile.toPath(), outputFile.toPath());
            } catch (IOException moveError) {
                moveError.addSuppressed(linkError);
                throw moveError;
            }
            return;
        }
        deleteQuietly(tempFile);
    }
}
