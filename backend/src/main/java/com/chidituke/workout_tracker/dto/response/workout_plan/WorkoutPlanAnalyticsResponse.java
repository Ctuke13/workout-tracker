package com.chidituke.workout_tracker.dto.response.workout_plan;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkoutPlanAnalyticsResponse {

    private Long workoutPlanId;
    private String workoutPlanName;
    private Integer timesUsed;
    private Long totalCompletions;
    private Integer exerciseCount;
    private Double averageRating;
    private String difficultyLevel;
    private String category;
}