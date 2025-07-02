package com.chidituke.workout_tracker.model.workout;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "workout_plans")
public class WorkoutPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_plan_id")
    private Long id;

    @Column(nullable = false)
    private String workoutName;

    @Column(columnDefinition = "TEXT")
    private String workoutDescription;

    @Column(nullable = false)
    private String workoutCategory;

    @Column(name = "image_url")
    private String workoutImageUrl;

    @Column(name = "is_cardio")
    private boolean isCardio = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "workout_type")
    private WorkoutType workoutType = WorkoutType.STRENGTH;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    private DifficultyLevel difficultyLevel = DifficultyLevel.BEGINNER;

    @Column(name = "target_muscle_groups")
    private String targetMuscleGroups; // "CHEST,SHOULDERS,TRICEPS"

    @Column(name = "equipment_needed")
    private String equipmentNeeded;

    @Column(name = "subscription_tier_required")
    private String subscriptionTierRequired = "FREE";

    // Creator and popularity tracking:
    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "is_public")
    private Boolean isPublic = true;

    @Column(name = "times_used")
    private Integer timesUsed = 0;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    // Timestamps:
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Add enums:
    public enum WorkoutType {
        STRENGTH, CARDIO, FLEXIBILITY, MIXED, HIIT, POWERLIFTING
    }

    public enum DifficultyLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
}