package com.chidituke.workout_tracker.repository.progress;

import com.chidituke.workout_tracker.model.progress.UserProgression;
import com.chidituke.workout_tracker.model.progress.enums.Rank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for UserProgression entity operations.
 * <p>
 * Data Flow:
 * 1. Find user's progression when workout completed
 * 2. Update stats and XP
 * 3. Query leaderboards by seasonal/lifetime XP
 * 4. Find users for rank transitions
 */
@Repository
public interface UserProgressionRepository extends JpaRepository<UserProgression, Long> {

    /**
     * Find user progression by user ID.
     * Most common query - used whenever user completes workout.
     *
     * @param userId The user ID
     * @return Optional containing user progression
     */
    Optional<UserProgression> findByUserId(Long userId);

    /**
     * Check if user progression exists for a user.
     *
     * @param userId The user ID
     * @return true if exists
     */
    boolean existsByUserId(Long userId);

    // ========== LEADERBOARD QUERIES ==========

    /**
     * Get top N users by seasonal XP (for seasonal leaderboard).
     *
     * @param seasonId The season ID
     * @param limit    Max number of results
     * @return List of user progressions ordered by seasonal XP
     */
    @Query("SELECT up FROM UserProgression up " +
            "WHERE up.currentSeasonId = :seasonId " +
            "ORDER BY up.seasonalXp DESC")
    List<UserProgression> findTopBySeasonalXp(
            @Param("seasonId") Integer seasonId,
            @Param("limit") int limit
    );

    /**
     * Get top N users by lifetime XP (for all-time leaderboard).
     *
     * @param limit Max number of results
     * @return List of user progressions ordered by lifetime XP
     */
    @Query("SELECT up FROM UserProgression up ORDER BY up.lifetimeXp DESC")
    List<UserProgression> findTopByLifetimeXp(@Param("limit") int limit);

    /**
     * Get user's seasonal rank position.
     *
     * @param seasonId The season ID
     * @param userId   The user ID
     * @return The user's position (1 = first place)
     */
    @Query("SELECT COUNT(up) + 1 FROM UserProgression up " +
            "WHERE up.currentSeasonId = :seasonId " +
            "AND up.seasonalXp > (SELECT up2.seasonalXp FROM UserProgression up2 WHERE up2.userId = :userId)")
    Long findSeasonalRankPosition(
            @Param("seasonId") Integer seasonId,
            @Param("userId") Long userId
    );

    /**
     * Get user's lifetime rank position.
     *
     * @param userId The user ID
     * @return The user's position (1 = first place)
     */
    @Query("SELECT COUNT(up) + 1 FROM UserProgression up " +
            "WHERE up.lifetimeXp > (SELECT up2.lifetimeXp FROM UserProgression up2 WHERE up2.userId = :userId)")
    Long findLifetimeRankPosition(@Param("userId") Long userId);

    // ========== STREAK QUERIES ==========

    /**
     * Find all users with active streaks (worked out yesterday or today).
     * Used for daily streak maintenance jobs.
     *
     * @param cutoffDate The cutoff date (usually yesterday)
     * @return List of users with active streaks
     */
    @Query("SELECT up FROM UserProgression up " +
            "WHERE up.lastWorkoutDate >= :cutoffDate " +
            "AND up.currentStreakDays > 0")
    List<UserProgression> findUsersWithActiveStreaks(@Param("cutoffDate") LocalDate cutoffDate);

    /**
     * Find users who haven't worked out recently (streak likely broken).
     *
     * @param cutoffDate Date before which streak is broken
     * @return List of users with broken streaks
     */
    @Query("SELECT up FROM UserProgression up " +
            "WHERE up.lastWorkoutDate < :cutoffDate " +
            "AND up.currentStreakDays > 0")
    List<UserProgression> findUsersWithBrokenStreaks(@Param("cutoffDate") LocalDate cutoffDate);

    // ========== SEASON QUERIES ==========

    /**
     * Find all users in a specific season.
     * Used when transitioning seasons.
     *
     * @param seasonId The season ID
     * @return List of user progressions in that season
     */
    List<UserProgression> findByCurrentSeasonId(Integer seasonId);

    /**
     * Count users in a specific season.
     *
     * @param seasonId The season ID
     * @return Count of users
     */
    Long countByCurrentSeasonId(Integer seasonId);

    // ========== RANK QUERIES ==========

    /**
     * Find all users at a specific seasonal rank.
     *
     * @param rank The rank
     * @return List of users at that rank
     */
    List<UserProgression> findBySeasonalRank(Rank rank);

    /**
     * Find all users at a specific lifetime rank.
     *
     * @param rank The rank
     * @return List of users at that rank
     */
    List<UserProgression> findByLifetimeRank(Rank rank);

    // ========== STATISTICS QUERIES ==========

    /**
     * Get total XP earned across all users (for system stats).
     *
     * @return Total lifetime XP
     */
    @Query("SELECT SUM(up.lifetimeXp) FROM UserProgression up")
    Long getTotalXpEarnedAllUsers();

    /**
     * Get total workouts completed across all users.
     *
     * @return Total workouts
     */
    @Query("SELECT SUM(up.totalWorkoutsCompleted) FROM UserProgression up")
    Long getTotalWorkoutsAllUsers();
}