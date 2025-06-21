package com.chidituke.workout_tracker.dto.response.scheduled_workouts;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ProgramScheduleResponse {

    private Long programId;
    private String programName;
    private Integer durationWeeks;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<ScheduledWorkoutResponse> scheduledWorkouts;
    private Integer totalWorkouts;
    private Integer completedWorkouts;
    private Double progressPercentage;
    private Integer currentWeek;
    private LocalDate nextWorkoutDate;
}