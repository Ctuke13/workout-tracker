package com.chidituke.workout_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkoutRequest {
    @NotBlank(message = "Workout name is required")
    @Size(min = 2, max = 50, message = "Workout name must be between 2 and 50 characters")
    private String workoutName;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String workoutDescription;

    @NotBlank(message = "Category is required")
    private String workoutType;

    private String imageUrl;

    private Boolean isCardio = false;
}
