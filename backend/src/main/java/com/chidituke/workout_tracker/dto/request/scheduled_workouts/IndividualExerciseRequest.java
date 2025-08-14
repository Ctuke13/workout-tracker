package com.chidituke.workout_tracker.dto.request.scheduled_workouts;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for scheduling individual exercises with custom configuration.
 * This enables users to add single exercises to their calendar without needing
 * to create or select a complete workout plan.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndividualExerciseRequest {

    @NotNull(message = "Exercise ID is required")
    private Long exerciseId;

    @NotNull(message = "Scheduled date is required")
    private LocalDate scheduledDate;

    // Strength exercise configuration
    @Min(value = 1, message = "Sets must be at least 1")
    @Max(value = 50, message = "Sets cannot exceed 50")
    private Integer sets;

    private String reps; // Supports ranges like "8-12" or "AMRAP"

    @Min(value = 0, message = "Weight cannot be negative")
    private Double weight;

    @Min(value = 0, message = "Rest time cannot be negative")
    private Integer restSeconds;

    private String tempo; // Format: "3-1-2-1"

    @Min(value = 1, message = "RPE must be between 1 and 10")
    @Max(value = 10, message = "RPE must be between 1 and 10")
    private Integer targetRpe;

    // Cardio exercise configuration
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer targetDurationMinutes;

    @Min(value = 0, message = "Distance cannot be negative")
    private Double targetDistanceKm;

    @Min(value = 0, message = "Pace cannot be negative")
    private Double targetPace;

    // Isometric exercise configuration
    @Min(value = 1, message = "Hold duration must be at least 1 second")
    @Max(value = 600, message = "Hold duration cannot exceed 10 minutes")
    private Integer holdDurationSeconds;

    // Common fields
    private String notes;

    /**
     * Determines the likely exercise type based on provided configuration.
     * This helps the backend apply appropriate defaults and validation.
     */
    public boolean appearsToBeCardio() {
        return targetDurationMinutes != null || targetDistanceKm != null || targetPace != null;
    }

    public boolean appearsToBeIsometric() {
        return holdDurationSeconds != null;
    }

    public boolean appearsToBeStrength() {
        return !appearsToBeCardio() && !appearsToBeIsometric();
    }
}
