package com.chidituke.workout_tracker.controller;

import com.chidituke.workout_tracker.dto.request.workout_plan.WorkoutPlanRequest;
import com.chidituke.workout_tracker.dto.response.workout_plan.WorkoutPlanAnalyticsResponse;
import com.chidituke.workout_tracker.dto.response.workout_plan.WorkoutPlanListResponse;
import com.chidituke.workout_tracker.dto.response.workout_plan.WorkoutPlanResponse;
import com.chidituke.workout_tracker.dto.response.workout_plan.WorkoutPlanSearchResponse;
import com.chidituke.workout_tracker.exceptions.workout_plan.WorkoutPlanNotFoundException;
import com.chidituke.workout_tracker.exceptions.common.UnauthorizedOperationException;
import com.chidituke.workout_tracker.model.PlanExercise;
import com.chidituke.workout_tracker.model.WorkoutPlan.DifficultyLevel;
import com.chidituke.workout_tracker.model.WorkoutPlan.WorkoutType;
import com.chidituke.workout_tracker.service.WorkoutPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/workout-plans")
@RequiredArgsConstructor
@Tag(name = "Workout Plans", description = "API for managing workout plans")
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;

    // =======================
    // PUBLIC DISCOVERY ENDPOINTS
    // =======================

    @GetMapping
    @Operation(summary = "Get all public workout plans", description = "Retrieve all publicly available workout plans ordered by popularity")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved workout plans")
    public ResponseEntity<WorkoutPlanListResponse> getAllPublicWorkoutPlans() {
        List<WorkoutPlanResponse> workoutPlans = workoutPlanService.getAllPublicWorkoutPlans();

        WorkoutPlanListResponse response = WorkoutPlanListResponse.builder()
                .workoutPlans(workoutPlans)
                .totalCount(workoutPlans.size())
                .currentPage(0)
                .totalPages(1)
                .pageSize(workoutPlans.size())
                .hasNext(false)
                .hasPrevious(false)
                .isFiltered(false)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get workout plan by ID", description = "Retrieve a specific workout plan by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workout plan found"),
            @ApiResponse(responseCode = "404", description = "Workout plan not found")
    })
    public ResponseEntity<WorkoutPlanResponse> getWorkoutPlanById(
            @Parameter(description = "Workout plan ID") @PathVariable Long id,
            Principal principal) {

        Optional<WorkoutPlanResponse> workoutPlan;

        if (principal != null) {
            // User is authenticated, check both public and owned plans
            workoutPlan = workoutPlanService.getWorkoutPlanById(id, principal.getName());
        } else {
            // Not authenticated, only public plans
            workoutPlan = workoutPlanService.getWorkoutPlanById(id);
        }

        return workoutPlan
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get workout plans by category", description = "Retrieve workout plans filtered by category")
    public ResponseEntity<WorkoutPlanListResponse> getWorkoutPlansByCategory(
            @Parameter(description = "Workout category") @PathVariable String category) {

        List<WorkoutPlanResponse> workoutPlans = workoutPlanService.getWorkoutPlansByCategory(category);

        WorkoutPlanListResponse response = WorkoutPlanListResponse.builder()
                .workoutPlans(workoutPlans)
                .totalCount(workoutPlans.size())
                .category(category)
                .isFiltered(true)
                .appliedFilters(List.of("category=" + category))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get workout plans by type", description = "Retrieve workout plans filtered by workout type")
    public ResponseEntity<WorkoutPlanListResponse> getWorkoutPlansByType(
            @Parameter(description = "Workout type") @PathVariable WorkoutType type) {

        List<WorkoutPlanResponse> workoutPlans = workoutPlanService.getWorkoutPlansByType(type);

        WorkoutPlanListResponse response = WorkoutPlanListResponse.builder()
                .workoutPlans(workoutPlans)
                .totalCount(workoutPlans.size())
                .workoutType(type.name())
                .isFiltered(true)
                .appliedFilters(List.of("type=" + type.name()))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/difficulty/{difficulty}")
    @Operation(summary = "Get workout plans by difficulty", description = "Retrieve workout plans filtered by difficulty level")
    public ResponseEntity<WorkoutPlanListResponse> getWorkoutPlansByDifficulty(
            @Parameter(description = "Difficulty level") @PathVariable DifficultyLevel difficulty) {

        List<WorkoutPlanResponse> workoutPlans = workoutPlanService.getWorkoutPlansByDifficulty(difficulty);

        WorkoutPlanListResponse response = WorkoutPlanListResponse.builder()
                .workoutPlans(workoutPlans)
                .totalCount(workoutPlans.size())
                .difficulty(difficulty.name())
                .isFiltered(true)
                .appliedFilters(List.of("difficulty=" + difficulty.name()))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/equipment/{equipment}")
    @Operation(summary = "Get workout plans by equipment", description = "Retrieve workout plans that use specific equipment")
    public ResponseEntity<WorkoutPlanListResponse> getWorkoutPlansByEquipment(
            @Parameter(description = "Equipment type") @PathVariable String equipment) {

        List<WorkoutPlanResponse> workoutPlans = workoutPlanService.getWorkoutPlansByEquipment(equipment);

        WorkoutPlanListResponse response = WorkoutPlanListResponse.builder()
                .workoutPlans(workoutPlans)
                .totalCount(workoutPlans.size())
                .isFiltered(true)
                .appliedFilters(List.of("equipment=" + equipment))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/muscle-group/{muscleGroup}")
    @Operation(summary = "Get workout plans by muscle group", description = "Retrieve workout plans targeting specific muscle groups")
    public ResponseEntity<WorkoutPlanListResponse> getWorkoutPlansByMuscleGroup(
            @Parameter(description = "Target muscle group") @PathVariable String muscleGroup) {

        List<WorkoutPlanResponse> workoutPlans = workoutPlanService.getWorkoutPlansByMuscleGroup(muscleGroup);

        WorkoutPlanListResponse response = WorkoutPlanListResponse.builder()
                .workoutPlans(workoutPlans)
                .totalCount(workoutPlans.size())
                .isFiltered(true)
                .appliedFilters(List.of("muscleGroup=" + muscleGroup))
                .build();

        return ResponseEntity.ok(response);
    }

    // =======================
    // SEARCH & ADVANCED FILTERING
    // =======================

    @GetMapping("/search")
    @Operation(summary = "Search workout plans", description = "Search workout plans by name with optional filters")
    public ResponseEntity<WorkoutPlanSearchResponse> searchWorkoutPlans(
            @Parameter(description = "Search term") @RequestParam(required = false) String q,
            @Parameter(description = "Category filter") @RequestParam(required = false) String category,
            @Parameter(description = "Workout type filter") @RequestParam(required = false) WorkoutType workoutType,
            @Parameter(description = "Difficulty filter") @RequestParam(required = false) DifficultyLevel difficulty,
            @Parameter(description = "Equipment filter") @RequestParam(required = false) String equipment,
            @Parameter(description = "Muscle group filter") @RequestParam(required = false) String muscleGroup,
            @PageableDefault(size = 20, sort = "timesUsed", direction = Sort.Direction.DESC) Pageable pageable) {

        long startTime = System.currentTimeMillis();

        Page<WorkoutPlanResponse> results;

        if (hasAdvancedFilters(category, workoutType, difficulty, equipment, muscleGroup)) {
            // Use advanced filtering
            results = workoutPlanService.searchWorkoutPlansWithFilters(
                    category, workoutType, difficulty, equipment, muscleGroup, pageable);
        } else if (q != null && !q.trim().isEmpty()) {
            // Simple text search
            results = workoutPlanService.searchWorkoutPlans(q, pageable);
        } else {
            // No search term or filters, return all public plans with pagination
            results = Page.empty(pageable);
        }

        long searchTime = System.currentTimeMillis() - startTime;

        WorkoutPlanSearchResponse response = WorkoutPlanSearchResponse.builder()
                .results(results.getContent())
                .searchTerm(q)
                .totalResults((int) results.getTotalElements())
                .page(results.getNumber())
                .size(results.getSize())
                .hasMore(results.hasNext())
                .searchTimeMs(searchTime)
                .searchType(determineSearchType(q, category, workoutType, difficulty, equipment, muscleGroup))
                .build();

        return ResponseEntity.ok(response);
    }

    // =======================
    // POPULAR & RECOMMENDATIONS
    // =======================

    @GetMapping("/popular")
    @Operation(summary = "Get most popular workout plans", description = "Retrieve the most popular workout plans")
    public ResponseEntity<WorkoutPlanListResponse> getMostPopularWorkoutPlans(
            @Parameter(description = "Number of plans to return") @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {

        List<WorkoutPlanResponse> workoutPlans = workoutPlanService.getMostPopularWorkoutPlans(limit);

        WorkoutPlanListResponse response = WorkoutPlanListResponse.builder()
                .workoutPlans(workoutPlans)
                .totalCount(workoutPlans.size())
                .sortBy("popularity")
                .sortDirection("DESC")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/highly-rated")
    @Operation(summary = "Get highly rated workout plans", description = "Retrieve workout plans with high ratings")
    public ResponseEntity<WorkoutPlanListResponse> getHighlyRatedWorkoutPlans(
            @Parameter(description = "Minimum rating") @RequestParam(defaultValue = "4.0") @Min(1) @Max(5) Double minRating) {

        List<WorkoutPlanResponse> workoutPlans = workoutPlanService.getHighlyRatedWorkoutPlans(minRating);

        WorkoutPlanListResponse response = WorkoutPlanListResponse.builder()
                .workoutPlans(workoutPlans)
                .totalCount(workoutPlans.size())
                .sortBy("rating")
                .sortDirection("DESC")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/trending")
    @Operation(summary = "Get trending workout plans", description = "Retrieve currently trending workout plans")
    public ResponseEntity<WorkoutPlanListResponse> getTrendingWorkoutPlans(
            @Parameter(description = "Number of plans to return") @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {

        List<WorkoutPlanResponse> workoutPlans = workoutPlanService.getTrendingWorkoutPlans(limit);

        WorkoutPlanListResponse response = WorkoutPlanListResponse.builder()
                .workoutPlans(workoutPlans)
                .totalCount(workoutPlans.size())
                .sortBy("trending")
                .sortDirection("DESC")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get workout plan statistics", description = "Retrieve statistics about workout plans")
    public ResponseEntity<Map<String, Object>> getWorkoutPlanStatistics() {
        Map<String, Object> statistics = workoutPlanService.getWorkoutPlanStatistics();
        return ResponseEntity.ok(statistics);
    }

    // =======================
    // USER-SPECIFIC ENDPOINTS (AUTHENTICATED)
    // =======================

    @GetMapping("/accessible")
    @Operation(summary = "Get accessible workout plans", description = "Get workout plans accessible based on user's subscription tier")
    public ResponseEntity<WorkoutPlanListResponse> getAccessibleWorkoutPlans(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<WorkoutPlanResponse> workoutPlans = workoutPlanService.getAccessibleWorkoutPlans(userDetails.getUsername());

        WorkoutPlanListResponse response = WorkoutPlanListResponse.builder()
                .workoutPlans(workoutPlans)
                .totalCount(workoutPlans.size())
                .isFiltered(true)
                .appliedFilters(List.of("subscription-accessible"))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @Operation(summary = "Get user's created workout plans", description = "Retrieve workout plans created by the authenticated user")
    public ResponseEntity<WorkoutPlanListResponse> getUserCreatedWorkoutPlans(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<WorkoutPlanResponse> workoutPlans = workoutPlanService.getUserCreatedWorkoutPlans(userDetails.getUsername());

        WorkoutPlanListResponse response = WorkoutPlanListResponse.builder()
                .workoutPlans(workoutPlans)
                .totalCount(workoutPlans.size())
                .totalUserPlans((long) workoutPlans.size())
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my/private")
    @Operation(summary = "Get user's private workout plans", description = "Retrieve private workout plans created by the authenticated user")
    public ResponseEntity<WorkoutPlanListResponse> getUserPrivateWorkoutPlans(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<WorkoutPlanResponse> workoutPlans = workoutPlanService.getUserPrivateWorkoutPlans(userDetails.getUsername());

        WorkoutPlanListResponse response = WorkoutPlanListResponse.builder()
                .workoutPlans(workoutPlans)
                .totalCount(workoutPlans.size())
                .isFiltered(true)
                .appliedFilters(List.of("private"))
                .build();

        return ResponseEntity.ok(response);
    }

    // =======================
    // WORKOUT PLAN CRUD
    // =======================

    @PostMapping
    @Operation(summary = "Create new workout plan", description = "Create a new workout plan")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Workout plan created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<WorkoutPlanResponse> createWorkoutPlan(
            @Valid @RequestBody WorkoutPlanRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        WorkoutPlanResponse response = workoutPlanService.createWorkoutPlan(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update workout plan", description = "Update an existing workout plan")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workout plan updated successfully"),
            @ApiResponse(responseCode = "404", description = "Workout plan not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to update this workout plan")
    })
    public ResponseEntity<WorkoutPlanResponse> updateWorkoutPlan(
            @Parameter(description = "Workout plan ID") @PathVariable Long id,
            @Valid @RequestBody WorkoutPlanRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        WorkoutPlanResponse response = workoutPlanService.updateWorkoutPlan(id, userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete workout plan", description = "Delete a workout plan")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Workout plan deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Workout plan not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to delete this workout plan"),
            @ApiResponse(responseCode = "409", description = "Cannot delete workout plan that has been used")
    })
    public ResponseEntity<Void> deleteWorkoutPlan(
            @Parameter(description = "Workout plan ID") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        workoutPlanService.deleteWorkoutPlan(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/duplicate")
    @Operation(summary = "Duplicate workout plan", description = "Create a copy of an existing workout plan")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Workout plan duplicated successfully"),
            @ApiResponse(responseCode = "404", description = "Original workout plan not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to duplicate this workout plan")
    })
    public ResponseEntity<WorkoutPlanResponse> duplicateWorkoutPlan(
            @Parameter(description = "Workout plan ID to duplicate") @PathVariable Long id,
            @Parameter(description = "New name for the duplicated plan") @RequestParam(required = false) String newName,
            @AuthenticationPrincipal UserDetails userDetails) {

        WorkoutPlanResponse response = workoutPlanService.duplicateWorkoutPlan(id, userDetails.getUsername(), newName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =======================
    // USAGE & ANALYTICS
    // =======================

    @PostMapping("/{id}/use")
    @Operation(summary = "Record workout plan usage", description = "Record that a workout plan has been used")
    @ApiResponse(responseCode = "200", description = "Usage recorded successfully")
    public ResponseEntity<Void> recordWorkoutPlanUsage(
            @Parameter(description = "Workout plan ID") @PathVariable Long id) {

        workoutPlanService.recordWorkoutPlanUsage(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/analytics")
    @Operation(summary = "Get workout plan analytics", description = "Get analytics data for a workout plan")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Workout plan not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to view analytics for this workout plan")
    })
    public ResponseEntity<WorkoutPlanAnalyticsResponse> getWorkoutPlanAnalytics(
            @Parameter(description = "Workout plan ID") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        WorkoutPlanAnalyticsResponse analytics = workoutPlanService.getWorkoutPlanAnalytics(id, userDetails.getUsername());
        return ResponseEntity.ok(analytics);
    }

    // =======================
    // VISIBILITY MANAGEMENT
    // =======================

    @PutMapping("/{id}/visibility/public")
    @Operation(summary = "Make workout plan public", description = "Make a workout plan publicly visible")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workout plan made public"),
            @ApiResponse(responseCode = "404", description = "Workout plan not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to modify this workout plan")
    })
    public ResponseEntity<Void> makeWorkoutPlanPublic(
            @Parameter(description = "Workout plan ID") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        workoutPlanService.makeWorkoutPlanPublic(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/visibility/private")
    @Operation(summary = "Make workout plan private", description = "Make a workout plan private")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workout plan made private"),
            @ApiResponse(responseCode = "404", description = "Workout plan not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to modify this workout plan")
    })
    public ResponseEntity<Void> makeWorkoutPlanPrivate(
            @Parameter(description = "Workout plan ID") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        workoutPlanService.makeWorkoutPlanPrivate(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    // =======================
    // PLAN EXERCISES
    // =======================

    @GetMapping("/{id}/exercises")
    @Operation(summary = "Get workout plan exercises", description = "Get exercises for a workout plan")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Exercises retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Workout plan not found")
    })
    public ResponseEntity<List<PlanExercise>> getWorkoutPlanExercises(
            @Parameter(description = "Workout plan ID") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<PlanExercise> exercises;

        if (userDetails != null) {
            // User is authenticated, get subscription-aware exercises
            exercises = workoutPlanService.getAccessibleWorkoutPlanExercises(id, userDetails.getUsername());
        } else {
            // Not authenticated, get all exercises for public plans
            exercises = workoutPlanService.getWorkoutPlanExercises(id);
        }

        return ResponseEntity.ok(exercises);
    }

    // =======================
    // EXCEPTION HANDLERS
    // =======================

    @ExceptionHandler(WorkoutPlanNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleWorkoutPlanNotFound(WorkoutPlanNotFoundException ex) {
        Map<String, Object> error = Map.of(
                "error", "Workout Plan Not Found",
                "message", ex.getMessage(),
                "timestamp", System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UnauthorizedOperationException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedOperation(UnauthorizedOperationException ex) {
        Map<String, Object> error = Map.of(
                "error", "Unauthorized Operation",
                "message", ex.getMessage(),
                "timestamp", System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        Map<String, Object> error = Map.of(
                "error", "Operation Not Allowed",
                "message", ex.getMessage(),
                "timestamp", System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // =======================
    // HELPER METHODS
    // =======================

    private boolean hasAdvancedFilters(String category, WorkoutType workoutType, DifficultyLevel difficulty,
                                       String equipment, String muscleGroup) {
        return category != null || workoutType != null || difficulty != null ||
                equipment != null || muscleGroup != null;
    }

    private String determineSearchType(String q, String category, WorkoutType workoutType,
                                       DifficultyLevel difficulty, String equipment, String muscleGroup) {
        if (hasAdvancedFilters(category, workoutType, difficulty, equipment, muscleGroup)) {
            return "FILTERED";
        } else if (q != null && !q.trim().isEmpty()) {
            return "FULL_TEXT";
        } else {
            return "BROWSE";
        }
    }
}