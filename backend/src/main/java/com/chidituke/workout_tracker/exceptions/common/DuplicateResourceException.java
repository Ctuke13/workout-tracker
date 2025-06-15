package com.chidituke.workout_tracker.exceptions.common;

/**
 * Thrown when a resource already exists and duplicates are not allowed
 */
public class DuplicateResourceException extends WorkoutTrackerException {

    public DuplicateResourceException(String resourceType, String identifier) {
        super(String.format("%s with identifier '%s' already exists", resourceType, identifier));
    }

    public DuplicateResourceException(String message) {
        super(message);
    }
}