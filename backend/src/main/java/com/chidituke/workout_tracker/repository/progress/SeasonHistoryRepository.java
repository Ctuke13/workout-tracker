package com.chidituke.workout_tracker.repository.progress;

import com.chidituke.workout_tracker.model.progress.SeasonHistory;
import com.chidituke.workout_tracker.model.progress.enums.Rank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SeasonHistory entity operations.
 */
@Repository
public interface SeasonHistoryRepository extends JpaRepository<SeasonHistory, Long> {

    /**
     * Find all season history for a user.
     *
     * @param userId User ID
     * @return List of user's season history ordered by most recent
     */
    List<SeasonHistory> findByUserIdOrderByCompletedAtDesc(Long userId);

    /**
     * Find user's history for a specific season.
     *
     * @param userId   User ID
     * @param seasonId Season ID
     * @return Optional containing season history if found
     */
    Optional<SeasonHistory> findByUserIdAndSeasonId(Long userId, Integer seasonId);

    /**
     * Check if user has history for a season.
     *
     * @param userId   User ID
     * @param seasonId Season ID
     * @return true if history exists
     */
    boolean existsByUserIdAndSeasonId(Long userId, Integer seasonId);

    /**
     * Find all history entries for a season.
     *
     * @param seasonId Season ID
     * @return List of all users' history for that season
     */
    List<SeasonHistory> findBySeasonIdOrderByFinalSeasonalXpDesc(Integer seasonId);

    /**
     * Find top performers for a season by XP.
     *
     * @param seasonId Season ID
     * @param limit    Max results
     * @return List of top season histories
     */
    @Query("SELECT sh FROM SeasonHistory sh WHERE sh.seasonId = :seasonId " +
            "ORDER BY sh.finalSeasonalXp DESC LIMIT :limit")
    List<SeasonHistory> findTopPerformersForSeason(
            @Param("seasonId") Integer seasonId,
            @Param("limit") int limit
    );

    /**
     * Find users who achieved a specific rank in a season.
     *
     * @param seasonId Season ID
     * @param rank     Rank achieved
     * @return List of users who reached that rank
     */
    List<SeasonHistory> findBySeasonIdAndFinalSeasonalRank(Integer seasonId, Rank rank);

    /**
     * Count users who participated in a season.
     *
     * @param seasonId Season ID
     * @return Count of participants
     */
    Long countBySeasonId(Integer seasonId);

    /**
     * Get average XP earned in a season.
     *
     * @param seasonId Season ID
     * @return Average seasonal XP
     */
    @Query("SELECT AVG(sh.finalSeasonalXp) FROM SeasonHistory sh WHERE sh.seasonId = :seasonId")
    Double getAverageXpForSeason(@Param("seasonId") Integer seasonId);

    /**
     * Get user's rank in a specific season by percentile.
     *
     * @param userId   User ID
     * @param seasonId Season ID
     * @return User's percentile ranking
     */
    @Query("SELECT sh.finalPercentile FROM SeasonHistory sh " +
            "WHERE sh.userId = :userId AND sh.seasonId = :seasonId")
    Double getUserPercentileForSeason(
            @Param("userId") Long userId,
            @Param("seasonId") Integer seasonId
    );

    /**
     * Find user's best season by XP.
     *
     * @param userId User ID
     * @return Optional containing best season history
     */
    @Query("SELECT sh FROM SeasonHistory sh WHERE sh.userId = :userId " +
            "ORDER BY sh.finalSeasonalXp DESC LIMIT 1")
    Optional<SeasonHistory> findUserBestSeason(@Param("userId") Long userId);

    /**
     * Count seasons user participated in.
     *
     * @param userId User ID
     * @return Count of seasons
     */
    Long countByUserId(Long userId);

    /**
     * Get user's season progression over time.
     * Shows XP earned in each season.
     *
     * @param userId User ID
     * @return List ordered chronologically
     */
    @Query("SELECT sh FROM SeasonHistory sh WHERE sh.userId = :userId " +
            "ORDER BY sh.season.startDate ASC")
    List<SeasonHistory> findUserSeasonProgression(@Param("userId") Long userId);
}