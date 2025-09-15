package com.chidituke.workout_tracker.controller.exercise;

import com.chidituke.workout_tracker.controller.BaseApiController;
import com.chidituke.workout_tracker.dto.response.exercise.ExerciseFiltersDTO;
import com.chidituke.workout_tracker.dto.response.exercise.ExerciseResponseDTO;
import com.chidituke.workout_tracker.model.workout.Exercise;
import com.chidituke.workout_tracker.security.CurrentUser;
import com.chidituke.workout_tracker.security.UserPrincipal;
import com.chidituke.workout_tracker.service.exercise.ExerciseQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/exercises/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@Tag(name = "Exercise Analytics", description = "Professional analytics and exercise insights")
public class ExerciseAnalyticsController extends BaseApiController {

    private final ExerciseQueryService exerciseQueryService;

    // ===================================================================
    // POPULAR EXERCISES & TRENDING
    // ===================================================================

    @GetMapping("/popular")
    @Operation(summary = "Get popular exercises", description = "Get most popular exercises by usage with professional insights")
    public ResponseEntity<List<ExerciseResponseDTO>> getPopularExercises(
            @Parameter(description = "Maximum number of exercises to return")
            @RequestParam(defaultValue = "10") int limit) {

        try {
            List<Exercise> popularExercises = exerciseQueryService.findMostPopular(limit);
            List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(popularExercises);

            log.debug("Retrieved {} popular exercises for analytics", response.size());
            return okList(response);

        } catch (Exception e) {
            log.error("Failed to get popular exercises for analytics", e);
            return okList(Collections.emptyList());
        }
    }

    @GetMapping("/trending")
    @Operation(summary = "Get trending exercises", description = "Get exercises with increasing popularity")
    public ResponseEntity<List<ExerciseResponseDTO>> getTrendingExercises(
            @Parameter(description = "Number of days to analyze for trending")
            @RequestParam(defaultValue = "7") int days,
            @Parameter(description = "Maximum number of exercises to return")
            @RequestParam(defaultValue = "15") int limit) {

        try {
            // For now, return most popular as a placeholder
            // TODO: Implement actual trending logic based on usage growth rate
            List<Exercise> trendingExercises = exerciseQueryService.findMostPopular(limit);
            List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(trendingExercises);

            log.debug("Retrieved {} trending exercises for {} days", response.size(), days);
            return okList(response);

        } catch (Exception e) {
            log.error("Failed to get trending exercises", e);
            return okList(Collections.emptyList());
        }
    }

    // ===================================================================
    // FILTER ANALYTICS & DISTRIBUTION
    // ===================================================================

    @GetMapping("/filters/counts")
    @Cacheable(value = "exercise-filters-with-counts", unless = "#result.body == null")
    @Operation(summary = "Get filters with counts", description = "Get filter options with detailed exercise counts for analytics")
    public ResponseEntity<ExerciseFiltersDTO> getAvailableFiltersWithCounts() {
        try {
            ExerciseFiltersDTO filters = exerciseQueryService.getAvailableFiltersWithCounts();

            log.debug("Retrieved filter analytics with counts");
            return ok(filters);

        } catch (Exception e) {
            log.error("Failed to get filter analytics", e);
            return ok(ExerciseFiltersDTO.createDefault());
        }
    }

