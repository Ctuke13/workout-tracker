package com.chidituke.workout_tracker.exceptions.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private int status;
    private String error;
    private String message;
    private String path;
    private String traceId;

    // For validation errors
    private List<ValidationError> validationErrors;

    // For additional context
    private Map<String, Object> details;

    @Data
    @Builder
    public static class ValidationError {
        private String field;
        private Object rejectedValue;
        private String message;
    }

    // ✅ Static factory methods (these were missing)
    public static ErrorResponse of(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }

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
}