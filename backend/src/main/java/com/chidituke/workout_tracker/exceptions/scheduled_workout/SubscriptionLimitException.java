package com.chidituke.workout_tracker.exceptions.scheduled_workout;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when subscription limits prevent scheduling
 * (e.g., free users trying to schedule beyond 7 days)
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class SubscriptionLimitException extends RuntimeException {

    private final String feature;
    private final String requiredTier;
    private final String currentTier;

    public SubscriptionLimitException(String message) {
        super(message);
        this.feature = null;
        this.requiredTier = null;
        this.currentTier = null;
    }

    public SubscriptionLimitException(String feature, String requiredTier) {
        super(String.format("Feature '%s' requires %s subscription or higher", feature, requiredTier));
        this.feature = feature;
        this.requiredTier = requiredTier;
        this.currentTier = null;
    }

    public SubscriptionLimitException(String feature, String requiredTier, String currentTier) {
        super(String.format("Feature '%s' requires %s subscription. Current tier: %s",
                feature, requiredTier, currentTier));
        this.feature = feature;
        this.requiredTier = requiredTier;
        this.currentTier = currentTier;
    }

    public SubscriptionLimitException(String message, Throwable cause) {
        super(message, cause);
        this.feature = null;
        this.requiredTier = null;
        this.currentTier = null;
    }

    // Getters for additional context
    public String getFeature() { return feature; }
    public String getRequiredTier() { return requiredTier; }
    public String getCurrentTier() { return currentTier; }
}