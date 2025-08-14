package com.chidituke.workout_tracker.model.workout;

import com.chidituke.workout_tracker.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing the many-to-many relationship between users and their favorite exercises.
 * This junction table approach provides scalability and proper relational database design.
 */
@Entity
@Table(name = "user_exercise_favorites")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"userId", "exerciseId"})
@ToString(exclude = {"user", "exercise"})
public class UserExerciseFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "exercise_id", nullable = false)
    private Long exerciseId;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // ==================== RELATIONSHIPS ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", insertable = false, updatable = false)
    private Exercise exercise;

    // ==================== CONSTRUCTORS ====================

    /**
     * Create a new favorite relationship
     */
    public UserExerciseFavorite(Long userId, Long exerciseId) {
        this.userId = userId;
        this.exerciseId = exerciseId;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Create a new favorite relationship with entities
     */
    public UserExerciseFavorite(User user, Exercise exercise) {
        this.userId = user.getId();
        this.exerciseId = exercise.getId();
        this.user = user;
        this.exercise = exercise;
        this.createdAt = LocalDateTime.now();
    }

    // ==================== LIFECYCLE METHODS ====================

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // ==================== BUSINESS METHODS ====================

    /**
     * Get display name for this favorite
     */
    public String getDisplayName() {
        if (exercise != null) {
            return exercise.getExerciseName();
        }
        return "Exercise ID: " + exerciseId;
    }

    /**
     * Check if this favorite was created recently (within last 24 hours)
     */
    public boolean isRecentlyAdded() {
        if (createdAt == null) return false;
        return createdAt.isAfter(LocalDateTime.now().minusDays(1));
    }

    /**
     * Get the age of this favorite in days
     */
    public long getFavoriteAgeDays() {
        if (createdAt == null) return 0;
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toDays();
    }

    /**
     * Check if this favorite belongs to a specific user
     */
    public boolean belongsToUser(Long checkUserId) {
        return this.userId != null && this.userId.equals(checkUserId);
    }

    /**
     * Check if this favorite is for a specific exercise
     */
    public boolean isForExercise(Long checkExerciseId) {
        return this.exerciseId != null && this.exerciseId.equals(checkExerciseId);
    }
}
