// UserExerciseRating.java - Goes in model.workout package
package com.chidituke.workout_tracker.model.workout;

import com.chidituke.workout_tracker.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tracks user ratings and reviews for exercises
 * Prevents duplicate ratings with unique constraint
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_exercise_ratings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "exercise_id"}))
public class UserExerciseRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rating_id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @NotNull(message = "Rating is required")
    @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "5.0", message = "Rating cannot exceed 5.0")
    @Column(nullable = false)
    private Double rating;

    @Size(max = 500, message = "Review comment too long")
    @Column(length = 500)
    private String comment;

    @ElementCollection
    @CollectionTable(name = "user_rating_tags", joinColumns = @JoinColumn(name = "rating_id"))
    @Column(name = "tag", length = 50)
    private List<String> tags; // "effective", "challenging", "fun", "confusing"

    @Column(name = "rated_at", nullable = false, updatable = false)
    private LocalDateTime ratedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 🛠️ BUSINESS METHODS

    public boolean isPositiveRating() {
        return rating != null && rating >= 3.0;
    }

    public boolean isHighRating() {
        return rating != null && rating >= 4.0;
    }

    public boolean hasComment() {
        return comment != null && !comment.trim().isEmpty();
    }

    public boolean hasTags() {
        return tags != null && !tags.isEmpty();
    }

    public String getRatingDescription() {
        if (rating == null) return "No rating";
        if (rating >= 4.5) return "Excellent";
        if (rating >= 3.5) return "Good";
        if (rating >= 2.5) return "Average";
        if (rating >= 1.5) return "Poor";
        return "Very Poor";
    }

    // 📱 CONVENIENCE CONSTRUCTORS
    public UserExerciseRating(User user, Exercise exercise, Double rating) {
        this.user = user;
        this.exercise = exercise;
        this.rating = rating;
        this.ratedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UserExerciseRating(User user, Exercise exercise, Double rating, String comment) {
        this(user, exercise, rating);
        this.comment = comment;
    }

    public UserExerciseRating(User user, Exercise exercise, Double rating, String comment, List<String> tags) {
        this(user, exercise, rating, comment);
        this.tags = tags;
    }

    // ⏰ JPA LIFECYCLE METHODS
    @PrePersist
    protected void onCreate() {
        ratedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}