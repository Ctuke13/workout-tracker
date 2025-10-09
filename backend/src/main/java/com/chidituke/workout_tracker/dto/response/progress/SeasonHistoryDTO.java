package com.chidituke.workout_tracker.dto.response.progress;

import com.chidituke.workout_tracker.model.progress.SeasonHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for season history (archived season performance).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonHistoryDTO {

    // User info
    private Long userId;
    private String username;

    // Season info
    private Integer seasonId;
    private String seasonName;
    private String seasonType;

    // Final stats
    private Integer finalSeasonalXp;
    private String finalSeasonalRank;
    private Integer finalSeasonalTier;
    private String rankIcon;
    private BigDecimal finalPercentile;

    // Season achievements
    private Integer totalWorkoutsThisSeason;
    private Integer highestStreakThisSeason;
    private Integer perfectWeeksThisSeason;

    private LocalDateTime completedAt;

    // Display helpers
    private String performanceSummary;
    private String rankDisplayText;

    /**
     * Convert entity to DTO
     */
    public static SeasonHistoryDTO fromEntity(SeasonHistory history) {
        if (history == null) return null;

        String rankDisplay = history.getFinalSeasonalRank().name() + " " +
                getRomanNumeral(history.getFinalSeasonalTier());

        return SeasonHistoryDTO.builder()
                .userId(history.getUserId())
                .username(history.getUser() != null ? history.getUser().getUsername() : null)
                .seasonId(history.getSeasonId())
                .seasonName(history.getSeason() != null ? history.getSeason().getSeasonName() : null)
                .seasonType(history.getSeason() != null ? history.getSeason().getSeasonType().name() : null)
                .finalSeasonalXp(history.getFinalSeasonalXp())
                .finalSeasonalRank(history.getFinalSeasonalRank().name())
                .finalSeasonalTier(history.getFinalSeasonalTier())
                .rankIcon(history.getFinalSeasonalRank().getIcon())
                .finalPercentile(history.getFinalPercentile())
                .totalWorkoutsThisSeason(history.getTotalWorkoutsThisSeason())
                .highestStreakThisSeason(history.getHighestStreakThisSeason())
                .perfectWeeksThisSeason(history.getPerfectWeeksThisSeason())
                .completedAt(history.getCompletedAt())
                .performanceSummary(buildPerformanceSummary(history))
                .rankDisplayText(rankDisplay)
                .build();
    }

    /**
     * Build performance summary text
     */
    private static String buildPerformanceSummary(SeasonHistory history) {
        return String.format("Completed %d workouts with a %d-day best streak",
                history.getTotalWorkoutsThisSeason(),
                history.getHighestStreakThisSeason());
    }

    /**
     * Convert tier number to roman numeral
     */
    private static String getRomanNumeral(Integer tier) {
        return switch (tier) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> "";
        };
    }
}