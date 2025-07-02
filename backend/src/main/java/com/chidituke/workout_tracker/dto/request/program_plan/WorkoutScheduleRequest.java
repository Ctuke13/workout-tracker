package com.chidituke.workout_tracker.dto.request.program_plan;

import com.chidituke.workout_tracker.model.workout.ProgramPlan;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for scheduling a workout in a program
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutScheduleRequest {

    private Long workoutPlanId;

    @NotNull(message = "Week number is required")
    @Min(value = 1, message = "Week number must be at least 1")
    @Max(value = 52, message = "Week number cannot exceed 52")
    private Integer weekNumber;

    @NotNull(message = "Day number is required")
    @Min(value = 1, message = "Day number must be between 1 and 7")
    @Max(value = 7, message = "Day number must be between 1 and 7")
    private Integer dayNumber;

    private ProgramPlan.PhaseType phaseType;

    @DecimalMin(value = "0.0", message = "Target intensity must be at least 0.0")
    @DecimalMax(value = "100.0", message = "Target intensity cannot exceed 100.0")
    private BigDecimal targetIntensity;

    @Builder.Default
    private Boolean isOptional = false;

    // ✅ ADDED: Support for rest days
    @Builder.Default
    private Boolean isRestDay = false;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
