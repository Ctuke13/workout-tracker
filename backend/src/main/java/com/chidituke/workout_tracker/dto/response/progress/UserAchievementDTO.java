package com.chidituke.workout_tracker.dto.response.progress;

import com.chidituke.workout_tracker.model.progress.UserAchievement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for user's unlocked achievements.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAchievementDTO {

    private Long userAchievementId;
    private Long userId;
    private Integer achievementId;

    // Achievement details (embedded)
    private String achievementKey;
    private String name;
    private String description;
    private String category;
    private String rarity;
    private Integer bonusXp;
    private String icon;

    // Unlock info
    private LocalDateTime unlockedAt;
    private Integer bonusXpAwarded;
    private Integer progressValueAtUnlock;

    // Display helpers
    private String unlockMessage;
    private Boolean isNewlyUnlocked;

    /**
     * Convert entity to DTO
     */
    public static UserAchievementDTO fromEntity(UserAchievement userAchievement) {
        if (userAchievement == null) return null;

        var achievement = userAchievement.getAchievement();

        return UserAchievementDTO.builder()
                .userAchievementId(userAchievement.getUserAchievementId())
                .userId(userAchievement.getUserId())
                .achievementId(userAchievement.getAchievementId())
                .achievementKey(achievement != null ? achievement.getAchievementKey() : null)
                .name(achievement != null ? achievement.getName() : null)
                .description(achievement != null ? achievement.getDescription() : null)
                .category(achievement != null ? achievement.getCategory().name() : null)
                .rarity(achievement != null ? achievement.getRarity().name() : null)
                .bonusXp(achievement != null ? achievement.getBonusXp() : null)
                .icon(achievement != null ? achievement.getIcon() : null)
                .unlockedAt(userAchievement.getUnlockedAt())
                .bonusXpAwarded(userAchievement.getBonusXpAwarded())
                .progressValueAtUnlock(userAchievement.getProgressValueAtUnlock())
                .unlockMessage(buildUnlockMessage(userAchievement))
                .isNewlyUnlocked(isRecentlyUnlocked(userAchievement))
                .build();
    }

    /**
     * Build unlock message
     */
    private static String buildUnlockMessage(UserAchievement ua) {
        if (ua.getAchievement() == null) return null;
        return "Unlocked: " + ua.getAchievement().getName() + " (+" +
                ua.getBonusXpAwarded() + " XP)";
    }

    /**
     * Check if recently unlocked (within last hour)
     */
    private static Boolean isRecentlyUnlocked(UserAchievement ua) {
        if (ua.getUnlockedAt() == null) return false;
        return ua.getUnlockedAt().isAfter(LocalDateTime.now().minusHours(1));
    }
}