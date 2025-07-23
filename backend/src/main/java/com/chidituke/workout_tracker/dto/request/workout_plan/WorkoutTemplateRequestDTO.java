package com.chidituke.workout_tracker.dto.request.workout_plan;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class WorkoutTemplateRequestDTO {

    @NotBlank(message = "Workout name is required")
    @Size(min = 2, max = 100, message = "Workout name must be 2-100 characters")
    private String workoutName;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String workoutDescription;

    @NotBlank(message = "Category is required")
    private String workoutCategory;

    private String workoutImageUrl;

    private Boolean isCardio = false;

    @NotNull(message = "Workout type is required")
    private String workoutType; // Will be converted to enum

    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 480, message = "Duration cannot exceed 8 hours")
    private Integer estimatedDurationMinutes;

    @NotNull(message = "Difficulty level is required")
    private String difficultyLevel; // Will be converted to enum

    private String targetMuscleGroups;
    private String equipmentNeeded;

    @NotNull(message = "Subscription tier is required")
    private String subscriptionTierRequired = "FREE";

    private Boolean isPublic = true;
}