package com.chidituke.workout_tracker.controller.exercise;

import com.chidituke.workout_tracker.controller.BaseApiController;
import com.chidituke.workout_tracker.dto.request.exercise.*;
import com.chidituke.workout_tracker.dto.response.exercise.ExerciseResponseDTO;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.workout.Exercise;
import com.chidituke.workout_tracker.security.CurrentUser;
import com.chidituke.workout_tracker.security.UserPrincipal;
import com.chidituke.workout_tracker.service.exercise.ExerciseQueryService;
import com.chidituke.workout_tracker.service.exercise.ExerciseUserService;
import com.chidituke.workout_tracker.service.user.UserService;
import com.chidituke.workout_tracker.service.exercise.ExerciseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/exercises/user")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@Tag(name = "Exercise User Operations", description = "Personalized exercise operations for authenticated users")
@PreAuthorize("isAuthenticated()")
public class ExerciseUserController extends BaseApiController {

    private final ExerciseService exerciseService;
    private final UserService userService;
    private final ExerciseQueryService exerciseQueryService;
    private final ExerciseUserService exerciseUserService;

    // ===================================================================
    // PERSONALIZED RECOMMENDATIONS
    // ===================================================================

    @GetMapping("/recommended")
    @Operation(summary = "Get recommended exercises", description = "Get recommended exercises based on user profile")
    public ResponseEntity<List<ExerciseResponseDTO>> getRecommendedExercises(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "10") int limit) {

        try {
            User currentUser = userService.getUserById(userPrincipal.getId());
            List<Exercise> recommended = exerciseUserService.findRecommendedExercises(currentUser, limit);
            List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(recommended);

            log.debug("Retrieved {} recommended exercises for user {}", response.size(), userPrincipal.getId());
            return okList(response);

        } catch (Exception e) {
            log.error("Failed to get recommended exercises for user {}", userPrincipal.getId(), e);
            return okList(List.of()); // Return empty list on error
        }
    }

    @GetMapping("/insights")
    @Operation(summary = "Get user insights", description = "Get user exercise insights and analytics")
    public ResponseEntity<ExerciseUserService.UserExerciseInsights> getUserInsights(
            @CurrentUser UserPrincipal userPrincipal) {

        try {
            User currentUser = userService.getUserById(userPrincipal.getId());
            ExerciseUserService.UserExerciseInsights insights = exerciseUserService.getUserExerciseInsights(currentUser);

            log.debug("Retrieved insights for user {}", userPrincipal.getId());
            return ok(insights);

        } catch (Exception e) {
            log.error("Failed to get insights for user {}", userPrincipal.getId(), e);
            // Return null and let the service handle the default response
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===================================================================
    // WORKOUT PLAN GENERATION
    // ===================================================================

    @PostMapping("/workout-plan")
    @Operation(summary = "Generate workout plan", description = "Generate personalized workout plan")
    public ResponseEntity<List<ExerciseResponseDTO>> generateWorkoutPlan(
            @Valid @RequestBody ExerciseSelectionRequestDTO planRequest,
            @CurrentUser UserPrincipal userPrincipal) {

        try {
            User currentUser = userService.getUserById(userPrincipal.getId());
            List<Exercise> workoutPlan = exerciseUserService.buildWorkoutPlan(currentUser, planRequest);
            List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(workoutPlan);

            log.debug("Generated workout plan with {} exercises for user {}", response.size(), userPrincipal.getId());
            return okList(response);

        } catch (Exception e) {
            log.error("Failed to generate workout plan for user {}", userPrincipal.getId(), e);
            return okList(List.of()); // Return empty list on error
        }
    }

    // ===================================================================
    // EXERCISE INTERACTION & FEEDBACK
    // ===================================================================

    @PostMapping("/{id}/rate")
    @Operation(summary = "Rate exercise", description = "Rate an exercise with optional comment and tags")
    public ResponseEntity<Map<String, Object>> rateExercise(
            @PathVariable Long id,
            @Valid @RequestBody ExerciseRatingRequestDTO ratingRequest,
            @CurrentUser UserPrincipal userPrincipal) {

        try {
            User currentUser = userService.getUserById(userPrincipal.getId());
            exerciseService.rateExercise(
                    id,
                    currentUser,
                    ratingRequest.getRating(),
                    ratingRequest.getComment(),
                    ratingRequest.getTags()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Exercise rated successfully");
            response.put("exerciseId", id);
            response.put("rating", ratingRequest.getRating());

            log.debug("User {} rated exercise {} with rating {}", userPrincipal.getId(), id, ratingRequest.getRating());
            return ok(response);

        } catch (Exception e) {
            log.error("Failed to rate exercise {} for user {}", id, userPrincipal.getId(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to rate exercise");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ===================================================================
    // USAGE TRACKING
    // ===================================================================

    @PostMapping("/{id}/use")
    @Operation(summary = "Record usage", description = "Record exercise usage")
    public ResponseEntity<Map<String, Object>> recordExerciseUsage(
            @PathVariable Long id,
            @CurrentUser UserPrincipal userPrincipal) {

        try {
            User currentUser = userService.getUserById(userPrincipal.getId());
            exerciseUserService.recordExerciseUsage(id, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Exercise usage recorded");
            response.put("exerciseId", id);

            log.debug("Recorded exercise usage for exercise {} by user {}", id, userPrincipal.getId());
            return ok(response);

        } catch (Exception e) {
            log.error("Failed to record exercise usage for exercise {} by user {}", id, userPrincipal.getId(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to record exercise usage");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/{id}/workout")
    @Operation(summary = "Record workout usage", description = "Record workout usage with duration and notes")
    public ResponseEntity<Map<String, Object>> recordWorkoutUsage(
            @PathVariable Long id,
            @RequestParam(required = false) Integer durationMinutes,
            @RequestParam(required = false) String notes,
            @CurrentUser UserPrincipal userPrincipal) {

        try {
            User currentUser = userService.getUserById(userPrincipal.getId());
            exerciseService.recordWorkoutUsage(id, currentUser, durationMinutes, notes);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Workout usage recorded");
            response.put("exerciseId", id);
            response.put("durationMinutes", durationMinutes);

            log.debug("Recorded workout usage for exercise {} by user {} (duration: {} mins)",
                    id, userPrincipal.getId(), durationMinutes);
            return ok(response);

        } catch (Exception e) {
            log.error("Failed to record workout usage for exercise {} by user {}", id, userPrincipal.getId(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to record workout usage");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ===================================================================
    // WORKOUT MODES & CAPABILITIES
    // ===================================================================

    @GetMapping("/workout-modes")
    @Operation(summary = "Get workout modes", description = "Get available workout tracking modes")
    public ResponseEntity<List<Map<String, Object>>> getWorkoutTrackingModes() {
        try {
            List<Map<String, Object>> modes = List.of(
                    createWorkoutModeInfo(Exercise.WorkoutTrackingMode.TIME_BASED, "Cardio/Time-Based"),
                    createWorkoutModeInfo(Exercise.WorkoutTrackingMode.HOLD_BASED, "Isometric/Hold-Based"),
                    createWorkoutModeInfo(Exercise.WorkoutTrackingMode.REP_BASED, "Strength/Rep-Based")
            );

            return okList(modes);

        } catch (Exception e) {
            log.error("Failed to get workout modes", e);
            return okList(List.of());
        }
    }

    @GetMapping("/by-workout-mode/{mode}")
    @Operation(summary = "Get exercises by workout mode", description = "Get exercises filtered by workout tracking mode")
    public ResponseEntity<List<ExerciseResponseDTO>> getExercisesByWorkoutMode(
            @PathVariable String mode,
            @RequestParam(defaultValue = "20") int limit) {

        try {
            List<Exercise> exercises = switch (mode.toUpperCase()) {
                case "TIME_BASED" -> exerciseQueryService.findCardioExercises();
                case "HOLD_BASED" -> exerciseQueryService.findIsometricExercises();
                case "REP_BASED" -> exerciseQueryService.findRepBasedExercises();
                default -> List.of();
            };

            if (exercises.isEmpty() && !isValidWorkoutMode(mode)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid workout mode: " + mode);
                return ResponseEntity.badRequest().build();
            }

            List<ExerciseResponseDTO> response = exercises.stream()
                    .limit(limit)
                    .map(ExerciseResponseDTO::fromEntity)
                    .toList();

            log.debug("Retrieved {} exercises for workout mode: {}", response.size(), mode);
            return okList(response);

        } catch (Exception e) {
            log.error("Failed to get exercises for workout mode: {}", mode, e);
            return okList(List.of());
        }
    }

    // ===================================================================
    // HELPER METHODS
    // ===================================================================

    private Map<String, Object> createWorkoutModeInfo(Exercise.WorkoutTrackingMode mode, String displayName) {
        Map<String, Object> modeInfo = new HashMap<>();
        modeInfo.put("mode", mode.name());
        modeInfo.put("description", mode.getDescription());
        modeInfo.put("displayName", displayName);
        return modeInfo;
    }

    private boolean isValidWorkoutMode(String mode) {
        return mode.toUpperCase().matches("TIME_BASED|HOLD_BASED|REP_BASED");
    }
}