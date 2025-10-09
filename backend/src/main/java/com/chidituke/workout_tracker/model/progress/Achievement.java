package com.chidituke.workout_tracker.model.progress;

import com.chidituke.workout_tracker.model.progress.enums.AchievementCategory;
import com.chidituke.workout_tracker.model.progress.enums.Rarity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing an achievement definition.
 * <p>
 * Data Flow:
 * 1. 83 achievements pre-loaded in database via V016 migration
 * 2. AchievementService checks criteria against user stats
 * 3. If criteria met, UserAchievement record created
 * 4. Bonus XP awarded to user
 * <p>
 * Database Table: achievements
 * Related Table: user_achievements (tracks which users unlocked which achievements)
 */
@Entity
@Table(name = "achievements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "achievement_id")
    private Integer achievementId;

    /**
     * Unique identifier for achievement (e.g., "FIRST_WORKOUT", "CENTURY_CLUB")
     */
    @Column(name = "achievement_key", nullable = false, unique = true, length = 50)
    private String achievementKey;

    /**
     * Display name (e.g., "First Workout", "Century Club")
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Detailed description of what the achievement is for
     */
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Category this achievement belongs to
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private AchievementCategory category;

    /**
     * Rarity level (determines bonus XP)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "rarity", nullable = false, length = 20)
    private Rarity rarity;

    /**
     * Bonus XP awarded when unlocked (from rarity)
     */
    @Column(name = "bonus_xp", nullable = false)
    private Integer bonusXp;

    /**
     * Icon/emoji for visual display
     */
    @Column(name = "icon", length = 10)
    private String icon;

    /**
     * Whether this achievement is hidden until unlocked
     */
    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false;

    /**
     * Display order for sorting (lower = shown first)
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    /**
     * Criteria field name (e.g., "total_workouts_completed", "current_streak_days")
     */
    @Column(name = "criteria_field", length = 50)
    private String criteriaField;

    /**
     * Criteria operator (e.g., ">=", "=", ">")
     */
    @Column(name = "criteria_operator", length = 10)
    private String criteriaOperator;

    /**
     * Criteria value to compare against
     */
    @Column(name = "criteria_value")
    private Integer criteriaValue;

    /**
     * When this achievement was created
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (bonusXp == null && rarity != null) {
            bonusXp = rarity.getBonusXp();
        }
    }

    /**
     * Check if user's stat value meets the achievement criteria.
     *
     * @param userStatValue The user's current stat value
     * @return true if achievement criteria met
     */
    @Transient
    public boolean isCriteriaMet(Number userStatValue) {
        if (criteriaField == null || criteriaOperator == null || criteriaValue == null) {
            return false;
        }

        double userValue = userStatValue.doubleValue();
        double targetValue = criteriaValue.doubleValue();

        return switch (criteriaOperator) {
            case ">=" -> userValue >= targetValue;
            case ">" -> userValue > targetValue;
            case "=" -> userValue == targetValue;
            case "<=" -> userValue <= targetValue;
            case "<" -> userValue < targetValue;
            default -> false;
        };
    }
}