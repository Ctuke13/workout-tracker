package com.chidituke.workout_tracker.dto.response.scheduled_workouts;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class WorkoutConflictResponse {

    private Boolean hasConflicts;
    private List<ConflictDetail> conflicts;
    private List<String> suggestions;

    @Data
    @Builder
    public static class ConflictDetail {
        private LocalDate date;
        private String conflictType; // "SAME_TIME", "SAME_DAY", "OVERLAPPING"
        private String description;
        private Long existingWorkoutId;
        private String existingWorkoutName;
    }
}