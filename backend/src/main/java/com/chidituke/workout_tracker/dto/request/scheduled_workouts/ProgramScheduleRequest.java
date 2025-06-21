package com.chidituke.workout_tracker.dto.request.scheduled_workouts;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProgramScheduleRequest {

    @NotNull(message = "Program ID is required")
    private Long programId;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDate startDate;

    @Size(max = 7, message = "Cannot specify more than 7 days per week")
    private List<Integer> preferredDaysOfWeek; // 1=Monday, 7=Sunday

    private Boolean skipWeekends = false;

    @Size(max = 200, message = "Notes cannot exceed 200 characters")
    private String notes;
}