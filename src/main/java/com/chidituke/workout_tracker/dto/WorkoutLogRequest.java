package com.chidituke.workout_tracker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WorkoutLogRequest {
    @NotNull
    private Long workoutId;

    private LocalDate date = LocalDate.now();

    private String notes;
}
