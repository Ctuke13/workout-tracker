package com.chidituke.workout_tracker.dto.request.scheduled_workouts;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ScheduledWorkoutRequest {

    @NotNull(message = "Workout plan ID is required")
    private Long workoutPlanId;

    @NotNull(message = "Scheduled date is required")
    @Future(message = "Scheduled date must be in the future")
    private LocalDate scheduledDate;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String customNotes;

    private LocalDateTime reminderTime;

    // Optional program context
    private Long programId;

    @Min(value = 1, message = "Week number must be at least 1")
    @Max(value = 52, message = "Week number cannot exceed 52")
    private Integer weekNumber;

    private Integer estimatedDurationMinutes;
}