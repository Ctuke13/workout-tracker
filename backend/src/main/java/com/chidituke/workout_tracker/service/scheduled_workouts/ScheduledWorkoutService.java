package com.chidituke.workout_tracker.service.scheduled_workouts;

import com.chidituke.workout_tracker.controller.workout.ScheduledWorkoutController;
import com.chidituke.workout_tracker.dto.request.performance.CompleteSetRequest;
import com.chidituke.workout_tracker.dto.request.performance.CompleteWorkoutRequest;
import com.chidituke.workout_tracker.dto.request.scheduled_workouts.IndividualExerciseRequest;
import com.chidituke.workout_tracker.dto.request.scheduled_workouts.ScheduledWorkoutRequest;
import com.chidituke.workout_tracker.dto.request.workout_plan.ScheduleMultipleExercisesRequestDTO;
import com.chidituke.workout_tracker.dto.response.performance.PerformanceResponse;
import com.chidituke.workout_tracker.dto.response.performance.WorkoutExecutionSummary;
import com.chidituke.workout_tracker.dto.response.scheduled_workouts.CalendarViewResponse;
import com.chidituke.workout_tracker.dto.response.scheduled_workouts.ScheduledWorkoutResponse;
import com.chidituke.workout_tracker.dto.response.workout_session.WorkoutSessionResponse;
import com.chidituke.workout_tracker.model.workout.ScheduledWorkout;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Facade service for scheduled workouts that delegates to specialized services.
 * <p>
 * This service maintains backward compatibility while providing a clean interface
 * that coordinates between:
 * - ScheduledWorkoutQueryService: Read operations with caching
 * - WorkoutExecutionService: Performance tracking and completion
 * - WorkoutSchedulingService: Scheduling with subscription enforcement
 * - WorkoutAnalyticsService: Statistics and reporting
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduledWorkoutService {

    // Specialized services
    private final ScheduledWorkoutQueryService queryService;
    private final WorkoutExecutionService executionService;
    private final WorkoutSchedulingService schedulingService;
    private final WorkoutAnalyticsService analyticsService;

    // ==================== INDIVIDUAL EXERCISE SCHEDULING ====================

    /**
     * Schedule an individual exercise
     */
    @Transactional
    public ScheduledWorkoutResponse scheduleIndividualExercise(String username, IndividualExerciseRequest request) {
        log.debug("Delegating individual exercise scheduling to WorkoutSchedulingService");
        return schedulingService.scheduleIndividualExercise(username, request);
    }

    /**
     * Update a scheduled individual exercise
     */
    @Transactional
    public ScheduledWorkoutResponse updateScheduledExercise(String username, Long exerciseId,
                                                            IndividualExerciseRequest updates) {
        log.debug("Delegating scheduled exercise update to WorkoutSchedulingService");
        return schedulingService.updateScheduledExercise(username, exerciseId, updates);
    }

    /**
     * Update a scheduled individual exercise (String ID version)
     */
    @Transactional
    public ScheduledWorkoutResponse updateScheduledExercise(String username, String exerciseId,
                                                            IndividualExerciseRequest updates) {
        Long exerciseIdLong = Long.parseLong(exerciseId);
        return updateScheduledExercise(username, exerciseIdLong, updates);
    }

    /**
     * Delete a scheduled individual exercise
     */
    @Transactional
    public void deleteScheduledExercise(String username, Long exerciseId) {
        log.debug("Delegating scheduled exercise deletion to WorkoutSchedulingService");
        schedulingService.deleteScheduledExercise(username, exerciseId);
    }

    /**
     * Delete a scheduled individual exercise (String ID version)
     */
    @Transactional
    public void deleteScheduledExercise(String username, String exerciseId) {
        Long exerciseIdLong = Long.parseLong(exerciseId);
        deleteScheduledExercise(username, exerciseIdLong);
    }

    // ==================== WORKOUT PLAN SCHEDULING ====================

    /**
     * Schedule multiple exercises from a workout plan
     */
    @Transactional
    public List<ScheduledWorkoutResponse> scheduleWorkoutPlan(String username, ScheduleMultipleExercisesRequestDTO request) {
        log.debug("Delegating workout plan scheduling to WorkoutSchedulingService");
        return schedulingService.scheduleWorkoutPlan(username, request);
    }

    /**
     * Schedule a single workout
     */
    @Transactional
    public ScheduledWorkoutResponse scheduleWorkout(String username, ScheduledWorkoutRequest request) {
        log.debug("Delegating workout scheduling to WorkoutSchedulingService");
        return schedulingService.scheduleWorkout(username, request);
    }

    /**
     * Schedule workout plan (alternative method name for backward compatibility)
     */
    @Transactional
    public ScheduledWorkoutResponse scheduleWorkoutPlan(String username, ScheduledWorkoutRequest request) {
        return scheduleWorkout(username, request);
    }

    /**
     * Reschedule an existing workout to a new date
     */
    @Transactional
    public ScheduledWorkoutResponse rescheduleWorkout(String username, Long scheduledWorkoutId, LocalDate newDate) {
        log.debug("Delegating workout rescheduling to WorkoutSchedulingService");
        return schedulingService.rescheduleWorkout(username, scheduledWorkoutId, newDate);
    }

    /**
     * Schedule an entire workout program
     */
    @Transactional
    public List<ScheduledWorkoutResponse> scheduleProgram(String username, Long programId, LocalDate startDate) {
        log.debug("Delegating program scheduling to WorkoutSchedulingService");
        return schedulingService.scheduleProgram(username, programId, startDate);
    }

    // ==================== EXERCISE COMPLETION ====================

    /**
     * Mark exercise as completed (simple version)
     */
    @Transactional
    public ScheduledWorkoutResponse markExerciseCompleted(String username, Long exerciseId) {
        log.debug("Delegating exercise completion to WorkoutExecutionService");
        return executionService.markExerciseCompleted(username, exerciseId);
    }

    /**
     * Mark exercise as completed (String ID version)
     */
    @Transactional
    public ScheduledWorkoutResponse markExerciseCompleted(String username, String exerciseId) {
        Long exerciseIdLong = Long.parseLong(exerciseId);
        return markExerciseCompleted(username, exerciseIdLong);
    }

    /**
     * Mark exercise as completed with detailed data
     */
    @Transactional
    public ScheduledWorkoutResponse markExerciseCompleted(String username, String exerciseId,
                                                          LocalDateTime completedAt,
                                                          Integer totalDurationMinutes,
                                                          String notes,
                                                          String performanceRating) {
        log.debug("Delegating detailed exercise completion to WorkoutExecutionService");
        return executionService.markExerciseCompleted(Long.parseLong(exerciseId), username,
                completedAt, totalDurationMinutes, notes, performanceRating);
    }

    /**
     * Mark multiple exercises as completed
     */
    @Transactional
    public List<ScheduledWorkoutResponse> markMultipleExercisesCompleted(String username, List<Long> exerciseIds) {
        log.debug("Delegating multiple exercise completion to WorkoutExecutionService");
        return executionService.markMultipleExercisesCompleted(username, exerciseIds);
    }

    /**
     * Mark multiple exercises as completed (String IDs version)
     */
    @Transactional
    public List<ScheduledWorkoutResponse> markMultipleExercisesCompletedStringIds(String username, List<String> exerciseIds) {
        List<Long> exerciseIdsLong = exerciseIds.stream()
                .map(Long::parseLong)
                .toList();
        return markMultipleExercisesCompleted(username, exerciseIdsLong);
    }

    // ==================== WORKOUT EXECUTION & PERFORMANCE TRACKING ====================

    /**
     * Start a workout session from scheduled workout
     */
    @Transactional
    public WorkoutSessionResponse startWorkoutExecution(String username, Long scheduledWorkoutId) {
        log.debug("Delegating workout execution start to WorkoutExecutionService");
        return executionService.startWorkoutExecution(username, scheduledWorkoutId);
    }

    /**
     * Complete a set with detailed performance data
     */
    @Transactional
    public PerformanceResponse completeSet(String username, Long workoutSessionId, CompleteSetRequest request) {
        log.debug("Delegating set completion to WorkoutExecutionService");
        return executionService.completeSet(username, workoutSessionId, request);
    }

    /**
     * Complete an exercise (all sets done)
     */
    @Transactional
    public WorkoutSessionResponse completeExercise(String username, Long workoutSessionId,
                                                   Long exerciseId, String completionNotes) {
        log.debug("Delegating exercise completion to WorkoutExecutionService");
        return executionService.completeExercise(username, workoutSessionId, exerciseId, completionNotes);
    }

    /**
     * Complete entire workout session
     */
    @Transactional
    public ScheduledWorkoutResponse completeWorkoutSession(String username, Long workoutSessionId,
                                                           CompleteWorkoutRequest request) {
        log.debug("Delegating workout session completion to WorkoutExecutionService");
        return executionService.completeWorkoutSession(username, workoutSessionId, request);
    }

    /**
     * Get workout execution summary
     */
    public WorkoutExecutionSummary getWorkoutExecutionSummary(String username, Long workoutSessionId) {
        log.debug("Delegating workout execution summary to WorkoutExecutionService");
        return executionService.getWorkoutExecutionSummary(username, workoutSessionId);
    }

    /**
     * Get batch workout results
     */
    public Map<String, Object> getBatchWorkoutResults(String username, List<String> exerciseIds) {
        log.debug("Delegating batch workout results to WorkoutExecutionService");
        return executionService.getBatchWorkoutResults(username, exerciseIds);
    }

    // ==================== QUERY OPERATIONS ====================

    /**
     * Get user workouts
     */
    public List<ScheduledWorkoutResponse> getUserWorkouts(String username) {
        log.debug("Delegating user workouts query to ScheduledWorkoutQueryService");
        return queryService.getUserWorkouts(queryService.findUserByUsername(username).getId());
    }

    /**
     * Get upcoming workouts
     */
    public List<ScheduledWorkoutResponse> getUpcomingWorkouts(String username, int limit) {
        log.debug("Delegating upcoming workouts query to ScheduledWorkoutQueryService");
        return queryService.getUpcomingWorkouts(queryService.findUserByUsername(username).getId(), limit);
    }

    /**
     * Get recent completed workouts
     */
    public List<ScheduledWorkoutResponse> getRecentCompletedWorkouts(String username, int limit) {
        log.debug("Delegating recent completed workouts query to ScheduledWorkoutQueryService");
        return queryService.getRecentCompletedWorkouts(queryService.findUserByUsername(username).getId(), limit);
    }

    /**
     * Get workout calendar
     */
    public CalendarViewResponse getWorkoutCalendar(String username, LocalDate startDate, LocalDate endDate) {
        log.debug("Delegating workout calendar query to ScheduledWorkoutQueryService");
        return queryService.getWorkoutCalendar(queryService.findUserByUsername(username).getId(), startDate, endDate);
    }

    /**
     * Get calendar view
     */
    public CalendarViewResponse getCalendarView(String username, LocalDate startDate, LocalDate endDate) {
        log.debug("Delegating calendar view to ScheduledWorkoutQueryService");
        return queryService.getWorkoutCalendar(queryService.findUserByUsername(username).getId(), startDate, endDate);
    }

    /**
     * Get exercises for date
     */
    public List<ScheduledWorkoutResponse> getExercisesForDate(String username, LocalDate date) {
        log.debug("Delegating exercises for date query to ScheduledWorkoutQueryService");
        return queryService.getWorkoutsForDate(queryService.findUserByUsername(username).getId(), date);
    }

    /**
     * Get workouts for date range
     */
    public List<ScheduledWorkoutResponse> getScheduledExercisesForDateRange(String username,
                                                                            LocalDate startDate, LocalDate endDate) {
        log.debug("Delegating date range query to ScheduledWorkoutQueryService");
        return queryService.getWorkoutsForDateRange(queryService.findUserByUsername(username).getId(), startDate, endDate);
    }

    /**
     * Get today's workouts
     */
    public List<ScheduledWorkoutResponse> getTodaysWorkouts(String username) {
        log.debug("Delegating today's workouts query to ScheduledWorkoutQueryService");
        return queryService.getWorkoutsForDate(queryService.findUserByUsername(username).getId(), LocalDate.now());
    }

    /**
     * Get upcoming workouts for next N days
     */
    public List<ScheduledWorkoutResponse> getUpcomingWorkoutsForDays(String username, int days) {
        log.debug("Delegating upcoming workouts query to ScheduledWorkoutQueryService");
        LocalDate endDate = LocalDate.now().plusDays(days);
        return queryService.getWorkoutsForDateRange(queryService.findUserByUsername(username).getId(), LocalDate.now(), endDate);
    }

    /**
     * Get workouts by status
     */
    public List<ScheduledWorkoutResponse> getWorkoutsByStatus(String username, String status) {
        log.debug("Delegating workouts by status query to ScheduledWorkoutQueryService");
        return queryService.getWorkoutsByStatus(queryService.findUserByUsername(username).getId(),
                ScheduledWorkout.ScheduleStatus.valueOf(status));
    }

    /**
     * Get pending workouts
     */
    public List<ScheduledWorkoutResponse> getPendingWorkouts(String username) {
        log.debug("Delegating pending workouts query to ScheduledWorkoutQueryService");
        return queryService.getPendingWorkouts(queryService.findUserByUsername(username).getId());
    }

    /**
     * Get completed workouts
     */
    public List<ScheduledWorkoutResponse> getCompletedWorkouts(String username) {
        log.debug("Delegating completed workouts query to ScheduledWorkoutQueryService");
        return queryService.getCompletedWorkouts(queryService.findUserByUsername(username).getId());
    }

    /**
     * Search user workouts
     */
    public Page<ScheduledWorkoutResponse> searchUserWorkouts(String username, String searchTerm, Pageable pageable) {
        log.debug("Delegating workout search to ScheduledWorkoutQueryService");
        return queryService.searchUserWorkouts(queryService.findUserByUsername(username).getId(), searchTerm, pageable);
    }

    /**
     * Get user workouts paginated
     */
    public Page<ScheduledWorkoutResponse> getUserWorkoutsPaginated(String username, Pageable pageable) {
        log.debug("Delegating paginated workouts query to ScheduledWorkoutQueryService");
        return queryService.getUserWorkoutsPaginated(queryService.findUserByUsername(username).getId(), pageable);
    }

    // ==================== ANALYTICS & STATISTICS ====================

    /**
     * Get workout statistics
     */
    public ScheduledWorkoutController.WorkoutStatsResponse getWorkoutStats(String username, LocalDate date) {
        log.debug("Delegating workout stats to WorkoutAnalyticsService");
        return analyticsService.getWorkoutStats(username, date);
    }

    /**
     * Get scheduling analytics
     */
    public Map<String, Object> getSchedulingAnalytics(String username, LocalDate startDate, LocalDate endDate) {
        log.debug("Delegating scheduling analytics to WorkoutAnalyticsService");
        return analyticsService.getSchedulingAnalytics(username, startDate, endDate);
    }

    /**
     * Get user analytics summary
     */
    public Map<String, Object> getUserAnalyticsSummary(String username) {
        log.debug("Delegating user analytics summary to WorkoutAnalyticsService");
        return analyticsService.getUserAnalyticsSummary(username);
    }

    /**
     * Get current workout streak
     */
    public int getCurrentWorkoutStreak(String username) {
        log.debug("Delegating current streak to WorkoutAnalyticsService");
        return analyticsService.calculateCurrentStreak(queryService.findUserByUsername(username));
    }

    // ==================== DATA MANAGEMENT ====================

    /**
     * Clean up old scheduled workouts for FREE users
     */
    @Transactional
    public void cleanupOldScheduledWorkouts(String username) {
        log.debug("Delegating cleanup to WorkoutAnalyticsService");
        analyticsService.cleanupOldScheduledWorkouts(username);
    }

    /**
     * Generate exercise analysis report
     */
    public void logExerciseAnalysis(String username, LocalDate date) {
        log.debug("Delegating exercise analysis to WorkoutAnalyticsService");
        analyticsService.logExerciseAnalysis(username, date);
    }

    // ==================== ADVANCED WORKOUT OPERATIONS ====================

    @Transactional
    public ScheduledWorkoutResponse startScheduledWorkout(String username, Long scheduledWorkoutId) {
        log.debug("Starting scheduled workout - delegating to WorkoutExecutionService");
        // Start the workout execution (creates workout session)
        WorkoutSessionResponse session = executionService.startWorkoutExecution(username, scheduledWorkoutId);

        // Get the updated scheduled workout to return
        return queryService.getWorkoutResponse(scheduledWorkoutId);
    }

    /**
     * Get overdue workouts
     */
    public List<ScheduledWorkoutResponse> getOverdueWorkouts(String username) {
        log.debug("Delegating overdue workouts query to ScheduledWorkoutQueryService");
        Long userId = queryService.findUserByUsername(username).getId();
        return queryService.getWorkoutsForDateRange(userId, LocalDate.now().minusDays(30), LocalDate.now().minusDays(1))
                .stream()
                .filter(w -> "SCHEDULED".equals(w.getStatus()))
                .toList();
    }

    /**
     * Enhanced exercise completion with full performance tracking
     */
    @Transactional
    public ScheduledWorkoutResponse markExerciseCompletedWithPerformance(String username, String exerciseId,
                                                                         WorkoutExecutionService.WorkoutCompletionData completionData) {
        log.debug("Delegating enhanced exercise completion to WorkoutExecutionService");
        return executionService.markExerciseCompletedWithPerformance(username, exerciseId, completionData);
    }

    // ==================== BACKWARD COMPATIBILITY METHODS ====================

    /**
     * Permanently delete scheduled workout
     */
    @Transactional
    public void permanentlyDeleteScheduledWorkout(String username, Long scheduledWorkoutId) {
        log.debug("Delegating permanent deletion to WorkoutSchedulingService");
        schedulingService.deleteScheduledExercise(username, scheduledWorkoutId);
    }

    /**
     * Get program schedule
     */
    public List<ScheduledWorkoutResponse> getProgramSchedule(String username, Long programId) {
        log.debug("Delegating program schedule query to ScheduledWorkoutQueryService");
        // This would need implementation in the query service if needed
        throw new UnsupportedOperationException("Program schedule queries not yet implemented in query service");
    }

    /**
     * Get workouts for week
     */
    public List<ScheduledWorkoutResponse> getWorkoutsForWeek(String username, LocalDate weekStart) {
        log.debug("Delegating weekly workouts query to ScheduledWorkoutQueryService");
        return queryService.getWorkoutsForWeek(queryService.findUserByUsername(username).getId(), weekStart);
    }

    /**
     * Get workouts for month
     */
    public List<ScheduledWorkoutResponse> getWorkoutsForMonth(String username, int month, int year) {
        log.debug("Delegating monthly workouts query to ScheduledWorkoutQueryService");
        return queryService.getWorkoutsForMonth(queryService.findUserByUsername(username).getId(), month, year);
    }

    /**
     * Check for workout conflicts
     */
    public boolean hasWorkoutConflict(String username, LocalDateTime scheduledDateTime, Long excludeWorkoutId) {
        log.debug("Delegating conflict check to ScheduledWorkoutQueryService");
        return queryService.hasWorkoutConflict(queryService.findUserByUsername(username).getId(),
                scheduledDateTime, excludeWorkoutId);
    }

    /**
     * Get conflicting workouts
     */
    public List<ScheduledWorkoutResponse> getConflictingWorkouts(String username, LocalDateTime scheduledDateTime) {
        log.debug("Delegating conflicting workouts query to ScheduledWorkoutQueryService");
        return queryService.getConflictingWorkouts(queryService.findUserByUsername(username).getId(), scheduledDateTime);
    }
}