package com.chidituke.workout_tracker.service.progress;

import com.chidituke.workout_tracker.model.progress.Achievement;
import com.chidituke.workout_tracker.model.progress.UserAchievement;
import com.chidituke.workout_tracker.model.progress.UserProgression;
import com.chidituke.workout_tracker.model.progress.enums.AchievementCategory;
import com.chidituke.workout_tracker.model.progress.enums.Rarity;
import com.chidituke.workout_tracker.repository.progress.AchievementRepository;
import com.chidituke.workout_tracker.repository.progress.UserAchievementRepository;
import com.chidituke.workout_tracker.repository.progress.UserProgressionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for managing achievements and unlocking them.
 * <p>
 * Data Flow:
 * 1. User completes workout
 * 2. UserProgressionService updates stats
 * 3. UserProgressionService calls checkAndUnlockAchievements()
 * 4. This service checks all 83 achievements against user stats
 * 5. Newly unlocked achievements are saved
 * 6. Bonus XP is added to user's progression
 * 7. List of newly unlocked achievements returned
 * <p>
 * Achievement Checking Strategy:
 * - Check by criteria field to avoid checking all 83 every time
 * - Only check achievements user hasn't unlocked yet
 * - Award bonus XP immediately upon unlock
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserProgressionRepository userProgressionRepository;
    private final UserProgressionService userProgressionService;

    // ========== ACHIEVEMENT QUERIES ==========

    /**
     * Get all achievements (visible and hidden).
     *
     * @return List of all achievements
     */
    @Transactional(readOnly = true)
    public List<Achievement> getAllAchievements() {
        return achievementRepository.findAllByOrderByDisplayOrderAsc();
    }

    /**
     * Get only visible (non-hidden) achievements.
     *
     * @return List of visible achievements
     */
    @Transactional(readOnly = true)
    public List<Achievement> getVisibleAchievements() {
        return achievementRepository.findByIsHiddenFalseOrderByDisplayOrderAsc();
    }

    /**
     * Get achievements by category.
     *
     * @param category Achievement category
     * @return List of achievements in category
     */
    @Transactional(readOnly = true)
    public List<Achievement> getAchievementsByCategory(AchievementCategory category) {
        return achievementRepository.findByCategoryOrderByDisplayOrderAsc(category);
    }

    /**
     * Get achievements by rarity.
     *
     * @param rarity Rarity level
     * @return List of achievements with that rarity
     */
    @Transactional(readOnly = true)
    public List<Achievement> getAchievementsByRarity(Rarity rarity) {
        return achievementRepository.findByRarityOrderByDisplayOrderAsc(rarity);
    }

    /**
     * Get achievement by ID.
     *
     * @param achievementId Achievement ID
     * @return Optional containing achievement if found
     */
    @Transactional(readOnly = true)
    public Optional<Achievement> getAchievementById(Integer achievementId) {
        return achievementRepository.findById(achievementId);
    }

    /**
     * Get achievement by key.
     *
     * @param achievementKey Achievement key
     * @return Optional containing achievement if found
     */
    @Transactional(readOnly = true)
    public Optional<Achievement> getAchievementByKey(String achievementKey) {
        return achievementRepository.findByAchievementKey(achievementKey);
    }

    // ========== USER ACHIEVEMENT QUERIES ==========

    /**
     * Get all achievements unlocked by a user.
     *
     * @param userId User ID
     * @return List of unlocked achievements
     */
    @Transactional(readOnly = true)
    public List<UserAchievement> getUserAchievements(Long userId) {
        return userAchievementRepository.findByUserIdOrderByUnlockedAtDesc(userId);
    }

    /**
     * Get user's achievements by category.
     *
     * @param userId   User ID
     * @param category Achievement category
     * @return List of user achievements in that category
     */
    @Transactional(readOnly = true)
    public List<UserAchievement> getUserAchievementsByCategory(Long userId, AchievementCategory category) {
        return userAchievementRepository.findByUserIdAndCategory(userId, category);
    }

    /**
     * Get user's achievements by rarity.
     *
     * @param userId User ID
     * @param rarity Rarity level
     * @return List of user achievements with that rarity
     */
    @Transactional(readOnly = true)
    public List<UserAchievement> getUserAchievementsByRarity(Long userId, Rarity rarity) {
        return userAchievementRepository.findByUserIdAndRarity(userId, rarity);
    }

    /**
     * Check if user has unlocked a specific achievement.
     *
     * @param userId        User ID
     * @param achievementId Achievement ID
     * @return true if unlocked
     */
    @Transactional(readOnly = true)
    public boolean hasUnlockedAchievement(Long userId, Integer achievementId) {
        return userAchievementRepository.existsByUserIdAndAchievementId(userId, achievementId);
    }

    /**
     * Get recently unlocked achievements (last 24 hours).
     *
     * @param userId User ID
     * @return List of recently unlocked achievements
     */
    @Transactional(readOnly = true)
    public List<UserAchievement> getRecentlyUnlockedAchievements(Long userId) {
        LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
        return userAchievementRepository.findRecentlyUnlocked(userId, yesterday);
    }

    /**
     * Get achievement statistics for user.
     *
     * @param userId User ID
     * @return Achievement stats (total unlocked, by category, etc.)
     */
    @Transactional(readOnly = true)
    public AchievementStats getAchievementStats(Long userId) {
        Long totalUnlocked = userAchievementRepository.countByUserId(userId);
        Long totalAchievements = achievementRepository.count();
        Long totalBonusXp = userAchievementRepository.getTotalBonusXpEarned(userId);
        Double unlockPercentage = userAchievementRepository.getUnlockPercentage(userId, totalAchievements);

        return new AchievementStats(
                totalUnlocked,
                totalAchievements,
                totalBonusXp != null ? totalBonusXp : 0L,
                unlockPercentage != null ? unlockPercentage : 0.0
        );
    }

    // ========== ACHIEVEMENT CHECKING & UNLOCKING ==========

    /**
     * Check all achievements and unlock any newly met criteria.
     * This is the MAIN entry point called after workout completion.
     * <p>
     * Data Flow:
     * 1. Get user's progression stats
     * 2. Get all achievements user hasn't unlocked
     * 3. Check each achievement's criteria
     * 4. Unlock achievements with met criteria
     * 5. Award bonus XP for each unlock
     * 6. Return list of newly unlocked achievements
     *
     * @param userId User ID
     * @return List of newly unlocked achievements
     */
    @Transactional
    public List<UserAchievement> checkAndUnlockAchievements(Long userId) {
        // Get user's progression stats
        UserProgression progression = userProgressionService.getUserProgression(userId)
                .orElseThrow(() -> new IllegalStateException("User progression not found for user: " + userId));

        // Get all achievements
        List<Achievement> allAchievements = achievementRepository.findAll();

        // Filter out already unlocked achievements
        List<Achievement> notUnlockedYet = allAchievements.stream()
                .filter(achievement -> !hasUnlockedAchievement(userId, achievement.getAchievementId()))
                .toList();

        // Check each achievement and collect newly unlocked ones
        List<UserAchievement> newlyUnlocked = new ArrayList<>();

        for (Achievement achievement : notUnlockedYet) {
            if (checkAchievementCriteria(achievement, progression)) {
                UserAchievement unlocked = unlockAchievement(userId, achievement, progression);
                newlyUnlocked.add(unlocked);

                log.info("Achievement unlocked for user {}: {} ({})",
                        userId, achievement.getName(), achievement.getRarity());
            }
        }

        // Award bonus XP for all newly unlocked achievements
        if (!newlyUnlocked.isEmpty()) {
            int totalBonusXp = newlyUnlocked.stream()
                    .mapToInt(UserAchievement::getBonusXpAwarded)
                    .sum();

            progression.addXp(totalBonusXp);
            userProgressionRepository.save(progression);

            log.info("Awarded {} bonus XP to user {} for {} achievements",
                    totalBonusXp, userId, newlyUnlocked.size());
        }

        return newlyUnlocked;
    }

    /**
     * Check if a specific achievement's criteria is met.
     *
     * @param achievement Achievement to check
     * @param progression User's progression stats
     * @return true if criteria met
     */
    private boolean checkAchievementCriteria(Achievement achievement, UserProgression progression) {
        String criteriaField = achievement.getCriteriaField();

        if (criteriaField == null) {
            return false;
        }

        // Get the stat value from user progression
        Number statValue = getStatValue(progression, criteriaField);

        if (statValue == null) {
            return false;
        }

        // Use Achievement's built-in criteria checking
        return achievement.isCriteriaMet(statValue);
    }

    /**
     * Extract stat value from UserProgression based on field name.
     * Maps criteria_field from database to UserProgression properties.
     *
     * @param progression   User progression
     * @param criteriaField Field name to extract
     * @return Stat value, or null if field not found
     */
    private Number getStatValue(UserProgression progression, String criteriaField) {
        return switch (criteriaField) {
            // Workout Milestones
            case "total_workouts_completed" -> progression.getTotalWorkoutsCompleted();

            // Streak Achievements
            case "current_streak_days" -> progression.getCurrentStreakDays();
            case "longest_streak_days" -> progression.getLongestStreakDays();

            // Strength Volume
            case "total_volume_lifted" -> progression.getTotalVolumLifted();

            // Time-Based
            case "total_workout_minutes" -> progression.getTotalWorkoutMinutes();

            // Cardio Distance
            case "total_distance_km" -> progression.getTotalDistanceKm();

            // Isometric Endurance
            case "total_hold_seconds" -> progression.getTotalHoldSeconds();

            // Workout Diversity
            case "unique_exercises_tried" -> progression.getUniqueExercisesTried();
            case "cardio_workouts_completed" -> progression.getCardioWorkoutsCompleted();
            case "strength_workouts_completed" -> progression.getStrengthWorkoutsCompleted();
            case "isometric_workouts_completed" -> progression.getIsometricWorkoutsCompleted();

            // Weekly Challenges
            case "weekly_workout_count" -> progression.getWeeklyWorkoutCount();

            // Special Hidden
            case "first_of_month_count" -> progression.getFirstOfMonthCount();
            case "weekend_workout_count" -> progression.getWeekendWorkoutCount();

            // Sets
            case "total_sets_completed" -> progression.getTotalSetsCompleted();

            default -> {
                log.warn("Unknown criteria field: {}", criteriaField);
                yield null;
            }
        };
    }

    /**
     * Unlock an achievement for a user.
     *
     * @param userId      User ID
     * @param achievement Achievement to unlock
     * @param progression User progression (for progress value)
     * @return Newly created UserAchievement
     */
    private UserAchievement unlockAchievement(Long userId, Achievement achievement, UserProgression progression) {
        // Get the progress value at unlock
        Number statValue = getStatValue(progression, achievement.getCriteriaField());
        Integer progressValue = statValue != null ? statValue.intValue() : null;

        // Create unlock record
        UserAchievement userAchievement = UserAchievement.createUnlock(userId, achievement, progressValue);

        // Save to database
        return userAchievementRepository.save(userAchievement);
    }

    /**
     * Manually unlock an achievement for a user (admin function).
     * Use for special achievements or testing.
     *
     * @param userId        User ID
     * @param achievementId Achievement ID
     * @param awardBonusXp  Whether to award bonus XP
     * @return Unlocked achievement
     */
    @Transactional
    public UserAchievement manuallyUnlockAchievement(Long userId, Integer achievementId, boolean awardBonusXp) {
        // Check if already unlocked
        if (hasUnlockedAchievement(userId, achievementId)) {
            throw new IllegalStateException("User already has this achievement");
        }

        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new IllegalArgumentException("Achievement not found: " + achievementId));

        UserProgression progression = userProgressionService.getUserProgression(userId)
                .orElseThrow(() -> new IllegalStateException("User progression not found"));

        // Unlock achievement
        UserAchievement unlocked = unlockAchievement(userId, achievement, progression);

        // Award bonus XP if requested
        if (awardBonusXp) {
            progression.addXp(achievement.getBonusXp());
            userProgressionRepository.save(progression);
            log.info("Manually unlocked achievement {} for user {} with {} bonus XP",
                    achievement.getName(), userId, achievement.getBonusXp());
        }

        return unlocked;
    }

    // ========== ACHIEVEMENT PROGRESS TRACKING ==========

    /**
     * Get user's progress toward a specific achievement.
     * Returns percentage completion and current/target values.
     *
     * @param userId        User ID
     * @param achievementId Achievement ID
     * @return Achievement progress information
     */
    @Transactional(readOnly = true)
    public AchievementProgress getAchievementProgress(Long userId, Integer achievementId) {
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new IllegalArgumentException("Achievement not found: " + achievementId));

        // Check if already unlocked
        if (hasUnlockedAchievement(userId, achievementId)) {
            return new AchievementProgress(achievement, true, 100.0,
                    achievement.getCriteriaValue(), achievement.getCriteriaValue());
        }

        // Get user's current progress
        UserProgression progression = userProgressionService.getUserProgression(userId)
                .orElse(null);

        if (progression == null) {
            return new AchievementProgress(achievement, false, 0.0, 0, achievement.getCriteriaValue());
        }

        Number currentValue = getStatValue(progression, achievement.getCriteriaField());
        int current = currentValue != null ? currentValue.intValue() : 0;
        int target = achievement.getCriteriaValue();

        double percentage = Math.min(100.0, (current / (double) target) * 100.0);

        return new AchievementProgress(achievement, false, percentage, current, target);
    }

    /**
     * Get progress for all achievements in a category.
     *
     * @param userId   User ID
     * @param category Achievement category
     * @return List of achievement progress
     */
    @Transactional(readOnly = true)
    public List<AchievementProgress> getCategoryProgress(Long userId, AchievementCategory category) {
        List<Achievement> achievements = achievementRepository.findByCategoryOrderByDisplayOrderAsc(category);

        return achievements.stream()
                .map(achievement -> getAchievementProgress(userId, achievement.getAchievementId()))
                .toList();
    }

    // ========== HELPER CLASSES ==========

    /**
     * DTO for achievement statistics.
     */
    public record AchievementStats(
            Long totalUnlocked,
            Long totalAchievements,
            Long totalBonusXpEarned,
            Double unlockPercentage
    ) {
    }

    /**
     * DTO for achievement progress tracking.
     */
    public record AchievementProgress(
            Achievement achievement,
            boolean isUnlocked,
            double progressPercentage,
            int currentValue,
            int targetValue
    ) {
    }

}