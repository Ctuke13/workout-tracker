package com.chidituke.workout_tracker.exceptions.scheduled_workout;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when user tries to access a scheduled workout they don't own
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedScheduledWorkoutAccessException extends RuntimeException {

    private final String username;
    private final Long scheduledWorkoutId;

    public UnauthorizedScheduledWorkoutAccessException(String message) {
        super(message);
        this.username = null;
        this.scheduledWorkoutId = null;
    }

    public UnauthorizedScheduledWorkoutAccessException(Long scheduledWorkoutId, String username) {
        super(String.format("User '%s' does not have access to scheduled workout with id: %d",
                username, scheduledWorkoutId));
        this.username = username;
        this.scheduledWorkoutId = scheduledWorkoutId;
    }

    public UnauthorizedScheduledWorkoutAccessException(String message, String username, Long scheduledWorkoutId) {
        super(message);
        this.username = username;
        this.scheduledWorkoutId = scheduledWorkoutId;
    }

    public UnauthorizedScheduledWorkoutAccessException(String message, Throwable cause) {
        super(message, cause);
        this.username = null;
        this.scheduledWorkoutId = null;
    }

    // Getters for additional context
    public String getUsername() { return username; }
    public Long getScheduledWorkoutId() { return scheduledWorkoutId; }
}