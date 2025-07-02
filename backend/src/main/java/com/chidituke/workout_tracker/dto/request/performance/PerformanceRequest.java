package com.chidituke.workout_tracker.dto.request.performance;

import com.chidituke.workout_tracker.model.workout.PerformanceRecord;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Enhanced Performance Request DTO with comprehensive performance metrics
 * Supports all exercise types: strength, cardio, flexibility, plyometric
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceRequest {

    // ==============================================
    // REQUIRED RELATIONSHIPS
    // ==============================================

    @NotNull(message = "Workout log ID is required")
    private Long workoutLogId;

    @NotNull(message = "Exercise ID is required")
    private Long exerciseId; // ✅ ADDED: Missing from original

    @NotNull(message = "Set number is required")
    @Positive(message = "Set number must be positive")
    @Builder.Default
    private Integer setNumber = 1;

    // ==============================================
    // BASIC PERFORMANCE METRICS
    // ==============================================

    @Min(value = 0, message = "Reps cannot be negative")
    private Integer reps;

    @DecimalMin(value = "0.0", message = "Weight cannot be negative")
    @DecimalMax(value = "1000.0", message = "Weight cannot exceed 1000kg")
    private Double weight;

    // ==============================================
    // CARDIO METRICS
    // ==============================================

    @Min(value = 0, message = "Duration cannot be negative")
    @Max(value = 1440, message = "Duration cannot exceed 24 hours")
    private Integer durationMinutes;

    @DecimalMin(value = "0.0", message = "Duration cannot be negative")
    @DecimalMax(value = "86400.0", message = "Duration cannot exceed 24 hours")
    private Double durationSeconds;

    @DecimalMin(value = "0.0", message = "Distance cannot be negative")
    @DecimalMax(value = "1000.0", message = "Distance cannot exceed 1000km")
    private Double distanceKm;

    @Min(value = 0, message = "Calories cannot be negative")
    @Max(value = 10000, message = "Calories cannot exceed 10,000")
    private Integer caloriesBurned;

    // ==============================================
    // ADVANCED PERFORMANCE METRICS
    // ==============================================

    @Min(value = 1, message = "Perceived exertion must be between 1-10")
    @Max(value = 10, message = "Perceived exertion must be between 1-10")
    private Integer perceivedExertion; // RPE scale

    @Min(value = 1, message = "Form rating must be between 1-10")
    @Max(value = 10, message = "Form rating must be between 1-10")
    private Integer formRating; // Self-assessment of form quality

    @Min(value = 0, message = "Rest time cannot be negative")
    @Max(value = 3600, message = "Rest time cannot exceed 1 hour")
    private Integer restSeconds;

    @Pattern(regexp = "^\\d{1,2}-\\d{1,2}-\\d{1,2}-\\d{1,2}$|^$",
            message = "Tempo must be in format '3-1-2-1' (eccentric-pause-concentric-pause)")
    private String tempo;

    // ==============================================
    // SPECIALIZED EXERCISE METRICS
    // ==============================================

    @Min(value = 0, message = "Hold duration cannot be negative")
    @Max(value = 3600, message = "Hold duration cannot exceed 1 hour")
    private Integer holdDurationSeconds; // For isometric exercises

    @Min(value = 1, message = "Balance score must be between 1-10")
    @Max(value = 10, message = "Balance score must be between 1-10")
    private Integer balanceScore; // For balance exercises

    @DecimalMin(value = "0.0", message = "Jump height cannot be negative")
    @DecimalMax(value = "500.0", message = "Jump height cannot exceed 500cm")
    private Double jumpHeightCm; // For plyometric exercises

    @DecimalMin(value = "0.0", message = "Power output cannot be negative")
    @DecimalMax(value = "10000.0", message = "Power output cannot exceed 10,000 watts")
    private Double powerOutputWatts; // For power-based exercises

    // ==============================================
    // PROFESSIONAL TRAINING METRICS
    // ==============================================

    private Long assignedByTrainerId; // For trainer-assigned workouts

    @Min(value = 0, message = "Target reps cannot be negative")
    private Integer targetReps; // What was prescribed

    @DecimalMin(value = "0.0", message = "Target weight cannot be negative")
    private Double targetWeight; // What was supposed to be lifted

    private PerformanceRecord.AchievementStatus achievementStatus;

    // ==============================================
    // NOTES AND CONTEXT
    // ==============================================

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    @Size(max = 200, message = "Equipment description cannot exceed 200 characters")
    private String equipmentUsed; // "Barbell", "Dumbbells", "Resistance Bands"

    @Size(max = 100, message = "Environment description cannot exceed 100 characters")
    private String workoutEnvironment; // "GYM", "HOME", "OUTDOOR"

    // ==============================================
    // VALIDATION HELPER METHODS
    // ==============================================

    /**
     * Check if this request is for strength training
     */
    public boolean isStrengthTraining() {
        return reps != null && weight != null;
    }

    /**
     * Check if this request is for cardio
     */
    public boolean isCardio() {
        return durationMinutes != null || durationSeconds != null || distanceKm != null;
    }

    /**
     * Check if this request has timing metrics
     */
    public boolean hasTimingMetrics() {
        return durationMinutes != null || durationSeconds != null || holdDurationSeconds != null;
    }

    /**
     * Check if this request has advanced metrics
     */
    public boolean hasAdvancedMetrics() {
        return perceivedExertion != null || formRating != null ||
                restSeconds != null || tempo != null;
    }

    /**
     * Check if this request has professional coaching context
     */
    public boolean hasProfessionalContext() {
        return assignedByTrainerId != null || targetReps != null ||
                targetWeight != null || achievementStatus != null;
    }

    /**
     * Get total duration in seconds
     */
    public Double getTotalDurationSeconds() {
        double total = 0.0;
        if (durationMinutes != null) {
            total += durationMinutes * 60.0;
        }
        if (durationSeconds != null) {
            total += durationSeconds;
        }
        return total > 0 ? total : null;
    }

    /**
     * Calculate theoretical volume if both weight and reps are provided
     */
    public Double getTheoreticalVolume() {
        if (weight != null && reps != null) {
            return weight * reps;
        }
        return null;
    }

    /**
     * Check if the performance met or exceeded targets
     */
    public boolean metTargets() {
        if (targetReps == null || targetWeight == null || reps == null || weight == null) {
            return false;
        }

        double targetVolume = targetReps * targetWeight;
        double actualVolume = reps * weight;

        return actualVolume >= targetVolume;
    }

    /**
     * Get performance completeness score (0-1) based on filled fields
     */
    public double getCompletenessScore() {
        int totalFields = 25; // Total number of possible fields
        int filledFields = 0;

        // Count non-null fields
        if (reps != null) filledFields++;
        if (weight != null) filledFields++;
        if (durationMinutes != null) filledFields++;
        if (durationSeconds != null) filledFields++;
        if (distanceKm != null) filledFields++;
        if (caloriesBurned != null) filledFields++;
        if (perceivedExertion != null) filledFields++;
        if (formRating != null) filledFields++;
        if (restSeconds != null) filledFields++;
        if (tempo != null && !tempo.isEmpty()) filledFields++;
        if (holdDurationSeconds != null) filledFields++;
        if (balanceScore != null) filledFields++;
        if (jumpHeightCm != null) filledFields++;
        if (powerOutputWatts != null) filledFields++;
        if (assignedByTrainerId != null) filledFields++;
        if (targetReps != null) filledFields++;
        if (targetWeight != null) filledFields++;
        if (achievementStatus != null) filledFields++;
        if (notes != null && !notes.isEmpty()) filledFields++;
        if (equipmentUsed != null && !equipmentUsed.isEmpty()) filledFields++;
        if (workoutEnvironment != null && !workoutEnvironment.isEmpty()) filledFields++;

        return (double) filledFields / totalFields;
    }

    /**
     * Validate that essential data is provided based on exercise type
     */
    public boolean hasEssentialData() {
        // For strength training, need reps and weight
        if (isStrengthTraining()) {
            return reps != null && reps > 0 && weight != null && weight > 0;
        }

        // For cardio, need duration or distance
        if (isCardio()) {
            return (durationMinutes != null && durationMinutes > 0) ||
                    (durationSeconds != null && durationSeconds > 0) ||
                    (distanceKm != null && distanceKm > 0);
        }

        // For other exercises, at least some metric should be provided
        return holdDurationSeconds != null || balanceScore != null ||
                jumpHeightCm != null || powerOutputWatts != null;
    }
}