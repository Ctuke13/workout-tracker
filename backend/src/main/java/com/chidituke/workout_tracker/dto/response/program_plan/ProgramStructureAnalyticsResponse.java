package com.chidituke.workout_tracker.dto.response.program_plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for program structure analytics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramStructureAnalyticsResponse {

    private Integer totalWeeks;
    private Integer totalPlans;
    private Integer workoutDays;
    private Integer restDays;
    private BigDecimal averageIntensity;
    private List<Integer> weekNumbers;

    // Weekly breakdown
    private Map<Integer, WeekSummary> weekSummaries;

    // Phase analysis
    private Map<String, PhaseSummary> phaseSummaries;

    // Intensity distribution
    private IntensityDistribution intensityDistribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeekSummary {
        private Integer weekNumber;
        private Integer workoutCount;
        private Integer restDayCount;
        private BigDecimal averageIntensity;
        private List<String> phaseTypes;
        private Integer totalExercises;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhaseSummary {
        private String phaseType;
        private Integer weekCount;
        private Integer workoutCount;
        private BigDecimal averageIntensity;
        private List<Integer> weekNumbers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IntensityDistribution {
        private Integer lowIntensity;    // 1-3
        private Integer mediumIntensity; // 4-7
        private Integer highIntensity;   // 8-10
        private BigDecimal averageIntensity;
    }
}