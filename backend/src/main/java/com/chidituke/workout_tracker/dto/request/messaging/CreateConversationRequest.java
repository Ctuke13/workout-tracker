package com.chidituke.workout_tracker.dto.request.messaging;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for creating conversations
 */
@Data
public class CreateConversationRequest {

    @NotNull(message = "Other user ID is required")
    private Long otherUserId;

    @Size(max = 2000, message = "Initial message cannot exceed 2000 characters")
    private String initialMessage; // Optional first message
}
