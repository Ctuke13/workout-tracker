package com.chidituke.workout_tracker.dto.response.messaging;

import lombok.Builder;
import lombok.Data;

/**
 * Response DTO for workout plan summary
 */
@Data
@Builder
public class WorkoutPlanSummaryResponse {

    private Long id;
    private String workoutName;
    private String description;
    private String difficultyLevel;
    private Integer estimatedDurationMinutes;
    private UserSummaryResponse createdBy;
}
