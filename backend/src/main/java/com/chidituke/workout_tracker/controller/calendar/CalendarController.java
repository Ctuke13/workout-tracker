package com.chidituke.workout_tracker.controller.calendar;

import com.chidituke.workout_tracker.dto.request.scheduled_workouts.ScheduledWorkoutRequest;
import com.chidituke.workout_tracker.dto.response.scheduled_workouts.ScheduledWorkoutResponse;
import com.chidituke.workout_tracker.service.calendar.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Calendar", description = "Calendar and exercise scheduling operations")
@CrossOrigin(origins = "*") // Configure appropriately for production
public class CalendarController {

    private final CalendarService calendarService;

    // =============================================================================
    // EXERCISE SCHEDULING ENDPOINTS
    // =============================================================================

    /**
     * Get scheduled exercises for a date range
     * GET /api/calendar/exercises?startDate=2024-01-01&endDate=2024-01-07
     */
    @GetMapping("/exercises")
    @Operation(summary = "Get scheduled exercises for date range")
    public ResponseEntity<List<ScheduledWorkoutResponse>> getScheduledExercises(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            Authentication authentication) {

        log.info("📅 Getting scheduled exercises for user {} from {} to {}",
                authentication.getName(), startDate, endDate);

        List<ScheduledWorkoutResponse> exercises = calendarService.getScheduledExercisesForDateRange(
                authentication.getName(), startDate, endDate);

        log.info("✅ Found {} scheduled exercises", exercises.size());
        return ResponseEntity.ok(exercises);
    }

