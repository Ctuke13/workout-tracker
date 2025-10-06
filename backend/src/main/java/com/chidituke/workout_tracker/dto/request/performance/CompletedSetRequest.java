package com.chidituke.workout_tracker.dto.request.performance;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Individual set completion data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletedSetRequest {
    @NotNull
    private Integer setNumber;

    private Integer targetReps;
    private Integer actualReps;
    private Double targetWeight;
    private Double actualWeight;
    private String targetWeightUnit;
    private Integer rpe;
    private Integer restSeconds;
    private Integer actualRestSeconds;
    private Boolean completed;
    private Integer actualDurationMinutes;
    private Integer actualHoldSeconds;
    private String notes;
}
