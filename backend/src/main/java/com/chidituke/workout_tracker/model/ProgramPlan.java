package com.chidituke.workout_tracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// Add your actual package imports here:
// import your.package.entities.WorkoutProgram;
// import your.package.entities.WorkoutPlan;

@Entity
@Table(name = "program_plans",
        uniqueConstraints = @UniqueConstraint(columnNames = {"program_id", "week_number", "day_number"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private WorkoutProgram program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_plan_id", nullable = true)
    private WorkoutPlan workoutPlan;

    @Column(name = "week_number", nullable = false)
    @Min(value = 1, message = "Week number must be positive")
    @Max(value = 52, message = "Week number cannot exceed 52")
    private Integer weekNumber;

    @Column(name = "day_number", nullable = false)
    @Min(value = 1, message = "Day number must be between 1 and 7")
    @Max(value = 7, message = "Day number must be between 1 and 7")
    private Integer dayNumber;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "is_rest_day")
    @Builder.Default
    private Boolean isRestDay = false;

    @Column(name = "notes", length = 500)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "phase_type")
    private PhaseType phaseType;

    @Column(name = "target_intensity")
    @DecimalMin(value = "0.0", message = "Intensity must be non-negative")
    @DecimalMax(value = "100.0", message = "Intensity cannot exceed 100%")
    private BigDecimal targetIntensity;

    @Column(name = "is_optional")
    @Builder.Default
    private Boolean isOptional = false;

    // ✅ ADDED: Missing field that service expects
    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    // Business Logic Methods
    public String getDisplayName() {
        if (isRestDay) {
            return "Rest Day";
        }
        return workoutPlan != null ? workoutPlan.getWorkoutName() : "Workout";
    }

    public String getPhaseDescription() {
        return phaseType != null ? phaseType.getDescription() : "";
    }

    public boolean isValidSchedulePosition() {
        return weekNumber != null && weekNumber > 0 &&
                dayNumber != null && dayNumber >= 1 && dayNumber <= 7;
    }

    public String getWeekDayIdentifier() {
        return String.format("W%d-D%d", weekNumber, dayNumber);
    }

    public boolean canBeSkipped() {
        return isOptional || isRestDay;
    }

    public boolean isValidRestDay() {
        return isRestDay && workoutPlan == null;
    }

    public boolean isConsistent() {
        if (isRestDay) {
            return workoutPlan == null; // Rest days shouldn't have workout plans
        } else {
            return workoutPlan != null; // Workout days must have workout plans
        }
    }

    // Lifecycle callbacks for timestamp management
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        validateConsistency();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private void validateConsistency() {
        if (!isConsistent()) {
            throw new IllegalStateException(
                    String.format("Inconsistent ProgramPlan: isRestDay=%s but workoutPlan=%s",
                            isRestDay, workoutPlan != null ? "present" : "null"));
        }
    }


    public enum PhaseType {
        PREPARATION("Preparation Phase - Building foundation"),
        BASE_BUILDING("Base Building - Developing endurance and strength base"),
        INTENSITY("High Intensity - Peak performance training"),
        RECOVERY("Recovery Phase - Active recovery and restoration"),
        PEAK("Peak Performance - Competition preparation"),
        DELOAD("Deload Week - Reduced volume for recovery"),
        SPECIALIZATION("Specialization - Focus on specific skills/areas"),
        TRANSITION("Transition Phase - Moving between training blocks"),
        MAINTENANCE("Maintenance - Sustaining current fitness level");

        private final String description;

        PhaseType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public String getShortName() {
            return switch (this) {
                case PREPARATION -> "Prep";
                case BASE_BUILDING -> "Base";
                case INTENSITY -> "Intensity";
                case RECOVERY -> "Recovery";
                case PEAK -> "Peak";
                case DELOAD -> "Deload";
                case SPECIALIZATION -> "Special";
                case TRANSITION -> "Transition";
                case MAINTENANCE -> "Maintain";
            };
        }

        public boolean isHighIntensity() {
            return this == INTENSITY || this == PEAK || this == SPECIALIZATION;
        }

        public boolean isRecoveryFocused() {
            return this == RECOVERY || this == DELOAD || this == TRANSITION;
        }
    }
}