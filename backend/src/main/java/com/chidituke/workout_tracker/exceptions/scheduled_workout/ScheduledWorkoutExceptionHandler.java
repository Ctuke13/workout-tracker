package com.chidituke.workout_tracker.exceptions.scheduled_workout;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Global exception handler for scheduled workout exceptions
 */
@RestControllerAdvice
public class ScheduledWorkoutExceptionHandler {

    @ExceptionHandler(ScheduledWorkoutNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleScheduledWorkoutNotFound(ScheduledWorkoutNotFoundException ex) {
        return createErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), "SCHEDULED_WORKOUT_NOT_FOUND");
    }

    @ExceptionHandler(SchedulingConstraintException.class)
    public ResponseEntity<Map<String, Object>> handleSchedulingConstraint(SchedulingConstraintException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "SCHEDULING_CONSTRAINT_VIOLATION");
    }

    @ExceptionHandler(SubscriptionLimitException.class)
    public ResponseEntity<Map<String, Object>> handleSubscriptionLimit(SubscriptionLimitException ex) {
        Map<String, Object> response = createErrorResponseMap(HttpStatus.FORBIDDEN, ex.getMessage(), "SUBSCRIPTION_LIMIT_EXCEEDED");

        // Add additional context if available
        if (ex.getFeature() != null) response.put("feature", ex.getFeature());
        if (ex.getRequiredTier() != null) response.put("requiredTier", ex.getRequiredTier());
        if (ex.getCurrentTier() != null) response.put("currentTier", ex.getCurrentTier());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(WorkoutInProgressException.class)
    public ResponseEntity<Map<String, Object>> handleWorkoutInProgress(WorkoutInProgressException ex) {
        Map<String, Object> response = createErrorResponseMap(HttpStatus.CONFLICT, ex.getMessage(), "WORKOUT_IN_PROGRESS");

        if (ex.getInProgressWorkoutId() != null) {
            response.put("inProgressWorkoutId", ex.getInProgressWorkoutId());
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(UnauthorizedScheduledWorkoutAccessException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedAccess(UnauthorizedScheduledWorkoutAccessException ex) {
        Map<String, Object> response = createErrorResponseMap(HttpStatus.FORBIDDEN, ex.getMessage(), "UNAUTHORIZED_ACCESS");

        if (ex.getUsername() != null) response.put("username", ex.getUsername());
        if (ex.getScheduledWorkoutId() != null) response.put("scheduledWorkoutId", ex.getScheduledWorkoutId());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(SchedulingConflictException.class)
    public ResponseEntity<Map<String, Object>> handleSchedulingConflict(SchedulingConflictException ex) {
        Map<String, Object> response = createErrorResponseMap(HttpStatus.CONFLICT, ex.getMessage(), "SCHEDULING_CONFLICT");

        if (ex.getConflictDate() != null) response.put("conflictDate", ex.getConflictDate());
        if (ex.getConflictType() != null) response.put("conflictType", ex.getConflictType());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ProgramSchedulingException.class)
    public ResponseEntity<Map<String, Object>> handleProgramScheduling(ProgramSchedulingException ex) {
        Map<String, Object> response = createErrorResponseMap(HttpStatus.BAD_REQUEST, ex.getMessage(), "PROGRAM_SCHEDULING_ERROR");

        if (ex.getProgramId() != null) response.put("programId", ex.getProgramId());
        if (ex.getProgramName() != null) response.put("programName", ex.getProgramName());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(InvalidWorkoutStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidWorkoutState(InvalidWorkoutStateException ex) {
        Map<String, Object> response = createErrorResponseMap(HttpStatus.BAD_REQUEST, ex.getMessage(), "INVALID_WORKOUT_STATE");

        if (ex.getCurrentState() != null) response.put("currentState", ex.getCurrentState());
        if (ex.getRequiredState() != null) response.put("requiredState", ex.getRequiredState());
        if (ex.getOperation() != null) response.put("operation", ex.getOperation());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DataRetentionException.class)
    public ResponseEntity<Map<String, Object>> handleDataRetention(DataRetentionException ex) {
        Map<String, Object> response = createErrorResponseMap(HttpStatus.BAD_REQUEST, ex.getMessage(), "DATA_RETENTION_VIOLATION");

        if (ex.getRetentionPolicy() != null) response.put("retentionPolicy", ex.getRetentionPolicy());
        if (ex.getCutoffDate() != null) response.put("cutoffDate", ex.getCutoffDate());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Helper methods
    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message, String code) {
        return ResponseEntity.status(status).body(createErrorResponseMap(status, message, code));
    }

    private Map<String, Object> createErrorResponseMap(HttpStatus status, String message, String code) {
        return Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "code", code
        );
    }
}