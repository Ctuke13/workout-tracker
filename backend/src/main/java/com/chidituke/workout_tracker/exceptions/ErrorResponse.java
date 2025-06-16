package com.chidituke.workout_tracker.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Standardized error response for the Workout Tracker API
 * Provides consistent error formatting across all endpoints
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private Integer status;
    private String error;
    private String message;
    private String path;

    // Optional fields for additional context
    private String traceId;
    private List<ValidationError> validationErrors;
    private Map<String, Object> details;

    // ==================== STATIC FACTORY METHODS ====================

    /**
     * Create simple error response with basic information
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }

    /**
     * Create error response with trace ID for debugging
     */
    public static ErrorResponse of(int status, String error, String message, String path, String traceId) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .traceId(traceId)
                .build();
    }

    /**
     * Create validation error response
     */
    public static ErrorResponse validation(String message, String path, List<ValidationError> validationErrors) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(400)
                .error("Validation Failed")
                .message(message)
                .path(path)
                .validationErrors(validationErrors)
                .build();
    }

    // ==================== VALIDATION ERROR INNER CLASS ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ValidationError {
        private String field;
        private Object rejectedValue;
        private String message;

        public static ValidationError of(String field, Object rejectedValue, String message) {
            return ValidationError.builder()
                    .field(field)
                    .rejectedValue(rejectedValue)
                    .message(message)
                    .build();
        }
    }

    // ==================== CONVENIENCE METHODS ====================

    /**
     * Check if this is a validation error response
     */
    public boolean hasValidationErrors() {
        return validationErrors != null && !validationErrors.isEmpty();
    }

    /**
     * Get the number of validation errors
     */
    public int getValidationErrorCount() {
        return validationErrors != null ? validationErrors.size() : 0;
    }

    /**
     * Add additional details to the error response
     */
    public ErrorResponse withDetail(String key, Object value) {
        if (this.details == null) {
            this.details = new java.util.HashMap<>();
        }
        this.details.put(key, value);
        return this;
    }

    /**
     * Add trace ID for debugging
     */
    public ErrorResponse withTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }
}