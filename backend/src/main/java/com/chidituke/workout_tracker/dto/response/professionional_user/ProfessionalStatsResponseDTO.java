package com.chidituke.workout_tracker.dto.response.professionional_user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfessionalStatsResponseDTO {

    // Profile metrics
    private Integer profileViews;
    private Integer profileViewsThisMonth;
    private Double profileCompletionPercentage;

    // Client metrics
    private Integer totalClients;
    private Integer activeClients;
    private Integer newClientsThisMonth;
    private Integer clientRetentionRate;

    // Rating metrics
    private Double averageRating;
    private Integer totalReviews;
    private Integer fiveStarReviews;
    private Integer reviewsThisMonth;

    // Activity metrics
    private Integer sessionsCompleted;
    private Integer sessionsThisMonth;
    private Integer totalHoursLogged;
    private LocalDateTime lastActiveDate;

    // Financial metrics (if applicable)
    private Double totalEarnings;
    private Double earningsThisMonth;
    private Double averageSessionRate;

    // Growth metrics
    private Double profileViewGrowth; // Percentage change from last month
    private Double clientGrowth;
    private Double ratingGrowth;

    // Rankings
    private Integer localRanking; // Rank in their zipcode area
    private Integer categoryRanking; // Rank in their service type

    // Helper methods for display
    public String getProfileCompletionDisplay() {
        if (profileCompletionPercentage == null) {
            return "0% complete";
        }
        return String.format("%.1f%% complete", profileCompletionPercentage);
    }

    public String getRatingDisplay() {
        if (averageRating == null || totalReviews == null || totalReviews == 0) {
            return "No ratings yet";
        }
        return String.format("%.1f ⭐ (%d reviews)", averageRating, totalReviews);
    }

    public String getClientGrowthDisplay() {
        if (clientGrowth == null) {
            return "No data";
        }
        String sign = clientGrowth >= 0 ? "+" : "";
        return String.format("%s%.1f%%", sign, clientGrowth);
    }

    public String getProfileViewGrowthDisplay() {
        if (profileViewGrowth == null) {
            return "No data";
        }
        String sign = profileViewGrowth >= 0 ? "+" : "";
        return String.format("%s%.1f%%", sign, profileViewGrowth);
    }

    public String getTotalEarningsDisplay() {
        if (totalEarnings == null) {
            return "No earnings data";
        }
        return String.format("$%.2f", totalEarnings);
    }

    public String getEarningsThisMonthDisplay() {
        if (earningsThisMonth == null) {
            return "$0.00";
        }
        return String.format("$%.2f", earningsThisMonth);
    }

    public String getLocalRankingDisplay() {
        if (localRanking == null) {
            return "Not ranked";
        }
        return "#" + localRanking + " in local area";
    }

    public String getCategoryRankingDisplay() {
        if (categoryRanking == null) {
            return "Not ranked";
        }
        return "#" + categoryRanking + " in category";
    }

    public boolean hasPositiveGrowth() {
        return (clientGrowth != null && clientGrowth > 0) ||
                (profileViewGrowth != null && profileViewGrowth > 0);
    }

    public String getSessionsCompletedDisplay() {
        if (sessionsCompleted == null) {
            return "0 sessions";
        }
        return sessionsCompleted + (sessionsCompleted == 1 ? " session" : " sessions") + " completed";
    }

    public String getRetentionRateDisplay() {
        if (clientRetentionRate == null) {
            return "No data";
        }
        return clientRetentionRate + "% retention rate";
    }
}