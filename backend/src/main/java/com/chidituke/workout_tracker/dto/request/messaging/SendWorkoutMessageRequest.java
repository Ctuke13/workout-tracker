package com.chidituke.workout_tracker.dto.request.messaging;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for sharing workout content
 */
@Data
public class SendWorkoutMessageRequest {

    @Size(max = 2000, message = "Message content cannot exceed 2000 characters")
    private String content; // Optional message

    private Long workoutSessionId;
    private Long workoutPlanId;

    // Validation: exactly one of workoutSessionId or workoutPlanId must be provided
}