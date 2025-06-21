package com.chidituke.workout_tracker.dto.request.program_plan;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for bulk adding workouts to a program
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkAddRequest {

    @NotEmpty(message = "Workouts list cannot be empty")
    @Valid
    private List<WorkoutScheduleRequest> workouts;

    @Builder.Default
    private Boolean validateConflicts = true;

    private String notes;
}