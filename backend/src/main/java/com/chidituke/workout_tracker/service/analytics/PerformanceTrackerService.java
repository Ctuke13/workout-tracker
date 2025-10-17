package com.chidituke.workout_tracker.service.analytics;

import com.chidituke.workout_tracker.dto.response.analytics.PerformanceTrackerResponse;
import com.chidituke.workout_tracker.model.analytics.PerformanceMetric;
import com.chidituke.workout_tracker.model.progress.Season;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import com.chidituke.workout_tracker.repository.progress.SeasonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PerformanceTrackerService {

    private final UserRepository userRepository;
    private final SeasonRepository seasonRepository;
    // TODO: Add your workout repositories here

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Get performance data for a specific metric and time period
     */
    public PerformanceTrackerResponse getPerformanceData(String username, String metricStr, String periodStr) {
        log.info("Getting performance data - user: {}, metric: {}, period: {}", username, metricStr, periodStr);

        // Validate user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // Validate and parse metric
        PerformanceMetric metric = PerformanceMetric.fromString(metricStr);

        // Get date range based on period
        DateRange dateRange = getDateRange(periodStr);

        // Get raw data points from database (DAILY granularity)
        // TODO: Replace getRawDataPoints with actual database queries
        List<RawDataPoint> rawDataPoints = getRawDataPoints(user, metric, dateRange);

        // Aggregate data based on period
        List<PerformanceTrackerResponse.DataPoint> aggregatedDataPoints =
                aggregateData(rawDataPoints, periodStr);

        // Calculate summary statistics
        PerformanceTrackerResponse.Summary summary = calculateSummary(aggregatedDataPoints);

        return PerformanceTrackerResponse.builder()
                .metric(metric.name())
                .period(periodStr)
                .dataPoints(aggregatedDataPoints)
                .summary(summary)
                .build();
    }

    /**
     * Get raw data points from database (DAILY granularity)
     * TODO: Implement actual database queries here
     */
    private List<RawDataPoint> getRawDataPoints(User user, PerformanceMetric metric, DateRange dateRange) {
        log.warn("TODO: Implement database query for metric: {} between {} and {}",
                metric, dateRange.startDate, dateRange.endDate);

        // TODO: Replace with actual database queries like:
        //
        // switch (metric) {
        //     case WEIGHT:
        //         return workoutPerformanceRepository.findDailyMaxWeight(
        //             user.getId(), dateRange.startDate, dateRange.endDate
        //         );
        //     case VOLUME:
        //         return workoutPerformanceRepository.findDailyTotalVolume(
        //             user.getId(), dateRange.startDate, dateRange.endDate
        //         );
        //     // ... etc
        // }

        // For now, return mock data
        return generateMockRawDataPoints(dateRange, metric);
    }

    /**
     * Aggregate data based on period
     */
    private List<PerformanceTrackerResponse.DataPoint> aggregateData(
            List<RawDataPoint> rawDataPoints,
            String period) {

        switch (period.toUpperCase()) {
            case "WEEK":
            case "MONTH":
            case "SEASON":  // ✅ ADD THIS - treat like MONTH
                // No aggregation needed - use daily data
                return rawDataPoints.stream()
                        .map(raw -> PerformanceTrackerResponse.DataPoint.builder()
                                .date(raw.date.format(DATE_FORMATTER))
                                .value(raw.value)
                                .workoutCount(raw.workoutCount)
                                .build())
                        .collect(Collectors.toList());

            case "YEAR":
                // Aggregate to weekly data (52 points)
                return aggregateToWeekly(rawDataPoints);

            case "ALL_TIME":
                // Aggregate to monthly data (12-36 points)
                return aggregateToMonthly(rawDataPoints);

            default:
                throw new IllegalArgumentException("Invalid period: " + period);
        }
    }

    /**
     * Aggregate daily data to weekly averages
     */
    private List<PerformanceTrackerResponse.DataPoint> aggregateToWeekly(List<RawDataPoint> rawDataPoints) {
        Map<LocalDate, List<RawDataPoint>> weeklyGroups = rawDataPoints.stream()
                .collect(Collectors.groupingBy(point ->
                        // Group by the Monday of each week
                        point.date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                ));

        return weeklyGroups.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    LocalDate weekStart = entry.getKey();
                    List<RawDataPoint> weekPoints = entry.getValue();

                    double avgValue = weekPoints.stream()
                            .mapToDouble(p -> p.value)
                            .average()
                            .orElse(0.0);

                    int totalWorkouts = weekPoints.stream()
                            .mapToInt(p -> p.workoutCount)
                            .sum();

                    return PerformanceTrackerResponse.DataPoint.builder()
                            .date(weekStart.format(DATE_FORMATTER))
                            .value(avgValue)
                            .workoutCount(totalWorkouts)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Aggregate daily data to monthly averages
     */
    private List<PerformanceTrackerResponse.DataPoint> aggregateToMonthly(List<RawDataPoint> rawDataPoints) {
        Map<YearMonth, List<RawDataPoint>> monthlyGroups = rawDataPoints.stream()
                .collect(Collectors.groupingBy(point ->
                        YearMonth.from(point.date)
                ));

        return monthlyGroups.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    YearMonth yearMonth = entry.getKey();
                    List<RawDataPoint> monthPoints = entry.getValue();

                    double avgValue = monthPoints.stream()
                            .mapToDouble(p -> p.value)
                            .average()
                            .orElse(0.0);

                    int totalWorkouts = monthPoints.stream()
                            .mapToInt(p -> p.workoutCount)
                            .sum();

                    // Use first day of month as the date
                    LocalDate monthStart = yearMonth.atDay(1);

                    return PerformanceTrackerResponse.DataPoint.builder()
                            .date(monthStart.format(DATE_FORMATTER))
                            .value(avgValue)
                            .workoutCount(totalWorkouts)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Calculate summary statistics from data points
     */
    private PerformanceTrackerResponse.Summary calculateSummary(
            List<PerformanceTrackerResponse.DataPoint> dataPoints) {

        if (dataPoints.isEmpty()) {
            return PerformanceTrackerResponse.Summary.builder()
                    .average(0.0)
                    .peak(0.0)
                    .low(0.0)
                    .trend("STABLE")
                    .trendPercentage(0.0)
                    .totalDataPoints(0)
                    .build();
        }

        double average = dataPoints.stream()
                .mapToDouble(PerformanceTrackerResponse.DataPoint::getValue)
                .average()
                .orElse(0.0);

        double peak = dataPoints.stream()
                .mapToDouble(PerformanceTrackerResponse.DataPoint::getValue)
                .max()
                .orElse(0.0);

        double low = dataPoints.stream()
                .mapToDouble(PerformanceTrackerResponse.DataPoint::getValue)
                .min()
                .orElse(0.0);

        // Calculate trend (compare first vs last)
        double firstValue = dataPoints.get(0).getValue();
        double lastValue = dataPoints.get(dataPoints.size() - 1).getValue();
        double trendPercentage = firstValue != 0 ? ((lastValue - firstValue) / firstValue) * 100 : 0;

        String trend = trendPercentage > 5 ? "UP" :
                trendPercentage < -5 ? "DOWN" :
                        "STABLE";

        return PerformanceTrackerResponse.Summary.builder()
                .average(average)
                .peak(peak)
                .low(low)
                .trend(trend)
                .trendPercentage(trendPercentage)
                .totalDataPoints(dataPoints.size())
                .build();
    }

    /**
     * Get date range based on period
     */
    private DateRange getDateRange(String period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        switch (period.toUpperCase()) {
            case "WEEK":
                startDate = endDate.minusDays(6); // Last 7 days
                break;
            case "MONTH":
                startDate = endDate.minusDays(29); // Last 30 days
                break;
            case "SEASON":
                // ✅ NEW: Get current active season
                Season activeSeason = seasonRepository.findByIsActiveTrue()
                        .orElseThrow(() -> new RuntimeException("No active season found"));
                startDate = activeSeason.getStartDate();
                endDate = activeSeason.getEndDate();
                break;
            case "YEAR":
                startDate = endDate.minusYears(1);
                break;
            case "ALL_TIME":
                startDate = LocalDate.of(2020, 1, 1); // TODO: Get user's first workout date
                break;
            default:
                throw new IllegalArgumentException("Invalid period: " + period);
        }

        return new DateRange(startDate, endDate);
    }

    // ==================== MOCK DATA (TEMPORARY) ====================

    /**
     * Generate mock raw data points
     * TODO: Delete this method when implementing real database queries
     */
    private List<RawDataPoint> generateMockRawDataPoints(DateRange dateRange, PerformanceMetric metric) {
        List<RawDataPoint> dataPoints = new ArrayList<>();
        Random random = new Random();

        // Different base values for different metrics
        double minValue, maxValue;
        switch (metric) {
            case WEIGHT:
                minValue = 50.0;
                maxValue = 150.0;
                break;
            case VOLUME:
                minValue = 3000.0;
                maxValue = 8000.0;
                break;
            case REPS:
                minValue = 50.0;
                maxValue = 200.0;
                break;
            case SETS:
                minValue = 10.0;
                maxValue = 30.0;
                break;
            case DISTANCE:
                minValue = 2.0;
                maxValue = 10.0;
                break;
            case CALORIES:
                minValue = 200.0;
                maxValue = 600.0;
                break;
            case PACE:
                minValue = 5.0;
                maxValue = 8.0;
                break;
            case SPEED:
                minValue = 8.0;
                maxValue = 15.0;
                break;
            default:
                minValue = 10.0;
                maxValue = 100.0;
        }

        LocalDate currentDate = dateRange.startDate;
        while (!currentDate.isAfter(dateRange.endDate)) {
            double value = minValue + (maxValue - minValue) * random.nextDouble();
            int workoutCount = random.nextInt(3) + 1;

            dataPoints.add(new RawDataPoint(currentDate, value, workoutCount));
            currentDate = currentDate.plusDays(1);
        }

        return dataPoints;
    }

    // ==================== HELPER CLASSES ====================

    /**
     * Raw data point from database (daily granularity)
     */
    private static class RawDataPoint {
        final LocalDate date;
        final double value;
        final int workoutCount;

        RawDataPoint(LocalDate date, double value, int workoutCount) {
            this.date = date;
            this.value = value;
            this.workoutCount = workoutCount;
        }
    }

    /**
     * Date range for queries
     */
    private static class DateRange {
        final LocalDate startDate;
        final LocalDate endDate;

        DateRange(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}