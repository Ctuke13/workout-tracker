package com.chidituke.workout_tracker.dto.request.exercise;

import com.chidituke.workout_tracker.model.workout.Exercise;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class ExerciseSearchRequestDTO {
    @Size(max = 100, message = "Search query too long")
    private String search;

    private Exercise.ExerciseType exerciseType;

    private Exercise.DifficultyLevel difficultyLevel;

    private List<String> muscleGroups;

    private List<String> equipment;

    private Boolean requiresEquipment;

    private Boolean canDoAtHome;

    private Boolean isHighlyRated; // 4.0+ rating

    private Boolean isPopular; // 100+ usage count

    private Boolean isProfessionalContent;

    @Min(value = 0, message = "Page cannot be negative")
    private Integer page = 0;

    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size cannot exceed 100")
    private Integer size = 20;

    private String sortBy = "name"; // name, rating, usage, created

    @Pattern(regexp = "^(asc|desc)$", message = "Sort direction must be 'asc' or 'desc'")
    private String sortDirection = "asc";
}