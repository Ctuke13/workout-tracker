package com.chidituke.workout_tracker.repository.progress;

import com.chidituke.workout_tracker.model.progress.LeaderboardEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for LeaderboardEntry entity operations.
 */
@Repository
public interface LeaderboardEntryRepository extends JpaRepository<LeaderboardEntry, Long> {

    /**
     * Find leaderboard for a specific season and date.
     *
     * @param seasonId     Season ID
     * @param snapshotDate Snapshot date
     * @return List of leaderboard entries ordered by rank
     */
    List<LeaderboardEntry> findBySeasonIdAndSnapshotDateOrderByRankPositionAsc(
            Integer seasonId,
            LocalDate snapshotDate
    );

    /**
     * Find most recent leaderboard snapshot for a season.
     *
     * @param seasonId Season ID
     * @return List of most recent entries
     */
    @Query("SELECT le FROM LeaderboardEntry le WHERE le.seasonId = :seasonId " +
            "AND le.snapshotDate = (SELECT MAX(le2.snapshotDate) FROM LeaderboardEntry le2 WHERE le2.seasonId = :seasonId) " +
            "ORDER BY le.rankPosition ASC")
    List<LeaderboardEntry> findMostRecentForSeason(@Param("seasonId") Integer seasonId);

    /**
     * Find user's leaderboard history for a season.
     *
     * @param userId   User ID
     * @param seasonId Season ID
     * @return List of user's entries over time
     */
    List<LeaderboardEntry> findByUserIdAndSeasonIdOrderBySnapshotDateDesc(
            Long userId,
            Integer seasonId
    );

    /**
     * Find user's entry for specific season and date.
     *
     * @param userId       User ID
     * @param seasonId     Season ID
     * @param snapshotDate Snapshot date
     * @return Optional containing entry if found
     */
    Optional<LeaderboardEntry> findByUserIdAndSeasonIdAndSnapshotDate(
            Long userId,
            Integer seasonId,
            LocalDate snapshotDate
    );

    /**
     * Check if leaderboard snapshot exists for date.
     *
     * @param seasonId     Season ID
     * @param snapshotDate Snapshot date
     * @return true if snapshot exists
     */
    boolean existsBySeasonIdAndSnapshotDate(Integer seasonId, LocalDate snapshotDate);

    /**
     * Get top N users from most recent snapshot.
     *
     * @param seasonId Season ID
     * @param limit    Max results
     * @return List of top leaderboard entries
     */
    @Query("SELECT le FROM LeaderboardEntry le WHERE le.seasonId = :seasonId " +
            "AND le.snapshotDate = (SELECT MAX(le2.snapshotDate) FROM LeaderboardEntry le2 WHERE le2.seasonId = :seasonId) " +
            "ORDER BY le.rankPosition ASC LIMIT :limit")
    List<LeaderboardEntry> findTopNFromRecentSnapshot(
            @Param("seasonId") Integer seasonId,
            @Param("limit") int limit
    );

    /**
     * Get all snapshot dates for a season.
     *
     * @param seasonId Season ID
     * @return List of snapshot dates
     */
    @Query("SELECT DISTINCT le.snapshotDate FROM LeaderboardEntry le " +
            "WHERE le.seasonId = :seasonId ORDER BY le.snapshotDate DESC")
    List<LocalDate> findSnapshotDatesForSeason(@Param("seasonId") Integer seasonId);
}