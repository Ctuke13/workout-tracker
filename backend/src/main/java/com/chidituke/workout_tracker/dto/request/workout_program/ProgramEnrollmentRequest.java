package com.chidituke.workout_tracker.dto.request.workout_program;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ProgramEnrollmentRequest {

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDate startDate;

    @Size(max = 200, message = "Notes cannot exceed 200 characters")
    private String notes;
}