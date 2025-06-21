package com.chidituke.workout_tracker.dto.response.plan_exercise;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class WorkoutStructureResponse {

    private Long workoutPlanId;
    private String workoutPlanName;
    private Integer totalExercises;
    private Integer estimatedDurationMinutes;

    // Grouped exercises
    private List<PlanExerciseResponse> regularExercises;
    private List<SupersetResponse> supersets;
    private List<PlanExerciseResponse> optionalExercises;

    // Statistics
    private Map<String, Integer> exercisesByMuscleGroup;
    private Map<String, Integer> exercisesByEquipment;
    private String difficultyLevel;
}