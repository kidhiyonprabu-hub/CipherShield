package com.ciphershield.model;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe statistics tracking for application operations.
 */
public class OperationStats {
    private final AtomicInteger totalOperations = new AtomicInteger(0);
    private final AtomicInteger successfulOperations = new AtomicInteger(0);
    private final AtomicInteger failedOperations = new AtomicInteger(0);

    public OperationStats() {}

    public OperationStats(int total, int success, int fail) {
        this.totalOperations.set(total);
        this.successfulOperations.set(success);
        this.failedOperations.set(fail);
    }

    public void recordSuccess() {
        totalOperations.incrementAndGet();
        successfulOperations.incrementAndGet();
    }

    public void recordFailure() {
        totalOperations.incrementAndGet();
        failedOperations.incrementAndGet();
    }

    public int getTotalOperations() { return totalOperations.get(); }
    public int getSuccessfulOperations() { return successfulOperations.get(); }
    public int getFailedOperations() { return failedOperations.get(); }

    public double getSuccessRate() {
        int total = totalOperations.get();
        if (total == 0) return 100.0;
        return (successfulOperations.get() * 100.0) / total;
    }
}
