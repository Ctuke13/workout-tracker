package com.chidituke.workout_tracker.dto.request.messaging;

import com.chidituke.workout_tracker.model.messaging.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for sending text messages
 */
@Data
public class SendTextMessageRequest {

    @NotBlank(message = "Message content is required")
    @Size(max = 2000, message = "Message content cannot exceed 2000 characters")
    private String content;
}