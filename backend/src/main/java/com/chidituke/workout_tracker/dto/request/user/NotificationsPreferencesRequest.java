package com.chidituke.workout_tracker.dto.request.user;

import lombok.Data;

/**
 * Request body for PATCH /api/users/notification-preferences
 * All fields are optional — only non-null values are applied.
 */
@Data
public class NotificationsPreferencesRequest {
    private Boolean notifPetHealth;
    private Boolean notifStreakReminders;
    private Boolean notifAchievements;
    private Boolean notifRankSeason;
    private Boolean notifWeeklySummary;
    private Boolean notifSocialLeaderboard;
    private Boolean notifReengagement;
}
