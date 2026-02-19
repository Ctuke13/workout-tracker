package com.chidituke.workout_tracker.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Utility class for calculating workout streaks
 * A streak is consecutive days with at least one workout
 */
@Slf4j
public class StreakCalculator {

    /**
     * Calculate current active streak from workout dates
     *
     * @param workoutDates List of dates user completed workouts (should be sorted DESC)
     * @return Current streak in days (0 if no current streak)
     */
    public static int calculateCurrentStreak(List<LocalDate> workoutDates) {
        if (workoutDates == null || workoutDates.isEmpty()) {
            return 0;
        }

        // Remove duplicates and sort in descending order
        Set<LocalDate> uniqueDates = new TreeSet<>((a, b) -> b.compareTo(a));
        uniqueDates.addAll(workoutDates);

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // Check if user worked out today or yesterday
        // (Allow 1-day grace period - streak continues if worked out yesterday)
        if (!uniqueDates.contains(today) && !uniqueDates.contains(yesterday)) {
            log.debug("No workout today or yesterday - streak broken");
            return 0;
        }

        // Start counting from most recent workout day
        LocalDate currentDate = uniqueDates.contains(today) ? today : yesterday;
        int streak = 0;

        // Count consecutive days backwards
        for (LocalDate date : uniqueDates) {
            if (date.equals(currentDate)) {
                streak++;
                currentDate = currentDate.minusDays(1);
            } else if (date.isBefore(currentDate)) {
                // Gap found - streak ends
                break;
            }
        }

        log.debug("Calculated streak: {} days", streak);
        return streak;
    }

    /**
     * Calculate longest streak from workout dates
     *
     * @param workoutDates List of dates user completed workouts
     * @return Longest streak ever achieved
     */
    public static int calculateLongestStreak(List<LocalDate> workoutDates) {
        if (workoutDates == null || workoutDates.isEmpty()) {
            return 0;
        }

        Set<LocalDate> uniqueDates = new TreeSet<>(workoutDates);

        int maxStreak = 0;
        int currentStreak = 0;
        LocalDate previousDate = null;

        for (LocalDate date : uniqueDates) {
            if (previousDate == null || date.equals(previousDate.plusDays(1))) {
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
            } else {
                currentStreak = 1;
            }
            previousDate = date;
        }

        return maxStreak;
    }

    /**
     * Check if user has an active streak (worked out today or yesterday)
     *
     * @param workoutDates List of workout dates
     * @return true if streak is active
     */
    public static boolean hasActiveStreak(List<LocalDate> workoutDates) {
        if (workoutDates == null || workoutDates.isEmpty()) {
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        return workoutDates.contains(today) || workoutDates.contains(yesterday);
    }
}