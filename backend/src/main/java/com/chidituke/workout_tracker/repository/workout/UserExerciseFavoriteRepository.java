package com.chidituke.workout_tracker.repository.workout;

import com.chidituke.workout_tracker.model.workout.UserExerciseFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository for managing user exercise favorites with optimized queries for performance
 */
@Repository
public interface UserExerciseFavoriteRepository extends JpaRepository<UserExerciseFavorite, Long> {

    // ==================== BASIC QUERIES ====================

    /**
     * Find a specific favorite by user and exercise
     */
    Optional<UserExerciseFavorite> findByUserIdAndExerciseId(Long userId, Long exerciseId);

    /**
     * Check if a user has favorited a specific exercise
     */
    boolean existsByUserIdAndExerciseId(Long userId, Long exerciseId);

    /**
     * Get all exercise IDs that a user has favorited
     */
    @Query("SELECT f.exerciseId FROM UserExerciseFavorite f WHERE f.userId = :userId ORDER BY f.createdAt DESC")
    List<Long> findExerciseIdsByUserId(@Param("userId") Long userId);

    /**
     * Get user's favorites with pagination
     */
    Page<UserExerciseFavorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Get user's favorites as a list
     */
    List<UserExerciseFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Count user's total favorites
     */
    long countByUserId(Long userId);

    // ==================== EXERCISE-BASED QUERIES ====================

    /**
     * Get all users who favorited a specific exercise
     */
    List<UserExerciseFavorite> findByExerciseIdOrderByCreatedAtDesc(Long exerciseId);

    /**
     * Count how many users favorited a specific exercise
     */
    long countByExerciseId(Long exerciseId);

    /**
     * Get user IDs who favorited a specific exercise
     */
    @Query("SELECT f.userId FROM UserExerciseFavorite f WHERE f.exerciseId = :exerciseId")
    List<Long> findUserIdsByExerciseId(@Param("exerciseId") Long exerciseId);

    // ==================== BATCH OPERATIONS ====================

    /**
     * Check multiple exercises for favorite status by a user
     */
    @Query("SELECT f.exerciseId FROM UserExerciseFavorite f WHERE f.userId = :userId AND f.exerciseId IN :exerciseIds")
    Set<Long> findFavoritedExerciseIds(@Param("userId") Long userId, @Param("exerciseIds") List<Long> exerciseIds);

    /**
     * Remove favorite by user and exercise
     */
    @Modifying
    @Query("DELETE FROM UserExerciseFavorite f WHERE f.userId = :userId AND f.exerciseId = :exerciseId")
    void deleteByUserIdAndExerciseId(@Param("userId") Long userId, @Param("exerciseId") Long exerciseId);

