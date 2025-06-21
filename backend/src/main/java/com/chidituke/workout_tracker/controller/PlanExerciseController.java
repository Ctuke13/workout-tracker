package com.chidituke.workout_tracker.controller;

import com.chidituke.workout_tracker.dto.request.plan_exercise.PlanExerciseRequest;
import com.chidituke.workout_tracker.dto.response.plan_exercise.PlanExerciseResponse;
import com.chidituke.workout_tracker.dto.response.plan_exercise.SupersetResponse;
import com.chidituke.workout_tracker.service.PlanExerciseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/workout-plans/{workoutPlanId}/exercises")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Plan Exercises", description = "Manage exercises within workout plans")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class PlanExerciseController {

    private final PlanExerciseService planExerciseService;

    // =======================
    // EXERCISE RETRIEVAL
    // =======================

    @GetMapping
    @Operation(summary = "Get exercises in workout plan",
            description = "Get all exercises in a workout plan")
    public ResponseEntity<List<PlanExerciseResponse>> getExercisesInWorkoutPlan(
            @Parameter(description = "Workout plan ID") @PathVariable Long workoutPlanId,
            Authentication authentication) {

        List<PlanExerciseResponse> exercises;

        if (authentication != null) {
            // Authenticated user - apply subscription filtering
            exercises = planExerciseService.getExercisesInWorkoutPlan(workoutPlanId, authentication.getName());
        } else {
            // Public access - only if workout plan is public
            exercises = planExerciseService.getExercisesInWorkoutPlan(workoutPlanId);
        }

        return ResponseEntity.ok(exercises);
    }

    @GetMapping("/{planExerciseId}")
    @Operation(summary = "Get specific plan exercise",
            description = "Get details of a specific exercise within a workout plan")
    public ResponseEntity<PlanExerciseResponse> getPlanExercise(
            @Parameter(description = "Workout plan ID") @PathVariable Long workoutPlanId,
            @Parameter(description = "Plan exercise ID") @PathVariable Long planExerciseId,
            Authentication authentication) {

        String username = authentication != null ? authentication.getName() : null;
        Optional<PlanExerciseResponse> planExercise = planExerciseService
                .getPlanExerciseById(planExerciseId, username);

        return planExercise.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =======================
    // EXERCISE MANAGEMENT (Authenticated users only)
    // =======================

    @PostMapping
    @Operation(summary = "Add exercise to workout plan",
            description = "Add a new exercise to the workout plan")
    public ResponseEntity<PlanExerciseResponse> addExerciseToWorkoutPlan(
            @Parameter(description = "Workout plan ID") @PathVariable Long workoutPlanId,
            @Parameter(description = "Exercise details") @RequestBody @Valid PlanExerciseRequest request,
            Authentication authentication) {

        PlanExerciseResponse planExercise = planExerciseService
                .addExerciseToWorkoutPlan(workoutPlanId, authentication.getName(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(planExercise);
    }

    @PutMapping("/{planExerciseId}")
    @Operation(summary = "Update plan exercise",
            description = "Update an exercise within the workout plan")
    public ResponseEntity<PlanExerciseResponse> updatePlanExercise(
            @Parameter(description = "Workout plan ID") @PathVariable Long workoutPlanId,
            @Parameter(description = "Plan exercise ID") @PathVariable Long planExerciseId,
            @Parameter(description = "Updated exercise details") @RequestBody @Valid PlanExerciseRequest request,
            Authentication authentication) {

        PlanExerciseResponse planExercise = planExerciseService
                .updatePlanExercise(planExerciseId, authentication.getName(), request);

        return ResponseEntity.ok(planExercise);
    }

    @DeleteMapping("/{planExerciseId}")
    @Operation(summary = "Remove exercise from workout plan",
            description = "Remove an exercise from the workout plan")
    public ResponseEntity<Void> removePlanExercise(
            @Parameter(description = "Workout plan ID") @PathVariable Long workoutPlanId,
            @Parameter(description = "Plan exercise ID") @PathVariable Long planExerciseId,
            Authentication authentication) {

        planExerciseService.removePlanExercise(planExerciseId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    // =======================
    // EXERCISE ORDERING
    // =======================

    @PutMapping("/reorder")
    @Operation(summary = "Reorder exercises in workout plan",
            description = "Change the order of exercises in the workout plan")
    public ResponseEntity<List<PlanExerciseResponse>> reorderExercises(
            @Parameter(description = "Workout plan ID") @PathVariable Long workoutPlanId,
            @Parameter(description = "Ordered list of plan exercise IDs") @RequestBody List<Long> planExerciseIds,
            Authentication authentication) {

        List<PlanExerciseResponse> reorderedExercises = planExerciseService
                .reorderExercises(workoutPlanId, authentication.getName(), planExerciseIds);

        return ResponseEntity.ok(reorderedExercises);
    }

    // =======================
    // SUPERSET MANAGEMENT
    // =======================

    @PostMapping("/supersets")
    @Operation(summary = "Create superset",
            description = "Group exercises into a superset")
    public ResponseEntity<List<PlanExerciseResponse>> createSuperset(
            @Parameter(description = "Workout plan ID") @PathVariable Long workoutPlanId,
            @Parameter(description = "Plan exercise IDs to group") @RequestParam List<Long> planExerciseIds,
            @Parameter(description = "Superset group name") @RequestParam String supersetGroup,
            Authentication authentication) {

        List<PlanExerciseResponse> supersetExercises = planExerciseService
                .createSuperset(workoutPlanId, authentication.getName(), planExerciseIds, supersetGroup);

        return ResponseEntity.status(HttpStatus.CREATED).body(supersetExercises);
    }

    @DeleteMapping("/supersets/{supersetGroup}")
    @Operation(summary = "Remove superset",
            description = "Remove superset grouping from exercises")
    public ResponseEntity<Void> removeSuperset(
            @Parameter(description = "Workout plan ID") @PathVariable Long workoutPlanId,
            @Parameter(description = "Superset group name") @PathVariable String supersetGroup,
            Authentication authentication) {

        planExerciseService.removeSuperset(workoutPlanId, authentication.getName(), supersetGroup);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/supersets/{supersetGroup}")
    @Operation(summary = "Get superset exercises",
            description = "Get all exercises in a specific superset")
    public ResponseEntity<List<PlanExerciseResponse>> getSupersetExercises(
            @Parameter(description = "Workout plan ID") @PathVariable Long workoutPlanId,
            @Parameter(description = "Superset group name") @PathVariable String supersetGroup) {

        List<PlanExerciseResponse> supersetExercises = planExerciseService
                .getSupersetExercises(workoutPlanId, supersetGroup);

        return ResponseEntity.ok(supersetExercises);
    }

    // =======================
    // PRESCRIPTION MANAGEMENT
    // =======================

    @PutMapping("/{planExerciseId}/prescription")
    @Operation(summary = "Update exercise prescription",
            description = "Update sets, reps, weight, and other prescription details")
    public ResponseEntity<PlanExerciseResponse> updateExercisePrescription(
            @Parameter(description = "Workout plan ID") @PathVariable Long workoutPlanId,
            @Parameter(description = "Plan exercise ID") @PathVariable Long planExerciseId,
            @Parameter(description = "Prescription details")
            @RequestBody @Valid PlanExerciseService.ExercisePrescriptionRequest prescription,
            Authentication authentication) {

        PlanExerciseResponse planExercise = planExerciseService
                .updateExercisePrescription(planExerciseId, authentication.getName(), prescription);

        return ResponseEntity.ok(planExercise);
    }

    // =======================
    // ANALYTICS & QUERIES
    // =======================

    @GetMapping("/progression")
    @Operation(summary = "Get progression exercises",
            description = "Get exercises marked for progression tracking")
    public ResponseEntity<List<PlanExerciseResponse>> getProgressionExercises(
            @Parameter(description = "Workout plan ID") @PathVariable Long workoutPlanId,
            Authentication authentication) {

        List<PlanExerciseResponse> progressionExercises = planExerciseService
                .getExercisesWithProgression(workoutPlanId, authentication.getName());

        return ResponseEntity.ok(progressionExercises);
    }
}