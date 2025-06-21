package com.chidituke.workout_tracker.dto.request.scheduled_workouts;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RescheduleWorkoutRequest {

    @NotNull(message = "New scheduled date is required")
    @Future(message = "New scheduled date must be in the future")
    private LocalDate newScheduledDate;

    @Size(max = 200, message = "Reschedule reason cannot exceed 200 characters")
    private String reason;
}