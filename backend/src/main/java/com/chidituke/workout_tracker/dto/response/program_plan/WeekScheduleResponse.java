package com.chidituke.workout_tracker.dto.response.program_plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for week schedule details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeekScheduleResponse {

    private Integer weekNumber;
    private Long programId;
    private String programName;
    private List<DaySchedule> days;
    private WeekStatistics statistics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DaySchedule {
        private Integer dayNumber;
        private String dayName; // "Monday", "Tuesday", etc.
        private ProgramPlanResponse programPlan;
        private Boolean hasWorkout;
        private Boolean isRestDay;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeekStatistics {
        private Integer totalWorkouts;
        private Integer restDays;
        private BigDecimal averageIntensity;
        private Integer totalExercises;
        private String dominantPhase;
    }
}