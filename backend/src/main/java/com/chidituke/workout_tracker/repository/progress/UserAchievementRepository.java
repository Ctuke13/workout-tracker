package com.chidituke.workout_tracker.repository.progress;

import com.chidituke.workout_tracker.model.progress.UserAchievement;
import com.chidituke.workout_tracker.model.progress.enums.AchievementCategory;
import com.chidituke.workout_tracker.model.progress.enums.Rarity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for UserAchievement entity operations.
 */
@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    /**
     * Find all achievements unlocked by a user.
     *
     * @param userId User ID
     * @return List of user achievements
     */
    List<UserAchievement> findByUserIdOrderByUnlockedAtDesc(Long userId);

    /**
     * Check if user has unlocked a specific achievement.
     *
     * @param userId        User ID
     * @param achievementId Achievement ID
     * @return true if unlocked
     */
    boolean existsByUserIdAndAchievementId(Long userId, Integer achievementId);

    /**
     * Find specific user achievement.
     *
     * @param userId        User ID
     * @param achievementId Achievement ID
     * @return Optional containing user achievement if found
     */
    Optional<UserAchievement> findByUserIdAndAchievementId(Long userId, Integer achievementId);

    /**
     * Count total achievements unlocked by user.
     *
     * @param userId User ID
     * @return Count of unlocked achievements
     */
    Long countByUserId(Long userId);

    /**
     * Find user's achievements by category.
     *
     * @param userId   User ID
     * @param category Achievement category
     * @return List of user achievements in that category
     */
    @Query("SELECT ua FROM UserAchievement ua " +
            "JOIN ua.achievement a " +
            "WHERE ua.userId = :userId AND a.category = :category " +
            "ORDER BY ua.unlockedAt DESC")
    List<UserAchievement> findByUserIdAndCategory(
            @Param("userId") Long userId,
            @Param("category") AchievementCategory category
    );

    /**
     * Find user's achievements by rarity.
     *
     * @param userId User ID
     * @param rarity Rarity level
     * @return List of user achievements with that rarity
     */
    @Query("SELECT ua FROM UserAchievement ua " +
            "JOIN ua.achievement a " +
            "WHERE ua.userId = :userId AND a.rarity = :rarity " +
            "ORDER BY ua.unlockedAt DESC")
    List<UserAchievement> findByUserIdAndRarity(
            @Param("userId") Long userId,
            @Param("rarity") Rarity rarity
    );

    /**
     * Find recently unlocked achievements for user.
     *
     * @param userId User ID
     * @param since  DateTime to check from
     * @return List of recently unlocked achievements
     */
    @Query("SELECT ua FROM UserAchievement ua " +
            "WHERE ua.userId = :userId AND ua.unlockedAt >= :since " +
            "ORDER BY ua.unlockedAt DESC")
    List<UserAchievement> findRecentlyUnlocked(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since
    );

    /**
     * Calculate total bonus XP earned from achievements.
     *
     * @param userId User ID
     * @return Total bonus XP
     */
    @Query("SELECT SUM(ua.bonusXpAwarded) FROM UserAchievement ua WHERE ua.userId = :userId")
    Long getTotalBonusXpEarned(@Param("userId") Long userId);

    /**
     * Count achievements unlocked by category.
     *
     * @param userId   User ID
     * @param category Achievement category
     * @return Count of unlocked achievements in category
     */
    @Query("SELECT COUNT(ua) FROM UserAchievement ua " +
            "JOIN ua.achievement a " +
            "WHERE ua.userId = :userId AND a.category = :category")
    Long countByUserIdAndCategory(
            @Param("userId") Long userId,
            @Param("category") AchievementCategory category
    );

    /**
     * Get achievement unlock rate (percentage of all achievements unlocked).
     *
     * @param userId            User ID
     * @param totalAchievements Total number of achievements in system
     * @return Percentage unlocked (0-100)
     */
    @Query("SELECT (COUNT(ua) * 100.0 / :totalAchievements) FROM UserAchievement ua " +
            "WHERE ua.userId = :userId")
    Double getUnlockPercentage(
            @Param("userId") Long userId,
            @Param("totalAchievements") Long totalAchievements
    );
}