    @GetMapping("/distribution/types")
    @Operation(summary = "Get exercise type distribution", description = "Get detailed breakdown of exercises by type")
    public ResponseEntity<Map<String, Object>> getExerciseTypeDistribution() {
        try {
            List<Object[]> typeCounts = exerciseQueryService.getExerciseTypeCounts();

            Map<String, Object> distribution = new HashMap<>();
            List<Map<String, Object>> typeBreakdown = new ArrayList<>();
            int totalExercises = 0;

            for (Object[] typeCount : typeCounts) {
                Exercise.ExerciseType type = (Exercise.ExerciseType) typeCount[0];
                Integer count = ((Number) typeCount[1]).intValue();
                totalExercises += count;

                Map<String, Object> typeInfo = new HashMap<>();
                typeInfo.put("type", type.name());
                typeInfo.put("displayName", type.getDisplayName());
                typeInfo.put("count", count);
                typeBreakdown.add(typeInfo);
            }

            // Calculate percentages
            final int finalTotal = totalExercises;
            typeBreakdown.forEach(typeInfo -> {
                Integer count = (Integer) typeInfo.get("count");
                double percentage = finalTotal > 0 ? (count * 100.0 / finalTotal) : 0.0;
                typeInfo.put("percentage", Math.round(percentage * 100.0) / 100.0);
            });

            distribution.put("breakdown", typeBreakdown);
            distribution.put("totalExercises", totalExercises);
            distribution.put("typeCount", typeBreakdown.size());

            log.debug("Retrieved exercise type distribution: {} total exercises across {} types",
                    totalExercises, typeBreakdown.size());
            return ok(distribution);

        } catch (Exception e) {
            log.error("Failed to get exercise type distribution", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve distribution data");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/distribution/difficulty")
    @Operation(summary = "Get difficulty distribution", description = "Get breakdown of exercises by difficulty level")
    public ResponseEntity<Map<String, Object>> getDifficultyDistribution() {
        try {
            ExerciseFiltersDTO filters = exerciseQueryService.getAvailableFiltersWithCounts();

            Map<String, Object> distribution = new HashMap<>();
            List<Map<String, Object>> difficultyBreakdown = new ArrayList<>();
            long totalExercises = 0;

            for (ExerciseFiltersDTO.DifficultyLevelDTO difficulty : filters.getDifficultyLevels()) {
                totalExercises += difficulty.getCount();

                Map<String, Object> difficultyInfo = new HashMap<>();
                difficultyInfo.put("level", difficulty.getValue());
                difficultyInfo.put("description", difficulty.getDescription());
                difficultyInfo.put("count", difficulty.getCount());
                difficultyBreakdown.add(difficultyInfo);
            }

            // Calculate percentages
            final long finalTotal = totalExercises;
            difficultyBreakdown.forEach(difficultyInfo -> {
                Long count = (Long) difficultyInfo.get("count");
                double percentage = finalTotal > 0 ? (count * 100.0 / finalTotal) : 0.0;
                difficultyInfo.put("percentage", Math.round(percentage * 100.0) / 100.0);
            });

            distribution.put("breakdown", difficultyBreakdown);
            distribution.put("totalExercises", totalExercises);
            distribution.put("difficultyLevels", difficultyBreakdown.size());

            log.debug("Retrieved difficulty distribution: {} total exercises across {} levels",
                    totalExercises, difficultyBreakdown.size());
            return ok(distribution);

        } catch (Exception e) {
            log.error("Failed to get difficulty distribution", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve difficulty distribution");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ===================================================================
    // GOAL-BASED ANALYTICS
    // ===================================================================

    @GetMapping("/goals")
    @Operation(summary = "Get exercise goals analytics", description = "Get available fitness goals with detailed counts and insights")
    public ResponseEntity<List<Map<String, Object>>> getGoalsAnalytics() {
        try {
            List<Object[]> typeCounts = exerciseQueryService.getExerciseTypeCounts();

            Map<String, Integer> goalCounts = initializeGoalCounts();
            aggregateTypeCountsToGoals(typeCounts, goalCounts);

            List<Map<String, Object>> goals = goalCounts.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> goal = new HashMap<>();
                        goal.put("goal", entry.getKey());
                        goal.put("displayName", getGoalDisplayName(entry.getKey()));
                        goal.put("count", entry.getValue());
                        goal.put("description", getGoalDescription(entry.getKey()));
                        return goal;
                    })
                    .sorted((g1, g2) -> Integer.compare((Integer) g2.get("count"), (Integer) g1.get("count")))
                    .collect(Collectors.toList());

            log.debug("Retrieved goals analytics: {} goal categories", goals.size());
            return okList(goals);

        } catch (Exception e) {
            log.error("Failed to get goals analytics", e);
            return okList(Collections.emptyList());
        }
    }

    @GetMapping("/goals/{goalType}/exercises")
    @Operation(summary = "Get exercises by goal", description = "Get detailed exercise list for a specific fitness goal")
    public ResponseEntity<List<ExerciseResponseDTO>> getExercisesByGoal(
            @Parameter(description = "Goal type (fat-burn, muscle-building, endurance, etc.)")
            @PathVariable String goalType,
            @Parameter(description = "Maximum number of exercises to return")
            @RequestParam(defaultValue = "20") int limit) {

        try {
            Exercise.ExerciseType mappedType = mapGoalToExerciseType(goalType);
            if (mappedType == null) {
                log.warn("Invalid goal type requested: {}", goalType);
                return okList(Collections.emptyList());
            }

            List<Exercise> exercises = exerciseQueryService.findExercisesForWorkoutType(mappedType);
            List<ExerciseResponseDTO> response = exercises.stream()
                    .limit(limit)
                    .map(ExerciseResponseDTO::fromEntity)
                    .collect(Collectors.toList());

            log.debug("Retrieved {} exercises for goal: {}", response.size(), goalType);
            return okList(response);

        } catch (Exception e) {
            log.error("Failed to get exercises for goal: {}", goalType, e);
            return okList(Collections.emptyList());
        }
    }

    // ===================================================================
    // PROFESSIONAL INSIGHTS
    // ===================================================================

    @GetMapping("/insights/professional")
    @PreAuthorize("hasRole('PROFESSIONAL') or hasRole('ADMIN')")
    @Operation(summary = "Get professional insights", description = "Get advanced analytics for fitness professionals")
    public ResponseEntity<Map<String, Object>> getProfessionalInsights(
            @CurrentUser UserPrincipal userPrincipal) {

        try {
            Map<String, Object> insights = new HashMap<>();

            // Exercise library overview
            List<Exercise> professionalExercises = exerciseQueryService.findProfessionalExercises();
            List<Exercise> popularExercises = exerciseQueryService.findMostPopular(10);

            insights.put("professionalExerciseCount", professionalExercises.size());
            insights.put("topPerformingExercises", ExerciseResponseDTO.fromEntityList(popularExercises));

            // Type distribution for professional content
            List<Object[]> typeCounts = exerciseQueryService.getExerciseTypeCounts();
            insights.put("exerciseTypeDistribution", createTypeDistributionMap(typeCounts));

            // Professional content performance
            Map<String, Object> professionalMetrics = new HashMap<>();
            professionalMetrics.put("totalProfessionalExercises", professionalExercises.size());
            professionalMetrics.put("averageRating", calculateAverageRating(professionalExercises));
            professionalMetrics.put("totalUsage", calculateTotalUsage(professionalExercises));
            insights.put("professionalMetrics", professionalMetrics);

            log.debug("Generated professional insights for user {}", userPrincipal.getId());
            return ok(insights);

        } catch (Exception e) {
            log.error("Failed to generate professional insights for user {}", userPrincipal.getId(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to generate professional insights");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/insights/usage-patterns")
    @PreAuthorize("hasRole('PROFESSIONAL') or hasRole('ADMIN')")
    @Operation(summary = "Get usage patterns", description = "Analyze exercise usage patterns for insights")
    public ResponseEntity<Map<String, Object>> getUsagePatterns() {
        try {
            Map<String, Object> patterns = new HashMap<>();

            // Get popular exercises and analyze patterns
            List<Exercise> popularExercises = exerciseQueryService.findMostPopular(50);

            // Analyze by difficulty
            Map<String, Long> difficultyUsage = popularExercises.stream()
                    .collect(Collectors.groupingBy(
                            exercise -> exercise.getDifficultyLevel().name(),
                            Collectors.summingLong(exercise -> exercise.getUsageCount() != null ? exercise.getUsageCount() : 0L)
                    ));

            // Analyze by type
            Map<String, Long> typeUsage = popularExercises.stream()
                    .collect(Collectors.groupingBy(
                            exercise -> exercise.getExerciseType().name(),
                            Collectors.summingLong(exercise -> exercise.getUsageCount() != null ? exercise.getUsageCount() : 0L)
                    ));

            patterns.put("usageByDifficulty", difficultyUsage);
            patterns.put("usageByType", typeUsage);
            patterns.put("totalExercisesAnalyzed", popularExercises.size());
            patterns.put("analysisDate", java.time.LocalDateTime.now());

            log.debug("Generated usage patterns analysis");
            return ok(patterns);

        } catch (Exception e) {
            log.error("Failed to generate usage patterns", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to analyze usage patterns");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ===================================================================
    // HELPER METHODS
    // ===================================================================

    private Map<String, Integer> initializeGoalCounts() {
        Map<String, Integer> goalCounts = new HashMap<>();
        goalCounts.put("fat-burn", 0);
        goalCounts.put("muscle-building", 0);
        goalCounts.put("endurance", 0);
        goalCounts.put("flexibility", 0);
        goalCounts.put("sport-specific", 0);
        goalCounts.put("recovery", 0);
        return goalCounts;
    }

    private void aggregateTypeCountsToGoals(List<Object[]> typeCounts, Map<String, Integer> goalCounts) {
        for (Object[] typeCount : typeCounts) {
            Exercise.ExerciseType type = (Exercise.ExerciseType) typeCount[0];
            Integer count = ((Number) typeCount[1]).intValue();

            switch (type) {
                case CARDIO, PLYOMETRIC -> {
                    goalCounts.put("fat-burn", goalCounts.get("fat-burn") + count);
                    goalCounts.put("endurance", goalCounts.get("endurance") + count);
                }
                case STRENGTH -> goalCounts.put("muscle-building", goalCounts.get("muscle-building") + count);
                case FLEXIBILITY -> goalCounts.put("flexibility", goalCounts.get("flexibility") + count);
                case SPORTS_SPECIFIC -> goalCounts.put("sport-specific", goalCounts.get("sport-specific") + count);
                case REHABILITATION, BALANCE -> goalCounts.put("recovery", goalCounts.get("recovery") + count);
            }
        }
    }

    private Exercise.ExerciseType mapGoalToExerciseType(String goal) {
        return switch (goal) {
            case "fat-burn", "endurance" -> Exercise.ExerciseType.CARDIO;
            case "muscle-building" -> Exercise.ExerciseType.STRENGTH;
            case "flexibility" -> Exercise.ExerciseType.FLEXIBILITY;
            case "sport-specific" -> Exercise.ExerciseType.SPORTS_SPECIFIC;
            case "recovery" -> Exercise.ExerciseType.REHABILITATION;
            default -> null;
        };
    }

    private String getGoalDisplayName(String goal) {
        return switch (goal) {
            case "fat-burn" -> "Fat Burning";
            case "muscle-building" -> "Muscle Building";
            case "endurance" -> "Endurance Training";
            case "flexibility" -> "Flexibility & Mobility";
            case "sport-specific" -> "Sport-Specific Training";
            case "recovery" -> "Recovery & Rehabilitation";
            default -> goal;
        };
    }

    private String getGoalDescription(String goal) {
        return switch (goal) {
            case "fat-burn" -> "Cardiovascular exercises focused on calorie burn and weight management";
            case "muscle-building" -> "Strength training exercises for muscle growth and power development";
            case "endurance" -> "Aerobic exercises to improve cardiovascular fitness and stamina";
            case "flexibility" -> "Stretching and mobility exercises for improved range of motion";
            case "sport-specific" -> "Specialized exercises tailored for specific sports and activities";
            case "recovery" -> "Therapeutic exercises for injury prevention and rehabilitation";
            default -> "Fitness exercises for general health and wellness";
        };
    }

    private Map<String, Object> createTypeDistributionMap(List<Object[]> typeCounts) {
        Map<String, Object> distribution = new HashMap<>();

        for (Object[] typeCount : typeCounts) {
            Exercise.ExerciseType type = (Exercise.ExerciseType) typeCount[0];
            Integer count = ((Number) typeCount[1]).intValue();

            Map<String, Object> typeInfo = new HashMap<>();
            typeInfo.put("count", count);
            typeInfo.put("displayName", type.getDisplayName());

            distribution.put(type.name(), typeInfo);
        }

        return distribution;
    }

    private double calculateAverageRating(List<Exercise> exercises) {
        return exercises.stream()
                .filter(exercise -> exercise.getAverageRating() != null && exercise.getAverageRating() > 0)
                .mapToDouble(Exercise::getAverageRating)
                .average()
                .orElse(0.0);
    }

    private long calculateTotalUsage(List<Exercise> exercises) {
        return exercises.stream()
                .filter(exercise -> exercise.getUsageCount() != null)
                .mapToLong(Exercise::getUsageCount)
                .sum();
    }
}