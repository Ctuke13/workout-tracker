package com.chidituke.workout_tracker.controller.workout;

import com.chidituke.workout_tracker.dto.request.performance.PerformanceRequest;
import com.chidituke.workout_tracker.dto.response.performance.PerformanceResponse;
import com.chidituke.workout_tracker.service.workout.PerformanceService;
import com.chidituke.workout_tracker.service.user.SubscriptionService;
import com.chidituke.workout_tracker.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Enhanced Performance Controller with comprehensive endpoints
 * Includes analytics, progress tracking, and professional coaching features
 * UPDATED: Now congruent with PerformanceService methods
 */
@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Performance Records", description = "Performance tracking and analytics endpoints")
public class PerformanceController {

    private final PerformanceService performanceService;
    private final SubscriptionService subscriptionService;
    private final UserService userService;

    // ==============================================
    // BASIC PERFORMANCE RECORD OPERATIONS
    // ==============================================

    /**
     * Get performance records for a specific workout session (UPDATED: consistent naming)
     */
    @GetMapping("/workout-session/{workoutSessionId}")
    @Operation(summary = "Get performance records for a workout session",
            description = "Retrieve all performance records for a specific workout session")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Performance records retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - workout session does not belong to user"),
            @ApiResponse(responseCode = "404", description = "Workout session not found")
    })
    public ResponseEntity<List<PerformanceResponse>> getPerformanceByWorkoutSession(
            @Parameter(description = "Workout session ID") @PathVariable Long workoutSessionId,
            Authentication authentication) {

        String username = authentication.getName();
        List<PerformanceResponse> performances = performanceService.getPerformanceByWorkoutLog(workoutSessionId, username);
        return ResponseEntity.ok(performances);
    }

    /**
     * Get performance records for a specific workout session (LEGACY ENDPOINT)
     */
    @GetMapping("/workout-log/{workoutLogId}")
    @Operation(summary = "Get performance records for a workout log (legacy)",
            description = "Retrieve all performance records for a specific workout log")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Performance records retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - workout log does not belong to user"),
            @ApiResponse(responseCode = "404", description = "Workout log not found")
    })
    public ResponseEntity<List<PerformanceResponse>> getPerformanceByWorkoutLog(
            @Parameter(description = "Workout log ID") @PathVariable Long workoutLogId,
            Authentication authentication) {

        String username = authentication.getName();
        List<PerformanceResponse> performances = performanceService.getPerformanceByWorkoutLog(workoutLogId, username);
        return ResponseEntity.ok(performances);
    }

    /**
     * Get user performance history for a specific workout plan (UPDATED: consistent naming)
     */
    @GetMapping("/workout-plan/{workoutPlanId}")
    @Operation(summary = "Get performance history for a workout plan",
            description = "Get paginated performance history for a specific workout plan")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Performance history retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Workout plan not found")
    })
    public ResponseEntity<Page<PerformanceResponse>> getUserPerformanceByWorkoutPlan(
            @Parameter(description = "Workout plan ID") @PathVariable Long workoutPlanId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDir,
            Authentication authentication) {

        String username = authentication.getName();

        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<PerformanceResponse> performances = performanceService.getUserPerformanceByWorkout(
                username, workoutPlanId, pageable);

        return ResponseEntity.ok(performances);
    }

    /**
     * Get user performance history for a specific workout (LEGACY ENDPOINT)
     */
    @GetMapping("/workout/{workoutId}")
    @Operation(summary = "Get performance history for a workout (legacy)",
            description = "Get paginated performance history for a specific workout")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Performance history retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Workout not found")
    })
    public ResponseEntity<Page<PerformanceResponse>> getUserPerformanceByWorkout(
            @Parameter(description = "Workout ID") @PathVariable Long workoutId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDir,
            Authentication authentication) {

        String username = authentication.getName();

        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<PerformanceResponse> performances = performanceService.getUserPerformanceByWorkout(
                username, workoutId, pageable);

        return ResponseEntity.ok(performances);
    }

    /**
     * Get user performance history for a specific exercise
     */
    @GetMapping("/exercise/{exerciseId}")
    @Operation(summary = "Get performance history for an exercise",
            description = "Get paginated performance history for a specific exercise")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Performance history retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Exercise not found")
    })
    public ResponseEntity<Page<PerformanceResponse>> getUserPerformanceByExercise(
            @Parameter(description = "Exercise ID") @PathVariable Long exerciseId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDir,
            Authentication authentication) {

        String username = authentication.getName();

        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<PerformanceResponse> performances = performanceService.getUserPerformanceByExercise(
                username, exerciseId, pageable);

        return ResponseEntity.ok(performances);
    }

    /**
     * Create a new performance record
     */
    @PostMapping
    @Operation(summary = "Create a performance record",
            description = "Create a new performance record for strength, cardio, or other exercise types")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Performance record created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid performance data"),
            @ApiResponse(responseCode = "403", description = "Access denied - workout session does not belong to user"),
            @ApiResponse(responseCode = "404", description = "Workout session or exercise not found")
    })
    public ResponseEntity<PerformanceResponse> createPerformance(
            @Parameter(description = "Performance data") @RequestBody @Valid PerformanceRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        PerformanceResponse performance = performanceService.createPerformance(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(performance);
    }

    /**
     * Update an existing performance record
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a performance record",
            description = "Update an existing performance record")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Performance record updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid performance data"),
            @ApiResponse(responseCode = "403", description = "Access denied - performance record does not belong to user"),
            @ApiResponse(responseCode = "404", description = "Performance record not found")
    })
    public ResponseEntity<PerformanceResponse> updatePerformance(
            @Parameter(description = "Performance record ID") @PathVariable Long id,
            @Parameter(description = "Updated performance data") @RequestBody @Valid PerformanceRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        PerformanceResponse updatedPerformance = performanceService.updatePerformance(id, username, request);
        return ResponseEntity.ok(updatedPerformance);
    }

    /**
     * Delete a performance record
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a performance record",
            description = "Delete an existing performance record")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Performance record deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - performance record does not belong to user"),
            @ApiResponse(responseCode = "404", description = "Performance record not found")
    })
    public ResponseEntity<Void> deletePerformance(
            @Parameter(description = "Performance record ID") @PathVariable Long id,
            Authentication authentication) {

        String username = authentication.getName();
        performanceService.deletePerformance(id, username);
        return ResponseEntity.noContent().build();
    }

    // ==============================================
    // PERSONAL RECORDS AND PROGRESS TRACKING
    // ==============================================

    /**
     * Get maximum weight lifted for a specific exercise
     */
    @GetMapping("/analytics/max-weight/{exerciseId}")
    @Operation(summary = "Get maximum weight for an exercise",
            description = "Get the maximum weight ever lifted for a specific exercise")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Maximum weight retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Upgrade to PLUS for analytics features"),
            @ApiResponse(responseCode = "404", description = "Exercise not found")
    })
    public ResponseEntity<?> getMaxWeightForExercise(
            @Parameter(description = "Exercise ID") @PathVariable Long exerciseId,
            Authentication authentication) {

        String username = authentication.getName();

        if (!subscriptionService.isPlusTier(getUserIdFromAuth(authentication))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("🔒 Upgrade to PLUS for advanced analytics features!");
        }

        Double maxWeight = performanceService.getMaxWeightForExercise(username, exerciseId);
        return ResponseEntity.ok(Map.of(
                "exerciseId", exerciseId,
                "maxWeight", maxWeight,
                "unit", "kg"
        ));
    }

    /**
     * Get maximum volume for a specific exercise
     */
    @GetMapping("/analytics/max-volume/{exerciseId}")
    @Operation(summary = "Get maximum volume for an exercise",
            description = "Get the maximum volume (weight × reps) for a specific exercise")
    public ResponseEntity<?> getMaxVolumeForExercise(
            @Parameter(description = "Exercise ID") @PathVariable Long exerciseId,
            Authentication authentication) {

        String username = authentication.getName();

        if (!subscriptionService.isPlusTier(getUserIdFromAuth(authentication))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("🔒 Upgrade to PLUS for advanced analytics features!");
        }

        Double maxVolume = performanceService.getMaxVolumeForExercise(username, exerciseId);
        return ResponseEntity.ok(Map.of(
                "exerciseId", exerciseId,
                "maxVolume", maxVolume,
                "unit", "kg"
        ));
    }

    /**
     * Get strength progression for an exercise
     */
    @GetMapping("/analytics/progression/{exerciseId}")
    @Operation(summary = "Get strength progression for an exercise",
            description = "Get strength progression data showing best performance from each workout")
    public ResponseEntity<?> getStrengthProgression(
            @Parameter(description = "Exercise ID") @PathVariable Long exerciseId,
            Authentication authentication) {

        String username = authentication.getName();

        if (!subscriptionService.isPlusTier(getUserIdFromAuth(authentication))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("🔒 Upgrade to PLUS for advanced analytics features!");
        }

        List<PerformanceResponse> progression = performanceService.getStrengthProgression(username, exerciseId);
        return ResponseEntity.ok(progression);
    }

    // ==============================================
    // VOLUME AND ANALYTICS
    // ==============================================

    /**
     * Get total volume for a date range
     */
    @GetMapping("/analytics/volume")
    @Operation(summary = "Get total volume for date range",
            description = "Get total training volume (weight × reps) for a specified date range")
    public ResponseEntity<?> getTotalVolumeForDateRange(
            @Parameter(description = "Start date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        String username = authentication.getName();

        if (!subscriptionService.isPlusTier(getUserIdFromAuth(authentication))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("🔒 Upgrade to PLUS for advanced analytics features!");
        }

        Double totalVolume = performanceService.getTotalVolumeForDateRange(username, startDate, endDate);
        return ResponseEntity.ok(Map.of(
                "startDate", startDate,
                "endDate", endDate,
                "totalVolume", totalVolume != null ? totalVolume : 0.0,
                "unit", "kg"
        ));
    }

    /**
     * Get volume progression over time
     */
    @GetMapping("/analytics/volume-progression/{exerciseId}")
    @Operation(summary = "Get volume progression over time",
            description = "Get weekly volume progression for a specific exercise")
    public ResponseEntity<?> getVolumeProgression(
            @Parameter(description = "Exercise ID") @PathVariable Long exerciseId,
            @Parameter(description = "Number of weeks to analyze")
            @RequestParam(defaultValue = "12") @Positive int weeks,
            Authentication authentication) {

        String username = authentication.getName();

        if (!subscriptionService.isPlusTier(getUserIdFromAuth(authentication))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("🔒 Upgrade to PLUS for advanced analytics features!");
        }

        Map<String, Object> progression = performanceService.getVolumeProgression(username, exerciseId, weeks);
        return ResponseEntity.ok(progression);
    }

    /**
     * Get comprehensive performance analytics
     */
    @GetMapping("/analytics/comprehensive")
    @Operation(summary = "Get comprehensive performance analytics",
            description = "Get detailed performance analytics including volume, sessions, RPE, and more")
    public ResponseEntity<?> getPerformanceAnalytics(
            @Parameter(description = "Start date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        String username = authentication.getName();

        if (!subscriptionService.isPlusTier(getUserIdFromAuth(authentication))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("🔒 Upgrade to PLUS for advanced analytics features!");
        }

        Map<String, Object> analytics = performanceService.getPerformanceAnalytics(username, startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get exercise frequency statistics
     */
    @GetMapping("/analytics/exercise-frequency")
    @Operation(summary = "Get exercise frequency statistics",
            description = "Get statistics on how frequently each exercise is performed")
    public ResponseEntity<?> getExerciseFrequency(
            @Parameter(description = "Number of weeks to analyze")
            @RequestParam(defaultValue = "12") @Positive int weeks,
            Authentication authentication) {

        String username = authentication.getName();

        if (!subscriptionService.isPlusTier(getUserIdFromAuth(authentication))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("🔒 Upgrade to PLUS for advanced analytics features!");
        }

        List<Map<String, Object>> frequency = performanceService.getExerciseFrequency(username, weeks);
        return ResponseEntity.ok(frequency);
    }

    // ==============================================
    // PROFESSIONAL COACHING FEATURES
    // ==============================================

    /**
     * Get performances assigned by a specific trainer
     */
    @GetMapping("/trainer/{trainerId}")
    @Operation(summary = "Get performances assigned by trainer",
            description = "Get performance records assigned by a specific professional trainer")
    public ResponseEntity<Page<PerformanceResponse>> getPerformancesByTrainer(
            @Parameter(description = "Trainer ID") @PathVariable Long trainerId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        String username = authentication.getName();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PerformanceResponse> performances = performanceService.getPerformancesByTrainer(
                username, trainerId, pageable);

        return ResponseEntity.ok(performances);
    }

    /**
     * Get achievement statistics
     */
    @GetMapping("/analytics/achievements")
    @Operation(summary = "Get achievement statistics",
            description = "Get statistics on target achievements (exceeded, met, below target)")
    public ResponseEntity<?> getAchievementStatistics(Authentication authentication) {
        String username = authentication.getName();

        if (!subscriptionService.isPlusTier(getUserIdFromAuth(authentication))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("🔒 Upgrade to PLUS for advanced analytics features!");
        }

        Map<String, Long> achievements = performanceService.getAchievementStatistics(username);
        return ResponseEntity.ok(achievements);
    }

    /**
     * Get performances that exceeded targets
     */
    @GetMapping("/achievements/exceeded")
    @Operation(summary = "Get performances that exceeded targets",
            description = "Get paginated list of performances that exceeded their targets")
    public ResponseEntity<Page<PerformanceResponse>> getExceededTargets(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        String username = authentication.getName();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PerformanceResponse> performances = performanceService.getExceededTargets(username, pageable);

        return ResponseEntity.ok(performances);
    }

    // ==============================================
    // BULK OPERATIONS (UPDATED: consistent naming)
    // ==============================================

    /**
     * Bulk update rest times for a workout session
     */
    @PutMapping("/bulk/rest-times/{workoutSessionId}")
    @Operation(summary = "Bulk update rest times",
            description = "Update rest times for all performance records in a workout session")
    public ResponseEntity<Map<String, Object>> bulkUpdateRestTimesForWorkoutSession(
            @Parameter(description = "Workout session ID") @PathVariable Long workoutSessionId,
            @Parameter(description = "Rest time in seconds") @RequestParam Integer restSeconds,
            Authentication authentication) {

        String username = authentication.getName();
        int updatedCount = performanceService.bulkUpdateRestTimes(workoutSessionId, username, restSeconds);

        return ResponseEntity.ok(Map.of(
                "updatedCount", updatedCount,
                "restSeconds", restSeconds,
                "workoutSessionId", workoutSessionId
        ));
    }

    /**
     * Bulk update rest times for a workout log (LEGACY ENDPOINT)
     */
    @PutMapping("/bulk/rest-times/{workoutLogId}")
    @Operation(summary = "Bulk update rest times (legacy)",
            description = "Update rest times for all performance records in a workout log")
    public ResponseEntity<Map<String, Object>> bulkUpdateRestTimes(
            @Parameter(description = "Workout log ID") @PathVariable Long workoutLogId,
            @Parameter(description = "Rest time in seconds") @RequestParam Integer restSeconds,
            Authentication authentication) {

        String username = authentication.getName();
        int updatedCount = performanceService.bulkUpdateRestTimes(workoutLogId, username, restSeconds);

        return ResponseEntity.ok(Map.of(
                "updatedCount", updatedCount,
                "restSeconds", restSeconds,
                "workoutLogId", workoutLogId
        ));
    }

    /**
     * Bulk update perceived exertion for a workout session
     */
    @PutMapping("/bulk/rpe/{workoutSessionId}")
    @Operation(summary = "Bulk update perceived exertion",
            description = "Update RPE (Rate of Perceived Exertion) for all performance records in a workout session")
    public ResponseEntity<Map<String, Object>> bulkUpdateRPEForWorkoutSession(
            @Parameter(description = "Workout session ID") @PathVariable Long workoutSessionId,
            @Parameter(description = "RPE value (1-10)") @RequestParam Integer rpe,
            Authentication authentication) {

        String username = authentication.getName();
        int updatedCount = performanceService.bulkUpdateRPE(workoutSessionId, username, rpe);

        return ResponseEntity.ok(Map.of(
                "updatedCount", updatedCount,
                "rpe", rpe,
                "workoutSessionId", workoutSessionId
        ));
    }

    /**
     * Bulk update perceived exertion for a workout log (LEGACY ENDPOINT)
     */
    @PutMapping("/bulk/rpe/{workoutLogId}")
    @Operation(summary = "Bulk update perceived exertion (legacy)",
            description = "Update RPE (Rate of Perceived Exertion) for all performance records in a workout log")
    public ResponseEntity<Map<String, Object>> bulkUpdateRPE(
            @Parameter(description = "Workout log ID") @PathVariable Long workoutLogId,
            @Parameter(description = "RPE value (1-10)") @RequestParam Integer rpe,
            Authentication authentication) {

        String username = authentication.getName();
        int updatedCount = performanceService.bulkUpdateRPE(workoutLogId, username, rpe);

        return ResponseEntity.ok(Map.of(
                "updatedCount", updatedCount,
                "rpe", rpe,
                "workoutLogId", workoutLogId
        ));
    }

    // ==============================================
    // DASHBOARD AND RECENT ACTIVITY
    // ==============================================

    /**
     * Get recent performance activity for dashboard
     */
    @GetMapping("/recent")
    @Operation(summary = "Get recent performance activity",
            description = "Get recent performance records for dashboard display")
    public ResponseEntity<List<PerformanceResponse>> getRecentActivity(
            @Parameter(description = "Number of days to look back")
            @RequestParam(defaultValue = "7") @Positive int days,
            Authentication authentication) {

        String username = authentication.getName();
        List<PerformanceResponse> recentActivity = performanceService.getRecentActivity(username, days);
        return ResponseEntity.ok(recentActivity);
    }

    // ==============================================
    // UTILITY ENDPOINTS
    // ==============================================

    /**
     * Get performance record by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get performance record by ID",
            description = "Get a specific performance record by its ID")
    public ResponseEntity<PerformanceResponse> getPerformanceById(
            @Parameter(description = "Performance record ID") @PathVariable Long id,
            Authentication authentication) {

        // This would need to be implemented in the service
        // For now, return not implemented
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the performance service is healthy")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "service", "PerformanceService",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }

    // ==============================================
    // HELPER METHODS
    // ==============================================

    private Long getUserIdFromAuth(Authentication authentication) {
        String username = authentication.getName();
        return userService.getUserIdByUsername(username);
    }
}