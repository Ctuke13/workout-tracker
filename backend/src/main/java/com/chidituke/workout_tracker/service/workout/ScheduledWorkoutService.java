package com.chidituke.workout_tracker.service.workout;

import com.chidituke.workout_tracker.dto.request.scheduled_workouts.ScheduledWorkoutRequest;
import com.chidituke.workout_tracker.dto.response.scheduled_workouts.ScheduledWorkoutResponse;
import com.chidituke.workout_tracker.dto.response.scheduled_workouts.CalendarViewResponse;
import com.chidituke.workout_tracker.exceptions.scheduled_workout.*;
import com.chidituke.workout_tracker.exceptions.user.UserNotFoundException;
import com.chidituke.workout_tracker.exceptions.workout_plan.WorkoutPlanNotFoundException;
import com.chidituke.workout_tracker.mapper.workout.ScheduledWorkoutMapper;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.user.enums.SubscriptionTier;
import com.chidituke.workout_tracker.model.workout.ScheduledWorkout;
import com.chidituke.workout_tracker.model.workout.WorkoutPlan;
import com.chidituke.workout_tracker.model.workout.WorkoutProgram;
import com.chidituke.workout_tracker.model.workout.WorkoutSession;
import com.chidituke.workout_tracker.model.workout.PlanExercise;
import com.chidituke.workout_tracker.model.workout.Exercise;
import com.chidituke.workout_tracker.repository.workout.PlanExerciseRepository;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import com.chidituke.workout_tracker.repository.workout.ScheduledWorkoutRepository;
import com.chidituke.workout_tracker.repository.workout.WorkoutPlanRepository;
import com.chidituke.workout_tracker.repository.workout.WorkoutProgramRepository;
import com.chidituke.workout_tracker.repository.workout.WorkoutSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduledWorkoutService {

    private final ScheduledWorkoutRepository scheduledWorkoutRepository;
    private final UserRepository userRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final WorkoutProgramRepository workoutProgramRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final PlanExerciseRepository planExerciseRepository;
    private final ScheduledWorkoutMapper scheduledWorkoutMapper;

    // =======================
    // CALENDAR SCHEDULING
    // =======================

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

    @Transactional
    public ScheduledWorkoutResponse rescheduleWorkout(String username, Long scheduledWorkoutId,
                                                      LocalDate newDate) {
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
     * Validate that a workout can be deleted - uses existing exceptions
     */
    private void validateWorkoutCanBeDeleted(ScheduledWorkout scheduledWorkout) {
        ScheduledWorkout.ScheduleStatus status = scheduledWorkout.getStatus();

        switch (status) {
            case SCHEDULED:
            case CANCELLED:
                // ✅ Allow deletion
                log.debug("Allowing deletion of {} workout {}", status, scheduledWorkout.getId());
                break;

            case IN_PROGRESS:
                // ❌ Use existing WorkoutInProgressException
                throw new WorkoutInProgressException(scheduledWorkout.getId());

            case COMPLETED:
                // ❌ Use existing InvalidWorkoutStateException with detailed context
                throw new InvalidWorkoutStateException(
                        "delete",
                        "COMPLETED",
                        "SCHEDULED or CANCELLED"
                );

            default:
                // ❌ Use InvalidWorkoutStateException for unknown states
                throw new InvalidWorkoutStateException(
                        "delete",
                        status.toString(),
                        "SCHEDULED or CANCELLED"
                );
        }
    }

    /**
     * Permanently delete a scheduled workout from the database
     */
    @Transactional
    public void permanentlyDeleteScheduledWorkout(String username, Long scheduledWorkoutId) {
        log.debug("Attempting to permanently delete scheduled workout {} for user {}", scheduledWorkoutId, username);

        // Find the scheduled workout (uses your existing method)
        ScheduledWorkout scheduledWorkout = findScheduledWorkoutById(scheduledWorkoutId);

        // Verify ownership (uses your existing method)
        validateOwnership(scheduledWorkout, username);

        try {
            // Handle any related records before deletion
            handleRelatedRecordsBeforeDeletion(scheduledWorkout);

            // Actually delete the record from database
            scheduledWorkoutRepository.delete(scheduledWorkout);

            log.info("Successfully permanently deleted scheduled workout {} for user {}",
                    scheduledWorkoutId, username);

        } catch (Exception e) {
            log.error("Failed to delete scheduled workout {} for user {}: {}",
                    scheduledWorkoutId, username, e.getMessage());
            throw new RuntimeException("Failed to delete workout: " + e.getMessage());
        }
    }

    /**
     * Handle any related records that need to be cleaned up before deletion
     */
    private void handleRelatedRecordsBeforeDeletion(ScheduledWorkout scheduledWorkout) {
        // Check if there's a completed workout session
        if (scheduledWorkout.getCompletedSession() != null) {
            log.debug("Scheduled workout {} has a completed session, handling cleanup",
                    scheduledWorkout.getId());

            // Remove the connection but keep the session record
            scheduledWorkout.setCompletedSession(null);
            scheduledWorkoutRepository.save(scheduledWorkout);
        }

        // Handle any other related records if they exist
        // Example: Clean up reminders, notifications, etc.
    }

    // =======================
    // CALENDAR VIEW & RETRIEVAL
    // =======================

    public CalendarViewResponse getCalendarView(String username, LocalDate startDate, LocalDate endDate) {
        User user = findUserByUsername(username);

        List<ScheduledWorkout> scheduledWorkouts = scheduledWorkoutRepository
                .findByUserAndScheduledDateBetweenOrderByScheduledDateAsc(user, startDate, endDate);

        // ✅ FIXED: Use configuration-aware conversion instead of basic mapper
        Map<LocalDate, List<ScheduledWorkoutResponse>> calendarData = scheduledWorkouts.stream()
                .collect(Collectors.groupingBy(
                        ScheduledWorkout::getScheduledDate,
                        LinkedHashMap::new,
                        Collectors.mapping(this::convertToResponseWithConfiguration, Collectors.toList())
                ));

        return CalendarViewResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .workoutsByDate(calendarData)
                .totalScheduled(scheduledWorkouts.size())
                .build();
    }

    public List<ScheduledWorkoutResponse> getTodaysWorkouts(String username) {
        User user = findUserByUsername(username);
        List<ScheduledWorkout> todaysWorkouts = scheduledWorkoutRepository.findTodaysWorkouts(user);
        return scheduledWorkoutMapper.toResponseList(todaysWorkouts);
    }

    public List<ScheduledWorkoutResponse> getUpcomingWorkouts(String username, int days) {
        User user = findUserByUsername(username);
        LocalDate endDate = LocalDate.now().plusDays(days);
        List<ScheduledWorkout> upcomingWorkouts = scheduledWorkoutRepository
                .findUpcomingWorkouts(user, endDate);
        return scheduledWorkoutMapper.toResponseList(upcomingWorkouts);
    }

    public List<ScheduledWorkoutResponse> getOverdueWorkouts(String username) {
        User user = findUserByUsername(username);
        List<ScheduledWorkout> overdueWorkouts = scheduledWorkoutRepository.findOverdueWorkouts(user);
        return scheduledWorkoutMapper.toResponseList(overdueWorkouts);
    }

    private ScheduledWorkoutResponse convertToResponseWithConfiguration(ScheduledWorkout scheduledWorkout) {
        WorkoutPlan plan = scheduledWorkout.getWorkoutPlan();

        // Create WorkoutPlanInfo nested object
        ScheduledWorkoutResponse.WorkoutPlanInfo workoutPlanInfo = ScheduledWorkoutResponse.WorkoutPlanInfo.builder()
                .id(plan.getId())
                .name(plan.getWorkoutName())
                .description(plan.getWorkoutDescription())
                .difficulty(plan.getDifficultyLevel().name())
                .estimatedDurationMinutes(plan.getEstimatedDurationMinutes())
                .exerciseCount(getExerciseCount(plan))
                .category(plan.getWorkoutCategory())
                .imageUrl(plan.getWorkoutImageUrl())
                .isPublic(plan.getIsPublic())
                .build();

        // Create UserInfo nested object
        ScheduledWorkoutResponse.UserInfo userInfo = ScheduledWorkoutResponse.UserInfo.builder()
                .id(scheduledWorkout.getUser().getId())
                .username(scheduledWorkout.getUser().getUsername())
                .email(scheduledWorkout.getUser().getEmail())
                .firstName(scheduledWorkout.getUser().getFirstName())
                .lastName(scheduledWorkout.getUser().getLastName())
                .subscriptionTier(scheduledWorkout.getUser().getSubscriptionTier().name())
                .build();

        // Create WorkoutSessionInfo if completed session exists
        ScheduledWorkoutResponse.WorkoutSessionInfo sessionInfo = null;
        if (scheduledWorkout.getCompletedSession() != null) {
            WorkoutSession session = scheduledWorkout.getCompletedSession();
            sessionInfo = ScheduledWorkoutResponse.WorkoutSessionInfo.builder()
                    .id(session.getId())
                    .startTime(session.getCreatedAt())
                    .endTime(session.getUpdatedAt())
                    .actualDurationMinutes(session.getTotalDurationMinutes())
                    .notes(null)
                    .completed(true)
                    .build();
        }

        // Create base response builder
        ScheduledWorkoutResponse.ScheduledWorkoutResponseBuilder responseBuilder = ScheduledWorkoutResponse.builder()
                .id(scheduledWorkout.getId())
                .scheduledDate(scheduledWorkout.getScheduledDate())
                .status(scheduledWorkout.getStatus().name())
                .completedAt(scheduledWorkout.getCompletedAt())
                .estimatedDurationMinutes(scheduledWorkout.getEstimatedDurationMinutes())
                .customNotes(scheduledWorkout.getCustomNotes())
                .createdAt(scheduledWorkout.getCreatedAt())
                .updatedAt(scheduledWorkout.getUpdatedAt())
                .workoutPlan(workoutPlanInfo)
                .user(userInfo)
                .completedSession(sessionInfo)
                .weekNumber(scheduledWorkout.getWeekNumber())
                .dayOfWeek(scheduledWorkout.getDayOfWeek())
                .reminderTime(scheduledWorkout.getReminderTime())
                .createdByUserId(scheduledWorkout.getCreatedByUserId());

        // ✅ CRITICAL: Extract configuration from the first PlanExercise (for individual exercises)
        List<PlanExercise> planExercises = planExerciseRepository.findByWorkoutPlanOrderByOrderInWorkout(plan);
        if (!planExercises.isEmpty()) {
            PlanExercise planExercise = planExercises.get(0); // Single exercise for individual scheduling
            Exercise exercise = planExercise.getExercise();

            log.debug("🔍 Extracting configuration for exercise: {} (isCardio: {}, isIsometric: {})",
                    exercise.getExerciseName(), exercise.getIsCardio(), exercise.getIsIsometric());

            // Extract configuration based on exercise type
            if (exercise.getIsCardio()) {
                // Extract cardio configuration
                responseBuilder
                        .targetDurationMinutes(planExercise.getPrescribedSets()) // Duration stored in sets field
                        .targetDistanceKm(planExercise.getPrescribedWeightPercent()) // Distance stored in weight field
                        .targetPace(planExercise.getPrescribedRpe() != null ? planExercise.getPrescribedRpe().doubleValue() : null); // Pace stored in RPE field

                log.debug("✅ Extracted cardio config: duration={}min, distance={}km, pace={}",
                        planExercise.getPrescribedSets(), planExercise.getPrescribedWeightPercent(), planExercise.getPrescribedRpe());

            } else if (exercise.getIsIsometric()) {
                // Extract isometric configuration
                responseBuilder
                        .sets(planExercise.getPrescribedSets()) // Number of holds
                        .holdDurationSeconds(planExercise.getPrescribedRestSeconds()) // Hold duration stored in rest seconds
                        .restSeconds(60); // Default rest between holds

                log.debug("✅ Extracted isometric config: sets={}, holdDuration={}s",
                        planExercise.getPrescribedSets(), planExercise.getPrescribedRestSeconds());

            } else {
                // Extract strength configuration
                responseBuilder
                        .sets(planExercise.getPrescribedSets())
                        .reps(planExercise.getPrescribedReps())
                        .weight(planExercise.getPrescribedWeightPercent()) // Weight stored as percentage
                        .restSeconds(planExercise.getPrescribedRestSeconds())
                        .tempo(planExercise.getPrescribedTempo())
                        .targetRpe(planExercise.getPrescribedRpe());

                log.debug("✅ Extracted strength config: sets={}, reps={}, weight={}kg, rest={}s, rpe={}",
                        planExercise.getPrescribedSets(), planExercise.getPrescribedReps(),
                        planExercise.getPrescribedWeightPercent(), planExercise.getPrescribedRestSeconds(),
                        planExercise.getPrescribedRpe());
            }
        } else {
            log.warn("⚠️ No PlanExercise found for WorkoutPlan {}, using defaults", plan.getId());
        }

        return responseBuilder.build();
    }

    private Integer getExerciseCount(WorkoutPlan plan) {
        return (int) planExerciseRepository.countByWorkoutPlan(plan);
    }

    // =======================
    // WORKOUT EXECUTION
    // =======================

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

    @Transactional
    public List<ScheduledWorkoutResponse> scheduleProgram(String username, Long programId,
                                                          LocalDate startDate) {
        User user = findUserByUsername(username);
        WorkoutProgram program = findWorkoutProgramById(programId);

        // Validate user can schedule program
        validateProgramScheduling(user, program, startDate);

        // Get program workouts (this would require a ProgramWorkout junction table)
        // For now, we'll create a placeholder implementation
        List<ScheduledWorkout> scheduledWorkouts = createProgramSchedule(user, program, startDate);

        List<ScheduledWorkout> savedWorkouts = scheduledWorkoutRepository.saveAll(scheduledWorkouts);

        log.info("Program scheduled: {} starting {} for user {}",
                program.getName(), startDate, username);

        return scheduledWorkoutMapper.toResponseList(savedWorkouts);
    }

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

    public Map<String, Object> getSchedulingAnalytics(String username, LocalDate startDate,
                                                      LocalDate endDate) {
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
    // SUBSCRIPTION ENFORCEMENT
    // =======================

    private void validateSchedulingRequest(User user, ScheduledWorkoutRequest request) {
        // Check date is not in the past
        if (request.getScheduledDate().isBefore(LocalDate.now())) {
            throw new SchedulingConstraintException("Cannot schedule workouts in the past");
        }

        // Check subscription limits
        if (!canScheduleForDate(user, request.getScheduledDate())) {
            long daysOut = ChronoUnit.DAYS.between(LocalDate.now(), request.getScheduledDate());
            throw new SubscriptionLimitException(
                    String.format("Free tier limited to 7 days. Upgrade to schedule %d days out", daysOut));
        }

        // Check daily scheduling limit (prevent spam)
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
            case FREE -> 3;     // 2 workouts per day max
            case PLUS -> 4;     // 4 workouts per day max
            case PRO -> 10;     // 10 workouts per day max
            default -> 3;
        };
    }

    // =======================
    // CONFLICT DETECTION
    // =======================

    private void checkForSchedulingConflicts(User user, LocalDate date, Long excludeId) {
        Long excludeIdSafe = excludeId != null ? excludeId : -1L;

        List<ScheduledWorkout> conflicts = scheduledWorkoutRepository
                .findSchedulingConflicts(user, date, excludeIdSafe);

        // For now, just log conflicts but allow multiple workouts per day
        if (!conflicts.isEmpty()) {
            log.warn("Scheduling conflict detected: user {} already has {} workout(s) on {}",
                    user.getUsername(), conflicts.size(), date);
        }
    }

    // =======================
    // PROGRAM SCHEDULING HELPERS
    // =======================

    private void validateProgramScheduling(User user, WorkoutProgram program, LocalDate startDate) {
        // Check if user can schedule that far ahead for full program
        LocalDate programEndDate = startDate.plusWeeks(program.getDurationWeeks());

        if (!canScheduleForDate(user, programEndDate)) {
            throw new SubscriptionLimitException(
                    "Cannot schedule full program - upgrade for longer scheduling horizon");
        }
    }

    private List<ScheduledWorkout> createProgramSchedule(User user, WorkoutProgram program,
                                                         LocalDate startDate) {
        // This is a placeholder implementation
        // In reality, you'd need ProgramWorkout junction table to define the actual schedule
        List<ScheduledWorkout> scheduledWorkouts = new ArrayList<>();

        // Create a sample 3-day per week schedule
        for (int week = 1; week <= program.getDurationWeeks(); week++) {
            LocalDate weekStart = startDate.plusWeeks(week - 1);

            // Schedule Mon, Wed, Fri
            for (int dayOffset : Arrays.asList(0, 2, 4)) { // Mon=0, Wed=2, Fri=4
                LocalDate workoutDate = weekStart.plusDays(dayOffset);

                ScheduledWorkout scheduled = new ScheduledWorkout();
                scheduled.setUser(user);
                scheduled.setProgram(program);
                scheduled.setWeekNumber(week);
                scheduled.setDayOfWeek(workoutDate.getDayOfWeek().getValue());
                scheduled.setScheduledDate(workoutDate);
                // Would set workout plan from ProgramWorkout junction table

                scheduledWorkouts.add(scheduled);
            }
        }

        return scheduledWorkouts;
    }

    // =======================
    // HELPER METHODS
    // =======================

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    private WorkoutPlan findWorkoutPlanById(Long id) {
        return workoutPlanRepository.findById(id)
                .orElseThrow(() -> new WorkoutPlanNotFoundException(id));
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
}