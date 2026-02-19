package com.chidituke.workout_tracker.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDataExportResponse {

    // User Info
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String nickname;
    private String petName;

    // Account Info
    private LocalDateTime createdAt;
    private String subscriptionTier;

    // Workout Stats
    private Integer totalWorkouts;
    private Integer currentStreak;
    private Integer longestStreak;

    // Note: In a full implementation, this would include:
    // - List of all workouts
    // - Achievements
    // - Pet stats
    // - Goals
    // - Preferences
    // - etc.

    private String exportedAt;
    private String dataFormat;

    public UserDataExportResponse(Long userId, String username, String email,
                                  String firstName, String lastName, String nickname,
                                  String petName, LocalDateTime createdAt,
                                  Integer totalWorkouts, Integer currentStreak,
                                  Integer longestStreak, String subscriptionTier) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.petName = petName;
        this.createdAt = createdAt;
        this.totalWorkouts = totalWorkouts;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.subscriptionTier = subscriptionTier;
        this.exportedAt = LocalDateTime.now().toString();
        this.dataFormat = "JSON";
    }
}