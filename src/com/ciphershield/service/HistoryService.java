package com.ciphershield.service;

import com.ciphershield.model.HistoryEntry;
import com.ciphershield.model.OperationStats;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service managing local operation audit history.
 * Persists history to ~/.ciphershield/history.dat using Java object serialization.
 * Thread-safe with synchronized methods.
 * Resets gracefully if local data is missing or corrupted.
 */
public class HistoryService {

    private static final String APP_DIR_NAME = ".ciphershield";
    private static final String HISTORY_FILE_NAME = "history.dat";
    private static HistoryService instance;

    private final List<HistoryEntry> entries = new ArrayList<>();
    private final File storageFile;

    private HistoryService() {
        String userHome = System.getProperty("user.home", ".");
        File dir = new File(userHome, APP_DIR_NAME);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        this.storageFile = new File(dir, HISTORY_FILE_NAME);
        loadHistory();
    }

    public static synchronized HistoryService getInstance() {
        if (instance == null) {
            instance = new HistoryService();
        }
        return instance;
    }

    public synchronized void recordOperation(String opType, String targetName, boolean success, String errorCategory, long fileSizeBytes) {
        HistoryEntry entry = new HistoryEntry(opType, targetName, success, errorCategory, fileSizeBytes);
        entries.add(0, entry); // Most recent first
        saveHistory();
    }

    public synchronized List<HistoryEntry> getHistory() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public synchronized OperationStats getStats() {
        int total = entries.size();
        int success = 0;
        int fail = 0;
        for (HistoryEntry e : entries) {
            if (e.isSuccess()) {
                success++;
            } else {
                fail++;
            }
        }
        return new OperationStats(total, success, fail);
    }

    public synchronized void clearHistory() {
        entries.clear();
        if (storageFile.exists()) {
            try {
                storageFile.delete();
            } catch (Exception ignored) {}
        }
    }

    @SuppressWarnings("unchecked")
    private synchronized void loadHistory() {
        if (!storageFile.exists()) return;
        try (FileInputStream fis = new FileInputStream(storageFile);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                entries.clear();
                entries.addAll((List<HistoryEntry>) obj);
            }
        } catch (Exception e) {
            // In case of corruption or class mismatch, reset smoothly
            entries.clear();
        }
    }

    private synchronized void saveHistory() {
        try (FileOutputStream fos = new FileOutputStream(storageFile);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(new ArrayList<>(entries));
            oos.flush();
        } catch (Exception ignored) {
            // Non-critical persistence failure
        }
    }
}
