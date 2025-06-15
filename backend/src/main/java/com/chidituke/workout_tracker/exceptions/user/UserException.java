package com.chidituke.workout_tracker.exceptions.user;

import com.chidituke.workout_tracker.exceptions.common.WorkoutTrackerException;

/**
 * Thrown when a user-related operation fails
 */
public class UserException extends WorkoutTrackerException {

    public UserException(String message) {
        super(message);
    }

    public UserException(String message, Throwable cause) {
        super(message, cause);
    }
}