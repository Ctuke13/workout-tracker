package com.chidituke.workout_tracker.dto.request.exercise;

import com.chidituke.workout_tracker.model.workout.Exercise;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class ExerciseCreateRequestDTO {
    @NotBlank(message = "Exercise name is required")
    @Size(min = 2, max = 100, message = "Exercise name must be 2-100 characters")
    private String name;

    @Size(max = 10, message = "Emoji too long")
    private String emoji;

    @Size(max = 2000, message = "Description too long")
    private String description;

    @NotNull(message = "Exercise type is required")
    private Exercise.ExerciseType exerciseType;

    @NotNull(message = "Difficulty level is required")
    private Exercise.DifficultyLevel difficultyLevel;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 480, message = "Duration cannot exceed 8 hours")
    private Integer estimatedDurationMinutes;

    @Min(value = 0, message = "Calories cannot be negative")
    @Max(value = 2000, message = "Calorie estimate seems too high")
    private Integer estimatedCalories;

    @Size(max = 200, message = "Target muscle groups list too long")
    private List<String> targetMuscleGroups;

    private List<String> equipmentRequired;

    private List<String> benefits;

    private List<String> tips;

    @Size(max = 500, message = "Video URL too long")
    @Pattern(regexp = "^(https?://)?(www\\.)?(youtube\\.com|youtu\\.be|vimeo\\.com).*",
            message = "Video URL must be from YouTube or Vimeo")
    private String videoUrl;

    private Boolean isIsometric = false;
}