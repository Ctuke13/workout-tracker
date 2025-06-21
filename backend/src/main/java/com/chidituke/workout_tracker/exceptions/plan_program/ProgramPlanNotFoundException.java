package com.chidituke.workout_tracker.exceptions.plan_program;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a ProgramPlan is not found
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProgramPlanNotFoundException extends RuntimeException {

    private final Long programPlanId;
    private final Long programId;

    public ProgramPlanNotFoundException(String message) {
        super(message);
        this.programPlanId = null;
        this.programId = null;
    }

    public ProgramPlanNotFoundException(Long programPlanId) {
        super("Program plan not found with id: " + programPlanId);
        this.programPlanId = programPlanId;
        this.programId = null;
    }

    public ProgramPlanNotFoundException(Long programId, String context) {
        super("Program plan not found for program " + programId + ": " + context);
        this.programPlanId = null;
        this.programId = programId;
    }

    public Long getProgramPlanId() {
        return programPlanId;
    }

    public Long getProgramId() {
        return programId;
    }
}