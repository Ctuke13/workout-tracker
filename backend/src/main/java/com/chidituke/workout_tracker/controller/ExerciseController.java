package com.chidituke.workout_tracker.controller;

import com.chidituke.workout_tracker.dto.request.exercise.*;
import com.chidituke.workout_tracker.dto.response.exercise.*;
import com.chidituke.workout_tracker.model.Exercise;
import com.chidituke.workout_tracker.model.User;
import com.chidituke.workout_tracker.security.CurrentUser;
import com.chidituke.workout_tracker.service.ExerciseService;

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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}) // React development
public class ExerciseController {

    private final ExerciseService exerciseService;

    // 🌍 PUBLIC ENDPOINTS (No authentication required - Free Exercise Library!)

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

    // 🔧 FIXED - Clean version without try-catch and null check
    @GetMapping("/{id}")
    public ResponseEntity<ExerciseResponseDTO> getExerciseById(
            @PathVariable Long id,
            @CurrentUser(required = false) User currentUser) { // ✅ Consistent annotation usage

        // ✅ No try-catch needed - GlobalExceptionHandler handles ExerciseNotFoundException
        Exercise exercise = exerciseService.findById(id);

        // ✅ Fixed logic - no null check needed since findById throws exception if not found
        if (!exercise.isPublished()) {
            return ResponseEntity.notFound().build();
        }

        // Record usage only if user is logged in
        if (currentUser != null) {
            // ✅ No try-catch needed - GlobalExceptionHandler handles exceptions
            exerciseService.recordExerciseUsage(id, currentUser);
        }

        ExerciseResponseDTO response = ExerciseResponseDTO.fromEntity(exercise);
        return ResponseEntity.ok(response);
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
            @RequestParam(defaultValue = "name") String sort,
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

    // 👤 USER AUTHENTICATED ENDPOINTS

    @GetMapping("/recommended")
    public ResponseEntity<List<ExerciseResponseDTO>> getRecommendedExercises(
            @CurrentUser User currentUser,
            @RequestParam(defaultValue = "10") int limit) {

        List<Exercise> recommended = exerciseService.findRecommendedExercises(currentUser, limit);
        List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(recommended);

        return ResponseEntity.ok(response);
    }

    // 🔧 FIXED - Clean version without try-catch
    @PostMapping("/{id}/rate")
    public ResponseEntity<String> rateExercise(
            @PathVariable Long id,
            @Valid @RequestBody ExerciseRatingRequestDTO ratingRequest,
            @CurrentUser User currentUser) {

        // ✅ No try-catch needed - GlobalExceptionHandler handles InvalidExerciseDataException
        exerciseService.rateExercise(id, currentUser, ratingRequest.getRating());
        return ResponseEntity.ok("Exercise rated successfully");
    }

    // 🔧 FIXED - Clean version without try-catch
    @PostMapping("/{id}/use")
    public ResponseEntity<String> recordExerciseUsage(
            @PathVariable Long id,
            @CurrentUser User currentUser) {

        // ✅ No try-catch needed - GlobalExceptionHandler handles ExerciseNotFoundException
        exerciseService.recordExerciseUsage(id, currentUser);
        return ResponseEntity.ok("Exercise usage recorded");
    }

    // 🔧 FIXED - Clean version without try-catch
    @PostMapping("/workout-plan")
    public ResponseEntity<List<ExerciseResponseDTO>> generateWorkoutPlan(
            @Valid @RequestBody WorkoutPlanRequestDTO planRequest,
            @CurrentUser User currentUser) { //

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

    // 👨‍💼 PROFESSIONAL ENDPOINTS

    // 🔧 FIXED - Clean version without try-catch
    @PostMapping
    @PreAuthorize("hasRole('PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<ExerciseResponseDTO> createExercise(
            @Valid @RequestBody ExerciseCreateRequestDTO createRequest,
            @CurrentUser User currentUser) { // ✅ Consistent annotation usage

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
            @CurrentUser User currentUser) { // ✅ Consistent annotation usage

        // TODO: Implement exercise update functionality
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .header("X-Reason", "Exercise update functionality coming soon")
                .build();
    }

    // 🔒 ADMIN ENDPOINTS

    // 🔧 FIXED - Clean version without try-catch
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> approveExercise(
            @PathVariable Long id,
            @CurrentUser User admin) {
        exerciseService.approveExercise(id, admin);
        return ResponseEntity.ok("Exercise approved successfully");
    }

    // 🔧 FIXED - Clean version without try-catch
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteExercise(
            @PathVariable Long id,
            @CurrentUser User admin) {
        exerciseService.deleteExercise(id, admin);
        return ResponseEntity.ok("Exercise deleted successfully");
    }

    // 🔧 FIXED - Clean version without try-catch
    @PostMapping("/bulk-action")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> performBulkAction(
            @Valid @RequestBody BulkExerciseActionRequestDTO bulkRequest,
            @CurrentUser User admin) {
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

    // 🔧 FIXED - Clean version without try-catch
    @GetMapping("/{id}/analytics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSIONAL')")
    public ResponseEntity<ExerciseAnalyticsResponseDTO> getExerciseAnalytics(
            @PathVariable Long id,
            @CurrentUser User user) {
        ExerciseService.ExerciseAnalytics analytics = exerciseService.getExerciseAnalytics(id);

        // Use the DTO's fromServiceAnalytics method instead of manual building
        ExerciseAnalyticsResponseDTO response = ExerciseAnalyticsResponseDTO.fromServiceAnalytics(analytics);

        return ResponseEntity.ok(response);
    }

    // 🔧 HELPER METHODS

    // ✅ UNCHANGED - This method works correctly
    private Pageable createPageable(ExerciseSearchRequestDTO request) {
        // Create sort
        Sort.Direction direction = "desc".equalsIgnoreCase(request.getSortDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = Sort.by(direction, request.getSortBy());

        // Handle default sorting for relevance
        if ("relevance".equals(request.getSortBy())) {
            sort = Sort.by(Sort.Direction.DESC, "averageRating")
                    .and(Sort.by(Sort.Direction.DESC, "usageCount"))
                    .and(Sort.by(Sort.Direction.ASC, "name"));
        }

        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    // 🔥 REMOVED - All exception handlers removed since GlobalExceptionHandler handles everything
}