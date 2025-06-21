package com.chidituke.workout_tracker.exceptions.scheduled_workout;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when scheduling constraints are violated
 * (e.g., scheduling in the past, daily limits exceeded)
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SchedulingConstraintException extends RuntimeException {

    public SchedulingConstraintException(String message) {
        super(message);
    }

    public SchedulingConstraintException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchedulingConstraintException(String constraint, String value) {
        super(String.format("Scheduling constraint violated: %s (value: %s)", constraint, value));
    }
}