package com.chidituke.workout_tracker.exceptions.common;

/**
 * Thrown when a file upload or processing operation fails
 */
public class FileProcessingException extends WorkoutTrackerException {

    public FileProcessingException(String message) {
        super(message);
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
