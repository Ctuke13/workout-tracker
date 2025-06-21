package com.chidituke.workout_tracker.exceptions.workout_plan;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a WorkoutPlan is not found
 * Returns HTTP 404 NOT FOUND status
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class WorkoutPlanNotFoundException extends RuntimeException {

    private final Long workoutPlanId;
    private final String identifier;

    /**
     * Constructor with workout plan ID
     */
    public WorkoutPlanNotFoundException(Long workoutPlanId) {
        super("Workout plan not found with ID: " + workoutPlanId);
        this.workoutPlanId = workoutPlanId;
        this.identifier = String.valueOf(workoutPlanId);
    }

    /**
     * Constructor with custom message
     */
    public WorkoutPlanNotFoundException(String message) {
        super(message);
        this.workoutPlanId = null;
        this.identifier = null;
    }

    /**
     * Constructor with workout plan ID and custom message
     */
    public WorkoutPlanNotFoundException(Long workoutPlanId, String message) {
        super(message);
        this.workoutPlanId = workoutPlanId;
        this.identifier = String.valueOf(workoutPlanId);
    }

    /**
     * Constructor with string identifier (for name-based lookups)
     */
    public WorkoutPlanNotFoundException(String identifier, String message) {
        super(message);
        this.workoutPlanId = null;
        this.identifier = identifier;
    }

    /**
     * Constructor with cause
     */
    public WorkoutPlanNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.workoutPlanId = null;
        this.identifier = null;
    }

    /**
     * Static factory method for ID-based lookup
     */
    public static WorkoutPlanNotFoundException withId(Long workoutPlanId) {
        return new WorkoutPlanNotFoundException(workoutPlanId);
    }

    /**
     * Static factory method for name-based lookup
     */
    public static WorkoutPlanNotFoundException withName(String workoutName) {
        return new WorkoutPlanNotFoundException(workoutName, "Workout plan not found with name: " + workoutName);
    }

    /**
     * Static factory method for user access context
     */
    public static WorkoutPlanNotFoundException forUser(Long workoutPlanId, Long userId) {
        return new WorkoutPlanNotFoundException(workoutPlanId,
                "Workout plan with ID " + workoutPlanId + " not found or not accessible by user " + userId);
    }

    /**
     * Static factory method for public workout plans
     */
    public static WorkoutPlanNotFoundException publicPlan(Long workoutPlanId) {
        return new WorkoutPlanNotFoundException(workoutPlanId,
                "Public workout plan not found with ID: " + workoutPlanId);
    }

    /**
     * Static factory method for subscription tier restrictions
     */
    public static WorkoutPlanNotFoundException subscriptionRestricted(Long workoutPlanId, String userTier, String requiredTier) {
        return new WorkoutPlanNotFoundException(workoutPlanId,
                "Workout plan " + workoutPlanId + " requires " + requiredTier + " subscription, but user has " + userTier);
    }

    // Getters
    public Long getWorkoutPlanId() {
        return workoutPlanId;
    }

    public String getIdentifier() {
        return identifier;
    }

    // Helper methods for common usage patterns
    public boolean hasWorkoutPlanId() {
        return workoutPlanId != null;
    }

    public boolean hasIdentifier() {
        return identifier != null && !identifier.trim().isEmpty();
    }
}