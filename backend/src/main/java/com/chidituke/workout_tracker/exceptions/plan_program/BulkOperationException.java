package com.chidituke.workout_tracker.exceptions.plan_program;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.Map;

/**
 * Exception thrown when bulk operations partially fail
 */
@ResponseStatus(HttpStatus.PARTIAL_CONTENT)
public class BulkOperationException extends RuntimeException {

    private final List<String> successfulOperations;
    private final Map<String, String> failedOperations;
    private final int totalOperations;

    public BulkOperationException(String message, List<String> successfulOperations,
                                  Map<String, String> failedOperations, int totalOperations) {
        super(message);
        this.successfulOperations = successfulOperations;
        this.failedOperations = failedOperations;
        this.totalOperations = totalOperations;
    }

    public List<String> getSuccessfulOperations() {
        return successfulOperations;
    }

    public Map<String, String> getFailedOperations() {
        return failedOperations;
    }

    public int getTotalOperations() {
        return totalOperations;
    }

    public int getSuccessCount() {
        return successfulOperations.size();
    }

    public int getFailureCount() {
        return failedOperations.size();
    }
}