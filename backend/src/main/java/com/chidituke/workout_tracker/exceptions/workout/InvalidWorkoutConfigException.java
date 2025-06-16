package com.chidituke.workout_tracker.exceptions.workout;

public class InvalidWorkoutConfigException extends WorkoutException {
    public InvalidWorkoutConfigException(String reason) {
        super(String.format("Invalid workout configuration: %s", reason));
    }
}