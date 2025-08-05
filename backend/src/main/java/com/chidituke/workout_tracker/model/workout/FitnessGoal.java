package com.chidituke.workout_tracker.model.workout;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Fitness Goal entity - defines available fitness goals for exercises
 * Examples: build-muscle, lose-weight, improve-endurance, etc.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fitness_goals")
public class FitnessGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goal_id")
    private Integer goalId;

    @NotBlank(message = "Goal code is required")
    @Size(max = 50, message = "Goal code cannot exceed 50 characters")
    @Column(name = "goal_code", nullable = false, unique = true, length = 50)
    private String goalCode;  // e.g., "build-muscle", "lose-weight"

    @NotBlank(message = "Goal name is required")
    @Size(max = 100, message = "Goal name cannot exceed 100 characters")
    @Column(name = "goal_name", nullable = false, length = 100)
    private String goalName;  // e.g., "Build Muscle", "Lose Weight"

    @Size(max = 10, message = "Goal emoji cannot exceed 10 characters")
    @Column(name = "goal_emoji", length = 10)
    private String goalEmoji; // e.g., "💪", "🔥"

    @Column(name = "goal_description", columnDefinition = "TEXT")
    private String goalDescription;

    @NotNull(message = "Display order is required")
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 999;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 🔗 RELATIONSHIPS
    @OneToMany(mappedBy = "fitnessGoal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExerciseGoalMapping> exerciseMappings;

    // 🛠️ BUSINESS METHODS
    public boolean isActive() {
        return isActive != null && isActive;
    }

    public String getDisplayText() {
        if (goalEmoji != null && !goalEmoji.trim().isEmpty()) {
            return goalEmoji + " " + goalName;
        }
        return goalName;
    }

    // ✅ ADDED MISSING METHODS THAT Exercise.java EXPECTS:

    /**
     * Get the goal code (accessor method for Exercise.java)
     */
    public String getGoalCode() {
        return this.goalCode;
    }

    /**
     * Get the goal emoji (accessor method for Exercise.java)
     */
    public String getGoalEmoji() {
        return this.goalEmoji;
    }

    // 📱 CONVENIENCE CONSTRUCTORS
    public FitnessGoal(String goalCode, String goalName, String goalEmoji, String goalDescription, Integer displayOrder) {
        this.goalCode = goalCode;
        this.goalName = goalName;
        this.goalEmoji = goalEmoji;
        this.goalDescription = goalDescription;
        this.displayOrder = displayOrder;
        this.isActive = true;
    }

    // ⏰ JPA LIFECYCLE METHODS
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