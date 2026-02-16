package com.chidituke.workout_tracker.service.pet;

import com.chidituke.workout_tracker.model.pet.PetStats;
import com.chidituke.workout_tracker.model.pet.enums.EvolutionStage;
import com.chidituke.workout_tracker.repository.pet.PetStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Service for managing pet progression, XP, leveling, and evolution.
 * Handles all XP calculations including workout bonuses and care bonuses.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PetProgressionService {

    private final PetStatsRepository petStatsRepository;

    // ==========================================
    // WORKOUT XP CALCULATION
    // ==========================================

    /**
     * Calculates XP earned from a completed workout
     *
     * @param exerciseCount      Number of exercises completed
     * @param durationMinutes    Workout duration in minutes
     * @param totalSets          Total sets completed across all exercises
     * @param lastWorkoutDate    Date of previous workout (for streak calculation)
     * @param weeklyWorkoutCount Number of workouts completed this week (before this one)
     * @return Total XP earned from this workout
     */
    public int calculateWorkoutXp(
            int exerciseCount,
            int durationMinutes,
            int totalSets,
            LocalDateTime lastWorkoutDate,
            int weeklyWorkoutCount
    ) {
        int totalXp = 0;

        // BASE XP - Always awarded for completing a workout
        int baseXp = 30;
        totalXp += baseXp;
        log.debug("Base XP: {}", baseXp);

        // EXERCISE BONUS - Rewards more exercises
        int exerciseBonus = exerciseCount * 6;
        totalXp += exerciseBonus;
        log.debug("Exercise bonus: {} exercises × 6 = {} XP", exerciseCount, exerciseBonus);

        // DURATION BONUS - Rewards 30+ minute workouts
        int durationBonus = 0;
        if (durationMinutes >= 30) {
            durationBonus = 15;
            totalXp += durationBonus;
            log.debug("Duration bonus: {} minutes (30+) = {} XP", durationMinutes, durationBonus);
        }

        // STREAK BONUS - Rewards daily workout habit
        int streakBonus = 0;
        if (isStreakActive(lastWorkoutDate)) {
            streakBonus = 20;
            totalXp += streakBonus;
            log.debug("Streak bonus: Worked out yesterday = {} XP", streakBonus);
        }

        // VOLUME BONUS - Rewards hard work (10+ sets)
        int volumeBonus = 0;
        if (totalSets >= 10) {
            volumeBonus = 15;
            totalXp += volumeBonus;
            log.debug("Volume bonus: {} sets (10+) = {} XP", totalSets, volumeBonus);
        }

        // WEEKLY CONSISTENCY BONUS - Rewards 3+ workouts per week
        int weeklyBonus = 0;
        if (weeklyWorkoutCount >= 2) { // This is the 3rd workout (count before this one = 2)
            weeklyBonus = 25;
            totalXp += weeklyBonus;
            log.debug("Weekly consistency bonus: {}rd workout this week = {} XP", weeklyWorkoutCount + 1, weeklyBonus);
        }

        log.info("Total workout XP: {} (base:{} + exercises:{} + duration:{} + streak:{} + volume:{} + weekly:{})",
                totalXp, baseXp, exerciseBonus, durationBonus, streakBonus, volumeBonus, weeklyBonus);

        return totalXp;
    }

    /**
     * Checks if streak is active (worked out yesterday)
     *
     * @param lastWorkoutDate Last workout timestamp
     * @return true if worked out within last 24-48 hours
     */
    private boolean isStreakActive(LocalDateTime lastWorkoutDate) {
        if (lastWorkoutDate == null) {
            return false;
        }

        LocalDate lastWorkoutDay = lastWorkoutDate.toLocalDate();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();

        // Streak is active if last workout was yesterday or today
        return lastWorkoutDay.equals(yesterday) || lastWorkoutDay.equals(today);
    }

    // ==========================================
    // XP AWARDING & LEVEL-UP
    // ==========================================

    /**
     * Awards XP to a pet and processes any level-ups
     *
     * @param userId User ID
     * @param amount XP amount to award
     * @param source Source of XP (WORKOUT, FEED, BATHE, MOTIVATE, PERFECT_CARE)
     */
    @Transactional
    public void awardXp(Long userId, int amount, String source) {
        PetStats petStats = petStatsRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Pet not found for user " + userId));

        log.info("Awarding {} XP to pet {} (source: {})", amount, petStats.getPetStatsId(), source);

        // Add XP
        int currentXp = petStats.getXp();
        int newXp = currentXp + amount;
        petStats.setXp(newXp);
        petStats.setLastUpdated(LocalDateTime.now());

        // Save immediately to persist XP
        petStatsRepository.save(petStats);

        // Check for level-up(s)
        while (shouldLevelUp(petStats)) {
            processLevelUp(petStats);
        }

        log.info("Pet {} now has {} XP at level {}", petStats.getPetStatsId(), petStats.getXp(), petStats.getLevel());
    }

    /**
     * Checks if pet has enough XP to level up
     *
     * @param petStats Pet stats entity
     * @return true if XP >= required XP for next level
     */
    private boolean shouldLevelUp(PetStats petStats) {
        int currentXp = petStats.getXp();
        int currentLevel = petStats.getLevel();
        int xpRequired = getXpRequiredForLevel(currentLevel);

        return currentXp >= xpRequired;
    }

    /**
     * Processes a level-up, carries over excess XP, and checks for evolution
     *
     * @param petStats Pet stats entity
     */
    @Transactional
    public void processLevelUp(PetStats petStats) {
        int currentLevel = petStats.getLevel();
        int currentXp = petStats.getXp();
        int xpRequired = getXpRequiredForLevel(currentLevel);

        // Calculate excess XP to carry over
        int excessXp = currentXp - xpRequired;

        // Level up!
        int newLevel = currentLevel + 1;
        petStats.setLevel(newLevel);
        petStats.setXp(excessXp); // Carry over excess XP
        petStats.setLastUpdated(LocalDateTime.now());

        log.info("🎉 LEVEL UP! Pet {} leveled up from {} to {} (excess XP: {})",
                petStats.getPetStatsId(), currentLevel, newLevel, excessXp);

        // Save level-up
        petStatsRepository.save(petStats);

        // Check if evolution is triggered
        checkAndProcessEvolution(petStats);
    }

    /**
     * Gets XP required to level up from current level
     * Uses progressive scaling system
     *
     * @param currentLevel Current level
     * @return XP required for next level
     */
    public int getXpRequiredForLevel(int currentLevel) {
        if (currentLevel <= 5) return 100;      // Levels 1-5
        else if (currentLevel <= 10) return 150; // Levels 6-10
        else if (currentLevel <= 20) return 200; // Levels 11-20
        else if (currentLevel <= 30) return 300; // Levels 21-30
        else if (currentLevel <= 50) return 500; // Levels 31-50
        else if (currentLevel <= 75) return 750; // Levels 51-75
        else return 1000;                        // Levels 76-100+
    }

    // ==========================================
    // EVOLUTION SYSTEM
    // ==========================================

    /**
     * Checks if pet should evolve and processes evolution if needed
     *
     * @param petStats Pet stats entity
     */
    @Transactional
    public void checkAndProcessEvolution(PetStats petStats) {
        EvolutionStage currentStage = petStats.getEvolutionStage();
        int currentLevel = petStats.getLevel();

        // Get the appropriate stage for current level
        EvolutionStage expectedStage = EvolutionStage.getStageForLevel(currentLevel);

        // Check if evolution is needed
        if (currentStage != expectedStage) {
            log.info("🌟 EVOLUTION! Pet {} evolving from {} to {} at level {}",
                    petStats.getPetStatsId(), currentStage, expectedStage, currentLevel);

            petStats.setEvolutionStage(expectedStage);
            petStats.setLastUpdated(LocalDateTime.now());
            petStatsRepository.save(petStats);

            // TODO: Trigger evolution celebration event/animation
            // TODO: Award evolution bonus (crystals, special item, etc.)
        }
    }

    /**
     * Checks if pet can evolve to next stage
     *
     * @param userId User ID
     * @return true if level is high enough for next evolution
     */
    public boolean canEvolve(Long userId) {
        PetStats petStats = petStatsRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Pet not found for user " + userId));

        EvolutionStage currentStage = petStats.getEvolutionStage();
        int currentLevel = petStats.getLevel();

        // Can't evolve if already at max stage
        if (!currentStage.canEvolve()) {
            return false;
        }

        // Check if level is high enough for next stage
        int levelForNextStage = currentStage.getLevelForNextStage();
        return currentLevel >= levelForNextStage;
    }

    // ==========================================
    // CARE BONUSES
    // ==========================================

    /**
     * Awards XP for feeding when pet is hungry (fuel < 30)
     *
     * @param userId     User ID
     * @param fuelBefore Fuel level before feeding
     */
    @Transactional
    public void awardFeedBonus(Long userId, int fuelBefore) {
        if (fuelBefore < 30) {
            awardXp(userId, 5, "FEED_BONUS");
            log.info("Awarded feed bonus: Pet was hungry (fuel: {})", fuelBefore);
        }
    }

    /**
     * Awards XP for bathing when pet is dirty (cleanliness < 30)
     *
     * @param userId            User ID
     * @param cleanlinessBefore Cleanliness level before bathing
     */
    @Transactional
    public void awardBatheBonus(Long userId, int cleanlinessBefore) {
        if (cleanlinessBefore < 30) {
            awardXp(userId, 5, "BATHE_BONUS");
            log.info("Awarded bathe bonus: Pet was dirty (cleanliness: {})", cleanlinessBefore);
        }
    }

    /**
     * Awards XP for motivating when pet is discouraged (motivation < 30)
     *
     * @param userId           User ID
     * @param motivationBefore Motivation level before motivating
     */
    @Transactional
    public void awardMotivateBonus(Long userId, int motivationBefore) {
        if (motivationBefore < 30) {
            awardXp(userId, 10, "MOTIVATE_BONUS");
            log.info("Awarded motivate bonus: Pet was discouraged (motivation: {})", motivationBefore);
        }
    }

    /**
     * Awards daily bonus for perfect care (all stats healthy)
     * Should be called by scheduled job at midnight
     *
     * @param userId User ID
     */
    @Transactional
    public void checkAndAwardPerfectCareBonus(Long userId) {
        PetStats petStats = petStatsRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Pet not found for user " + userId));

        // Check if all stats are healthy
        boolean isPerfectCare = petStats.getFuel() > 70
                && petStats.getMotivation() > 70
                && petStats.getCleanliness() > 70
                && petStats.getFatigue() < 30;

        if (isPerfectCare) {
            awardXp(userId, 20, "PERFECT_CARE_DAILY");
            log.info("🌟 Awarded perfect care bonus: All stats healthy!");
        }
    }

    // ==========================================
    // UTILITY METHODS
    // ==========================================

    /**
     * Gets total XP earned by pet (for analytics)
     *
     * @param userId User ID
     * @return Total XP earned across all levels
     */
    public int getTotalXpEarned(Long userId) {
        PetStats petStats = petStatsRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Pet not found for user " + userId));

        int currentLevel = petStats.getLevel();
        int currentXp = petStats.getXp();

        // Calculate total XP from all previous levels
        int totalXp = currentXp; // Start with current level progress

        for (int level = 1; level < currentLevel; level++) {
            totalXp += getXpRequiredForLevel(level);
        }

        return totalXp;
    }

    /**
     * Gets XP needed to reach a specific level from current level
     *
     * @param userId      User ID
     * @param targetLevel Target level
     * @return XP needed to reach target level
     */
    public int getXpToLevel(Long userId, int targetLevel) {
        PetStats petStats = petStatsRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Pet not found for user " + userId));

        int currentLevel = petStats.getLevel();
        int currentXp = petStats.getXp();

        if (targetLevel <= currentLevel) {
            return 0; // Already at or past target
        }

        // Calculate XP needed
        int xpNeeded = -currentXp; // Subtract current progress

        for (int level = currentLevel; level < targetLevel; level++) {
            xpNeeded += getXpRequiredForLevel(level);
        }

        return xpNeeded;
    }

    /**
     * Get the next evolution stage after the current one
     * Returns null if already at maximum stage
     *
     * @param currentStage Current evolution stage
     * @return Next stage or null if at max
     */
    public EvolutionStage getNextEvolutionStage(EvolutionStage currentStage) {
        switch (currentStage) {
            case BABY:
                return EvolutionStage.KID;
            case KID:
                return EvolutionStage.TEEN;
            case TEEN:
                return EvolutionStage.ADULT;
            case ADULT:
                return EvolutionStage.CHAMPION;
            case CHAMPION:
                return EvolutionStage.LEGENDARY;
            case LEGENDARY:
                return null; // Already at max
            default:
                return null;
        }
    }

    /**
     * Get the level required for a specific evolution stage
     *
     * @param stage Evolution stage
     * @return Required level
     */
    public int getLevelForEvolution(EvolutionStage stage) {
        switch (stage) {
            case BABY:
                return 1;
            case KID:
                return 11;
            case TEEN:
                return 26;
            case ADULT:
                return 51;
            case CHAMPION:
                return 76;
            case LEGENDARY:
                return 100;
            default:
                return 1;
        }
    }
}