package com.chidituke.workout_tracker.dto.request.performance;

import com.chidituke.workout_tracker.controller.workout.ScheduledWorkoutController;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * Request DTO for workout completion with performance
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutCompletionRequest {
    @NotNull
    private String exerciseId;

    @NotNull
    private String scheduledExerciseId;

    private String completedAt;

    @NotNull
    private Integer totalDurationMinutes;

    private List<CompletedSetRequest> sets;
    private String notes;
    private String performanceRating;
    private List<Object> personalRecords;
    private List<Object> improvements;

    // Optional workout session data
    private Integer difficultyRating;
    private Double overallEffort;
    private String mood;
    private String location;
    private String workoutFeedback;
    private String performanceSummary;

    // Optional cardio data
    private Double distanceKm;
    private Integer caloriesBurned;

    @Min(value = 1, message = "Must complete at least 1 exercise")
    private Integer exerciseCount = 1;
}
