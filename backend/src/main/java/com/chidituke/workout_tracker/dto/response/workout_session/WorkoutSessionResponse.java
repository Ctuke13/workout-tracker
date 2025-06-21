package com.chidituke.workout_tracker.dto.response.workout_session;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class WorkoutSessionResponse {

    private Long id;
    private LocalDate date;
    private Integer totalDurationMinutes;
    private Integer estimatedCalories;
    private Integer difficultyRating;
    private Double overallEffort;
    private String mood;
    private String location;
    private String notes;

    // Workout plan info
    private Long workoutPlanId;
    private String workoutPlanName;
    private String workoutPlanCategory;

    // Program context
    private Long programId;
    private String programName;
    private Integer weekNumber;

    // Scheduled workout link
    private Long scheduledWorkoutId;

    // Social features
    private Boolean isShared;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}