package com.ciphershield.model;

/**
 * Result model representing a sensitive pattern detected by PrivacyGuard.
 * Notice: For privacy compliance, matchedText is only kept in volatile memory for the UI preview.
 */
public class ScanResult {
    public enum Category {
        EMAIL("Email Address"),
        PHONE("Phone Number"),
        STUDENT_ID("Student / Campus ID");

        private final String displayName;
        Category(String displayName) {
            this.displayName = displayName;
        }
        public String getDisplayName() {
            return displayName;
        }
    }

    private final Category category;
    private final String matchedText;
    private final int lineNumber;
    private final int startOffset;

    public ScanResult(Category category, String matchedText, int lineNumber, int startOffset) {
        this.category = category;
        this.matchedText = matchedText;
        this.lineNumber = lineNumber;
        this.startOffset = startOffset;
    }

    public Category getCategory() { return category; }
    public String getMatchedText() { return matchedText; }
    public int getLineNumber() { return lineNumber; }
    public int getStartOffset() { return startOffset; }
}
