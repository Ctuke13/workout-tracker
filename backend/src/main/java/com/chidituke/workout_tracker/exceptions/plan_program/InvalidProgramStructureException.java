package com.chidituke.workout_tracker.exceptions.plan_program;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when program structure is invalid
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidProgramStructureException extends RuntimeException {

    private final String structureType;
    private final String validationRule;

    public InvalidProgramStructureException(String message) {
        super(message);
        this.structureType = null;
        this.validationRule = null;
    }

    public InvalidProgramStructureException(String message, String structureType, String validationRule) {
        super(message);
        this.structureType = structureType;
        this.validationRule = validationRule;
    }

    public InvalidProgramStructureException(String message, Throwable cause) {
        super(message, cause);
        this.structureType = null;
        this.validationRule = null;
    }

    public String getStructureType() {
        return structureType;
    }

    public String getValidationRule() {
        return validationRule;
    }
}