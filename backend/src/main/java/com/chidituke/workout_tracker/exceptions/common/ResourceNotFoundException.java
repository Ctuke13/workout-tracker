package com.chidituke.workout_tracker.exceptions.common;

/**
 * Thrown when a requested resource is not found
 */
public class ResourceNotFoundException extends WorkoutTrackerException {

    public ResourceNotFoundException(String resourceType, Long id) {
        super(String.format("%s with ID %d not found", resourceType, id));
    }

    public ResourceNotFoundException(String resourceType, String identifier) {
        super(String.format("%s with identifier '%s' not found", resourceType, identifier));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}