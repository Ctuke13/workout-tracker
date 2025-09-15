package com.chidituke.workout_tracker.service.exercise;

import com.chidituke.workout_tracker.dto.response.exercise.ExerciseFiltersDTO;
import com.chidituke.workout_tracker.exceptions.exercise.ExerciseNotFoundException;
import com.chidituke.workout_tracker.model.workout.Exercise;
import com.chidituke.workout_tracker.repository.workout.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for public exercise queries and discovery operations.
 * Handles all read-only, non-user-specific exercise operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseQueryService {

    private final ExerciseRepository exerciseRepository;

    // ==================== BASIC EXERCISE RETRIEVAL ====================

    /**
     * Find exercise by ID
     */
    public Exercise findById(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new ExerciseNotFoundException(id));
    }

    /**
     * Get published exercises with pagination
     */
    public Page<Exercise> findPublishedExercises(Pageable pageable) {
        return exerciseRepository.findByPublishedTrueOrderByExerciseNameAsc(pageable);
    }

    /**
     * Get most popular exercises (cached for performance)
     */
    @Cacheable(value = "popular-exercises", key = "#limit")
    public List<Exercise> findMostPopular(int limit) {
        return exerciseRepository.findMostPopular(PageRequest.of(0, limit)).getContent();
    }

    // ==================== EXERCISE SEARCH & FILTERING ====================

    /**
     * Search exercises with comprehensive filters
     */
    public Page<Exercise> searchExercises(String searchTerm, List<String> muscleGroups,
                                          List<String> equipment, Exercise.DifficultyLevel difficulty,
                                          Pageable pageable) {
        String muscleGroup = (muscleGroups != null && !muscleGroups.isEmpty()) ? muscleGroups.get(0) : null;
        return exerciseRepository.searchExercisesWithFilters(searchTerm, muscleGroup, null, difficulty, pageable);
    }

    /**
     * Find exercises by type with sorting
     */
    public List<Exercise> findExercisesForWorkoutType(Exercise.ExerciseType type) {
        return exerciseRepository.findByExerciseTypeAndPublishedTrueOrderByExerciseNameAsc(type).stream()
                .sorted(Comparator.comparing(Exercise::getAverageRating).reversed()
                        .thenComparing(Exercise::getUsageCount).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Find suitable exercises based on equipment and difficulty
     */
    public List<Exercise> findSuitableExercises(List<String> availableEquipment,
                                                Exercise.DifficultyLevel maxDifficulty) {
        return exerciseRepository.findPublishedExercises().stream()
                .filter(exercise -> hasRequiredEquipment(exercise, availableEquipment))
                .filter(exercise -> exercise.getDifficultyLevel().ordinal() <= maxDifficulty.ordinal())
                .sorted(this::compareExercisesByRelevance)
                .collect(Collectors.toList());
    }

    // ==================== WORKOUT TRACKING MODE QUERIES ====================

    /**
     * Find cardio exercises (TIME_BASED tracking mode)
     */
    public List<Exercise> findCardioExercises() {
        return exerciseRepository.findByIsCardioTrueAndPublishedTrueOrderByAverageRatingDesc();
    }

    /**
     * Find isometric exercises (HOLD_BASED tracking mode)
     */
    public List<Exercise> findIsometricExercises() {
        return exerciseRepository.findByIsIsometricTrueAndPublishedTrueOrderByAverageRatingDesc();
    }

    /**
     * Find strength exercises (REP_BASED tracking mode)
     */
    public List<Exercise> findRepBasedExercises() {
        return exerciseRepository.findByIsCardioFalseAndIsIsometricFalseAndPublishedTrueOrderByAverageRatingDesc();
    }

    /**
     * Find exercises by workout tracking mode
     */
    public List<Exercise> findExercisesByTrackingMode(Exercise.WorkoutTrackingMode mode) {
        return switch (mode) {
            case TIME_BASED -> findCardioExercises();
            case HOLD_BASED -> findIsometricExercises();
            case REP_BASED -> findRepBasedExercises();
        };
    }


    /**
     * Find exercises by workout tracking mode with limit
     */
    public List<Exercise> findExercisesByTrackingMode(Exercise.WorkoutTrackingMode mode, int limit) {
        return findExercisesByTrackingMode(mode).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ==================== PROFESSIONAL CONTENT QUERIES ====================

    /**
     * Find professional exercises
     */
    public List<Exercise> findProfessionalExercises() {
        return exerciseRepository.findByCreatedByProfessionalTrueAndPublishedTrueOrderByAverageRatingDesc();
    }

    /**
     * Find professional exercises with pagination
     */
    public Page<Exercise> findProfessionalExercises(Pageable pageable) {
        return exerciseRepository.findByCreatedByProfessionalTrueAndPublishedTrue(pageable);
    }

    /**
     * Find all published exercises with professional content prioritized
     */
    public List<Exercise> findExercisesWithProfessionalFirst() {
        return exerciseRepository.findPublishedExercisesOrderByProfessionalFirst();
    }

    /**
     * Find exercises by professional status
     */
    public List<Exercise> findExercisesByProfessionalStatus(boolean isProfessional) {
        if (isProfessional) {
            return findProfessionalExercises();
        } else {
            // Return community exercises (non-professional)
            return exerciseRepository.findPublishedExercises().stream()
                    .filter(exercise -> !exercise.isCreatedByProfessional())
                    .collect(Collectors.toList());
        }
    }

    // ==================== FILTER OPERATIONS ====================

    /**
     * Get available filter options (basic)
     */
    public ExerciseFiltersDTO getAvailableFilters() {
        return ExerciseFiltersDTO.createDefault();
    }

    /**
     * Get available filter options with actual counts from database
     */
    @Cacheable(value = "exercise-filters", unless = "#result == null")
    public ExerciseFiltersDTO getAvailableFiltersWithCounts() {
        // Get actual counts from database
        List<Object[]> typeCounts = exerciseRepository.getExerciseTypeCounts();
        List<Object[]> difficultyCounts = exerciseRepository.getDifficultyLevelCounts();

        // Convert to DTOs with real counts
        List<ExerciseFiltersDTO.ExerciseTypeDTO> exerciseTypes = Arrays.stream(Exercise.ExerciseType.values())
                .map(type -> {
                    long count = typeCounts.stream()
                            .filter(row -> row[0].equals(type))
                            .mapToLong(row -> (Long) row[1])
                            .findFirst()
                            .orElse(0L);

                    return ExerciseFiltersDTO.ExerciseTypeDTO.builder()
                            .value(type.name())
                            .displayName(type.getDisplayName())
                            .count(count)
                            .build();
                })
                .toList();

        List<ExerciseFiltersDTO.DifficultyLevelDTO> difficultyLevels = Arrays.stream(Exercise.DifficultyLevel.values())
                .map(level -> {
                    long count = difficultyCounts.stream()
                            .filter(row -> row[0].equals(level))
                            .mapToLong(row -> (Long) row[1])
                            .findFirst()
                            .orElse(0L);

                    return ExerciseFiltersDTO.DifficultyLevelDTO.builder()
                            .value(level.name())
                            .description(level.getDescription())
                            .count(count)
                            .build();
                })
                .toList();

        return ExerciseFiltersDTO.builder()
                .exerciseTypes(exerciseTypes)
                .difficultyLevels(difficultyLevels)
                .equipment(List.of(
                        "dumbbells", "barbell", "yoga_mat", "resistance_bands",
                        "kettlebell", "jump_rope", "pull_up_bar", "medicine_ball",
                        "foam_roller", "exercise_bike", "treadmill", "elliptical"
                ))
                .muscleGroups(List.of(
                        "CHEST", "BACK", "SHOULDERS", "BICEPS", "TRICEPS", "FOREARMS",
                        "CORE", "ABS", "OBLIQUES", "QUADS", "HAMSTRINGS", "GLUTES",
                        "CALVES", "CARDIO", "FULL_BODY"
                ))
                .build();
    }

    /**
     * Get exercise type counts for analytics
     */
    public List<Object[]> getExerciseTypeCounts() {
        return exerciseRepository.countByExerciseType();
    }

    // ==================== UTILITY & HELPER METHODS ====================

    /**
     * Check if exercise has required equipment
     */
    private boolean hasRequiredEquipment(Exercise exercise, List<String> availableEquipment) {
        if (exercise.getEquipmentRequired() == null || exercise.getEquipmentRequired().isEmpty()) {
            return true;
        }

        if (availableEquipment == null || availableEquipment.isEmpty()) {
            return false; // Exercise requires equipment but none available
        }

        // Use Set for O(1) lookup instead of List.contains() which is O(n)
        Set<String> availableSet = new HashSet<>(availableEquipment);
        return exercise.getEquipmentRequired().stream()
                .allMatch(availableSet::contains);
    }

    /**
     * Compare exercises by relevance score
     */
    private int compareExercisesByRelevance(Exercise e1, Exercise e2) {
        double score1 = calculateRelevanceScore(e1);
        double score2 = calculateRelevanceScore(e2);
        return Double.compare(score2, score1); // Higher score first
    }

    /**
     * Calculate relevance score for exercise ranking
     */
    private double calculateRelevanceScore(Exercise exercise) {
        double score = 0.0;

        // Rating weight (40%)
        if (exercise.getAverageRating() != null && exercise.getTotalRatings() > 0) {
            score += exercise.getAverageRating() * 0.4;
        }

        // Popularity weight (30%)
        if (exercise.getUsageCount() != null) {
            score += Math.min(exercise.getUsageCount() / 1000.0, 1.0) * 0.3;
        }

        // Professional content weight (20%)
        if (exercise.isFromVerifiedSource()) {
            score += 0.2;
        }

        // Recency weight (10%)
        if (exercise.getCreatedAt() != null) {
            long daysSinceCreation = java.time.Duration.between(
                    exercise.getCreatedAt(), java.time.LocalDateTime.now()).toDays();
            if (daysSinceCreation < 30) {
                score += 0.1 * (30 - daysSinceCreation) / 30.0;
            }
        }

        return score;
    }
}