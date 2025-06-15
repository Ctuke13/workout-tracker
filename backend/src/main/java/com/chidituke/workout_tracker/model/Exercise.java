package com.chidituke.workout_tracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@Entity
@Table(name = "exercises")
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Exercise name is required")
    @Size(min = 2, max = 100, message = "Exercise name must be 2-100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 10, message = "Emoji too long")
    @Column(length = 10)
    private String emoji;

    @Size(max = 2000, message = "Description too long")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type", nullable = false)
    private ExerciseType exerciseType = ExerciseType.STRENGTH;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false)
    private DifficultyLevel difficultyLevel = DifficultyLevel.BEGINNER;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 480, message = "Duration cannot exceed 8 hours")
    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @Min(value = 0, message = "Calories cannot be negative")
    @Max(value = 2000, message = "Calorie estimate seems too high")
    @Column(name = "estimated_calories")
    private Integer estimatedCalories;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "exercise_muscle_groups", joinColumns = @JoinColumn(name = "exercise_id"))
    @Column(name = "muscle_group", length = 50)
    private List<String> targetMuscleGroups; // ["CHEST", "SHOULDERS", "TRICEPS"]

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "exercise_equipment", joinColumns = @JoinColumn(name = "exercise_id"))
    @Column(name = "equipment", length = 50)
    private List<String> equipmentRequired;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "exercise_benefits", joinColumns = @JoinColumn(name = "exercise_id"))
    @Column(name = "benefit", length = 100)
    private List<String> benefits;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "exercise_tips", joinColumns = @JoinColumn(name = "exercise_id"))
    @Column(name = "tip", length = 200)
    private List<String> tips;

    @Size(max = 500, message = "Video URL too long")
    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "created_by_user_id")
    private Long createdByUserId; // Who created this exercise

    @Column(name = "created_by_professional")
    private Boolean createdByProfessional = false;

//    @Column(name = "verified_exercise")
//    private Boolean verifiedExercise = false; // Admin or professional verified

    @Min(value = 0, message = "Usage count cannot be negative")
    @Column(name = "usage_count")
    private Integer usageCount = 0; // Popularity tracking

    @DecimalMin(value = "0.0", message = "Average rating cannot be negative")
    @DecimalMax(value = "5.0", message = "Average rating cannot exceed 5.0")
    @Column(name = "average_rating", precision = 3, scale = 2)
    private Double averageRating = 0.0; // User ratings

    @Min(value = 0, message = "Total ratings cannot be negative")
    @Column(name = "total_ratings")
    private Integer totalRatings = 0; // Track number of ratings for accurate averaging

    @Column(nullable = false)
    private Boolean published = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 📱 ENUMS
    public enum ExerciseType {
        STRENGTH("Strength Training"),
        CARDIO("Cardiovascular"),
        FLEXIBILITY("Flexibility & Mobility"),
        BALANCE("Balance & Stability"),
        PLYOMETRIC("Plyometric & Power"),
        REHABILITATION("Rehabilitation"),
        SPORTS_SPECIFIC("Sports Specific");

        private final String displayName;

        ExerciseType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum DifficultyLevel {
        BEGINNER("Beginner - No experience needed"),
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

    // 🛠️ SIMPLE DOMAIN METHODS (Keep in Model)

    public boolean requiresEquipment() {
        return equipmentRequired != null && !equipmentRequired.isEmpty();
    }

    public boolean isPublished() {
        return published != null && published;
    }

    public boolean isPopular() {
        return usageCount != null && usageCount > 100; // Configurable threshold
    }

    public boolean isHighlyRated() {
        return averageRating != null && averageRating >= 4.0 && totalRatings >= 5;
    }

    public boolean isFromVerifiedSource() {
        return createdByProfessional != null && createdByProfessional;
    }

    // Removed subscription gating - all exercises are free to access

    public String getEquipmentSummary() {
        if (!requiresEquipment()) {
            return "No equipment needed";
        }

        if (equipmentRequired.size() == 1) {
            return equipmentRequired.get(0);
        }

        return equipmentRequired.size() + " items needed";
    }

    public String getDifficultyDescription() {
        return difficultyLevel != null ? difficultyLevel.getDescription() : "Unknown difficulty";
    }

    public List<String> getTargetMuscleGroupsList() {
        if (targetMuscleGroups == null || targetMuscleGroups.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(targetMuscleGroups); // Return defensive copy
    }

    public boolean targetsMuscleGroup(String muscleGroup) {
        if (targetMuscleGroups == null || targetMuscleGroups.isEmpty()) {
            return false;
        }
        return targetMuscleGroups.stream()
                .anyMatch(group -> group.trim().equalsIgnoreCase(muscleGroup.trim()));
    }

    public void incrementUsage() {
        this.usageCount = (this.usageCount == null ? 0 : this.usageCount) + 1;
    }

    public boolean canDoAtHome() {
        if (!requiresEquipment()) {
            return true; // No equipment needed
        }

        // Check if only basic home equipment
        List<String> homeEquipment = List.of("dumbbells", "resistance_bands",
                "yoga_mat", "kettlebell", "jump_rope",
                "resistance_band", "bodyweight");
        return equipmentRequired.stream()
                .allMatch(equipment -> homeEquipment.contains(equipment.toLowerCase().trim()));
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