package com.chidituke.workout_tracker.service.stats;

import com.chidituke.workout_tracker.dto.response.stats.WeeklyStatsResponse;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.repository.workout.WorkoutSessionRepository;
import com.chidituke.workout_tracker.util.StreakCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * Service for calculating weekly workout statistics
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyStatsService {

    private final WorkoutSessionRepository workoutSessionRepository;

    /**
     * Get weekly statistics for a user
     *
     * @param user The user to get stats for
     * @return WeeklyStatsResponse with all weekly metrics
     */
    @Transactional(readOnly = true)
    public WeeklyStatsResponse getWeeklyStats(User user) {
        log.info("Calculating weekly stats for user: {}", user.getId());

        // Get current week boundaries (Monday to Sunday)
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        // Count workouts this week
        Long workoutsThisWeek = workoutSessionRepository.countByUserAndDateRange(
                user, weekStart, weekEnd
        );

        // Calculate current streak
        LocalDate streakStartDate = today.minusDays(90); // Look back 90 days for streak
        List<LocalDate> workoutDates = workoutSessionRepository.findWorkoutDatesForStreak(
                user, streakStartDate
        );
        int currentStreak = StreakCalculator.calculateCurrentStreak(workoutDates);

        // TODO: Calculate XP this week (requires XP tracking in WorkoutSession)
        // For now, use 0 as placeholder
        int xpThisWeek = 0;

        // Build response
        WeeklyStatsResponse.WeeklyStatsResponseBuilder builder = WeeklyStatsResponse.builder()
                .workoutsThisWeek(workoutsThisWeek.intValue())
                .xpThisWeek(xpThisWeek)
                .currentStreak(currentStreak)
                .weekStartDate(weekStart)
                .weekEndDate(weekEnd);

        // Add goal tracking if user has a goal set
        Integer weeklyGoal = user.getWeeklyWorkoutGoal();
        if (weeklyGoal != null && weeklyGoal > 0) {
            double progress = weeklyGoal > 0 ? (double) workoutsThisWeek / weeklyGoal : 0.0;
            boolean goalAchieved = workoutsThisWeek >= weeklyGoal;
            int workoutsRemaining = Math.max(0, weeklyGoal - workoutsThisWeek.intValue());

            builder
                    .weeklyGoal(weeklyGoal)
                    .goalType(user.getGoalType() != null ? user.getGoalType() : "workouts")
                    .goalProgress(Math.min(1.0, progress))
                    .goalAchieved(goalAchieved)
                    .workoutsRemaining(workoutsRemaining)
                    .message(generateGoalMessage(workoutsThisWeek.intValue(), weeklyGoal, goalAchieved));
        }

        WeeklyStatsResponse response = builder.build();
        log.info("Weekly stats calculated: {} workouts, {} streak",
                response.getWorkoutsThisWeek(), response.getCurrentStreak());

        return response;
    }

    /**
     * Generate encouraging message based on goal progress
     */
    private String generateGoalMessage(int completed, int goal, boolean achieved) {
        if (achieved) {
            return "🎉 Goal achieved! Great work!";
        }

        int remaining = goal - completed;
        if (remaining == 1) {
            return "💪 1 more workout to hit your goal!";
        } else {
            return String.format("💪 %d more workouts to hit your goal!", remaining);
        }
    }

    /**
     * Get week start date (Monday)
     */
    public LocalDate getWeekStart() {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * Get week end date (Sunday)
     */
    public LocalDate getWeekEnd() {
        return LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }
}