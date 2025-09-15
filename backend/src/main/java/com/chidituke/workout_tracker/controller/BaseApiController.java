package com.chidituke.workout_tracker.controller;

import com.chidituke.workout_tracker.exceptions.common.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * Base controller providing consistent response patterns for all API endpoints
 * Simplified version without servlet dependencies for easier compilation
 */
@RestController
public abstract class BaseApiController {

    // ==================== SUCCESS RESPONSE METHODS ====================

    /**
     * Standard success response with data
     */
    protected <T> ResponseEntity<T> ok(T data) {
        return ResponseEntity.ok(data);
    }

    /**
     * Success response for lists (ensures never null)
     */
    protected <T> ResponseEntity<List<T>> okList(List<T> data) {
        return ResponseEntity.ok(data != null ? data : Collections.emptyList());
    }

    /**
     * Success response with custom HTTP status
     */
    protected <T> ResponseEntity<T> success(T data, HttpStatus status) {
        return ResponseEntity.status(status).body(data);
    }

    // ==================== ERROR RESPONSE METHODS ====================

    /**
     * Bad request error
     */
    protected ResponseEntity<ErrorResponse> badRequest(String message) {
        ErrorResponse error = ErrorResponse.of(
                400,
                "Bad Request",
                message,
                null
        );
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Unauthorized error
     */
    protected ResponseEntity<ErrorResponse> unauthorized(String message) {
        ErrorResponse error = ErrorResponse.of(
                401,
                "Unauthorized",
                message,
                null
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Forbidden error
     */
    protected ResponseEntity<ErrorResponse> forbidden(String message) {
        ErrorResponse error = ErrorResponse.of(
                403,
                "Forbidden",
                message,
                null
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Not found error
     */
    protected ResponseEntity<ErrorResponse> notFound(String message) {
        ErrorResponse error = ErrorResponse.of(
                404,
                "Not Found",
                message,
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Internal server error
     */
    protected ResponseEntity<ErrorResponse> internalError(String message) {
        ErrorResponse error = ErrorResponse.of(
                500,
                "Internal Server Error",
                message,
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Validation error
     */
    protected ResponseEntity<ErrorResponse> validationError(
            String message,
            List<ErrorResponse.ValidationError> validationErrors) {

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now())
                .status(400)
                .error("Validation Failed")
                .message(message)
                .path(null)
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.badRequest().body(error);
    }
}