package com.chidituke.workout_tracker.model.workout;

import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.workout.Exercise;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

@Data
@Entity
@Table(name = "scheduled_workouts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "scheduled_date", "workout_plan_id"}))
public class ScheduledWorkout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scheduled_workout_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;

    @Column(name = "target_sets")
    private Integer targetSets;

    @Column(name = "target_reps")
    private String targetReps;

    @Column(name = "target_weight")
    private Double targetWeight;

    @Column(name = "target_weight_unit")
    private String targetWeightUnit = "lbs";

    @Column(name = "rest_seconds")
    private Integer restSeconds;

    @Column(name = "tempo")
    private String tempo;

    @Column(name = "target_rpe")
    private Integer targetRpe;

    // Cardio fields
    @Column(name = "target_duration_minutes")
    private Integer targetDurationMinutes;

    @Column(name = "actual_duration_minutes")
    private Integer actualDurationMinutes;

    @Column(name = "target_distance_km")
    private Double targetDistanceKm;

    @Column(name = "target_pace")
    private Double targetPace;

    // Isometric fields
    @Column(name = "hold_duration_seconds")
    private Integer holdDurationSeconds;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    // =============================================================================
    // Workout Plan Relationship
    // =============================================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_plan_id")
    private WorkoutPlan workoutPlan;

    @Column(name = "scheduled_date", nullable = false)
    @NotNull(message = "Scheduled date is required")
    private LocalDate scheduledDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ScheduleStatus status = ScheduleStatus.SCHEDULED;

    // Optional program context (for multi-week programs)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private WorkoutProgram program;

    @Column(name = "week_number")
    private Integer weekNumber; // Which week of the program

    @Column(name = "day_of_week")
    private Integer dayOfWeek; // 1=Monday, 7=Sunday

    // User customizations
    @Column(name = "custom_notes", columnDefinition = "TEXT")
    private String customNotes;

    @Column(name = "reminder_time")
    private LocalDateTime reminderTime;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    // Completion tracking
    @OneToOne(mappedBy = "scheduledWorkout", cascade = CascadeType.ALL)
    private WorkoutSession completedSession;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Metadata
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    // ==================== CALORIE PREDICTIONS ====================

    @Column(name = "predicted_calories")
    private Integer predictedCalories;

    @Column(name = "calorie_prediction_method", length = 50)
    private String caloriePredictionMethod; // "EXERCISE_HISTORY", "MET_BASED", etc.

    @Column(name = "calorie_prediction_confidence")
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    private Double caloriePredictionConfidence;

    // =============================================================================
    // EXERCISE RESOLUTION METHODS (Key business logic!)
    // =============================================================================

    /**
     * Get the exercise for this scheduled workout
     * Handles both direct exercise references and workout plan exercises via PlanExercise
     */
    public Exercise getResolvedExercise() {
        // Priority 1: Direct exercise reference
        if (exercise != null) {
            return exercise;
        }

        // Priority 2: Exercise from workout plan via PlanExercise
        if (workoutPlan != null) {
            // Get the first exercise from the workout plan
            // Note: For multi-exercise workout plans, you might need additional logic
            // to determine which specific exercise this scheduled workout refers to

            Exercise firstExercise = workoutPlan.getFirstExercise();
            if (firstExercise != null) {
                return firstExercise;
            }

            // If workout plan has multiple exercises, you might want to:
            // 1. Add an exercise_order field to scheduled_workouts
            // 2. Create separate scheduled_workout entries for each exercise
            // 3. Use some other business logic to determine the specific exercise

            if (workoutPlan.getExerciseCount() > 1) {
                System.out.println("Warning: Workout plan " + workoutPlan.getId() +
                        " has " + workoutPlan.getExerciseCount() + " exercises. " +
                        "Consider adding exercise_order field or creating separate scheduled workouts.");
            }
        }

        return null;
    }

    /**
     * ✅ NEW: Get all exercises from workout plan (for multi-exercise workouts)
     */
    public List<Exercise> getAllWorkoutPlanExercises() {
        if (workoutPlan != null) {
            return workoutPlan.getExercises();
        }
        return Collections.emptyList();
    }

    /**
     * ✅ NEW: Check if this is a multi-exercise workout
     */
    public boolean isMultiExerciseWorkout() {
        return workoutPlan != null && workoutPlan.getExerciseCount() > 1;
    }

    /**
     * Check if this scheduled workout has a valid exercise reference
     */
    public boolean hasValidExercise() {
        return getResolvedExercise() != null;
    }

    /**
     * Get the exercise type for this scheduled workout
     */
    public String getExerciseType() {
        Exercise resolvedExercise = getResolvedExercise();
        if (resolvedExercise != null && resolvedExercise.getExerciseType() != null) {
            return resolvedExercise.getExerciseType().name();
        }

        // Fallback: Use workout plan type if available
        if (workoutPlan != null && workoutPlan.getWorkoutType() != null) {
            switch (workoutPlan.getWorkoutType()) {
                case CARDIO:
                case HIIT:
                    return "CARDIO";
                case STRENGTH:
                case POWERLIFTING:
                    return "STRENGTH";
                case FLEXIBILITY:
                    return "FLEXIBILITY";
                case MIXED:
                default:
                    return "STRENGTH"; // Default for mixed workouts
            }
        }

        // Final fallback: try to determine from configuration
        if (targetDurationMinutes != null || targetDistanceKm != null || targetPace != null) {
            return "CARDIO";
        }
        if (holdDurationSeconds != null) {
            return "BALANCE"; // Isometric
        }
        return "STRENGTH"; // Default
    }

    /**
     * Check if this is a cardio workout
     */
    public boolean isCardioWorkout() {
        Exercise resolvedExercise = getResolvedExercise();
        if (resolvedExercise != null) {
            return resolvedExercise.getIsCardio() != null && resolvedExercise.getIsCardio();
        }

        // Fallback: Check workout plan
        if (workoutPlan != null) {
            return workoutPlan.isCardio() ||
                    workoutPlan.getWorkoutType() == WorkoutPlan.WorkoutType.CARDIO ||
                    workoutPlan.getWorkoutType() == WorkoutPlan.WorkoutType.HIIT;
        }

        // Final fallback: Check configuration
        return targetDurationMinutes != null || targetDistanceKm != null || targetPace != null;
    }

    /**
     * Check if this is an isometric workout
     */
    public boolean isIsometricWorkout() {
        Exercise resolvedExercise = getResolvedExercise();
        if (resolvedExercise != null) {
            return resolvedExercise.getIsIsometric() != null && resolvedExercise.getIsIsometric();
        }

        // Fallback: Check workout plan
        if (workoutPlan != null) {
            return workoutPlan.getWorkoutType() == WorkoutPlan.WorkoutType.FLEXIBILITY;
        }

        // Final fallback: Check configuration
        return holdDurationSeconds != null;
    }

    /**
     * Check if this is a strength workout
     */
    public boolean isStrengthWorkout() {
        return !isCardioWorkout() && !isIsometricWorkout();
    }

    /**
     * ✅ NEW: Get workout tracking mode from exercise or workout plan
     */
    public String getWorkoutTrackingMode() {
        Exercise resolvedExercise = getResolvedExercise();
        if (resolvedExercise != null) {
            Exercise.WorkoutTrackingMode mode = resolvedExercise.getWorkoutTrackingMode();
            switch (mode) {
                case TIME_BASED:
                    return "cardio";
                case HOLD_BASED:
                    return "isometric";
                case REP_BASED:
                    return "strength";
                default:
                    return "strength";
            }
        }

        // Fallback: Use workout plan type
        if (workoutPlan != null && workoutPlan.getWorkoutType() != null) {
            switch (workoutPlan.getWorkoutType()) {
                case CARDIO:
                case HIIT:
                    return "cardio";
                case FLEXIBILITY:
                    return "isometric";
                case STRENGTH:
                case POWERLIFTING:
                case MIXED:
                default:
                    return "strength";
            }
        }

        // Final fallback: detect from configuration
        if (targetDurationMinutes != null || targetDistanceKm != null || targetPace != null) {
            return "cardio";
        }
        if (holdDurationSeconds != null) {
            return "isometric";
        }
        return "strength";
    }

    // =============================================================================
    // EXISTING ENUMS (Keep as-is)
    // =============================================================================
    public enum ScheduleStatus {
        SCHEDULED,      // Future workout
        IN_PROGRESS,    // Currently doing workout
        COMPLETED,      // Finished workout
        CANCELLED,      // User cancelled
        SKIPPED,        // Missed/skipped workout
        RESCHEDULED     // Moved to different date
    }

    // =============================================================================
    // EXISTING BUSINESS LOGIC METHODS (Keep as-is, just update validation)
    // =============================================================================

    /**
     * ✅ UPDATED: Check if user can schedule workout this far in advance
     */
    public boolean canSchedule(User user, LocalDate date) {
        if (user.getSubscriptionTier() == null) {
            return false;
        }

        switch (user.getSubscriptionTier()) {
            case FREE:
                long daysOut = ChronoUnit.DAYS.between(LocalDate.now(), date);
                return daysOut <= 7; // Free users: 7 days max
            case PLUS:
                long weeksOut = ChronoUnit.WEEKS.between(LocalDate.now(), date);
                return weeksOut <= 8; // Plus users: 8 weeks max
            case PRO:
                return true; // Pro users: unlimited
            default:
                return false;
        }
    }

    public boolean isOverdue() {
        return status == ScheduleStatus.SCHEDULED &&
                scheduledDate.isBefore(LocalDate.now());
    }

    public boolean isToday() {
        return scheduledDate.equals(LocalDate.now());
    }

    public boolean isUpcoming() {
        return status == ScheduleStatus.SCHEDULED &&
                scheduledDate.isAfter(LocalDate.now());
    }

    public boolean canBeStarted() {
        return status == ScheduleStatus.SCHEDULED &&
                (isToday() || isOverdue());
    }

    public boolean canBeCancelled() {
        return status == ScheduleStatus.SCHEDULED ||
                status == ScheduleStatus.IN_PROGRESS;
    }

    public boolean canBeRescheduled() {
        return status == ScheduleStatus.SCHEDULED &&
                scheduledDate.isAfter(LocalDate.now());
    }

    public Double getTargetWeightInKg() {
        if (targetWeight == null) return null;
        if ("lbs".equals(targetWeightUnit)) {
            return targetWeight * 0.453592; // Convert lbs to kg
        }
        return targetWeight;
    }

    public Double getTargetWeightInLbs() {
        if (targetWeight == null) return null;
        if ("kg".equals(targetWeightUnit)) {
            return targetWeight * 2.20462; // Convert kg to lbs
        }
        return targetWeight;
    }

    public void setTargetWeightInKg(Double weightKg) {
        this.targetWeight = weightKg;
        this.targetWeightUnit = "kg";
    }

    public void setTargetWeightInLbs(Double weightLbs) {
        this.targetWeight = weightLbs;
        this.targetWeightUnit = "lbs";
    }

    /**
     * Start the scheduled workout
     */
    public void startWorkout() {
        if (!canBeStarted()) {
            throw new IllegalStateException("Cannot start workout in current state: " + status);
        }
        this.status = ScheduleStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Complete the scheduled workout
     */
    public void completeWorkout(WorkoutSession session) {
        if (status != ScheduleStatus.IN_PROGRESS) {
            throw new IllegalStateException("Workout must be in progress to complete");
        }
        this.status = ScheduleStatus.COMPLETED;
        this.completedSession = session;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // Set back-reference
        session.setScheduledWorkout(this);
    }

    /**
     * Cancel the scheduled workout
     */
    public void cancelWorkout() {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Cannot cancel workout in current state: " + status);
        }
        this.status = ScheduleStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Reschedule the workout to a new date
     */
    public void reschedule(LocalDate newDate) {
        if (!canBeRescheduled()) {
            throw new IllegalStateException("Cannot reschedule workout in current state: " + status);
        }

        if (!canSchedule(user, newDate)) {
            throw new IllegalArgumentException("Cannot schedule workout that far in advance");
        }

        this.scheduledDate = newDate;
        this.status = ScheduleStatus.RESCHEDULED;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDisplayStatus() {
        if (isOverdue() && status == ScheduleStatus.SCHEDULED) {
            return "OVERDUE";
        }
        return status.name();
    }

    public long getDaysUntilScheduled() {
        return ChronoUnit.DAYS.between(LocalDate.now(), scheduledDate);
    }

    // =============================================================================
    //  JPA LIFECYCLE METHODS (Enhanced validation)
    // =============================================================================
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        //  Must have either exercise or workout plan
        if (exercise == null && workoutPlan == null) {
            throw new IllegalArgumentException("Scheduled workout must have either an exercise or workout plan");
        }

        // Set estimated duration from workout plan if not set
        if (estimatedDurationMinutes == null && workoutPlan != null) {
            estimatedDurationMinutes = workoutPlan.getEstimatedDurationMinutes();
        }

        // Validate scheduling constraints
        if (!canSchedule(user, scheduledDate)) {
            throw new IllegalArgumentException("Cannot schedule workout that far in advance for user's subscription tier");
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}