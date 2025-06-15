package com.chidituke.workout_tracker.dto.request.exercise;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class ExerciseRatingRequestDTO {
    @NotNull(message = "Rating is required")
    @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "5.0", message = "Rating cannot exceed 5.0")
    private Double rating;

    @Size(max = 500, message = "Review comment too long")
    private String comment; // Optional review comment

    private List<String> tags; // "effective", "challenging", "fun", "confusing"
}