package com.chidituke.workout_tracker.dto.response.workout_session;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class WorkoutSessionAnalyticsResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalSessions;
    private Long totalDurationMinutes;
    private Long totalCaloriesBurned;
    private Double averageDurationMinutes;
    private Double averageEffortRating;
    private List<Object[]> moodStatistics;
}
