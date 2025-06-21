package com.chidituke.workout_tracker.dto.response.workout_program;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class ProgramProgressResponse {

    private Long programId;
    private String programName;
    private Integer totalWeeks;
    private Integer completedWeeks;
    private Integer currentWeek;
    private Double progressPercentage;
    private Boolean isCompleted;
    private Integer totalSessions;
    private LocalDate nextWorkoutDate;
    private LocalDate estimatedCompletionDate;
}