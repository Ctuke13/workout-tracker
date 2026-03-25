package com.chidituke.workout_tracker.controller.exercise;

import com.chidituke.workout_tracker.controller.BaseApiController;
import com.chidituke.workout_tracker.dto.request.exercise.*;
import com.chidituke.workout_tracker.dto.response.exercise.*;
import com.chidituke.workout_tracker.model.workout.Exercise;
import com.chidituke.workout_tracker.security.CurrentUser;
import com.chidituke.workout_tracker.security.UserPrincipal;
import com.chidituke.workout_tracker.service.exercise.ExerciseUserService;
import com.chidituke.workout_tracker.service.user.UserService;
import com.chidituke.workout_tracker.service.exercise.ExerciseQueryService;  // Add this import
import com.chidituke.workout_tracker.service.exercise.ExerciseFavoritesService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@Tag(name = "Exercise Management", description = "Exercise library and management endpoints")
public class ExerciseController extends BaseApiController {

    // Updated dependencies - add ExerciseQueryService
    private final ExerciseQueryService exerciseQueryService;
    private final UserService userService;
    private final ExerciseFavoritesService favoritesService;
    private final ExerciseUserService exerciseUserService;

    // ===================================================================
    // 🌍 PUBLIC ENDPOINTS (Frontend Integration)
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

        Page<Exercise> exercisePage = exerciseQueryService.searchExercises(
                searchRequest.getSearch(),
                searchRequest.getMuscleGroups(),
                searchRequest.getEquipment(),
                searchRequest.getDifficultyLevel(),
                pageable
        );

        List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(exercisePage.getContent());
        log.debug("✅ Returned {} public exercises", response.size());

        return okList(response);
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

        Page<Exercise> exercisePage = exerciseQueryService.searchExercises(
                searchRequest.getSearch(),
                searchRequest.getMuscleGroups(),
                searchRequest.getEquipment(),
                searchRequest.getDifficultyLevel(),
                pageable
        );

        List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(exercisePage.getContent());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/filters")
    @Operation(summary = "Get filter options", description = "Get available filter options for frontend")
    public ResponseEntity<Map<String, Object>> getPublicFilterOptions() {

        ExerciseFiltersDTO filters = exerciseQueryService.getAvailableFiltersWithCounts();

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
    // 🎯 CORE EXERCISE ENDPOINTS (Existing functionality)
    // ===================================================================

    @GetMapping("/goals")
    @Operation(summary = "Get fitness goals", description = "Get available fitness goals for filtering")
    public ResponseEntity<List<Map<String, Object>>> getFitnessGoals() {
        List<Map<String, Object>> goals = List.of(
                Map.of("goal", "fat-burn", "label", "Fat Burn", "emoji", "🔥", "count", 0),
                Map.of("goal", "muscle-building", "label", "Muscle Building", "emoji", "💪", "count", 0),
                Map.of("goal", "endurance", "label", "Endurance", "emoji", "🏃", "count", 0),
                Map.of("goal", "flexibility", "label", "Flexibility", "emoji", "🤸", "count", 0),
                Map.of("goal", "sport-specific", "label", "Sport Specific", "emoji", "🏅", "count", 0),
                Map.of("goal", "recovery", "label", "Recovery", "emoji", "🏥", "count", 0)
        );
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get exercise by ID", description = "Get detailed exercise information")
    public ResponseEntity<Map<String, Object>> getExerciseById(@PathVariable Long id) {
        try {
            Exercise exercise = exerciseQueryService.findById(id);
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
        Page<Exercise> exercisePage = exerciseQueryService.searchExercises(
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

    @GetMapping("/filters")
    @Operation(summary = "Get filter options", description = "Get available filter options")
    public ResponseEntity<ExerciseFiltersDTO> getAvailableFilters() {
        // Use ExerciseQueryService instead of creating default
        ExerciseFiltersDTO filters = exerciseQueryService.getAvailableFilters();
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

        // Use ExerciseQueryService instead of ExerciseService
        List<Exercise> exercises = exerciseQueryService.findExercisesForWorkoutType(type);
        List<ExerciseResponseDTO> response = exercises.stream()
                .limit(limit)
                .map(ExerciseResponseDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ===================================================================
    // 🔧 HELPER METHODS (unchanged)
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
        searchRequest.setSize(500);
        searchRequest.setSortBy("usageCount");
        searchRequest.setSortDirection("desc");

        return searchRequest;
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