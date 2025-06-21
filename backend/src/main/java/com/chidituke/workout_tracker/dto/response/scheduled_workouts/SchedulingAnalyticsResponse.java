package com.chidituke.workout_tracker.dto.response.scheduled_workouts;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class SchedulingAnalyticsResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalScheduled;
    private Double completionRate;
    private List<DayOfWeekFrequency> frequencyByDayOfWeek;
    private Map<String, Object> additionalMetrics;

    @Data
    @Builder
    public static class DayOfWeekFrequency {
        private Integer dayOfWeek; // 1=Monday, 7=Sunday
        private String dayName;
        private Long count;
        private Double percentage;
    }
}

