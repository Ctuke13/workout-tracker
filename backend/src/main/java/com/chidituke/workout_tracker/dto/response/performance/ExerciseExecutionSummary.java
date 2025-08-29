package com.chidituke.workout_tracker.dto.response.performance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for individual exercise execution summary
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseExecutionSummary {
    private Long exerciseId;
    private String exerciseName;
    private Boolean isCompleted;
    private Integer totalSets;
    private Double averageRpe;
    private Double averageFormRating;
    private Double totalVolume;
    private List<PerformanceResponse> performanceRecords;
}
