package com.chidituke.workout_tracker.controller.workout;

import com.chidituke.workout_tracker.dto.request.scheduled_workouts.*;
import com.chidituke.workout_tracker.dto.response.scheduled_workouts.*;
import com.chidituke.workout_tracker.dto.request.scheduled_workouts.ScheduledWorkoutRequest;
import com.chidituke.workout_tracker.dto.response.scheduled_workouts.ScheduledWorkoutResponse;
import com.chidituke.workout_tracker.exceptions.scheduled_workout.ScheduledWorkoutNotFoundException;
import com.chidituke.workout_tracker.exceptions.scheduled_workout.UnauthorizedScheduledWorkoutAccessException;
import com.chidituke.workout_tracker.exceptions.scheduled_workout.WorkoutInProgressException;
import com.chidituke.workout_tracker.exceptions.scheduled_workout.InvalidWorkoutStateException;
import com.chidituke.workout_tracker.service.workout.ScheduledWorkoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * Calendar and Workout Scheduling Controller
 * Handles all calendar functionality including scheduling, rescheduling, and program enrollment
 */
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Calendar & Scheduling", description = "Workout calendar and scheduling endpoints")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ScheduledWorkoutController {

    private final ScheduledWorkoutService scheduledWorkoutService;

    // =======================
    // CALENDAR VIEW ENDPOINTS
    // =======================

    /**
     * Get calendar view for a date range
     */
    @GetMapping
    @Operation(summary = "Get calendar view",
            description = "Get scheduled workouts for a date range (calendar view)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Calendar data retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range")
    })
    public ResponseEntity<CalendarViewResponse> getCalendarView(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        String username = authentication.getName();
        CalendarViewResponse calendar = scheduledWorkoutService.getCalendarView(username, startDate, endDate);
        return ResponseEntity.ok(calendar);
    }

    /**
     * Get today's workouts
     */
    @GetMapping("/today")
    @Operation(summary = "Get today's workouts",
            description = "Get all workouts scheduled for today")
    public ResponseEntity<List<ScheduledWorkoutResponse>> getTodaysWorkouts(Authentication authentication) {
        String username = authentication.getName();
        List<ScheduledWorkoutResponse> todaysWorkouts = scheduledWorkoutService.getTodaysWorkouts(username);
        return ResponseEntity.ok(todaysWorkouts);
    }

    /**
     * Get upcoming workouts
     */
    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming workouts",
            description = "Get upcoming workouts for specified number of days")
    public ResponseEntity<List<ScheduledWorkoutResponse>> getUpcomingWorkouts(
            @Parameter(description = "Number of days to look ahead")
            @RequestParam(defaultValue = "7") @Positive int days,
            Authentication authentication) {

        String username = authentication.getName();
        List<ScheduledWorkoutResponse> upcomingWorkouts = scheduledWorkoutService
                .getUpcomingWorkouts(username, days);
        return ResponseEntity.ok(upcomingWorkouts);
    }

    /**
     * Get overdue workouts
     */
    @GetMapping("/overdue")
    @Operation(summary = "Get overdue workouts",
            description = "Get workouts that were scheduled but not completed")
    public ResponseEntity<List<ScheduledWorkoutResponse>> getOverdueWorkouts(Authentication authentication) {
        String username = authentication.getName();
        List<ScheduledWorkoutResponse> overdueWorkouts = scheduledWorkoutService.getOverdueWorkouts(username);
        return ResponseEntity.ok(overdueWorkouts);
    }

    // =======================
    // WORKOUT SCHEDULING
    // =======================

    /**
     * Schedule a single workout
     */
    @PostMapping("/schedule")
    @Operation(summary = "Schedule a workout",
            description = "Schedule a single workout for a specific date")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Workout scheduled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid scheduling request"),
            @ApiResponse(responseCode = "403", description = "Subscription limits exceeded"),
            @ApiResponse(responseCode = "409", description = "Scheduling conflict detected")
    })
    public ResponseEntity<ScheduledWorkoutResponse> scheduleWorkout(
            @Parameter(description = "Workout scheduling details")
            @RequestBody @Valid ScheduledWorkoutRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        ScheduledWorkoutResponse scheduledWorkout = scheduledWorkoutService
                .scheduleWorkout(username, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(scheduledWorkout);
    }

    /**
     * Reschedule an existing workout
     */
    @PutMapping("/{scheduledWorkoutId}/reschedule")
    @Operation(summary = "Reschedule a workout",
            description = "Move a scheduled workout to a different date")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workout rescheduled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid reschedule request"),
            @ApiResponse(responseCode = "403", description = "User does not own this scheduled workout"),
            @ApiResponse(responseCode = "404", description = "Scheduled workout not found"),
            @ApiResponse(responseCode = "409", description = "Scheduling conflict on new date")
    })
    public ResponseEntity<ScheduledWorkoutResponse> rescheduleWorkout(
            @Parameter(description = "Scheduled workout ID") @PathVariable Long scheduledWorkoutId,
            @Parameter(description = "Reschedule details") @RequestBody @Valid RescheduleWorkoutRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        ScheduledWorkoutResponse rescheduledWorkout = scheduledWorkoutService
                .rescheduleWorkout(username, scheduledWorkoutId, request.getNewScheduledDate());

        return ResponseEntity.ok(rescheduledWorkout);
    }

    /**
     * Delete a scheduled workout permanently
     */
    @DeleteMapping("/{scheduledWorkoutId}")
    @Operation(summary = "Delete a scheduled workout",
            description = "Permanently delete a scheduled workout from the user's calendar")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Workout deleted successfully"),
            @ApiResponse(responseCode = "403", description = "User does not own this scheduled workout"),
            @ApiResponse(responseCode = "404", description = "Scheduled workout not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete workout in current state")
    })
    public ResponseEntity<Void> deleteScheduledWorkout(
            @Parameter(description = "Scheduled workout ID") @PathVariable Long scheduledWorkoutId,
            Authentication authentication) {

        String username = authentication.getName();
        log.info("User {} requesting deletion of scheduled workout {}", username, scheduledWorkoutId);

        try {
            // ✅ CHANGED: Actually delete the workout instead of just cancelling
            scheduledWorkoutService.permanentlyDeleteScheduledWorkout(username, scheduledWorkoutId);

            log.info("Successfully deleted scheduled workout {} for user {}", scheduledWorkoutId, username);
            return ResponseEntity.noContent().build();

        } catch (ScheduledWorkoutNotFoundException e) {
            log.warn("Scheduled workout {} not found for user {}", scheduledWorkoutId, username);
            return ResponseEntity.notFound().build();

        } catch (UnauthorizedScheduledWorkoutAccessException e) {
            log.warn("User {} attempted to delete workout {} they don't own", username, scheduledWorkoutId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (WorkoutInProgressException e) {
            log.warn("Cannot delete workout {} - in progress: {}", scheduledWorkoutId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .header("X-Error-Reason", e.getMessage())
                    .build();

        } catch (InvalidWorkoutStateException e) {
            log.warn("Cannot delete workout {} in current state: {}", scheduledWorkoutId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Reason", e.getMessage())
                    .build();
        }
    }

    // =======================
    // WORKOUT EXECUTION
    // =======================

    /**
     * Start a scheduled workout
     */
    @PostMapping("/{scheduledWorkoutId}/start")
    @Operation(summary = "Start a scheduled workout",
            description = "Mark a scheduled workout as in progress")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workout started successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot start workout in current state"),
            @ApiResponse(responseCode = "403", description = "User does not own this scheduled workout"),
            @ApiResponse(responseCode = "404", description = "Scheduled workout not found"),
            @ApiResponse(responseCode = "409", description = "Another workout already in progress")
    })
    public ResponseEntity<ScheduledWorkoutResponse> startScheduledWorkout(
            @Parameter(description = "Scheduled workout ID") @PathVariable Long scheduledWorkoutId,
            Authentication authentication) {

        String username = authentication.getName();
        ScheduledWorkoutResponse startedWorkout = scheduledWorkoutService
                .startScheduledWorkout(username, scheduledWorkoutId);

        return ResponseEntity.ok(startedWorkout);
    }

    // Note: Completing a workout would typically be handled by the WorkoutSession endpoint
    // when a WorkoutSession is created/completed, it would automatically update the ScheduledWorkout

    // =======================
    // PROGRAM SCHEDULING
    // =======================

    /**
     * Schedule an entire program
     */
    @PostMapping("/schedule-program")
    @Operation(summary = "Schedule a workout program",
            description = "Schedule all workouts for a multi-week program")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Program scheduled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid program scheduling request"),
            @ApiResponse(responseCode = "403", description = "Subscription limits exceeded"),
            @ApiResponse(responseCode = "404", description = "Program not found")
    })
    public ResponseEntity<List<ScheduledWorkoutResponse>> scheduleProgram(
            @Parameter(description = "Program scheduling details")
            @RequestBody @Valid ProgramScheduleRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        List<ScheduledWorkoutResponse> scheduledWorkouts = scheduledWorkoutService
                .scheduleProgram(username, request.getProgramId(), request.getStartDate());

        return ResponseEntity.status(HttpStatus.CREATED).body(scheduledWorkouts);
    }

    /**
     * Get program schedule
     */
    @GetMapping("/program/{programId}")
    @Operation(summary = "Get program schedule",
            description = "Get all scheduled workouts for a specific program")
    public ResponseEntity<List<ScheduledWorkoutResponse>> getProgramSchedule(
            @Parameter(description = "Program ID") @PathVariable Long programId,
            Authentication authentication) {

        String username = authentication.getName();
        List<ScheduledWorkoutResponse> programSchedule = scheduledWorkoutService
                .getProgramSchedule(username, programId);

        return ResponseEntity.ok(programSchedule);
    }

    // =======================
    // ANALYTICS & STATISTICS
    // =======================

    /**
     * Get scheduling analytics
     */
    @GetMapping("/analytics")
    @Operation(summary = "Get scheduling analytics",
            description = "Get analytics on scheduling patterns and completion rates")
    public ResponseEntity<Map<String, Object>> getSchedulingAnalytics(
            @Parameter(description = "Start date for analytics")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for analytics")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        String username = authentication.getName();
        Map<String, Object> analytics = scheduledWorkoutService
                .getSchedulingAnalytics(username, startDate, endDate);

        return ResponseEntity.ok(analytics);
    }

    // =======================
    // UTILITY ENDPOINTS
    // =======================

    /**
     * Get scheduling constraints for user
     */
    @GetMapping("/constraints")
    @Operation(summary = "Get scheduling constraints",
            description = "Get user's scheduling limits based on subscription tier")
    public ResponseEntity<Map<String, Object>> getSchedulingConstraints(Authentication authentication) {
        // This would return information about how far ahead the user can schedule
        // based on their subscription tier

        return ResponseEntity.ok(Map.of(
                "message", "Scheduling constraints endpoint - to be implemented",
                "subscriptionTier", "FREE", // Get from user
                "maxDaysAhead", 7,
                "maxWorkoutsPerDay", 2
        ));
    }

    /**
     * Check for scheduling conflicts
     */
    @PostMapping("/check-conflicts")
    @Operation(summary = "Check for scheduling conflicts",
            description = "Check if scheduling a workout would create conflicts")
    public ResponseEntity<WorkoutConflictResponse> checkSchedulingConflicts(
            @Parameter(description = "Conflict check request")
            @RequestBody @Valid ScheduledWorkoutRequest request,
            Authentication authentication) {

        // This would check for conflicts before actually scheduling
        return ResponseEntity.ok(WorkoutConflictResponse.builder()
                .hasConflicts(false)
                .conflicts(List.of())
                .suggestions(List.of())
                .build());
    }

    /**
     * Bulk reschedule workouts
     */
    @PutMapping("/bulk-reschedule")
    @Operation(summary = "Bulk reschedule workouts",
            description = "Reschedule multiple workouts at once")
    public ResponseEntity<List<ScheduledWorkoutResponse>> bulkRescheduleWorkouts(
            @Parameter(description = "Bulk reschedule request")
            @RequestBody Map<Long, LocalDate> workoutDateMap, // scheduledWorkoutId -> newDate
            Authentication authentication) {

        // This would handle bulk rescheduling operations
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .header("X-Reason", "Bulk reschedule functionality coming soon")
                .build();
    }

    // =======================
    // SUBSCRIPTION & MAINTENANCE
    // =======================

    /**
     * Cleanup old scheduled workouts (for free users)
     */
    @PostMapping("/cleanup")
    @Operation(summary = "Cleanup old data",
            description = "Clean up old scheduled workouts (free tier data retention)")
    public ResponseEntity<Map<String, String>> cleanupOldData(Authentication authentication) {
        String username = authentication.getName();
        scheduledWorkoutService.cleanupOldScheduledWorkouts(username);

        return ResponseEntity.ok(Map.of(
                "message", "Data cleanup completed",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the calendar service is healthy")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "service", "ScheduledWorkoutService",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}