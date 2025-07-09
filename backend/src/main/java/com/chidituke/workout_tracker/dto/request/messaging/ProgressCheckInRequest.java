package com.chidituke.workout_tracker.dto.request.messaging;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for progress check-in requests
 */
@Data
public class ProgressCheckInRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    private String message; // Optional custom message
}
