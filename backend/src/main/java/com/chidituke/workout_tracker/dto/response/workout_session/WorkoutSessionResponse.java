package com.chidituke.workout_tracker.dto.response.workout_session;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Enhanced WorkoutSessionResponse DTO
 * Includes all fields for complete workout session tracking and performance monitoring
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutSessionResponse {

    // Core session identification
    private Long id;
    private LocalDate date;

    // : Session status and completion tracking
    private String sessionStatus; // PLANNED, IN_PROGRESS, COMPLETED, CANCELLED, PAUSED
    private Integer totalExercisesPlanned;
    private Integer totalExercisesCompleted;
    private Double completionPercentage;
    private String workoutFeedback;
    private String performanceSummary;

    // Session metrics
    private Integer totalDurationMinutes;
    private Integer estimatedCalories;
    private Integer difficultyRating; // 1-10
    private Double overallEffort; // 1.0-10.0

    // Session context
    private String mood; // ENERGETIC, TIRED, MOTIVATED, FOCUSED, etc.
    private String location; // HOME, GYM, PARK, OFFICE, etc.
    private String notes;

    // Workout plan info
    private Long workoutPlanId;
    private String workoutPlanName;
    private String workoutPlanCategory;

    // Program context (for multi-week programs)
    private Long programId;
    private String programName;
    private Integer weekNumber;

    // Scheduled workout link
    private Long scheduledWorkoutId;

    // Social features
    private Boolean isShared;

    // Performance tracking summary
    private Integer totalSetsCompleted;
    private Integer totalRepsCompleted;
    private Double totalVolumeLifted; // Total weight * reps
    private Double averageRpe; // Average perceived exertion
    private Double averageFormRating; // Average form quality
    private Integer totalRestTimeMinutes; // Total rest time between sets

    // Exercise breakdown summary
    private Integer strengthExercisesCount;
    private Integer cardioExercisesCount;
    private Integer isometricExercisesCount;
    private String primaryMuscleGroupsTargeted; // Comma-separated list

    // Performance comparison
    private String overallPerformanceRating; // EXCELLENT, GOOD, AVERAGE, NEEDS_IMPROVEMENT
    private Integer exercisesMetTarget; // Number of exercises that met target
    private Integer exercisesExceededTarget; // Number of exercises that exceeded target
    private Double targetCompletionRate; // Percentage of targets met or exceeded

    // Calories and effort tracking
    private Integer actualCaloriesBurned; // Calculated from performance data
    private Double intensityScore; // Calculated intensity based on RPE and duration
    private String workoutEffectivenessRating; // Based on completion rate and effort

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime startedAt; // When workout execution began
    private LocalDateTime completedAt; // When workout was finished

    // Quick access flags
    private Boolean isCompleted;
    private Boolean isInProgress;
    private Boolean hasPerformanceData; // Whether this session has detailed performance records
    private Boolean canBeRepeated; // Whether this workout can be easily repeated
}