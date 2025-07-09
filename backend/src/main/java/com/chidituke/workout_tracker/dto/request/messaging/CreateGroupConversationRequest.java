package com.chidituke.workout_tracker.dto.request.messaging;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for creating group conversations
 */
@Data
public class CreateGroupConversationRequest {

    @NotBlank(message = "Group name is required")
    @Size(max = 100, message = "Group name cannot exceed 100 characters")
    private String name;

    @NotNull(message = "Participant IDs are required")
    @Size(min = 2, message = "Group must have at least 2 participants")
    private List<Long> participantIds;

    @Size(max = 2000, message = "Initial message cannot exceed 2000 characters")
    private String initialMessage; // Optional first message
}
