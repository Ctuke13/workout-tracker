package com.chidituke.workout_tracker.exceptions.user;

import com.chidituke.workout_tracker.exceptions.common.WorkoutTrackerException;

/**
 * Thrown when a requested user cannot be found
 */
public class UserNotFoundException extends WorkoutTrackerException {

    public UserNotFoundException(Long id) {
        super(String.format("User with ID %d not found", id));
    }

    public UserNotFoundException(String identifier) {
        super(String.format("User '%s' not found", identifier));
    }
}