package com.chidituke.workout_tracker.exceptions.user;

import com.chidituke.workout_tracker.exceptions.common.WorkoutTrackerException;

/**
 * Thrown when a professional verification fails
 */
public class ProfessionalVerificationException extends WorkoutTrackerException {

    public ProfessionalVerificationException(String message) {
        super(message);
    }

    public ProfessionalVerificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ProfessionalVerificationException required() {
        return new ProfessionalVerificationException("Professional verification required for this operation");
    }

    public static ProfessionalVerificationException forOperation(String operation) {
        return new ProfessionalVerificationException(String.format("Professional verification required to %s", operation));
    }
}
