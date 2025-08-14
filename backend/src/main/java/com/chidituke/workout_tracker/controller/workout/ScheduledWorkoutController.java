package com.chidituke.workout_tracker.controller.workout;

import com.chidituke.workout_tracker.dto.request.scheduled_workouts.*;
import com.chidituke.workout_tracker.dto.response.scheduled_workouts.*;
import com.chidituke.workout_tracker.dto.request.scheduled_workouts.ScheduledWorkoutRequest;
import com.chidituke.workout_tracker.dto.request.workout_plan.ScheduleMultipleExercisesRequestDTO;
import com.chidituke.workout_tracker.dto.response.scheduled_workouts.ScheduledWorkoutResponse;
import com.chidituke.workout_tracker.exceptions.scheduled_workout.ScheduledWorkoutNotFoundException;
import com.chidituke.workout_tracker.exceptions.scheduled_workout.UnauthorizedScheduledWorkoutAccessException;
import com.chidituke.workout_tracker.exceptions.scheduled_workout.WorkoutInProgressException;
import com.chidituke.workout_tracker.exceptions.scheduled_workout.InvalidWorkoutStateException;
import com.chidituke.workout_tracker.exceptions.subscription.SubscriptionLimitExceededException;
import com.chidituke.workout_tracker.dto.request.scheduled_workouts.IndividualExerciseRequest;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.service.workout.ScheduledWorkoutService;
import com.chidituke.workout_tracker.service.user.SubscriptionService;
import com.chidituke.workout_tracker.service.user.UserService;
import com.chidituke.workout_tracker.repository.workout.ScheduledWorkoutRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Unified Calendar and Workout Scheduling Controller
 * Handles all calendar functionality including individual exercises, workout plans, and program scheduling
 */
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Calendar & Scheduling", description = "Unified workout calendar and scheduling endpoints")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ScheduledWorkoutController {

    private final ScheduledWorkoutService scheduledWorkoutService;
    private final SubscriptionService subscriptionService;
    private final UserService userService;
    private final ScheduledWorkoutRepository scheduledWorkoutRepository;

    // ===========================================================================================
    // CALENDAR VIEW ENDPOINTS
    // ===========================================================================================

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

    /**
     * Get exercises for a specific date
     */
    @GetMapping("/exercises/date/{dateString}")
    @Operation(summary = "Get exercises for a specific date",
            description = "Get all scheduled exercises for a specific date")
    public ResponseEntity<List<ScheduledWorkoutResponse>> getExercisesForDate(
            @Parameter(description = "Date string (YYYY-MM-DD)")
            @PathVariable String dateString,
            Authentication authentication) {

        String username = authentication.getName();
        LocalDate date = LocalDate.parse(dateString);
        List<ScheduledWorkoutResponse> exercises = scheduledWorkoutService.getExercisesForDate(username, date);

        log.info("Found {} exercises for user {} on {}", exercises.size(), username, dateString);
        return ResponseEntity.ok(exercises);
    }

    /**
     * Get scheduled exercises for a date range
     */
    @GetMapping("/exercises")
    @Operation(summary = "Get scheduled exercises for date range",
            description = "Get all scheduled exercises within a date range")
    public ResponseEntity<List<ScheduledWorkoutResponse>> getScheduledExercises(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        String username = authentication.getName();
        List<ScheduledWorkoutResponse> exercises = scheduledWorkoutService
                .getScheduledExercisesForDateRange(username, startDate, endDate);

        log.info("Found {} scheduled exercises for user {} from {} to {}",
                exercises.size(), username, startDate, endDate);
        return ResponseEntity.ok(exercises);
    }

    // ===========================================================================================
    // INDIVIDUAL EXERCISE SCHEDULING
    // ===========================================================================================

    /**
     * Schedule an individual exercise (with subscription validation)
     */
    @PostMapping("/schedule-exercise")
    @Operation(summary = "Schedule an individual exercise",
            description = "Schedule a single exercise with custom configuration and subscription validation")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Exercise scheduled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid exercise request"),
            @ApiResponse(responseCode = "403", description = "Subscription limits exceeded"),
            @ApiResponse(responseCode = "404", description = "Exercise not found")
    })
    public ResponseEntity<ScheduledWorkoutResponse> scheduleIndividualExercise(
            @Parameter(description = "Individual exercise scheduling details")
            @Valid @RequestBody IndividualExerciseRequest request,
            Authentication authentication) {

        try {
            String username = authentication.getName();

            log.info("Scheduling individual exercise {} for user {} on {}",
                    request.getExerciseId(), username, request.getScheduledDate());

            ScheduledWorkoutResponse response = scheduledWorkoutService
                    .scheduleIndividualExercise(username, request);

            log.info("Successfully scheduled individual exercise for user {}", username);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (SubscriptionLimitExceededException e) {
            log.warn("Subscription limit exceeded for user {}: {}", authentication.getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header("X-Upgrade-Required", "true")
                    .header("X-Error-Type", "SUBSCRIPTION_LIMIT")
                    .header("X-Error-Message", e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Error scheduling individual exercise: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update a scheduled individual exercise
     */
    @PutMapping("/exercises/{exerciseId}")
    @Operation(summary = "Update a scheduled exercise",
            description = "Update the configuration of a scheduled individual exercise")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Exercise updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid update request"),
            @ApiResponse(responseCode = "403", description = "User does not own this exercise"),
            @ApiResponse(responseCode = "404", description = "Scheduled exercise not found")
    })
    public ResponseEntity<ScheduledWorkoutResponse> updateScheduledExercise(
            @Parameter(description = "Scheduled exercise ID")
            @PathVariable Long exerciseId,
            @Parameter(description = "Updated exercise configuration")
            @Valid @RequestBody IndividualExerciseRequest updates,
            Authentication authentication) {

        try {
            String username = authentication.getName();

            log.info("Updating scheduled exercise {} for user {}", exerciseId, username);

            ScheduledWorkoutResponse response = scheduledWorkoutService
                    .updateScheduledExercise(username, exerciseId, updates);

            log.info("Successfully updated scheduled exercise {} for user {}", exerciseId, username);
            return ResponseEntity.ok(response);

        } catch (ScheduledWorkoutNotFoundException e) {
            log.warn("Scheduled exercise {} not found for user {}", exerciseId, authentication.getName());
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedScheduledWorkoutAccessException e) {
            log.warn("User {} attempted to update exercise {} they don't own",
                    authentication.getName(), exerciseId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error updating scheduled exercise: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a scheduled individual exercise
     */
    @DeleteMapping("/exercises/{exerciseId}")
    @Operation(summary = "Delete a scheduled exercise",
            description = "Permanently delete a scheduled individual exercise")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Exercise deleted successfully"),
            @ApiResponse(responseCode = "403", description = "User does not own this exercise"),
            @ApiResponse(responseCode = "404", description = "Scheduled exercise not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete exercise in current state")
    })
    public ResponseEntity<Void> deleteScheduledExercise(
            @Parameter(description = "Scheduled exercise ID")
            @PathVariable Long exerciseId,
            Authentication authentication) {

        try {
            String username = authentication.getName();

            log.info("Deleting scheduled exercise {} for user {}", exerciseId, username);

            scheduledWorkoutService.deleteScheduledExercise(username, exerciseId);

            log.info("Successfully deleted scheduled exercise {} for user {}", exerciseId, username);
            return ResponseEntity.noContent().build();

        } catch (ScheduledWorkoutNotFoundException e) {
            log.warn("Scheduled exercise {} not found for user {}", exerciseId, authentication.getName());
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedScheduledWorkoutAccessException e) {
            log.warn("User {} attempted to delete exercise {} they don't own",
                    authentication.getName(), exerciseId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error deleting scheduled exercise: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===========================================================================================
    // WORKOUT PLAN SCHEDULING
    // ===========================================================================================

    /**
     * Schedule a single workout plan
     */
    @PostMapping("/schedule-workout-plan")
    @Operation(summary = "Schedule a workout plan",
            description = "Schedule a single workout plan for a specific date")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Workout plan scheduled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid scheduling request"),
            @ApiResponse(responseCode = "403", description = "Subscription limits exceeded"),
            @ApiResponse(responseCode = "404", description = "Workout plan not found"),
            @ApiResponse(responseCode = "409", description = "Scheduling conflict detected")
    })
    public ResponseEntity<ScheduledWorkoutResponse> scheduleWorkoutPlan(
            @Parameter(description = "Workout plan scheduling details")
            @RequestBody @Valid ScheduledWorkoutRequest request,
            Authentication authentication) {

        try {
            String username = authentication.getName();

            log.info("Scheduling workout plan {} for user {} on {}",
                    request.getWorkoutPlanId(), username, request.getScheduledDate());

            ScheduledWorkoutResponse scheduledWorkout = scheduledWorkoutService
                    .scheduleWorkout(username, request);

            log.info("Successfully scheduled workout plan for user {}", username);
            return ResponseEntity.status(HttpStatus.CREATED).body(scheduledWorkout);

        } catch (SubscriptionLimitExceededException e) {
            log.warn("Subscription limit exceeded for user {}: {}", authentication.getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header("X-Upgrade-Required", "true")
                    .header("X-Error-Type", "SUBSCRIPTION_LIMIT")
                    .header("X-Error-Message", e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Error scheduling workout plan: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Schedule multiple exercises from a workout plan (enforces subscription limits)
     */
    @PostMapping("/schedule-multiple-exercises")
    @Operation(summary = "Schedule multiple exercises from workout plan",
            description = "Schedule multiple exercises from a workout plan with subscription validation")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Exercises scheduled successfully"),
            @ApiResponse(responseCode = "403", description = "Subscription limit exceeded"),
            @ApiResponse(responseCode = "404", description = "Workout plan not found")
    })
    public ResponseEntity<List<ScheduledWorkoutResponse>> scheduleMultipleExercisesFromPlan(
            @Parameter(description = "Multiple exercises scheduling details")
            @Valid @RequestBody ScheduleMultipleExercisesRequestDTO request,
            Authentication authentication) {

        try {
            String username = authentication.getName();

            log.info("Scheduling multiple exercises from workout plan {} for user {} on {}",
                    request.getWorkoutPlanId(), username, request.getScheduledDate());

            List<ScheduledWorkoutResponse> scheduledWorkouts = scheduledWorkoutService
                    .scheduleWorkoutPlan(username, request);

            log.info("Successfully scheduled {} exercises from workout plan for user {}",
                    scheduledWorkouts.size(), username);
            return ResponseEntity.status(HttpStatus.CREATED).body(scheduledWorkouts);

        } catch (SubscriptionLimitExceededException e) {
            log.warn("Subscription limit exceeded for user {}: {}", authentication.getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header("X-Upgrade-Required", "true")
                    .header("X-Error-Type", "SUBSCRIPTION_LIMIT")
                    .header("X-Error-Message", e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Error scheduling multiple exercises: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===========================================================================================
    // EXERCISE COMPLETION TRACKING
    // ===========================================================================================

    /**
     * Mark exercise as completed
     */
    @PutMapping("/exercises/{exerciseId}/complete")
    @Operation(summary = "Mark exercise as completed",
            description = "Mark a scheduled exercise as completed")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Exercise marked as completed"),
            @ApiResponse(responseCode = "403", description = "User does not own this exercise"),
            @ApiResponse(responseCode = "404", description = "Scheduled exercise not found"),
            @ApiResponse(responseCode = "409", description = "Exercise already completed or cannot be completed")
    })
    public ResponseEntity<ScheduledWorkoutResponse> markExerciseCompleted(
            @Parameter(description = "Scheduled exercise ID")
            @PathVariable Long exerciseId,
            Authentication authentication) {

        try {
            String username = authentication.getName();

            log.info("Marking exercise {} as completed for user {}", exerciseId, username);

            ScheduledWorkoutResponse response = scheduledWorkoutService
                    .markExerciseCompleted(username, exerciseId);

            log.info("Successfully marked exercise {} as completed for user {}", exerciseId, username);
            return ResponseEntity.ok(response);

        } catch (ScheduledWorkoutNotFoundException e) {
            log.warn("Scheduled exercise {} not found for user {}", exerciseId, authentication.getName());
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedScheduledWorkoutAccessException e) {
            log.warn("User {} attempted to complete exercise {} they don't own",
                    authentication.getName(), exerciseId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error marking exercise as completed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mark multiple exercises as completed (batch operation)
     */
    @PutMapping("/exercises/complete-batch")
    @Operation(summary = "Mark multiple exercises as completed",
            description = "Mark multiple scheduled exercises as completed in a single operation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Exercises marked as completed"),
            @ApiResponse(responseCode = "400", description = "Invalid batch completion request"),
            @ApiResponse(responseCode = "403", description = "User does not own some exercises")
    })
    public ResponseEntity<List<ScheduledWorkoutResponse>> markMultipleExercisesCompleted(
            @Parameter(description = "Batch completion request")
            @Valid @RequestBody BatchCompleteRequest request,
            Authentication authentication) {

        try {
            String username = authentication.getName();

            log.info("Marking {} exercises as completed for user {}",
                    request.getExerciseIds().size(), username);

            List<ScheduledWorkoutResponse> responses = scheduledWorkoutService
                    .markMultipleExercisesCompleted(username, request.getExerciseIds());

            log.info("Successfully marked {} exercises as completed for user {}",
                    responses.size(), username);
            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            log.error("Error marking multiple exercises as completed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===========================================================================================
    // WORKOUT MANAGEMENT
    // ===========================================================================================

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
            @Parameter(description = "Scheduled workout ID")
            @PathVariable Long scheduledWorkoutId,
            @Parameter(description = "Reschedule details")
            @RequestBody @Valid RescheduleWorkoutRequest request,
            Authentication authentication) {

        try {
            String username = authentication.getName();

            log.info("Rescheduling workout {} for user {} to {}",
                    scheduledWorkoutId, username, request.getNewScheduledDate());

            ScheduledWorkoutResponse rescheduledWorkout = scheduledWorkoutService
                    .rescheduleWorkout(username, scheduledWorkoutId, request.getNewScheduledDate());

            log.info("Successfully rescheduled workout {} for user {}", scheduledWorkoutId, username);
            return ResponseEntity.ok(rescheduledWorkout);

        } catch (ScheduledWorkoutNotFoundException e) {
            log.warn("Scheduled workout {} not found for user {}", scheduledWorkoutId, authentication.getName());
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedScheduledWorkoutAccessException e) {
            log.warn("User {} attempted to reschedule workout {} they don't own",
                    authentication.getName(), scheduledWorkoutId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error rescheduling workout: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
            @Parameter(description = "Scheduled workout ID")
            @PathVariable Long scheduledWorkoutId,
            Authentication authentication) {

        try {
            String username = authentication.getName();

            log.info("Deleting scheduled workout {} for user {}", scheduledWorkoutId, username);

            scheduledWorkoutService.permanentlyDeleteScheduledWorkout(username, scheduledWorkoutId);

            log.info("Successfully deleted scheduled workout {} for user {}", scheduledWorkoutId, username);
            return ResponseEntity.noContent().build();

        } catch (ScheduledWorkoutNotFoundException e) {
            log.warn("Scheduled workout {} not found for user {}", scheduledWorkoutId, authentication.getName());
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedScheduledWorkoutAccessException e) {
            log.warn("User {} attempted to delete workout {} they don't own",
                    authentication.getName(), scheduledWorkoutId);
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
        } catch (Exception e) {
            log.error("Error deleting scheduled workout: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

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
            @Parameter(description = "Scheduled workout ID")
            @PathVariable Long scheduledWorkoutId,
            Authentication authentication) {

        try {
            String username = authentication.getName();

            log.info("Starting scheduled workout {} for user {}", scheduledWorkoutId, username);

            ScheduledWorkoutResponse startedWorkout = scheduledWorkoutService
                    .startScheduledWorkout(username, scheduledWorkoutId);

            log.info("Successfully started scheduled workout {} for user {}", scheduledWorkoutId, username);
            return ResponseEntity.ok(startedWorkout);

        } catch (ScheduledWorkoutNotFoundException e) {
            log.warn("Scheduled workout {} not found for user {}", scheduledWorkoutId, authentication.getName());
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedScheduledWorkoutAccessException e) {
            log.warn("User {} attempted to start workout {} they don't own",
                    authentication.getName(), scheduledWorkoutId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("Error starting scheduled workout: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===========================================================================================
    // PROGRAM SCHEDULING
    // ===========================================================================================

    /**
     * Schedule an entire workout program
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
    public ResponseEntity<List<ScheduledWorkoutResponse>> scheduleWorkoutProgram(
            @Parameter(description = "Program scheduling details")
            @RequestBody @Valid ProgramScheduleRequest request,
            Authentication authentication) {

        try {
            String username = authentication.getName();

            log.info("Scheduling workout program {} for user {} starting {}",
                    request.getProgramId(), username, request.getStartDate());

            List<ScheduledWorkoutResponse> scheduledWorkouts = scheduledWorkoutService
                    .scheduleProgram(username, request.getProgramId(), request.getStartDate());

            log.info("Successfully scheduled {} workouts from program for user {}",
                    scheduledWorkouts.size(), username);
            return ResponseEntity.status(HttpStatus.CREATED).body(scheduledWorkouts);

        } catch (SubscriptionLimitExceededException e) {
            log.warn("Subscription limit exceeded for user {}: {}", authentication.getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header("X-Upgrade-Required", "true")
                    .header("X-Error-Type", "SUBSCRIPTION_LIMIT")
                    .header("X-Error-Message", e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Error scheduling workout program: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get program schedule
     */
    @GetMapping("/program/{programId}")
    @Operation(summary = "Get program schedule",
            description = "Get all scheduled workouts for a specific program")
    public ResponseEntity<List<ScheduledWorkoutResponse>> getProgramSchedule(
            @Parameter(description = "Program ID")
            @PathVariable Long programId,
            Authentication authentication) {

        String username = authentication.getName();

        log.info("Getting program schedule for program {} and user {}", programId, username);

        List<ScheduledWorkoutResponse> programSchedule = scheduledWorkoutService
                .getProgramSchedule(username, programId);

        log.info("Found {} scheduled workouts for program {} and user {}",
                programSchedule.size(), programId, username);
        return ResponseEntity.ok(programSchedule);
    }

    /**
     * Schedule a new exercise using calendar-style endpoint
     * POST /api/calendar/exercises (alternative to /schedule-exercise)
     */
    @PostMapping("/exercises")
    @Operation(summary = "Schedule a new individual exercise (calendar style)",
            description = "Alternative endpoint for scheduling individual exercises")
    public ResponseEntity<ScheduledWorkoutResponse> scheduleExerciseCalendarStyle(
            @Parameter(description = "Individual exercise scheduling details")
            @Valid @RequestBody IndividualExerciseRequest request,
            Authentication authentication) {

        // Delegate to existing method
        return scheduleIndividualExercise(request, authentication);
    }

    /**
     * Schedule a complete workout plan using calendar-style endpoint
     * POST /api/calendar/workout-plans (alternative to /schedule-workout-plan)
     */
    @PostMapping("/workout-plans")
    @Operation(summary = "Schedule a complete workout plan (calendar style)",
            description = "Alternative endpoint for scheduling workout plans")
    public ResponseEntity<ScheduledWorkoutResponse> scheduleWorkoutPlanCalendarStyle(
            @Parameter(description = "Workout plan scheduling details")
            @Valid @RequestBody ScheduledWorkoutRequest request,
            Authentication authentication) {

        // Delegate to existing method
        return scheduleWorkoutPlan(request, authentication);
    }

    // ===========================================================================================
    // STATISTICS & ANALYTICS
    // ===========================================================================================

    /**
     * Get detailed workout statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "Get workout statistics",
            description = "Get comprehensive workout statistics and progress tracking")
    public ResponseEntity<WorkoutStatsResponse> getWorkoutStats(
            @Parameter(description = "Optional specific date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {

        String username = authentication.getName();

        log.info("Getting workout statistics for user {} (date: {})",
                username, date != null ? date : "all time");

        WorkoutStatsResponse stats = scheduledWorkoutService.getWorkoutStats(username, date);

        return ResponseEntity.ok(stats);
    }

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

        log.info("Getting scheduling analytics for user {} from {} to {}", username, startDate, endDate);

        Map<String, Object> analytics = scheduledWorkoutService
                .getSchedulingAnalytics(username, startDate, endDate);

        return ResponseEntity.ok(analytics);
    }

    // ===========================================================================================
    // SUBSCRIPTION & LIMITS
    // ===========================================================================================

    /**
     * Get user's subscription limits for scheduling
     */
    @GetMapping("/subscription-limits")
    @Operation(summary = "Get user subscription limits for scheduling",
            description = "Get current subscription limits and daily usage for calendar")
    public ResponseEntity<Map<String, Object>> getSchedulingSubscriptionLimits(
            Authentication authentication) {
        try {
            String username = authentication.getName();

            log.info("Getting subscription limits for user {}", username);

            Map<String, Object> limits = getSchedulingLimitsForUser(username);

            return ResponseEntity.ok(limits);
        } catch (Exception e) {
            log.error("Error fetching scheduling subscription limits: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get scheduling constraints for user
     */
    @GetMapping("/constraints")
    @Operation(summary = "Get scheduling constraints",
            description = "Get user's scheduling limits based on subscription tier")
    public ResponseEntity<Map<String, Object>> getSchedulingConstraints(Authentication authentication) {
        try {
            String username = authentication.getName();

            log.info("Getting scheduling constraints for user {}", username);

            Map<String, Object> constraints = getSchedulingLimitsForUser(username);

            return ResponseEntity.ok(constraints);
        } catch (Exception e) {
            log.error("Error getting scheduling constraints: {}", e.getMessage());
            // Fallback to basic constraints
            return ResponseEntity.ok(Map.of(
                    "subscriptionTier", "FREE",
                    "dailyExerciseLimit", 3,
                    "advanceSchedulingDays", 7,
                    "maxWorkoutsPerDay", 3
            ));
        }
    }

    // ===========================================================================================
    // UTILITY ENDPOINTS
    // ===========================================================================================

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

        String username = authentication.getName();

        log.info("Checking scheduling conflicts for user {} on {}", username, request.getScheduledDate());

        // This would check for conflicts before actually scheduling
        WorkoutConflictResponse conflictResponse = WorkoutConflictResponse.builder()
                .hasConflicts(false)
                .conflicts(List.of())
                .suggestions(List.of())
                .build();

        return ResponseEntity.ok(conflictResponse);
    }

    /**
     * Bulk reschedule workouts
     */
    @PutMapping("/bulk-reschedule")
    @Operation(summary = "Bulk reschedule workouts",
            description = "Reschedule multiple workouts at once")
    public ResponseEntity<List<ScheduledWorkoutResponse>> bulkRescheduleWorkouts(
            @Parameter(description = "Bulk reschedule request - Map of scheduledWorkoutId -> newDate")
            @RequestBody Map<Long, LocalDate> workoutDateMap,
            Authentication authentication) {

        String username = authentication.getName();

        log.info("Bulk rescheduling {} workouts for user {}", workoutDateMap.size(), username);

        // Implementation would go here
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .header("X-Reason", "Bulk reschedule functionality coming soon")
                .build();
    }

    /**
     * Cleanup old scheduled workouts (for subscription management)
     */
    @PostMapping("/cleanup")
    @Operation(summary = "Cleanup old data",
            description = "Clean up old scheduled workouts (subscription-based data retention)")
    public ResponseEntity<Map<String, String>> cleanupOldData(Authentication authentication) {
        String username = authentication.getName();

        log.info("Cleaning up old data for user {}", username);

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
    @Operation(summary = "Health check",
            description = "Check if the calendar service is healthy")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "service", "ScheduledWorkoutService",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }

    // ===========================================================================================
    // EXCEPTION HANDLERS
    // ===========================================================================================

    /**
     * Handle subscription limit exceeded exceptions
     */
    @ExceptionHandler(SubscriptionLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleSubscriptionLimitExceeded(
            SubscriptionLimitExceededException ex) {

        Map<String, Object> error = Map.of(
                "error", "Subscription Limit Exceeded",
                "message", ex.getMessage(),
                "upgradeRequired", true,
                "timestamp", System.currentTimeMillis()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .header("X-Upgrade-Required", "true")
                .header("X-Error-Type", "SUBSCRIPTION_LIMIT")
                .body(error);
    }

    // ===========================================================================================
    // HELPER METHODS
    // ===========================================================================================

    /**
     * Get scheduling limits for user - COMPLETED IMPLEMENTATION
     */
    private Map<String, Object> getSchedulingLimitsForUser(String username) {
        try {
            // Handle Optional properly
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            Long userId = user.getId();

            // Get subscription limits
            Map<String, Object> limits = subscriptionService.getWorkoutSubscriptionLimits(userId);

            // Use User object instead of Optional
            LocalDate today = LocalDate.now();
            long todayExercises = scheduledWorkoutRepository.countByUserAndScheduledDate(user, today);

            limits.put("todayExerciseCount", todayExercises);
            limits.put("todayExercisesRemaining",
                    Math.max(0, (Integer) limits.get("dailyExerciseLimit") - todayExercises));

            return limits;

        } catch (Exception e) {
            log.error("Error fetching scheduling limits: {}", e.getMessage());
            // Fallback for FREE users
            return Map.of(
                    "tier", "FREE",
                    "dailyExerciseLimit", 3,
                    "advanceSchedulingDays", 7,
                    "todayExerciseCount", 0,
                    "todayExercisesRemaining", 3,
                    "canAccessWorkoutPlans", false,
                    "upgradeSuggestion", "Upgrade to PLUS to access complete workout plans!"
            );
        }
    }

    // ===========================================================================================
    // INNER CLASSES - DTOs FOR NEW FUNCTIONALITY
    // ===========================================================================================


    /**
     * Request DTO for batch completion operations
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchCompleteRequest {
        @NotEmpty(message = "Exercise IDs list cannot be empty")
        private List<Long> exerciseIds;
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