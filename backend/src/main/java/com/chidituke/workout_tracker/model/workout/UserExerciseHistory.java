package com.chidituke.workout_tracker.model.workout;

import com.chidituke.workout_tracker.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tracks user exercise usage history for recommendations and analytics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_exercise_history",
        indexes = {
                @Index(name = "idx_user_exercise_history_user", columnList = "user_id"),
                @Index(name = "idx_user_exercise_history_exercise", columnList = "exercise_id"),
                @Index(name = "idx_user_exercise_history_used_at", columnList = "used_at"),
                @Index(name = "idx_user_exercise_history_context", columnList = "context")
        })
public class UserExerciseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @NotNull
    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 480, message = "Duration cannot exceed 8 hours")
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @NotBlank
    @Size(max = 20)
    @Column(length = 20, nullable = false)
    private String context; // "view", "workout", "favorite", "rate", "share"

    @Size(max = 200)
    @Column(length = 200)
    private String notes; // Optional user notes about the session

    @Column(name = "workout_plan_id")
    private Long workoutPlanId; // If part of a structured workout

    // 🛠️ BUSINESS METHODS

    public boolean isRecentUsage() {
        return usedAt != null && usedAt.isAfter(LocalDateTime.now().minusDays(7));
    }

    public boolean isWorkoutUsage() {
        return "workout".equals(context);
    }

    public boolean isViewOnly() {
        return "view".equals(context);
    }

    public boolean isFavoriteAction() {
        return "favorite".equals(context);
    }

    public boolean hasNotes() {
        return notes != null && !notes.trim().isEmpty();
    }

    public boolean isPartOfWorkoutPlan() {
        return workoutPlanId != null;
    }

    // 📱 CONVENIENCE CONSTRUCTORS
    public UserExerciseHistory(User user, Exercise exercise, String context) {
        this.user = user;
        this.exercise = exercise;
        this.context = context;
        this.usedAt = LocalDateTime.now();
    }

    public UserExerciseHistory(User user, Exercise exercise, String context, Integer durationMinutes) {
        this(user, exercise, context);
        this.durationMinutes = durationMinutes;
    }

    public UserExerciseHistory(User user, Exercise exercise, String context, Integer durationMinutes, String notes) {
        this(user, exercise, context, durationMinutes);
        this.notes = notes;
    }

    // 📊 CONTEXT CONSTANTS
    public static final String CONTEXT_VIEW = "view";
    public static final String CONTEXT_WORKOUT = "workout";
    public static final String CONTEXT_FAVORITE = "favorite";
    public static final String CONTEXT_RATE = "rate";
    public static final String CONTEXT_SHARE = "share";

    // ⏰ JPA LIFECYCLE METHODS
    @PrePersist
    protected void onCreate() {
        if (usedAt == null) {
            usedAt = LocalDateTime.now();
        }
    }
}