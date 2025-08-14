package com.chidituke.workout_tracker.service.workout;

import com.chidituke.workout_tracker.dto.request.scheduled_workouts.ScheduledWorkoutRequest;
import com.chidituke.workout_tracker.dto.request.workout_plan.ScheduleMultipleExercisesRequestDTO;
import com.chidituke.workout_tracker.dto.request.scheduled_workouts.IndividualExerciseRequest;
import com.chidituke.workout_tracker.dto.response.scheduled_workouts.ScheduledWorkoutResponse;
import com.chidituke.workout_tracker.dto.response.scheduled_workouts.CalendarViewResponse;
import com.chidituke.workout_tracker.exceptions.scheduled_workout.*;
import com.chidituke.workout_tracker.exceptions.user.UserNotFoundException;
import com.chidituke.workout_tracker.exceptions.workout_plan.WorkoutPlanNotFoundException;
import com.chidituke.workout_tracker.exceptions.subscription.SubscriptionLimitExceededException;
import com.chidituke.workout_tracker.mapper.workout.ScheduledWorkoutMapper;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.user.enums.SubscriptionTier;
import com.chidituke.workout_tracker.model.workout.ScheduledWorkout;
import com.chidituke.workout_tracker.model.workout.WorkoutPlan;
import com.chidituke.workout_tracker.model.workout.WorkoutProgram;
import com.chidituke.workout_tracker.model.workout.WorkoutSession;
import com.chidituke.workout_tracker.model.workout.PlanExercise;
import com.chidituke.workout_tracker.model.workout.Exercise;
import com.chidituke.workout_tracker.model.workout.WorkoutPlan.DifficultyLevel;
import com.chidituke.workout_tracker.repository.workout.PlanExerciseRepository;
import com.chidituke.workout_tracker.repository.workout.ExerciseRepository;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import com.chidituke.workout_tracker.repository.workout.ScheduledWorkoutRepository;
import com.chidituke.workout_tracker.repository.workout.WorkoutPlanRepository;
import com.chidituke.workout_tracker.repository.workout.WorkoutProgramRepository;
import com.chidituke.workout_tracker.repository.workout.WorkoutSessionRepository;
import com.chidituke.workout_tracker.controller.workout.ScheduledWorkoutController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ✅ FIXED: ScheduledWorkoutService aligned with existing codebase
 *
 * Comprehensive service for managing scheduled workouts with:
 * - Individual workout scheduling ✅
 * - Complete workout plan scheduling with subscription limits ✅
 * - Individual exercise scheduling ✅
 * - Exercise completion tracking ✅
 * - Calendar views with exercise configuration ✅
 * - Workout statistics and analytics ✅
 * - Subscription enforcement (3-exercise daily limit for FREE users) ✅
 * - Program scheduling and analytics ✅
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduledWorkoutService {

    // =======================
    // DEPENDENCIES
    // =======================
    private final ScheduledWorkoutRepository scheduledWorkoutRepository;
    private final UserRepository userRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final WorkoutProgramRepository workoutProgramRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final PlanExerciseRepository planExerciseRepository;
    private final ExerciseRepository exerciseRepository;
    private final ScheduledWorkoutMapper scheduledWorkoutMapper;

    // =======================
    // ✅ FIXED: INDIVIDUAL EXERCISE SCHEDULING
    // =======================

    /**
     * Schedule an individual exercise by creating a temporary single-exercise workout plan.
     * This method bridges the gap between exercise-based user thinking and
     * workout-plan-based data storage.
     */
    @Transactional
    public ScheduledWorkoutResponse scheduleIndividualExercise(String username,
                                                               IndividualExerciseRequest request) {
        try {
            log.info("🎯 Scheduling individual exercise {} for user {} on {}",
                    request.getExerciseId(), username, request.getScheduledDate());

            // Validate user exists and exercise exists
            User user = findUserByUsername(username);
            Exercise exercise = findExerciseById(request.getExerciseId());

            // Check subscription limits before proceeding
            validateIndividualExerciseScheduling(user, request);

            // Create a minimal temporary workout plan containing just this exercise
            WorkoutPlan temporaryPlan = createTemporaryWorkoutPlan(exercise, request);
            WorkoutPlan savedPlan = workoutPlanRepository.save(temporaryPlan);

            log.debug("✅ Created temporary workout plan with ID: {}", savedPlan.getId());

            // Create the scheduled workout entry
            ScheduledWorkout scheduledWorkout = buildScheduledWorkout(user, savedPlan, request);

            // Apply exercise-specific configuration based on exercise type
            applyExerciseConfiguration(scheduledWorkout, exercise, request);

            ScheduledWorkout saved = scheduledWorkoutRepository.save(scheduledWorkout);

            log.info("✅ Successfully scheduled individual exercise for user {}, ID: {}",
                    username, saved.getId());
            return scheduledWorkoutMapper.toResponse(saved);

        } catch (Exception e) {
            log.error("❌ Failed to schedule individual exercise: {}", e.getMessage(), e);
            // Preserve specific exception types while providing user-friendly messages
            if (e instanceof SubscriptionLimitExceededException) {
                throw e;
            } else {
                throw new RuntimeException("Failed to schedule exercise: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Update a scheduled individual exercise
     */
    @Transactional
    public ScheduledWorkoutResponse updateScheduledExercise(String username, Long exerciseId,
                                                            IndividualExerciseRequest updates) {
        ScheduledWorkout scheduledWorkout = findScheduledWorkoutById(exerciseId);
        validateOwnership(scheduledWorkout, username);

        log.info("🔄 Updating scheduled exercise {} for user {}", exerciseId, username);

        // Update the workout details
        if (updates.getScheduledDate() != null) {
            scheduledWorkout.setScheduledDate(updates.getScheduledDate());
        }
        if (updates.getNotes() != null) {
            scheduledWorkout.setCustomNotes(updates.getNotes());
        }

        // Update exercise configuration
        Exercise exercise = findExerciseById(updates.getExerciseId());
        setExerciseConfiguration(scheduledWorkout, exercise, updates);
        scheduledWorkout.setEstimatedDurationMinutes(calculateIndividualExerciseDuration(exercise, updates));

        ScheduledWorkout saved = scheduledWorkoutRepository.save(scheduledWorkout);

        log.info("✅ Successfully updated scheduled exercise {} for user {}", exerciseId, username);
        return scheduledWorkoutMapper.toResponse(saved);
    }

    /**
     * Delete a scheduled individual exercise
     */
    @Transactional
    public void deleteScheduledExercise(String username, Long exerciseId) {
        ScheduledWorkout scheduledWorkout = findScheduledWorkoutById(exerciseId);
        validateOwnership(scheduledWorkout, username);
        validateWorkoutCanBeDeleted(scheduledWorkout);

        log.info("🗑️ Deleting scheduled exercise {} for user {}", exerciseId, username);

        try {
            // Clean up the temporary workout plan if it was created for individual exercise
            WorkoutPlan plan = scheduledWorkout.getWorkoutPlan();
            boolean isIndividualPlan = plan != null &&
                    plan.getWorkoutDescription() != null &&
                    plan.getWorkoutDescription().contains("Individual exercise");

            handleRelatedRecordsBeforeDeletion(scheduledWorkout);
            scheduledWorkoutRepository.delete(scheduledWorkout);

            // Delete the temporary individual plan if it was created for this exercise
            if (isIndividualPlan && plan != null) {
                workoutPlanRepository.delete(plan);
                log.debug("Deleted temporary individual workout plan {}", plan.getId());
            }

            log.info("✅ Successfully deleted scheduled exercise {} for user {}", exerciseId, username);

        } catch (Exception e) {
            log.error("Failed to delete scheduled exercise {} for user {}: {}",
                    exerciseId, username, e.getMessage());
            throw new RuntimeException("Failed to delete exercise: " + e.getMessage());
        }
    }

    // =======================
    // ✅ FIXED: EXERCISE COMPLETION TRACKING
    // =======================

    /**
     * Mark exercise as completed
     */
    @Transactional
    public ScheduledWorkoutResponse markExerciseCompleted(String username, Long exerciseId) {
        ScheduledWorkout scheduledWorkout = findScheduledWorkoutById(exerciseId);
        validateOwnership(scheduledWorkout, username);

        log.info("✅ Marking exercise {} as completed for user {}", exerciseId, username);

        // Validate that the exercise can be completed
        if (scheduledWorkout.getStatus() == ScheduledWorkout.ScheduleStatus.COMPLETED) {
            throw new IllegalStateException("Exercise is already completed");
        }

        if (scheduledWorkout.getStatus() == ScheduledWorkout.ScheduleStatus.CANCELLED) {
            throw new IllegalStateException("Cannot complete a cancelled exercise");
        }

        // Mark as completed
        scheduledWorkout.setStatus(ScheduledWorkout.ScheduleStatus.COMPLETED);
        scheduledWorkout.setCompletedAt(LocalDateTime.now());

        ScheduledWorkout saved = scheduledWorkoutRepository.save(scheduledWorkout);

        log.info("✅ Successfully marked exercise {} as completed for user {}", exerciseId, username);
        return scheduledWorkoutMapper.toResponse(saved);
    }

    @Transactional
    public List<ScheduledWorkoutResponse> markMultipleExercisesCompleted(String username, List<Long> exerciseIds) {
        List<ScheduledWorkoutResponse> responses = new ArrayList<>();

        for (Long exerciseId : exerciseIds) {
            try {
                ScheduledWorkoutResponse response = markExerciseCompleted(username, exerciseId);
                responses.add(response);
            } catch (Exception e) {
                log.error("Failed to mark exercise {} as completed: {}", exerciseId, e.getMessage());
            }
        }

        return responses;
    }

    /**
     * Mark multiple exercises as completed (batch operation)
     */
    @Transactional
    public List<ScheduledWorkoutResponse> markMultipleExercisesCompletedByStringIds(String username, List<String> exerciseIds) {
        List<Long> longIds = exerciseIds.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
        return markMultipleExercisesCompleted(username, longIds);
    }

    // =======================
    // ✅ FIXED: ENHANCED CALENDAR VIEWS
    // =======================

    /**
     * Get exercises for a specific date
     */
    public List<ScheduledWorkoutResponse> getExercisesForDate(String username, LocalDate date) {
        User user = findUserByUsername(username);

        log.debug("📅 Getting exercises for user {} on {}", username, date);

        List<ScheduledWorkout> exercises = scheduledWorkoutRepository
                .findByUserAndScheduledDateOrderByCreatedAtAsc(user, date);

        return exercises.stream()
                .map(scheduledWorkoutMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get scheduled exercises for a date range
     */
    public List<ScheduledWorkoutResponse> getScheduledExercisesForDateRange(String username,
                                                                            LocalDate startDate, LocalDate endDate) {
        User user = findUserByUsername(username);

        log.debug("📅 Getting exercises for user {} from {} to {}", username, startDate, endDate);

        List<ScheduledWorkout> exercises = scheduledWorkoutRepository
                .findByUserAndScheduledDateBetweenOrderByScheduledDateAsc(user, startDate, endDate);

        return exercises.stream()
                .map(scheduledWorkoutMapper::toResponse)
                .collect(Collectors.toList());
    }

    // =======================
    // ✅ FIXED: WORKOUT STATISTICS
    // =======================

    /**
     * Get comprehensive workout statistics
     */
    public ScheduledWorkoutController.WorkoutStatsResponse getWorkoutStats(String username, LocalDate date) {
        User user = findUserByUsername(username);
        LocalDate today = LocalDate.now();

        log.debug("📊 Getting workout statistics for user {} (date: {})",
                username, date != null ? date : "all time");

        // Calculate date ranges
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

        // Today's stats
        List<ScheduledWorkout> todaysWorkouts = scheduledWorkoutRepository
                .findByUserAndScheduledDateOrderByCreatedAtAsc(user, today);
        int exercisesScheduledToday = todaysWorkouts.size();
        int exercisesCompletedToday = (int) todaysWorkouts.stream()
                .filter(sw -> sw.getStatus() == ScheduledWorkout.ScheduleStatus.COMPLETED)
                .count();
        int minutesWorkedOutToday = todaysWorkouts.stream()
                .filter(sw -> sw.getStatus() == ScheduledWorkout.ScheduleStatus.COMPLETED)
                .mapToInt(sw -> sw.getEstimatedDurationMinutes() != null ? sw.getEstimatedDurationMinutes() : 0)
                .sum();

        // This week's stats
        List<ScheduledWorkout> weekWorkouts = scheduledWorkoutRepository
                .findByUserAndScheduledDateBetweenOrderByScheduledDateAsc(user, weekStart, weekEnd);
        int exercisesScheduledThisWeek = weekWorkouts.size();
        int exercisesCompletedThisWeek = (int) weekWorkouts.stream()
                .filter(sw -> sw.getStatus() == ScheduledWorkout.ScheduleStatus.COMPLETED)
                .count();
        int minutesWorkedOutThisWeek = weekWorkouts.stream()
                .filter(sw -> sw.getStatus() == ScheduledWorkout.ScheduleStatus.COMPLETED)
                .mapToInt(sw -> sw.getEstimatedDurationMinutes() != null ? sw.getEstimatedDurationMinutes() : 0)
                .sum();

        // This month's stats
        List<ScheduledWorkout> monthWorkouts = scheduledWorkoutRepository
                .findByUserAndScheduledDateBetweenOrderByScheduledDateAsc(user, monthStart, monthEnd);
        int exercisesScheduledThisMonth = monthWorkouts.size();
        int exercisesCompletedThisMonth = (int) monthWorkouts.stream()
                .filter(sw -> sw.getStatus() == ScheduledWorkout.ScheduleStatus.COMPLETED)
                .count();
        int minutesWorkedOutThisMonth = monthWorkouts.stream()
                .filter(sw -> sw.getStatus() == ScheduledWorkout.ScheduleStatus.COMPLETED)
                .mapToInt(sw -> sw.getEstimatedDurationMinutes() != null ? sw.getEstimatedDurationMinutes() : 0)
                .sum();

        // Completion rates
        double completionRateThisWeek = exercisesScheduledThisWeek > 0 ?
                (double) exercisesCompletedThisWeek / exercisesScheduledThisWeek * 100 : 0.0;
        double completionRateThisMonth = exercisesScheduledThisMonth > 0 ?
                (double) exercisesCompletedThisMonth / exercisesScheduledThisMonth * 100 : 0.0;

        // Calculate streaks and other metrics
        int currentStreak = calculateCurrentStreak(user);
        int longestStreak = calculateLongestStreak(user);
        LocalDate lastWorkoutDate = findLastWorkoutDate(user);
        String lastWorkoutType = findLastWorkoutType(user);
        String favoriteExerciseType = findFavoriteExerciseType(user);

        // Get total completed workouts and minutes
        List<ScheduledWorkout> allCompletedWorkouts = scheduledWorkoutRepository
                .findByUserAndStatusOrderByScheduledDateAsc(user, ScheduledWorkout.ScheduleStatus.COMPLETED);
        int totalWorkoutsCompleted = allCompletedWorkouts.size();
        int totalMinutesWorkedOut = allCompletedWorkouts.stream()
                .mapToInt(sw -> sw.getEstimatedDurationMinutes() != null ? sw.getEstimatedDurationMinutes() : 0)
                .sum();

        return ScheduledWorkoutController.WorkoutStatsResponse.builder()
                .exercisesScheduledToday(exercisesScheduledToday)
                .exercisesCompletedToday(exercisesCompletedToday)
                .minutesWorkedOutToday(minutesWorkedOutToday)
                .exercisesScheduledThisWeek(exercisesScheduledThisWeek)
                .exercisesCompletedThisWeek(exercisesCompletedThisWeek)
                .minutesWorkedOutThisWeek(minutesWorkedOutThisWeek)
                .exercisesScheduledThisMonth(exercisesScheduledThisMonth)
                .exercisesCompletedThisMonth(exercisesCompletedThisMonth)
                .minutesWorkedOutThisMonth(minutesWorkedOutThisMonth)
                .currentStreak(currentStreak)
                .longestStreak(longestStreak)
                .completionRateThisWeek(completionRateThisWeek)
                .completionRateThisMonth(completionRateThisMonth)
                .lastWorkoutDate(lastWorkoutDate)
                .lastWorkoutType(lastWorkoutType)
                .totalWorkoutsCompleted(totalWorkoutsCompleted)
                .totalMinutesWorkedOut(totalMinutesWorkedOut)
                .favoriteExerciseType(favoriteExerciseType)
                .build();
    }

    // =======================
    // ✅ EXISTING: WORKOUT PLAN SCHEDULING (SUBSCRIPTION-AWARE)
    // =======================

    /**
     * Schedule multiple exercises from a workout plan with subscription validation
     */
    @Transactional
    public List<ScheduledWorkoutResponse> scheduleWorkoutPlan(String username, ScheduleMultipleExercisesRequestDTO request) {
        User user = findUserByUsername(username);
        WorkoutPlan workoutPlan = findWorkoutPlanById(request.getWorkoutPlanId());

        log.info("🎯 Scheduling workout plan: {} for user {} on {}",
                workoutPlan.getWorkoutName(), username, request.getScheduledDate());

        // Get exercises from the workout plan
        List<PlanExercise> planExercises = planExerciseRepository.findByWorkoutPlanOrderByOrderInWorkout(workoutPlan);

        if (planExercises.isEmpty()) {
            throw new IllegalArgumentException("Workout plan has no exercises configured");
        }

        // ✅ CRITICAL: Validate subscription limits BEFORE scheduling
        validateWorkoutPlanScheduling(user, request, planExercises.size());

        List<ScheduledWorkout> scheduledWorkouts = new ArrayList<>();

        // Determine which exercises to schedule (respect FREE user limits)
        List<PlanExercise> exercisesToSchedule = getExercisesToSchedule(planExercises, request, user);

        log.info("📋 Scheduling {} out of {} exercises for {} user",
                exercisesToSchedule.size(), planExercises.size(), user.getSubscriptionTier());

        // Convert each plan exercise to a scheduled workout
        for (int i = 0; i < exercisesToSchedule.size(); i++) {
            PlanExercise planExercise = exercisesToSchedule.get(i);

            ScheduledWorkout scheduledWorkout = new ScheduledWorkout();
            scheduledWorkout.setUser(user);
            scheduledWorkout.setWorkoutPlan(createIndividualExercisePlan(planExercise));
            scheduledWorkout.setScheduledDate(request.getScheduledDate());
            scheduledWorkout.setCustomNotes(buildExerciseNotes(request.getNotes(), planExercise, i + 1));

            // Set timing based on auto-spacing preference
            if (request.getAutoSpaceWorkouts() != null && request.getAutoSpaceWorkouts()) {
                LocalDateTime scheduledTime = calculateScheduledTime(request, i, exercisesToSchedule.size());
                scheduledWorkout.setReminderTime(scheduledTime);
            } else if (request.getPreferredStartTime() != null) {
                LocalDateTime baseTime = request.getScheduledDate().atTime(request.getPreferredStartTime());
                scheduledWorkout.setReminderTime(baseTime.plusMinutes(i * (request.getSpacingMinutes() != null ? request.getSpacingMinutes() : 15)));
            }

            // Set estimated duration from plan exercise
            scheduledWorkout.setEstimatedDurationMinutes(calculateExerciseDuration(planExercise));

            // Set exercise configuration from plan exercise
            setExerciseConfigurationFromPlan(scheduledWorkout, planExercise);

            scheduledWorkouts.add(scheduledWorkout);
        }

        // Save all scheduled workouts
        List<ScheduledWorkout> saved = scheduledWorkoutRepository.saveAll(scheduledWorkouts);

        log.info("✅ Successfully scheduled {} exercises from workout plan for user {}",
                saved.size(), username);

        return saved.stream()
                .map(scheduledWorkoutMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Schedule a single workout (individual exercise or workout plan)
     */
    @Transactional
    public ScheduledWorkoutResponse scheduleWorkout(String username, ScheduledWorkoutRequest request) {
        User user = findUserByUsername(username);
        WorkoutPlan workoutPlan = findWorkoutPlanById(request.getWorkoutPlanId());

        // Validate scheduling constraints
        validateSchedulingRequest(user, request);

        // Check for conflicts
        checkForSchedulingConflicts(user, request.getScheduledDate(), null);

        // Create scheduled workout
        ScheduledWorkout scheduledWorkout = new ScheduledWorkout();
        scheduledWorkout.setUser(user);
        scheduledWorkout.setWorkoutPlan(workoutPlan);
        scheduledWorkout.setScheduledDate(request.getScheduledDate());
        scheduledWorkout.setCustomNotes(request.getCustomNotes());
        scheduledWorkout.setReminderTime(request.getReminderTime());

        // Set program context if provided
        if (request.getProgramId() != null) {
            WorkoutProgram program = findWorkoutProgramById(request.getProgramId());
            scheduledWorkout.setProgram(program);
            scheduledWorkout.setWeekNumber(request.getWeekNumber());
            scheduledWorkout.setDayOfWeek(request.getScheduledDate().getDayOfWeek().getValue());
        }

        ScheduledWorkout saved = scheduledWorkoutRepository.save(scheduledWorkout);

        log.info("Workout scheduled: {} for user {} on {}",
                workoutPlan.getWorkoutName(), username, request.getScheduledDate());

        return scheduledWorkoutMapper.toResponse(saved);
    }

    /**
     * Reschedule an existing workout to a new date
     */
    @Transactional
    public ScheduledWorkoutResponse rescheduleWorkout(String username, Long scheduledWorkoutId, LocalDate newDate) {
        ScheduledWorkout scheduledWorkout = findScheduledWorkoutById(scheduledWorkoutId);
        validateOwnership(scheduledWorkout, username);

        // Validate new date
        if (!scheduledWorkout.canSchedule(scheduledWorkout.getUser(), newDate)) {
            throw new SchedulingConstraintException("Cannot schedule workout that far in advance");
        }

        // Check for conflicts on new date
        checkForSchedulingConflicts(scheduledWorkout.getUser(), newDate, scheduledWorkoutId);

        // Reschedule
        scheduledWorkout.reschedule(newDate);
        ScheduledWorkout saved = scheduledWorkoutRepository.save(scheduledWorkout);

        log.info("Workout rescheduled: {} moved to {} for user {}",
                scheduledWorkoutId, newDate, username);

        return scheduledWorkoutMapper.toResponse(saved);
    }

    /**
     * Permanently delete a scheduled workout from the database
     */
    @Transactional
    public void permanentlyDeleteScheduledWorkout(String username, Long scheduledWorkoutId) {
        log.debug("Attempting to permanently delete scheduled workout {} for user {}", scheduledWorkoutId, username);

        ScheduledWorkout scheduledWorkout = findScheduledWorkoutById(scheduledWorkoutId);
        validateOwnership(scheduledWorkout, username);
        validateWorkoutCanBeDeleted(scheduledWorkout);

        try {
            handleRelatedRecordsBeforeDeletion(scheduledWorkout);
            scheduledWorkoutRepository.delete(scheduledWorkout);

            log.info("Successfully permanently deleted scheduled workout {} for user {}",
                    scheduledWorkoutId, username);

        } catch (Exception e) {
            log.error("Failed to delete scheduled workout {} for user {}: {}",
                    scheduledWorkoutId, username, e.getMessage());
            throw new RuntimeException("Failed to delete workout: " + e.getMessage());
        }
    }



    // =======================
    // CALENDAR VIEW & RETRIEVAL
    // =======================

    /**
     * Get calendar view with exercise configuration details
     */
    public CalendarViewResponse getCalendarView(String username, LocalDate startDate, LocalDate endDate) {
        User user = findUserByUsername(username);

        List<ScheduledWorkout> scheduledWorkouts = scheduledWorkoutRepository
                .findByUserAndScheduledDateBetweenOrderByScheduledDateAsc(user, startDate, endDate);

        Map<LocalDate, List<ScheduledWorkoutResponse>> calendarData = scheduledWorkouts.stream()
                .collect(Collectors.groupingBy(
                        ScheduledWorkout::getScheduledDate,
                        LinkedHashMap::new,
                        Collectors.mapping(scheduledWorkoutMapper::toResponse, Collectors.toList())
                ));

        return CalendarViewResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .workoutsByDate(calendarData)
                .totalScheduled(scheduledWorkouts.size())
                .build();
    }

    /**
     * Get today's scheduled workouts
     */
    public List<ScheduledWorkoutResponse> getTodaysWorkouts(String username) {
        User user = findUserByUsername(username);
        List<ScheduledWorkout> todaysWorkouts = scheduledWorkoutRepository.findTodaysWorkouts(user);
        return scheduledWorkoutMapper.toResponseList(todaysWorkouts);
    }

    /**
     * Get upcoming workouts for the next N days
     */
    public List<ScheduledWorkoutResponse> getUpcomingWorkouts(String username, int days) {
        User user = findUserByUsername(username);
        LocalDate endDate = LocalDate.now().plusDays(days);
        List<ScheduledWorkout> upcomingWorkouts = scheduledWorkoutRepository
                .findUpcomingWorkouts(user, endDate);
        return scheduledWorkoutMapper.toResponseList(upcomingWorkouts);
    }

    /**
     * Get overdue workouts (scheduled in the past but not completed)
     */
    public List<ScheduledWorkoutResponse> getOverdueWorkouts(String username) {
        User user = findUserByUsername(username);
        List<ScheduledWorkout> overdueWorkouts = scheduledWorkoutRepository.findOverdueWorkouts(user);
        return scheduledWorkoutMapper.toResponseList(overdueWorkouts);
    }

    // =======================
    // WORKOUT EXECUTION
    // =======================

    /**
     * Start a scheduled workout (sets status to IN_PROGRESS)
     */
    @Transactional
    public ScheduledWorkoutResponse startScheduledWorkout(String username, Long scheduledWorkoutId) {
        ScheduledWorkout scheduledWorkout = findScheduledWorkoutById(scheduledWorkoutId);
        validateOwnership(scheduledWorkout, username);

        // Check if user already has a workout in progress
        boolean hasInProgress = scheduledWorkoutRepository.existsByUserAndStatus(
                scheduledWorkout.getUser(), ScheduledWorkout.ScheduleStatus.IN_PROGRESS);

        if (hasInProgress) {
            throw new WorkoutInProgressException("Cannot start new workout while another is in progress");
        }

        scheduledWorkout.startWorkout();
        ScheduledWorkout saved = scheduledWorkoutRepository.save(scheduledWorkout);

        log.info("Workout started: {} for user {}", scheduledWorkoutId, username);

        return scheduledWorkoutMapper.toResponse(saved);
    }

    /**
     * Complete a scheduled workout with session data
     */
    @Transactional
    public ScheduledWorkoutResponse completeScheduledWorkout(String username, Long scheduledWorkoutId,
                                                             WorkoutSession workoutSession) {
        ScheduledWorkout scheduledWorkout = findScheduledWorkoutById(scheduledWorkoutId);
        validateOwnership(scheduledWorkout, username);

        // Validate workout session belongs to this scheduled workout
        if (!workoutSession.getWorkoutPlan().getId().equals(scheduledWorkout.getWorkoutPlan().getId())) {
            throw new IllegalArgumentException("Workout session does not match scheduled workout plan");
        }

        scheduledWorkout.completeWorkout(workoutSession);
        ScheduledWorkout saved = scheduledWorkoutRepository.save(scheduledWorkout);

        log.info("Workout completed: {} for user {}", scheduledWorkoutId, username);

        return scheduledWorkoutMapper.toResponse(saved);
    }

    // =======================
    // PROGRAM SCHEDULING
    // =======================

    /**
     * Schedule an entire workout program
     */
    @Transactional
    public List<ScheduledWorkoutResponse> scheduleProgram(String username, Long programId, LocalDate startDate) {
        User user = findUserByUsername(username);
        WorkoutProgram program = findWorkoutProgramById(programId);

        validateProgramScheduling(user, program, startDate);

        List<ScheduledWorkout> scheduledWorkouts = createProgramSchedule(user, program, startDate);
        List<ScheduledWorkout> savedWorkouts = scheduledWorkoutRepository.saveAll(scheduledWorkouts);

        log.info("Program scheduled: {} starting {} for user {}",
                program.getName(), startDate, username);

        return scheduledWorkoutMapper.toResponseList(savedWorkouts);
    }

    /**
     * Get scheduled workouts for a specific program
     */
    public List<ScheduledWorkoutResponse> getProgramSchedule(String username, Long programId) {
        User user = findUserByUsername(username);
        WorkoutProgram program = findWorkoutProgramById(programId);

        List<ScheduledWorkout> programWorkouts = scheduledWorkoutRepository
                .findByUserAndProgramOrderByWeekNumberAscDayOfWeekAsc(user, program);

        return scheduledWorkoutMapper.toResponseList(programWorkouts);
    }

    // =======================
    // ANALYTICS & STATISTICS
    // =======================

    /**
     * Get scheduling analytics for a date range
     */
    public Map<String, Object> getSchedulingAnalytics(String username, LocalDate startDate, LocalDate endDate) {
        User user = findUserByUsername(username);

        Long totalScheduled = scheduledWorkoutRepository
                .countScheduledWorkoutsInRange(user, startDate, endDate);

        Double completionRate = scheduledWorkoutRepository
                .calculateCompletionRate(user, startDate, endDate)
                .orElse(0.0);

        List<Object[]> frequencyByDay = scheduledWorkoutRepository
                .getWorkoutFrequencyByDayOfWeek(user);

        return Map.of(
                "totalScheduled", totalScheduled,
                "completionRate", completionRate,
                "frequencyByDayOfWeek", frequencyByDay,
                "period", Map.of("start", startDate, "end", endDate)
        );
    }

    // =======================
    // DATA RETENTION (FREE USER LIMITS)
    // =======================

    /**
     * Clean up old scheduled workouts for FREE users (30-day retention)
     */
    @Transactional
    public void cleanupOldScheduledWorkouts(String username) {
        User user = findUserByUsername(username);

        // Only cleanup for free users
        if (user.getSubscriptionTier() != SubscriptionTier.FREE) {
            return;
        }

        LocalDate cutoffDate = LocalDate.now().minusDays(30);
        List<ScheduledWorkout> oldWorkouts = scheduledWorkoutRepository
                .findOldWorkoutsForCleanup(user, cutoffDate);

        scheduledWorkoutRepository.deleteAll(oldWorkouts);

        log.info("Cleaned up {} old scheduled workouts for free user {}",
                oldWorkouts.size(), username);
    }

    // =======================
    // ✅ HELPER METHODS FOR EXERCISE CONFIGURATION
    // =======================

    /**
     * Set exercise configuration on scheduled workout from individual request
     */
    private void setExerciseConfiguration(ScheduledWorkout scheduledWorkout, Exercise exercise,
                                          IndividualExerciseRequest request) {
        if (exercise.getIsCardio()) {
            scheduledWorkout.setTargetDurationMinutes(request.getTargetDurationMinutes());
            scheduledWorkout.setTargetDistanceKm(request.getTargetDistanceKm());
            scheduledWorkout.setTargetPace(request.getTargetPace());
        } else if (exercise.getIsIsometric()) {
            scheduledWorkout.setTargetSets(request.getSets());
            scheduledWorkout.setHoldDurationSeconds(request.getHoldDurationSeconds());
            scheduledWorkout.setRestSeconds(60);
        } else {
            scheduledWorkout.setTargetSets(request.getSets());
            scheduledWorkout.setTargetReps(request.getReps());
            scheduledWorkout.setTargetWeight(request.getWeight());
            scheduledWorkout.setTargetWeightUnit("lbs");
            scheduledWorkout.setRestSeconds(request.getRestSeconds());
            scheduledWorkout.setTempo(request.getTempo());
            scheduledWorkout.setTargetRpe(request.getTargetRpe());
        }
    }

    /**
     * Set exercise configuration from plan exercise
     */
    private void setExerciseConfigurationFromPlan(ScheduledWorkout scheduledWorkout, PlanExercise planExercise) {
        Exercise exercise = planExercise.getExercise();

        if (exercise.getIsCardio()) {
            scheduledWorkout.setTargetDurationMinutes(planExercise.getPrescribedSets());
            scheduledWorkout.setTargetDistanceKm(planExercise.getPrescribedWeightPercent());
            scheduledWorkout.setTargetPace(planExercise.getPrescribedRpe() != null ? planExercise.getPrescribedRpe().doubleValue() : null);
        } else if (exercise.getIsIsometric()) {
            scheduledWorkout.setTargetSets(planExercise.getPrescribedSets());
            scheduledWorkout.setHoldDurationSeconds(planExercise.getPrescribedRestSeconds());
            scheduledWorkout.setRestSeconds(60);
        } else {
            scheduledWorkout.setTargetSets(planExercise.getPrescribedSets());
            scheduledWorkout.setTargetReps(planExercise.getPrescribedReps() != null ? planExercise.getPrescribedReps().toString() : null);
            scheduledWorkout.setTargetWeight(planExercise.getPrescribedWeightPercent());
            scheduledWorkout.setTargetWeightUnit("lbs");
            scheduledWorkout.setRestSeconds(planExercise.getPrescribedRestSeconds());
            scheduledWorkout.setTempo(planExercise.getPrescribedTempo());
            scheduledWorkout.setTargetRpe(planExercise.getPrescribedRpe() != null ? planExercise.getPrescribedRpe().intValue() : null);
        }
    }

    // =======================
    // ✅ NEW: INDIVIDUAL EXERCISE HELPERS
    // =======================

    /**
     * Create individual exercise plan from request
     */
    private WorkoutPlan createIndividualExercisePlan(Exercise exercise,
                                                     IndividualExerciseRequest request) {
        WorkoutPlan individualPlan = new WorkoutPlan();
        individualPlan.setWorkoutName(exercise.getExerciseName());
        individualPlan.setWorkoutDescription("Individual exercise from workout plan");
        individualPlan.setDifficultyLevel(DifficultyLevel.INTERMEDIATE);
        individualPlan.setEstimatedDurationMinutes(calculateIndividualExerciseDuration(exercise, request));

        // Set category based on exercise
        if (exercise.getTargetMuscleGroups() != null && !exercise.getTargetMuscleGroups().isEmpty()) {
            individualPlan.setWorkoutCategory(exercise.getTargetMuscleGroups().get(0));
        } else if (exercise.getExerciseType() != null) {
            individualPlan.setWorkoutCategory(exercise.getExerciseType().getDisplayName());
        } else {
            individualPlan.setWorkoutCategory("General");
        }

        individualPlan.setSubscriptionTierRequired("FREE");
        individualPlan.setIsPublic(false);

        // Save the plan first to get an ID
        WorkoutPlan savedPlan = workoutPlanRepository.save(individualPlan);

        // Create and save the PlanExercise to store configuration
        PlanExercise planExercise = new PlanExercise();
        planExercise.setWorkoutPlan(savedPlan);
        planExercise.setExercise(exercise);
        planExercise.setOrderInWorkout(1);

        // Set configuration based on exercise type and request
        if (exercise.getIsCardio()) {
            planExercise.setPrescribedSets(request.getTargetDurationMinutes());
            planExercise.setPrescribedWeightPercent(request.getTargetDistanceKm());
            planExercise.setPrescribedRpe(request.getTargetRpe() != null ? request.getTargetRpe().intValue() : null);
        } else if (exercise.getIsIsometric()) {
            planExercise.setPrescribedSets(request.getSets() != null ? request.getSets() : 3);
            planExercise.setPrescribedRestSeconds(request.getHoldDurationSeconds() != null ? request.getHoldDurationSeconds() : 30);
        } else {
            planExercise.setPrescribedSets(request.getSets() != null ? request.getSets() : 3);
            planExercise.setPrescribedReps(request.getReps() != null ? request.getReps() : "10");
            planExercise.setPrescribedWeightPercent(request.getWeight());
            planExercise.setPrescribedRestSeconds(request.getRestSeconds() != null ? request.getRestSeconds() : 90);
            planExercise.setPrescribedTempo(request.getTempo());
            planExercise.setPrescribedRpe(request.getTargetRpe() != null ? request.getTargetRpe().intValue() : null);
        }

        planExerciseRepository.save(planExercise);

        return savedPlan;
    }

    /**
     * Calculate duration for individual exercise based on type and configuration
     */
    private Integer calculateIndividualExerciseDuration(Exercise exercise,
                                                        IndividualExerciseRequest request) {
        if (exercise.getIsCardio()) {
            return request.getTargetDurationMinutes() != null ? request.getTargetDurationMinutes() : 30;
        } else if (exercise.getIsIsometric()) {
            Integer sets = request.getSets() != null ? request.getSets() : 3;
            Integer holdDuration = request.getHoldDurationSeconds() != null ? request.getHoldDurationSeconds() : 30;
            return sets * (holdDuration + 60) / 60;
        } else {
            Integer sets = request.getSets() != null ? request.getSets() : 3;
            Integer restSeconds = request.getRestSeconds() != null ? request.getRestSeconds() : 90;
            int workTime = sets * 45;
            int restTime = (sets - 1) * restSeconds;
            return (workTime + restTime) / 60;
        }
    }

    /**
     * Validate individual exercise scheduling with subscription limits
     */
    private void validateIndividualExerciseScheduling(User user,
                                                      IndividualExerciseRequest request) {
        if (request.getScheduledDate().isBefore(LocalDate.now())) {
            throw new SchedulingConstraintException("Cannot schedule exercises in the past");
        }

        if (!canScheduleForDate(user, request.getScheduledDate())) {
            long daysOut = ChronoUnit.DAYS.between(LocalDate.now(), request.getScheduledDate());
            throw new SubscriptionLimitExceededException(
                    String.format("Free tier limited to 7 days. Upgrade to schedule %d days out", daysOut));
        }

        long currentExercisesOnDate = scheduledWorkoutRepository.countByUserAndScheduledDate(user, request.getScheduledDate());
        int dailyLimit = getDailySchedulingLimit(user);

        if (currentExercisesOnDate >= dailyLimit) {
            if (user.getSubscriptionTier() == SubscriptionTier.FREE) {
                throw new SubscriptionLimitExceededException(String.format(
                        "FREE users limited to %d exercises per day. Upgrade to PLUS for unlimited scheduling!",
                        dailyLimit));
            } else {
                throw new SchedulingConstraintException("Daily scheduling limit reached");
            }
        }
    }

    // =======================
    // ✅ STATISTICS CALCULATION HELPERS
    // =======================

    private int calculateCurrentStreak(User user) {
        LocalDate today = LocalDate.now();
        int streak = 0;
        LocalDate checkDate = today;

        while (true) {
            List<ScheduledWorkout> dayWorkouts = scheduledWorkoutRepository
                    .findByUserAndScheduledDateOrderByCreatedAtAsc(user, checkDate);

            boolean hasCompletedWorkout = dayWorkouts.stream()
                    .anyMatch(sw -> sw.getStatus() == ScheduledWorkout.ScheduleStatus.COMPLETED);

            if (hasCompletedWorkout) {
                streak++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }

            if (streak >= 365) break;
        }

        return streak;
    }

    private int calculateLongestStreak(User user) {
        List<ScheduledWorkout> allCompletedWorkouts = scheduledWorkoutRepository
                .findByUserAndStatusOrderByScheduledDateAsc(user, ScheduledWorkout.ScheduleStatus.COMPLETED);

        if (allCompletedWorkouts.isEmpty()) return 0;

        Set<LocalDate> completedDates = allCompletedWorkouts.stream()
                .map(ScheduledWorkout::getScheduledDate)
                .collect(Collectors.toSet());

        int longestStreak = 0;
        int currentStreak = 0;
        LocalDate earliestDate = allCompletedWorkouts.get(0).getScheduledDate();
        LocalDate latestDate = allCompletedWorkouts.get(allCompletedWorkouts.size() - 1).getScheduledDate();

        for (LocalDate date = earliestDate; !date.isAfter(latestDate); date = date.plusDays(1)) {
            if (completedDates.contains(date)) {
                currentStreak++;
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
        }

        return longestStreak;
    }

    private LocalDate findLastWorkoutDate(User user) {
        List<ScheduledWorkout> completedWorkouts = scheduledWorkoutRepository
                .findByUserAndStatusOrderByScheduledDateAsc(user, ScheduledWorkout.ScheduleStatus.COMPLETED);

        return completedWorkouts.isEmpty() ? null :
                completedWorkouts.get(completedWorkouts.size() - 1).getScheduledDate();
    }

    private String findLastWorkoutType(User user) {
        List<ScheduledWorkout> completedWorkouts = scheduledWorkoutRepository
                .findByUserAndStatusOrderByScheduledDateAsc(user, ScheduledWorkout.ScheduleStatus.COMPLETED);

        if (completedWorkouts.isEmpty()) return null;

        ScheduledWorkout lastWorkout = completedWorkouts.get(completedWorkouts.size() - 1);
        WorkoutPlan plan = lastWorkout.getWorkoutPlan();
        return plan != null ? plan.getWorkoutCategory() : "Unknown";
    }

    private String findFavoriteExerciseType(User user) {
        List<ScheduledWorkout> completedWorkouts = scheduledWorkoutRepository
                .findByUserAndStatusOrderByScheduledDateAsc(user, ScheduledWorkout.ScheduleStatus.COMPLETED);

        Map<String, Long> typeFrequency = completedWorkouts.stream()
                .filter(sw -> sw.getWorkoutPlan() != null && sw.getWorkoutPlan().getWorkoutCategory() != null)
                .collect(Collectors.groupingBy(
                        sw -> sw.getWorkoutPlan().getWorkoutCategory(),
                        Collectors.counting()
                ));

        return typeFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Mixed");
    }

    // =======================
    // ✅ EXISTING: WORKOUT PLAN SCHEDULING HELPERS
    // =======================

    private void validateWorkoutPlanScheduling(User user, ScheduleMultipleExercisesRequestDTO request, int totalExercises) {
        if (request.getScheduledDate().isBefore(LocalDate.now())) {
            throw new SchedulingConstraintException("Cannot schedule workouts in the past");
        }

        if (!canScheduleForDate(user, request.getScheduledDate())) {
            long daysOut = ChronoUnit.DAYS.between(LocalDate.now(), request.getScheduledDate());
            throw new SubscriptionLimitExceededException(
                    String.format("Free tier limited to 7 days. Upgrade to schedule %d days out", daysOut));
        }

        long currentExercisesOnDate = scheduledWorkoutRepository.countByUserAndScheduledDate(user, request.getScheduledDate());
        int dailyLimit = getDailySchedulingLimit(user);

        if (currentExercisesOnDate + totalExercises > dailyLimit) {
            if (user.getSubscriptionTier() == SubscriptionTier.FREE) {
                throw new SubscriptionLimitExceededException(String.format(
                        "FREE users limited to %d exercises per day. This workout has %d exercises. " +
                                "Upgrade to PLUS to schedule complete workout plans!",
                        dailyLimit, totalExercises));
            } else {
                throw new SchedulingConstraintException("Daily scheduling limit reached");
            }
        }
    }

    private List<PlanExercise> getExercisesToSchedule(List<PlanExercise> allExercises,
                                                      ScheduleMultipleExercisesRequestDTO request,
                                                      User user) {
        if (request.getExerciseIdsToSchedule() != null && !request.getExerciseIdsToSchedule().isEmpty()) {
            return allExercises.stream()
                    .filter(pe -> request.getExerciseIdsToSchedule().contains(pe.getExercise().getId()))
                    .collect(Collectors.toList());
        }

        if (user.getSubscriptionTier() == SubscriptionTier.FREE) {
            long currentScheduledToday = scheduledWorkoutRepository.countByUserAndScheduledDate(user, request.getScheduledDate());
            int remainingLimit = Math.max(0, (int)(getDailySchedulingLimit(user) - currentScheduledToday));

            if (remainingLimit == 0) {
                throw new SubscriptionLimitExceededException("Daily exercise limit reached. Upgrade to PLUS for unlimited scheduling!");
            }

            return allExercises.stream()
                    .limit(remainingLimit)
                    .collect(Collectors.toList());
        }

        return allExercises;
    }

    private WorkoutPlan createIndividualExercisePlan(PlanExercise planExercise) {
        WorkoutPlan individualPlan = new WorkoutPlan();
        individualPlan.setWorkoutName(planExercise.getExercise().getExerciseName());
        individualPlan.setWorkoutDescription("Individual exercise from workout plan");
        individualPlan.setDifficultyLevel(DifficultyLevel.INTERMEDIATE);
        individualPlan.setEstimatedDurationMinutes(calculateExerciseDuration(planExercise));

        Exercise exercise = planExercise.getExercise();

        if (exercise.getTargetMuscleGroups() != null && !exercise.getTargetMuscleGroups().isEmpty()) {
            individualPlan.setWorkoutCategory(exercise.getTargetMuscleGroups().get(0));
        } else if (exercise.getExerciseType() != null) {
            individualPlan.setWorkoutCategory(exercise.getExerciseType().getDisplayName());
        } else {
            individualPlan.setWorkoutCategory("General");
        }

        individualPlan.setSubscriptionTierRequired("FREE");
        individualPlan.setIsPublic(false);

        return individualPlan;
    }

    private String buildExerciseNotes(String baseNotes, PlanExercise planExercise, int exerciseNumber) {
        StringBuilder notes = new StringBuilder();

        if (baseNotes != null && !baseNotes.trim().isEmpty()) {
            notes.append(baseNotes);
        }

        Exercise exercise = planExercise.getExercise();
        if (exercise.getIsCardio()) {
            notes.append(notes.length() > 0 ? " | " : "").append("Cardio: ");
            if (planExercise.getPrescribedSets() != null) {
                notes.append(planExercise.getPrescribedSets()).append(" min");
            }
        } else if (exercise.getIsIsometric()) {
            notes.append(notes.length() > 0 ? " | " : "").append("Isometric: ");
            if (planExercise.getPrescribedSets() != null) {
                notes.append(planExercise.getPrescribedSets()).append(" sets");
            }
        } else {
            if (planExercise.getPrescribedSets() != null && planExercise.getPrescribedReps() != null) {
                notes.append(notes.length() > 0 ? " | " : "")
                        .append(planExercise.getPrescribedSets()).append("x").append(planExercise.getPrescribedReps());
            }
        }

        return notes.toString();
    }

    private LocalDateTime calculateScheduledTime(ScheduleMultipleExercisesRequestDTO request, int exerciseIndex, int totalExercises) {
        LocalDateTime baseTime;

        if (request.getPreferredStartTime() != null) {
            baseTime = request.getScheduledDate().atTime(request.getPreferredStartTime());
        } else {
            baseTime = request.getScheduledDate().atTime(9, 0);
        }

        int spacingMinutes = request.getSpacingMinutes() != null ? request.getSpacingMinutes() :
                (totalExercises > 1 ? (8 * 60) / (totalExercises - 1) : 0);

        return baseTime.plusMinutes(exerciseIndex * spacingMinutes);
    }

    private Integer calculateExerciseDuration(PlanExercise planExercise) {
        Exercise exercise = planExercise.getExercise();

        if (exercise.getIsCardio()) {
            return planExercise.getPrescribedSets() != null ? planExercise.getPrescribedSets() : 30;
        } else if (exercise.getIsIsometric()) {
            Integer sets = planExercise.getPrescribedSets();
            Integer holdDuration = planExercise.getPrescribedRestSeconds();
            if (sets != null && holdDuration != null) {
                return sets * (holdDuration + 60) / 60;
            }
            return 10;
        } else {
            Integer sets = planExercise.getPrescribedSets();
            Integer restSeconds = planExercise.getPrescribedRestSeconds();
            if (sets != null) {
                int workTime = sets * 45;
                int restTime = restSeconds != null ? (sets - 1) * restSeconds : (sets - 1) * 90;
                return (workTime + restTime) / 60;
            }
            return 15;
        }
    }

    // =======================
    // SUBSCRIPTION ENFORCEMENT & VALIDATION
    // =======================

    private void validateSchedulingRequest(User user, ScheduledWorkoutRequest request) {
        if (request.getScheduledDate().isBefore(LocalDate.now())) {
            throw new SchedulingConstraintException("Cannot schedule workouts in the past");
        }

        if (!canScheduleForDate(user, request.getScheduledDate())) {
            long daysOut = ChronoUnit.DAYS.between(LocalDate.now(), request.getScheduledDate());
            throw new SubscriptionLimitExceededException(
                    String.format("Free tier limited to 7 days. Upgrade to schedule %d days out", daysOut));
        }

        List<ScheduledWorkout> existingOnDate = scheduledWorkoutRepository
                .findByUserAndScheduledDateOrderByCreatedAtAsc(user, request.getScheduledDate());

        if (existingOnDate.size() >= getDailySchedulingLimit(user)) {
            throw new SchedulingConstraintException("Daily scheduling limit reached");
        }
    }

    private boolean canScheduleForDate(User user, LocalDate date) {
        return new ScheduledWorkout().canSchedule(user, date);
    }

    private int getDailySchedulingLimit(User user) {
        return switch (user.getSubscriptionTier()) {
            case FREE -> 3;
            case PLUS -> 8;
            case PRO -> 15;
            default -> 3;
        };
    }

    // =======================
    // DELETION & CLEANUP VALIDATION
    // =======================

    private void validateWorkoutCanBeDeleted(ScheduledWorkout scheduledWorkout) {
        ScheduledWorkout.ScheduleStatus status = scheduledWorkout.getStatus();

        switch (status) {
            case SCHEDULED:
            case CANCELLED:
                log.debug("Allowing deletion of {} workout {}", status, scheduledWorkout.getId());
                break;
            case IN_PROGRESS:
                throw new WorkoutInProgressException(scheduledWorkout.getId());
            case COMPLETED:
                throw new InvalidWorkoutStateException("delete", "COMPLETED", "SCHEDULED or CANCELLED");
            default:
                throw new InvalidWorkoutStateException("delete", status.toString(), "SCHEDULED or CANCELLED");
        }
    }

    private void handleRelatedRecordsBeforeDeletion(ScheduledWorkout scheduledWorkout) {
        if (scheduledWorkout.getCompletedSession() != null) {
            log.debug("Scheduled workout {} has a completed session, handling cleanup",
                    scheduledWorkout.getId());
            scheduledWorkout.setCompletedSession(null);
            scheduledWorkoutRepository.save(scheduledWorkout);
        }
    }

    // =======================
    // CONFLICT DETECTION
    // =======================

    private void checkForSchedulingConflicts(User user, LocalDate date, Long excludeId) {
        Long excludeIdSafe = excludeId != null ? excludeId : -1L;

        List<ScheduledWorkout> conflicts = scheduledWorkoutRepository
                .findSchedulingConflicts(user, date, excludeIdSafe);

        if (!conflicts.isEmpty()) {
            log.warn("Scheduling conflict detected: user {} already has {} workout(s) on {}",
                    user.getUsername(), conflicts.size(), date);
        }
    }

    // =======================
    // PROGRAM SCHEDULING HELPERS
    // =======================

    private void validateProgramScheduling(User user, WorkoutProgram program, LocalDate startDate) {
        LocalDate programEndDate = startDate.plusWeeks(program.getDurationWeeks());

        if (!canScheduleForDate(user, programEndDate)) {
            throw new SubscriptionLimitExceededException(
                    "Cannot schedule full program - upgrade for longer scheduling horizon");
        }
    }

    private List<ScheduledWorkout> createProgramSchedule(User user, WorkoutProgram program, LocalDate startDate) {
        List<ScheduledWorkout> scheduledWorkouts = new ArrayList<>();

        for (int week = 1; week <= program.getDurationWeeks(); week++) {
            LocalDate weekStart = startDate.plusWeeks(week - 1);

            for (int dayOffset : Arrays.asList(0, 2, 4)) {
                LocalDate workoutDate = weekStart.plusDays(dayOffset);

                ScheduledWorkout scheduled = new ScheduledWorkout();
                scheduled.setUser(user);
                scheduled.setProgram(program);
                scheduled.setWeekNumber(week);
                scheduled.setDayOfWeek(workoutDate.getDayOfWeek().getValue());
                scheduled.setScheduledDate(workoutDate);

                scheduledWorkouts.add(scheduled);
            }
        }

        return scheduledWorkouts;
    }

    // =======================
    // ✅ MISSING METHODS FROM CALENDAR CONTROLLER
    // =======================

    /**
     * Schedule workout plan (alternative endpoint for calendar)
     */
    @Transactional
    public ScheduledWorkoutResponse scheduleWorkoutPlan(String username, ScheduledWorkoutRequest request) {
        // This method already exists above as scheduleWorkout() - just delegate to it
        return scheduleWorkout(username, request);
    }

    /**
     * Update scheduled exercise with String ID (for calendar controller compatibility)
     */
    @Transactional
    public ScheduledWorkoutResponse updateScheduledExercise(String username, String exerciseId,
                                                            IndividualExerciseRequest updates) {
        // Convert String ID to Long and delegate to existing method
        Long exerciseIdLong = Long.parseLong(exerciseId);
        return updateScheduledExercise(username, exerciseIdLong, updates);
    }

    /**
     * Delete scheduled exercise with String ID (for calendar controller compatibility)
     */
    @Transactional
    public void deleteScheduledExercise(String username, String exerciseId) {
        // Convert String ID to Long and delegate to existing method
        Long exerciseIdLong = Long.parseLong(exerciseId);
        deleteScheduledExercise(username, exerciseIdLong);
    }

    /**
     * Mark exercise completed with String ID (for calendar controller compatibility)
     */
    @Transactional
    public ScheduledWorkoutResponse markExerciseCompleted(String username, String exerciseId) {
        // Convert String ID to Long and delegate to existing method
        Long exerciseIdLong = Long.parseLong(exerciseId);
        return markExerciseCompleted(username, exerciseIdLong);
    }

    /**
     * Mark multiple exercises completed with String IDs (for calendar controller compatibility)
     */
    @Transactional
    public List<ScheduledWorkoutResponse> markMultipleExercisesCompletedStringIds(String username, List<String> exerciseIds){
        // Convert String IDs to Long and delegate to existing method
        List<Long> exerciseIdsLong = exerciseIds.stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());
        return markMultipleExercisesCompleted(username, exerciseIdsLong);
    };

    // =======================
    // HELPER METHODS - ENTITY LOOKUPS
    // =======================

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    private WorkoutPlan findWorkoutPlanById(Long id) {
        return workoutPlanRepository.findById(id)
                .orElseThrow(() -> new WorkoutPlanNotFoundException(id));
    }

    private Exercise findExerciseById(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercise not found: " + id));
    }

    private WorkoutProgram findWorkoutProgramById(Long id) {
        return workoutProgramRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workout program not found: " + id));
    }

    private ScheduledWorkout findScheduledWorkoutById(Long id) {
        return scheduledWorkoutRepository.findById(id)
                .orElseThrow(() -> new ScheduledWorkoutNotFoundException(id));
    }

    private void validateOwnership(ScheduledWorkout scheduledWorkout, String username) {
        if (!scheduledWorkout.getUser().getUsername().equals(username)) {
            throw new UnauthorizedScheduledWorkoutAccessException(
                    "User does not have access to this scheduled workout");
        }
    }

    /**
     * Creates a minimal temporary workout plan for a single exercise.
     * These plans are lightweight containers that hold just enough information
     * to satisfy the database structure while remaining invisible to users.
     */
    private WorkoutPlan createTemporaryWorkoutPlan(Exercise exercise, IndividualExerciseRequest request) {
        WorkoutPlan temporaryPlan = new WorkoutPlan();

        // Set the minimum required fields for database constraints
        temporaryPlan.setWorkoutName(exercise.getExerciseName() + " (Individual)");
        temporaryPlan.setWorkoutDescription("Individual exercise scheduled from calendar");

        // Map exercise difficulty to workout plan difficulty safely
        temporaryPlan.setDifficultyLevel(mapExerciseDifficultyToWorkoutDifficulty(exercise.getDifficultyLevel()));

        temporaryPlan.setEstimatedDurationMinutes(calculateEstimatedDuration(exercise, request));

        // Set category based on exercise characteristics
        temporaryPlan.setWorkoutCategory(determineWorkoutCategory(exercise));

        // Mark as accessible to all users since it's a single exercise
        temporaryPlan.setSubscriptionTierRequired("FREE");
        temporaryPlan.setIsPublic(false); // Not searchable in workout plan library

        // Set metadata to identify this as a system-generated temporary plan
        temporaryPlan.setCreatedByUserId(null); // System-generated
        temporaryPlan.setCreatedByProfessional(false);

        return temporaryPlan;
    }

    /**
     * Builds the core ScheduledWorkout entity with common fields.
     * This separates the basic scheduling logic from exercise-specific configuration.
     */
    private ScheduledWorkout buildScheduledWorkout(User user, WorkoutPlan workoutPlan,
                                                   IndividualExerciseRequest request) {
        ScheduledWorkout scheduledWorkout = new ScheduledWorkout();
        scheduledWorkout.setUser(user);
        scheduledWorkout.setWorkoutPlan(workoutPlan);
        scheduledWorkout.setScheduledDate(request.getScheduledDate());
        scheduledWorkout.setCustomNotes(request.getNotes());
        scheduledWorkout.setEstimatedDurationMinutes(workoutPlan.getEstimatedDurationMinutes());

        return scheduledWorkout;
    }

    /**
     * Applies exercise-specific configuration based on the exercise's tracking mode.
     * This method handles the complexity of different exercise types while providing
     * sensible defaults for missing values.
     */
    private void applyExerciseConfiguration(ScheduledWorkout scheduledWorkout, Exercise exercise,
                                            IndividualExerciseRequest request) {
        try {
            log.debug("🔧 Applying configuration for exercise: {} (cardio: {}, isometric: {})",
                    exercise.getExerciseName(), exercise.getIsCardio(), exercise.getIsIsometric());

            if (exercise.getIsCardio() != null && exercise.getIsCardio()) {
                configureCardioExercise(scheduledWorkout, request);
            } else if (exercise.getIsIsometric() != null && exercise.getIsIsometric()) {
                configureIsometricExercise(scheduledWorkout, request);
            } else {
                configureStrengthExercise(scheduledWorkout, request);
            }

        } catch (Exception e) {
            log.warn("⚠️ Error applying exercise configuration, using defaults: {}", e.getMessage());
            // Set safe defaults to prevent scheduling failure
            applySafeDefaults(scheduledWorkout);
        }
    }

    private void configureCardioExercise(ScheduledWorkout scheduledWorkout, IndividualExerciseRequest request) {
        scheduledWorkout.setTargetDurationMinutes(
                request.getTargetDurationMinutes() != null ? request.getTargetDurationMinutes() : 20);
        scheduledWorkout.setTargetDistanceKm(request.getTargetDistanceKm());
        scheduledWorkout.setTargetPace(request.getTargetPace());

        log.debug("✅ Configured cardio: duration={}min, distance={}km, pace={}",
                scheduledWorkout.getTargetDurationMinutes(),
                scheduledWorkout.getTargetDistanceKm(),
                scheduledWorkout.getTargetPace());
    }

    private void configureIsometricExercise(ScheduledWorkout scheduledWorkout, IndividualExerciseRequest request) {
        scheduledWorkout.setTargetSets(request.getSets() != null ? request.getSets() : 3);
        scheduledWorkout.setHoldDurationSeconds(
                request.getHoldDurationSeconds() != null ? request.getHoldDurationSeconds() : 30);
        scheduledWorkout.setRestSeconds(request.getRestSeconds() != null ? request.getRestSeconds() : 60);

        log.debug("✅ Configured isometric: sets={}, hold={}s, rest={}s",
                scheduledWorkout.getTargetSets(),
                scheduledWorkout.getHoldDurationSeconds(),
                scheduledWorkout.getRestSeconds());
    }

    private void configureStrengthExercise(ScheduledWorkout scheduledWorkout, IndividualExerciseRequest request) {
        scheduledWorkout.setTargetSets(request.getSets() != null ? request.getSets() : 3);
        scheduledWorkout.setTargetReps(request.getReps() != null ? request.getReps() : "10");
        scheduledWorkout.setTargetWeight(request.getWeight());
        scheduledWorkout.setTargetWeightUnit("lbs"); // Default for American users
        scheduledWorkout.setRestSeconds(request.getRestSeconds() != null ? request.getRestSeconds() : 90);
        scheduledWorkout.setTempo(request.getTempo());
        scheduledWorkout.setTargetRpe(request.getTargetRpe());

        log.debug("✅ Configured strength: sets={}, reps={}, weight={}lbs, rest={}s",
                scheduledWorkout.getTargetSets(),
                scheduledWorkout.getTargetReps(),
                scheduledWorkout.getTargetWeight(),
                scheduledWorkout.getRestSeconds());
    }

    /**
     * Maps exercise difficulty levels to workout plan difficulty levels.
     * This handles cases where the enums might not match exactly.
     */
    private WorkoutPlan.DifficultyLevel mapExerciseDifficultyToWorkoutDifficulty(Exercise.DifficultyLevel exerciseDifficulty) {
        if (exerciseDifficulty == null) {
            return WorkoutPlan.DifficultyLevel.INTERMEDIATE;
        }

        switch (exerciseDifficulty) {
            case BEGINNER:
                return WorkoutPlan.DifficultyLevel.BEGINNER;
            case INTERMEDIATE:
                return WorkoutPlan.DifficultyLevel.INTERMEDIATE;
            case ADVANCED:
                return WorkoutPlan.DifficultyLevel.ADVANCED;
            default:
                return WorkoutPlan.DifficultyLevel.INTERMEDIATE;
        }
    }

    /**
     * Calculates estimated duration based on exercise type and configuration.
     */
    private Integer calculateEstimatedDuration(Exercise exercise, IndividualExerciseRequest request) {
        if (exercise.getIsCardio() != null && exercise.getIsCardio()) {
            return request.getTargetDurationMinutes() != null ?
                    request.getTargetDurationMinutes() :
                    (exercise.getEstimatedDurationMinutes() != null ? exercise.getEstimatedDurationMinutes() : 20);
        } else if (exercise.getIsIsometric() != null && exercise.getIsIsometric()) {
            Integer sets = request.getSets() != null ? request.getSets() : 3;
            Integer holdDuration = request.getHoldDurationSeconds() != null ? request.getHoldDurationSeconds() : 30;
            Integer rest = request.getRestSeconds() != null ? request.getRestSeconds() : 60;

            // Calculate total time: (hold time * sets) + (rest time * (sets - 1))
            int totalSeconds = (holdDuration * sets) + (rest * (sets - 1));
            return Math.max(1, totalSeconds / 60);
        } else {
            // Strength exercise: estimate based on sets and rest
            Integer sets = request.getSets() != null ? request.getSets() : 3;
            Integer restSeconds = request.getRestSeconds() != null ? request.getRestSeconds() : 90;

            int workTime = sets * 45; // Assume 45 seconds per set
            int restTime = (sets - 1) * restSeconds;
            return Math.max(1, (workTime + restTime) / 60);
        }
    }

    /**
     * Determines appropriate workout category based on exercise characteristics.
     */
    private String determineWorkoutCategory(Exercise exercise) {
        if (exercise.getTargetMuscleGroups() != null && !exercise.getTargetMuscleGroups().isEmpty()) {
            return exercise.getTargetMuscleGroups().get(0);
        } else if (exercise.getExerciseType() != null) {
            return exercise.getExerciseType().getDisplayName();
        } else {
            return "General";
        }
    }

    /**
     * Applies safe default values if configuration fails.
     */
    private void applySafeDefaults(ScheduledWorkout scheduledWorkout) {
        scheduledWorkout.setTargetSets(3);
        scheduledWorkout.setTargetReps("10");
        scheduledWorkout.setRestSeconds(90);
        scheduledWorkout.setTargetWeightUnit("lbs");
    }
}