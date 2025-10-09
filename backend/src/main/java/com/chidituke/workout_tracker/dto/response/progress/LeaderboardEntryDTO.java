package com.chidituke.workout_tracker.dto.response.progress;

import com.chidituke.workout_tracker.model.progress.LeaderboardEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Response DTO for leaderboard entry.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntryDTO {

    // User info (would typically join with User entity)
    private Long userId;
    private String username;
    private String profileImageUrl;

    // Season info
    private Integer seasonId;
    private LocalDate snapshotDate;

    // Ranking
    private Integer rankPosition;
    private Integer seasonalXp;
    private String seasonalRank;
    private Integer seasonalTier;
    private String rankIcon;

    // Stats
    private Integer workoutsCompleted;
    private Integer currentStreak;
    private Integer achievementsCount;
    private Double percentile;

    // Change tracking
    private Integer rankChange;
    private String rankChangeDirection; // "up", "down", "same"

    /**
     * Convert entity to DTO
     */
    public static LeaderboardEntryDTO fromEntity(LeaderboardEntry entry) {
        if (entry == null) return null;

        String changeDirection = "same";
        if (entry.getRankChange() != null) {
            if (entry.getRankChange() > 0) changeDirection = "up";
            else if (entry.getRankChange() < 0) changeDirection = "down";
        }

        return LeaderboardEntryDTO.builder()
                .userId(entry.getUserId())
                .username(entry.getUser() != null ? entry.getUser().getUsername() : null)
                .profileImageUrl(entry.getUser() != null ? entry.getUser().getProfileImageUrl() : null)
                .seasonId(entry.getSeasonId())
                .snapshotDate(entry.getSnapshotDate())
                .rankPosition(entry.getRankPosition())
                .seasonalXp(entry.getSeasonalXp())
                .seasonalRank(entry.getSeasonalRank().name())
                .seasonalTier(entry.getSeasonalTier())
                .rankIcon(entry.getSeasonalRank().getIcon())
                .workoutsCompleted(entry.getWorkoutsCompleted())
                .currentStreak(entry.getCurrentStreak())
                .achievementsCount(entry.getAchievementsCount())
                .percentile(entry.getPercentile())
                .rankChange(entry.getRankChange())
                .rankChangeDirection(changeDirection)
                .build();
    }
}