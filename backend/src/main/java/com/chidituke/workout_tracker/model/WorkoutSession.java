package com.chidituke.workout_tracker.model;

import com.chidituke.workout_tracker.controller.AuthController;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "workout_logs")
public class WorkoutSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = false)
    private WorkoutPlan workout;

    @Column(name = "total_duration_minutes")
    private Integer totalDurationMinutes;

    @Column(name = "estimated_calories")
    private Integer estimatedCalories;

    @Column(name = "difficulty_rating")
    @Min(value = 1, message = "Difficulty rating must be between 1 and 10")
    @Max(value = 10, message = "Difficulty rating must be between 1 and 10")
    private Integer difficultyRating;

    @Column(name = "overall_effort")
    @DecimalMin(value = "1.0", message = "Effort must be between 1 and 10")
    @DecimalMax(value = "10.0", message = "Effort must be between 1 and 10")
    private Double overallEffort;

    @Enumerated(EnumType.STRING)
    @Column(name = "mood")
    private WorkoutMood mood;

    @Enumerated(EnumType.STRING)
    @Column(name = "location")
    private WorkoutLocation location;

    // Program integration - NOTE: WorkoutProgram class needs to be created
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "program_id")
    private WorkoutProgram program;

    @Column(name = "week_number")
    private Integer weekNumber;

    // Social features
    @Column(name = "is_shared")
    private Boolean isShared = false;

    // Timestamps
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDate date;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Link to scheduled workout (if this session was from a scheduled workout)
    @OneToOne
    @JoinColumn(name = "scheduled_workout_id")
    private ScheduledWorkout scheduledWorkout;

    public boolean isFromScheduledWorkout() {
        return scheduledWorkout != null;
    }

    public boolean wasScheduledToday() {
        return isFromScheduledWorkout() && scheduledWorkout.isToday();
    }

    public boolean wasOverdue() {
        return isFromScheduledWorkout() && scheduledWorkout.isOverdue();
    }


    // ENUMS
    public enum WorkoutMood {
        ENERGETIC, TIRED, MOTIVATED, FOCUSED,
        STRESSED, RELAXED, PUMPED, SLUGGISH
    }

    public enum WorkoutLocation {
        HOME, GYM, PARK, OFFICE, HOTEL,
        BEACH, TRAIL, STUDIO, OTHER
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