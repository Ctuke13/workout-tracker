package com.chidituke.workout_tracker.exceptions.workout;

/**
 * Exception thrown when a workout log is not found
 */
public class WorkoutLogNotFoundException extends RuntimeException {

    public WorkoutLogNotFoundException(String message) {
        super(message);
    }

    public WorkoutLogNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static WorkoutLogNotFoundException withId(Long id) {
        return new WorkoutLogNotFoundException("Workout log not found with ID: " + id);
    }
}