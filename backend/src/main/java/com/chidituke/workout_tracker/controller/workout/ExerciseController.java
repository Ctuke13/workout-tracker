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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}) // React development
public class ExerciseController {

    private final ExerciseService exerciseService;
    private final UserService userService;
    private final ExerciseRepository exerciseRepository;

    // ===================================================================
    // 🌍 FRONTEND-SPECIFIC ENDPOINTS (New additions for React frontend)
    // ===================================================================

    @GetMapping("/public")
    public ResponseEntity<List<ExerciseResponseDTO>> getPublicExercises(
            @RequestParam(required = false) String goal,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String equipment) {

        log.debug("Public frontend request - goal: {}, difficulty: {}, equipment: {}", goal, difficulty, equipment);

        // Convert frontend parameters to your existing search structure
        ExerciseSearchRequestDTO searchRequest = new ExerciseSearchRequestDTO();

        // Map frontend difficulty to your enum
        if (difficulty != null && !difficulty.equals("all")) {
            try {
                searchRequest.setDifficultyLevel(Exercise.DifficultyLevel.valueOf(difficulty.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid difficulty level: {}", difficulty);
            }
        }

        // Map frontend equipment
        if (equipment != null && !equipment.equals("all")) {
            if ("None".equals(equipment)) {
                searchRequest.setRequiresEquipment(false);
            } else {
                searchRequest.setEquipment(List.of(equipment));
            }
        }

        // Map frontend goal to exercise type (until you add fitnessGoals field)
        if (goal != null && !goal.equals("all")) {
            Exercise.ExerciseType mappedType = mapGoalToExerciseType(goal);
            if (mappedType != null) {
                searchRequest.setExerciseType(mappedType);
            }
        }

        searchRequest.setPage(0);
        searchRequest.setSize(100); // Frontend loads all initially
        searchRequest.setSortBy("usageCount");
        searchRequest.setSortDirection("desc");

        // Use your existing search logic
        Pageable pageable = createPageable(searchRequest);
        Page<Exercise> exercisePage = exerciseService.searchExercises(
                searchRequest.getSearch(),
                searchRequest.getMuscleGroups(),
                searchRequest.getEquipment(),
                searchRequest.getDifficultyLevel(),
                pageable
        );

        // Return exercises (using existing DTO for now)
        List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(exercisePage.getContent());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/search")
    public ResponseEntity<List<ExerciseResponseDTO>> searchPublicExercises(
            @RequestParam String q,
            @RequestParam(required = false) String goal,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String equipment) {

        log.debug("Public search request - query: {}, goal: {}, difficulty: {}, equipment: {}", q, goal, difficulty, equipment);

        // Same logic as above but with search term
        ExerciseSearchRequestDTO searchRequest = new ExerciseSearchRequestDTO();
        searchRequest.setSearch(q);

        // Map frontend parameters (same logic as above)
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

        // Use your existing search logic
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
    public ResponseEntity<List<Map<String, Object>>> getGoals() {
        // Get counts using your existing repository method
        List<Object[]> typeCounts = exerciseService.getExerciseTypeCounts();

        // Map exercise types to frontend goals
        Map<String, Integer> goalCounts = new HashMap<>();
        goalCounts.put("fat-burn", 0);
        goalCounts.put("muscle-building", 0);
        goalCounts.put("endurance", 0);
        goalCounts.put("flexibility", 0);
        goalCounts.put("sport-specific", 0);
        goalCounts.put("recovery", 0);

        // Aggregate counts from exercise types to goals
        for (Object[] typeCount : typeCounts) {
            Exercise.ExerciseType type = (Exercise.ExerciseType) typeCount[0];
            Integer count = ((Number) typeCount[1]).intValue();

            switch (type) {
                case CARDIO:
                    goalCounts.put("fat-burn", goalCounts.get("fat-burn") + count);
                    goalCounts.put("endurance", goalCounts.get("endurance") + count);
                    break;
                case PLYOMETRIC:
                    goalCounts.put("fat-burn", goalCounts.get("fat-burn") + count);
                    goalCounts.put("endurance", goalCounts.get("endurance") + count);
                    break;
                case STRENGTH:
                    goalCounts.put("muscle-building", goalCounts.get("muscle-building") + count);
                    break;
                case FLEXIBILITY:
                    goalCounts.put("flexibility", goalCounts.get("flexibility") + count);
                    break;
                case SPORTS_SPECIFIC:
                    goalCounts.put("sport-specific", goalCounts.get("sport-specific") + count);
                    break;
                case REHABILITATION:
                    goalCounts.put("recovery", goalCounts.get("recovery") + count);
                    break;
                case BALANCE:
                    goalCounts.put("recovery", goalCounts.get("recovery") + count);
                    break;
            }
        }

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
    public ResponseEntity<Map<String, Object>> getPublicFilterOptions() {
        // Use your existing filter logic but return in frontend format
        ExerciseFiltersDTO filters = exerciseService.getAvailableFiltersWithCounts();

        // Convert to frontend format
        List<String> equipment = filters.getEquipment();
        if (!equipment.contains("None")) {
            equipment = new ArrayList<>(equipment);
            equipment.add(0, "None");
        }

        List<String> difficulties = filters.getDifficultyLevels().stream()
                .map(diff -> diff.getValue().charAt(0) + diff.getValue().substring(1).toLowerCase())
                .collect(Collectors.toList());

        Map<String, Object> frontendFilters = new HashMap<>();
        frontendFilters.put("equipment", equipment);
        frontendFilters.put("difficulties", difficulties);

        return ResponseEntity.ok(frontendFilters);
    }

    // ===================================================================
    // 🌍 PUBLIC ENDPOINTS (Your existing endpoints - unchanged)
    // ===================================================================

    @GetMapping
    public ResponseEntity<ExerciseListResponseDTO> getAllExercises(
            @Valid @ModelAttribute ExerciseSearchRequestDTO searchRequest) {

        log.debug("Searching exercises with filters: {}", searchRequest);

        // Create pageable from request
        Pageable pageable = createPageable(searchRequest);

        // Search exercises with filters (no user required - free library!)
        Page<Exercise> exercisePage = exerciseService.searchExercises(
                searchRequest.getSearch(),
                searchRequest.getMuscleGroups(),
                searchRequest.getEquipment(),
                searchRequest.getDifficultyLevel(),
                pageable
        );

        // Build response with available filters
        ExerciseListResponseDTO response = ExerciseListResponseDTO.fromPage(exercisePage);
        response.setAvailableFilters(ExerciseFiltersDTO.createDefault());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getExerciseById(
            @PathVariable Long id,
            @CurrentUser(required = false) UserPrincipal userPrincipal) {

        try {
            System.out.println("🔍 DEBUG: getExerciseById called with ID: " + id);
            System.out.println("🔍 DEBUG: UserPrincipal: " + userPrincipal);

            // ✅ Use repository directly instead of service
            Optional<Exercise> exerciseOpt = exerciseRepository.findById(id);

            if (exerciseOpt.isEmpty()) {
                System.out.println("🔍 DEBUG: Exercise not found");
                return ResponseEntity.notFound().build();
            }

            Exercise exercise = exerciseOpt.get();
            System.out.println("🔍 DEBUG: Exercise found: " + exercise.getExerciseName());
            System.out.println("🔍 DEBUG: Exercise published: " + exercise.isPublished());

            // Check if exercise is published
            if (!exercise.isPublished()) {
                System.out.println("🔍 DEBUG: Exercise not published, returning 404");
                return ResponseEntity.notFound().build();
            }

            // Record usage only if user is logged in
            if (userPrincipal != null) {
                System.out.println("🔍 DEBUG: Recording usage for user ID: " + userPrincipal.getId());
                try {
                    User currentUser = userService.getUserById(userPrincipal.getId());
                    System.out.println("🔍 DEBUG: User found: " + currentUser.getUsername());

                    // ✅ Try the usage recording - this might be where the error occurs
                    exerciseService.recordExerciseUsage(id, currentUser);
                    System.out.println("🔍 DEBUG: Usage recorded successfully");
                } catch (Exception e) {
                    System.err.println("🔍 DEBUG: Error recording usage: " + e.getMessage());
                    e.printStackTrace();
                    // Continue without failing the request
                }
            } else {
                System.out.println("🔍 DEBUG: No user logged in, skipping usage recording");
            }

            // ✅ Create simple response map instead of DTO conversion
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

            System.out.println("🔍 DEBUG: Response created successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("🔍 DEBUG: Exception in getExerciseById: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/search")
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

        // Build search request
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

        // Use the same logic as getAllExercises - no duplication
        return getAllExercises(searchRequest);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ExerciseResponseDTO>> getPopularExercises(
            @RequestParam(defaultValue = "10") int limit) {

        List<Exercise> popularExercises = exerciseService.findMostPopular(limit);
        List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(popularExercises);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<ExerciseResponseDTO>> getExercisesByType(
            @PathVariable Exercise.ExerciseType type,
            @RequestParam(defaultValue = "20") int limit) {

        List<Exercise> exercises = exerciseService.findExercisesForWorkoutType(type);
        List<ExerciseResponseDTO> response = exercises.stream()
                .limit(limit)
                .map(ExerciseResponseDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }

    // ⚡ OPTIMIZED - Added caching for static filter data
    @GetMapping("/filters")
    @Cacheable("exercise-filters") // Add caching if Spring Cache is configured
    public ResponseEntity<ExerciseFiltersDTO> getAvailableFilters() {
        // Return available filter options for frontend
        ExerciseFiltersDTO filters = ExerciseFiltersDTO.createDefault();
        return ResponseEntity.ok(filters);
    }

    @GetMapping("/filters/counts")
    @Cacheable(value = "exercise-filters-with-counts", unless = "#result.body == null")
    public ResponseEntity<ExerciseFiltersDTO> getAvailableFiltersWithCounts() {
        ExerciseFiltersDTO filters = exerciseService.getAvailableFiltersWithCounts();
        return ResponseEntity.ok(filters);
    }

    // ===================================================================
    // 👤 USER AUTHENTICATED ENDPOINTS - 🆕 ENHANCED WITH REAL IMPLEMENTATIONS
    // ===================================================================

    @GetMapping("/recommended")
    @PreAuthorize("isAuthenticated()")  // ✅ Add this line - requires any authentication
    public ResponseEntity<List<ExerciseResponseDTO>> getRecommendedExercises(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "10") int limit) {

        User currentUser = userService.getUserById(userPrincipal.getId());
        List<Exercise> recommended = exerciseService.findRecommendedExercises(currentUser, limit);
        List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(recommended);

        return ResponseEntity.ok(response);
    }

    // 🆕 UPDATED - Now supports comments and tags
    @PostMapping("/{id}/rate")
    public ResponseEntity<String> rateExercise(
            @PathVariable Long id,
            @Valid @RequestBody ExerciseRatingRequestDTO ratingRequest,
            @CurrentUser UserPrincipal userPrincipal) {

        // Get the full User object from UserPrincipal
        User currentUser = userService.getUserById(userPrincipal.getId());

        // Pass all rating parameters to the service
        exerciseService.rateExercise(
                id,
                currentUser,
                ratingRequest.getRating(),
                ratingRequest.getComment(),    // Now supported!
                ratingRequest.getTags()        // Now supported!
        );

        return ResponseEntity.ok("Exercise rated successfully");
    }

    @PostMapping("/{id}/use")
    public ResponseEntity<String> recordExerciseUsage(
            @PathVariable Long id,
            @CurrentUser UserPrincipal userPrincipal) {

        // Get the full User object from UserPrincipal
        User currentUser = userService.getUserById(userPrincipal.getId());

        // ✅ No try-catch needed - GlobalExceptionHandler handles ExerciseNotFoundException
        exerciseService.recordExerciseUsage(id, currentUser);
        return ResponseEntity.ok("Exercise usage recorded");
    }

    // 🆕 NEW - Workout usage tracking with duration and notes
    @PostMapping("/{id}/workout")
    public ResponseEntity<String> recordWorkoutUsage(
            @PathVariable Long id,
            @RequestParam(required = false) Integer durationMinutes,
            @RequestParam(required = false) String notes,
            @CurrentUser UserPrincipal userPrincipal) {

        User currentUser = userService.getUserById(userPrincipal.getId());
        exerciseService.recordWorkoutUsage(id, currentUser, durationMinutes, notes);
        return ResponseEntity.ok("Workout usage recorded");
    }

    // 🆕 NEW - Get user exercise insights and analytics
    @GetMapping("/insights")
    public ResponseEntity<ExerciseService.UserExerciseInsights> getUserInsights(
            @CurrentUser UserPrincipal userPrincipal) {

        User currentUser = userService.getUserById(userPrincipal.getId());
        ExerciseService.UserExerciseInsights insights = exerciseService.getUserExerciseInsights(currentUser);
        return ResponseEntity.ok(insights);
    }

    @PostMapping("/workout-plan")
    public ResponseEntity<List<ExerciseResponseDTO>> generateWorkoutPlan(
            @Valid @RequestBody WorkoutPlanRequestDTO planRequest,
            @CurrentUser UserPrincipal userPrincipal) {

        User currentUser = userService.getUserById(userPrincipal.getId());

        // Convert DTO to service request
        ExerciseService.WorkoutPlanRequest serviceRequest = new ExerciseService.WorkoutPlanRequest();
        serviceRequest.setTargetMuscleGroups(planRequest.getTargetMuscleGroups());
        serviceRequest.setAvailableEquipment(planRequest.getAvailableEquipment());
        serviceRequest.setMaxDifficulty(planRequest.getMaxDifficulty());
        serviceRequest.setTargetDurationMinutes(planRequest.getTargetDurationMinutes());
        serviceRequest.setExercisesPerMuscleGroup(planRequest.getExercisesPerMuscleGroup());

        // ✅ No try-catch needed - GlobalExceptionHandler handles InvalidExerciseDataException
        List<Exercise> workoutPlan = exerciseService.buildWorkoutPlan(currentUser, serviceRequest);
        List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(workoutPlan);

        return ResponseEntity.ok(response);
    }

    // ===================================================================
    // 👨‍💼 PROFESSIONAL ENDPOINTS (Your existing endpoints - unchanged)
    // ===================================================================

    // 🔧 FIXED - Clean version without try-catch
    @PostMapping
    @PreAuthorize("hasRole('PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<ExerciseResponseDTO> createExercise(
            @Valid @RequestBody ExerciseCreateRequestDTO createRequest,
            @CurrentUser UserPrincipal userPrincipal) {

        User currentUser = userService.getUserById(userPrincipal.getId());

        // Convert DTO to service request
        ExerciseService.ExerciseCreationRequest serviceRequest = new ExerciseService.ExerciseCreationRequest();
        serviceRequest.setName(createRequest.getName());
        serviceRequest.setDescription(createRequest.getDescription());
        serviceRequest.setExerciseType(createRequest.getExerciseType());
        serviceRequest.setDifficultyLevel(createRequest.getDifficultyLevel());
        serviceRequest.setTargetMuscleGroups(createRequest.getTargetMuscleGroups());
        serviceRequest.setEquipmentRequired(createRequest.getEquipmentRequired());
        serviceRequest.setBenefits(createRequest.getBenefits());
        serviceRequest.setTips(createRequest.getTips());
        serviceRequest.setVideoUrl(createRequest.getVideoUrl());

        Exercise exercise = exerciseService.createProfessionalExercise(currentUser, serviceRequest);
        ExerciseResponseDTO response = ExerciseResponseDTO.fromEntity(exercise);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PROFESSIONAL') and @exerciseService.isExerciseCreatedByUser(#id, authentication.principal.id))")
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
    // 🔒 ADMIN ENDPOINTS (Your existing endpoints - unchanged)
    // ===================================================================

    // 🔧 FIXED - Clean version without try-catch
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> approveExercise(
            @PathVariable Long id,
            @CurrentUser UserPrincipal userPrincipal) {

        User admin = userService.getUserById(userPrincipal.getId());
        exerciseService.approveExercise(id, admin);
        return ResponseEntity.ok("Exercise approved successfully");
    }

    // 🔧 FIXED - Clean version without try-catch
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteExercise(
            @PathVariable Long id,
            @CurrentUser UserPrincipal userPrincipal) {

        User admin = userService.getUserById(userPrincipal.getId());
        exerciseService.deleteExercise(id, admin);
        return ResponseEntity.ok("Exercise deleted successfully");
    }

    // 🔧 FIXED - Clean version without try-catch
    @PostMapping("/bulk-action")
    @PreAuthorize("hasRole('ADMIN')")
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

        return ResponseEntity.ok(String.format(
                "Bulk action '%s' completed on %d exercises",
                bulkRequest.getAction(),
                bulkRequest.getExerciseIds().size()
        ));
    }

    @GetMapping("/{id}/analytics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSIONAL')")
    public ResponseEntity<ExerciseAnalyticsResponseDTO> getExerciseAnalytics(
            @PathVariable Long id,
            @CurrentUser UserPrincipal userPrincipal) {

        ExerciseService.ExerciseAnalytics analytics = exerciseService.getExerciseAnalytics(id);

        // Use the DTO's fromServiceAnalytics method instead of manual building
        ExerciseAnalyticsResponseDTO response = ExerciseAnalyticsResponseDTO.fromServiceAnalytics(analytics);

        return ResponseEntity.ok(response);
    }

    // ===================================================================
    // 🔧 HELPER METHODS (Enhanced with new methods)
    // ===================================================================

    private Pageable createPageable(ExerciseSearchRequestDTO request) {
        String sortField = request.getSortBy();
        if ("name".equals(sortField)) {
            sortField = "exerciseName";
        }

        // Create sort
        Sort.Direction direction = "desc".equalsIgnoreCase(request.getSortDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = Sort.by(direction, sortField);

        // Handle default sorting for relevance
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
}