package com.chidituke.workout_tracker.dto.response.progress;

import com.chidituke.workout_tracker.model.progress.Season;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for Season information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonDTO {

    private Integer seasonId;
    private String seasonName;
    private String seasonType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private LocalDateTime createdAt;

    // Calculated fields
    private Long daysRemaining;
    private Integer daysInSeason;
    private Double progressPercentage;

    /**
     * Convert entity to DTO
     */
    public static SeasonDTO fromEntity(Season season) {
        if (season == null) return null;

        LocalDate today = LocalDate.now();
        long daysRemaining = today.until(season.getEndDate()).getDays();
        long totalDays = season.getStartDate().until(season.getEndDate()).getDays();
        double progress = totalDays > 0 ?
                ((double) (totalDays - daysRemaining) / totalDays) * 100.0 : 0.0;

        return SeasonDTO.builder()
                .seasonId(season.getSeasonId())
                .seasonName(season.getSeasonName())
                .seasonType(season.getSeasonType().name())
                .startDate(season.getStartDate())
                .endDate(season.getEndDate())
                .isActive(season.getIsActive())
                .createdAt(season.getCreatedAt())
                .daysRemaining(Math.max(0, daysRemaining))
                .daysInSeason((int) totalDays)
                .progressPercentage(Math.round(progress * 100.0) / 100.0)
                .build();
    }
}