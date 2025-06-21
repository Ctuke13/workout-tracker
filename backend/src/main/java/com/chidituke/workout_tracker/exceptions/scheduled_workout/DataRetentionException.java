package com.chidituke.workout_tracker.exceptions.scheduled_workout;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when scheduled workout data retention policies are violated
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DataRetentionException extends RuntimeException {

    private final String retentionPolicy;
    private final java.time.LocalDate cutoffDate;

    public DataRetentionException(String message) {
        super(message);
        this.retentionPolicy = null;
        this.cutoffDate = null;
    }

    public DataRetentionException(String message, String retentionPolicy) {
        super(message);
        this.retentionPolicy = retentionPolicy;
        this.cutoffDate = null;
    }

    public DataRetentionException(String message, String retentionPolicy, java.time.LocalDate cutoffDate) {
        super(message);
        this.retentionPolicy = retentionPolicy;
        this.cutoffDate = cutoffDate;
    }

    public DataRetentionException(String retentionPolicy, java.time.LocalDate cutoffDate) {
        super(String.format("Data retention policy '%s' prevents access to data before %s",
                retentionPolicy, cutoffDate));
        this.retentionPolicy = retentionPolicy;
        this.cutoffDate = cutoffDate;
    }

    public DataRetentionException(String message, Throwable cause) {
        super(message, cause);
        this.retentionPolicy = null;
        this.cutoffDate = null;
    }

    // Getters for additional context
    public String getRetentionPolicy() { return retentionPolicy; }
    public java.time.LocalDate getCutoffDate() { return cutoffDate; }
}