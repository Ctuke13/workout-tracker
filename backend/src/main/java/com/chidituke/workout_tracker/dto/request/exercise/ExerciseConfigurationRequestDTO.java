package com.chidituke.workout_tracker.dto.request.exercise;

import com.chidituke.workout_tracker.model.workout.Exercise;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseConfigurationRequestDTO {

    // Workout tracking mode from Exercise entity
    private Exercise.WorkoutTrackingMode trackingMode;

    // Strength Configuration (maps to PlanExercise fields)
    private Integer prescribedSets;
    private String prescribedReps;
    private Double prescribedWeightPercent; // Using existing field instead of weightKg
    private Integer prescribedRestSeconds;
    private String prescribedTempo;
    private Integer prescribedRpe;

    // Additional strength fields (not in PlanExercise but useful for requests)
    private Boolean dropSets;
    private Boolean clusterSets;
    private Boolean pauseReps;
    private Integer pauseDurationSeconds;
    private Integer percentage1RM; // Can be converted to prescribedWeightPercent

    // Cardio Configuration (creative mapping to existing PlanExercise fields)
    // Note: These will be stored creatively in existing PlanExercise fields
    private Integer targetDurationMinutes; // Can store in prescribedSets for cardio
    private Double targetDistanceKm; // Can store in prescribedWeightPercent for cardio
    private Double targetPaceMinPerKm; // Can store in prescribedRpe for cardio (converted to int)
    private Double targetSpeedKmh;
    private Integer targetHeartRateMin;
    private Integer targetHeartRateMax;

    // Removed non-existent enums:
    // private PlanExercise.IntensityZone intensityZone; // REMOVED - doesn't exist
    // private PlanExercise.CardioType cardioType; // REMOVED - doesn't exist

    private Integer inclinePercentage;
    private Integer resistanceLevel;

    // Isometric Configuration (creative mapping)
    private Integer holdDurationSeconds; // Can store in prescribedRestSeconds for isometric
    private Integer holdRepetitions; // Can store in prescribedSets for isometric
    private Integer restBetweenHoldsSeconds;
    private Integer progressionIncrementSeconds;
    private Integer targetMuscleTensionPercentage;

    // Common Configuration (maps directly to PlanExercise fields)
    private String instructions; // Maps to PlanExercise.instructions
    private String coachingCues; // Maps to PlanExercise.coachingCues
    private String modificationNotes; // Maps to PlanExercise.modificationNotes
    private String equipmentAlternatives; // Maps to PlanExercise.equipmentAlternatives

    // Progression and metadata
    private Boolean isProgressionExercise; // Maps to PlanExercise.isProgressionExercise
    private String progressionGoal; // Maps to PlanExercise.progressionGoal
    private Boolean isOptional; // Maps to PlanExercise.isOptional
    private Boolean isSuperset; // Maps to PlanExercise.isSuperset
    private String supersetGroup; // Maps to PlanExercise.supersetGroup
    private String subscriptionTierRequired; // Maps to PlanExercise.subscriptionTierRequired

    // Difficulty modifier (not directly in PlanExercise, but useful for calculations)
    private Double difficultyModifier;

    // Helper method for difficulty level determination
    public String getDifficultyLevel() {
        if (difficultyModifier == null) return "NORMAL";
        if (difficultyModifier < 0.8) return "EASY";
        if (difficultyModifier > 1.3) return "HARD";
        return "NORMAL";
    }

    // Helper method to determine if this is a cardio configuration
    public boolean isCardioConfiguration() {
        return targetDurationMinutes != null ||
                targetDistanceKm != null ||
                targetPaceMinPerKm != null ||
                targetSpeedKmh != null ||
                targetHeartRateMin != null ||
                targetHeartRateMax != null;
    }

    // Helper method to determine if this is an isometric configuration
    public boolean isIsometricConfiguration() {
        return holdDurationSeconds != null ||
                holdRepetitions != null ||
                targetMuscleTensionPercentage != null;
    }

    // Helper method to determine if this is a strength configuration
    public boolean isStrengthConfiguration() {
        return prescribedSets != null ||
                prescribedReps != null ||
                prescribedWeightPercent != null ||
                percentage1RM != null;
    }
}