package com.chidituke.workout_tracker.dto.response.performance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for workout execution summary
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutExecutionSummary {
    private Long workoutSessionId;
    private String sessionStatus;
    private Integer totalExercisesPlanned;
    private Integer totalExercisesCompleted;
    private Double completionPercentage;
    private Integer totalDurationMinutes;
    private List<ExerciseExecutionSummary> exerciseSummaries;
    private String overallPerformanceRating;
}
