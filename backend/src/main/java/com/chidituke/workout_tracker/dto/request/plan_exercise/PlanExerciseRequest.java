package com.chidituke.workout_tracker.dto.request.plan_exercise;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PlanExerciseRequest {

    @NotNull(message = "Exercise ID is required")
    private Long exerciseId;

    @Min(value = 1, message = "Order must be at least 1")
    private Integer orderInWorkout;

    // Prescription fields
    @Min(value = 1, message = "Sets must be at least 1")
    @Max(value = 20, message = "Sets cannot exceed 20")
    private Integer prescribedSets;

    @Size(max = 50, message = "Reps description too long")
    private String prescribedReps; // "8-12", "15", "AMRAP", "30 seconds"

    @DecimalMin(value = "0.0", message = "Weight percent cannot be negative")
    @DecimalMax(value = "200.0", message = "Weight percent cannot exceed 200%")
    private Double prescribedWeightPercent;

    @Min(value = 0, message = "Rest seconds cannot be negative")
    @Max(value = 600, message = "Rest cannot exceed 10 minutes")
    private Integer prescribedRestSeconds;

    @Size(max = 20, message = "Tempo description too long")
    private String prescribedTempo; // "3-1-2-1"

    @Min(value = 1, message = "RPE must be between 1 and 10")
    @Max(value = 10, message = "RPE must be between 1 and 10")
    private Integer prescribedRpe;

    // Instructions
    @Size(max = 1000, message = "Instructions too long")
    private String instructions;

    @Size(max = 500, message = "Coaching cues too long")
    private String coachingCues;

    @Size(max = 500, message = "Modification notes too long")
    private String modificationNotes;

    // Alternative exercise
    private Long alternativeExerciseId;

    // Progression
    private Boolean isProgressionExercise = false;

    @Size(max = 200, message = "Progression goal too long")
    private String progressionGoal;

    // Metadata
    private Boolean isOptional = false;
    private Boolean isSuperset = false;
    private String supersetGroup;

    @Size(max = 200, message = "Equipment alternatives too long")
    private String equipmentAlternatives;

    @NotNull(message = "Subscription tier is required")
    private String subscriptionTierRequired = "FREE";
}