package com.chidituke.workout_tracker.model.progress;

import com.chidituke.workout_tracker.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity tracking which achievements each user has unlocked.
 * <p>
 * Data Flow:
 * 1. User completes workout
 * 2. Stats updated in UserProgression
 * 3. AchievementService checks all achievements
 * 4. If criteria met and not already unlocked, create UserAchievement
 * 5. Award bonus XP to user
 * <p>
 * Database Table: user_achievements
 */
@Entity
@Table(name = "user_achievements",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "achievement_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_achievement_id")
    private Long userAchievementId;

    // ========== RELATIONSHIPS ==========

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "achievement_id", nullable = false)
    private Integer achievementId;

    @ManyToOne(fetch = FetchType.EAGER) // EAGER because we often need achievement details
    @JoinColumn(name = "achievement_id", insertable = false, updatable = false)
    private Achievement achievement;

    // ========== UNLOCK INFO ==========

    /**
     * When the achievement was unlocked
     */
    @Column(name = "unlocked_at", nullable = false)
    private LocalDateTime unlockedAt;

    /**
     * How much bonus XP was awarded for this achievement
     */
    @Column(name = "bonus_xp_awarded", nullable = false)
    private Integer bonusXpAwarded;

    /**
     * User's stat value when achievement was unlocked
     * (e.g., if achievement was for 100 workouts, this would be 100)
     */
    @Column(name = "progress_value_at_unlock")
    private Integer progressValueAtUnlock;

    @PrePersist
    protected void onCreate() {
        if (unlockedAt == null) {
            unlockedAt = LocalDateTime.now();
        }
    }

    /**
     * Create a new user achievement unlock.
     *
     * @param userId        User ID
     * @param achievement   Achievement that was unlocked
     * @param progressValue User's stat value at unlock
     * @return New UserAchievement instance
     */
    public static UserAchievement createUnlock(Long userId, Achievement achievement, Integer progressValue) {
        UserAchievement ua = new UserAchievement();
        ua.setUserId(userId);
        ua.setAchievementId(achievement.getAchievementId());
        ua.setAchievement(achievement);
        ua.setBonusXpAwarded(achievement.getBonusXp());
        ua.setProgressValueAtUnlock(progressValue);
        ua.setUnlockedAt(LocalDateTime.now());
        return ua;
    }
}