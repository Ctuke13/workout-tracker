package com.chidituke.workout_tracker.dto.response.messaging;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for workout session summary
 */
@Data
@Builder
public class WorkoutSessionSummaryResponse {

    private Long id;
    private String workoutName;
    private LocalDateTime sessionDate;
    private Integer durationMinutes;
    private String notes;
    private WorkoutPlanSummaryResponse workoutPlan;
}