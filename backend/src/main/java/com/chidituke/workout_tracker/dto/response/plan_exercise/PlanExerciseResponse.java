package com.chidituke.workout_tracker.dto.response.plan_exercise;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PlanExerciseResponse {

    private Long id;
    private Integer orderInWorkout;

    // Exercise information
    private Long exerciseId;
    private String exerciseName;
    private String exerciseDescription;
    private String exerciseType;
    private String exerciseDifficulty;
    private String exerciseImageUrl;

    // Prescription
    private Integer prescribedSets;
    private String prescribedReps;
    private Double prescribedWeightPercent;
    private Integer prescribedRestSeconds;
    private String prescribedTempo;
    private Integer prescribedRpe;
    private String formattedPrescription;

    // Instructions
    private String instructions;
    private String coachingCues;
    private String modificationNotes;

    // Alternative exercise
    private Long alternativeExerciseId;
    private String alternativeExerciseName;
    private Boolean hasAlternative;

    // Progression
    private Boolean isProgressionExercise;
    private String progressionGoal;

    // Metadata
    private Boolean isOptional;
    private Boolean isSuperset;
    private String supersetGroup;
    private String equipmentAlternatives;
    private String subscriptionTierRequired;

    // Creator information
    private Long createdByUserId;
    private Boolean isUserCustomization;

    // Access control
    private Boolean isAccessibleToCurrentUser;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Utility fields
    private String displayOrder; // "1. Bench Press"
}