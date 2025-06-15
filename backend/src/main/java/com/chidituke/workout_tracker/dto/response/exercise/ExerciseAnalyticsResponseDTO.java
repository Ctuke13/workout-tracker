package com.chidituke.workout_tracker.dto.response.exercise;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExerciseAnalyticsResponseDTO {
    private Long exerciseId;
    private String exerciseName;
    private Integer totalUsage;
    private Double averageRating;
    private Integer totalRatings;
    private Integer popularityRank;
    private Double usageGrowthRate;
    private Boolean isFromVerifiedSource;
    private String qualityScore; // "Excellent", "Good", "Fair", "Poor", "Unrated"
    private String popularityLevel; // "Very Popular", "Popular", "Moderate", "Low", "New"

    // Additional analytics metrics
    private Integer usageThisWeek;
    private Integer usageThisMonth;
    private Double ratingTrend; // Positive/negative rating trend
    private String targetAudience; // "Beginner", "Intermediate", "Advanced", "All Levels"
    private Boolean isRising; // Trending upward in usage

    public static ExerciseAnalyticsResponseDTO fromServiceAnalytics(
            com.chidituke.workout_tracker.service.ExerciseService.ExerciseAnalytics analytics) {
        return ExerciseAnalyticsResponseDTO.builder()
                .exerciseId(analytics.getExerciseId())
                .exerciseName(analytics.getExerciseName())
                .totalUsage(analytics.getTotalUsage())
                .averageRating(analytics.getAverageRating())
                .totalRatings(analytics.getTotalRatings())
                .popularityRank(analytics.getPopularityRank())
                .usageGrowthRate(analytics.getUsageGrowthRate())
                .isFromVerifiedSource(analytics.getIsFromVerifiedSource())
                .qualityScore(calculateQualityScore(analytics.getAverageRating(), analytics.getTotalRatings()))
                .popularityLevel(calculatePopularityLevel(analytics.getTotalUsage()))
                .isRising(analytics.getUsageGrowthRate() != null && analytics.getUsageGrowthRate() > 0.1)
                .build();
    }

    private static String calculateQualityScore(Double averageRating, Integer totalRatings) {
        if (averageRating == null || totalRatings == null || totalRatings < 5) {
            return "Unrated";
        }

        if (averageRating >= 4.5) return "Excellent";
        if (averageRating >= 4.0) return "Good";
        if (averageRating >= 3.0) return "Fair";
        return "Poor";
    }

    private static String calculatePopularityLevel(Integer usageCount) {
        if (usageCount == null) return "New";

        if (usageCount >= 1000) return "Very Popular";
        if (usageCount >= 500) return "Popular";
        if (usageCount >= 100) return "Moderate";
        return "Low";
    }
}