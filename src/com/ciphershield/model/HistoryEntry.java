package com.ciphershield.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Audit log entry for security operations.
 * CRITICAL SECURITY REQUIREMENT:
 * Never store passwords, encryption keys, plaintexts, or sensitive scanner matches.
 */
public class HistoryEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final Date timestamp;
    private final String operationType; // e.g. TEXT_ENCRYPT, FILE_DECRYPT, BATCH_ENCRYPT, HASH_VERIFY
    private final String targetName;    // filename or label (no plaintext)
    private final boolean success;
    private final String errorCategory; // Safe category, e.g. "AUTH_FAILED", "NONE"
    private final long fileSizeBytes;

    public HistoryEntry(String operationType, String targetName, boolean success, String errorCategory, long fileSizeBytes) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.timestamp = new Date();
        this.operationType = operationType;
        this.targetName = (targetName != null && !targetName.trim().isEmpty()) ? targetName : "N/A";
        this.success = success;
        this.errorCategory = (errorCategory != null && !errorCategory.trim().isEmpty()) ? errorCategory : (success ? "NONE" : "UNKNOWN_ERROR");
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getId() { return id; }
    public Date getTimestamp() { return timestamp; }
    public String getFormattedTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timestamp);
    }
    public String getOperationType() { return operationType; }
    public String getTargetName() { return targetName; }
    public boolean isSuccess() { return success; }
    public String getErrorCategory() { return errorCategory; }
    public long getFileSizeBytes() { return fileSizeBytes; }

    public String getFormattedSize() {
        if (fileSizeBytes <= 0) return "—";
        if (fileSizeBytes < 1024) return fileSizeBytes + " B";
        if (fileSizeBytes < 1024 * 1024) return String.format("%.1f KB", fileSizeBytes / 1024.0);
        return String.format("%.1f MB", fileSizeBytes / (1024.0 * 1024.0));
    }
}
