package com.chidituke.workout_tracker.model.workout;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Junction table entity for Exercise-Goal many-to-many relationship
 * Includes relevance scoring and primary goal designation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exercise_goal_mapping")
@IdClass(ExerciseGoalMappingId.class)
public class ExerciseGoalMapping {

    @Id
    @Column(name = "exercise_id")
    private Long exerciseId;

    @Id
    @Column(name = "goal_id")
    private Integer goalId;

    @NotNull(message = "Relevance score is required")
    @Min(value = 1, message = "Relevance score must be at least 1")
    @Max(value = 5, message = "Relevance score cannot exceed 5")
    @Column(name = "relevance_score", nullable = false)
    private Integer relevanceScore = 3;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 🔗 RELATIONSHIPS
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", insertable = false, updatable = false)
    private Exercise exercise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", insertable = false, updatable = false)
    private FitnessGoal fitnessGoal;

    // 🛠️ BUSINESS METHODS
    public boolean isPrimaryGoal() {
        return isPrimary != null && isPrimary;
    }

    public boolean isExcellentFit() {
        return relevanceScore != null && relevanceScore >= 4;
    }

    public boolean isGoodFit() {
        return relevanceScore != null && relevanceScore >= 3;
    }

    public String getRelevanceDescription() {
        if (relevanceScore == null) return "Unknown";
        return switch (relevanceScore) {
            case 5 -> "Perfect fit";
            case 4 -> "Great fit";
            case 3 -> "Good fit";
            case 2 -> "Fair fit";
            case 1 -> "Poor fit";
            default -> "Unknown";
        };
    }

    // ✅ ADDED MISSING METHODS THAT Exercise.java EXPECTS:

    /**
     * Get the fitness goal (accessor method for Exercise.java)
     */
    public FitnessGoal getFitnessGoal() {
        return this.fitnessGoal;
    }

    /**
     * Get the relevance score (accessor method for Exercise.java)
     */
    public Integer getRelevanceScore() {
        return this.relevanceScore;
    }

    // 📱 CONVENIENCE CONSTRUCTORS
    public ExerciseGoalMapping(Long exerciseId, Integer goalId, Integer relevanceScore, Boolean isPrimary) {
        this.exerciseId = exerciseId;
        this.goalId = goalId;
        this.relevanceScore = relevanceScore;
        this.isPrimary = isPrimary;
    }

    public ExerciseGoalMapping(Long exerciseId, Integer goalId, Integer relevanceScore, Boolean isPrimary, String notes) {
        this.exerciseId = exerciseId;
        this.goalId = goalId;
        this.relevanceScore = relevanceScore;
        this.isPrimary = isPrimary;
        this.notes = notes;
    }

    // ⏰ JPA LIFECYCLE METHODS
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}