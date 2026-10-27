package com.ciphershield.service;

import com.ciphershield.crypto.SecureFileHandler;
import com.ciphershield.model.BatchFileItem;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service for concurrently processing multi-file encryption and decryption.
 * Uses bounded ExecutorService to prevent system resource exhaustion.
 *
 * CO3: Multithreading and Performance Enhancement.
 */
public class BatchProcessor {

    public interface BatchCallback {
        void onFileStart(BatchFileItem item, int index, int total);
        void onFileComplete(BatchFileItem item, int index, int total);
        void onFileFailed(BatchFileItem item, int index, int total, String error);
        void onBatchFinished(int successCount, int failCount, boolean cancelled);
    }

    private final ExecutorService executorService;
    private final AtomicBoolean cancellationRequested = new AtomicBoolean(false);

    public BatchProcessor() {
        // Bounded thread pool of 2 threads to prevent disk saturation and maintain responsiveness
        this.executorService = Executors.newFixedThreadPool(2, new ThreadFactory() {
            private int counter = 1;
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "BatchWorker-" + counter++);
                t.setDaemon(true);
                return t;
            }
        });
    }

    public void cancel() {
        cancellationRequested.set(true);
    }

    public boolean isCancelled() {
        return cancellationRequested.get();
    }

    /**
     * Executes the batch operation in background threads.
     */
    public void executeBatch(final List<BatchFileItem> items,
                             final char[] password,
                             final boolean isEncrypt,
                             final File outputDir,
                             final BatchCallback callback) {
        cancellationRequested.set(false);

        CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                int total = items.size();
                int successCount = 0;
                int failCount = 0;
                try {
                  for (int i = 0; i < total; i++) {
                    if (cancellationRequested.get()) {
                        for (int j = i; j < total; j++) {
                            items.get(j).setStatus(BatchFileItem.Status.CANCELLED);
                        }
                        return;
                    }

                    final BatchFileItem item = items.get(i);
                    final int currentIndex = i + 1;
                    item.setStatus(BatchFileItem.Status.PROCESSING);
                    item.setErrorMessage("");
                    item.setOutputFile(null);
                    if (callback != null) {
                        callback.onFileStart(item, currentIndex, total);
                    }

                    try {
                        File inputFile = item.getFile();
                        File outputFile;
                        if (isEncrypt) {
                            outputFile = SecureFileHandler.getAvailableOutputFile(outputDir,
                                    inputFile.getName() + SecureFileHandler.ENCRYPTED_EXTENSION);
                        } else {
                            String name = inputFile.getName();
                            if (name.toLowerCase(java.util.Locale.ROOT)
                                    .endsWith(SecureFileHandler.ENCRYPTED_EXTENSION)) {
                                name = name.substring(0, name.length() - SecureFileHandler.ENCRYPTED_EXTENSION.length());
                            } else {
                                name = name + ".dec";
                            }
                            outputFile = SecureFileHandler.getAvailableOutputFile(outputDir, name);
                        }

                        SecureFileHandler.ProgressCallback progressCallback = new SecureFileHandler.ProgressCallback() {
                            @Override
                            public void onProgress(int percentage, String status) {
                                // Progress within file
                            }

                            @Override
                            public boolean isCancelled() {
                                return cancellationRequested.get();
                            }
                        };

                        if (isEncrypt) {
                            SecureFileHandler.encryptFile(inputFile, outputFile, password, progressCallback);
                            HistoryService.getInstance().recordOperation("BATCH_ENCRYPT", inputFile.getName(), true, "NONE", inputFile.length());
                        } else {
                            SecureFileHandler.decryptFile(inputFile, outputFile, password, progressCallback);
                            HistoryService.getInstance().recordOperation("BATCH_DECRYPT", inputFile.getName(), true, "NONE", inputFile.length());
                        }

                        item.setOutputFile(outputFile);
                        item.setStatus(BatchFileItem.Status.SUCCESS);
                        successCount++;
                        if (callback != null) {
                            callback.onFileComplete(item, currentIndex, total);
                        }

                    } catch (Exception e) {
                        item.setStatus(BatchFileItem.Status.FAILED);
                        String errMsg = e.getMessage() != null && !e.getMessage().trim().isEmpty()
                                ? e.getMessage() : "Operation failed: " + e.getClass().getSimpleName();
                        item.setErrorMessage(errMsg);
                        failCount++;
                        HistoryService.getInstance().recordOperation(
                                isEncrypt ? "BATCH_ENCRYPT" : "BATCH_DECRYPT",
                                item.getFileName(),
                                false,
                                e.getClass().getSimpleName(),
                                item.getFileSize()
                        );
                        if (callback != null) {
                            callback.onFileFailed(item, currentIndex, total, errMsg);
                        }
                    }
                  }
                } finally {
                    if (password != null) {
                        Arrays.fill(password, '\0');
                    }
                    if (callback != null) {
                        callback.onBatchFinished(successCount, failCount, cancellationRequested.get());
                    }
                }
            }
        }, executorService);
    }

    public void shutdown() {
        executorService.shutdownNow();
    }
}
