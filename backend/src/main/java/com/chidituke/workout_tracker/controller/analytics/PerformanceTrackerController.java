//package com.chidituke.workout_tracker.controller.analytics;
//
//import com.chidituke.workout_tracker.dto.response.analytics.PerformanceTrackerResponse;
//import com.chidituke.workout_tracker.service.analytics.PerformanceTrackerService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/analytics/performance-tracker")
//@RequiredArgsConstructor
//@Slf4j
//@Tag(name = "Performance Tracker", description = "Performance metrics over time for analytics charts")
//public class PerformanceTrackerController {
//
//    private final PerformanceTrackerService performanceTrackerService;
//
//    /**
//     * Get performance data for a specific metric and time period
//     * <p>
//     * GET /api/analytics/performance-tracker?metric=WEIGHT&period=WEEK
//     */
//    @GetMapping
//    @Operation(summary = "Get performance tracker data",
//            description = "Get chart data for a specific metric over a time period")
//    public ResponseEntity<PerformanceTrackerResponse> getPerformanceData(
//            @RequestParam String metric,
//            @RequestParam String period,
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        log.info("Getting performance data for user: {}, metric: {}, period: {}",
//                userDetails.getUsername(), metric, period);
//
//        PerformanceTrackerResponse data = performanceTrackerService.getPerformanceData(
//                userDetails.getUsername(),
//                metric,
//                period
//        );
//
//        return ResponseEntity.ok(data);
//    }
//}