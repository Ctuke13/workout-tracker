package com.chidituke.workout_tracker.exceptions.subscription;

/**
 * Exception thrown when user exceeds their subscription tier limits
 * (e.g., daily exercise limit, advance scheduling limit)
 */
public class SubscriptionLimitExceededException extends SubscriptionException {

    public SubscriptionLimitExceededException(String message) {
        super(message);
    }

    /**
     * Constructor for daily exercise limit exceeded
     */
    public static SubscriptionLimitExceededException dailyExerciseLimit(int currentCount, int limit, String tier) {
        return new SubscriptionLimitExceededException(
                String.format("Daily exercise limit exceeded. You have %d/%d exercises scheduled for today. " +
                        "Upgrade from %s for higher limits.", currentCount, limit, tier)
        );
    }

    /**
     * Constructor for advance scheduling limit exceeded
     */
    public static SubscriptionLimitExceededException advanceSchedulingLimit(int days, String tier) {
        return new SubscriptionLimitExceededException(
                String.format("%s users can only schedule workouts %d days in advance. " +
                        "Upgrade to PLUS to schedule up to 30 days ahead!", tier, days)
        );
    }

    /**
     * Constructor for workout plan access denied
     */
    public static SubscriptionLimitExceededException workoutPlanAccess(String userTier, String requiredTier) {
        return new SubscriptionLimitExceededException(
                String.format("Your %s subscription doesn't include access to %s tier workout plans. " +
                        "Upgrade to access this plan.", userTier, requiredTier)
        );
    }

    /**
     * Constructor for workout plan exercise count limit
     */
    public static SubscriptionLimitExceededException workoutPlanExerciseLimit(int exerciseCount, int dailyLimit, String tier) {
        if ("FREE".equals(tier)) {
            return new SubscriptionLimitExceededException(
                    String.format("Adding this workout plan (%d exercises) would exceed your daily limit of %d exercises. " +
                                    "Upgrade to PLUS for access to complete workout plans with up to 15 exercises per day!",
                            exerciseCount, dailyLimit)
            );
        } else {
            return new SubscriptionLimitExceededException(
                    String.format("Adding this workout plan would exceed your daily limit of %d exercises", dailyLimit)
            );
        }
    }
}