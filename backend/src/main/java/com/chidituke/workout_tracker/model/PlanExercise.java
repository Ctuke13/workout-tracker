package com.chidituke.workout_tracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "plan_exercise")
public class PlanExercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_exercise_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_plan_id", nullable = false)
    @NotNull(message = "Workout is required")
    private WorkoutPlan workoutPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    @NotNull(message = "Exercise is required")
    private Exercise exercise;

    @Column(name = "order_in_workout", nullable = false)
    @Min(value = 1, message = "Order must be at least 1")
    private Integer orderInWorkout; // 1st, 2nd, 3rd exercise in the workout

    // Default prescription for this exercise in this workout
    @Column(name = "prescribed_sets")
    private Integer prescribedSets; // Default: 3 sets

    @Column(name = "prescribed_reps")
    private String prescribedReps; // "8-12", "15", "AMRAP", "30 seconds"

    @Column(name = "prescribed_weight_percent")
    private Double prescribedWeightPercent; // 75% of 1RM

    @Column(name = "prescribed_rest_seconds")
    private Integer prescribedRestSeconds; // 90 seconds between sets

    @Column(name = "prescribed_tempo")
    private String prescribedTempo; // "3-1-2-1"

    @Column(name = "prescribed_rpe")
    private Integer prescribedRpe; // Target RPE: 7-8

    // Exercise-specific instructions
    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions; // "Focus on slow eccentric", "Pause at bottom"

    @Column(name = "coaching_cues", columnDefinition = "TEXT")
    private String coachingCues; // "Keep chest up", "Drive through heels"

    // Optional modifications/alternatives
    @Column(name = "modification_notes", columnDefinition = "TEXT")
    private String modificationNotes; // "Use dumbbells if no barbell", "Scale to knee push-ups"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alternative_exercise_id")
    private Exercise alternativeExercise; // Backup exercise if primary not available

    // Progression tracking
    @Column(name = "is_progression_exercise")
    private Boolean isProgressionExercise = false; // Track this exercise for progression

    @Column(name = "progression_goal")
    private String progressionGoal; // "Increase weight by 5lbs each week"

    // Subscription/access control
    @Column(name = "subscription_tier_required")
    private String subscriptionTierRequired = "FREE"; // "FREE", "PLUS", "PRO"

    // Metadata
    @Column(name = "is_optional")
    private Boolean isOptional = false; // Can skip this exercise

    @Column(name = "is_superset")
    private Boolean isSuperset = false; // Part of a superset

    @Column(name = "superset_group")
    private String supersetGroup; // "A", "B", "C" - exercises with same letter are superseted

    @Column(name = "equipment_alternatives", columnDefinition = "TEXT")
    private String equipmentAlternatives; // "Dumbbells, resistance bands, bodyweight"

    // Creator tracking
    @Column(name = "created_by_user_id")
    private Long createdByUserId; // Who added this exercise to the workout

    @Column(name = "is_user_customization")
    private Boolean isUserCustomization = false; // User modified the original workout

    // Timestamps
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ENUMS
    public enum SupersetType {
        NONE,           // Regular exercise
        SUPERSET,       // 2 exercises back-to-back
        TRISET,         // 3 exercises back-to-back
        CIRCUIT         // Multiple exercises in sequence
    }

    public enum ProgressionType {
        WEIGHT,         // Increase weight
        REPS,           // Increase reps
        SETS,           // Increase sets
        TIME,           // Increase duration
        DISTANCE,       // Increase distance
        DIFFICULTY      // Increase exercise difficulty
    }

    // JPA LIFECYCLE METHODS
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // UTILITY METHODS
    public String getDisplayOrder() {
        return orderInWorkout + ". " + exercise.getExerciseName();
    }

    public boolean hasAlternative() {
        return alternativeExercise != null ||
                (equipmentAlternatives != null && !equipmentAlternatives.trim().isEmpty());
    }

    public boolean isAccessibleToUser(String userSubscriptionTier) {
        if ("FREE".equals(subscriptionTierRequired)) return true;
        if ("PLUS".equals(subscriptionTierRequired)) return !"FREE".equals(userSubscriptionTier);
        if ("PRO".equals(subscriptionTierRequired)) return "PRO".equals(userSubscriptionTier);
        return true;
    }

    public String getFormattedPrescription() {
        StringBuilder prescription = new StringBuilder();

        if (prescribedSets != null) {
            prescription.append(prescribedSets).append(" sets");
        }

        if (prescribedReps != null) {
            if (prescription.length() > 0) prescription.append(" × ");
            prescription.append(prescribedReps).append(" reps");
        }

        if (prescribedRpe != null) {
            prescription.append(" @ RPE ").append(prescribedRpe);
        }

        if (prescribedRestSeconds != null) {
            prescription.append(" (").append(prescribedRestSeconds).append("s rest)");
        }

        return prescription.toString();
    }
}