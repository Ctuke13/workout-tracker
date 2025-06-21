package com.chidituke.workout_tracker.dto.request.workout_program;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class WorkoutProgramRequest {

    @NotBlank(message = "Program name is required")
    @Size(min = 2, max = 100, message = "Program name must be 2-100 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Program type is required")
    private String programType; // Will be converted to enum

    @NotNull(message = "Difficulty level is required")
    private String difficultyLevel; // Will be converted to enum

    @Min(value = 1, message = "Duration must be at least 1 week")
    @Max(value = 52, message = "Duration cannot exceed 52 weeks")
    @NotNull(message = "Duration in weeks is required")
    private Integer durationWeeks;

    @Min(value = 1, message = "Sessions per week must be at least 1")
    @Max(value = 7, message = "Sessions per week cannot exceed 7")
    @NotNull(message = "Sessions per week is required")
    private Integer sessionsPerWeek;

    @Size(max = 500, message = "Target goals cannot exceed 500 characters")
    private String targetGoals;

    @Size(max = 500, message = "Equipment needed cannot exceed 500 characters")
    private String equipmentNeeded;

    private Boolean isPublic = true;
}