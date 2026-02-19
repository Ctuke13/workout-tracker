package com.chidituke.workout_tracker.controller.stats;

import com.chidituke.workout_tracker.dto.request.user.UserGoalRequest;
import com.chidituke.workout_tracker.dto.response.stats.WeeklyStatsResponse;
import com.chidituke.workout_tracker.dto.response.user.UserGoalResponse;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.service.stats.WeeklyStatsService;
import com.chidituke.workout_tracker.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST Controller for weekly workout statistics and goal management
 * <p>
 * Endpoints:
 * - GET  /api/user/stats/weekly - Get this week's progress
 * - GET  /api/user/goal        - Get current goal settings
 * - PUT  /api/user/goal        - Update weekly workout goal
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class WeeklyStatsController {

    private final WeeklyStatsService weeklyStatsService;
    private final UserService userService;

    /**
     * GET /api/user/stats/weekly
     * <p>
     * Get user's weekly workout statistics
     *
     * @param authentication Current authenticated user
     * @return WeeklyStatsResponse with workouts, XP, streak, and goal progress
     */
    @GetMapping("/stats/weekly")
    public ResponseEntity<WeeklyStatsResponse> getWeeklyStats(Authentication authentication) {
        log.info("GET /api/user/stats/weekly - User: {}", authentication.getName());

        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        WeeklyStatsResponse stats = weeklyStatsService.getWeeklyStats(user);

        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/user/goal
     * <p>
     * Get user's current goal settings
     *
     * @param authentication Current authenticated user
     * @return UserGoalResponse with current goal configuration
     */
    @GetMapping("/goal")
    public ResponseEntity<UserGoalResponse> getGoal(Authentication authentication) {
        log.info("GET /api/user/goal - User: {}", authentication.getName());

        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserGoalResponse response = UserGoalResponse.builder()
                .weeklyWorkoutGoal(user.getWeeklyWorkoutGoal())
                .goalType(user.getGoalType() != null ? user.getGoalType() : "workouts")
                .hasGoalSet(user.getWeeklyWorkoutGoal() != null && user.getWeeklyWorkoutGoal() > 0)
                .goalLevel(determineGoalLevel(user.getWeeklyWorkoutGoal()))
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/user/goal
     * <p>
     * Update user's weekly workout goal
     *
     * @param request        UserGoalRequest with new goal settings
     * @param authentication Current authenticated user
     * @return Updated UserGoalResponse
     */
    @PutMapping("/goal")
    public ResponseEntity<UserGoalResponse> updateGoal(
            @Valid @RequestBody UserGoalRequest request,
            Authentication authentication) {

        log.info("PUT /api/user/goal - User: {}, New goal: {}",
                authentication.getName(), request.getWeeklyWorkoutGoal());

        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update user's goal
        user.setWeeklyWorkoutGoal(request.getWeeklyWorkoutGoal());
        if (request.getGoalType() != null) {
            user.setGoalType(request.getGoalType());
        }

        // Save updated user
        User updatedUser = userService.updateUser(user);

        // Build response
        UserGoalResponse response = UserGoalResponse.builder()
                .weeklyWorkoutGoal(updatedUser.getWeeklyWorkoutGoal())
                .goalType(updatedUser.getGoalType() != null ? updatedUser.getGoalType() : "workouts")
                .hasGoalSet(updatedUser.getWeeklyWorkoutGoal() != null && updatedUser.getWeeklyWorkoutGoal() > 0)
                .goalLevel(determineGoalLevel(updatedUser.getWeeklyWorkoutGoal()))
                .build();

        log.info("Goal updated successfully for user: {}", authentication.getName());

        return ResponseEntity.ok(response);
    }

    /**
     * Determine goal level based on weekly workout count
     */
    private String determineGoalLevel(Integer weeklyGoal) {
        if (weeklyGoal == null || weeklyGoal == 0) {
            return null;
        }

        if (weeklyGoal <= 3) {
            return "beginner"; // 2-3 workouts/week
        } else if (weeklyGoal <= 4) {
            return "regular"; // 3-4 workouts/week
        } else {
            return "dedicated"; // 5-6+ workouts/week
        }
    }
}