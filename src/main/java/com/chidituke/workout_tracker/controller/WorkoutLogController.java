package com.chidituke.workout_tracker.controller;

import com.chidituke.workout_tracker.dto.WorkoutLogRequest;
import com.chidituke.workout_tracker.dto.WorkoutLogResponse;
import com.chidituke.workout_tracker.service.WorkoutLogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/workout-logs")
public class WorkoutLogController {

    @Autowired
    private WorkoutLogService workoutLogService;

    /**
     * Start a new workout session
     * POST /api/workout-logs
     */
    @PostMapping
    public ResponseEntity<WorkoutLogResponse> startWorkoutSession(
            @RequestBody @Valid WorkoutLogRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        WorkoutLogResponse workoutLog = workoutLogService.startWorkoutSession(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutLog);
    }

    /**
     * Get all workout history for current user
     * GET /api/workout-logs
     */
    @GetMapping
    public ResponseEntity<List<WorkoutLogResponse>> getWorkoutHistory(Authentication authentication) {
        String username = authentication.getName();
        List<WorkoutLogResponse> workoutLogs = workoutLogService.getUserWorkoutHistory(username);
        return ResponseEntity.ok(workoutLogs);
    }

    /**
     * Get recent workout logs
     * GET /api/workout-logs/recent?limit=10
     */
    @GetMapping("/recent")
    public ResponseEntity<List<WorkoutLogResponse>> getRecentWorkoutLogs(
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {

        String username = authentication.getName();
        List<WorkoutLogResponse> workoutLogs = workoutLogService.getRecentWorkoutLogs(username, limit);
        return ResponseEntity.ok(workoutLogs);
    }

    /**
     * Get specific workout log by ID
     * GET /api/workout-logs/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<WorkoutLogResponse> getWorkoutLogById(
            @PathVariable Long id,
            Authentication authentication) {

        String username = authentication.getName();
        Optional<WorkoutLogResponse> workoutLog = workoutLogService.getWorkoutLogById(id, username);
        return workoutLog.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update workout log
     * PUT /api/workout-logs/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<WorkoutLogResponse> updateWorkoutLog(
            @PathVariable Long id,
            @RequestBody @Valid WorkoutLogRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        Optional<WorkoutLogResponse> updatedWorkoutLog = workoutLogService.updateWorkoutLog(id, username, request);
        return updatedWorkoutLog.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete workout log
     * DELETE /api/workout-logs/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkoutLog(
            @PathVariable Long id,
            Authentication authentication) {

        String username = authentication.getName();
        boolean deleted = workoutLogService.deleteWorkoutLog(id, username);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    /**
     * Get workout statistics
     * GET /api/workout-logs/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Long> getWorkoutStats(Authentication authentication) {
        String username = authentication.getName();
        long totalWorkouts = workoutLogService.getTotalWorkoutCount(username);
        return ResponseEntity.ok(totalWorkouts);
    }
}
