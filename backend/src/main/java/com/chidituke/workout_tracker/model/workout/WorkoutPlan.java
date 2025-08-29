package com.chidituke.workout_tracker.model.workout;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@Entity
@Table(name = "workout_plans")
public class WorkoutPlan {

    // =============================================================================
    // ENTITY FIELDS
    // =============================================================================

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

    // =============================================================================
    // CREATOR AND POPULARITY TRACKING
    // =============================================================================

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "is_public")
    private Boolean isPublic = true;

    @Column(name = "times_used")
    private Integer timesUsed = 0;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "created_by_professional")
    private Boolean createdByProfessional = false;

    // =============================================================================
    // TIMESTAMPS
    // =============================================================================

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // =============================================================================
    // RELATIONSHIPS
    // =============================================================================

    @OneToMany(mappedBy = "workoutPlan", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("orderInWorkout ASC") // Maintain exercise order
    private List<PlanExercise> planExercises = new ArrayList<>();

    // =============================================================================
    // ENUMS
    // =============================================================================

    public enum WorkoutType {
        STRENGTH, CARDIO, FLEXIBILITY, MIXED, HIIT, POWERLIFTING
    }

    public enum DifficultyLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }

    // =============================================================================
    // EXERCISE RELATIONSHIP METHODS
    // =============================================================================

    /**
     * Get all PlanExercise entities for this workout plan
     */
    public List<PlanExercise> getPlanExercises() {
        return planExercises != null ? planExercises : new ArrayList<>();
    }

    /**
     * Set the plan exercises for this workout plan
     */
    public void setPlanExercises(List<PlanExercise> planExercises) {
        this.planExercises = planExercises != null ? planExercises : new ArrayList<>();
    }

    /**
     * Get all exercises in this workout plan (convenience method)
     */
    public List<Exercise> getExercises() {
        return getPlanExercises().stream()
                .map(PlanExercise::getExercise)
                .filter(Objects::nonNull) // ✅ FIXED: Now Objects import is available
                .collect(Collectors.toList());
    }

    /**
     * Get the first exercise in this workout plan
     */
    public Exercise getFirstExercise() {
        List<Exercise> exercises = getExercises();
        return exercises.isEmpty() ? null : exercises.get(0);
    }

    /**
     * Get exercise by order in workout
     */
    public Exercise getExerciseByOrder(int order) {
        return getPlanExercises().stream()
                .filter(pe -> pe.getOrderInWorkout() != null && pe.getOrderInWorkout() == order)
                .map(PlanExercise::getExercise)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the number of exercises in this workout plan
     */
    public int getExerciseCount() {
        return getPlanExercises().size();
    }

    /**
     * Check if this workout plan has any exercises
     */
    public boolean hasExercises() {
        return !getPlanExercises().isEmpty();
    }

    /**
     * Check if this is a single-exercise workout plan
     */
    public boolean isSingleExercise() {
        return getExerciseCount() == 1;
    }

    /**
     * Add an exercise to this workout plan
     */
    public PlanExercise addExercise(Exercise exercise, int order) {
        PlanExercise planExercise = new PlanExercise();
        planExercise.setWorkoutPlan(this);
        planExercise.setExercise(exercise);
        planExercise.setOrderInWorkout(order);

        if (this.planExercises == null) {
            this.planExercises = new ArrayList<>();
        }
        this.planExercises.add(planExercise);

        return planExercise;
    }

    /**
     * Remove an exercise from this workout plan
     */
    public void removeExercise(Exercise exercise) {
        if (this.planExercises != null) {
            this.planExercises.removeIf(pe ->
                    pe.getExercise() != null && pe.getExercise().getId().equals(exercise.getId()));
        }
    }

    // =============================================================================
    // EXERCISE TYPE ANALYSIS METHODS
    // =============================================================================

    /**
     * Get exercises by type (cardio, strength, etc.)
     */
    public List<Exercise> getExercisesByType(Exercise.ExerciseType type) {
        return getExercises().stream()
                .filter(exercise -> exercise.getExerciseType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Check if workout plan contains any cardio exercises
     */
    public boolean hasCardioExercises() {
        return getExercises().stream()
                .anyMatch(exercise -> exercise.getIsCardio() != null && exercise.getIsCardio());
    }

    /**
     * Check if workout plan contains any isometric exercises
     */
    public boolean hasIsometricExercises() {
        return getExercises().stream()
                .anyMatch(exercise -> exercise.getIsIsometric() != null && exercise.getIsIsometric());
    }

    /**
     * Check if workout plan contains any strength exercises
     */
    public boolean hasStrengthExercises() {
        return getExercises().stream()
                .anyMatch(exercise -> !exercise.getIsCardio() && !exercise.getIsIsometric());
    }

    /**
     * Get the primary exercise type of this workout plan
     */
    public Exercise.ExerciseType getPrimaryExerciseType() {
        List<Exercise> exercises = getExercises();
        if (exercises.isEmpty()) {
            return Exercise.ExerciseType.STRENGTH; // Default
        }

        // Count exercise types
        Map<Exercise.ExerciseType, Long> typeCounts = exercises.stream()
                .collect(Collectors.groupingBy(Exercise::getExerciseType, Collectors.counting()));

        // Return the most common type
        return typeCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Exercise.ExerciseType.STRENGTH);
    }

    // =============================================================================
    // PROFESSIONAL CONTENT METHODS
    // =============================================================================

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
     * Check if this workout plan was created by a professional (convenience method)
     */
    public boolean isCreatedByProfessional() {
        return getCreatedByProfessional();
    }

    /**
     * Mark this workout plan as created by a professional
     */
    public void markAsProfessionalContent() { // ✅ FIXED: Typo in method name
        this.createdByProfessional = true;
    }

    /**
     * Mark this workout plan as created by a regular user
     */
    public void markAsUserContent() {
        this.createdByProfessional = false;
    }

    /**
     * Check if this is verified professional content
     */
    public boolean isVerifiedContent() {
        return isCreatedByProfessional();
    }

    /**
     * Get content source description for UI display
     */
    public String getContentSourceDescription() {
        return isCreatedByProfessional() ? "Professional Content" : "Community Content";
    }

    /**
     * Get content badge for UI display
     */
    public String getContentBadge() {
        return isCreatedByProfessional() ? "👨‍⚕️ PRO" : "👤 User";
    }

    // =============================================================================
    // ACCESSIBILITY AND FEATURES METHODS
    // =============================================================================

    /**
     * Check if this workout plan is publicly accessible
     */
    public boolean isPubliclyAccessible() {
        return isPublic != null && isPublic;
    }

    /**
     * Check if this workout plan is popular (high usage)
     */
    public boolean isPopular() {
        return timesUsed != null && timesUsed > 50;
    }

    /**
     * Check if this workout plan is highly rated
     */
    public boolean isHighlyRated() {
        return averageRating != null && averageRating >= 4.0;
    }

    /**
     * Check if this workout plan requires equipment
     */
    public boolean requiresEquipment() {
        return equipmentNeeded != null &&
                !equipmentNeeded.trim().isEmpty() &&
                !equipmentNeeded.equalsIgnoreCase("None");
    }

    /**
     * Check if this workout plan is accessible to a specific subscription tier
     */
    public boolean isAccessibleToTier(String userTier) {
        if (subscriptionTierRequired == null || "FREE".equals(subscriptionTierRequired)) {
            return true; // Free content accessible to everyone
        }

        if (userTier == null) {
            return false; // Premium content not accessible to unsubscribed users
        }

        return switch (userTier.toUpperCase()) {
            case "FREE" -> "FREE".equals(subscriptionTierRequired);
            case "PLUS" -> "FREE".equals(subscriptionTierRequired) || "PLUS".equals(subscriptionTierRequired);
            case "PRO" -> true; // Pro tier can access everything
            default -> false;
        };
    }

    /**
     * Increment the usage count when workout plan is used
     */
    public void incrementUsage() {
        this.timesUsed = (this.timesUsed == null ? 0 : this.timesUsed) + 1;
    }

    // =============================================================================
    // DISPLAY AND FORMATTING METHODS
    // =============================================================================

    /**
     * Get difficulty description for UI display
     */
    public String getDifficultyDescription() {
        if (difficultyLevel == null) {
            return "Unknown difficulty";
        }

        return switch (difficultyLevel) {
            case BEGINNER -> "Beginner - Perfect for newcomers";
            case INTERMEDIATE -> "Intermediate - Some experience recommended";
            case ADVANCED -> "Advanced - For experienced athletes";
        };
    }

    /**
     * Get workout type description for UI display
     */
    public String getWorkoutTypeDescription() {
        if (workoutType == null) {
            return "Mixed workout";
        }

        return switch (workoutType) {
            case STRENGTH -> "Strength Training";
            case CARDIO -> "Cardiovascular Training";
            case FLEXIBILITY -> "Flexibility & Mobility";
            case MIXED -> "Mixed Training";
            case HIIT -> "High-Intensity Interval Training";
            case POWERLIFTING -> "Powerlifting";
        };
    }

    /**
     * Get subscription tier description for UI display
     */
    public String getSubscriptionTierDescription() {
        if (subscriptionTierRequired == null) {
            return "Free Access";
        }

        return switch (subscriptionTierRequired.toUpperCase()) {
            case "FREE" -> "Free Access";
            case "PLUS" -> "Plus Subscription Required";
            case "PRO" -> "Pro Subscription Required";
            default -> "Premium Access Required";
        };
    }

    /**
     * Get formatted duration for UI display
     */
    public String getFormattedDuration() {
        if (estimatedDurationMinutes == null) {
            return "Duration varies";
        }

        if (estimatedDurationMinutes < 60) {
            return estimatedDurationMinutes + " min";
        } else {
            int hours = estimatedDurationMinutes / 60;
            int minutes = estimatedDurationMinutes % 60;
            return minutes > 0 ? hours + "h " + minutes + "m" : hours + "h";
        }
    }

    /**
     * Get a summary of target muscle groups
     */
    public String getMuscleGroupsSummary() {
        if (targetMuscleGroups == null || targetMuscleGroups.trim().isEmpty()) {
            return "Full body";
        }

        String[] groups = targetMuscleGroups.split(",");
        if (groups.length <= 2) {
            return targetMuscleGroups.replace(",", ", ");
        } else {
            return groups[0] + ", " + groups[1] + " +" + (groups.length - 2) + " more";
        }
    }

    // =============================================================================
    // JPA LIFECYCLE METHODS
    // =============================================================================

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}