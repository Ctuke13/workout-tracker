package com.chidituke.workout_tracker.exceptions.exercise;

import com.chidituke.workout_tracker.exceptions.common.WorkoutTrackerException;

/**
 * Thrown when an exercise-related operation fails
 */
public class ExerciseException extends WorkoutTrackerException {

    public ExerciseException(String message) {
        super(message);
    }

    public ExerciseException(String message, Throwable cause) {
        super(message, cause);
    }
}
