package com.chidituke.workout_tracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Performance Record entity representing exercise performance data
 * Enhanced with comprehensive performance metrics and proper indexing
 */
@Data
@Entity
@Table(name = "performance_records", indexes = {
        @Index(name = "idx_performance_workout_log", columnList = "workout_log_id"),
        @Index(name = "idx_performance_exercise", columnList = "exercise_id"),
        @Index(name = "idx_performance_user_date", columnList = "workout_log_id, created_at"),
        @Index(name = "idx_performance_user_exercise", columnList = "workout_log_id, exercise_id"),
        @Index(name = "idx_performance_set_number", columnList = "workout_log_id, set_number")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"workoutLog", "exercise"})
@EqualsAndHashCode(exclude = {"workoutLog", "exercise"})
@EntityListeners(AuditingEntityListener.class)
public class PerformanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==============================================
    // CORE RELATIONSHIPS
    // ==============================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    @NotNull(message = "Exercise is required")
    private Exercise exercise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_log_id", nullable = false)
    @NotNull(message = "Workout log is required")
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
     * Check if this performance exceeded targets
     */
    public boolean exceededTargets() {
        return achievementStatus == AchievementStatus.EXCEEDED;
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
}