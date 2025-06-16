package com.chidituke.workout_tracker.exceptions.common;

import com.chidituke.workout_tracker.exceptions.common.WorkoutTrackerException;

/**
 * Thrown when a user attempts an operation they don't have permission to perform.
 * This is different from authentication (who you are) - this is about authorization (what you can do).
 */
public class UnauthorizedOperationException extends WorkoutTrackerException {

    /**
     * Creates exception with operation name
     */
    public UnauthorizedOperationException(String operation) {
        super(String.format("User is not authorized to perform operation: %s", operation));
    }

    /**
     * Creates exception with operation name and specific reason
     */
    public UnauthorizedOperationException(String operation, String reason) {
        super(String.format("User is not authorized to perform operation: %s. Reason: %s", operation, reason));
    }

    /**
     * Creates exception with custom message
     */
    public UnauthorizedOperationException(String message, boolean isCustomMessage) {
        super(message);
    }

    /**
     * Creates exception for resource-specific operations
     */
    public static UnauthorizedOperationException forResource(String operation, String resourceType, Long resourceId) {
        return new UnauthorizedOperationException(
                String.format("User is not authorized to %s %s with ID %d", operation, resourceType, resourceId),
                true
        );
    }

    /**
     * Creates exception for role-based restrictions
     */
    public static UnauthorizedOperationException requiresRole(String operation, String requiredRole) {
        return new UnauthorizedOperationException(
                operation,
                String.format("This operation requires %s role", requiredRole)
        );
    }

    /**
     * Creates exception for ownership restrictions
     */
    public static UnauthorizedOperationException notOwner(String operation, String resourceType) {
        return new UnauthorizedOperationException(
                operation,
                String.format("User can only %s their own %s", operation, resourceType)
        );
    }
}