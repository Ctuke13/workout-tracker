package com.chidituke.workout_tracker.service;

import com.chidituke.workout_tracker.dto.request.workout_session.WorkoutSessionRequest;
import com.chidituke.workout_tracker.dto.response.workout_session.WorkoutSessionResponse;
import com.chidituke.workout_tracker.dto.response.workout_session.WorkoutSessionAnalyticsResponse;
import com.chidituke.workout_tracker.exceptions.user.UserNotFoundException;
import com.chidituke.workout_tracker.exceptions.workout_plan.WorkoutPlanNotFoundException;
import com.chidituke.workout_tracker.exceptions.workout_session.WorkoutSessionNotFoundException;
import com.chidituke.workout_tracker.exceptions.scheduled_workout.ScheduledWorkoutNotFoundException;
import com.chidituke.workout_tracker.mapper.WorkoutSessionMapper;
import com.chidituke.workout_tracker.model.*;
import com.chidituke.workout_tracker.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutSessionService {

    private final WorkoutSessionRepository workoutSessionRepository;
    private final UserRepository userRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final WorkoutProgramRepository workoutProgramRepository;
    private final ScheduledWorkoutRepository scheduledWorkoutRepository;
    private final WorkoutSessionMapper workoutSessionMapper;

    // =======================
    // WORKOUT SESSION CRUD
    // =======================

    @Transactional
    public WorkoutSessionResponse createWorkoutSession(String username, WorkoutSessionRequest request) {
        User user = findUserByUsername(username);
        WorkoutPlan workoutPlan = findWorkoutPlanById(request.getWorkoutPlanId());

        WorkoutSession workoutSession = new WorkoutSession();
        workoutSession.setUser(user);
        workoutSession.setWorkoutPlan(workoutPlan);
        workoutSession.setDate(request.getDate() != null ? request.getDate() : LocalDate.now());

        // Map request fields
        workoutSessionMapper.mapRequestToEntity(request, workoutSession);

        // Handle scheduled workout completion if provided
        if (request.getScheduledWorkoutId() != null) {
            ScheduledWorkout scheduledWorkout = findScheduledWorkoutById(request.getScheduledWorkoutId());
            validateScheduledWorkoutOwnership(scheduledWorkout, user);

            // Link the session to scheduled workout
            workoutSession.setScheduledWorkout(scheduledWorkout);

            // Mark scheduled workout as completed
            scheduledWorkout.completeWorkout(workoutSession);
        }

        // Handle program context
        if (request.getProgramId() != null) {
            WorkoutProgram program = findWorkoutProgramById(request.getProgramId());
            workoutSession.setProgram(program);
            workoutSession.setWeekNumber(request.getWeekNumber());
        }

        WorkoutSession saved = workoutSessionRepository.save(workoutSession);

        log.info("Workout session created: {} for user {} on {}",
                workoutPlan.getWorkoutName(), username, workoutSession.getDate());

        return workoutSessionMapper.toResponse(saved);
    }

    @Transactional
    public WorkoutSessionResponse updateWorkoutSession(Long sessionId, String username,
                                                       WorkoutSessionRequest request) {
        WorkoutSession workoutSession = findWorkoutSessionById(sessionId);
        validateOwnership(workoutSession, username);

        // Update fields from request
        workoutSessionMapper.mapRequestToEntity(request, workoutSession);

        WorkoutSession saved = workoutSessionRepository.save(workoutSession);

        log.info("Workout session updated: {} for user {}", sessionId, username);

        return workoutSessionMapper.toResponse(saved);
    }

    @Transactional
    public void deleteWorkoutSession(Long sessionId, String username) {
        WorkoutSession workoutSession = findWorkoutSessionById(sessionId);
        validateOwnership(workoutSession, username);

        // If this session was linked to a scheduled workout, update the scheduled workout status
        if (workoutSession.getScheduledWorkout() != null) {
            ScheduledWorkout scheduledWorkout = workoutSession.getScheduledWorkout();
            scheduledWorkout.setStatus(ScheduledWorkout.ScheduleStatus.SCHEDULED);
            scheduledWorkout.setCompletedAt(null);
            scheduledWorkout.setCompletedSession(null);
            scheduledWorkoutRepository.save(scheduledWorkout);
        }

        workoutSessionRepository.delete(workoutSession);

        log.info("Workout session deleted: {} for user {}", sessionId, username);
    }

    // =======================
    // WORKOUT SESSION RETRIEVAL
    // =======================

    public List<WorkoutSessionResponse> getUserWorkoutHistory(String username) {
        User user = findUserByUsername(username);
        List<WorkoutSession> sessions = workoutSessionRepository.findByUserOrderByDateDesc(user);
        return workoutSessionMapper.toResponseList(sessions);
    }

    public List<WorkoutSessionResponse> getWorkoutSessionsByDate(String username, LocalDate date) {
        User user = findUserByUsername(username);
        List<WorkoutSession> sessions = workoutSessionRepository.findByUserAndDate(user, date);
        return workoutSessionMapper.toResponseList(sessions);
    }

    public List<WorkoutSessionResponse> getWorkoutSessionsByDateRange(String username,
                                                                      LocalDate startDate,
                                                                      LocalDate endDate) {
        User user = findUserByUsername(username);
        List<WorkoutSession> sessions = workoutSessionRepository
                .findByUserAndDateBetween(user, startDate, endDate);
        return workoutSessionMapper.toResponseList(sessions);
    }

    public Optional<WorkoutSessionResponse> getWorkoutSessionById(Long sessionId, String username) {
        return workoutSessionRepository.findById(sessionId)
                .filter(session -> session.getUser().getUsername().equals(username))
                .map(workoutSessionMapper::toResponse);
    }

    public List<WorkoutSessionResponse> getRecentWorkoutSessions(String username, int limit) {
        User user = findUserByUsername(username);
        List<WorkoutSession> sessions = workoutSessionRepository.findByUserOrderByDateDesc(user)
                .stream()
                .limit(limit)
                .toList();
        return workoutSessionMapper.toResponseList(sessions);
    }

    // =======================
    // WORKOUT PLAN SPECIFIC QUERIES
    // =======================

    public List<WorkoutSessionResponse> getSessionsForWorkoutPlan(String username, Long workoutPlanId) {
        User user = findUserByUsername(username);
        WorkoutPlan workoutPlan = findWorkoutPlanById(workoutPlanId);

        List<WorkoutSession> sessions = workoutSessionRepository
                .findByUserAndWorkoutPlanOrderByDateDesc(user, workoutPlan);
        return workoutSessionMapper.toResponseList(sessions);
    }

    public long getWorkoutPlanCompletionCount(String username, Long workoutPlanId) {
        User user = findUserByUsername(username);
        WorkoutPlan workoutPlan = findWorkoutPlanById(workoutPlanId);

        return workoutSessionRepository.countByUserAndWorkoutPlan(user, workoutPlan);
    }

    public Optional<WorkoutSessionResponse> getLastCompletionOfWorkoutPlan(String username, Long workoutPlanId) {
        User user = findUserByUsername(username);
        WorkoutPlan workoutPlan = findWorkoutPlanById(workoutPlanId);

        return workoutSessionRepository.findTopByUserAndWorkoutPlanOrderByDateDesc(user, workoutPlan)
                .map(workoutSessionMapper::toResponse);
    }

    // =======================
    // PROGRAM TRACKING
    // =======================

    public List<WorkoutSessionResponse> getProgramSessions(String username, Long programId) {
        User user = findUserByUsername(username);
        WorkoutProgram program = findWorkoutProgramById(programId);

        List<WorkoutSession> sessions = workoutSessionRepository
                .findByUserAndProgramOrderByWeekNumberAscDateAsc(user, program);
        return workoutSessionMapper.toResponseList(sessions);
    }

    public List<WorkoutSessionResponse> getProgramWeekSessions(String username, Long programId,
                                                               Integer weekNumber) {
        User user = findUserByUsername(username);
        WorkoutProgram program = findWorkoutProgramById(programId);

        List<WorkoutSession> sessions = workoutSessionRepository
                .findByUserAndProgramAndWeekNumberOrderByDate(user, program, weekNumber);
        return workoutSessionMapper.toResponseList(sessions);
    }

    public Map<String, Object> getProgramProgress(String username, Long programId) {
        User user = findUserByUsername(username);
        WorkoutProgram program = findWorkoutProgramById(programId);

        Long completedWeeks = workoutSessionRepository
                .countCompletedWeeksByUserAndProgram(user, program);

        Optional<Integer> currentWeek = workoutSessionRepository
                .findMaxWeekNumberByUserAndProgram(user, program);

        double progressPercentage = (completedWeeks.doubleValue() / program.getDurationWeeks()) * 100;

        return Map.of(
                "programId", programId,
                "programName", program.getName(),
                "totalWeeks", program.getDurationWeeks(),
                "completedWeeks", completedWeeks,
                "currentWeek", currentWeek.orElse(0),
                "progressPercentage", Math.min(progressPercentage, 100.0),
                "isCompleted", completedWeeks >= program.getDurationWeeks()
        );
    }

    // =======================
    // ANALYTICS & STATISTICS
    // =======================

    public WorkoutSessionAnalyticsResponse getWorkoutAnalytics(String username, LocalDate startDate,
                                                               LocalDate endDate) {
        User user = findUserByUsername(username);

        Long totalSessions = workoutSessionRepository.countByUserAndDateRange(user, startDate, endDate);

        Optional<Long> totalDuration = workoutSessionRepository.sumTotalDurationByUser(user);
        Optional<Long> totalCalories = workoutSessionRepository.sumCaloriesByUser(user);
        Optional<Double> averageDuration = workoutSessionRepository.averageDurationByUser(user);

        List<Object[]> moodStats = workoutSessionRepository.countByMoodForUser(user);
        Optional<Double> averageEffort = workoutSessionRepository.averageEffortByUser(user);

        return WorkoutSessionAnalyticsResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalSessions(totalSessions)
                .totalDurationMinutes(totalDuration.orElse(0L))
                .totalCaloriesBurned(totalCalories.orElse(0L))
                .averageDurationMinutes(averageDuration.orElse(0.0))
                .averageEffortRating(averageEffort.orElse(0.0))
                .moodStatistics(moodStats)
                .build();
    }

    public long getTotalWorkoutCount(String username) {
        User user = findUserByUsername(username);
        return workoutSessionRepository.countByUser(user);
    }

    public Long getWorkoutCountForDateRange(String username, LocalDate startDate, LocalDate endDate) {
        User user = findUserByUsername(username);
        return workoutSessionRepository.countByUserAndDateRange(user, startDate, endDate);
    }

    public List<WorkoutSessionResponse> getWorkoutStreak(String username) {
        User user = findUserByUsername(username);
        LocalDate startDate = LocalDate.now().minusDays(30); // Look back 30 days for streak

        List<LocalDate> workoutDates = workoutSessionRepository
                .findWorkoutDatesForStreak(user, startDate);

        // Calculate current streak
        int currentStreak = calculateCurrentStreak(workoutDates);

        // Return recent sessions that contribute to streak
        List<WorkoutSession> streakSessions = workoutSessionRepository
                .findRecentWorkouts(user, LocalDate.now().minusDays(currentStreak));

        return workoutSessionMapper.toResponseList(streakSessions);
    }

    public boolean hasWorkedOutToday(String username) {
        User user = findUserByUsername(username);
        return workoutSessionRepository.existsByUserAndDate(user, LocalDate.now());
    }

    // =======================
    // DATA RETENTION (FREE USERS)
    // =======================

    @Transactional
    public void cleanupOldWorkoutSessions(String username) {
        User user = findUserByUsername(username);

        // Only cleanup for free users
        if (user.getSubscriptionTier() != SubscriptionTier.FREE) {
            return;
        }

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);

        List<WorkoutSession> oldSessions = workoutSessionRepository
                .findSessionsToCleanup(user, cutoffDate);

        workoutSessionRepository.deleteAll(oldSessions);

        log.info("Cleaned up {} old workout sessions for free user {}",
                oldSessions.size(), username);
    }

    public List<WorkoutSessionResponse> getSessionsWithinRetentionPeriod(String username) {
        User user = findUserByUsername(username);

        LocalDateTime cutoffDate;
        if (user.getSubscriptionTier() == SubscriptionTier.FREE) {
            cutoffDate = LocalDateTime.now().minusDays(30);
        } else {
            cutoffDate = LocalDateTime.now().minusYears(10); // Effectively no limit for paid users
        }

        List<WorkoutSession> sessions = workoutSessionRepository
                .findSessionsWithinRetentionPeriod(user, cutoffDate);

        return workoutSessionMapper.toResponseList(sessions);
    }

    // =======================
    // SOCIAL FEATURES
    // =======================

    @Transactional
    public WorkoutSessionResponse shareWorkoutSession(Long sessionId, String username) {
        WorkoutSession workoutSession = findWorkoutSessionById(sessionId);
        validateOwnership(workoutSession, username);

        workoutSession.setIsShared(true);
        WorkoutSession saved = workoutSessionRepository.save(workoutSession);

        log.info("Workout session shared: {} by user {}", sessionId, username);

        return workoutSessionMapper.toResponse(saved);
    }

    @Transactional
    public WorkoutSessionResponse unshareWorkoutSession(Long sessionId, String username) {
        WorkoutSession workoutSession = findWorkoutSessionById(sessionId);
        validateOwnership(workoutSession, username);

        workoutSession.setIsShared(false);
        WorkoutSession saved = workoutSessionRepository.save(workoutSession);

        log.info("Workout session unshared: {} by user {}", sessionId, username);

        return workoutSessionMapper.toResponse(saved);
    }

    public List<WorkoutSessionResponse> getSharedWorkoutSessions(String username) {
        User user = findUserByUsername(username);
        List<WorkoutSession> sharedSessions = workoutSessionRepository
                .findByUserAndIsSharedTrueOrderByDateDesc(user);
        return workoutSessionMapper.toResponseList(sharedSessions);
    }

    // =======================
    // SCHEDULED WORKOUT INTEGRATION
    // =======================

    @Transactional
    public WorkoutSessionResponse startScheduledWorkout(String username, Long scheduledWorkoutId) {
        User user = findUserByUsername(username);
        ScheduledWorkout scheduledWorkout = findScheduledWorkoutById(scheduledWorkoutId);
        validateScheduledWorkoutOwnership(scheduledWorkout, user);

        // Create a new workout session from the scheduled workout
        WorkoutSession workoutSession = new WorkoutSession();
        workoutSession.setUser(user);
        workoutSession.setWorkoutPlan(scheduledWorkout.getWorkoutPlan());
        workoutSession.setDate(LocalDate.now());
        workoutSession.setScheduledWorkout(scheduledWorkout);

        if (scheduledWorkout.getProgram() != null) {
            workoutSession.setProgram(scheduledWorkout.getProgram());
            workoutSession.setWeekNumber(scheduledWorkout.getWeekNumber());
        }

        // Start the scheduled workout
        scheduledWorkout.startWorkout();

        WorkoutSession saved = workoutSessionRepository.save(workoutSession);
        scheduledWorkoutRepository.save(scheduledWorkout);

        log.info("Started scheduled workout: {} for user {}", scheduledWorkoutId, username);

        return workoutSessionMapper.toResponse(saved);
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

    private WorkoutSession findWorkoutSessionById(Long id) {
        return workoutSessionRepository.findById(id)
                .orElseThrow(() -> new WorkoutSessionNotFoundException(id));
    }

    private ScheduledWorkout findScheduledWorkoutById(Long id) {
        return scheduledWorkoutRepository.findById(id)
                .orElseThrow(() -> new ScheduledWorkoutNotFoundException(id));
    }

    private void validateOwnership(WorkoutSession workoutSession, String username) {
        if (!workoutSession.getUser().getUsername().equals(username)) {
            throw new RuntimeException("User does not have access to this workout session");
        }
    }

    private void validateScheduledWorkoutOwnership(ScheduledWorkout scheduledWorkout, User user) {
        if (!scheduledWorkout.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("User does not have access to this scheduled workout");
        }
    }

    private int calculateCurrentStreak(List<LocalDate> workoutDates) {
        if (workoutDates.isEmpty()) {
            return 0;
        }

        // Sort dates in descending order (most recent first)
        workoutDates.sort((d1, d2) -> d2.compareTo(d1));

        int streak = 0;
        LocalDate currentDate = LocalDate.now();

        for (LocalDate workoutDate : workoutDates) {
            if (workoutDate.equals(currentDate) || workoutDate.equals(currentDate.minusDays(streak))) {
                streak++;
                currentDate = workoutDate;
            } else {
                break;
            }
        }

        return streak;
    }

    public WorkoutSession findById(Long sessionId) {
        return workoutSessionRepository.findById(sessionId)
                .orElseThrow(() -> new WorkoutSessionNotFoundException(sessionId));
    }

    public long countByUserId(Long userId) {
        User user = findUserById(userId);
        return workoutSessionRepository.countByUser(user);
    }

    // Helper method if it doesn't exist
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }
}