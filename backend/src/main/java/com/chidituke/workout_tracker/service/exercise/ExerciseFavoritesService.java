package com.chidituke.workout_tracker.service.exercise;

import com.chidituke.workout_tracker.exceptions.common.ResourceNotFoundException;
import com.chidituke.workout_tracker.exceptions.common.DuplicateResourceException;
import com.chidituke.workout_tracker.model.workout.Exercise;
import com.chidituke.workout_tracker.model.workout.UserExerciseFavorite;
import com.chidituke.workout_tracker.repository.workout.ExerciseRepository;
import com.chidituke.workout_tracker.repository.workout.UserExerciseFavoriteRepository;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl; // ✅ ADD THIS IMPORT
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing user exercise favorites with comprehensive business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExerciseFavoritesService {

    private final UserExerciseFavoriteRepository favoriteRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;

    // ==================== CORE FAVORITE OPERATIONS ====================

    /**
     * Add an exercise to user's favorites
     */
    public UserExerciseFavorite addToFavorites(Long userId, Long exerciseId) {
        log.debug("Adding exercise {} to favorites for user {}", exerciseId, userId);

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        // Validate exercise exists and is published
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found with ID: " + exerciseId));

        if (!exercise.isPublished()) {
            throw new ResourceNotFoundException("Exercise is not available for favoriting");
        }

        // Check if already favorited
        if (favoriteRepository.existsByUserIdAndExerciseId(userId, exerciseId)) {
            throw new DuplicateResourceException("Exercise is already in favorites");
        }

        // Create and save favorite
        UserExerciseFavorite favorite = UserExerciseFavorite.builder()
                .userId(userId)
                .exerciseId(exerciseId)
                .createdAt(LocalDateTime.now())
                .build();

        UserExerciseFavorite saved = favoriteRepository.save(favorite);
        log.info("Added exercise {} to favorites for user {}", exerciseId, userId);

        return saved;
    }

    /**
     * Remove an exercise from user's favorites
     */
    public void removeFromFavorites(Long userId, Long exerciseId) {
        log.debug("Removing exercise {} from favorites for user {}", exerciseId, userId);

        // Check if favorite exists
        if (!favoriteRepository.existsByUserIdAndExerciseId(userId, exerciseId)) {
            throw new ResourceNotFoundException("Exercise is not in favorites");
        }

        favoriteRepository.deleteByUserIdAndExerciseId(userId, exerciseId);
        log.info("Removed exercise {} from favorites for user {}", exerciseId, userId);
    }

    /**
     * Toggle favorite status (add if not favorited, remove if favorited)
     */
    public boolean toggleFavorite(Long userId, Long exerciseId) {
        log.debug("Toggling favorite status for exercise {} and user {}", exerciseId, userId);

        if (favoriteRepository.existsByUserIdAndExerciseId(userId, exerciseId)) {
            removeFromFavorites(userId, exerciseId);
            return false; // Removed from favorites
        } else {
            addToFavorites(userId, exerciseId);
            return true; // Added to favorites
        }
    }

    // ==================== QUERY OPERATIONS ====================

    /**
     * Check if user has favorited a specific exercise
     */
    @Transactional(readOnly = true)
    public boolean isFavorited(Long userId, Long exerciseId) {
        return favoriteRepository.existsByUserIdAndExerciseId(userId, exerciseId);
    }

    /**
     * Get user's favorite exercises with full exercise details
     */
    @Transactional(readOnly = true)
    public List<Exercise> getUserFavoriteExercises(Long userId) {
        log.debug("Getting favorite exercises for user {}", userId);

        List<Long> exerciseIds = favoriteRepository.findExerciseIdsByUserId(userId);

        if (exerciseIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Get exercises and maintain the order from favorites
        Map<Long, Exercise> exerciseMap = exerciseRepository.findAllById(exerciseIds)
                .stream()
                .collect(Collectors.toMap(Exercise::getId, exercise -> exercise));

        return exerciseIds.stream()
                .map(exerciseMap::get)
                .filter(Objects::nonNull)
                .filter(Exercise::isPublished) // Only return published exercises
                .collect(Collectors.toList());
    }

    /**
     * Get user's favorite exercises with pagination
     * ✅ FIXED: Using PageImpl instead of Page.of()
     */
    @Transactional(readOnly = true)
    public Page<Exercise> getUserFavoriteExercises(Long userId, Pageable pageable) {
        log.debug("Getting favorite exercises for user {} with pagination", userId);

        Page<UserExerciseFavorite> favoritePage = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<Long> exerciseIds = favoritePage.getContent()
                .stream()
                .map(UserExerciseFavorite::getExerciseId)
                .collect(Collectors.toList());

        if (exerciseIds.isEmpty()) {
            return Page.empty(pageable);
        }

        Map<Long, Exercise> exerciseMap = exerciseRepository.findAllById(exerciseIds)
                .stream()
                .filter(Exercise::isPublished)
                .collect(Collectors.toMap(Exercise::getId, exercise -> exercise));

        List<Exercise> exercises = exerciseIds.stream()
                .map(exerciseMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // ✅ FIXED: Use PageImpl constructor instead of Page.of()
        return new PageImpl<>(exercises, pageable, favoritePage.getTotalElements());
    }

    /**
     * Get exercise IDs that user has favorited (for checking favorite status in bulk)
     */
    @Transactional(readOnly = true)
    public Set<Long> getUserFavoriteExerciseIds(Long userId) {
        return new HashSet<>(favoriteRepository.findExerciseIdsByUserId(userId));
    }

    /**
     * Check favorite status for multiple exercises
     */
    @Transactional(readOnly = true)
    public Map<Long, Boolean> checkFavoriteStatus(Long userId, List<Long> exerciseIds) {
        Set<Long> favoritedIds = favoriteRepository.findFavoritedExerciseIds(userId, exerciseIds);

        return exerciseIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        favoritedIds::contains
                ));
    }

    /**
     * Count user's total favorites
     */
    @Transactional(readOnly = true)
    public long getUserFavoriteCount(Long userId) {
        return favoriteRepository.countByUserId(userId);
    }

    // ==================== ANALYTICS & INSIGHTS ====================

    /**
     * Get most favorited exercises
     */
    @Transactional(readOnly = true)
    public List<Exercise> getMostFavoritedExercises(int limit) {
        log.debug("Getting most favorited exercises with limit {}", limit);

        List<Object[]> favoriteStats = favoriteRepository.findMostFavoritedExercises(limit);

        List<Long> exerciseIds = favoriteStats.stream()
                .map(stat -> (Long) stat[0])
                .collect(Collectors.toList());

        if (exerciseIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Exercise> exerciseMap = exerciseRepository.findAllById(exerciseIds)
                .stream()
                .filter(Exercise::isPublished)
                .collect(Collectors.toMap(Exercise::getId, exercise -> exercise));

        return exerciseIds.stream()
                .map(exerciseMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get favorite count for a specific exercise
     */
    @Transactional(readOnly = true)
    public long getExerciseFavoriteCount(Long exerciseId) {
        return favoriteRepository.countByExerciseId(exerciseId);
    }

    /**
     * Get trending favorites (recently added)
     */
    @Transactional(readOnly = true)
    public List<Exercise> getTrendingFavorites(int days, int limit) {
        log.debug("Getting trending favorites from last {} days with limit {}", days, limit);

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Object[]> trendingStats = favoriteRepository.getTrendingFavorites(since);

        List<Long> exerciseIds = trendingStats.stream()
                .limit(limit)
                .map(stat -> (Long) stat[0])
                .collect(Collectors.toList());

        if (exerciseIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Exercise> exerciseMap = exerciseRepository.findAllById(exerciseIds)
                .stream()
                .filter(Exercise::isPublished)
                .collect(Collectors.toMap(Exercise::getId, exercise -> exercise));

        return exerciseIds.stream()
                .map(exerciseMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get comprehensive favorite statistics
     */
    @Transactional(readOnly = true)
    public FavoriteStatistics getFavoriteStatistics() {
        Object[] stats = favoriteRepository.getFavoriteStatistics();

        if (stats == null || stats.length != 4) {
            return FavoriteStatistics.builder()
                    .totalUsers(0L)
                    .totalExercises(0L)
                    .totalFavorites(0L)
                    .averageFavoritesPerUser(0.0)
                    .build();
        }

        return FavoriteStatistics.builder()
                .totalUsers((Long) stats[0])
                .totalExercises((Long) stats[1])
                .totalFavorites((Long) stats[2])
                .averageFavoritesPerUser((Double) stats[3])
                .build();
    }

    // ==================== RECOMMENDATIONS ====================

    /**
     * Get recommended exercises based on user's favorites (collaborative filtering)
     */
    @Transactional(readOnly = true)
    public List<Exercise> getRecommendedExercises(Long userId, int limit) {
        log.debug("Getting recommended exercises for user {} with limit {}", userId, limit);

        List<Object[]> recommendations = favoriteRepository.findRecommendedExercisesByFavorites(userId);

        List<Long> exerciseIds = recommendations.stream()
                .limit(limit)
                .map(rec -> (Long) rec[0])
                .collect(Collectors.toList());

        if (exerciseIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Exercise> exerciseMap = exerciseRepository.findAllById(exerciseIds)
                .stream()
                .filter(Exercise::isPublished)
                .collect(Collectors.toMap(Exercise::getId, exercise -> exercise));

        return exerciseIds.stream()
                .map(exerciseMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Find users with similar favorite preferences
     */
    @Transactional(readOnly = true)
    public List<Long> findUsersWithSimilarFavorites(Long userId, int minCommonFavorites) {
        return favoriteRepository.findUsersWithSimilarFavorites(userId, minCommonFavorites);
    }

    // ==================== BULK OPERATIONS ====================

    /**
     * Add multiple exercises to favorites
     */
    public List<UserExerciseFavorite> addMultipleToFavorites(Long userId, List<Long> exerciseIds) {
        log.debug("Adding {} exercises to favorites for user {}", exerciseIds.size(), userId);

        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        // Get existing favorites to avoid duplicates
        Set<Long> existingFavorites = favoriteRepository.findFavoritedExerciseIds(userId, exerciseIds);

        // Filter out already favorited exercises
        List<Long> newFavoriteIds = exerciseIds.stream()
                .filter(id -> !existingFavorites.contains(id))
                .collect(Collectors.toList());

        if (newFavoriteIds.isEmpty()) {
            log.info("All requested exercises are already favorited by user {}", userId);
            return Collections.emptyList();
        }

        // Validate exercises exist and are published
        List<Exercise> exercises = exerciseRepository.findAllById(newFavoriteIds)
                .stream()
                .filter(Exercise::isPublished)
                .collect(Collectors.toList());

        if (exercises.isEmpty()) {
            throw new ResourceNotFoundException("No valid exercises found for favoriting");
        }

        // Create favorites
        List<UserExerciseFavorite> newFavorites = exercises.stream()
                .map(exercise -> UserExerciseFavorite.builder()
                        .userId(userId)
                        .exerciseId(exercise.getId())
                        .createdAt(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());

        List<UserExerciseFavorite> saved = favoriteRepository.saveAll(newFavorites);
        log.info("Added {} exercises to favorites for user {}", saved.size(), userId);

        return saved;
    }

    /**
     * Remove multiple exercises from favorites
     */
    public void removeMultipleFromFavorites(Long userId, List<Long> exerciseIds) {
        log.debug("Removing {} exercises from favorites for user {}", exerciseIds.size(), userId);

        exerciseIds.forEach(exerciseId -> {
            if (favoriteRepository.existsByUserIdAndExerciseId(userId, exerciseId)) {
                favoriteRepository.deleteByUserIdAndExerciseId(userId, exerciseId);
            }
        });

        log.info("Removed exercises from favorites for user {}", userId);
    }

    /**
     * Clear all favorites for a user
     */
    public void clearAllUserFavorites(Long userId) {
        log.debug("Clearing all favorites for user {}", userId);

        long count = favoriteRepository.countByUserId(userId);
        favoriteRepository.deleteAllByUserId(userId);

        log.info("Cleared {} favorites for user {}", count, userId);
    }

    // ==================== ADMIN OPERATIONS ====================

    /**
     * Clean up favorites for deleted exercises (admin use)
     */
    public void cleanupDeletedExerciseFavorites(Long exerciseId) {
        log.debug("Cleaning up favorites for deleted exercise {}", exerciseId);

        long count = favoriteRepository.countByExerciseId(exerciseId);
        favoriteRepository.deleteAllByExerciseId(exerciseId);

        log.info("Cleaned up {} favorites for deleted exercise {}", count, exerciseId);
    }

    /**
     * Get recent favorite activity
     */
    @Transactional(readOnly = true)
    public List<UserExerciseFavorite> getRecentFavoriteActivity(int days, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return favoriteRepository.findRecentFavorites(since)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ==================== INNER CLASSES ====================

    /**
     * Statistics about the favorites system
     */
    @lombok.Data
    @lombok.Builder
    public static class FavoriteStatistics {
        private Long totalUsers;
        private Long totalExercises;
        private Long totalFavorites;
        private Double averageFavoritesPerUser;
    }
}