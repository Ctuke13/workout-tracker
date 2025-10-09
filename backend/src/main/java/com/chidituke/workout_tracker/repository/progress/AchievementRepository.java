package com.chidituke.workout_tracker.repository.progress;

import com.chidituke.workout_tracker.model.progress.Achievement;
import com.chidituke.workout_tracker.model.progress.enums.AchievementCategory;
import com.chidituke.workout_tracker.model.progress.enums.Rarity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Achievement entity operations.
 */
@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Integer> {

    /**
     * Find achievement by its unique key.
     *
     * @param achievementKey The achievement key
     * @return Optional containing achievement if found
     */
    Optional<Achievement> findByAchievementKey(String achievementKey);

    /**
     * Find all achievements in a category.
     *
     * @param category Achievement category
     * @return List of achievements
     */
    List<Achievement> findByCategoryOrderByDisplayOrderAsc(AchievementCategory category);

    /**
     * Find all achievements by rarity.
     *
     * @param rarity Rarity level
     * @return List of achievements
     */
    List<Achievement> findByRarityOrderByDisplayOrderAsc(Rarity rarity);

    /**
     * Find all visible (non-hidden) achievements.
     *
     * @return List of visible achievements
     */
    List<Achievement> findByIsHiddenFalseOrderByDisplayOrderAsc();

    /**
     * Find all hidden achievements.
     *
     * @return List of hidden achievements
     */
    List<Achievement> findByIsHiddenTrueOrderByDisplayOrderAsc();

    /**
     * Get all achievements ordered by display order.
     *
     * @return List of all achievements
     */
    List<Achievement> findAllByOrderByDisplayOrderAsc();

    /**
     * Find achievements by criteria field (for checking specific stat).
     *
     * @param criteriaField The field name to check
     * @return List of achievements checking that field
     */
    List<Achievement> findByCriteriaField(String criteriaField);

    /**
     * Count achievements by category.
     *
     * @param category Achievement category
     * @return Count of achievements
     */
    Long countByCategory(AchievementCategory category);

    /**
     * Count achievements by rarity.
     *
     * @param rarity Rarity level
     * @return Count of achievements
     */
    Long countByRarity(Rarity rarity);
}