package com.chidituke.workout_tracker.exceptions.workout;

public class WorkoutInProgressException extends WorkoutException {
    public WorkoutInProgressException(Long workoutId) {
        super(String.format("Workout %d is currently in progress and cannot be modified", workoutId));
    }
}