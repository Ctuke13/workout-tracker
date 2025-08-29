package com.chidituke.workout_tracker.model.workout;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Performance Record entity representing exercise performance data
 * Enhanced with comprehensive performance metrics and proper indexing
 */
@Data
@Entity
@Table(name = "performance_records", indexes = {
        @Index(name = "idx_performance_workout_session", columnList = "workout_session_id"),
        @Index(name = "idx_performance_exercise", columnList = "exercise_id"),
        @Index(name = "idx_performance_user_date", columnList = "workout_session_id, created_at"),
        @Index(name = "idx_performance_user_exercise", columnList = "workout_session_id, exercise_id"),
        @Index(name = "idx_performance_set_number", columnList = "workout_session_id, set_number")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"workoutSession", "exercise"})
@EqualsAndHashCode(exclude = {"workoutSession", "exercise"})
@EntityListeners(AuditingEntityListener.class)
public class PerformanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "performance_record_id")
    private Long id;

    // ==============================================
    // CORE RELATIONSHIPS
    // ==============================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    @NotNull(message = "Exercise is required")
    private Exercise exercise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_session_id", nullable = false)
    @NotNull(message = "Workout Session is required")
    private WorkoutSession workoutSession;

    // ==============================================
    // BASIC PERFORMANCE METRICS
    // ==============================================

    @Column(name = "set_number", nullable = false)
    @NotNull(message = "Set number is required")
    @Positive(message = "Set number must be positive")
    @Builder.Default
    private Integer setNumber = 1;

    @Column(name = "reps")
    @Min(value = 0, message = "Reps cannot be negative")
    private Integer reps;

    @Column(name = "weight")
    @DecimalMin(value = "0.0", message = "Weight cannot be negative")
    private Double weight;

    // ==============================================
    // CARDIO METRICS
    // ==============================================

    @Column(name = "duration_minutes")
    @Min(value = 0, message = "Duration cannot be negative")
    private Integer durationMinutes;

    @Column(name = "duration_seconds")
    @DecimalMin(value = "0.0", message = "Duration cannot be negative")
    private Double durationSeconds;

    @Column(name = "distance_km")
    @DecimalMin(value = "0.0", message = "Distance cannot be negative")
    private Double distanceKm;

    @Column(name = "calories_burned")
    @Min(value = 0, message = "Calories cannot be negative")
    private Integer caloriesBurned;

    // ==============================================
    // ADVANCED PERFORMANCE METRICS
    // ==============================================

    @Column(name = "perceived_exertion")
    @Min(value = 1, message = "Perceived exertion must be between 1-10")
    @Max(value = 10, message = "Perceived exertion must be between 1-10")
    private Integer perceivedExertion; // RPE scale 1-10

    @Column(name = "form_rating")
    @Min(value = 1, message = "Form rating must be between 1-10")
    @Max(value = 10, message = "Form rating must be between 1-10")
    private Integer formRating; // Self-assessment of form quality

    @Column(name = "rest_seconds")
    @Min(value = 0, message = "Rest time cannot be negative")
    private Integer restSeconds;

    @Column(name = "tempo", length = 20)
    @Pattern(regexp = "^\\d{1,2}-\\d{1,2}-\\d{1,2}-\\d{1,2}$|^$",
            message = "Tempo must be in format '3-1-2-1' (eccentric-pause-concentric-pause)")
    private String tempo;

    // ==============================================
    // SPECIALIZED EXERCISE METRICS
    // ==============================================

    @Column(name = "hold_duration_seconds")
    @Min(value = 0, message = "Hold duration cannot be negative")
    private Integer holdDurationSeconds; // For isometric exercises

    @Column(name = "balance_score")
    @Min(value = 1, message = "Balance score must be between 1-10")
    @Max(value = 10, message = "Balance score must be between 1-10")
    private Integer balanceScore;

    @Column(name = "jump_height_cm")
    @DecimalMin(value = "0.0", message = "Jump height cannot be negative")
    private Double jumpHeightCm;

    @Column(name = "power_output_watts")
    @DecimalMin(value = "0.0", message = "Power output cannot be negative")
    private Double powerOutputWatts;

    // ==============================================
    // NEW: REST TIME AND SET TIMING TRACKING
    // ==============================================

    @Column(name = "set_start_time")
    private LocalDateTime setStartTime;

    @Column(name = "set_end_time")
    private LocalDateTime setEndTime;

    @Column(name = "rest_time_before_set_seconds")
    @Min(value = 0, message = "Rest time cannot be negative")
    private Integer restTimeBeforeSetSeconds; // Time rested before this set

    @Column(name = "actual_set_duration_seconds")
    @Min(value = 0, message = "Set duration cannot be negative")
    private Integer actualSetDurationSeconds;

    // ==============================================
    // EXERCISE COMPLETION TRACKING
    // ==============================================

    @Column(name = "is_exercise_completed")
    private Boolean isExerciseCompleted = false; // Last set of this exercise

    @Column(name = "exercise_completion_notes", length = 500)
    private String exerciseCompletionNotes;

    // ==============================================
    // ENHANCED: TARGET COMPARISON (Add these if not present)
    // ==============================================

    @Column(name = "target_reps_planned")
    private Integer targetRepsPlanned; // What was planned for this set

    @Column(name = "target_weight_planned")
    private Double targetWeightPlanned; // What weight was planned

    @Column(name = "performance_vs_target")
    @Enumerated(EnumType.STRING)
    private PerformanceVsTarget performanceVsTarget;

    // ==============================================
    // PROFESSIONAL TRAINING METRICS
    // ==============================================

    @Column(name = "assigned_by_trainer_id")
    private Long assignedByTrainerId;

    @Column(name = "target_reps")
    @Min(value = 0, message = "Target reps cannot be negative")
    private Integer targetReps;

    @Column(name = "target_weight")
    @DecimalMin(value = "0.0", message = "Target weight cannot be negative")
    private Double targetWeight;

    @Enumerated(EnumType.STRING)
    @Column(name = "achievement_status", length = 20)
    @Builder.Default
    private AchievementStatus achievementStatus = AchievementStatus.NOT_SET;

    // ==============================================
    // NOTES AND METADATA
    // ==============================================

    @Column(name = "notes", length = 1000)
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    @Column(name = "equipment_used", length = 200)
    @Size(max = 200, message = "Equipment description cannot exceed 200 characters")
    private String equipmentUsed;

    @Column(name = "workout_environment", length = 100)
    @Size(max = 100, message = "Environment description cannot exceed 100 characters")
    private String workoutEnvironment; // "GYM", "HOME", "OUTDOOR"

    // ==============================================
    // AUDIT FIELDS
    // ==============================================

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==============================================
    // BUSINESS LOGIC METHODS
    // ==============================================

    /**
     * Calculate total volume (weight × reps)
     */
    public Double calculateVolume() {
        if (weight != null && reps != null) {
            return weight * reps;
        }
        return 0.0;
    }

    /**
     * Calculate performance efficiency based on targets
     */
    public Double calculateEfficiency() {
        if (targetReps != null && targetWeight != null && reps != null && weight != null) {
            double targetVolume = targetReps * targetWeight;
            double actualVolume = reps * weight;
            return targetVolume > 0 ? (actualVolume / targetVolume) * 100 : 0.0;
        }
        return null;
    }

    /**
     * Get workout intensity based on perceived exertion
     */
    public WorkoutIntensity getWorkoutIntensity() {
        if (perceivedExertion == null) return WorkoutIntensity.UNKNOWN;

        return switch (perceivedExertion) {
            case 1, 2, 3 -> WorkoutIntensity.LOW;
            case 4, 5, 6 -> WorkoutIntensity.MODERATE;
            case 7, 8 -> WorkoutIntensity.HIGH;
            case 9, 10 -> WorkoutIntensity.MAXIMUM;
            default -> WorkoutIntensity.UNKNOWN;
        };
    }

    /**
     * Check if this is a personal record for the exercise
     */
    public boolean isPotentialPersonalRecord() {
        // This would be determined by comparing with historical data
        // Implementation would require additional context
        return weight != null && reps != null && weight > 0 && reps > 0;
    }

    /**
     * Get performance score (0-100) based on multiple factors
     */
    public Double getPerformanceScore() {
        double score = 0.0;
        int factors = 0;

        // Factor 1: Form rating (40% weight)
        if (formRating != null) {
            score += (formRating / 10.0) * 40;
            factors++;
        }

        // Factor 2: Achievement vs target (40% weight)
        if (achievementStatus == AchievementStatus.EXCEEDED) {
            score += 40;
            factors++;
        } else if (achievementStatus == AchievementStatus.MET) {
            score += 30;
            factors++;
        } else if (achievementStatus == AchievementStatus.BELOW_TARGET) {
            score += 15;
            factors++;
        }

        // Factor 3: Perceived exertion efficiency (20% weight)
        if (perceivedExertion != null) {
            // Lower perceived exertion with good performance is better
            score += Math.max(0, (11 - perceivedExertion) / 10.0) * 20;
            factors++;
        }

        return factors > 0 ? Math.min(100.0, score) : null;
    }

    // ==============================================
