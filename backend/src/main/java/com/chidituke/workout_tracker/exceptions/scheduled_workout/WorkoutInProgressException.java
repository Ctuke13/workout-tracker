package com.chidituke.workout_tracker.exceptions.scheduled_workout;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when trying to start a workout while another is in progress
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class WorkoutInProgressException extends RuntimeException {

    private final Long inProgressWorkoutId;

    public WorkoutInProgressException(String message) {
        super(message);
        this.inProgressWorkoutId = null;
    }

    public WorkoutInProgressException() {
        super("Cannot start new workout while another workout is in progress");
        this.inProgressWorkoutId = null;
    }

    public WorkoutInProgressException(String message, Long inProgressWorkoutId) {
        super(message);
        this.inProgressWorkoutId = inProgressWorkoutId;
    }

    public WorkoutInProgressException(Long inProgressWorkoutId) {
        super("Cannot start new workout. Workout with ID " + inProgressWorkoutId + " is already in progress");
        this.inProgressWorkoutId = inProgressWorkoutId;
    }

    public WorkoutInProgressException(String message, Throwable cause) {
        super(message, cause);
        this.inProgressWorkoutId = null;
    }

    // Getter for additional context
    public Long getInProgressWorkoutId() { return inProgressWorkoutId; }
}
