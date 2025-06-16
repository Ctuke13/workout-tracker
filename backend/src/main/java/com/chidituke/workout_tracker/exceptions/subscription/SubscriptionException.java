package com.chidituke.workout_tracker.exceptions.subscription;

import com.chidituke.workout_tracker.exceptions.common.WorkoutTrackerException;

public class SubscriptionException extends WorkoutTrackerException {
    public SubscriptionException(String message) {
        super(message);
    }
}