package com.chidituke.workout_tracker.service.scheduled_workouts;

import com.chidituke.workout_tracker.controller.workout.ScheduledWorkoutController;
import com.chidituke.workout_tracker.exceptions.user.UserNotFoundException;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.user.enums.SubscriptionTier;
import com.chidituke.workout_tracker.model.workout.ScheduledWorkout;
import com.chidituke.workout_tracker.model.workout.WorkoutPlan;
import com.chidituke.workout_tracker.repository.scheduled_workouts.ScheduledWorkoutRepository;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for workout analytics, statistics, and reporting.
 * Handles all aspects of workout data analysis and performance metrics.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutAnalyticsService {

    private final ScheduledWorkoutRepository scheduledWorkoutRepository;
    private final UserRepository userRepository;

    // ==================== COMPREHENSIVE WORKOUT STATISTICS ====================

    /**
     * Get comprehensive workout statistics for a user
     */
    @Cacheable(value = "user-workout-stats", key = "#username + '_' + #date")
    public ScheduledWorkoutController.WorkoutStatsResponse getWorkoutStats(String username, LocalDate date) {
        User user = findUserByUsername(username);
        LocalDate today = LocalDate.now();

        log.debug("Getting workout statistics for user {} (date: {})",
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

    // ==================== SCHEDULING ANALYTICS ====================

    /**
     * Get scheduling analytics for a date range
     */
    @Cacheable(value = "scheduling-analytics", key = "#username + '_' + #startDate + '_' + #endDate")
    public Map<String, Object> getSchedulingAnalytics(String username, LocalDate startDate, LocalDate endDate) {
        User user = findUserByUsername(username);

        Long totalScheduled = scheduledWorkoutRepository
                .countScheduledWorkoutsInRange(user, startDate, endDate);

        Double completionRate = scheduledWorkoutRepository
                .calculateCompletionRate(user, startDate, endDate)
                .orElse(0.0);

        List<Object[]> frequencyByDay = scheduledWorkoutRepository
                .getWorkoutFrequencyByDayOfWeek(user);

        // Calculate additional metrics
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalScheduled", totalScheduled);
        analytics.put("completionRate", completionRate);
        analytics.put("frequencyByDayOfWeek", frequencyByDay);
        analytics.put("period", Map.of("start", startDate, "end", endDate));

        // Add scheduling patterns
        analytics.put("schedulingPatterns", analyzeSchedulingPatterns(user, startDate, endDate));
        analytics.put("performanceMetrics", calculatePerformanceMetrics(user, startDate, endDate));
        analytics.put("consistencyScore", calculateConsistencyScore(user, startDate, endDate));

        return analytics;
    }

    // ==================== STREAK CALCULATIONS ====================

    @Cacheable(value = "user-streak", key = "#user.id")
    public int calculateCurrentStreak(User user) {
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

            // Prevent infinite loops
            if (streak >= 365) break;
        }

        return streak;
    }

    @Cacheable(value = "user-longest-streak", key = "#user.id")
    public int calculateLongestStreak(User user) {
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

    // ==================== WORKOUT PATTERN ANALYSIS ====================

    public LocalDate findLastWorkoutDate(User user) {
        List<ScheduledWorkout> completedWorkouts = scheduledWorkoutRepository
                .findByUserAndStatusOrderByScheduledDateAsc(user, ScheduledWorkout.ScheduleStatus.COMPLETED);

        return completedWorkouts.isEmpty() ? null :
                completedWorkouts.get(completedWorkouts.size() - 1).getScheduledDate();
    }

    public String findLastWorkoutType(User user) {
        List<ScheduledWorkout> completedWorkouts = scheduledWorkoutRepository
                .findByUserAndStatusOrderByScheduledDateAsc(user, ScheduledWorkout.ScheduleStatus.COMPLETED);

        if (completedWorkouts.isEmpty()) return null;

        ScheduledWorkout lastWorkout = completedWorkouts.get(completedWorkouts.size() - 1);
        WorkoutPlan plan = lastWorkout.getWorkoutPlan();
        return plan != null ? plan.getWorkoutCategory() : "Unknown";
    }

    @Cacheable(value = "user-favorite-exercise-type", key = "#user.id")
    public String findFavoriteExerciseType(User user) {
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

    // ==================== ADVANCED ANALYTICS ====================

    private Map<String, Object> analyzeSchedulingPatterns(User user, LocalDate startDate, LocalDate endDate) {
        List<ScheduledWorkout> workouts = scheduledWorkoutRepository
                .findByUserAndScheduledDateBetweenOrderByScheduledDateAsc(user, startDate, endDate);

        Map<String, Object> patterns = new HashMap<>();

        // Most active day of week
        Map<Integer, Long> dayFrequency = workouts.stream()
                .collect(Collectors.groupingBy(
                        sw -> sw.getScheduledDate().getDayOfWeek().getValue(),
                        Collectors.counting()
                ));

        String mostActiveDay = dayFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> java.time.DayOfWeek.of(entry.getKey()).name())
                .orElse("NONE");

        patterns.put("mostActiveDayOfWeek", mostActiveDay);

        // Average workouts per week
        long weeks = java.time.temporal.ChronoUnit.WEEKS.between(startDate, endDate) + 1;
        double avgWorkoutsPerWeek = weeks > 0 ? (double) workouts.size() / weeks : 0;
        patterns.put("averageWorkoutsPerWeek", avgWorkoutsPerWeek);

        // Preferred workout duration
        OptionalDouble avgDuration = workouts.stream()
                .filter(sw -> sw.getEstimatedDurationMinutes() != null)
                .mapToInt(ScheduledWorkout::getEstimatedDurationMinutes)
                .average();
        patterns.put("averageWorkoutDuration", avgDuration.orElse(0.0));

        return patterns;
    }

    private Map<String, Object> calculatePerformanceMetrics(User user, LocalDate startDate, LocalDate endDate) {
        List<ScheduledWorkout> workouts = scheduledWorkoutRepository
                .findByUserAndScheduledDateBetweenOrderByScheduledDateAsc(user, startDate, endDate);

        Map<String, Object> metrics = new HashMap<>();

        // Completion rate by workout type
        Map<String, Double> completionByType = workouts.stream()
                .filter(sw -> sw.getWorkoutPlan() != null && sw.getWorkoutPlan().getWorkoutCategory() != null)
                .collect(Collectors.groupingBy(
                        sw -> sw.getWorkoutPlan().getWorkoutCategory(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    long completed = list.stream()
                                            .filter(sw -> sw.getStatus() == ScheduledWorkout.ScheduleStatus.COMPLETED)
                                            .count();
                                    return list.isEmpty() ? 0.0 : (double) completed / list.size() * 100;
                                }
                        )
                ));

        metrics.put("completionRatesByType", completionByType);

        // Total volume metrics
        int totalScheduled = workouts.size();
        int totalCompleted = (int) workouts.stream()
                .filter(sw -> sw.getStatus() == ScheduledWorkout.ScheduleStatus.COMPLETED)
                .count();
        int totalSkipped = (int) workouts.stream()
                .filter(sw -> sw.getStatus() == ScheduledWorkout.ScheduleStatus.CANCELLED)
                .count();

        metrics.put("totalScheduled", totalScheduled);
        metrics.put("totalCompleted", totalCompleted);
        metrics.put("totalSkipped", totalSkipped);
        metrics.put("overallCompletionRate", totalScheduled > 0 ? (double) totalCompleted / totalScheduled * 100 : 0.0);

        return metrics;
    }

    private double calculateConsistencyScore(User user, LocalDate startDate, LocalDate endDate) {
        List<ScheduledWorkout> workouts = scheduledWorkoutRepository
                .findByUserAndScheduledDateBetweenOrderByScheduledDateAsc(user, startDate, endDate);

        if (workouts.isEmpty()) return 0.0;

        // Calculate consistency based on completion rate and regularity
        long completedCount = workouts.stream()
                .filter(sw -> sw.getStatus() == ScheduledWorkout.ScheduleStatus.COMPLETED)
                .count();

        double completionRate = (double) completedCount / workouts.size();

        // Calculate regularity (how evenly spaced are the workouts)
        Set<LocalDate> workoutDates = workouts.stream()
                .filter(sw -> sw.getStatus() == ScheduledWorkout.ScheduleStatus.COMPLETED)
                .map(ScheduledWorkout::getScheduledDate)
                .collect(Collectors.toSet());

        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        double regularityScore = totalDays > 0 ? (double) workoutDates.size() / totalDays : 0;

        // Combine completion rate (70%) and regularity (30%) for consistency score
        return (completionRate * 0.7 + regularityScore * 0.3) * 100;
    }

    // ==================== DATA RETENTION & CLEANUP ====================

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

    /**
     * Get user analytics summary for dashboard
     */
    @Cacheable(value = "user-analytics-summary", key = "#username")
    public Map<String, Object> getUserAnalyticsSummary(String username) {
        User user = findUserByUsername(username);

        Map<String, Object> summary = new HashMap<>();

        // Basic stats
        summary.put("currentStreak", calculateCurrentStreak(user));
        summary.put("longestStreak", calculateLongestStreak(user));
        summary.put("favoriteExerciseType", findFavoriteExerciseType(user));
        summary.put("lastWorkoutDate", findLastWorkoutDate(user));

        // This month's performance
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

        List<ScheduledWorkout> monthWorkouts = scheduledWorkoutRepository
                .findByUserAndScheduledDateBetweenOrderByScheduledDateAsc(user, monthStart, monthEnd);

        int monthlyScheduled = monthWorkouts.size();
        int monthlyCompleted = (int) monthWorkouts.stream()
                .filter(sw -> sw.getStatus() == ScheduledWorkout.ScheduleStatus.COMPLETED)
                .count();

        summary.put("monthlyScheduled", monthlyScheduled);
        summary.put("monthlyCompleted", monthlyCompleted);
        summary.put("monthlyCompletionRate", monthlyScheduled > 0 ?
                (double) monthlyCompleted / monthlyScheduled * 100 : 0.0);

        // Consistency score
        summary.put("consistencyScore", calculateConsistencyScore(user, monthStart, monthEnd));

        return summary;
    }

    /**
     * Generate exercise analysis report for debugging
     */
    public void logExerciseAnalysis(String username, LocalDate date) {
        try {
            User user = findUserByUsername(username);
            List<ScheduledWorkout> workouts = scheduledWorkoutRepository
                    .findByUserAndScheduledDateOrderByCreatedAtAsc(user, date);

            log.info("Exercise Analysis Report for {} on {}:", username, date);
            log.info("Found {} scheduled workouts", workouts.size());

            for (ScheduledWorkout workout : workouts) {
                if (workout.getExercise() != null) {
                    log.info("Workout {}: {} -> Status: {} (Cardio: {}, Isometric: {})",
                            workout.getId(),
                            workout.getExercise().getExerciseName(),
                            workout.getStatus(),
                            workout.getExercise().getIsCardio(),
                            workout.getExercise().getIsIsometric());
                } else {
                    log.info("Workout {}: No exercise resolved (Plan: {})",
                            workout.getId(),
                            workout.getWorkoutPlan() != null ? workout.getWorkoutPlan().getWorkoutName() : "None");
                }
            }

        } catch (Exception e) {
            log.error("Failed to generate exercise analysis: {}", e.getMessage(), e);
        }
    }

    // ==================== HELPER METHODS ====================

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }
}