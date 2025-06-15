package com.chidituke.workout_tracker.exceptions.workout;


import com.chidituke.workout_tracker.exceptions.common.WorkoutTrackerException;

/**
 * Thrown when a workout-related operation fails
 */
public class WorkoutException extends WorkoutTrackerException {

    public WorkoutException(String message) {
        super(message);
    }

    public WorkoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
