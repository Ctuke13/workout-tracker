package com.chidituke.workout_tracker.service.analytics;

import com.chidituke.workout_tracker.model.workout.WorkoutSession;
import com.chidituke.workout_tracker.model.workout.PerformanceRecord;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.repository.workout.WorkoutSessionRepository;
import com.chidituke.workout_tracker.repository.workout.PerformanceRecordRepository;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final WorkoutSessionRepository workoutSessionRepository;
    private final PerformanceRecordRepository performanceRecordRepository;
    private final UserRepository userRepository;

    // ==================== TIME PERIOD ENUM ====================

    public enum TimePeriod {
        WEEK, MONTH, YEAR, ALL_TIME
    }

    // ==================== UNIFIED SUMMARY METHOD ====================

    /**
     * Get summary for any time period with comparison to previous period
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSummary(String username, TimePeriod period) {
        switch (period) {
            case WEEK:
                return getWeeklySummary(username);
            case MONTH:
                return getMonthlySummary(username);
            case YEAR:
                return getYearlySummary(username);
            case ALL_TIME:
                return getAllTimeSummary(username);
            default:
                throw new IllegalArgumentException("Invalid time period: " + period);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPerformanceTrackerData(
            String username,
            String metric,
            String period,
            Long exerciseId) { // ✅ ADD THIS PARAMETER

        User user = getUserByUsername(username);

        // Determine date range based on period
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        switch (period.toUpperCase()) {
            case "WEEK":
                startDate = endDate.minusWeeks(1);
                break;
            case "MONTH":
                startDate = endDate.minusMonths(1);
                break;
            case "SEASON":
                startDate = endDate.minusMonths(3);
                break;
            case "YEAR":
                startDate = endDate.minusYears(1);
                break;
            default:
                throw new IllegalArgumentException("Invalid period: " + period);
        }

        // Get workout sessions for the period
        List<WorkoutSession> sessions = workoutSessionRepository
                .findByUserAndDateBetween(user, startDate, endDate);

        // ✅ Filter by exercise if specified
        List<PerformanceRecord> records = new ArrayList<>();
        for (WorkoutSession session : sessions) {
            for (PerformanceRecord record : session.getPerformanceRecords()) {
                // If exerciseId is specified, only include that exercise
                if (exerciseId == null || record.getExercise().getId().equals(exerciseId)) {
                    records.add(record);
                }
            }
        }

        // Group by date and calculate metric values
        Map<LocalDate, List<PerformanceRecord>> recordsByDate = records.stream()
                .collect(Collectors.groupingBy(r -> r.getWorkoutSession().getDate()));

        // Calculate data points based on metric
        List<Map<String, Object>> dataPoints = new ArrayList<>();

        for (Map.Entry<LocalDate, List<PerformanceRecord>> entry : recordsByDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<PerformanceRecord> dayRecords = entry.getValue();

            double value = calculateMetricValue(metric, dayRecords);

            Map<String, Object> point = new HashMap<>();
            point.put("date", date.toString());
            point.put("value", value);
            point.put("workoutCount", dayRecords.stream()
                    .map(r -> r.getWorkoutSession().getId())
                    .distinct()
                    .count());

            dataPoints.add(point);
        }

        // Sort by date
        dataPoints.sort((a, b) ->
                ((String) a.get("date")).compareTo((String) b.get("date")));

        // Calculate summary statistics
        double[] values = dataPoints.stream()
                .mapToDouble(p -> (double) p.get("value"))
                .toArray();

        Map<String, Object> summary = new HashMap<>();
        if (values.length > 0) {
            summary.put("average", Arrays.stream(values).average().orElse(0));
            summary.put("peak", Arrays.stream(values).max().orElse(0));
            summary.put("low", Arrays.stream(values).min().orElse(0));

            // Calculate trend (simple: compare first half to second half)
            int midPoint = values.length / 2;
            double firstHalfAvg = Arrays.stream(values, 0, midPoint).average().orElse(0);
            double secondHalfAvg = Arrays.stream(values, midPoint, values.length).average().orElse(0);

            String trend = "STABLE";
            double trendPercentage = 0;
            if (firstHalfAvg > 0) {
                trendPercentage = ((secondHalfAvg - firstHalfAvg) / firstHalfAvg) * 100;
                if (trendPercentage > 5) trend = "UP";
                else if (trendPercentage < -5) trend = "DOWN";
            }

            summary.put("trend", trend);
            summary.put("trendPercentage", trendPercentage);
        } else {
            summary.put("average", 0);
            summary.put("peak", 0);
            summary.put("low", 0);
            summary.put("trend", "STABLE");
            summary.put("trendPercentage", 0);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("dataPoints", dataPoints);
        response.put("summary", summary);

        return response;
    }

    private double calculateMetricValue(String metric, List<PerformanceRecord> records) {
        switch (metric.toUpperCase()) {
            case "WEIGHT":
                // Max weight for the day
                return records.stream()
                        .filter(r -> r.getWeight() != null)
                        .mapToDouble(PerformanceRecord::getWeight)
                        .max()
                        .orElse(0);

            case "VOLUME":
                // Total volume for the day
                return records.stream()
                        .filter(r -> r.getWeight() != null && r.getReps() != null)
                        .mapToDouble(r -> r.getWeight() * r.getReps())
                        .sum();

            case "REPS":
                // Total reps for the day
                return records.stream()
                        .filter(r -> r.getReps() != null)
                        .mapToInt(PerformanceRecord::getReps)
                        .sum();

            case "SETS":
                // Total sets for the day
                return records.size();

            case "DISTANCE":
                // Total distance for the day
                return records.stream()
                        .filter(r -> r.getDistanceKm() != null)
                        .mapToDouble(PerformanceRecord::getDistanceKm)
                        .sum();

            case "PACE":
                // Average pace for the day
                return records.stream()
                        .filter(r -> r.getPaceMinPerKm() != null)
                        .mapToDouble(PerformanceRecord::getPaceMinPerKm)
                        .average()
                        .orElse(0);

            case "SPEED":
                // Average speed for the day
                return records.stream()
                        .filter(r -> r.getSpeedKmPerHour() != null)
                        .mapToDouble(PerformanceRecord::getSpeedKmPerHour)
                        .average()
                        .orElse(0);

            case "CALORIES":
                // Total calories for the day
                return records.stream()
                        .filter(r -> r.getCaloriesBurned() != null)
                        .mapToInt(PerformanceRecord::getCaloriesBurned)
                        .sum();

            // ✅ ADD THESE NEW CASES FOR ISOMETRIC EXERCISES
            case "HOLD_DURATION":
                // Average hold duration for the day (in seconds)
                return records.stream()
                        .filter(r -> r.getHoldDurationSeconds() != null)
                        .mapToInt(PerformanceRecord::getHoldDurationSeconds)
                        .average()
                        .orElse(0);

            case "TOTAL_HOLD_TIME":
                // Total cumulative hold time for the day (in seconds)
                return records.stream()
                        .filter(r -> r.getHoldDurationSeconds() != null)
                        .mapToInt(PerformanceRecord::getHoldDurationSeconds)
                        .sum();

            case "MAX_HOLD":
                // Maximum single hold duration for the day (in seconds)
                return records.stream()
                        .filter(r -> r.getHoldDurationSeconds() != null)
                        .mapToInt(PerformanceRecord::getHoldDurationSeconds)
                        .max()
                        .orElse(0);

            default:
                return 0;
        }
    }

    /**
     * Get summaries for all time periods at once (efficient for frontend)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAllPeriodSummaries(String username) {
        Map<String, Object> allSummaries = new HashMap<>();
        allSummaries.put("week", getWeeklySummary(username));
        allSummaries.put("month", getMonthlySummary(username));
        allSummaries.put("year", getYearlySummary(username));
        allSummaries.put("allTime", getAllTimeSummary(username));
        return allSummaries;
    }

    // ==================== WEEKLY SUMMARY ====================

    /**
     * Get this week's summary (Monday to Sunday)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getWeeklySummary(String username) {
        User user = getUserByUsername(username);

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6); // Sunday

        LocalDate lastWeekStart = weekStart.minusWeeks(1);
        LocalDate lastWeekEnd = lastWeekStart.plusDays(6);

        // Get sessions
        List<WorkoutSession> thisWeekSessions = workoutSessionRepository
                .findByUserAndDateBetween(user, weekStart, weekEnd);
        List<WorkoutSession> lastWeekSessions = workoutSessionRepository
                .findByUserAndDateBetween(user, lastWeekStart, lastWeekEnd);

        return buildSummaryResponse(
                "WEEK",
                weekStart,
                weekEnd,
                thisWeekSessions,
                lastWeekSessions
        );
    }

    // ==================== MONTHLY SUMMARY ====================

    /**
     * Get this month's summary (Calendar month)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getMonthlySummary(String username) {
        User user = getUserByUsername(username);

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate monthEnd = today.with(TemporalAdjusters.lastDayOfMonth());

        LocalDate lastMonthStart = monthStart.minusMonths(1);
        LocalDate lastMonthEnd = monthStart.minusDays(1);

        // Get sessions
        List<WorkoutSession> thisMonthSessions = workoutSessionRepository
                .findByUserAndDateBetween(user, monthStart, monthEnd);
        List<WorkoutSession> lastMonthSessions = workoutSessionRepository
                .findByUserAndDateBetween(user, lastMonthStart, lastMonthEnd);

        return buildSummaryResponse(
                "MONTH",
                monthStart,
                monthEnd,
                thisMonthSessions,
                lastMonthSessions
        );
    }

    // ==================== YEARLY SUMMARY ====================

    /**
     * Get this year's summary (Calendar year)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getYearlySummary(String username) {
        User user = getUserByUsername(username);

        LocalDate today = LocalDate.now();
        LocalDate yearStart = today.with(TemporalAdjusters.firstDayOfYear());
        LocalDate yearEnd = today.with(TemporalAdjusters.lastDayOfYear());

        LocalDate lastYearStart = yearStart.minusYears(1);
        LocalDate lastYearEnd = yearStart.minusDays(1);

        // Get sessions
        List<WorkoutSession> thisYearSessions = workoutSessionRepository
                .findByUserAndDateBetween(user, yearStart, yearEnd);
        List<WorkoutSession> lastYearSessions = workoutSessionRepository
                .findByUserAndDateBetween(user, lastYearStart, lastYearEnd);

        return buildSummaryResponse(
                "YEAR",
                yearStart,
                yearEnd,
                thisYearSessions,
                lastYearSessions
        );
    }

    // ==================== ALL-TIME SUMMARY ====================

    /**
     * Get all-time summary (no comparison period)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAllTimeSummary(String username) {
        User user = getUserByUsername(username);

        // Get ALL sessions for this user
        List<WorkoutSession> allSessions = workoutSessionRepository
                .findByUserOrderByDateDesc(user);

        if (allSessions.isEmpty()) {
            return buildEmptySummary("ALL_TIME");
        }

        // Find first and last workout dates
        LocalDate firstWorkout = allSessions.stream()
                .map(WorkoutSession::getDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());

        LocalDate lastWorkout = allSessions.stream()
                .map(WorkoutSession::getDate)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());

        // Calculate stats
        int totalWorkouts = allSessions.size();
        int totalMinutes = allSessions.stream()
                .mapToInt(s -> s.getTotalDurationMinutes() != null ? s.getTotalDurationMinutes() : 0)
                .sum();
        double totalVolume = calculateTotalVolume(allSessions);

        // Calculate days since first workout
        long daysSinceFirstWorkout = java.time.temporal.ChronoUnit.DAYS.between(firstWorkout, LocalDate.now()) + 1;
        double workoutsPerWeek = daysSinceFirstWorkout > 0
                ? (totalWorkouts * 7.0) / daysSinceFirstWorkout
                : 0;

        Map<String, Object> summary = new HashMap<>();
        summary.put("period", "ALL_TIME");
        summary.put("startDate", firstWorkout);
        summary.put("endDate", lastWorkout);
        summary.put("workouts", totalWorkouts);
        summary.put("minutes", totalMinutes);
        summary.put("volume", Math.round(totalVolume));
        summary.put("daysSinceFirstWorkout", daysSinceFirstWorkout);
        summary.put("workoutsPerWeek", Math.round(workoutsPerWeek * 10) / 10.0);
        summary.put("averageMinutesPerWorkout", totalWorkouts > 0 ? Math.round((double) totalMinutes / totalWorkouts) : 0);

        // No comparison for all-time
        summary.put("workoutChange", 0);
        summary.put("minutesChange", 0);
        summary.put("volumeChange", 0);

        return summary;
    }

    // ==================== PERSONAL RECORDS ====================

    /**
     * Get recent personal records (last 30 days by default)
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecentPersonalRecords(String username, int days) {
        User user = getUserByUsername(username);
        LocalDate startDate = LocalDate.now().minusDays(days);

        List<WorkoutSession> recentSessions = workoutSessionRepository
                .findByUserAndDateBetween(user, startDate, LocalDate.now());

        return extractPersonalRecords(recentSessions);
    }

    /**
     * Get all-time personal records
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllTimePersonalRecords(String username) {
        User user = getUserByUsername(username);
        List<WorkoutSession> allSessions = workoutSessionRepository
                .findByUserOrderByDateDesc(user);

        return extractPersonalRecords(allSessions);
    }

    // ==================== TOP EXERCISES ====================

    /**
     * Get most used exercises for a time period
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopExercises(String username, TimePeriod period, int limit) {
        User user = getUserByUsername(username);

        LocalDate startDate;
        LocalDate endDate = LocalDate.now();

        switch (period) {
            case WEEK:
                startDate = endDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                break;
            case MONTH:
                startDate = endDate.with(TemporalAdjusters.firstDayOfMonth());
                break;
            case YEAR:
                startDate = endDate.with(TemporalAdjusters.firstDayOfYear());
                break;
            case ALL_TIME:
                return getTopExercisesAllTime(username, limit);
            default:
                throw new IllegalArgumentException("Invalid period: " + period);
        }

        List<WorkoutSession> sessions = workoutSessionRepository
                .findByUserAndDateBetween(user, startDate, endDate);

        return extractTopExercises(sessions, limit);
    }

    /**
     * Get most used exercises all-time
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopExercisesAllTime(String username, int limit) {
        User user = getUserByUsername(username);
        List<WorkoutSession> allSessions = workoutSessionRepository
                .findByUserOrderByDateDesc(user);

        return extractTopExercises(allSessions, limit);
    }

    // ==================== EXERCISE PROGRESSION ====================

    /**
     * Get progression trend for specific exercise (last N weeks)
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getExerciseProgression(String username, Long exerciseId, int weeks) {
        User user = getUserByUsername(username);
        LocalDate startDate = LocalDate.now().minusWeeks(weeks);

        List<WorkoutSession> sessions = workoutSessionRepository
                .findByUserAndDateBetween(user, startDate, LocalDate.now());

        List<Map<String, Object>> progressionData = new ArrayList<>();

        for (WorkoutSession session : sessions) {
            for (PerformanceRecord record : session.getPerformanceRecords()) {
                if (record.getExercise().getId().equals(exerciseId)) {
                    Map<String, Object> dataPoint = new HashMap<>();
                    dataPoint.put("date", session.getDate());
                    dataPoint.put("weight", record.getWeight());
                    dataPoint.put("reps", record.getReps());
                    dataPoint.put("volume", record.getWeight() != null && record.getReps() != null
                            ? record.getWeight() * record.getReps() : 0);
                    dataPoint.put("setNumber", record.getSetNumber());
                    progressionData.add(dataPoint);
                }
            }
        }

        // Sort by date
        progressionData.sort((a, b) ->
                ((LocalDate) a.get("date")).compareTo((LocalDate) b.get("date")));

        return progressionData;
    }

    // ==================== HELPER METHODS ====================

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    /**
     * Build a summary response with comparison to previous period
     */
    private Map<String, Object> buildSummaryResponse(
            String period,
            LocalDate startDate,
            LocalDate endDate,
            List<WorkoutSession> currentSessions,
            List<WorkoutSession> previousSessions) {

        // Calculate current period stats
        int currentWorkouts = currentSessions.size();
        int currentMinutes = currentSessions.stream()
                .mapToInt(s -> s.getTotalDurationMinutes() != null ? s.getTotalDurationMinutes() : 0)
                .sum();
        double currentVolume = calculateTotalVolume(currentSessions);

        // Calculate previous period stats for comparison
        int previousWorkouts = previousSessions.size();
        int previousMinutes = previousSessions.stream()
                .mapToInt(s -> s.getTotalDurationMinutes() != null ? s.getTotalDurationMinutes() : 0)
                .sum();
        double previousVolume = calculateTotalVolume(previousSessions);

        // Calculate percentage changes
        double workoutChange = calculatePercentageChange(previousWorkouts, currentWorkouts);
        double minutesChange = calculatePercentageChange(previousMinutes, currentMinutes);
        double volumeChange = calculatePercentageChange(previousVolume, currentVolume);

        Map<String, Object> summary = new HashMap<>();
        summary.put("period", period);
        summary.put("startDate", startDate);
        summary.put("endDate", endDate);
        summary.put("workouts", currentWorkouts);
        summary.put("minutes", currentMinutes);
        summary.put("volume", Math.round(currentVolume));
        summary.put("workoutChange", Math.round(workoutChange));
        summary.put("minutesChange", Math.round(minutesChange));
        summary.put("volumeChange", Math.round(volumeChange));
        summary.put("averageMinutesPerWorkout", currentWorkouts > 0
                ? Math.round((double) currentMinutes / currentWorkouts) : 0);

        return summary;
    }

    private Map<String, Object> buildEmptySummary(String period) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("period", period);
        summary.put("workouts", 0);
        summary.put("minutes", 0);
        summary.put("volume", 0);
        summary.put("workoutChange", 0);
        summary.put("minutesChange", 0);
        summary.put("volumeChange", 0);
        return summary;
    }

    private double calculateTotalVolume(List<WorkoutSession> sessions) {
        double totalVolume = 0;
        for (WorkoutSession session : sessions) {
            for (PerformanceRecord record : session.getPerformanceRecords()) {
                if (record.getWeight() != null && record.getReps() != null) {
                    totalVolume += record.getWeight() * record.getReps();
                }
            }
        }
        return totalVolume;
    }

    private double calculatePercentageChange(double oldValue, double newValue) {
        if (oldValue == 0) {
            return newValue > 0 ? 100 : 0;
        }
        return ((newValue - oldValue) / oldValue) * 100;
    }

    /**
     * Extract personal records from sessions
     * Supports: MAX_WEIGHT, MAX_VOLUME, MAX_HOLD, MAX_DISTANCE, BEST_TIME
     */
    private List<Map<String, Object>> extractPersonalRecords(List<WorkoutSession> sessions) {
        // Group performance records by exercise
        Map<Long, List<PerformanceRecord>> recordsByExercise = new HashMap<>();

        for (WorkoutSession session : sessions) {
            for (PerformanceRecord record : session.getPerformanceRecords()) {
                Long exerciseId = record.getExercise().getId();
                recordsByExercise.computeIfAbsent(exerciseId, k -> new ArrayList<>()).add(record);
            }
        }

        List<Map<String, Object>> prs = new ArrayList<>();

        // Find PRs for each exercise
        for (Map.Entry<Long, List<PerformanceRecord>> entry : recordsByExercise.entrySet()) {
            List<PerformanceRecord> records = entry.getValue();

            // 1. Find max weight PR
            records.stream()
                    .filter(r -> r.getWeight() != null && r.getWeight() > 0)
                    .max(Comparator.comparing(PerformanceRecord::getWeight))
                    .ifPresent(maxWeightRecord -> {
                        Map<String, Object> pr = new HashMap<>();
                        pr.put("type", "MAX_WEIGHT");
                        pr.put("exerciseName", maxWeightRecord.getExercise().getExerciseName());
                        pr.put("exerciseId", maxWeightRecord.getExercise().getId());
                        pr.put("value", maxWeightRecord.getWeight());
                        pr.put("reps", maxWeightRecord.getReps());
                        pr.put("date", maxWeightRecord.getWorkoutSession().getDate());
                        pr.put("unit", "lbs");
                        prs.add(pr);
                    });

            // 2. Find max volume PR (weight × reps)
            records.stream()
                    .filter(r -> r.getWeight() != null && r.getReps() != null)
                    .max(Comparator.comparing(r -> r.getWeight() * r.getReps()))
                    .ifPresent(maxVolumeRecord -> {
                        double volume = maxVolumeRecord.getWeight() * maxVolumeRecord.getReps();
                        Map<String, Object> pr = new HashMap<>();
                        pr.put("type", "MAX_VOLUME");
                        pr.put("exerciseName", maxVolumeRecord.getExercise().getExerciseName());
                        pr.put("exerciseId", maxVolumeRecord.getExercise().getId());
                        pr.put("value", Math.round(volume));
                        pr.put("weight", maxVolumeRecord.getWeight());
                        pr.put("reps", maxVolumeRecord.getReps());
                        pr.put("date", maxVolumeRecord.getWorkoutSession().getDate());
                        pr.put("unit", "lbs");
                        prs.add(pr);
                    });

            // ✅ 3. Find max hold PR (for isometric exercises)
            records.stream()
                    .filter(r -> r.getHoldDurationSeconds() != null && r.getHoldDurationSeconds() > 0)
                    .max(Comparator.comparing(PerformanceRecord::getHoldDurationSeconds))
                    .ifPresent(maxHoldRecord -> {
                        Map<String, Object> pr = new HashMap<>();
                        pr.put("type", "MAX_HOLD");
                        pr.put("exerciseName", maxHoldRecord.getExercise().getExerciseName());
                        pr.put("exerciseId", maxHoldRecord.getExercise().getId());
                        pr.put("value", maxHoldRecord.getHoldDurationSeconds());
                        pr.put("date", maxHoldRecord.getWorkoutSession().getDate());
                        pr.put("unit", "seconds");
                        prs.add(pr);
                    });

            // ✅ 4. Find max distance PR (for cardio)
            records.stream()
                    .filter(r -> r.getDistanceKm() != null && r.getDistanceKm() > 0)
                    .max(Comparator.comparing(PerformanceRecord::getDistanceKm))
                    .ifPresent(maxDistanceRecord -> {
                        Map<String, Object> pr = new HashMap<>();
                        pr.put("type", "MAX_DISTANCE");
                        pr.put("exerciseName", maxDistanceRecord.getExercise().getExerciseName());
                        pr.put("exerciseId", maxDistanceRecord.getExercise().getId());
                        pr.put("value", maxDistanceRecord.getDistanceKm());
                        pr.put("date", maxDistanceRecord.getWorkoutSession().getDate());
                        pr.put("unit", "km");
                        prs.add(pr);
                    });

            // ✅ 5. Find best time PR (fastest duration for timed exercises)
            records.stream()
                    .filter(r -> r.getDurationSeconds() != null && r.getDurationSeconds() > 0)
                    .min(Comparator.comparing(PerformanceRecord::getDurationSeconds)) // MIN for fastest time
                    .ifPresent(bestTimeRecord -> {
                        Map<String, Object> pr = new HashMap<>();
                        pr.put("type", "BEST_TIME");
                        pr.put("exerciseName", bestTimeRecord.getExercise().getExerciseName());
                        pr.put("exerciseId", bestTimeRecord.getExercise().getId());
                        pr.put("value", bestTimeRecord.getDurationSeconds());
                        pr.put("date", bestTimeRecord.getWorkoutSession().getDate());
                        pr.put("unit", "time");
                        prs.add(pr);
                    });
        }

        // Sort by date (most recent first) and limit to 10
        return prs.stream()
                .sorted((a, b) -> ((LocalDate) b.get("date")).compareTo((LocalDate) a.get("date")))
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Extract top exercises from sessions
     */
    private List<Map<String, Object>> extractTopExercises(List<WorkoutSession> sessions, int limit) {
        // Count exercise usage and calculate total volume
        Map<Long, ExerciseStats> exerciseStatsMap = new HashMap<>();

        for (WorkoutSession session : sessions) {
            for (PerformanceRecord record : session.getPerformanceRecords()) {
                Long exerciseId = record.getExercise().getId();
                String exerciseName = record.getExercise().getExerciseName();
                String trackingMode = record.getExercise().getWorkoutTrackingMode().name();

                ExerciseStats stats = exerciseStatsMap.computeIfAbsent(exerciseId,
                        k -> new ExerciseStats(exerciseId, exerciseName, trackingMode));

                stats.incrementCount();

                // Add volume if applicable
                if (record.getWeight() != null && record.getReps() != null) {
                    stats.addVolume(record.getWeight() * record.getReps());
                }
            }
        }

        // Convert to list and sort by count
        return exerciseStatsMap.values().stream()
                .sorted((a, b) -> Integer.compare(b.count, a.count))
                .limit(limit)
                .map(stats -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("exerciseId", stats.exerciseId);
                    map.put("exerciseName", stats.name);
                    map.put("trackingMode", stats.trackingMode); // ✅ ADD THIS
                    map.put("count", stats.count);
                    map.put("volume", Math.round(stats.totalVolume));
                    return map;
                })
                .collect(Collectors.toList());
    }

    // Helper class for tracking exercise stats
    private static class ExerciseStats {
        Long exerciseId;
        String name;
        String trackingMode; // ✅ ADD THIS
        int count = 0;
        double totalVolume = 0;

        ExerciseStats(Long exerciseId, String name, String trackingMode) { // ✅ UPDATE CONSTRUCTOR
            this.exerciseId = exerciseId;
            this.name = name;
            this.trackingMode = trackingMode; // ✅ ADD THIS
        }

        void incrementCount() {
            count++;
        }

        void addVolume(double volume) {
            totalVolume += volume;
        }
    }
}