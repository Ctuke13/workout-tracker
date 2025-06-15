package com.chidituke.workout_tracker.exceptions.common;

/**
 * Base exception for all workout tracker business logic exceptions
 */
public abstract class WorkoutTrackerException extends RuntimeException {

    protected WorkoutTrackerException(String message) {
        super(message);
    }

    protected WorkoutTrackerException(String message, Throwable cause) {
        super(message, cause);
    }
}