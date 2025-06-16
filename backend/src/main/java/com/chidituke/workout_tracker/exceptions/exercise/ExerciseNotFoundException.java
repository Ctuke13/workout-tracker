package com.chidituke.workout_tracker.exceptions.exercise;

/**
 * Thrown when a requested exercise cannot be found
 */
public class ExerciseNotFoundException extends ExerciseException {

    public ExerciseNotFoundException(Long id) {
        super(String.format("Exercise with ID %d not found", id));
    }

    public ExerciseNotFoundException(String name) {
        super(String.format("Exercise '%s' not found", name));
    }
}