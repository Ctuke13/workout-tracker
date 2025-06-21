package com.chidituke.workout_tracker.exceptions.scheduled_workout;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there are conflicts in scheduling
 * (e.g., double-booking, time conflicts)
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class SchedulingConflictException extends RuntimeException {

    private final java.time.LocalDate conflictDate;
    private final String conflictType;

    public SchedulingConflictException(String message) {
        super(message);
        this.conflictDate = null;
        this.conflictType = null;
    }

    public SchedulingConflictException(String message, java.time.LocalDate conflictDate) {
        super(message);
        this.conflictDate = conflictDate;
        this.conflictType = null;
    }

    public SchedulingConflictException(String message, java.time.LocalDate conflictDate, String conflictType) {
        super(message);
        this.conflictDate = conflictDate;
        this.conflictType = conflictType;
    }

    public SchedulingConflictException(java.time.LocalDate conflictDate, String conflictType) {
        super(String.format("Scheduling conflict on %s: %s", conflictDate, conflictType));
        this.conflictDate = conflictDate;
        this.conflictType = conflictType;
    }

    public SchedulingConflictException(String message, Throwable cause) {
        super(message, cause);
        this.conflictDate = null;
        this.conflictType = null;
    }

    // Getters for additional context
    public java.time.LocalDate getConflictDate() { return conflictDate; }
    public String getConflictType() { return conflictType; }
}