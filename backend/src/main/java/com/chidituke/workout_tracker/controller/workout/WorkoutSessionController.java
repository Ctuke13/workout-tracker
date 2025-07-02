package com.chidituke.workout_tracker.controller.workout;

import com.chidituke.workout_tracker.dto.request.workout_session.WorkoutSessionRequest;
import com.chidituke.workout_tracker.dto.response.workout_session.WorkoutSessionAnalyticsResponse;
import com.chidituke.workout_tracker.dto.response.workout_session.WorkoutSessionResponse;
import com.chidituke.workout_tracker.exceptions.ErrorResponse;
import com.chidituke.workout_tracker.exceptions.workout_session.WorkoutSessionNotFoundException;
import com.chidituke.workout_tracker.service.workout.WorkoutSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/workout-sessions")
@RequiredArgsConstructor
@Tag(name = "Workout Sessions", description = "API for managing workout sessions and tracking")
public class WorkoutSessionController {

    private final WorkoutSessionService workoutSessionService;

    // =======================
    // WORKOUT SESSION CRUD
    // =======================

    @PostMapping
    @Operation(summary = "Create workout session", description = "Create a new workout session")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Workout session created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Workout plan not found")
    })
    public ResponseEntity<WorkoutSessionResponse> createWorkoutSession(
            @Valid @RequestBody WorkoutSessionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        WorkoutSessionResponse response = workoutSessionService.createWorkoutSession(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{sessionId}")
    @Operation(summary = "Update workout session", description = "Update an existing workout session")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workout session updated successfully"),
            @ApiResponse(responseCode = "404", description = "Workout session not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to update this session")
    })
    public ResponseEntity<WorkoutSessionResponse> updateWorkoutSession(
            @Parameter(description = "Workout session ID") @PathVariable Long sessionId,
            @Valid @RequestBody WorkoutSessionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        WorkoutSessionResponse response = workoutSessionService.updateWorkoutSession(sessionId, userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Delete workout session", description = "Delete a workout session")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Workout session deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Workout session not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to delete this session")
    })
    public ResponseEntity<Void> deleteWorkoutSession(
            @Parameter(description = "Workout session ID") @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        workoutSessionService.deleteWorkoutSession(sessionId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Get workout session by ID", description = "Retrieve a specific workout session")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workout session found"),
            @ApiResponse(responseCode = "404", description = "Workout session not found")
    })
    public ResponseEntity<WorkoutSessionResponse> getWorkoutSessionById(
            @Parameter(description = "Workout session ID") @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Optional<WorkoutSessionResponse> session = workoutSessionService.getWorkoutSessionById(sessionId, userDetails.getUsername());
        return session.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // =======================
    // WORKOUT SESSION RETRIEVAL
    // =======================

    @GetMapping
    @Operation(summary = "Get user's workout history", description = "Retrieve user's complete workout history")
    @ApiResponse(responseCode = "200", description = "Workout history retrieved successfully")
    public ResponseEntity<List<WorkoutSessionResponse>> getUserWorkoutHistory(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<WorkoutSessionResponse> sessions = workoutSessionService.getUserWorkoutHistory(userDetails.getUsername());
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent workout sessions", description = "Get user's most recent workout sessions")
    @ApiResponse(responseCode = "200", description = "Recent sessions retrieved successfully")
    public ResponseEntity<List<WorkoutSessionResponse>> getRecentWorkoutSessions(
            @Parameter(description = "Number of sessions to return") @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<WorkoutSessionResponse> sessions = workoutSessionService.getRecentWorkoutSessions(userDetails.getUsername(), limit);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Get workout sessions by date", description = "Retrieve workout sessions for a specific date")
    @ApiResponse(responseCode = "200", description = "Sessions for date retrieved successfully")
    public ResponseEntity<List<WorkoutSessionResponse>> getWorkoutSessionsByDate(
            @Parameter(description = "Date in YYYY-MM-DD format")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<WorkoutSessionResponse> sessions = workoutSessionService.getWorkoutSessionsByDate(userDetails.getUsername(), date);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get workout sessions by date range", description = "Retrieve workout sessions within a date range")
    @ApiResponse(responseCode = "200", description = "Sessions for date range retrieved successfully")
    public ResponseEntity<List<WorkoutSessionResponse>> getWorkoutSessionsByDateRange(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<WorkoutSessionResponse> sessions = workoutSessionService.getWorkoutSessionsByDateRange(
                userDetails.getUsername(), startDate, endDate);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/today")
    @Operation(summary = "Check if user worked out today", description = "Check if the user has any workout sessions for today")
    @ApiResponse(responseCode = "200", description = "Status retrieved successfully")
    public ResponseEntity<Map<String, Object>> hasWorkedOutToday(
            @AuthenticationPrincipal UserDetails userDetails) {

        boolean workedOutToday = workoutSessionService.hasWorkedOutToday(userDetails.getUsername());
        List<WorkoutSessionResponse> todaySessions = workoutSessionService.getWorkoutSessionsByDate(userDetails.getUsername(), LocalDate.now());

        Map<String, Object> response = Map.of(
                "hasWorkedOutToday", workedOutToday,
                "date", LocalDate.now(),
                "sessionsToday", todaySessions.size(),
                "sessions", todaySessions
        );

        return ResponseEntity.ok(response);
    }

    // =======================
    // WORKOUT PLAN SPECIFIC QUERIES
    // =======================

    @GetMapping("/workout-plan/{workoutPlanId}")
    @Operation(summary = "Get sessions for workout plan", description = "Retrieve user's sessions for a specific workout plan")
    @ApiResponse(responseCode = "200", description = "Workout plan sessions retrieved successfully")
    public ResponseEntity<List<WorkoutSessionResponse>> getSessionsForWorkoutPlan(
            @Parameter(description = "Workout plan ID") @PathVariable Long workoutPlanId,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<WorkoutSessionResponse> sessions = workoutSessionService.getSessionsForWorkoutPlan(
                userDetails.getUsername(), workoutPlanId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/workout-plan/{workoutPlanId}/count")
    @Operation(summary = "Get workout plan completion count", description = "Get number of times user completed a workout plan")
    @ApiResponse(responseCode = "200", description = "Completion count retrieved successfully")
    public ResponseEntity<Map<String, Object>> getWorkoutPlanCompletionCount(
            @Parameter(description = "Workout plan ID") @PathVariable Long workoutPlanId,
            @AuthenticationPrincipal UserDetails userDetails) {

        long count = workoutSessionService.getWorkoutPlanCompletionCount(userDetails.getUsername(), workoutPlanId);

        Map<String, Object> response = Map.of(
                "workoutPlanId", workoutPlanId,
                "completionCount", count
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/workout-plan/{workoutPlanId}/last-completion")
    @Operation(summary = "Get last completion of workout plan", description = "Get user's most recent completion of a workout plan")
    @ApiResponse(responseCode = "200", description = "Last completion retrieved successfully")
    public ResponseEntity<WorkoutSessionResponse> getLastCompletionOfWorkoutPlan(
            @Parameter(description = "Workout plan ID") @PathVariable Long workoutPlanId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Optional<WorkoutSessionResponse> lastCompletion = workoutSessionService.getLastCompletionOfWorkoutPlan(
                userDetails.getUsername(), workoutPlanId);

        return lastCompletion.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // =======================
    // PROGRAM TRACKING
    // =======================

    @GetMapping("/program/{programId}")
    @Operation(summary = "Get program sessions", description = "Retrieve user's sessions for a specific program")
    @ApiResponse(responseCode = "200", description = "Program sessions retrieved successfully")
    public ResponseEntity<List<WorkoutSessionResponse>> getProgramSessions(
            @Parameter(description = "Program ID") @PathVariable Long programId,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<WorkoutSessionResponse> sessions = workoutSessionService.getProgramSessions(
                userDetails.getUsername(), programId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/program/{programId}/week/{weekNumber}")
    @Operation(summary = "Get program week sessions", description = "Retrieve sessions for a specific week of a program")
    @ApiResponse(responseCode = "200", description = "Program week sessions retrieved successfully")
    public ResponseEntity<List<WorkoutSessionResponse>> getProgramWeekSessions(
            @Parameter(description = "Program ID") @PathVariable Long programId,
            @Parameter(description = "Week number") @PathVariable Integer weekNumber,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<WorkoutSessionResponse> sessions = workoutSessionService.getProgramWeekSessions(
                userDetails.getUsername(), programId, weekNumber);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/program/{programId}/progress")
    @Operation(summary = "Get program progress", description = "Get user's progress through a specific program")
    @ApiResponse(responseCode = "200", description = "Program progress retrieved successfully")
    public ResponseEntity<Map<String, Object>> getProgramProgress(
            @Parameter(description = "Program ID") @PathVariable Long programId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> progress = workoutSessionService.getProgramProgress(userDetails.getUsername(), programId);
        return ResponseEntity.ok(progress);
    }

    // =======================
    // ANALYTICS & STATISTICS
    // =======================

    @GetMapping("/analytics")
    @Operation(summary = "Get workout analytics", description = "Get comprehensive workout analytics for a date range")
    @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully")
    public ResponseEntity<WorkoutSessionAnalyticsResponse> getWorkoutAnalytics(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetails userDetails) {

        WorkoutSessionAnalyticsResponse analytics = workoutSessionService.getWorkoutAnalytics(
                userDetails.getUsername(), startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/stats/total-count")
    @Operation(summary = "Get total workout count", description = "Get user's total number of workout sessions")
    @ApiResponse(responseCode = "200", description = "Total count retrieved successfully")
    public ResponseEntity<Map<String, Object>> getTotalWorkoutCount(
            @AuthenticationPrincipal UserDetails userDetails) {

        long totalCount = workoutSessionService.getTotalWorkoutCount(userDetails.getUsername());

        Map<String, Object> response = Map.of(
                "totalWorkoutCount", totalCount
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/count-range")
    @Operation(summary = "Get workout count for date range", description = "Get number of workouts in a specific date range")
    @ApiResponse(responseCode = "200", description = "Count for range retrieved successfully")
    public ResponseEntity<Map<String, Object>> getWorkoutCountForDateRange(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long count = workoutSessionService.getWorkoutCountForDateRange(userDetails.getUsername(), startDate, endDate);

        Map<String, Object> response = Map.of(
                "startDate", startDate,
                "endDate", endDate,
                "workoutCount", count
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/streak")
    @Operation(summary = "Get workout streak", description = "Get user's current workout streak information")
    @ApiResponse(responseCode = "200", description = "Workout streak retrieved successfully")
    public ResponseEntity<List<WorkoutSessionResponse>> getWorkoutStreak(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<WorkoutSessionResponse> streakSessions = workoutSessionService.getWorkoutStreak(userDetails.getUsername());
        return ResponseEntity.ok(streakSessions);
    }

    // =======================
    // DATA RETENTION (FREE USERS)
    // =======================

    @PostMapping("/cleanup")
    @Operation(summary = "Cleanup old workout sessions", description = "Remove old workout sessions for free users (30-day retention)")
    @ApiResponse(responseCode = "200", description = "Cleanup completed successfully")
    public ResponseEntity<Map<String, Object>> cleanupOldWorkoutSessions(
            @AuthenticationPrincipal UserDetails userDetails) {

        workoutSessionService.cleanupOldWorkoutSessions(userDetails.getUsername());

        Map<String, Object> response = Map.of(
                "message", "Cleanup completed successfully",
                "timestamp", java.time.LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/retention")
    @Operation(summary = "Get sessions within retention period", description = "Get workout sessions within the user's data retention period")
    @ApiResponse(responseCode = "200", description = "Sessions within retention period retrieved successfully")
    public ResponseEntity<List<WorkoutSessionResponse>> getSessionsWithinRetentionPeriod(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<WorkoutSessionResponse> sessions = workoutSessionService.getSessionsWithinRetentionPeriod(userDetails.getUsername());
        return ResponseEntity.ok(sessions);
    }

    // =======================
    // SOCIAL FEATURES
    // =======================

    @PostMapping("/{sessionId}/share")
    @Operation(summary = "Share workout session", description = "Make a workout session publicly shareable")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session shared successfully"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to share this session")
    })
    public ResponseEntity<WorkoutSessionResponse> shareWorkoutSession(
            @Parameter(description = "Workout session ID") @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        WorkoutSessionResponse response = workoutSessionService.shareWorkoutSession(sessionId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sessionId}/unshare")
    @Operation(summary = "Unshare workout session", description = "Make a workout session private again")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session unshared successfully"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to unshare this session")
    })
    public ResponseEntity<WorkoutSessionResponse> unshareWorkoutSession(
            @Parameter(description = "Workout session ID") @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        WorkoutSessionResponse response = workoutSessionService.unshareWorkoutSession(sessionId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/shared")
    @Operation(summary = "Get shared workout sessions", description = "Get user's shared workout sessions")
    @ApiResponse(responseCode = "200", description = "Shared sessions retrieved successfully")
    public ResponseEntity<List<WorkoutSessionResponse>> getSharedWorkoutSessions(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<WorkoutSessionResponse> sharedSessions = workoutSessionService.getSharedWorkoutSessions(userDetails.getUsername());
        return ResponseEntity.ok(sharedSessions);
    }

    // =======================
    // SCHEDULED WORKOUT INTEGRATION
    // =======================

    @PostMapping("/scheduled/{scheduledWorkoutId}/start")
    @Operation(summary = "Start scheduled workout", description = "Start a workout session from a scheduled workout")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Scheduled workout started successfully"),
            @ApiResponse(responseCode = "404", description = "Scheduled workout not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to start this scheduled workout")
    })
    public ResponseEntity<WorkoutSessionResponse> startScheduledWorkout(
            @Parameter(description = "Scheduled workout ID") @PathVariable Long scheduledWorkoutId,
            @AuthenticationPrincipal UserDetails userDetails) {

        WorkoutSessionResponse response = workoutSessionService.startScheduledWorkout(userDetails.getUsername(), scheduledWorkoutId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =======================
    // EXCEPTION HANDLERS
    // =======================

    @ExceptionHandler(WorkoutSessionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWorkoutSessionNotFound(
            WorkoutSessionNotFoundException ex, HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Workout Session Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {

        // Check if it's a business logic exception
        if (ex.getMessage() != null &&
                (ex.getMessage().contains("not found") ||
                        ex.getMessage().contains("does not exist") ||
                        ex.getMessage().contains("not have access"))) {

            HttpStatus status = ex.getMessage().contains("not have access") ?
                    HttpStatus.FORBIDDEN : HttpStatus.NOT_FOUND;

            ErrorResponse error = ErrorResponse.of(
                    status.value(),
                    status == HttpStatus.FORBIDDEN ? "Access Denied" : "Resource Not Found",
                    ex.getMessage(),
                    request.getRequestURI()
            );

            return ResponseEntity.status(status).body(error);
        }

        // For other runtime exceptions, return 500
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}