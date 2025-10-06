package com.chidituke.workout_tracker.model.workout;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@Entity
@Table(name = "exercises")
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exercise_id")
    private Long id;

    @NotBlank(message = "Exercise name is required")
    @Size(min = 2, max = 100, message = "Exercise name must be 2-100 characters")
    @Column(name = "exercise_name", nullable = false, length = 100)
    private String exerciseName;

    @Size(max = 10, message = "Emoji too long")
    @Column(length = 10)
    private String emoji;

    @Size(max = 2000, message = "Description too long")
    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "is_cardio", nullable = false)
    private Boolean isCardio = false;

    @Column(name = "is_isometric", nullable = false)
    private Boolean isIsometric = false;

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
    private Long createdByUserId;

    @Column(name = "created_by_professional")
    private Boolean createdByProfessional = false;

    @Min(value = 0, message = "Usage count cannot be negative")
    @Column(name = "usage_count")
    private Integer usageCount = 0; // Popularity tracking

    @DecimalMin(value = "0.0", message = "Average rating cannot be negative")
    @DecimalMax(value = "5.0", message = "Average rating cannot exceed 5.0")
    @Column(name = "average_rating")
    private Double averageRating = 0.0; // User ratings

    @Min(value = 0, message = "Total ratings cannot be negative")
    @Column(name = "total_ratings")
    private Integer totalRatings = 0; // Track number of ratings for accurate averaging

    @Column(nullable = false)
    private Boolean published = true;

    @Column(name = "is_favorite")
    private Boolean isFavorite = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 🎯 FITNESS GOALS RELATIONSHIP
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id") // Use JoinColumn instead of mappedBy for composite keys
    private List<ExerciseGoalMapping> goalMappings;

    // ==================== MET VALUES FOR CALORIE CALCULATION ====================

    @Column(name = "met_value_light")
    @DecimalMin(value = "0.0", message = "MET value cannot be negative")
    private Double metValueLight;

    @Column(name = "met_value_moderate")
    @DecimalMin(value = "0.0", message = "MET value cannot be negative")
    private Double metValueModerate;

    @Column(name = "met_value_vigorous")
    @DecimalMin(value = "0.0", message = "MET value cannot be negative")
    private Double metValueVigorous;

    @Column(name = "base_calories_per_minute")
    @Min(value = 0, message = "Base calories cannot be negative")
    private Integer baseCaloriesPerMinute;

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

    // 🔄 WORKOUT TRACKING MODALITY ENUM
    public enum WorkoutTrackingMode {
        TIME_BASED("Track time, distance, pace"),           // Cardio exercises
        HOLD_BASED("Track hold duration and sets"),         // Isometric exercises
        REP_BASED("Track sets, reps, and weight");          // Traditional strength

        private final String description;

        WorkoutTrackingMode(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // =======================
    // 🆕 STEP 2: FAVORITE METHODS
    // =======================

    /**
     * Get the favorite status with null safety
     */
    public Boolean getIsFavorite() {
        return this.isFavorite != null ? this.isFavorite : false;
    }

    /**
     * Set the favorite status
     */
    public void setIsFavorite(Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    /**
     * Check if exercise is marked as favorite (convenience method)
     */
    public boolean isFavorite() {
        return getIsFavorite();
    }

    /**
     * Mark this exercise as a favorite
     */
    public void markAsFavorite() {
        this.isFavorite = true;
    }

    /**
     * Remove this exercise from favorites
     */
    public void removeFromFavorites() {
        this.isFavorite = false;
    }

    /**
     * Toggle favorite status
     */
    public void toggleFavorite() {
        this.isFavorite = !getIsFavorite();
    }

    // =======================
    // 🆕 PROFESSIONAL CONTENT METHODS
    // =======================

    /**
     * Get the professional creation status with null safety
     */
    public Boolean getCreatedByProfessional() {
        return this.createdByProfessional != null ? this.createdByProfessional : false;
    }

    /**
     * Set the professional creation status
     */
    public void setCreatedByProfessional(Boolean createdByProfessional) {
        this.createdByProfessional = createdByProfessional;
    }

    /**
     * Check if this exercise was created by a professional (convenience method)
     */
    public boolean isCreatedByProfessional() {
        return getCreatedByProfessional();
    }

    /**
     * Mark this exercise as created by a professional
     */
    public void markAsProfessionalContent() {
        this.createdByProfessional = true;
    }

    /**
     * Mark this exercise as created by a regular user
     */
    public void markAsUserContent() {
        this.createdByProfessional = false;
    }

    // =======================
    // 🛠️ ENHANCED DOMAIN METHODS
    // =======================

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

    // 🔄 ENHANCED EXERCISE TYPE AND TRACKING METHODS

    public void setExerciseType(ExerciseType exerciseType) {
        this.exerciseType = exerciseType;
        // Automatically set isCardio based on exercise type
        this.isCardio = (exerciseType == ExerciseType.CARDIO);

        // Note: isIsometric should be set manually based on specific exercise characteristics
        // since it's not directly derivable from ExerciseType alone
    }

    public Boolean getIsCardio() {
        // Always derive from exerciseType to ensure consistency
        return this.exerciseType == ExerciseType.CARDIO;
    }

    public Boolean getIsIsometric() {
        return this.isIsometric != null ? this.isIsometric : false;
    }

    /**
     * Determines the workout tracking mode based on exercise characteristics
     */
    public WorkoutTrackingMode getWorkoutTrackingMode() {
        if (this.isCardio != null && this.isCardio) {
            return WorkoutTrackingMode.TIME_BASED;
        }
        if (this.isIsometric != null && this.isIsometric) {
            return WorkoutTrackingMode.HOLD_BASED;
        }
        return WorkoutTrackingMode.REP_BASED;
    }

    /**
     * Get tracking mode description for UI display
     */
    public String getTrackingModeDescription() {
        return getWorkoutTrackingMode().getDescription();
    }

    /**
     * Check if this is a time-based exercise (cardio)
     */
    public boolean isTimeBased() {
        return getWorkoutTrackingMode() == WorkoutTrackingMode.TIME_BASED;
    }

    /**
     * Check if this is a hold-based exercise (isometric)
     */
    public boolean isHoldBased() {
        return getWorkoutTrackingMode() == WorkoutTrackingMode.HOLD_BASED;
    }

    /**
     * Check if this is a rep-based exercise (traditional strength)
     */
    public boolean isRepBased() {
        return getWorkoutTrackingMode() == WorkoutTrackingMode.REP_BASED;
    }

    /**
     * Get tracking instructions for users
     */
    public String getTrackingInstructions() {
        switch (getWorkoutTrackingMode()) {
            case TIME_BASED:
                return "Track your time, distance, and pace during this exercise.";
            case HOLD_BASED:
                return "Track how long you can hold the position and rest between holds.";
            case REP_BASED:
                return "Track sets, repetitions, and weight used for this exercise.";
            default:
                return "Follow the exercise instructions and track your progress.";
        }
    }

    /**
     * Validate that isCardio and isIsometric settings are consistent
     */
    public boolean hasValidModalitySettings() {
        // Only one tracking mode should be true
        boolean cardio = this.isCardio != null && this.isCardio;
        boolean isometric = this.isIsometric != null && this.isIsometric;

        // Both cannot be true at the same time
        return !(cardio && isometric);
    }

    /**
     * Auto-correct modality settings if inconsistent
     */
    public void validateAndCorrectModality() {
        if (!hasValidModalitySettings()) {
            // If both are true, prioritize cardio (since it's derived from exerciseType)
            if (this.isCardio != null && this.isCardio) {
                this.isIsometric = false;
            }
        }
    }

    // 🎯 FITNESS GOALS METHODS

    /**
     * Get all fitness goals associated with this exercise
     */
    public List<FitnessGoal> getFitnessGoals() {
        if (goalMappings == null || goalMappings.isEmpty()) {
            return List.of();
        }
        return goalMappings.stream()
                .map(ExerciseGoalMapping::getFitnessGoal)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get the primary fitness goal for this exercise
     */
    public Optional<FitnessGoal> getPrimaryGoal() {
        if (goalMappings == null || goalMappings.isEmpty()) {
            return Optional.empty();
        }
        return goalMappings.stream()
                .filter(ExerciseGoalMapping::isPrimaryGoal)
                .map(ExerciseGoalMapping::getFitnessGoal)
                .findFirst();
    }

    /**
     * Get primary goal display text for UI
     */
    public String getPrimaryGoalDisplay() {
        return getPrimaryGoal()
                .map(FitnessGoal::getDisplayText)
                .orElse("General Fitness");
    }

    /**
     * Get primary goal code for API responses
     */
    public String getPrimaryGoalCode() {
        return getPrimaryGoal()
                .map(FitnessGoal::getGoalCode)
                .orElse("general-fitness");
    }

    /**
     * Get primary goal emoji for UI
     */
    public String getPrimaryGoalEmoji() {
        return getPrimaryGoal()
                .map(FitnessGoal::getGoalEmoji)
                .orElse("🎯");
    }

    /**
     * Check if exercise supports a specific goal (relevance >= 3)
     */
    public boolean supportsGoal(String goalCode) {
        if (goalMappings == null || goalMappings.isEmpty()) {
            return false;
        }
        return goalMappings.stream()
                .filter(mapping -> mapping.getRelevanceScore() >= 3) // Only good fits or better
                .map(ExerciseGoalMapping::getFitnessGoal)
                .filter(Objects::nonNull)
                .anyMatch(goal -> goalCode.equals(goal.getGoalCode()));
    }

    /**
     * Check if exercise is excellent fit for a goal (relevance >= 4)
     */
    public boolean isExcellentForGoal(String goalCode) {
        if (goalMappings == null || goalMappings.isEmpty()) {
            return false;
        }
        return goalMappings.stream()
                .filter(mapping -> mapping.getFitnessGoal() != null)
                .filter(mapping -> goalCode.equals(mapping.getFitnessGoal().getGoalCode()))
                .anyMatch(mapping -> mapping.getRelevanceScore() >= 4);
    }

    /**
     * Get relevance score for a specific goal
     */
    public int getRelevanceForGoal(String goalCode) {
        if (goalMappings == null || goalMappings.isEmpty()) {
            return 0;
        }
        return goalMappings.stream()
                .filter(mapping -> mapping.getFitnessGoal() != null)
                .filter(mapping -> goalCode.equals(mapping.getFitnessGoal().getGoalCode()))
                .mapToInt(ExerciseGoalMapping::getRelevanceScore)
                .findFirst()
                .orElse(0);
    }

    /**
     * Get all goal codes this exercise supports (relevance >= 3)
     */
    public List<String> getSupportedGoalCodes() {
        if (goalMappings == null || goalMappings.isEmpty()) {
            return List.of();
        }
        return goalMappings.stream()
                .filter(mapping -> mapping.getRelevanceScore() >= 3)
                .map(ExerciseGoalMapping::getFitnessGoal)
                .filter(Objects::nonNull)
                .map(FitnessGoal::getGoalCode)
                .collect(Collectors.toList());
    }

    /**
     * Get all goal codes with their relevance scores
     */
    public List<String> getAllGoalCodesWithRelevance() {
        if (goalMappings == null || goalMappings.isEmpty()) {
            return List.of();
        }
        return goalMappings.stream()
                .filter(mapping -> mapping.getFitnessGoal() != null)
                .map(mapping -> mapping.getFitnessGoal().getGoalCode() + ":" + mapping.getRelevanceScore())
                .collect(Collectors.toList());
    }

    /**
     * Check if exercise has any goal mappings
     */
    public boolean hasGoalMappings() {
        return goalMappings != null && !goalMappings.isEmpty();
    }

    /**
     * Get goal count for this exercise
     */
    public int getGoalCount() {
        return goalMappings != null ? goalMappings.size() : 0;
    }

    /**
     * Get highest relevance score across all goals
     */
    public int getHighestRelevanceScore() {
        if (goalMappings == null || goalMappings.isEmpty()) {
            return 0;
        }
        return goalMappings.stream()
                .mapToInt(ExerciseGoalMapping::getRelevanceScore)
                .max()
                .orElse(0);
    }

    /**
     * Check if this exercise is perfect for any goal (has any 5-star relevance)
     */
    public boolean isPerfectForAnyGoal() {
        return getHighestRelevanceScore() == 5;
    }

    /**
     * Get goals this exercise is excellent for (relevance >= 4)
     */
    public List<FitnessGoal> getExcellentGoals() {
        if (goalMappings == null || goalMappings.isEmpty()) {
            return List.of();
        }
        return goalMappings.stream()
                .filter(mapping -> mapping.getRelevanceScore() >= 4)
                .map(ExerciseGoalMapping::getFitnessGoal)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get a summary of goals for display (e.g., "💪 Build Muscle, 🔥 Lose Weight")
     */
    public String getGoalsSummary() {
        List<FitnessGoal> excellentGoals = getExcellentGoals();
        if (excellentGoals.isEmpty()) {
            return getPrimaryGoalDisplay();
        }

        return excellentGoals.stream()
                .limit(3) // Show max 3 goals to avoid clutter
                .map(FitnessGoal::getDisplayText)
                .collect(Collectors.joining(", "));
    }

    // ⏰ JPA LIFECYCLE METHODS
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        validateAndCorrectModality(); // Ensure consistent modality settings
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        validateAndCorrectModality(); // Ensure consistent modality settings
    }
}