    /**
     * Remove all favorites for a user
     */
    @Modifying
    @Query("DELETE FROM UserExerciseFavorite f WHERE f.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    /**
     * Remove all favorites for an exercise (when exercise is deleted)
     */
    @Modifying
    @Query("DELETE FROM UserExerciseFavorite f WHERE f.exerciseId = :exerciseId")
    void deleteAllByExerciseId(@Param("exerciseId") Long exerciseId);

    // ==================== ANALYTICS QUERIES ====================

    /**
     * Get most favorited exercises
     */
    @Query("SELECT f.exerciseId, COUNT(f) as favoriteCount " +
            "FROM UserExerciseFavorite f " +
            "GROUP BY f.exerciseId " +
            "ORDER BY favoriteCount DESC")
    List<Object[]> findMostFavoritedExercises();

    /**
     * Get most favorited exercises with limit
     */
    @Query("SELECT f.exerciseId, COUNT(f) as favoriteCount " +
            "FROM UserExerciseFavorite f " +
            "GROUP BY f.exerciseId " +
            "ORDER BY favoriteCount DESC " +
            "LIMIT :limit")
    List<Object[]> findMostFavoritedExercises(@Param("limit") int limit);

    /**
     * Get users with most favorites
     */
    @Query("SELECT f.userId, COUNT(f) as favoriteCount " +
            "FROM UserExerciseFavorite f " +
            "GROUP BY f.userId " +
            "ORDER BY favoriteCount DESC")
    List<Object[]> findUsersWithMostFavorites();

    /**
     * Get favorite statistics for admin dashboard
     */
    @Query("SELECT COUNT(DISTINCT f.userId) as totalUsers, " +
            "COUNT(DISTINCT f.exerciseId) as totalExercises, " +
            "COUNT(f) as totalFavorites " +
            "FROM UserExerciseFavorite f")
    Object[] getFavoriteStatistics();

    // ==================== TIME-BASED QUERIES ====================

    /**
     * Get recently added favorites
     */
    @Query("SELECT f FROM UserExerciseFavorite f WHERE f.createdAt >= :since ORDER BY f.createdAt DESC")
    List<UserExerciseFavorite> findRecentFavorites(@Param("since") LocalDateTime since);

    /**
     * Get user's recent favorites
     */
    @Query("SELECT f FROM UserExerciseFavorite f WHERE f.userId = :userId AND f.createdAt >= :since ORDER BY f.createdAt DESC")
    List<UserExerciseFavorite> findUserRecentFavorites(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    /**
     * Count favorites added in a time period
     */
    @Query("SELECT COUNT(f) FROM UserExerciseFavorite f WHERE f.createdAt BETWEEN :start AND :end")
    long countFavoritesInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Get daily favorite counts for analytics
     */
    @Query("SELECT DATE(f.createdAt) as favoriteDate, COUNT(f) as count " +
            "FROM UserExerciseFavorite f " +
            "WHERE f.createdAt >= :since " +
            "GROUP BY DATE(f.createdAt) " +
            "ORDER BY favoriteDate DESC")
    List<Object[]> getDailyFavoriteCounts(@Param("since") LocalDateTime since);

    // ==================== ADVANCED QUERIES ====================

    /**
     * Find users who favorited similar exercises (collaborative filtering)
     */
    @Query("SELECT DISTINCT f2.userId " +
            "FROM UserExerciseFavorite f1, UserExerciseFavorite f2 " +
            "WHERE f1.userId = :userId " +
            "AND f2.userId != :userId " +
            "AND f1.exerciseId = f2.exerciseId " +
            "GROUP BY f2.userId " +
            "HAVING COUNT(f2.exerciseId) >= :minCommonFavorites")
    List<Long> findUsersWithSimilarFavorites(@Param("userId") Long userId, @Param("minCommonFavorites") int minCommonFavorites);

    /**
     * Get exercises favorited by users with similar preferences
     */
    @Query("SELECT DISTINCT f2.exerciseId, COUNT(f2.exerciseId) as frequency " +
            "FROM UserExerciseFavorite f1, UserExerciseFavorite f2 " +
            "WHERE f1.userId = :userId " +
            "AND f2.userId != :userId " +
            "AND f1.exerciseId = f2.exerciseId " +
            "AND f2.exerciseId NOT IN (SELECT f3.exerciseId FROM UserExerciseFavorite f3 WHERE f3.userId = :userId) " +
            "GROUP BY f2.exerciseId " +
            "ORDER BY frequency DESC")
    List<Object[]> findRecommendedExercisesByFavorites(@Param("userId") Long userId);

    /**
     * Get favorite trends (exercises gaining popularity)
     */
    @Query("SELECT f.exerciseId, COUNT(f) as recentCount " +
            "FROM UserExerciseFavorite f " +
            "WHERE f.createdAt >= :since " +
            "GROUP BY f.exerciseId " +
            "ORDER BY recentCount DESC")
    List<Object[]> getTrendingFavorites(@Param("since") LocalDateTime since);

    // ==================== BULK OPERATIONS ====================

    /**
     * Check if any of the exercises are favorited by user (for bulk operations)
     */
    @Query("SELECT COUNT(f) > 0 FROM UserExerciseFavorite f WHERE f.userId = :userId AND f.exerciseId IN :exerciseIds")
    boolean hasAnyFavorites(@Param("userId") Long userId, @Param("exerciseIds") List<Long> exerciseIds);

    /**
     * Get favorite counts for multiple exercises
     */
    @Query("SELECT f.exerciseId, COUNT(f) " +
            "FROM UserExerciseFavorite f " +
            "WHERE f.exerciseId IN :exerciseIds " +
            "GROUP BY f.exerciseId")
    List<Object[]> getFavoriteCountsForExercises(@Param("exerciseIds") List<Long> exerciseIds);

    // ==================== CLEANUP QUERIES ====================

    /**
     * Find orphaned favorites (exercise or user no longer exists)
     * Note: These should be handled by foreign key constraints, but useful for data integrity checks
     */
    @Query("SELECT f FROM UserExerciseFavorite f WHERE f.exercise IS NULL OR f.user IS NULL")
    List<UserExerciseFavorite> findOrphanedFavorites();

    /**
     * Delete old favorites (for data retention policies)
     */
    @Modifying
    @Query("DELETE FROM UserExerciseFavorite f WHERE f.createdAt < :cutoffDate")
    int deleteOldFavorites(@Param("cutoffDate") LocalDateTime cutoffDate);
}
