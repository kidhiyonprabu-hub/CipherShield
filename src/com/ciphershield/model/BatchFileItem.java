package com.ciphershield.model;

import java.io.File;

/**
 * Model representing a single file item queued or processed in Batch Center.
 */
public class BatchFileItem {
    public enum Status {
        PENDING("Pending"),
        PROCESSING("Processing..."),
        SUCCESS("Success"),
        FAILED("Failed"),
        CANCELLED("Cancelled");

        private final String display;
        Status(String display) { this.display = display; }
        public String getDisplay() { return display; }
    }

    private final File file;
    private Status status;
    private String errorMessage;
    private long processedBytes;
    private File outputFile;

    public BatchFileItem(File file) {
        this.file = file;
        this.status = Status.PENDING;
        this.errorMessage = "";
        this.processedBytes = 0;
    }

    public File getFile() { return file; }
    public String getFileName() { return file != null ? file.getName() : "Unknown"; }
    public long getFileSize() { return file != null ? file.length() : 0; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public long getProcessedBytes() { return processedBytes; }
    public void setProcessedBytes(long processedBytes) { this.processedBytes = processedBytes; }
    public File getOutputFile() { return outputFile; }
    public void setOutputFile(File outputFile) { this.outputFile = outputFile; }

    public String getFormattedSize() {
        long sz = getFileSize();
        if (sz < 1024) return sz + " B";
        if (sz < 1024 * 1024) return String.format("%.1f KB", sz / 1024.0);
        return String.format("%.1f MB", sz / (1024.0 * 1024.0));
    }
}
