package com.chidituke.workout_tracker.exceptions.scheduled_workout;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a workout operation cannot be performed because a workout is currently in progress.
 * This covers scenarios like:
 * - Starting a new workout while another is in progress
 * - Deleting a workout that is currently in progress
 * - Modifying a workout that is currently in progress
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class WorkoutInProgressException extends RuntimeException {

    private final Long inProgressWorkoutId;
    private final String operation;

    // ==================== BASIC CONSTRUCTORS ====================

    /**
     * Default constructor with generic message
     */
    public WorkoutInProgressException() {
        super("Cannot perform operation while a workout is in progress");
        this.inProgressWorkoutId = null;
        this.operation = null;
    }

    /**
     * Constructor with custom message only
     */
    public WorkoutInProgressException(String message) {
        super(message);
        this.inProgressWorkoutId = null;
        this.operation = null;
    }

    /**
     * Constructor with message and cause
     */
    public WorkoutInProgressException(String message, Throwable cause) {
        super(message, cause);
        this.inProgressWorkoutId = null;
        this.operation = null;
    }

    // ==================== CONTEXT-AWARE CONSTRUCTORS ====================

    /**
     * Constructor with workout ID (auto-generates appropriate message)
     */
    public WorkoutInProgressException(Long inProgressWorkoutId) {
        super("Cannot perform operation. Workout with ID " + inProgressWorkoutId + " is currently in progress");
        this.inProgressWorkoutId = inProgressWorkoutId;
        this.operation = null;
    }

    /**
     * Constructor with custom message and workout ID
     */
    public WorkoutInProgressException(String message, Long inProgressWorkoutId) {
        super(message);
        this.inProgressWorkoutId = inProgressWorkoutId;
        this.operation = null;
    }

    // ==================== STATIC FACTORY METHODS ====================

    /**
     * Factory method for starting workout scenarios
     */
    public static WorkoutInProgressException forStarting() {
        return new WorkoutInProgressException("Cannot start new workout while another workout is in progress");
    }

    /**
     * Factory method for starting workout scenarios with specific workout ID
     */
    public static WorkoutInProgressException forStarting(Long inProgressWorkoutId) {
        return new WorkoutInProgressException(
                "Cannot start new workout. Workout with ID " + inProgressWorkoutId + " is already in progress",
                inProgressWorkoutId);
    }

    /**
     * Factory method for deletion scenarios
     */
    public static WorkoutInProgressException forDeletion(Long workoutId) {
        WorkoutInProgressException exception = new WorkoutInProgressException(
                "Cannot delete workout with ID " + workoutId + " because it is currently in progress",
                workoutId);
        return new WorkoutInProgressException(exception.getMessage(), workoutId) {
            @Override
            public String getOperation() {
                return "delete";
            }
        };
    }

    /**
     * Factory method for modification scenarios
     */
    public static WorkoutInProgressException forModification(Long workoutId) {
        return new WorkoutInProgressException(
                "Cannot modify workout with ID " + workoutId + " because it is currently in progress",
                workoutId);
    }

    /**
     * Factory method for rescheduling scenarios
     */
    public static WorkoutInProgressException forRescheduling(Long workoutId) {
        return new WorkoutInProgressException(
                "Cannot reschedule workout with ID " + workoutId + " because it is currently in progress",
                workoutId);
    }

    /**
     * Factory method for cancellation scenarios
     */
    public static WorkoutInProgressException forCancellation(Long workoutId) {
        return new WorkoutInProgressException(
                "Cannot cancel workout with ID " + workoutId + " because it is currently in progress",
                workoutId);
    }

    /**
     * Generic factory method for any operation
     */
    public static WorkoutInProgressException forOperation(String operation, Long workoutId) {
        return new WorkoutInProgressException(
                String.format("Cannot %s workout with ID %d because it is currently in progress",
                        operation, workoutId),
                workoutId);
    }

    // ==================== GETTERS ====================

    /**
     * Get the ID of the workout that is currently in progress
     * @return the workout ID, or null if not specified
     */
    public Long getInProgressWorkoutId() {
        return inProgressWorkoutId;
    }

    /**
     * Get the operation that was being attempted
     * @return the operation name, or null if not specified
     */
    public String getOperation() {
        return operation;
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Check if this exception has workout ID context
     */
    public boolean hasWorkoutId() {
        return inProgressWorkoutId != null;
    }

    /**
     * Check if this exception has operation context
     */
    public boolean hasOperation() {
        return operation != null && !operation.trim().isEmpty();
    }

    /**
     * Get a user-friendly description of the error
     */
    public String getUserFriendlyMessage() {
        if (hasWorkoutId() && hasOperation()) {
            return String.format("Cannot %s this workout because it's currently in progress", operation);
        } else if (hasWorkoutId()) {
            return "This workout is currently in progress and cannot be modified";
        } else {
            return "A workout is currently in progress. Please complete it before starting a new operation";
        }
    }
}