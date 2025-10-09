package com.chidituke.workout_tracker.model.progress;

import com.chidituke.workout_tracker.model.progress.enums.Rank;
import com.chidituke.workout_tracker.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a leaderboard snapshot entry.
 * <p>
 * Purpose:
 * - Capture weekly/monthly leaderboard standings
 * - Track user rankings over time
 * - Enable historical leaderboard queries
 * - Show rank changes and trends
 * <p>
 * Database Table: leaderboard_entries
 */
@Entity
@Table(name = "leaderboard_entries",
        indexes = {
                @Index(name = "idx_leaderboard_season_date", columnList = "season_id, snapshot_date"),
                @Index(name = "idx_leaderboard_user", columnList = "user_id"),
                @Index(name = "idx_leaderboard_rank", columnList = "rank_position")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leaderboard_entry_id")
    private Long leaderboardEntryId;

    // ========== RELATIONSHIPS ==========

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "season_id", nullable = false)
    private Integer seasonId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", insertable = false, updatable = false)
    private Season season;

    // ========== LEADERBOARD DATA ==========

    /**
     * Date this snapshot was taken
     */
    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    /**
     * User's rank position (1 = first place)
     */
    @Column(name = "rank_position", nullable = false)
    private Integer rankPosition;

    /**
     * User's seasonal XP at snapshot time
     */
    @Column(name = "seasonal_xp", nullable = false)
    private Integer seasonalXp;

    /**
     * User's seasonal rank at snapshot time
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "seasonal_rank", nullable = false, length = 20)
    private Rank seasonalRank;

    /**
     * User's seasonal tier at snapshot time
     */
    @Column(name = "seasonal_tier", nullable = false)
    private Integer seasonalTier;

    /**
     * Total workouts completed in season at snapshot time
     */
    @Column(name = "workouts_completed", nullable = false)
    private Integer workoutsCompleted;

    /**
     * Current streak at snapshot time
     */
    @Column(name = "current_streak", nullable = false)
    private Integer currentStreak;

    /**
     * Number of achievements unlocked at snapshot time
     */
    @Column(name = "achievements_count", nullable = false)
    private Integer achievementsCount;

    /**
     * Percentile ranking (0-100, where 100 = top 1%)
     */
    @Column(name = "percentile")
    private Double percentile;

    /**
     * Change in rank position since last snapshot
     * Positive = moved up, Negative = moved down, 0 = no change
     */
    @Column(name = "rank_change")
    private Integer rankChange;

    /**
     * When this entry was created
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Create a leaderboard entry from user progression.
     *
     * @param progression       User progression
     * @param seasonId          Season ID
     * @param snapshotDate      Snapshot date
     * @param rankPosition      User's rank position
     * @param achievementsCount Number of achievements unlocked
     * @param percentile        Percentile ranking
     * @return New LeaderboardEntry
     */
    public static LeaderboardEntry fromUserProgression(
            UserProgression progression,
            Integer seasonId,
            LocalDate snapshotDate,
            Integer rankPosition,
            Integer achievementsCount,
            Double percentile) {

        LeaderboardEntry entry = new LeaderboardEntry();
        entry.setUserId(progression.getUserId());
        entry.setSeasonId(seasonId);
        entry.setSnapshotDate(snapshotDate);
        entry.setRankPosition(rankPosition);
        entry.setSeasonalXp(progression.getSeasonalXp());
        entry.setSeasonalRank(progression.getSeasonalRank());
        entry.setSeasonalTier(progression.getSeasonalTier());
        entry.setWorkoutsCompleted(progression.getTotalWorkoutsCompleted());
        entry.setCurrentStreak(progression.getCurrentStreakDays());
        entry.setAchievementsCount(achievementsCount);
        entry.setPercentile(percentile);
        entry.setRankChange(0); // Calculate separately if comparing to previous

        return entry;
    }
}