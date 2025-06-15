package com.chidituke.workout_tracker.dto.request.exercise;

import com.chidituke.workout_tracker.model.Exercise;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class WorkoutPlanRequestDTO {
    @NotEmpty(message = "At least one target muscle group is required")
    @Size(max = 10, message = "Cannot target more than 10 muscle groups")
    private List<String> targetMuscleGroups;

    private List<String> availableEquipment;

    @NotNull(message = "Maximum difficulty level is required")
    private Exercise.DifficultyLevel maxDifficulty;

    @Min(value = 5, message = "Workout duration must be at least 5 minutes")
    @Max(value = 180, message = "Workout duration cannot exceed 3 hours")
    private Integer targetDurationMinutes;

    @Min(value = 1, message = "Must have at least 1 exercise per muscle group")
    @Max(value = 5, message = "Cannot have more than 5 exercises per muscle group")
    private Integer exercisesPerMuscleGroup = 2;

    private Boolean homeWorkout = false; // Only bodyweight/home equipment

    private Boolean quickWorkout = false; // Under 30 minutes, high intensity
}