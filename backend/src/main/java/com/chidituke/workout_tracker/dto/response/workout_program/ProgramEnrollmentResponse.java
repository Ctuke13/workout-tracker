package com.chidituke.workout_tracker.dto.response.workout_program;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class ProgramEnrollmentResponse {

    private Long programId;
    private String programName;
    private LocalDate enrollmentDate;
    private Integer durationWeeks;
    private Integer sessionsPerWeek;
    private LocalDate estimatedCompletionDate;
    private String status;
}