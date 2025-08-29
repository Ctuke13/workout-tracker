package com.chidituke.workout_tracker.controller.workout;

import com.chidituke.workout_tracker.dto.request.exercise.*;
import com.chidituke.workout_tracker.dto.response.exercise.*;
import com.chidituke.workout_tracker.model.workout.Exercise;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.repository.workout.ExerciseRepository;
import com.chidituke.workout_tracker.security.CurrentUser;
import com.chidituke.workout_tracker.security.UserPrincipal;
import com.chidituke.workout_tracker.service.user.UserService;
import com.chidituke.workout_tracker.service.workout.ExerciseService;
import com.chidituke.workout_tracker.service.workout.ExerciseFavoritesService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@Tag(name = "Exercise Management", description = "Exercise library and management endpoints")
public class ExerciseController {

    private final ExerciseService exerciseService;
    private final UserService userService;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseFavoritesService favoritesService;

    // ===================================================================
    // 🌐 PUBLIC ENDPOINTS (Frontend Integration)
    // ===================================================================

    @GetMapping("/public")
    @Operation(summary = "Get public exercises", description = "Get exercises with filtering for frontend")
    public ResponseEntity<List<ExerciseResponseDTO>> getPublicExercises(
            @RequestParam(required = false) String goal,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String equipment) {

        log.debug("📋 Public frontend request - goal: {}, difficulty: {}, equipment: {}", goal, difficulty, equipment);

        ExerciseSearchRequestDTO searchRequest = buildSearchRequest(goal, difficulty, equipment, null);
        Pageable pageable = createPageable(searchRequest);

        Page<Exercise> exercisePage = exerciseService.searchExercises(
                searchRequest.getSearch(),
                searchRequest.getMuscleGroups(),
                searchRequest.getEquipment(),
                searchRequest.getDifficultyLevel(),
                pageable
        );

        List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(exercisePage.getContent());
        log.debug("✅ Returned {} public exercises", response.size());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/enhanced")
    @Operation(summary = "Get enhanced exercises", description = "Get exercises with favorite status for authenticated users")
    public ResponseEntity<List<Map<String, Object>>> getPublicExercisesWithFavorites(
            @RequestParam(required = false) String goal,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String equipment,
            @CurrentUser(required = false) UserPrincipal userPrincipal) {

        try {
            ResponseEntity<List<ExerciseResponseDTO>> exercisesResponse = getPublicExercises(goal, difficulty, equipment);
            List<ExerciseResponseDTO> exercises = exercisesResponse.getBody();

            if (exercises == null || exercises.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            // Make variables effectively final for lambda
            final Map<Long, Boolean> favoriteStatus;
            if (userPrincipal != null) {
                List<Long> exerciseIds = exercises.stream()
                        .map(ExerciseResponseDTO::getId)
                        .collect(Collectors.toList());

                favoriteStatus = favoritesService.checkFavoriteStatus(userPrincipal.getId(), exerciseIds);
            } else {
                favoriteStatus = Collections.emptyMap();
            }

            List<Map<String, Object>> enhancedResponse = exercises.stream()
                    .map(exercise -> createEnhancedExerciseMap(exercise, favoriteStatus))
                    .collect(Collectors.toList());

            log.debug("✅ Enhanced {} exercises with favorite status for user: {}",
                    enhancedResponse.size(),
                    userPrincipal != null ? userPrincipal.getId() : "guest");

            return ResponseEntity.ok(enhancedResponse);

        } catch (Exception e) {
            log.error("❌ Failed to get enhanced exercises", e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @GetMapping("/public/search")
    @Operation(summary = "Search public exercises", description = "Search exercises with query and filters")
    public ResponseEntity<List<ExerciseResponseDTO>> searchPublicExercises(
            @RequestParam String q,
            @RequestParam(required = false) String goal,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String equipment) {

        log.debug("🔍 Public search request - query: {}, goal: {}, difficulty: {}, equipment: {}", q, goal, difficulty, equipment);

        ExerciseSearchRequestDTO searchRequest = buildSearchRequest(goal, difficulty, equipment, q);
        Pageable pageable = createPageable(searchRequest);

        Page<Exercise> exercisePage = exerciseService.searchExercises(
                searchRequest.getSearch(),
                searchRequest.getMuscleGroups(),
                searchRequest.getEquipment(),
                searchRequest.getDifficultyLevel(),
                pageable
        );

        List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(exercisePage.getContent());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/goals")
    @Operation(summary = "Get exercise goals", description = "Get available fitness goals with counts")
    public ResponseEntity<List<Map<String, Object>>> getGoals() {
        List<Object[]> typeCounts = exerciseService.getExerciseTypeCounts();

        Map<String, Integer> goalCounts = initializeGoalCounts();
        aggregateTypeCountsToGoals(typeCounts, goalCounts);

        List<Map<String, Object>> goals = goalCounts.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> goal = new HashMap<>();
                    goal.put("goal", entry.getKey());
                    goal.put("count", entry.getValue());
                    return goal;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(goals);
    }

    @GetMapping("/public/filters")
    @Operation(summary = "Get filter options", description = "Get available filter options for frontend")
    public ResponseEntity<Map<String, Object>> getPublicFilterOptions() {
        ExerciseFiltersDTO filters = exerciseService.getAvailableFiltersWithCounts();

        List<String> equipment = new ArrayList<>(filters.getEquipment());
        if (!equipment.contains("None")) {
            equipment.add(0, "None");
        }

        // Make the filters reference effectively final for lambda
        final ExerciseFiltersDTO finalFilters = filters;
        List<String> difficulties = finalFilters.getDifficultyLevels().stream()
                .map(diff -> capitalizeFirst(diff.getValue()))
                .collect(Collectors.toList());

        Map<String, Object> frontendFilters = new HashMap<>();
        frontendFilters.put("equipment", equipment);
        frontendFilters.put("difficulties", difficulties);

        return ResponseEntity.ok(frontendFilters);
    }

    // ===================================================================
    // ⭐ FAVORITES ENDPOINTS (Complete Implementation)
    // ===================================================================

    @GetMapping("/favorites")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user favorites", description = "Get user's favorite exercises with pagination")
    public ResponseEntity<List<ExerciseResponseDTO>> getUserFavoriteExercises(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            // Support both paginated and non-paginated calls for backward compatibility
            if (page == 0 && size == 20) {
                // Default call - return all favorites (existing behavior)
                List<Exercise> favorites = favoritesService.getUserFavoriteExercises(userPrincipal.getId());
                List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(favorites);

                log.debug("✅ Retrieved {} favorite exercises for user {}", response.size(), userPrincipal.getId());
                return ResponseEntity.ok(response);
            } else {
                // Paginated call - now that service is fixed, we can use proper pagination
                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
                Page<Exercise> favoritesPage = favoritesService.getUserFavoriteExercises(userPrincipal.getId(), pageable);
                List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(favoritesPage.getContent());

                log.debug("✅ Retrieved {} favorite exercises (page {}) for user {}", response.size(), page, userPrincipal.getId());
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            log.error("❌ Failed to get favorite exercises for user {}", userPrincipal.getId(), e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @GetMapping("/favorites/count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user favorites count", description = "Get total number of user's favorite exercises")
    public ResponseEntity<Map<String, Object>> getUserFavoritesCount(@CurrentUser UserPrincipal userPrincipal) {

        try {
            long count = favoritesService.getUserFavoriteCount(userPrincipal.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("favoriteCount", count);
            response.put("hasAnyFavorites", count > 0);
            response.put("userId", userPrincipal.getId());

            log.debug("✅ Retrieved favorite count: {} for user {}", count, userPrincipal.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Failed to get favorite count for user {}", userPrincipal.getId(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("favoriteCount", 0L);
            errorResponse.put("hasAnyFavorites", false);
            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/favorites/{exerciseId}/toggle")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Toggle favorite", description = "Add or remove exercise from favorites")
    public ResponseEntity<Map<String, Object>> toggleExerciseFavorite(
            @PathVariable Long exerciseId,
            @CurrentUser UserPrincipal userPrincipal) {

        try {
            boolean isFavorite = favoritesService.toggleFavorite(userPrincipal.getId(), exerciseId);

            Map<String, Object> response = new HashMap<>();
            response.put("exerciseId", exerciseId);
            response.put("isFavorite", isFavorite);
            response.put("action", isFavorite ? "added" : "removed");
            response.put("message", isFavorite ? "Exercise added to favorites" : "Exercise removed from favorites");

            log.debug("✅ Toggled favorite status for exercise {} (user {}): {}",
                    exerciseId, userPrincipal.getId(), isFavorite);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Failed to toggle favorite for exercise {} (user {})", exerciseId, userPrincipal.getId(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update favorite status");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/favorites/ids")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get favorite IDs", description = "Get lightweight list of user's favorite exercise IDs")
    public ResponseEntity<Set<Long>> getFavoriteExerciseIds(@CurrentUser UserPrincipal userPrincipal) {
        try {
            Set<Long> favoriteIds = favoritesService.getUserFavoriteExerciseIds(userPrincipal.getId());
            return ResponseEntity.ok(favoriteIds);
        } catch (Exception e) {
            log.error("❌ Failed to get favorite IDs for user {}", userPrincipal.getId(), e);
            return ResponseEntity.ok(Collections.emptySet());
        }
    }

    @PostMapping("/favorites/check")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Check multiple favorites", description = "Check favorite status for multiple exercises")
    public ResponseEntity<Map<Long, Boolean>> checkMultipleFavoriteStatus(
            @RequestBody List<Long> exerciseIds,
            @CurrentUser UserPrincipal userPrincipal) {

        try {
            Map<Long, Boolean> favoriteStatus = favoritesService.checkFavoriteStatus(userPrincipal.getId(), exerciseIds);
            return ResponseEntity.ok(favoriteStatus);
        } catch (Exception e) {
            log.error("❌ Failed to check multiple favorites for user {}", userPrincipal.getId(), e);
            return ResponseEntity.ok(exerciseIds.stream()
                    .collect(Collectors.toMap(id -> id, id -> false)));
        }
    }

    @PostMapping("/favorites/bulk/add")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add multiple favorites", description = "Add multiple exercises to favorites")
    public ResponseEntity<Map<String, Object>> addMultipleToFavorites(
            @RequestBody List<Long> exerciseIds,
            @CurrentUser UserPrincipal userPrincipal) {

        try {
            var favorites = favoritesService.addMultipleToFavorites(userPrincipal.getId(), exerciseIds);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("addedCount", favorites.size());
            response.put("requestedCount", exerciseIds.size());
            response.put("message", String.format("Added %d exercises to favorites", favorites.size()));

            log.debug("✅ Added {} exercises to favorites for user {}", favorites.size(), userPrincipal.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Failed to add multiple favorites for user {}", userPrincipal.getId(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("addedCount", 0);
            errorResponse.put("error", "Failed to add exercises to favorites");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/favorites/bulk/remove")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove multiple favorites", description = "Remove multiple exercises from favorites")
    public ResponseEntity<Map<String, Object>> removeMultipleFromFavorites(
            @RequestBody List<Long> exerciseIds,
            @CurrentUser UserPrincipal userPrincipal) {

        try {
            favoritesService.removeMultipleFromFavorites(userPrincipal.getId(), exerciseIds);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("removedCount", exerciseIds.size());
            response.put("message", String.format("Removed %d exercises from favorites", exerciseIds.size()));

            log.debug("✅ Removed {} exercises from favorites for user {}", exerciseIds.size(), userPrincipal.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Failed to remove multiple favorites for user {}", userPrincipal.getId(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("removedCount", 0);
            errorResponse.put("error", "Failed to remove exercises from favorites");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/favorites/clear")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Clear all favorites", description = "Remove all exercises from user's favorites")
    public ResponseEntity<Map<String, Object>> clearAllFavorites(@CurrentUser UserPrincipal userPrincipal) {

        try {
            long count = favoritesService.getUserFavoriteCount(userPrincipal.getId());
            favoritesService.clearAllUserFavorites(userPrincipal.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("clearedCount", count);
            response.put("message", String.format("Cleared %d favorites", count));

            log.debug("✅ Cleared {} favorites for user {}", count, userPrincipal.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Failed to clear favorites for user {}", userPrincipal.getId(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("clearedCount", 0);
            errorResponse.put("error", "Failed to clear favorites");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ===================================================================
    // 📊 ANALYTICS & INSIGHTS ENDPOINTS
    // ===================================================================

    @GetMapping("/favorites/trending")
    @Operation(summary = "Get trending favorites", description = "Get exercises that are trending in favorites")
    public ResponseEntity<List<ExerciseResponseDTO>> getTrendingFavorites(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit) {

        List<Exercise> trending = favoritesService.getTrendingFavorites(days, limit);
        List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(trending);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/favorites/most-popular")
    @Operation(summary = "Get most favorited", description = "Get most favorited exercises overall")
    public ResponseEntity<List<ExerciseResponseDTO>> getMostFavoritedExercises(
            @RequestParam(defaultValue = "10") int limit) {

        List<Exercise> mostFavorited = favoritesService.getMostFavoritedExercises(limit);
        List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(mostFavorited);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/favorites/recommendations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get recommended based on favorites", description = "Get exercise recommendations based on favorites")
    public ResponseEntity<List<ExerciseResponseDTO>> getRecommendedBasedOnFavorites(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "10") int limit) {

        List<Exercise> recommended = favoritesService.getRecommendedExercises(userPrincipal.getId(), limit);
        List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(recommended);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/favorites/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get favorite statistics", description = "Get comprehensive favorite system statistics")
    public ResponseEntity<ExerciseFavoritesService.FavoriteStatistics> getFavoriteStatistics() {
        ExerciseFavoritesService.FavoriteStatistics stats = favoritesService.getFavoriteStatistics();
        return ResponseEntity.ok(stats);
    }

    // ===================================================================
    // 🎯 CORE EXERCISE ENDPOINTS (Existing functionality)
    // ===================================================================

    @GetMapping("/{id}")
    @Operation(summary = "Get exercise by ID", description = "Get detailed exercise information")
    public ResponseEntity<Map<String, Object>> getExerciseById(
            @PathVariable Long id,
            @CurrentUser(required = false) UserPrincipal userPrincipal) {

        try {
            Optional<Exercise> exerciseOpt = exerciseRepository.findById(id);

            if (exerciseOpt.isEmpty() || !exerciseOpt.get().isPublished()) {
                return ResponseEntity.notFound().build();
            }

            Exercise exercise = exerciseOpt.get();

            // Record usage for authenticated users
            if (userPrincipal != null) {
                try {
                    User currentUser = userService.getUserById(userPrincipal.getId());
                    exerciseService.recordExerciseUsage(id, currentUser);
                } catch (Exception e) {
                    log.warn("Failed to record exercise usage for user {} on exercise {}", userPrincipal.getId(), id);
                }
            }

            Map<String, Object> response = createDetailedExerciseMap(exercise);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get exercise by ID: {}", id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve exercise");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping
    @Operation(summary = "Search exercises", description = "Search exercises with comprehensive filtering")
    public ResponseEntity<ExerciseListResponseDTO> getAllExercises(
            @Valid @ModelAttribute ExerciseSearchRequestDTO searchRequest) {

        log.debug("🔍 Searching exercises with filters: {}", searchRequest);

        Pageable pageable = createPageable(searchRequest);
        Page<Exercise> exercisePage = exerciseService.searchExercises(
                searchRequest.getSearch(),
                searchRequest.getMuscleGroups(),
                searchRequest.getEquipment(),
                searchRequest.getDifficultyLevel(),
                pageable
        );

        ExerciseListResponseDTO response = ExerciseListResponseDTO.fromPage(exercisePage);
        response.setAvailableFilters(ExerciseFiltersDTO.createDefault());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/popular")
    @Operation(summary = "Get popular exercises", description = "Get most popular exercises by usage")
    public ResponseEntity<List<ExerciseResponseDTO>> getPopularExercises(
            @RequestParam(defaultValue = "10") int limit) {

        List<Exercise> popularExercises = exerciseService.findMostPopular(limit);
        List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(popularExercises);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/filters")
    @Cacheable("exercise-filters")
    @Operation(summary = "Get filter options", description = "Get available filter options")
    public ResponseEntity<ExerciseFiltersDTO> getAvailableFilters() {
        ExerciseFiltersDTO filters = ExerciseFiltersDTO.createDefault();
        return ResponseEntity.ok(filters);
    }

    @GetMapping("/filters/counts")
    @Cacheable(value = "exercise-filters-with-counts", unless = "#result.body == null")
    @Operation(summary = "Get filters with counts", description = "Get filter options with exercise counts")
    public ResponseEntity<ExerciseFiltersDTO> getAvailableFiltersWithCounts() {
        ExerciseFiltersDTO filters = exerciseService.getAvailableFiltersWithCounts();
        return ResponseEntity.ok(filters);
    }

    @GetMapping("/search")
    @Operation(summary = "Legacy search", description = "Legacy search endpoint for backward compatibility")
    public ResponseEntity<ExerciseListResponseDTO> searchExercises(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) List<String> muscleGroups,
            @RequestParam(required = false) List<String> equipment,
            @RequestParam(required = false) Exercise.DifficultyLevel difficulty,
            @RequestParam(required = false) Exercise.ExerciseType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "exerciseName") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        ExerciseSearchRequestDTO searchRequest = new ExerciseSearchRequestDTO();
        searchRequest.setSearch(query);
        searchRequest.setMuscleGroups(muscleGroups);
        searchRequest.setEquipment(equipment);
        searchRequest.setDifficultyLevel(difficulty);
        searchRequest.setExerciseType(type);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy(sort);
        searchRequest.setSortDirection(direction);

        return getAllExercises(searchRequest);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get exercises by type", description = "Get exercises filtered by exercise type")
    public ResponseEntity<List<ExerciseResponseDTO>> getExercisesByType(
            @PathVariable Exercise.ExerciseType type,
            @RequestParam(defaultValue = "20") int limit) {

        List<Exercise> exercises = exerciseService.findExercisesForWorkoutType(type);
        List<ExerciseResponseDTO> response = exercises.stream()
                .limit(limit)
                .map(ExerciseResponseDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ===================================================================
    // 👤 USER AUTHENTICATED ENDPOINTS
    // ===================================================================

    @GetMapping("/recommended")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get recommended exercises", description = "Get recommended exercises based on user profile")
    public ResponseEntity<List<ExerciseResponseDTO>> getRecommendedExercises(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "10") int limit) {

        User currentUser = userService.getUserById(userPrincipal.getId());
        List<Exercise> recommended = exerciseService.findRecommendedExercises(currentUser, limit);
        List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(recommended);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/rate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Rate exercise", description = "Rate an exercise with optional comment and tags")
    public ResponseEntity<String> rateExercise(
            @PathVariable Long id,
            @Valid @RequestBody ExerciseRatingRequestDTO ratingRequest,
            @CurrentUser UserPrincipal userPrincipal) {

        User currentUser = userService.getUserById(userPrincipal.getId());
        exerciseService.rateExercise(
                id,
                currentUser,
                ratingRequest.getRating(),
                ratingRequest.getComment(),
                ratingRequest.getTags()
        );

        return ResponseEntity.ok("Exercise rated successfully");
    }

    @PostMapping("/{id}/use")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Record usage", description = "Record exercise usage")
    public ResponseEntity<String> recordExerciseUsage(
            @PathVariable Long id,
            @CurrentUser UserPrincipal userPrincipal) {

        User currentUser = userService.getUserById(userPrincipal.getId());
        exerciseService.recordExerciseUsage(id, currentUser);
        return ResponseEntity.ok("Exercise usage recorded");
    }

    @PostMapping("/{id}/workout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Record workout usage", description = "Record workout usage with duration and notes")
    public ResponseEntity<String> recordWorkoutUsage(
            @PathVariable Long id,
            @RequestParam(required = false) Integer durationMinutes,
            @RequestParam(required = false) String notes,
            @CurrentUser UserPrincipal userPrincipal) {

        User currentUser = userService.getUserById(userPrincipal.getId());
        exerciseService.recordWorkoutUsage(id, currentUser, durationMinutes, notes);
        return ResponseEntity.ok("Workout usage recorded");
    }

    @GetMapping("/insights")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user insights", description = "Get user exercise insights and analytics")
    public ResponseEntity<ExerciseService.UserExerciseInsights> getUserInsights(
            @CurrentUser UserPrincipal userPrincipal) {

        User currentUser = userService.getUserById(userPrincipal.getId());
        ExerciseService.UserExerciseInsights insights = exerciseService.getUserExerciseInsights(currentUser);
        return ResponseEntity.ok(insights);
    }

    @PostMapping("/workout-plan")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Generate workout plan", description = "Generate personalized workout plan")
    public ResponseEntity<List<ExerciseResponseDTO>> generateWorkoutPlan(
            @Valid @RequestBody ExerciseSelectionRequestDTO planRequest,
            @CurrentUser UserPrincipal userPrincipal) {

        User currentUser = userService.getUserById(userPrincipal.getId());
        List<Exercise> workoutPlan = exerciseService.buildWorkoutPlan(currentUser, planRequest);
        List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(workoutPlan);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/workout-modes")
    @Operation(summary = "Get workout modes", description = "Get available workout tracking modes")
    public ResponseEntity<List<Map<String, Object>>> getWorkoutTrackingModes() {
        List<Map<String, Object>> modes = new ArrayList<>();

        for (Exercise.WorkoutTrackingMode mode : Exercise.WorkoutTrackingMode.values()) {
            Map<String, Object> modeInfo = new HashMap<>();
            modeInfo.put("mode", mode.name());
            modeInfo.put("description", mode.getDescription());
            modeInfo.put("displayName", getWorkoutModeDisplayName(mode));
            modes.add(modeInfo);
        }

        return ResponseEntity.ok(modes);
    }

    @GetMapping("/by-workout-mode/{mode}")
    @Operation(summary = "Get exercises by workout mode", description = "Get exercises filtered by workout tracking mode")
    public ResponseEntity<List<ExerciseResponseDTO>> getExercisesByWorkoutMode(
            @PathVariable String mode,
            @RequestParam(defaultValue = "20") int limit) {

        List<Exercise> exercises = switch (mode.toUpperCase()) {
            case "TIME_BASED" -> exerciseService.findCardioExercises();
            case "HOLD_BASED" -> exerciseService.findIsometricExercises();
            case "REP_BASED" -> exerciseService.findRepBasedExercises();
            default -> Collections.emptyList();
        };

        if (exercises.isEmpty() && !mode.toUpperCase().matches("TIME_BASED|HOLD_BASED|REP_BASED")) {
            return ResponseEntity.badRequest().build();
        }

        List<ExerciseResponseDTO> response = exercises.stream()
                .limit(limit)
                .map(ExerciseResponseDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ===================================================================
    // 👨‍💼 PROFESSIONAL ENDPOINTS (Creator functionality)
    // ===================================================================

    @PostMapping
    @PreAuthorize("hasRole('PROFESSIONAL') or hasRole('ADMIN')")
    @Operation(summary = "Create exercise", description = "Create a new exercise (professionals/admins only)")
    public ResponseEntity<ExerciseResponseDTO> createExercise(
            @Valid @RequestBody ExerciseCreateRequestDTO createRequest,
            @CurrentUser UserPrincipal userPrincipal) {

        User currentUser = userService.getUserById(userPrincipal.getId());
        Exercise exercise = exerciseService.createProfessionalExercise(currentUser, createRequest);
        ExerciseResponseDTO response = ExerciseResponseDTO.fromEntity(exercise);

        log.info("✅ Created new exercise {} by user {}", exercise.getId(), userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PROFESSIONAL') and @exerciseService.isExerciseCreatedByUser(#id, authentication.principal.id))")
    @Operation(summary = "Update exercise", description = "Update an existing exercise")
    public ResponseEntity<ExerciseResponseDTO> updateExercise(
            @PathVariable Long id,
            @Valid @RequestBody ExerciseUpdateRequestDTO updateRequest,
            @CurrentUser UserPrincipal userPrincipal) {

        // TODO: Implement exercise update functionality
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .header("X-Reason", "Exercise update functionality coming soon")
                .build();
    }

    // ===================================================================
    // 🔒 ADMIN ENDPOINTS (Administrative functions)
    // ===================================================================

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve exercise", description = "Approve a pending exercise for publication")
    public ResponseEntity<String> approveExercise(
            @PathVariable Long id,
            @CurrentUser UserPrincipal userPrincipal) {

        User admin = userService.getUserById(userPrincipal.getId());
        exerciseService.approveExercise(id, admin);

        log.info("✅ Exercise {} approved by admin {}", id, userPrincipal.getId());
        return ResponseEntity.ok("Exercise approved successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete exercise", description = "Delete an exercise (admin only)")
    public ResponseEntity<String> deleteExercise(
            @PathVariable Long id,
            @CurrentUser UserPrincipal userPrincipal) {

        User admin = userService.getUserById(userPrincipal.getId());
        exerciseService.deleteExercise(id, admin);

        // Clean up associated favorites
        favoritesService.cleanupDeletedExerciseFavorites(id);

        log.info("✅ Exercise {} deleted by admin {}", id, userPrincipal.getId());
        return ResponseEntity.ok("Exercise deleted successfully");
    }

    @PostMapping("/bulk-action")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bulk exercise actions", description = "Perform bulk actions on multiple exercises")
    public ResponseEntity<String> performBulkAction(
            @Valid @RequestBody BulkExerciseActionRequestDTO bulkRequest,
            @CurrentUser UserPrincipal userPrincipal) {

        User admin = userService.getUserById(userPrincipal.getId());
        exerciseService.performBulkAction(
                bulkRequest.getExerciseIds(),
                bulkRequest.getAction(),
                bulkRequest.getReason(),
                admin
        );

        log.info("✅ Bulk action '{}' completed on {} exercises by admin {}",
                bulkRequest.getAction(), bulkRequest.getExerciseIds().size(), userPrincipal.getId());

        return ResponseEntity.ok(String.format(
                "Bulk action '%s' completed on %d exercises",
                bulkRequest.getAction(),
                bulkRequest.getExerciseIds().size()
        ));
    }

    @GetMapping("/{id}/analytics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSIONAL')")
    @Operation(summary = "Get exercise analytics", description = "Get detailed analytics for an exercise")
    public ResponseEntity<ExerciseAnalyticsResponseDTO> getExerciseAnalytics(
            @PathVariable Long id,
            @CurrentUser UserPrincipal userPrincipal) {

        ExerciseService.ExerciseAnalytics analytics = exerciseService.getExerciseAnalytics(id);
        ExerciseAnalyticsResponseDTO response = ExerciseAnalyticsResponseDTO.fromServiceAnalytics(analytics);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/recent-activity")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get recent activity", description = "Get recent favorites activity for admin monitoring")
    public ResponseEntity<List<Map<String, Object>>> getRecentFavoriteActivity(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "50") int limit) {

        var recentActivity = favoritesService.getRecentFavoriteActivity(days, limit);

        List<Map<String, Object>> response = recentActivity.stream()
                .map(favorite -> {
                    Map<String, Object> activityMap = new HashMap<>();
                    activityMap.put("userId", favorite.getUserId());
                    activityMap.put("exerciseId", favorite.getExerciseId());
                    activityMap.put("createdAt", favorite.getCreatedAt());
                    activityMap.put("action", "favorited");
                    return activityMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ===================================================================
    // 🔧 HELPER METHODS
    // ===================================================================

    private Map<String, Object> createEnhancedExerciseMap(ExerciseResponseDTO exercise, Map<Long, Boolean> favoriteStatus) {
        Map<String, Object> exerciseMap = new HashMap<>();

        // Core exercise data
        exerciseMap.put("id", exercise.getId());
        exerciseMap.put("name", exercise.getName());
        exerciseMap.put("exerciseName", exercise.getName()); // Fixed: using getName() consistently
        exerciseMap.put("description", exercise.getDescription());
        exerciseMap.put("emoji", exercise.getEmoji());
        exerciseMap.put("exerciseType", exercise.getExerciseType());
        exerciseMap.put("difficultyLevel", exercise.getDifficultyLevel());
        exerciseMap.put("isCardio", exercise.getIsCardio());
        exerciseMap.put("isIsometric", exercise.getIsIsometric());
        exerciseMap.put("estimatedDurationMinutes", exercise.getEstimatedDurationMinutes());
        exerciseMap.put("averageRating", exercise.getAverageRating());
        exerciseMap.put("usageCount", exercise.getUsageCount());

        // Favorite status
        exerciseMap.put("isFavorite", favoriteStatus.getOrDefault(exercise.getId(), false));

        return exerciseMap;
    }

    private Map<String, Object> createDetailedExerciseMap(Exercise exercise) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", exercise.getId());
        response.put("name", exercise.getExerciseName());
        response.put("description", exercise.getDescription());
        response.put("exerciseType", exercise.getExerciseType().toString());
        response.put("difficultyLevel", exercise.getDifficultyLevel().toString());
        response.put("targetMuscleGroups", exercise.getTargetMuscleGroups());
        response.put("equipmentRequired", exercise.getEquipmentRequired());
        response.put("usageCount", exercise.getUsageCount());
        response.put("averageRating", exercise.getAverageRating());
        response.put("totalRatings", exercise.getTotalRatings());
        response.put("isCardio", exercise.getIsCardio());
        response.put("isIsometric", exercise.getIsIsometric());
        response.put("workoutTrackingMode", exercise.getWorkoutTrackingMode().toString());
        response.put("trackingInstructions", exercise.getTrackingInstructions());
        return response;
    }

    private ExerciseSearchRequestDTO buildSearchRequest(String goal, String difficulty, String equipment, String query) {
        ExerciseSearchRequestDTO searchRequest = new ExerciseSearchRequestDTO();

        if (query != null) {
            searchRequest.setSearch(query);
        }

        if (difficulty != null && !difficulty.equals("all")) {
            try {
                searchRequest.setDifficultyLevel(Exercise.DifficultyLevel.valueOf(difficulty.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid difficulty level: {}", difficulty);
            }
        }

        if (equipment != null && !equipment.equals("all")) {
            if ("None".equals(equipment)) {
                searchRequest.setRequiresEquipment(false);
            } else {
                searchRequest.setEquipment(List.of(equipment));
            }
        }

        if (goal != null && !goal.equals("all")) {
            Exercise.ExerciseType mappedType = mapGoalToExerciseType(goal);
            if (mappedType != null) {
                searchRequest.setExerciseType(mappedType);
            }
        }

        searchRequest.setPage(0);
        searchRequest.setSize(100);
        searchRequest.setSortBy("usageCount");
        searchRequest.setSortDirection("desc");

        return searchRequest;
    }

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

            // Make variables effectively final for lambda use
            final String fatBurnKey = "fat-burn";
            final String enduranceKey = "endurance";
            final String muscleBuildingKey = "muscle-building";
            final String flexibilityKey = "flexibility";
            final String sportSpecificKey = "sport-specific";
            final String recoveryKey = "recovery";

            switch (type) {
                case CARDIO, PLYOMETRIC -> {
                    goalCounts.put(fatBurnKey, goalCounts.get(fatBurnKey) + count);
                    goalCounts.put(enduranceKey, goalCounts.get(enduranceKey) + count);
                }
                case STRENGTH -> goalCounts.put(muscleBuildingKey, goalCounts.get(muscleBuildingKey) + count);
                case FLEXIBILITY -> goalCounts.put(flexibilityKey, goalCounts.get(flexibilityKey) + count);
                case SPORTS_SPECIFIC -> goalCounts.put(sportSpecificKey, goalCounts.get(sportSpecificKey) + count);
                case REHABILITATION, BALANCE -> goalCounts.put(recoveryKey, goalCounts.get(recoveryKey) + count);
            }
        }
    }

    private Pageable createPageable(ExerciseSearchRequestDTO request) {
        String sortField = "name".equals(request.getSortBy()) ? "exerciseName" : request.getSortBy();

        Sort.Direction direction = "desc".equalsIgnoreCase(request.getSortDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortField);

        if ("relevance".equals(request.getSortBy())) {
            sort = Sort.by(Sort.Direction.DESC, "averageRating")
                    .and(Sort.by(Sort.Direction.DESC, "usageCount"))
                    .and(Sort.by(Sort.Direction.ASC, "exerciseName"));
        }

        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    private Exercise.ExerciseType mapGoalToExerciseType(String goal) {
        return switch (goal) {
            case "fat-burn" -> Exercise.ExerciseType.CARDIO;
            case "muscle-building" -> Exercise.ExerciseType.STRENGTH;
            case "endurance" -> Exercise.ExerciseType.CARDIO;
            case "flexibility" -> Exercise.ExerciseType.FLEXIBILITY;
            case "sport-specific" -> Exercise.ExerciseType.SPORTS_SPECIFIC;
            case "recovery" -> Exercise.ExerciseType.REHABILITATION;
            default -> null;
        };
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.charAt(0) + str.substring(1).toLowerCase();
    }

    private String getWorkoutModeDisplayName(Exercise.WorkoutTrackingMode mode) {
        return switch (mode) {
            case TIME_BASED -> "Cardio/Time-Based";
            case HOLD_BASED -> "Isometric/Hold-Based";
            case REP_BASED -> "Strength/Rep-Based";
        };
    }
}