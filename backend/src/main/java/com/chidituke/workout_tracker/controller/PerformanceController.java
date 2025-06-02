package com.chidituke.workout_tracker.controller;

import com.chidituke.workout_tracker.dto.PerformanceRequest;
import com.chidituke.workout_tracker.dto.PerformanceResponse;
import com.chidituke.workout_tracker.service.PerformanceService;
import com.chidituke.workout_tracker.service.SubscriptionService;
import com.chidituke.workout_tracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    @Autowired
    private PerformanceService performanceService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UserService userService;

    /**
     * Get performance records for a specific workout log
     * GET /api/performance/workout-log/{workoutLogId}
     */
    @GetMapping("/workout-log/{workoutLogId}")
    public ResponseEntity<List<PerformanceResponse>> getPerformanceByWorkoutLog(
            @PathVariable Long workoutLogId,
            Authentication authentication) {

        String username = authentication.getName();
        List<PerformanceResponse> performances = performanceService.getPerformanceByWorkoutLog(workoutLogId, username);
        return ResponseEntity.ok(performances);
    }

    /**
     * Get user performance history for a specific workout
     * GET /api/performance/workout/{workoutId}
     */
    @GetMapping("/workout/{workoutId}")
    public ResponseEntity<List<PerformanceResponse>> getUserPerformanceByWorkout(
            @PathVariable Long workoutId,
            Authentication authentication) {

        String username = authentication.getName();
        List<PerformanceResponse> performances = performanceService.getUserPerformanceByWorkout(username, workoutId);
        return ResponseEntity.ok(performances);
    }

    /**
     * Create a new performance record
     * POST /api/performance
     */
    @PostMapping
    public ResponseEntity<PerformanceResponse> createPerformance(
            @RequestBody @Valid PerformanceRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        PerformanceResponse performance = performanceService.createPerformance(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(performance);
    }

    /**
     * Update an existing performance record
     * PUT /api/performance/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<PerformanceResponse> updatePerformance(
            @PathVariable Long id,
            @RequestBody @Valid PerformanceRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        Optional<PerformanceResponse> updatedPerformance = performanceService.updatePerformance(id, username, request);
        return updatedPerformance.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a performance record
     * DELETE /api/performance/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerformance(
            @PathVariable Long id,
            Authentication authentication) {

        String username = authentication.getName();
        boolean deleted = performanceService.deletePerformance(id, username);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    /**
     * Get maximum weight lifted for a specific workout
     * GET /api/performance/analytics/max-weight/{workoutId}
     */
    @GetMapping("/analytics/max-weight/{workoutId}")
    public ResponseEntity<?> getMaxWeightForWorkout(
            @PathVariable Long workoutId,
            Authentication authentication) {

        String username = authentication.getName();

        if(!subscriptionService.isPlusTier(getUserIdFromAuth(authentication))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("🔒 Upgrade to PLUS for advanced analytics features!");
        }
        Double maxWeight = performanceService.getMaxWeightForWorkout(username, workoutId);
        return ResponseEntity.ok(maxWeight != null ? maxWeight : 0.0);
    }

    /**
     * Get total volume (weight × reps) for a date range
     * GET /api/performance/analytics/volume?startDate=2024-01-01&endDate=2024-01-31
     */
    @GetMapping("/analytics/volume")
    public ResponseEntity<?> getTotalVolumeForDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        String username = authentication.getName();

        if (!subscriptionService.isPlusTier(getUserIdFromAuth(authentication))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("🔒 Upgrade to PLUS for advanced analytics features!");
        }

        Double totalVolume = performanceService.getTotalVolumeForDateRange(username, startDate, endDate);
        return ResponseEntity.ok(totalVolume != null ? totalVolume : 0.0);
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        String username = authentication.getName();
        return userService.getUserIdByUsername(username);
    }
}