package com.chidituke.workout_tracker.exceptions.auth;

import com.chidituke.workout_tracker.exceptions.common.WorkoutTrackerException;

public class AuthException extends WorkoutTrackerException {
    public AuthException(String message) {
        super(message);
    }
}
