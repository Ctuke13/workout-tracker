package com.chidituke.workout_tracker.dto.response.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for Performance Tracker Chart data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceTrackerResponse {

    private String metric;           // WEIGHT, VOLUME, REPS, etc.
    private String period;           // WEEK, MONTH, YEAR, ALL_TIME
    private List<DataPoint> dataPoints;
    private Summary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPoint {
        private String date;         // ISO date format: "2025-01-15"
        private Double value;        // Metric value
        private Integer workoutCount; // Number of workouts on this date
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Double average;
        private Double peak;
        private Double low;
        private String trend;        // UP, DOWN, STABLE
        private Double trendPercentage;
        private Integer totalDataPoints;
    }
}
