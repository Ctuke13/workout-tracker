package com.chidituke.workout_tracker.dto.request.workout_session;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class WorkoutSessionRequest {

    @NotNull(message = "Workout plan ID is required")
    private Long workoutPlanId;

    private LocalDate date; // Defaults to today if not provided

    @Min(value = 0, message = "Duration cannot be negative")
    private Integer totalDurationMinutes;

    @Min(value = 0, message = "Calories cannot be negative")
    private Integer estimatedCalories;

    @Min(value = 1, message = "Difficulty rating must be between 1 and 10")
    @Max(value = 10, message = "Difficulty rating must be between 1 and 10")
    private Integer difficultyRating;

    @DecimalMin(value = "1.0", message = "Effort must be between 1 and 10")
    @DecimalMax(value = "10.0", message = "Effort must be between 1 and 10")
    private Double overallEffort;

    private String mood; // Will be converted to enum
    private String location; // Will be converted to enum

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    // Optional scheduled workout context
    private Long scheduledWorkoutId;

    // Optional program context
    private Long programId;

    @Min(value = 1, message = "Week number must be at least 1")
    private Integer weekNumber;

    private Boolean isShared = false;
}
