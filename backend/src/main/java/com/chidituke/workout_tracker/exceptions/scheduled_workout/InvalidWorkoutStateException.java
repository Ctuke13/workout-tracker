package com.chidituke.workout_tracker.exceptions.scheduled_workout;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when scheduled workout state is invalid for the requested operation
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidWorkoutStateException extends RuntimeException {

    private final String currentState;
    private final String requiredState;
    private final String operation;

    // Basic constructor with just message
    public InvalidWorkoutStateException(String message) {
        super(message);
        this.currentState = null;
        this.requiredState = null;
        this.operation = null;
    }

    // Constructor for operation-based exceptions (auto-generates message)
    public InvalidWorkoutStateException(String operation, String currentState, String requiredState) {
        super(String.format("Cannot perform '%s' operation. Current state: %s, Required state: %s",
                operation, currentState, requiredState));
        this.operation = operation;
        this.currentState = currentState;
        this.requiredState = requiredState;
    }

    // Constructor with custom message and context (reordered parameters to avoid conflict)
    public InvalidWorkoutStateException(String message, Throwable cause, String currentState, String requiredState) {
        super(message, cause);
        this.currentState = currentState;
        this.requiredState = requiredState;
        this.operation = null;
    }

    // Constructor with message and cause only
    public InvalidWorkoutStateException(String message, Throwable cause) {
        super(message, cause);
        this.currentState = null;
        this.requiredState = null;
        this.operation = null;
    }

    // Static factory methods for better clarity (alternative approach)
    public static InvalidWorkoutStateException withOperation(String operation, String currentState, String requiredState) {
        return new InvalidWorkoutStateException(operation, currentState, requiredState);
    }

    public static InvalidWorkoutStateException withCustomMessage(String message, String currentState, String requiredState) {
        InvalidWorkoutStateException exception = new InvalidWorkoutStateException(message);
        // We can't set final fields after construction, so this approach has limitations
        return exception;
    }

    // Getters for additional context
    public String getCurrentState() {
        return currentState;
    }

    public String getRequiredState() {
        return requiredState;
    }

    public String getOperation() {
        return operation;
    }
}