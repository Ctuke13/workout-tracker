package com.chidituke.workout_tracker.dto.request.exercise;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class BulkExerciseActionRequestDTO {
    @NotEmpty(message = "Exercise IDs are required")
    @Size(max = 100, message = "Cannot perform bulk action on more than 100 exercises")
    private List<Long> exerciseIds;

    @NotBlank(message = "Action is required")
    @Pattern(regexp = "^(publish|unpublish|delete|approve|reject)$",
            message = "Action must be: publish, unpublish, delete, approve, or reject")
    private String action;

    @Size(max = 500, message = "Reason too long")
    private String reason; // Optional reason for admin actions
}