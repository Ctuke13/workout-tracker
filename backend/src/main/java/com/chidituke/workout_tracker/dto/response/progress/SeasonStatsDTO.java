package com.chidituke.workout_tracker.dto.response.progress;

import com.chidituke.workout_tracker.service.progress.LeaderboardService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Response DTO for season statistics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonStatsDTO {

    private Integer seasonId;
    private Long totalParticipants;
    private Double averageXp;
    private List<SeasonHistoryDTO> topPerformers;

    // Calculated stats
    private String participationLevel;
    private String competitionLevel;

    /**
     * Convert from service stats
     */
    public static SeasonStatsDTO fromStats(LeaderboardService.SeasonStats stats) {
        if (stats == null) return null;

        List<SeasonHistoryDTO> topPerformers = stats.topPerformers().stream()
                .map(SeasonHistoryDTO::fromEntity)
                .collect(Collectors.toList());

        String participation = getParticipationLevel(stats.totalParticipants());
        String competition = getCompetitionLevel(stats.averageXp());

        return SeasonStatsDTO.builder()
                .seasonId(stats.seasonId())
                .totalParticipants(stats.totalParticipants())
                .averageXp(Math.round(stats.averageXp() * 100.0) / 100.0)
                .topPerformers(topPerformers)
                .participationLevel(participation)
                .competitionLevel(competition)
                .build();
    }

    /**
     * Get participation level description
     */
    private static String getParticipationLevel(Long participants) {
        if (participants >= 1000) return "Very High";
        if (participants >= 500) return "High";
        if (participants >= 100) return "Moderate";
        if (participants >= 50) return "Low";
        return "Very Low";
    }

    /**
     * Get competition level based on average XP
     */
    private static String getCompetitionLevel(Double avgXp) {
        if (avgXp >= 1000) return "Intense";
        if (avgXp >= 500) return "Competitive";
        if (avgXp >= 200) return "Moderate";
        if (avgXp >= 50) return "Casual";
        return "Relaxed";
    }
}