// NEW: BUSINESS LOGIC METHODS
// ==============================================

    /**
     * Calculate actual set duration in seconds
     */
    public Integer calculateActualSetDuration() {
        if (setStartTime != null && setEndTime != null) {
            return (int) ChronoUnit.SECONDS.between(setStartTime, setEndTime);
        }
        return actualSetDurationSeconds;
    }

    /**
     * Set timing for this set
     */
    public void recordSetTiming(LocalDateTime startTime, LocalDateTime endTime) {
        this.setStartTime = startTime;
        this.setEndTime = endTime;
        this.actualSetDurationSeconds = calculateActualSetDuration();
    }

    /**
     * Record rest time before this set
     */
    public void recordRestTime(Integer restSeconds) {
        this.restTimeBeforeSetSeconds = restSeconds;
    }

    /**
     * Compare performance vs planned targets
     */
    public void evaluatePerformanceVsTarget() {
        if (targetRepsPlanned == null && targetWeightPlanned == null) {
            this.performanceVsTarget = PerformanceVsTarget.NOT_SET;
            return;
        }

        boolean metReps = (targetRepsPlanned == null) ||
                (reps != null && reps >= targetRepsPlanned);
        boolean metWeight = (targetWeightPlanned == null) ||
                (weight != null && weight >= targetWeightPlanned);

        if (metReps && metWeight) {
            // Check if exceeded
            boolean exceededReps = (targetRepsPlanned != null) &&
                    (reps != null && reps > targetRepsPlanned);
            boolean exceededWeight = (targetWeightPlanned != null) &&
                    (weight != null && weight > targetWeightPlanned);

            if (exceededReps || exceededWeight) {
                this.performanceVsTarget = PerformanceVsTarget.EXCEEDED;
            } else {
                this.performanceVsTarget = PerformanceVsTarget.MET;
            }
        } else {
            this.performanceVsTarget = PerformanceVsTarget.BELOW;
        }
    }

    /**
     * Mark this as the final set of an exercise
     */
    public void markExerciseCompleted(boolean completed, String notes) {
        this.isExerciseCompleted = completed;
        this.exerciseCompletionNotes = notes;
    }

    /**
     * Get rest time in minutes and seconds format
     */
    public String getFormattedRestTime() {
        if (restTimeBeforeSetSeconds == null || restTimeBeforeSetSeconds == 0) {
            return "No rest recorded";
        }

        int minutes = restTimeBeforeSetSeconds / 60;
        int seconds = restTimeBeforeSetSeconds % 60;

        if (minutes == 0) {
            return seconds + "s";
        } else if (seconds == 0) {
            return minutes + "m";
        } else {
            return minutes + "m " + seconds + "s";
        }
    }

    /**
     * Get set duration in formatted string
     */
    public String getFormattedSetDuration() {
        Integer duration = actualSetDurationSeconds != null ?
                actualSetDurationSeconds :
                calculateActualSetDuration();

        if (duration == null || duration == 0) {
            return "Duration not recorded";
        }

        if (duration < 60) {
            return duration + "s";
        } else {
            int minutes = duration / 60;
            int seconds = duration % 60;
            return minutes + "m " + seconds + "s";
        }
    }

    /**
     * Check if this performance exceeded targets
     */
    public boolean exceededTargets() {
        return performanceVsTarget == PerformanceVsTarget.EXCEEDED;
    }

    /**
     * Check if this performance met targets
     */
    public boolean metTargets() {
        return performanceVsTarget == PerformanceVsTarget.MET;
    }

    // ==============================================
    // JPA LIFECYCLE METHODS
    // ==============================================

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ==============================================
    // ENUMS
    // ==============================================

    public enum AchievementStatus {
        NOT_SET("Not Set"),
        EXCEEDED("Exceeded Target"),
        MET("Met Target"),
        BELOW_TARGET("Below Target"),
        PARTIAL("Partially Completed");

        private final String displayName;

        AchievementStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum WorkoutIntensity {
        UNKNOWN("Unknown"),
        LOW("Low Intensity"),
        MODERATE("Moderate Intensity"),
        HIGH("High Intensity"),
        MAXIMUM("Maximum Intensity");

        private final String displayName;

        WorkoutIntensity(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum PerformanceVsTarget {
        EXCEEDED("Exceeded planned target"),
        MET("Met planned target exactly"),
        BELOW("Below planned target"),
        MODIFIED("Modified exercise/target"),
        NOT_SET("No target comparison available");

        private final String description;

        PerformanceVsTarget(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}