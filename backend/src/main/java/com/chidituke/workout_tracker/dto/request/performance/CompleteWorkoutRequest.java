package com.chidituke.workout_tracker.dto.request.performance;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for completing entire workout
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteWorkoutRequest {
    @NotNull
    private Integer totalDurationMinutes;

    private Integer difficultyRating; // 1-10
    private Double overallEffort; // 1.0-10.0
    private String mood; // ENERGETIC, TIRED, etc.
    private String location; // HOME, GYM, etc.
    private String workoutFeedback;
    private String performanceSummary;
}