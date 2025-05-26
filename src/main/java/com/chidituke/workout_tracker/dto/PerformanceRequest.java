package com.chidituke.workout_tracker.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PerformanceRequest {
    @NotNull(message = "Workout log ID is required")
    private Long workoutLogId;

    private Integer setNumber = 1;

    // For strength training
    @Positive(message = "Reps must be positive")
    private Integer reps;

    @Positive(message = "Weight must be positive")
    private Double weight;

    //For cardio
    @Positive(message = "Duration must be positive")
    private Integer durationMinutes;

    @Positive(message = "Duration must be positive")
    private Double durationSeconds;

    @Positive(message = "Distance must be positive")
    private Double distanceKm;

    // Additional metrics
    @Positive(message = "Calories must be positive")
    private Double caloriesBurned;

    private String notes;
}
