package com.chidituke.workout_tracker.exceptions.scheduled_workout;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a scheduled workout cannot be found
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ScheduledWorkoutNotFoundException extends RuntimeException {

    public ScheduledWorkoutNotFoundException(Long id) {
        super("Scheduled workout not found with id: " + id);
    }

    public ScheduledWorkoutNotFoundException(String message) {
        super(message);
    }

    public ScheduledWorkoutNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}