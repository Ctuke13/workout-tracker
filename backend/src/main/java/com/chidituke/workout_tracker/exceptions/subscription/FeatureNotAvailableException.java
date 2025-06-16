package com.chidituke.workout_tracker.exceptions.subscription;

public class FeatureNotAvailableException extends SubscriptionException {
    public FeatureNotAvailableException(String feature, String requiredPlan) {
        super(String.format("Feature '%s' requires %s subscription", feature, requiredPlan));
    }
}