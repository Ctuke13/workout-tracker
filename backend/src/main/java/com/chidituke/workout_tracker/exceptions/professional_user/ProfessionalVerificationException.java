package com.chidituke.workout_tracker.exceptions.professional_user;

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
}
