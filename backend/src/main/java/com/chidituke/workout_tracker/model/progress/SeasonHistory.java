package com.chidituke.workout_tracker.model.progress;

import com.chidituke.workout_tracker.model.progress.enums.Rank;
import com.chidituke.workout_tracker.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a user's archived season performance.
 * Created when a season ends to preserve final standings.
 * <p>
 * Purpose:
 * - Archive user's final season stats
 * - Track season-over-season progression
 * - Enable historical comparisons
 * - Show career highlights
 * <p>
 * Database Table: season_history
 */
@Entity
@Table(name = "season_history",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "season_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeasonHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "season_history_id")
    private Long seasonHistoryId;

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

    // ========== FINAL SEASON STATS ==========

    /**
     * Final seasonal XP earned
     */
    @Column(name = "final_seasonal_xp", nullable = false)
    private Integer finalSeasonalXp;

    /**
     * Final seasonal rank achieved
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "final_seasonal_rank", nullable = false, length = 20)
    private Rank finalSeasonalRank;

    /**
     * Final seasonal tier
     */
    @Column(name = "final_seasonal_tier", nullable = false)
    private Integer finalSeasonalTier;

    /**
     * Final percentile ranking (95.50 = top 4.5%)
     */
    @Column(name = "final_percentile", precision = 5, scale = 2)
    private BigDecimal finalPercentile;

    // ========== SEASON ACHIEVEMENTS ==========

    /**
     * Total workouts completed this season
     */
    @Column(name = "total_workouts_this_season", nullable = false)
    private Integer totalWorkoutsThisSeason;

    /**
     * Highest streak achieved this season
     */
    @Column(name = "highest_streak_this_season", nullable = false)
    private Integer highestStreakThisSeason;

    /**
     * Number of perfect weeks (7/7 workouts)
     */
    @Column(name = "perfect_weeks_this_season", nullable = false)
    private Integer perfectWeeksThisSeason;

    /**
     * When this season was completed/archived
     */
    @Column(name = "completed_at", nullable = false, updatable = false)
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        completedAt = LocalDateTime.now();
    }

    /**
     * Create season history from user progression at season end.
     *
     * @param progression        User progression
     * @param seasonId           Season ID
     * @param percentile         Final percentile ranking
     * @param workoutsThisSeason Workouts completed in season
     * @param perfectWeeks       Number of perfect weeks
     * @return New SeasonHistory
     */
    public static SeasonHistory fromUserProgression(
            UserProgression progression,
            Integer seasonId,
            BigDecimal percentile,
            Integer workoutsThisSeason,
            Integer perfectWeeks) {

        SeasonHistory history = new SeasonHistory();
        history.setUserId(progression.getUserId());
        history.setSeasonId(seasonId);
        history.setFinalSeasonalXp(progression.getSeasonalXp());
        history.setFinalSeasonalRank(progression.getSeasonalRank());
        history.setFinalSeasonalTier(progression.getSeasonalTier());
        history.setFinalPercentile(percentile);
        history.setTotalWorkoutsThisSeason(workoutsThisSeason);
        history.setHighestStreakThisSeason(progression.getLongestStreakDays());
        history.setPerfectWeeksThisSeason(perfectWeeks);

        return history;
    }
}