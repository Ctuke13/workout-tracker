package com.chidituke.workout_tracker.dto.request.progress;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for processing workout completion in the progression system.
 * Focuses on stats needed for XP calculation and achievement tracking.
 * <p>
 * This is separate from performance.WorkoutCompletionRequest which tracks
 * detailed workout performance (sets, reps, PRs, etc).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressionUpdateRequest {

    /**
     * Duration of the workout in minutes (for XP calculation)
     */
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    /**
     * Total number of sets completed (for statistics)
     */
    @NotNull(message = "Sets completed is required")
    @Min(value = 0, message = "Sets cannot be negative")
    private Integer setsCompleted;

    /**
     * Total volume lifted in kg (for strength volume achievements)
     */
    @Builder.Default
    private BigDecimal volumeLifted = BigDecimal.ZERO;

    /**
     * Total distance covered in km (for cardio distance achievements)
     */
    @Builder.Default
    private BigDecimal distanceKm = BigDecimal.ZERO;

    /**
     * Total hold duration in seconds (for isometric endurance achievements)
     */
    @Builder.Default
    private Integer holdSeconds = 0;

    /**
     * Number of new unique exercises tried in this workout
     */
    @Builder.Default
    private Integer uniqueExercisesCount = 0;

    /**
     * Type of workout: CARDIO, STRENGTH, ISOMETRIC
     */
    @NotNull(message = "Workout type is required")
    private String workoutType;

    /**
     * Number of exercises completed in workout (for pet crystal rewards)
     */
    @Builder.Default
    private Integer exerciseCount = 0;

    /**
     * Whether the workout qualifies for a consistency bonus (15% XP boost).
     * Calculated on the frontend based on duration and sets — rewards honest,
     * realistic workout sessions.
     */
    @Builder.Default
    private Boolean consistencyBonus = false;
}