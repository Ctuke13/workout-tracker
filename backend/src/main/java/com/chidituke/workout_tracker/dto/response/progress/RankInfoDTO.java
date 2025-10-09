package com.chidituke.workout_tracker.dto.response.progress;

import com.chidituke.workout_tracker.model.progress.UserProgression;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for user rank information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankInfoDTO {

    // Seasonal rank
    private String seasonalRank;
    private Integer seasonalTier;
    private Integer seasonalXp;
    private String seasonalRankIcon;
    private Long seasonalPosition;
    private Integer xpToNextSeasonalRank;
    private String nextSeasonalRank;
    private Double seasonalRankProgress;

    // Lifetime rank
    private String lifetimeRank;
    private Integer lifetimeTier;
    private Integer lifetimeXp;
    private String lifetimeRankIcon;
    private Long lifetimePosition;
    private Integer xpToNextLifetimeRank;
    private String nextLifetimeRank;
    private Double lifetimeRankProgress;

    /**
     * Convert entity to DTO with position data
     */
    public static RankInfoDTO fromProgression(
            UserProgression progression,
            Long seasonalPosition,
            Long lifetimePosition) {

        if (progression == null) return null;

        return RankInfoDTO.builder()
                .seasonalRank(progression.getSeasonalRank().name())
                .seasonalTier(progression.getSeasonalTier())
                .seasonalXp(progression.getSeasonalXp())
                .seasonalRankIcon(progression.getSeasonalRank().getIcon())
                .seasonalPosition(seasonalPosition)
                .xpToNextSeasonalRank(progression.getXpToNextSeasonalRank())
                .nextSeasonalRank(progression.getSeasonalRank().getNextRank() != null ?
                        progression.getSeasonalRank().getNextRank().name() : null)
                .seasonalRankProgress(progression.getSeasonalRank()
                        .getProgressPercentage(progression.getSeasonalXp()))
                .lifetimeRank(progression.getLifetimeRank().name())
                .lifetimeTier(progression.getLifetimeTier())
                .lifetimeXp(progression.getLifetimeXp())
                .lifetimeRankIcon(progression.getLifetimeRank().getIcon())
                .lifetimePosition(lifetimePosition)
                .xpToNextLifetimeRank(progression.getXpToNextLifetimeRank())
                .nextLifetimeRank(progression.getLifetimeRank().getNextRank() != null ?
                        progression.getLifetimeRank().getNextRank().name() : null)
                .lifetimeRankProgress(progression.getLifetimeRank()
                        .getProgressPercentage(progression.getLifetimeXp()))
                .build();
    }
}