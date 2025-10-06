package com.chidituke.workout_tracker.controller.workout;

import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.workout.PerformanceRecord;
import com.chidituke.workout_tracker.model.workout.WorkoutSession;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import com.chidituke.workout_tracker.repository.workout.WorkoutSessionRepository;
import com.chidituke.workout_tracker.service.workout.CalorieCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for calorie tracking and analytics
 */
@Slf4j
@RestController
@RequestMapping("/api/calories")
@RequiredArgsConstructor
public class CalorieController {

    private final CalorieCalculationService calorieCalculationService;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final UserRepository userRepository;

    /**
     * Get calorie breakdown for a workout session
     * <p>
     * GET /api/calories/session/{sessionId}
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSessionCalories(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Getting calorie breakdown for session: {} by user: {}", sessionId, userDetails.getUsername());

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        WorkoutSession session = workoutSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Workout session not found"));

        // Security check
        if (!session.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        // Build response with calorie breakdown
        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", session.getId());
        response.put("totalCaloriesCalculated", session.getTotalCaloriesCalculated());
        response.put("actualCaloriesBurned", session.getActualCaloriesBurned());
        response.put("userReportedCalories", session.getUserReportedCalories());
        response.put("calculationStatus", session.getCalorieCalculationStatus());
        response.put("calorieUnit", user.getPreferredCalorieUnit());

        // Add per-exercise breakdown
        List<Map<String, Object>> exerciseBreakdown = session.getPerformanceRecords().stream()
                .map(record -> {
                    Map<String, Object> exerciseData = new HashMap<>();
                    exerciseData.put("exerciseName", record.getExercise().getExerciseName());
                    exerciseData.put("setNumber", record.getSetNumber());
                    exerciseData.put("calories", record.getCaloriesBurned());
                    exerciseData.put("metValue", record.getMetValueUsed());
                    exerciseData.put("intensity", record.getIntensityLevel());
                    exerciseData.put("perceivedExertion", record.getPerceivedExertion());
                    return exerciseData;
                })
                .toList();

        response.put("exerciseBreakdown", exerciseBreakdown);

        // Add formatted display string
        if (session.getTotalCaloriesCalculated() != null) {
            String formatted = calorieCalculationService.getCaloriesInPreferredUnit(
                    session.getTotalCaloriesCalculated(), user);
            response.put("formattedCalories", formatted);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Recalculate calories for a workout session
     * Useful if user updates their weight or exercise MET values change
     * <p>
     * POST /api/calories/session/{sessionId}/recalculate
     */
    @PostMapping("/session/{sessionId}/recalculate")
    public ResponseEntity<Map<String, Object>> recalculateSessionCalories(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Recalculating calories for session: {} by user: {}", sessionId, userDetails.getUsername());

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        WorkoutSession session = workoutSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Workout session not found"));

        // Security check
        if (!session.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        // Recalculate all performance records
        for (PerformanceRecord record : session.getPerformanceRecords()) {
            calorieCalculationService.calculateCalories(record, user);
        }

        // Update session totals
        Integer totalCalories = calorieCalculationService.calculateSessionCalories(session, user);
        workoutSessionRepository.save(session);

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", sessionId);
        response.put("totalCalories", totalCalories);
        response.put("status", "recalculated");

        log.info("Recalculated {} calories for session {}", totalCalories, sessionId);
        return ResponseEntity.ok(response);
    }

    /**
     * Submit user feedback on calorie accuracy
     * Helps improve future calculations
     * <p>
     * POST /api/calories/session/{sessionId}/feedback
     */
    @PostMapping("/session/{sessionId}/feedback")
    public ResponseEntity<Map<String, String>> submitCalorieFeedback(
            @PathVariable Long sessionId,
            @RequestBody Map<String, Object> feedback,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Submitting calorie feedback for session: {} by user: {}", sessionId, userDetails.getUsername());

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        WorkoutSession session = workoutSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Workout session not found"));

        // Security check
        if (!session.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        // Extract feedback data
        Integer accuracyRating = feedback.get("accuracyRating") != null ?
                ((Number) feedback.get("accuracyRating")).intValue() : null;
        Integer userReportedCalories = feedback.get("userReportedCalories") != null ?
                ((Number) feedback.get("userReportedCalories")).intValue() : null;

        // Update session with feedback
        if (accuracyRating != null) {
            session.setCalorieAccuracyRating(accuracyRating);
        }
        if (userReportedCalories != null) {
            session.setUserReportedCalories(userReportedCalories);
            session.setActualCaloriesBurned(userReportedCalories);
        }

        workoutSessionRepository.save(session);

        Map<String, String> response = new HashMap<>();
        response.put("status", "feedback_recorded");
        response.put("message", "Thank you for your feedback!");

        log.info("Recorded calorie feedback for session {}: rating={}, reported={}",
                sessionId, accuracyRating, userReportedCalories);

        return ResponseEntity.ok(response);
    }

    /**
     * Get user's calorie tracking preferences
     * <p>
     * GET /api/calories/preferences
     */
    @GetMapping("/preferences")
    public ResponseEntity<Map<String, Object>> getCaloriePreferences(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> preferences = new HashMap<>();
        preferences.put("calorieTrackingEnabled", user.getCalorieTrackingEnabled());
        preferences.put("preferredCalorieUnit", user.getPreferredCalorieUnit());
        preferences.put("calorieGoalDaily", user.getCalorieGoalDaily());
        preferences.put("calorieAdjustmentFactor", user.getCalorieAdjustmentFactor());

        return ResponseEntity.ok(preferences);
    }

    /**
     * Update user's calorie tracking preferences
     * <p>
     * PUT /api/calories/preferences
     */
    @PutMapping("/preferences")
    public ResponseEntity<Map<String, String>> updateCaloriePreferences(
            @RequestBody Map<String, Object> preferences,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Updating calorie preferences for user: {}", userDetails.getUsername());

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update preferences
        if (preferences.containsKey("calorieTrackingEnabled")) {
            user.setCalorieTrackingEnabled((Boolean) preferences.get("calorieTrackingEnabled"));
        }
        if (preferences.containsKey("preferredCalorieUnit")) {
            user.setPreferredCalorieUnit((String) preferences.get("preferredCalorieUnit"));
        }
        if (preferences.containsKey("calorieGoalDaily")) {
            user.setCalorieGoalDaily(((Number) preferences.get("calorieGoalDaily")).intValue());
        }
        if (preferences.containsKey("calorieAdjustmentFactor")) {
            Double factor = ((Number) preferences.get("calorieAdjustmentFactor")).doubleValue();
            // Validate adjustment factor is reasonable (0.5 to 1.5)
            if (factor >= 0.5 && factor <= 1.5) {
                user.setCalorieAdjustmentFactor(factor);
            }
        }

        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("status", "preferences_updated");

        log.info("Updated calorie preferences for user: {}", userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Get calorie statistics for a date range
     * <p>
     * GET /api/calories/stats?startDate=2025-01-01&endDate=2025-01-31
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCalorieStats(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Getting calorie stats for user: {} from {} to {}",
                userDetails.getUsername(), startDate, endDate);

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Query sessions in date range
        List<WorkoutSession> sessions = workoutSessionRepository
                .findByUserAndDateBetween(user,
                        java.time.LocalDate.parse(startDate),
                        java.time.LocalDate.parse(endDate));

        // Calculate statistics
        int totalCalories = sessions.stream()
                .mapToInt(s -> s.getTotalCaloriesCalculated() != null ? s.getTotalCaloriesCalculated() : 0)
                .sum();

        int workoutCount = sessions.size();
        int averagePerWorkout = workoutCount > 0 ? totalCalories / workoutCount : 0;

        // Find highest calorie workout
        WorkoutSession maxSession = sessions.stream()
                .max((s1, s2) -> {
                    int cal1 = s1.getTotalCaloriesCalculated() != null ? s1.getTotalCaloriesCalculated() : 0;
                    int cal2 = s2.getTotalCaloriesCalculated() != null ? s2.getTotalCaloriesCalculated() : 0;
                    return Integer.compare(cal1, cal2);
                })
                .orElse(null);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCalories", totalCalories);
        stats.put("workoutCount", workoutCount);
        stats.put("averagePerWorkout", averagePerWorkout);
        stats.put("highestCalorieWorkout", maxSession != null ? maxSession.getTotalCaloriesCalculated() : 0);
        stats.put("dailyGoal", user.getCalorieGoalDaily());

        // Calculate goal progress if daily goal is set
        if (user.getCalorieGoalDaily() != null && user.getCalorieGoalDaily() > 0) {
            long dayCount = java.time.temporal.ChronoUnit.DAYS.between(
                    java.time.LocalDate.parse(startDate),
                    java.time.LocalDate.parse(endDate)) + 1;
            int totalGoal = user.getCalorieGoalDaily() * (int) dayCount;
            double progressPercent = (double) totalCalories / totalGoal * 100;
            stats.put("goalProgress", Math.round(progressPercent));
        }

        return ResponseEntity.ok(stats);
    }
}