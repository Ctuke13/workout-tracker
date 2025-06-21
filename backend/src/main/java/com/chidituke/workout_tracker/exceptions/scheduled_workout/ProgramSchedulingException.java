package com.chidituke.workout_tracker.exceptions.scheduled_workout;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when workout scheduling is not allowed due to program constraints
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ProgramSchedulingException extends RuntimeException {

    private final Long programId;
    private final String programName;

    public ProgramSchedulingException(String message) {
        super(message);
        this.programId = null;
        this.programName = null;
    }

    public ProgramSchedulingException(String message, Long programId) {
        super(message);
        this.programId = programId;
        this.programName = null;
    }

    public ProgramSchedulingException(String message, Long programId, String programName) {
        super(message);
        this.programId = programId;
        this.programName = programName;
    }

    public ProgramSchedulingException(String message, Throwable cause) {
        super(message, cause);
        this.programId = null;
        this.programName = null;
    }

    // Getters for additional context
    public Long getProgramId() { return programId; }
    public String getProgramName() { return programName; }
}
