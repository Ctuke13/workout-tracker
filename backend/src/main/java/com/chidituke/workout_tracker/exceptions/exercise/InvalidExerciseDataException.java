package com.chidituke.workout_tracker.exceptions.exercise;

public class InvalidExerciseDataException extends ExerciseException {
    public InvalidExerciseDataException(String field, String reason) {
        super(String.format("Invalid exercise data for field '%s': %s", field, reason));
    }
}