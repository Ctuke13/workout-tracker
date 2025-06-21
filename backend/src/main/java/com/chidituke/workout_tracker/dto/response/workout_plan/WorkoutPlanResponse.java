package com.chidituke.workout_tracker.dto.response.workout_plan;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class WorkoutPlanResponse {

    private Long id;
    private String workoutName;
    private String workoutDescription;
    private String workoutCategory;
    private String workoutImageUrl;
    private Boolean isCardio;
    private String workoutType;
    private Integer estimatedDurationMinutes;
    private String difficultyLevel;
    private String targetMuscleGroups;
    private String equipmentNeeded;
    private String subscriptionTierRequired;

    // Creator info
    private Long createdByUserId;
    private Boolean isPublic;

    // Popularity metrics
    private Integer timesUsed;
    private Double averageRating;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}