package com.chidituke.workout_tracker.controller.analytics;

import com.chidituke.workout_tracker.service.analytics.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for analytics and insights
 * Base path: /api/analytics
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics", description = "Analytics and insights endpoints")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // ==================== MULTI-PERIOD SUMMARIES ====================

    /**
     * Get summaries for ALL time periods at once
     * Most efficient for frontend - one API call gets everything
     * <p>
     * GET /api/analytics/summary/all
     */
    @GetMapping("/summary/all")
    @Operation(summary = "Get all period summaries",
            description = "Get week, month, year, and all-time summaries in one call")
    public ResponseEntity<Map<String, Object>> getAllPeriodSummaries(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Getting all period summaries for user: {}", userDetails.getUsername());
        Map<String, Object> allSummaries = analyticsService.getAllPeriodSummaries(userDetails.getUsername());
        return ResponseEntity.ok(allSummaries);
    }

    // ==================== INDIVIDUAL PERIOD SUMMARIES ====================

    /**
     * Get this week's summary (Monday to Sunday)
     * <p>
     * GET /api/analytics/summary/week
     */
    @GetMapping("/summary/week")
    @Operation(summary = "Get weekly summary",
            description = "Get this week's workout summary with comparison to last week")
    public ResponseEntity<Map<String, Object>> getWeeklySummary(
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> summary = analyticsService.getWeeklySummary(userDetails.getUsername());
        return ResponseEntity.ok(summary);
    }

    /**
     * Get this month's summary
     * <p>
     * GET /api/analytics/summary/month
     */
    @GetMapping("/summary/month")
    @Operation(summary = "Get monthly summary",
            description = "Get this month's workout summary with comparison to last month")
    public ResponseEntity<Map<String, Object>> getMonthlySummary(
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> summary = analyticsService.getMonthlySummary(userDetails.getUsername());
        return ResponseEntity.ok(summary);
    }

    /**
     * Get this year's summary
     * <p>
     * GET /api/analytics/summary/year
     */
    @GetMapping("/summary/year")
    @Operation(summary = "Get yearly summary",
            description = "Get this year's workout summary with comparison to last year")
    public ResponseEntity<Map<String, Object>> getYearlySummary(
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> summary = analyticsService.getYearlySummary(userDetails.getUsername());
        return ResponseEntity.ok(summary);
    }

    /**
     * Get all-time summary
     * <p>
     * GET /api/analytics/summary/all-time
     */
    @GetMapping("/summary/all-time")
    @Operation(summary = "Get all-time summary",
            description = "Get lifetime workout summary")
    public ResponseEntity<Map<String, Object>> getAllTimeSummary(
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> summary = analyticsService.getAllTimeSummary(userDetails.getUsername());
        return ResponseEntity.ok(summary);
    }

    // ==================== PERSONAL RECORDS ====================

    /**
     * Get recent personal records
     * <p>
     * GET /api/analytics/personal-records/recent?days=30
     */
    @GetMapping("/personal-records/recent")
    @Operation(summary = "Get recent personal records",
            description = "Get recent PRs from the last N days")
    public ResponseEntity<List<Map<String, Object>>> getRecentPersonalRecords(
            @Parameter(description = "Number of days to look back")
            @RequestParam(defaultValue = "30") int days,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Getting recent PRs ({} days) for user: {}", days, userDetails.getUsername());
        List<Map<String, Object>> prs = analyticsService.getRecentPersonalRecords(
                userDetails.getUsername(), days);
        return ResponseEntity.ok(prs);
    }

    /**
     * Get all-time personal records
     * <p>
     * GET /api/analytics/personal-records/all-time
     */
    @GetMapping("/personal-records/all-time")
    @Operation(summary = "Get all-time personal records",
            description = "Get all-time PRs across all exercises")
    public ResponseEntity<List<Map<String, Object>>> getAllTimePersonalRecords(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Getting all-time PRs for user: {}", userDetails.getUsername());
        List<Map<String, Object>> prs = analyticsService.getAllTimePersonalRecords(userDetails.getUsername());
        return ResponseEntity.ok(prs);
    }

    // ==================== TOP EXERCISES ====================

    /**
     * Get top exercises for a specific time period
     * <p>
     * GET /api/analytics/top-exercises?period=WEEK&limit=5
     */
    @GetMapping("/top-exercises")
    @Operation(summary = "Get top exercises",
            description = "Get most frequently used exercises for a time period")
    public ResponseEntity<List<Map<String, Object>>> getTopExercises(
            @Parameter(description = "Time period: WEEK, MONTH, YEAR, ALL_TIME")
            @RequestParam(defaultValue = "WEEK") AnalyticsService.TimePeriod period,
            @Parameter(description = "Number of exercises to return")
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Getting top {} exercises for period {} for user: {}",
                limit, period, userDetails.getUsername());
        List<Map<String, Object>> topExercises = analyticsService.getTopExercises(
                userDetails.getUsername(), period, limit);
        return ResponseEntity.ok(topExercises);
    }

    // ==================== EXERCISE PROGRESSION ====================

    /**
     * Get progression trend for specific exercise
     * <p>
     * GET /api/analytics/exercise/{exerciseId}/progression?weeks=12
     */
    @GetMapping("/exercise/{exerciseId}/progression")
    @Operation(summary = "Get exercise progression",
            description = "Get progression trend for a specific exercise over N weeks")
    public ResponseEntity<List<Map<String, Object>>> getExerciseProgression(
            @Parameter(description = "Exercise ID")
            @PathVariable Long exerciseId,
            @Parameter(description = "Number of weeks to analyze")
            @RequestParam(defaultValue = "12") int weeks,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Getting {}-week progression for exercise {} for user: {}",
                weeks, exerciseId, userDetails.getUsername());
        List<Map<String, Object>> progression = analyticsService.getExerciseProgression(
                userDetails.getUsername(), exerciseId, weeks);
        return ResponseEntity.ok(progression);
    }
}
