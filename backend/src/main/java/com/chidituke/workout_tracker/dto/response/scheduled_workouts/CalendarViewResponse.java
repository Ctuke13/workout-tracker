package com.chidituke.workout_tracker.dto.response.scheduled_workouts;

import com.chidituke.workout_tracker.dto.response.scheduled_workouts.ScheduledWorkoutResponse;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class CalendarViewResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private Map<LocalDate, List<ScheduledWorkoutResponse>> workoutsByDate;
    private Integer totalScheduled;
    private CalendarStats stats;

    @Data
    @Builder
    public static class CalendarStats {
        private Integer scheduledCount;
        private Integer completedCount;
        private Integer inProgressCount;
        private Integer overdueCount;
        private Integer cancelledCount;
        private Double completionRate;
    }
}
