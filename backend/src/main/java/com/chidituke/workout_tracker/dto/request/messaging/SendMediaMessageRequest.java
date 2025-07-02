package com.chidituke.workout_tracker.dto.request.messaging;

import com.chidituke.workout_tracker.model.messaging.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for sending media messages
 */
@Data
public class SendMediaMessageRequest {

    @Size(max = 2000, message = "Message content cannot exceed 2000 characters")
    private String content; // Optional caption

    @NotNull(message = "Message type is required")
    private MessageType messageType;

    // File will be handled as MultipartFile in controller
}
