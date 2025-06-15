package com.chidituke.workout_tracker.exceptions;

/**
 * Base exception for all workout tracker business logic exceptions
 */
//public abstract class WorkoutTrackerException extends RuntimeException {
//
//    protected WorkoutTrackerException(String message) {
//        super(message);
//    }
//
//    protected WorkoutTrackerException(String message, Throwable cause) {
//        super(message, cause);
//    }
//}

/**
 * Thrown when a requested resource is not found
 */
//public class ResourceNotFoundException extends WorkoutTrackerException {
//
//    public ResourceNotFoundException(String resourceType, Long id) {
//        super(String.format("%s with ID %d not found", resourceType, id));
//    }
//
//    public ResourceNotFoundException(String resourceType, String identifier) {
//        super(String.format("%s with identifier '%s' not found", resourceType, identifier));
//    }
//
//    public ResourceNotFoundException(String message) {
//        super(message);
//    }
//}

/**
 * Thrown when a user attempts an unauthorized operation
 */
public class UnauthorizedOperationException extends WorkoutTrackerException {

    public UnauthorizedOperationException(String operation) {
        super(String.format("User is not authorized to perform operation: %s", operation));
    }

    public UnauthorizedOperationException(String operation, String reason) {
        super(String.format("User is not authorized to perform operation: %s. Reason: %s", operation, reason));
    }

    public UnauthorizedOperationException(String message, boolean isCustomMessage) {
        super(message);
    }

    public static com.chidituke.workout_tracker.exceptions.common.UnauthorizedOperationException forResource(String operation, String resourceType, Long resourceId) {
        return new com.chidituke.workout_tracker.exceptions.common.UnauthorizedOperationException(
                String.format("User is not authorized to %s %s with ID %d", operation, resourceType, resourceId),
                true
        );
    }

    public static com.chidituke.workout_tracker.exceptions.common.UnauthorizedOperationException requiresRole(String operation, String requiredRole) {
        return new com.chidituke.workout_tracker.exceptions.common.UnauthorizedOperationException(
                operation,
                String.format("This operation requires %s role", requiredRole)
        );
    }

    public static com.chidituke.workout_tracker.exceptions.common.UnauthorizedOperationException notOwner(String operation, String resourceType) {
        return new com.chidituke.workout_tracker.exceptions.common.UnauthorizedOperationException(
                operation,
                String.format("User can only %s their own %s", operation, resourceType)
        );
    }
}

/**
 * Thrown when an exercise-related operation fails
 */
public class ExerciseException extends WorkoutTrackerException {

    public ExerciseException(String message) {
        super(message);
    }

    public ExerciseException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Thrown when a user-related operation fails
 */
public class UserException extends WorkoutTrackerException {

    public UserException(String message) {
        super(message);
    }

    public UserException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Thrown when a workout-related operation fails
 */
public class WorkoutException extends WorkoutTrackerException {

    public WorkoutException(String message) {
        super(message);
    }

    public WorkoutException(String message, Throwable cause) {
        super(message, cause);
    }
}

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

/**
 * Thrown when a resource already exists and duplicates are not allowed
 */
public class DuplicateResourceException extends WorkoutTrackerException {

    public DuplicateResourceException(String resourceType, String identifier) {
        super(String.format("%s with identifier '%s' already exists", resourceType, identifier));
    }

    public DuplicateResourceException(String message) {
        super(message);
    }
}

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

/**
 * Thrown when a file upload or processing operation fails
 */
public class FileProcessingException extends WorkoutTrackerException {

    public FileProcessingException(String message) {
        super(message);
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}