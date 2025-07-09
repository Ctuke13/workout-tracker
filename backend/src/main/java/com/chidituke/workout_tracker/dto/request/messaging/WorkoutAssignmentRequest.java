package com.chidituke.workout_tracker.dto.request.messaging;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for workout assignments (professional feature)
 */
@Data
public class WorkoutAssignmentRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotNull(message = "Workout plan ID is required")
    private Long workoutPlanId;

    @Size(max = 1000, message = "Instructions cannot exceed 1000 characters")
    private String instructions; // Optional trainer instructions
}