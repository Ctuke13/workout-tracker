package com.chidituke.workout_tracker.controller;  // ✅ Fixed spelling

import com.chidituke.workout_tracker.dto.ExerciseDTO;
import com.chidituke.workout_tracker.dto.GoalSummaryDTO;
import com.chidituke.workout_tracker.model.Exercise;
import com.chidituke.workout_tracker.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/public/exercises")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class PublicExerciseController {

    private final ExerciseRepository exerciseRepository;  // ✅ Added 'final' for @RequiredArgsConstructor


    /**
     * Get all exercises with optional filtering (NO SEARCH)
     * GET /api/public/exercises?goal=fat-burn&difficulty=beginner&equipment=none
     */
    @GetMapping
    public ResponseEntity<List<ExerciseDTO>> getExercises(
            @RequestParam(required = false) String goal,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String equipment) {  // ← Removed 'search' parameter

        log.info("Fetching exercises with filters - goal: {}, difficulty: {}, equipment: {}", goal, difficulty, equipment);

        List<Exercise> exercises;

        // Handle filtering with reliable Spring Data methods (NO SEARCH LOGIC)
        if (goal != null && difficulty != null && equipment != null) {
            exercises = exerciseRepository.findByGoalAndDifficultyAndEquipmentAndPublishedTrueOrderByNameAsc(goal, difficulty, equipment);
        }
        else if (goal != null && difficulty != null) {
            exercises = exerciseRepository.findByGoalAndDifficultyAndPublishedTrueOrderByNameAsc(goal, difficulty);
        }
        else if (goal != null && equipment != null) {
            exercises = exerciseRepository.findByGoalAndEquipmentAndPublishedTrueOrderByNameAsc(goal, equipment);
        }
        else if (difficulty != null && equipment != null) {
            exercises = exerciseRepository.findByDifficultyAndEquipmentAndPublishedTrueOrderByNameAsc(difficulty, equipment);
        }
        else if (goal != null) {
            exercises = exerciseRepository.findByGoalAndPublishedTrueOrderByNameAsc(goal);
        }
        else if (difficulty != null) {
            exercises = exerciseRepository.findByDifficultyAndPublishedTrueOrderByNameAsc(difficulty);
        }
        else if (equipment != null) {
            exercises = exerciseRepository.findByEquipmentAndPublishedTrueOrderByNameAsc(equipment);
        }
        else {
            exercises = exerciseRepository.findByPublishedTrueOrderByNameAsc();
        }

        List<ExerciseDTO> exerciseDTOs = exercises.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        log.info("Found {} exercises", exerciseDTOs.size());
        return ResponseEntity.ok(exerciseDTOs);
    }

    /**
     * Get exercises by goal category
     * GET /api/public/exercises/goal/{goal}
     */
    @GetMapping("/goal/{goal}")  // ✅ Added specific path to avoid conflicts
    public ResponseEntity<List<ExerciseDTO>> getExercisesByGoal(@PathVariable String goal) {
        log.info("Fetching exercises for goal: {}", goal);

        List<Exercise> exercises = exerciseRepository.findByGoalAndPublishedTrueOrderByNameAsc(goal);
        List<ExerciseDTO> exerciseDTOs = exercises.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        if (exerciseDTOs.isEmpty()) {
            log.warn("No exercises found for goal: {}", goal);
        }

        return ResponseEntity.ok(exerciseDTOs);
    }

    /**
     * Get single exercise by ID
     * GET /api/public/exercises/123
     */
    @GetMapping("/{id}")  // ✅ Added missing annotation, removed duplicate method
    public ResponseEntity<ExerciseDTO> getExercise(@PathVariable Long id) {
        log.info("Fetching exercise with id: {}", id);

        Optional<Exercise> exercise = exerciseRepository.findById(id);

        if (exercise.isPresent() && exercise.get().isPublished()) {
            return ResponseEntity.ok(convertToDTO(exercise.get()));
        } else {
            log.warn("Exercise not found or not published: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get goal categories with exercise counts
     * GET /api/public/exercises/goals
     */
    @GetMapping("/goals")  // ✅ Removed duplicate method
    public ResponseEntity<List<GoalSummaryDTO>> getGoalSummaries() {
        log.info("Fetching goal summaries");

        List<Object[]> goalCounts = exerciseRepository.findGoalsWithCounts();
        List<GoalSummaryDTO> goalSummaries = goalCounts.stream()
                .map(row -> new GoalSummaryDTO((String) row[0], ((Number) row[1]).intValue()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(goalSummaries);
    }

    /**
     * Get filter options (equipment, difficulties)
     * GET /api/public/exercises/filters
     */
    @GetMapping("/filters")
    public ResponseEntity<FilterOptionsDTO> getFilterOptions() {
        log.info("Fetching filter options");

        List<String> equipment = exerciseRepository.findDistinctEquipment();
        List<String> difficulties = exerciseRepository.findDistinctDifficulties();

        FilterOptionsDTO filterOptions = new FilterOptionsDTO(equipment, difficulties);
        return ResponseEntity.ok(filterOptions);
    }

    /**
     * Search exercises with optional filtering
     * GET /api/public/exercises/search?q=hiit
     * GET /api/public/exercises/search?q=hiit&goal=fat-burn&difficulty=Intermediate&equipment=None
     * GET /api/public/exercises/search?q=     (empty - returns all, can still be filtered)
     */
    @GetMapping("/search")
    public ResponseEntity<List<ExerciseDTO>> searchExercises(
            @RequestParam("q") String query,
            @RequestParam(required = false) String goal,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String equipment) {

        log.info("Searching exercises with query: '{}', goal: {}, difficulty: {}, equipment: {}",
                query, goal, difficulty, equipment);

        List<Exercise> exercises;

        // Step 1: Get search results (or all exercises if empty search)
        if (isSearchEmpty(query)) {
            log.info("Empty search query - returning all exercises");
            exercises = exerciseRepository.findByPublishedTrueOrderByNameAsc();
        } else {
            log.info("Performing search for: '{}'", query.trim());
            exercises = exerciseRepository.searchExercises(query.trim());
        }

        // Step 2: Apply filters to search results
        List<Exercise> filteredExercises = exercises.stream()
                .filter(exercise -> goal == null || goal.equals(exercise.getGoal()))
                .filter(exercise -> difficulty == null || difficulty.equals(exercise.getDifficulty()))
                .filter(exercise -> equipment == null || equipment.equals(exercise.getEquipment()))
                .collect(Collectors.toList());

        // Step 3: Convert to DTOs
        List<ExerciseDTO> exerciseDTOs = filteredExercises.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        log.info("Found {} exercises for search query: '{}' with filters applied", exerciseDTOs.size(), query);
        return ResponseEntity.ok(exerciseDTOs);
    }

    /**
     * Helper method to check if search query is effectively empty
     */
    private boolean isSearchEmpty(String query) {
        if (query == null) return true;

        String trimmed = query.trim();

        return trimmed.isEmpty() ||                    // Empty string
                trimmed.equals("\"\"") ||               // Literal quotes
                trimmed.equals("''") ||                 // Single quotes
                trimmed.equals("null");                 // String "null"
    }

    /**
     * Convert Exercise entity to DTO
     */
    private ExerciseDTO convertToDTO(Exercise exercise) {
        return ExerciseDTO.builder()
                .id(exercise.getId())
                .name(exercise.getName())
                .goal(exercise.getGoal())
                .duration(exercise.getDuration())
                .equipment(exercise.getEquipment())
                .difficulty(exercise.getDifficulty())
                .calories(exercise.getCalories())
                .emoji(exercise.getEmoji())
                .description(exercise.getDescription())
                .benefits(exercise.getBenefits())
                .tips(exercise.getTips())
                .videoUrl(exercise.getVideoUrl())
                .build();
    }

    /**
     * Inner class for filter options response
     */
    public static class FilterOptionsDTO {
        public final List<String> equipment;
        public final List<String> difficulties;

        public FilterOptionsDTO(List<String> equipment, List<String> difficulties) {
            this.equipment = equipment;
            this.difficulties = difficulties;
        }
    }
}