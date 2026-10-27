package com.ciphershield.test;

import com.ciphershield.crypto.CryptoEngine;
import com.ciphershield.crypto.HashEngine;
import com.ciphershield.crypto.SecureFileHandler;
import com.ciphershield.exception.DecryptionException;
import com.ciphershield.exception.FileFormatException;
import com.ciphershield.exception.ValidationException;
import com.ciphershield.model.BatchFileItem;
import com.ciphershield.model.ScanResult;
import com.ciphershield.service.BatchProcessor;
import com.ciphershield.service.PrivacyScanner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Automated Test Suite for CipherShield.
 * Runs unit, integration, and security tests.
 * All tests utilize isolated temporary directories and self-cleaning mock assets.
 */
public class TestRunner {

    private static int passedCount = 0;
    private static int failedCount = 0;
    private static final List<String> results = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("   CIPHERSHIELD AUTOMATED QUALITY ASSURANCE & SECURITY SUITE     ");
        System.out.println("=================================================================\n");

        File tempDir = null;
        try {
            tempDir = Files.createTempDirectory("ciphershield_test_").toFile();
            System.setProperty("user.home", tempDir.getAbsolutePath());

            // Run all 15 tests
            test1_TextRoundTrip();
            test2_FileRoundTrip(tempDir);
            test3_WrongPasswordRejection();
            test4_ModifiedCiphertextRejection();
            test5_ModifiedAuthTagRejection();
            test6_CorruptedAndTruncatedFileRejection(tempDir);
            test7_MissingFileHandling(tempDir);
            test8_InvalidFileHeaderHandling(tempDir);
            test9_EmptyInputValidation();
            test10_DuplicateOutputProtection(tempDir);
            test11_Sha256MatchAndMismatch(tempDir);
            test12_PrivacyGuardDetection();
            test13_BatchProcessingFailureRecovery(tempDir);
            test14_CancellationBehavior(tempDir);
            test15_BackgroundTaskResponsiveness();
            test16_PrivacyGuardTextFileScanning(tempDir);
            test17_PrivacyGuardDocxScanning(tempDir);
            test18_PrivacyGuardFileValidation(tempDir);
            test19_BatchDecryptionRoundTrip(tempDir);
            test20_BatchInvalidPasswordAndCorruptContainer(tempDir);
            test21_BatchDestinationNamesAndRepeatedRuns(tempDir);

        } catch (Exception e) {
            System.err.println("Unexpected test harness failure: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (tempDir != null && tempDir.exists()) {
                deleteRecursive(tempDir);
            }
        }

        System.out.println("\n=================================================================");
        System.out.println("                      TEST EXECUTION SUMMARY                     ");
        System.out.println("=================================================================");
        for (String r : results) {
            System.out.println(r);
        }
        System.out.println("-----------------------------------------------------------------");
        System.out.println(String.format("Total: %d | Passed: %d | Failed: %d | Pass Rate: %.1f%%",
                (passedCount + failedCount), passedCount, failedCount,
                (passedCount * 100.0) / (passedCount + failedCount)));
        System.out.println("=================================================================\n");

