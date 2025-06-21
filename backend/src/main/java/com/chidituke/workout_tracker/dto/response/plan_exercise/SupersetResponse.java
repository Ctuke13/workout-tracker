package com.chidituke.workout_tracker.dto.response.plan_exercise;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SupersetResponse {

    private String supersetGroup;
    private String supersetType; // "SUPERSET", "TRISET", "CIRCUIT"
    private List<PlanExerciseResponse> exercises;
    private Integer totalExercises;
    private String instructions; // "Perform exercises back-to-back with no rest"
}
