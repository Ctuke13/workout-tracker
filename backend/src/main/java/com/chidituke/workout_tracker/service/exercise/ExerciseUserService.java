package com.chidituke.workout_tracker.service.exercise;

import com.chidituke.workout_tracker.dto.request.exercise.ExerciseCreateRequestDTO;
import com.chidituke.workout_tracker.dto.request.exercise.ExerciseSelectionRequestDTO;
import com.chidituke.workout_tracker.exceptions.exercise.InvalidExerciseDataException;
import com.chidituke.workout_tracker.exceptions.user.UserNotFoundException;
import com.chidituke.workout_tracker.mapper.workout.ExerciseMapper;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.workout.Exercise;
import com.chidituke.workout_tracker.model.workout.UserExerciseHistory;
import com.chidituke.workout_tracker.model.workout.UserExerciseRating;
import com.chidituke.workout_tracker.repository.workout.ExerciseRepository;
import com.chidituke.workout_tracker.repository.workout.UserExerciseHistoryRepository;
import com.chidituke.workout_tracker.repository.workout.UserExerciseRatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseUserService {

    private final ExerciseRepository exerciseRepository;
    private final UserExerciseRatingRepository ratingRepository;
    private final UserExerciseHistoryRepository historyRepository;
    private final ExerciseMapper exerciseMapper;
    private final ExerciseQueryService exerciseQueryService;
  
    /**
     * Find exercises by workout tracking mode with limit
     */
    public List<Exercise> findExercisesByTrackingMode(Exercise.WorkoutTrackingMode mode, int limit) {
        return exerciseQueryService.findExercisesByTrackingMode(mode, limit);
    }

    /**
     * Find user's favorite exercises
     * Note: This is a simplified implementation. In a real system, you'd want
     * user-specific favorites, not just exercises marked as globally favorite.
     */
    public List<Exercise> findFavoriteExercises(String username) {
        // For now, return globally marked favorites
        // TODO: Implement user-specific favorites with a UserFavoriteExercise entity
        return exerciseRepository.findByIsFavoriteTrueAndPublishedTrueOrderByExerciseNameAsc();
    }

    /**
     * Remove exercise from favorites for user
     * TODO: Implement proper user-specific favorites
     */
    @Transactional
    public void removeExerciseFromFavorites(Long exerciseId, String username) {
        Exercise exercise = exerciseQueryService.findById(exerciseId); // Changed line
        exercise.removeFromFavorites();
        exerciseRepository.save(exercise);
        log.info("Exercise {} removed from favorites by user {}", exerciseId, username);
    }

    /**
     * Find favorite exercises with pagination
     */
    public Page<Exercise> findFavoriteExercises(String username, Pageable pageable) {
        // For now, return globally marked favorites with pagination
        // TODO: Implement user-specific favorites
        return exerciseRepository.findByIsFavoriteTrueAndPublishedTrue(pageable);
    }

    /**
     * Mark exercise as favorite for user
     * TODO: Implement proper user-specific favorites
     */
    @Transactional
    public void markExerciseAsFavorite(Long exerciseId, String username) {
        Exercise exercise = exerciseQueryService.findById(exerciseId); // Changed line
        exercise.markAsFavorite();
        exerciseRepository.save(exercise);
        log.info("Exercise {} marked as favorite by user {}", exerciseId, username);
    }

    // =======================
    // 🔍 EXERCISE DISCOVERY & SEARCH (NO SUBSCRIPTION FILTERING - ALL FREE!)
    // =======================

    public List<Exercise> findSuitableExercises(User user, List<String> availableEquipment,
                                                Exercise.DifficultyLevel maxDifficulty) {
        return exerciseRepository.findPublishedExercises().stream()
                .filter(exercise -> hasRequiredEquipment(exercise, availableEquipment))
                .filter(exercise -> exercise.getDifficultyLevel().ordinal() <= maxDifficulty.ordinal())
                .sorted(this::compareExercisesByRelevance)
                .collect(Collectors.toList());
    }

    public List<Exercise> findRecommendedExercises(User user, int limit) {

        if (user == null) {
            // Return popular exercises for anonymous users
            return exerciseRepository.findRecommendations(PageRequest.of(0, limit)).getContent();
        }

        List<Exercise> recentExercises = getRecentlyUsedExercises(user);
        List<String> preferredMuscleGroups = extractPreferredMuscleGroups(recentExercises);
        List<String> preferredTypes = extractPreferredExerciseTypes(user);

        if (preferredMuscleGroups.isEmpty() && preferredTypes.isEmpty()) {
            // New user - return popular exercises
            return exerciseRepository.findRecommendations(PageRequest.of(0, limit)).getContent();
        }

        // Get personalized recommendations
        List<Exercise> recommendations = new ArrayList<>();

        // Add exercises based on preferred muscle groups
        if (!preferredMuscleGroups.isEmpty()) {
            List<Exercise> muscleGroupRecs = exerciseRepository.findRecommendationsByMuscleGroups(
                    preferredMuscleGroups, PageRequest.of(0, limit / 2)).getContent();
            recommendations.addAll(muscleGroupRecs);
        }

        // Fill remaining slots with highly rated exercises user hasn't tried
        if (recommendations.size() < limit) {
            List<Exercise> untriedExercises = findUntriedHighlyRatedExercises(user, limit - recommendations.size());
            recommendations.addAll(untriedExercises);
        }

        // Remove duplicates and limit results
        return recommendations.stream()
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ===================================================================
    // 📊 RATING & ANALYTICS - 🆕 REAL IMPLEMENTATION
    // ===================================================================

    @Transactional
    public void rateExercise(Long exerciseId, User user, double rating, String comment, List<String> tags) {

        if (user == null) {
            throw new UserNotFoundException("User not found - authentication required");
        }

        if (rating < 0.0 || rating > 5.0) {
            throw new InvalidExerciseDataException("rating", "Rating must be between 0.0 and 5.0");
        }

        Exercise exercise = exerciseQueryService.findById(exerciseId);

        // Check if user already rated this exercise
        if (hasUserRatedExercise(user, exercise)) {
            // 🔧 FIXED - Now uses InvalidExerciseDataException
            throw new InvalidExerciseDataException("rating", "User has already rated this exercise");
        }

        // Create and save user rating
        UserExerciseRating userRating = new UserExerciseRating(user, exercise, rating, comment, tags);
        ratingRepository.save(userRating);

        // Update exercise rating
        updateExerciseRating(exercise, rating);
        exerciseRepository.save(exercise);

        // Record user's rating in history
        recordExerciseInHistory(user, exercise, UserExerciseHistory.CONTEXT_RATE);

        log.info("Exercise rated: {} - {} stars by user {}",
                exercise.getExerciseName(), rating, user.getId());
    }

    // Overloaded method for backward compatibility (rating only)
    @Transactional
    public void rateExercise(Long exerciseId, User user, double rating) {
        rateExercise(exerciseId, user, rating, null, null);
    }

    @Transactional
    public void recordExerciseUsage(Long exerciseId, User user) {
        if (user == null) {
            log.debug("User is null - skipping usage recording for exercise {}", exerciseId);
            return; // Silently skip if user is null (for anonymous usage)
        }
        Exercise exercise = exerciseQueryService.findById(exerciseId);

        exercise.incrementUsage();
        exerciseRepository.save(exercise);

        // Record user's exercise history
        recordExerciseInHistory(user, exercise, UserExerciseHistory.CONTEXT_VIEW);

        log.debug("Exercise usage recorded: {} by user {}", exercise.getExerciseName(), user.getId());
    }

    @Transactional
    public void recordWorkoutUsage(Long exerciseId, User user, Integer durationMinutes, String notes) {

        if (user == null) {
            throw new UserNotFoundException("User not found - authentication required");
        }

        Exercise exercise = exerciseQueryService.findById(exerciseId);

        exercise.incrementUsage();
        exerciseRepository.save(exercise);

        // Record detailed workout usage
        UserExerciseHistory history = new UserExerciseHistory(
                user, exercise, UserExerciseHistory.CONTEXT_WORKOUT, durationMinutes, notes);
        historyRepository.save(history);

        log.info("Workout usage recorded: {} - {} minutes by user {}",
                exercise.getExerciseName(), durationMinutes, user.getId());
    }

    public UserExerciseInsights getUserExerciseInsights(User user) {
        if (user == null) {
            // Return empty insights for null user
            return UserExerciseInsights.builder()
                    .totalExercisesTried(0)
                    .totalWorkouts(0)
                    .totalRatingsGiven(0)
                    .averageRatingGiven(0.0)
                    .preferredMuscleGroups(List.of())
                    .favoriteExercises(List.of())
                    .weeklyActivity(0)
                    .monthlyActivity(0)
                    .build();
        }

        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        // Get user statistics
        List<UserExerciseHistory> recentHistory = historyRepository.findUserRecentHistory(user.getId(), monthAgo);
        List<UserExerciseRating> userRatings = ratingRepository.findByUserId(user.getId());
        Long workoutCount = historyRepository.countUserWorkouts(user.getId(), monthAgo);

        // Calculate insights
        List<Object[]> preferredMuscleGroups = historyRepository.getUserPreferredMuscleGroups(user.getId(), monthAgo);
        List<Object[]> mostUsedExercises = historyRepository.getUserMostUsedExercises(user.getId());
        List<UserExerciseRating> favoriteExercises = ratingRepository.findUserHighRatedExercises(user.getId(), 4.0);

        return UserExerciseInsights.builder()
                .totalExercisesTried(getTriedExerciseIds(user).size())
                .totalWorkouts(workoutCount.intValue())
                .totalRatingsGiven(userRatings.size())
                .averageRatingGiven(userRatings.stream().mapToDouble(UserExerciseRating::getRating).average().orElse(0.0))
                .preferredMuscleGroups(preferredMuscleGroups.stream().limit(3).map(row -> (String) row[0]).collect(Collectors.toList()))
                .favoriteExercises(favoriteExercises.stream().limit(5).map(UserExerciseRating::getExercise).collect(Collectors.toList()))
                .weeklyActivity(historyRepository.countUsageSince(weekAgo).intValue())
                .monthlyActivity(recentHistory.size())
                .build();
    }

    // 🏋️ WORKOUT INTEGRATION

    public List<Exercise> buildWorkoutPlan(User user, ExerciseSelectionRequestDTO request) {
        validateExerciseSelectionRequestDTO(request); // 🔧 Changed method name

        // Use optimized repository query instead of multiple calls
        List<Exercise> candidateExercises = exerciseRepository.findOptimizedForWorkoutPlan(
                request.getTargetMuscleGroups(),
                request.getMaxDifficulty(),
                PageRequest.of(0, 100) // Reasonable limit for candidate selection
        );

        // Filter by equipment availability (in memory - more efficient than SQL for this)
        List<Exercise> availableExercises = candidateExercises.stream()
                .filter(ex -> hasRequiredEquipment(ex, request.getAvailableEquipment()))
                .collect(Collectors.toList());

        if (availableExercises.isEmpty()) {
            throw new InvalidExerciseDataException("availableEquipment",
                    "No exercises found matching your equipment and difficulty requirements");
        }

        // 🔧 COMMENT OUT: This functionality doesn't exist in WorkoutPlanRequest yet
        /*
        if (request.getPreferredWorkoutModes() != null && !request.getPreferredWorkoutModes().isEmpty()) {
            availableExercises = availableExercises.stream()
                    .filter(exercise -> {
                        Exercise.WorkoutTrackingMode mode = exercise.getWorkoutTrackingMode();
                        return request.getPreferredWorkoutModes().contains(mode);
                    })
                    .collect(Collectors.toList());
        }

        if (request.getHomeWorkout() != null && request.getHomeWorkout()) {
            availableExercises = availableExercises.stream()
                    .filter(Exercise::canDoAtHome)
                    .collect(Collectors.toList());
        }
        */

        // Rest of your existing logic stays the same...
        Map<String, List<Exercise>> exercisesByMuscleGroup = availableExercises.stream()
                .flatMap(ex -> ex.getTargetMuscleGroups().stream()
                        .filter(request.getTargetMuscleGroups()::contains)
                        .map(mg -> Map.entry(mg, ex)))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        List<Exercise> selectedExercises = new ArrayList<>();
        for (String muscleGroup : request.getTargetMuscleGroups()) {
            List<Exercise> muscleGroupExercises = exercisesByMuscleGroup.getOrDefault(muscleGroup, List.of())
                    .stream()
                    .sorted((e1, e2) -> Double.compare(
                            exerciseMapper.calculateRelevanceScore(e2),
                            exerciseMapper.calculateRelevanceScore(e1)
                    ))
                    .limit(request.getExercisesPerMuscleGroup())
                    .collect(Collectors.toList());

            selectedExercises.addAll(muscleGroupExercises);
        }

        adjustWorkoutForDuration(selectedExercises, request.getTargetDurationMinutes());
        return selectedExercises;
    }

    public boolean isExerciseCreatedByUser(Long exerciseId, Long userId) {
        if (exerciseId == null || userId == null) {
            return false;
        }

        Exercise exercise = exerciseQueryService.findById(exerciseId);
        if (exercise == null) {
            return false;
        }

        return userId.equals(exercise.getCreatedByUserId());
    }

    // ===================================================================
    // 🔧 HELPER METHODS - 🆕 REAL IMPLEMENTATIONS
    // ===================================================================

    // 🆕 REAL IMPLEMENTATION - Now uses database
    private boolean hasUserRatedExercise(User user, Exercise exercise) {
        if (user == null || exercise == null) {
            return false; // Safe default - user hasn't rated if user is null
        }
        return ratingRepository.existsByUserIdAndExerciseId(user.getId(), exercise.getId());
    }

    private void updateExerciseRating(Exercise exercise, double newRating) {
        Integer currentTotal = exercise.getTotalRatings();
        Double currentAverage = exercise.getAverageRating();

        if (currentTotal == null) currentTotal = 0;
        if (currentAverage == null) currentAverage = 0.0;

        double totalPoints = currentAverage * currentTotal;
        int newTotal = currentTotal + 1;
        double newAverage = (totalPoints + newRating) / newTotal;

        exercise.setTotalRatings(newTotal);
        exercise.setAverageRating(newAverage);
    }

    private void recordExerciseInHistory(User user, Exercise exercise, String context) {
        UserExerciseHistory history = new UserExerciseHistory(user, exercise, context);
        historyRepository.save(history);

        log.debug("Exercise history recorded: user={}, exercise={}, context={}",
                user.getId(), exercise.getId(), context);
    }

    private List<Exercise> getRecentlyUsedExercises(User user) {
        if (user == null) {
            return List.of();
        }
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        List<UserExerciseHistory> recentHistory = historyRepository.findUserRecentHistory(user.getId(), since);

        return recentHistory.stream()
                .map(UserExerciseHistory::getExercise)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> extractPreferredMuscleGroups(List<Exercise> recentExercises) {
        if (recentExercises.isEmpty()) {
            return List.of();
        }

        return recentExercises.stream()
                .flatMap(ex -> ex.getTargetMuscleGroups().stream())
                .collect(Collectors.groupingBy(group -> group, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // 🆕 NEW - Extract exercise type preferences from user history
    private List<String> extractPreferredExerciseTypes(User user) {
        if (user == null) {
            return List.of();
        }
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        List<Object[]> typePreferences = historyRepository.getUserPreferredExerciseTypes(user.getId(), since);

        return typePreferences.stream()
                .limit(2) // Top 2 preferred types
                .map(row -> ((Exercise.ExerciseType) row[0]).name())
                .collect(Collectors.toList());
    }

    // 🆕 NEW - Find exercises user hasn't tried
    private List<Exercise> findUntriedHighlyRatedExercises(User user, int limit) {
        List<Exercise> highlyRated = exerciseRepository.findHighlyRated();
        Set<Long> triedExerciseIds = getTriedExerciseIds(user);

        return highlyRated.stream()
                .filter(exercise -> !triedExerciseIds.contains(exercise.getId()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    // 🆕 NEW - Get set of exercise IDs user has tried
    private Set<Long> getTriedExerciseIds(User user) {
        if (user == null) {
            return Set.of();
        }
        List<UserExerciseHistory> userHistory = historyRepository.findByUserId(user.getId());
        return userHistory.stream()
                .map(history -> history.getExercise().getId())
                .collect(Collectors.toSet());
    }

    // 🔧 FIXED - Added null check for equipment list
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

    private int compareExercisesByRelevance(Exercise e1, Exercise e2) {
        // Use mapper method for consistency
        double score1 = exerciseMapper.calculateRelevanceScore(e1);
        double score2 = exerciseMapper.calculateRelevanceScore(e2);
        return Double.compare(score2, score1); // Higher score first
    }


    private boolean determineIfIsometric(Exercise exercise) {
        if (exercise.getExerciseName() == null) {
            return false;
        }

        String exerciseName = exercise.getExerciseName().toLowerCase();

        // List of isometric exercise patterns
        List<String> isometricPatterns = List.of(
                "plank", "wall sit", "dead hang", "bridge hold", "static hold",
                "isometric", "hold", "static", "wall squat", "glute bridge"
        );

        return isometricPatterns.stream()
                .anyMatch(pattern -> exerciseName.contains(pattern));
    }

    private void adjustWorkoutForDuration(List<Exercise> exercises, Integer targetDurationMinutes) {
        if (targetDurationMinutes == null) return;

        int currentDuration = exercises.stream()
                .mapToInt(ex -> ex.getEstimatedDurationMinutes() != null ? ex.getEstimatedDurationMinutes() : 0)
                .sum();

        // TODO: Logic to add or remove exercises to meet target duration
        log.debug("Adjusting workout duration from {} to {} minutes", currentDuration, targetDurationMinutes);
    }

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
                    exercise.getCreatedAt(), LocalDateTime.now()).toDays();
            if (daysSinceCreation < 30) {
                score += 0.1 * (30 - daysSinceCreation) / 30.0;
            }
        }

        return score;
    }

    private void validateExerciseSelectionRequestDTO(ExerciseSelectionRequestDTO request) {
        if (request.getTargetMuscleGroups() == null || request.getTargetMuscleGroups().isEmpty()) {
            throw new InvalidExerciseDataException("targetMuscleGroups", "At least one target muscle group is required");
        }

        if (request.getMaxDifficulty() == null) {
            throw new InvalidExerciseDataException("maxDifficulty", "Maximum difficulty level is required");
        }

        if (request.getExercisesPerMuscleGroup() != null &&
                (request.getExercisesPerMuscleGroup() < 1 || request.getExercisesPerMuscleGroup() > 10)) {
            throw new InvalidExerciseDataException("exercisesPerMuscleGroup",
                    "Exercises per muscle group must be between 1 and 10");
        }
    }

    // User Exercise Insights DTO
    @lombok.Data
    @lombok.Builder
    public static class UserExerciseInsights {
        private Integer totalExercisesTried;
        private Integer totalWorkouts;
        private Integer totalRatingsGiven;
        private Double averageRatingGiven;
        private List<String> preferredMuscleGroups;
        private List<Exercise> favoriteExercises;
        private Integer weeklyActivity;
        private Integer monthlyActivity;
    }
}
