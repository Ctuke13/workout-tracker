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

    @Column(name = "created_by_professional")
    private Boolean createdByProfessional = false;

    // =======================
    //  PROFESSIONAL CONTENT METHODS
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
     * Check if this workout plan was created by a professional (convenience method)
     */
    public boolean isCreatedByProfessional() {
        return getCreatedByProfessional();
    }

    /**
     * Mark this workout plan as created by a professional
     */
    public void markAsProfeessionalContent() {
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

    // =======================
    // ENHANCED DOMAIN METHODS
    // =======================

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

    // Add enums:
    public enum WorkoutType {
        STRENGTH, CARDIO, FLEXIBILITY, MIXED, HIIT, POWERLIFTING
    }

    public enum DifficultyLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }

    // ⏰ JPA LIFECYCLE METHODS
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}