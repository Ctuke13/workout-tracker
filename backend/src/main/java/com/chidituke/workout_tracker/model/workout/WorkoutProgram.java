package com.chidituke.workout_tracker.model.workout;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "workout_programs")
public class WorkoutProgram {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_program_id")
    private Long id;

    @NotBlank(message = "Program name is required")
    @Size(min = 2, max = 100, message = "Program name must be 2-100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 1000, message = "Description too long")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "program_type", nullable = false)
    private ProgramType programType = ProgramType.STRENGTH;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false)
    private DifficultyLevel difficultyLevel = DifficultyLevel.BEGINNER;

    @Min(value = 1, message = "Duration must be at least 1 week")
    @Max(value = 52, message = "Duration cannot exceed 52 weeks")
    @Column(name = "duration_weeks", nullable = false)
    private Integer durationWeeks;

    @Min(value = 1, message = "Sessions per week must be at least 1")
    @Max(value = 7, message = "Sessions per week cannot exceed 7")
    @Column(name = "sessions_per_week", nullable = false)
    private Integer sessionsPerWeek;

    @Column(name = "target_goals", length = 500)
    private String targetGoals;

    @Column(name = "equipment_needed", length = 500)
    private String equipmentNeeded;

    // Creator information
    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "created_by_professional")
    private Boolean createdByProfessional = false;

    // Status and visibility
    @Column(name = "is_published")
    private Boolean isPublished = true;

    @Column(name = "is_public")
    private Boolean isPublic = true;

    // Usage tracking
    @Column(name = "enrollment_count")
    private Integer enrollmentCount = 0;

    @Column(name = "completion_count")
    private Integer completionCount = 0;

    @DecimalMin(value = "0.0", message = "Average rating cannot be negative")
    @DecimalMax(value = "5.0", message = "Average rating cannot exceed 5.0")
    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Min(value = 0, message = "Total ratings cannot be negative")
    @Column(name = "total_ratings")
    private Integer totalRatings = 0;

    // Timestamps
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ENUMS
    public enum ProgramType {
        STRENGTH("Strength Training"),
        CARDIO("Cardiovascular"),
        WEIGHT_LOSS("Weight Loss"),
        MUSCLE_GAIN("Muscle Gain"),
        ENDURANCE("Endurance"),
        FLEXIBILITY("Flexibility"),
        REHABILITATION("Rehabilitation"),
        SPORTS_SPECIFIC("Sports Specific");

        private final String displayName;

        ProgramType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum DifficultyLevel {
        BEGINNER("Beginner - Perfect for starting out"),
        INTERMEDIATE("Intermediate - Some experience recommended"),
        ADVANCED("Advanced - For experienced athletes");

        private final String description;

        DifficultyLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // BUSINESS METHODS
    public boolean isActive() {
        return isPublished != null && isPublished;
    }

    public boolean isPopular() {
        return enrollmentCount != null && enrollmentCount > 50;
    }

    public boolean isHighlyRated() {
        return averageRating != null && averageRating >= 4.0 && totalRatings >= 5;
    }

    public double getCompletionRate() {
        if (enrollmentCount == null || enrollmentCount == 0) {
            return 0.0;
        }
        return (completionCount != null ? completionCount : 0) * 100.0 / enrollmentCount;
    }

    public void incrementEnrollment() {
        this.enrollmentCount = (this.enrollmentCount == null ? 0 : this.enrollmentCount) + 1;
    }

    public void incrementCompletion() {
        this.completionCount = (this.completionCount == null ? 0 : this.completionCount) + 1;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}