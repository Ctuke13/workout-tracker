package com.chidituke.workout_tracker.dto.response.scheduled_workouts;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class UpcomingWorkoutsResponse {

    private LocalDate today;
    private List<ScheduledWorkoutResponse> todaysWorkouts;
    private List<ScheduledWorkoutResponse> tomorrowsWorkouts;
    private List<ScheduledWorkoutResponse> thisWeeksWorkouts;
    private List<ScheduledWorkoutResponse> overdueWorkouts;
    private Integer totalUpcoming;
    private String motivationalMessage;
}
