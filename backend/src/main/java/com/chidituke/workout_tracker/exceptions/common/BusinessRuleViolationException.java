package com.chidituke.workout_tracker.exceptions.common;

/**
 * Thrown when an operation violates business rules
 */
public class BusinessRuleViolationException extends WorkoutTrackerException {

    public BusinessRuleViolationException(String rule) {
        super(String.format("Business rule violation: %s", rule));
    }

    public BusinessRuleViolationException(String rule, String details) {
        super(String.format("Business rule violation: %s. Details: %s", rule, details));
    }
}