        if (failedCount > 0) {
            System.exit(1);
        }
    }

    private static void recordResult(int testNum, String name, boolean success, String details) {
        if (success) {
            passedCount++;
            results.add(String.format("[PASS] Test %02d: %-42s %s", testNum, name, details));
            System.out.println(String.format("[PASS] Test %02d: %s", testNum, name));
        } else {
            failedCount++;
            results.add(String.format("[FAIL] Test %02d: %-42s ERROR: %s", testNum, name, details));
            System.err.println(String.format("[FAIL] Test %02d: %s - %s", testNum, name, details));
        }
    }

    // 1. Text encryption/decryption round trip
    private static void test1_TextRoundTrip() {
        try {
            CryptoEngine engine = new CryptoEngine();
            String original = "Confidential MicroProject Data 2026! @#$%^&*()_+";
            char[] pass = "StrongPassword#2026".toCharArray();

            String encrypted = engine.encryptText(original, pass);
            String decrypted = engine.decryptText(encrypted, pass);

            boolean match = original.equals(decrypted);
            recordResult(1, "Text Encryption/Decryption Round Trip", match, match ? "(100% fidelity)" : "(Mismatch)");
        } catch (Exception e) {
            recordResult(1, "Text Encryption/Decryption Round Trip", false, e.getMessage());
        }
    }

    // 2. File encryption/decryption round trip
    private static void test2_FileRoundTrip(File tempDir) {
        try {
            File src = new File(tempDir, "sample_doc.bin");
            File enc = new File(tempDir, "sample_doc.bin.senc");
            File dec = new File(tempDir, "sample_doc_decrypted.bin");

            // Write 16KB of arbitrary binary content
            byte[] originalBytes = new byte[16384];
            new Random(42).nextBytes(originalBytes);
            try (FileOutputStream fos = new FileOutputStream(src)) {
                fos.write(originalBytes);
            }

            char[] pass = "FileSecretKey!9988".toCharArray();

            SecureFileHandler.encryptFile(src, enc, pass, null);
            SecureFileHandler.decryptFile(enc, dec, pass, null);

            byte[] decryptedBytes = Files.readAllBytes(dec.toPath());
            boolean match = Arrays.equals(originalBytes, decryptedBytes);

            recordResult(2, "File Encryption/Decryption Round Trip", match, "(16KB binary file verified)");
        } catch (Exception e) {
            recordResult(2, "File Encryption/Decryption Round Trip", false, e.getMessage());
        }
    }

    // 3. Wrong-password rejection
    private static void test3_WrongPasswordRejection() {
        try {
            CryptoEngine engine = new CryptoEngine();
            String secret = "Sensitive payload to be protected";
            char[] correctPass = "CorrectPassword123".toCharArray();
            char[] wrongPass = "WrongPassword456".toCharArray();

            String ciphertext = engine.encryptText(secret, correctPass);

            try {
                engine.decryptText(ciphertext, wrongPass);
                recordResult(3, "Wrong-Password Rejection", false, "Decryption succeeded with wrong password!");
            } catch (DecryptionException e) {
                recordResult(3, "Wrong-Password Rejection", true, "(Rejected safely with AEADBadTagException)");
            }
        } catch (Exception e) {
            recordResult(3, "Wrong-Password Rejection", false, e.getMessage());
        }
    }

    // 4. Modified ciphertext rejection
    private static void test4_ModifiedCiphertextRejection() {
        try {
            CryptoEngine engine = new CryptoEngine();
            String secret = "Highly sensitive banking credentials";
            char[] pass = "BankingKey!2026".toCharArray();

            byte[] encrypted = engine.encryptBytes(secret.getBytes("UTF-8"), pass);

            // Tamper with ciphertext byte in payload area
            encrypted[encrypted.length - 20] ^= 0xFF;

            try {
                engine.decryptBytes(encrypted, pass);
                recordResult(4, "Modified Ciphertext Rejection", false, "Tampered ciphertext was decrypted!");
            } catch (DecryptionException e) {
                recordResult(4, "Modified Ciphertext Rejection", true, "(Tampering detected and rejected)");
            }
        } catch (Exception e) {
            recordResult(4, "Modified Ciphertext Rejection", false, e.getMessage());
        }
    }

    // 5. Modified authentication-tag rejection
    private static void test5_ModifiedAuthTagRejection() {
        try {
            CryptoEngine engine = new CryptoEngine();
            String secret = "Integrity check test message";
            char[] pass = "AuthTagKey!77".toCharArray();

            byte[] encrypted = engine.encryptBytes(secret.getBytes("UTF-8"), pass);

            // The last 16 bytes are the GCM authentication tag. Flip a bit in the last byte.
            encrypted[encrypted.length - 1] ^= 0x01;

            try {
                engine.decryptBytes(encrypted, pass);
                recordResult(5, "Modified Auth-Tag Rejection", false, "Tampered auth tag was accepted!");
            } catch (DecryptionException e) {
                recordResult(5, "Modified Auth-Tag Rejection", true, "(GCM tag verification failed as required)");
            }
        } catch (Exception e) {
            recordResult(5, "Modified Auth-Tag Rejection", false, e.getMessage());
        }
    }

    // 6. Corrupted and truncated file rejection
    private static void test6_CorruptedAndTruncatedFileRejection(File tempDir) {
        try {
            File validSrc = new File(tempDir, "test6_valid.txt");
            File validEnc = new File(tempDir, "test6_valid.txt.senc");
            File truncEnc = new File(tempDir, "test6_truncated.senc");
            File decDst = new File(tempDir, "test6_dec.txt");

            try (FileOutputStream fos = new FileOutputStream(validSrc)) {
                fos.write("Data to be encrypted and truncated".getBytes("UTF-8"));
            }

            char[] pass = "Test6Pass!123".toCharArray();
            SecureFileHandler.encryptFile(validSrc, validEnc, pass, null);

            // Create truncated file (cut off half of payload)
            byte[] encBytes = Files.readAllBytes(validEnc.toPath());
            byte[] truncated = Arrays.copyOf(encBytes, 20); // Header only, no payload
            try (FileOutputStream fos = new FileOutputStream(truncEnc)) {
                fos.write(truncated);
            }

            try {
                SecureFileHandler.decryptFile(truncEnc, decDst, pass, null);
                recordResult(6, "Corrupted/Truncated File Rejection", false, "Truncated file was accepted!");
            } catch (Exception e) {
                recordResult(6, "Corrupted/Truncated File Rejection", true, "(Truncation intercepted: " + e.getClass().getSimpleName() + ")");
            }
        } catch (Exception e) {
            recordResult(6, "Corrupted/Truncated File Rejection", false, e.getMessage());
        }
    }

    // 7. Missing-file handling
    private static void test7_MissingFileHandling(File tempDir) {
        try {
            File nonExistent = new File(tempDir, "phantom_file_" + System.currentTimeMillis() + ".dat");
            File out = new File(tempDir, "out.senc");
            char[] pass = "Pass123!".toCharArray();

            try {
                SecureFileHandler.encryptFile(nonExistent, out, pass, null);
                recordResult(7, "Missing-File Handling", false, "Missing file did not throw exception!");
            } catch (ValidationException e) {
                recordResult(7, "Missing-File Handling", true, "(ValidationException caught smoothly)");
            }
        } catch (Exception e) {
            recordResult(7, "Missing-File Handling", false, e.getMessage());
        }
    }

    // 8. Invalid file-header handling
    private static void test8_InvalidFileHeaderHandling(File tempDir) {
        try {
            File fakeHeaderFile = new File(tempDir, "fake_header.senc");
            File out = new File(tempDir, "fake_out.dat");

            // Write random bytes as header instead of "CSHD"
            try (FileOutputStream fos = new FileOutputStream(fakeHeaderFile)) {
                fos.write(new byte[]{'B', 'A', 'D', '!', 0x00, 0x01, 0x00, 0x00});
                fos.write(new byte[100]);
            }

            char[] pass = "Pass123!".toCharArray();
            try {
                SecureFileHandler.decryptFile(fakeHeaderFile, out, pass, null);
                recordResult(8, "Invalid File-Header Handling", false, "Bad header accepted!");
            } catch (FileFormatException e) {
                recordResult(8, "Invalid File-Header Handling", true, "(FileFormatException caught: " + e.getMessage() + ")");
            }
        } catch (Exception e) {
            recordResult(8, "Invalid File-Header Handling", false, e.getMessage());
        }
    }

    // 9. Empty-input validation
    private static void test9_EmptyInputValidation() {
        try {
            CryptoEngine engine = new CryptoEngine();
            char[] pass = "Pass123!".toCharArray();

            boolean caught = false;
            try {
                engine.encryptText("", pass);
            } catch (ValidationException e) {
                caught = true;
            }

            try {
                engine.encryptText("Hello", new char[0]);
                caught = false;
            } catch (ValidationException e) {
                caught = caught && true;
            }

            recordResult(9, "Empty-Input Validation", caught, "(Empty text & empty password rejected)");
        } catch (Exception e) {
            recordResult(9, "Empty-Input Validation", false, e.getMessage());
        }
    }

    // 10. Duplicate-output protection
    private static void test10_DuplicateOutputProtection(File tempDir) {
        try {
            File src = new File(tempDir, "dup_src.txt");
            File existingDest = new File(tempDir, "already_exists.senc");

            try (FileOutputStream fos = new FileOutputStream(src)) { fos.write("Data".getBytes()); }
            try (FileOutputStream fos = new FileOutputStream(existingDest)) { fos.write("Old data".getBytes()); }

            char[] pass = "Pass123!".toCharArray();
            try {
                SecureFileHandler.encryptFile(src, existingDest, pass, null);
                recordResult(10, "Duplicate-Output Protection", false, "Overwrote existing file without checking!");
            } catch (ValidationException e) {
                recordResult(10, "Duplicate-Output Protection", true, "(Protected destination from accidental overwrite)");
            }
        } catch (Exception e) {
            recordResult(10, "Duplicate-Output Protection", false, e.getMessage());
        }
    }

    // 11. SHA-256 match and mismatch
    private static void test11_Sha256MatchAndMismatch(File tempDir) {
        try {
            File f = new File(tempDir, "hash_test.txt");
            String data = "Integrity Lab Testing Content 2026";
            try (FileOutputStream fos = new FileOutputStream(f)) { fos.write(data.getBytes("UTF-8")); }

            String actualHash = HashEngine.calculateFileSHA256(f, null);
            String stringHash = HashEngine.calculateStringSHA256(data);

            boolean matchExact = HashEngine.compareHashes(actualHash, stringHash);
            boolean mismatchTest = HashEngine.compareHashes(actualHash, "0000000000000000000000000000000000000000000000000000000000000000");

            boolean success = matchExact && !mismatchTest;
            recordResult(11, "SHA-256 Match and Mismatch", success, "(Exact hash matched, dummy hash rejected)");
        } catch (Exception e) {
            recordResult(11, "SHA-256 Match and Mismatch", false, e.getMessage());
        }
    }

    // 12. PrivacyGuard detection
    private static void test12_PrivacyGuardDetection() {
        try {
            PrivacyScanner scanner = new PrivacyScanner();
            String sample = "Hello team, please contact alice.smith@college.edu or call 555-234-5678.\n" +
                    "Student registered ID: STU-987654 in Computer Science.";

            List<ScanResult> results = scanner.scanText(sample);

            boolean foundEmail = false;
            boolean foundPhone = false;
            boolean foundId = false;

            for (ScanResult r : results) {
                if (r.getCategory() == ScanResult.Category.EMAIL) foundEmail = true;
                if (r.getCategory() == ScanResult.Category.PHONE) foundPhone = true;
                if (r.getCategory() == ScanResult.Category.STUDENT_ID) foundId = true;
            }

            boolean allFound = foundEmail && foundPhone && foundId;
            recordResult(12, "PrivacyGuard Pattern Detection", allFound,
                    String.format("(Detected: Email=%b, Phone=%b, StudentID=%b)", foundEmail, foundPhone, foundId));
        } catch (Exception e) {
            recordResult(12, "PrivacyGuard Pattern Detection", false, e.getMessage());
        }
    }

    // 13. Batch-processing failure recovery
    private static void test13_BatchProcessingFailureRecovery(File tempDir) {
        try {
            File outDir = new File(tempDir, "batch_out");
            outDir.mkdirs();

            File good1 = new File(tempDir, "good1.txt");
            File good2 = new File(tempDir, "good2.txt");
            File bad = new File(tempDir, "bad_ghost.txt"); // Not created on disk

            try (FileOutputStream fos = new FileOutputStream(good1)) { fos.write("Data1".getBytes()); }
            try (FileOutputStream fos = new FileOutputStream(good2)) { fos.write("Data2".getBytes()); }

            List<BatchFileItem> items = new ArrayList<>();
            items.add(new BatchFileItem(good1));
            items.add(new BatchFileItem(bad)); // Will fail
            items.add(new BatchFileItem(good2));

            BatchProcessor processor = new BatchProcessor();
            final CountDownLatch latch = new CountDownLatch(1);
            final int[] outcome = new int[2]; // [success, fail]

            processor.executeBatch(items, "BatchPass!1".toCharArray(), true, outDir, new BatchProcessor.BatchCallback() {
                @Override
                public void onFileStart(BatchFileItem item, int index, int total) {}
                @Override
                public void onFileComplete(BatchFileItem item, int index, int total) {}
                @Override
                public void onFileFailed(BatchFileItem item, int index, int total, String error) {}
                @Override
                public void onBatchFinished(int successCount, int failCount, boolean cancelled) {
                    outcome[0] = successCount;
                    outcome[1] = failCount;
                    latch.countDown();
                }
            });

            boolean finished = latch.await(10, TimeUnit.SECONDS);
            processor.shutdown();

            boolean correct = finished && outcome[0] == 2 && outcome[1] == 1;
            recordResult(13, "Batch Processing Failure Isolation", correct,
                    String.format("(Processed: 2 success, 1 isolated failure: %b)", correct));
        } catch (Exception e) {
            recordResult(13, "Batch Processing Failure Isolation", false, e.getMessage());
        }
    }

    // 14. Cancellation behaviour
    private static void test14_CancellationBehavior(File tempDir) {
        try {
            final AtomicBoolean cancelledCheck = new AtomicBoolean(false);

            SecureFileHandler.ProgressCallback cb = new SecureFileHandler.ProgressCallback() {
                private int callCount = 0;
                @Override
                public void onProgress(int percentage, String status) {
                    callCount++;
                }

                @Override
                public boolean isCancelled() {
                    return true; // Immediately simulate cancellation
                }
            };

            File src = new File(tempDir, "cancel_test.bin");
            File dst = new File(tempDir, "cancel_test.bin.senc");
            try (FileOutputStream fos = new FileOutputStream(src)) {
                fos.write(new byte[1024]);
            }

            try {
                SecureFileHandler.encryptFile(src, dst, "Pass123!".toCharArray(), cb);
                recordResult(14, "Cancellation Behaviour", false, "Did not abort upon cancellation signal!");
            } catch (Exception e) {
                // Should clean up temp file and throw cancel exception
                boolean cleanedUp = !dst.exists();
                recordResult(14, "Cancellation Behaviour", cleanedUp, "(Aborted safely, output cleaned up)");
            }
        } catch (Exception e) {
            recordResult(14, "Cancellation Behaviour", false, e.getMessage());
        }
    }

    // 15. Background task and GUI responsiveness
    private static void test15_BackgroundTaskResponsiveness() {
        try {
            // Verify KeyDerivation performance and parameter bounds
            long t0 = System.currentTimeMillis();
            com.ciphershield.crypto.KeyDerivation.deriveKey("TestPass".toCharArray(), new byte[16], 100000, 256);
            long elapsed = System.currentTimeMillis() - t0;

            boolean fastEnough = elapsed < 3000;
            recordResult(15, "Crypto Responsiveness Benchmark", fastEnough,
                    String.format("(PBKDF2 100k rounds benchmark: %d ms)", elapsed));
        } catch (Exception e) {
            recordResult(15, "Crypto Responsiveness Benchmark", false, e.getMessage());
        }
    }

    private static void test16_PrivacyGuardTextFileScanning(File tempDir) {
        try {
            File textFile = new File(tempDir, "privacy_scan.txt");
            String sample = "alice@college.edu 555-234-5678 STU-987654";
            Files.write(textFile.toPath(), sample.getBytes(StandardCharsets.UTF_8));

            PrivacyScanner scanner = new PrivacyScanner();
            String loaded = scanner.readFileContent(textFile);
            List<ScanResult> results = scanner.scanFile(textFile);
            boolean detected = loaded.contains(sample) && hasAllPrivacyCategories(results);
            recordResult(16, "PrivacyGuard TXT File Scanning", detected,
                    "(Loaded file contents and detected email, phone, student ID)");
        } catch (Exception e) {
            recordResult(16, "PrivacyGuard TXT File Scanning", false, e.getMessage());
        }
    }

    private static void test17_PrivacyGuardDocxScanning(File tempDir) {
        try {
            File docx = new File(tempDir, "privacy_scan.docx");
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">" +
                    "<w:body><w:p><w:r><w:t>alice@college.edu 555-234-5678</w:t></w:r></w:p>" +
                    "<w:p><w:r><w:t>STU-987654</w:t></w:r></w:p></w:body></w:document>";
            try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(docx))) {
                zip.putNextEntry(new ZipEntry("word/document.xml"));
                zip.write(xml.getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }

            PrivacyScanner scanner = new PrivacyScanner();
            String loaded = scanner.readFileContent(docx);
            boolean detected = loaded.contains("alice@college.edu")
                    && hasAllPrivacyCategories(scanner.scanText(loaded));
            recordResult(17, "PrivacyGuard DOCX File Scanning", detected,
                    "(Extracted document XML and detected email, phone, student ID)");
        } catch (Exception e) {
            recordResult(17, "PrivacyGuard DOCX File Scanning", false, e.getMessage());
        }
    }

    private static void test18_PrivacyGuardFileValidation(File tempDir) {
        File empty = new File(tempDir, "empty_privacy.txt");
        File unsupported = new File(tempDir, "unsupported_privacy.pdf");
        File malformed = new File(tempDir, "malformed_privacy.docx");
        try {
            Files.write(empty.toPath(), new byte[0]);
            Files.write(unsupported.toPath(), "not a supported document".getBytes(StandardCharsets.UTF_8));
            try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(malformed))) {
                zip.putNextEntry(new ZipEntry("word/document.xml"));
                zip.write("<not-valid-xml".getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }

            PrivacyScanner scanner = new PrivacyScanner();
            boolean emptyRejected = rejectsFile(scanner, empty, "empty");
            boolean unsupportedRejected = rejectsFile(scanner, unsupported, "supports .txt and .docx");
            boolean malformedRejected = rejectsFile(scanner, malformed, "malformed or corrupted");
            boolean allRejected = emptyRejected && unsupportedRejected && malformedRejected;
            recordResult(18, "PrivacyGuard Empty/Unsupported/Malformed Files", allRejected,
                    String.format("(empty=%b, unsupported=%b, malformed=%b)",
                            emptyRejected, unsupportedRejected, malformedRejected));
        } catch (Exception e) {
            recordResult(18, "PrivacyGuard Empty/Unsupported/Malformed Files", false, e.getMessage());
        }
    }

    private static void test19_BatchDecryptionRoundTrip(File tempDir) {
        BatchProcessor processor = new BatchProcessor();
        try {
            File source = new File(tempDir, "batch_decrypt_source.txt");
            File encrypted = new File(tempDir, "batch_decrypt_source.txt.senc");
            File outputDir = new File(tempDir, "batch_decrypt_output");
            outputDir.mkdirs();
            byte[] original = "Valid CipherShield batch decryption payload".getBytes(StandardCharsets.UTF_8);
            Files.write(source.toPath(), original);
            char[] password = "BatchRoundTrip#2026".toCharArray();
            SecureFileHandler.encryptFile(source, encrypted, password, null);

            BatchFileItem item = new BatchFileItem(encrypted);
            BatchResult result = runBatch(processor, Collections.singletonList(item),
                    "BatchRoundTrip#2026".toCharArray(), false, outputDir);
            boolean success = result.finished && result.success == 1 && result.failed == 0
                    && item.getStatus() == BatchFileItem.Status.SUCCESS
                    && item.getOutputFile() != null
                    && Arrays.equals(original, Files.readAllBytes(item.getOutputFile().toPath()));
            recordResult(19, "Batch Valid .senc Decryption", success,
                    "(Decrypts with the same authenticated file handler as File Security)");
        } catch (Exception e) {
            recordResult(19, "Batch Valid .senc Decryption", false, e.getMessage());
        } finally {
            processor.shutdown();
        }
    }

    private static void test20_BatchInvalidPasswordAndCorruptContainer(File tempDir) {
        BatchProcessor processor = new BatchProcessor();
        try {
            File source = new File(tempDir, "batch_error_source.txt");
            File validContainer = new File(tempDir, "batch_error_source.txt.senc");
            File corruptContainer = new File(tempDir, "batch_corrupt.senc");
            File outputDir = new File(tempDir, "batch_error_output");
            outputDir.mkdirs();
            Files.write(source.toPath(), "authenticated payload".getBytes(StandardCharsets.UTF_8));
            SecureFileHandler.encryptFile(source, validContainer, "CorrectBatchPass".toCharArray(), null);
            Files.write(corruptContainer.toPath(), "not a CipherShield container".getBytes(StandardCharsets.UTF_8));

            BatchFileItem wrongPassword = new BatchFileItem(validContainer);
            BatchFileItem corrupt = new BatchFileItem(corruptContainer);
            BatchResult result = runBatch(processor, Arrays.asList(wrongPassword, corrupt),
                    "WrongBatchPass".toCharArray(), false, outputDir);
            boolean failuresIdentified = result.finished && result.success == 0 && result.failed == 2
                    && wrongPassword.getErrorMessage().contains("Authentication failed")
                    && corrupt.getErrorMessage().contains("Invalid file format")
                    && outputDir.listFiles().length == 0;
            recordResult(20, "Batch Wrong Password and Corrupt File", failuresIdentified,
                    "(Both rejected with distinct errors; no plaintext output created)");
        } catch (Exception e) {
            recordResult(20, "Batch Wrong Password and Corrupt File", false, e.getMessage());
        } finally {
            processor.shutdown();
        }
    }

    private static void test21_BatchDestinationNamesAndRepeatedRuns(File tempDir) {
        BatchProcessor processor = new BatchProcessor();
        try {
            File firstDir = new File(tempDir, "duplicate_source_a");
            File secondDir = new File(tempDir, "duplicate_source_b");
            File outputDir = new File(tempDir, "duplicate_batch_output");
            firstDir.mkdirs();
            secondDir.mkdirs();
            outputDir.mkdirs();
            File firstSource = new File(firstDir, "report.txt");
            File secondSource = new File(secondDir, "report.txt");
            Files.write(firstSource.toPath(), "first file".getBytes(StandardCharsets.UTF_8));
            Files.write(secondSource.toPath(), "second file".getBytes(StandardCharsets.UTF_8));
            List<BatchFileItem> items = Arrays.asList(new BatchFileItem(firstSource), new BatchFileItem(secondSource));

            BatchResult firstRun = runBatch(processor, items, "DuplicateBatch#2026".toCharArray(), true, outputDir);
            Set<String> firstNames = new HashSet<String>();
            for (File file : outputDir.listFiles()) firstNames.add(file.getName());
            boolean firstRunSafe = firstRun.finished && firstRun.success == 2 && firstRun.failed == 0
                    && firstNames.contains("report.txt.senc") && firstNames.contains("report.txt (1).senc");

            BatchResult secondRun = runBatch(processor, items, "DuplicateBatch#2026".toCharArray(), true, outputDir);
            Set<String> allNames = new HashSet<String>();
            for (File file : outputDir.listFiles()) allNames.add(file.getName());
            boolean repeatedRunSafe = secondRun.finished && secondRun.success == 2 && secondRun.failed == 0
                    && allNames.contains("report.txt (2).senc") && allNames.contains("report.txt (3).senc");

            File decryptCollisionDir = new File(tempDir, "decrypt_collision_output");
            decryptCollisionDir.mkdirs();
            File encryptedSource = new File(tempDir, "existing_destination.senc");
            File protectedDestination = new File(decryptCollisionDir, "existing_destination");
            byte[] original = "decrypted without overwriting".getBytes(StandardCharsets.UTF_8);
            Files.write(firstSource.toPath(), original);
            SecureFileHandler.encryptFile(firstSource, encryptedSource, "DuplicateBatch#2026".toCharArray(), null);
            byte[] keepExisting = "preserve this existing file".getBytes(StandardCharsets.UTF_8);
            Files.write(protectedDestination.toPath(), keepExisting);
            BatchFileItem decryptItem = new BatchFileItem(encryptedSource);
            BatchResult decryptRun = runBatch(processor, Collections.singletonList(decryptItem),
                    "DuplicateBatch#2026".toCharArray(), false, decryptCollisionDir);
            boolean destinationPreserved = decryptRun.finished && decryptRun.success == 1
                    && Arrays.equals(keepExisting, Files.readAllBytes(protectedDestination.toPath()))
                    && decryptItem.getOutputFile().getName().equals("existing_destination (1)")
                    && Arrays.equals(original, Files.readAllBytes(decryptItem.getOutputFile().toPath()));
            recordResult(21, "Batch Duplicate Names and Repeated Runs",
                    firstRunSafe && repeatedRunSafe && destinationPreserved,
                    "(Unique names across repeated runs; existing decryption destination preserved)");
        } catch (Exception e) {
            recordResult(21, "Batch Duplicate Names and Repeated Runs", false, e.getMessage());
        } finally {
            processor.shutdown();
        }
    }

    private static boolean hasAllPrivacyCategories(List<ScanResult> results) {
        boolean email = false;
        boolean phone = false;
        boolean studentId = false;
        for (ScanResult result : results) {
            if (result.getCategory() == ScanResult.Category.EMAIL) email = true;
            if (result.getCategory() == ScanResult.Category.PHONE) phone = true;
            if (result.getCategory() == ScanResult.Category.STUDENT_ID) studentId = true;
        }
        return email && phone && studentId;
    }

    private static boolean rejectsFile(PrivacyScanner scanner, File file, String expectedMessage) {
        try {
            scanner.readFileContent(file);
            return false;
        } catch (IOException e) {
            return e.getMessage() != null && e.getMessage().contains(expectedMessage);
        }
    }

    private static BatchResult runBatch(BatchProcessor processor, List<BatchFileItem> items, char[] password,
                                        boolean encrypt, File outputDir) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final BatchResult result = new BatchResult();
        processor.executeBatch(items, password, encrypt, outputDir, new BatchProcessor.BatchCallback() {
            @Override
            public void onFileStart(BatchFileItem item, int index, int total) {
            }

            @Override
            public void onFileComplete(BatchFileItem item, int index, int total) {
            }

            @Override
            public void onFileFailed(BatchFileItem item, int index, int total, String error) {
            }

            @Override
            public void onBatchFinished(int successCount, int failCount, boolean cancelled) {
                result.success = successCount;
                result.failed = failCount;
                result.finished = !cancelled;
                latch.countDown();
            }
        });
        if (!latch.await(30, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Batch test timed out.");
        }
        return result;
    }

    private static class BatchResult {
        private int success;
        private int failed;
        private boolean finished;
    }

    private static void deleteRecursive(File f) {
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) {
                for (File c : children) deleteRecursive(c);
            }
        }
        f.delete();
    }
}
