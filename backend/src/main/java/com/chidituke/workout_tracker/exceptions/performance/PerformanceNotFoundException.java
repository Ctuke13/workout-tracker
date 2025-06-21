package com.chidituke.workout_tracker.exceptions.performance;

/**
 * Exception thrown when a performance record is not found
 */
public class PerformanceNotFoundException extends RuntimeException {

    public PerformanceNotFoundException(String message) {
        super(message);
    }

    public PerformanceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static PerformanceNotFoundException withId(Long id) {
        return new PerformanceNotFoundException("Performance record not found with ID: " + id);
    }
}