    /**
     * Schedule a new exercise (using existing individual exercise endpoint)
     * POST /api/calendar/exercises
     */
    @PostMapping("/exercises")
    @Operation(summary = "Schedule a new individual exercise")
    public ResponseEntity<ScheduledWorkoutResponse> scheduleExercise(
            @Valid @RequestBody IndividualExerciseRequest request,
            Authentication authentication) {

        log.info("📅 Scheduling exercise {} for user {} on {}",
                request.getExerciseId(), authentication.getName(), request.getScheduledDate());

        ScheduledWorkoutResponse response = calendarService.scheduleIndividualExercise(
                authentication.getName(), request);

        log.info("✅ Successfully scheduled exercise");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update a scheduled exercise
     * PUT /api/calendar/exercises/{exerciseId}
     */
    @PutMapping("/exercises/{exerciseId}")
    @Operation(summary = "Update a scheduled exercise configuration")
    public ResponseEntity<ScheduledWorkoutResponse> updateScheduledExercise(
            @Parameter(description = "Scheduled exercise ID")
            @PathVariable String exerciseId,

            @Valid @RequestBody IndividualExerciseRequest updates,
            Authentication authentication) {

        log.info("📅 Updating scheduled exercise {} for user {}", exerciseId, authentication.getName());

        ScheduledWorkoutResponse response = calendarService.updateScheduledExercise(
                authentication.getName(), exerciseId, updates);

        log.info("✅ Successfully updated exercise");
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a scheduled exercise
     * DELETE /api/calendar/exercises/{exerciseId}
     */
    @DeleteMapping("/exercises/{exerciseId}")
    @Operation(summary = "Delete a scheduled exercise")
    public ResponseEntity<Void> deleteScheduledExercise(
            @Parameter(description = "Scheduled exercise ID")
            @PathVariable String exerciseId,

            Authentication authentication) {

        log.info("📅 Deleting scheduled exercise {} for user {}", exerciseId, authentication.getName());

        calendarService.deleteScheduledExercise(authentication.getName(), exerciseId);

        log.info("✅ Successfully deleted scheduled exercise");
        return ResponseEntity.noContent().build();
    }

    /**
     * Get exercises for a specific date
     * GET /api/calendar/exercises/date/{dateString}
     */
    @GetMapping("/exercises/date/{dateString}")
    @Operation(summary = "Get exercises for a specific date")
    public ResponseEntity<List<ScheduledWorkoutResponse>> getExercisesForDate(
            @Parameter(description = "Date string (YYYY-MM-DD)")
            @PathVariable String dateString,

            Authentication authentication) {

        log.info("📅 Getting exercises for user {} on date {}", authentication.getName(), dateString);

        LocalDate date = LocalDate.parse(dateString);
        List<ScheduledWorkoutResponse> exercises = calendarService.getExercisesForDate(
                authentication.getName(), date);

        log.info("✅ Found {} exercises for {}", exercises.size(), dateString);
        return ResponseEntity.ok(exercises);
    }

    // =============================================================================
    // EXERCISE COMPLETION ENDPOINTS
    // =============================================================================

    /**
     * Mark exercise as completed
     * PUT /api/calendar/exercises/{exerciseId}/complete
     */
    @PutMapping("/exercises/{exerciseId}/complete")
    @Operation(summary = "Mark exercise as completed")
    public ResponseEntity<ScheduledWorkoutResponse> markExerciseCompleted(
            @Parameter(description = "Scheduled exercise ID")
            @PathVariable String exerciseId,

            Authentication authentication) {

        log.info("✅ Marking exercise {} as completed for user {}", exerciseId, authentication.getName());

        ScheduledWorkoutResponse response = calendarService.markExerciseCompleted(
                authentication.getName(), exerciseId);

        log.info("✅ Successfully marked exercise as completed");
        return ResponseEntity.ok(response);
    }

    /**
     * Mark multiple exercises as completed (batch operation)
     * PUT /api/calendar/exercises/complete-batch
     */
    @PutMapping("/exercises/complete-batch")
    @Operation(summary = "Mark multiple exercises as completed")
    public ResponseEntity<List<ScheduledWorkoutResponse>> markMultipleExercisesCompleted(
            @Valid @RequestBody BatchCompleteRequest request,
            Authentication authentication) {

        log.info("✅ Marking {} exercises as completed for user {}",
                request.getExerciseIds().size(), authentication.getName());

        List<ScheduledWorkoutResponse> responses = calendarService.markMultipleExercisesCompleted(
                authentication.getName(), request.getExerciseIds());

        log.info("✅ Successfully marked {} exercises as completed", responses.size());
        return ResponseEntity.ok(responses);
    }

    // =============================================================================
    // WORKOUT STATISTICS ENDPOINTS
    // =============================================================================

    /**
     * Get workout statistics
     * GET /api/calendar/stats?date=2024-01-15 (optional)
     */
    @GetMapping("/stats")
    @Operation(summary = "Get workout statistics")
    public ResponseEntity<WorkoutStatsResponse> getWorkoutStats(
            @Parameter(description = "Optional specific date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

            Authentication authentication) {

        log.info("📊 Getting workout statistics for user {} (date: {})",
                authentication.getName(), date != null ? date : "all time");

        WorkoutStatsResponse stats = calendarService.getWorkoutStats(authentication.getName(), date);

        return ResponseEntity.ok(stats);
    }

    // =============================================================================
    // WORKOUT PLAN ENDPOINTS (Future Implementation)
    // =============================================================================

    /**
     * Schedule a complete workout plan
     * POST /api/calendar/workout-plans
     */
    @PostMapping("/workout-plans")
    @Operation(summary = "Schedule a complete workout plan")
    public ResponseEntity<ScheduledWorkoutResponse> scheduleWorkoutPlan(
            @Valid @RequestBody ScheduledWorkoutRequest request,
            Authentication authentication) {

        log.info("📋 Scheduling workout plan {} for user {} on {}",
                request.getWorkoutPlanId(), authentication.getName(), request.getScheduledDate());

        ScheduledWorkoutResponse response = calendarService.scheduleWorkoutPlan(
                authentication.getName(), request);

        log.info("✅ Successfully scheduled workout plan");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =============================================================================
    // NEW DTOs NEEDED (Only these 3!)
    // =============================================================================

    /**
     * Request DTO for individual exercise scheduling (simplified version of your existing DTOs)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IndividualExerciseRequest {
        @NotNull(message = "Exercise ID is required")
        private Long exerciseId;

        @NotNull(message = "Scheduled date is required")
        private LocalDate scheduledDate;

        // Strength fields
        private Integer sets;
        private String reps;
        private Double weight;
        private Integer restSeconds;
        private String tempo;
        private Integer targetRpe;

        // Cardio fields
        private Integer targetDurationMinutes;
        private Double targetDistanceKm;
        private Double targetPace;

        // Isometric fields
        private Integer holdDurationSeconds;

        // Common fields
        private String notes;
    }

    /**
     * Request DTO for batch completion operations
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchCompleteRequest {
        @NotEmpty(message = "Exercise IDs list cannot be empty")
        private List<String> exerciseIds;
    }

    /**
     * Response DTO for workout statistics
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkoutStatsResponse {
        // Today's stats
        private Integer exercisesScheduledToday;
        private Integer exercisesCompletedToday;
        private Integer minutesWorkedOutToday;

        // This week's stats
        private Integer exercisesScheduledThisWeek;
        private Integer exercisesCompletedThisWeek;
        private Integer minutesWorkedOutThisWeek;

        // This month's stats
        private Integer exercisesScheduledThisMonth;
        private Integer exercisesCompletedThisMonth;
        private Integer minutesWorkedOutThisMonth;

        // Streak information
        private Integer currentStreak;
        private Integer longestStreak;

        // Completion rates
        private Double completionRateThisWeek;
        private Double completionRateThisMonth;

        // Last workout information
        private LocalDate lastWorkoutDate;
        private String lastWorkoutType;

        // Achievements/milestones
        private Integer totalWorkoutsCompleted;
        private Integer totalMinutesWorkedOut;
        private String favoriteExerciseType;
    }
}