package com.chidituke.workout_tracker.dto.response.program_plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for program plan details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramPlanResponse {

    private Long id;
    private Long programId;
    private String programName;
    private Long workoutPlanId;
    private String workoutPlanName;
    private Integer weekNumber;
    private Integer dayNumber;
    private String phaseType;
    private BigDecimal targetIntensity;
    private Boolean isOptional;
    private Boolean isRestDay;
    private String notes;
    private Integer displayOrder;
    private Long createdByUserId;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Workout plan details (if needed)
    private WorkoutPlanSummary workoutPlanSummary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkoutPlanSummary {
        private String name;
        private String description;
        private Integer estimatedDuration;
        private String difficulty;
        private Integer exerciseCount;
        private String primaryMuscleGroups;
    }
}

