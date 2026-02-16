package com.chidituke.workout_tracker.mapper.pet;

import com.chidituke.workout_tracker.dto.response.pet.PetStatsResponse;
import com.chidituke.workout_tracker.model.pet.PetStats;
import com.chidituke.workout_tracker.model.pet.enums.EvolutionStage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

/**
 * Mapper for PetStats entity to DTO transformations
 * Follows the project's mapper pattern for entity-DTO conversion
 */
@Component
public class PetStatsMapper {

    /**
     * Convert PetStats entity to PetStatsResponse DTO
     *
     * @param petStats The PetStats entity
     * @return PetStatsResponse DTO with all pet information
     */
    public PetStatsResponse toResponse(PetStats petStats) {
        if (petStats == null) {
            return null;
        }

        // Calculate evolution-related fields
        int xpRequired = calculateXpRequiredForNextLevel(petStats.getLevel());
        EvolutionStage currentStage = petStats.getEvolutionStage();
        EvolutionStage nextStage = currentStage.getNextStage();
        boolean canEvolve = currentStage.canEvolve() &&
                petStats.getLevel() >= currentStage.getLevelForNextStage();

        PetStatsResponse response = PetStatsResponse.builder()
                // Identifiers
                .petStatsId(petStats.getPetStatsId())
                .userId(petStats.getUserId())

                // Pet Identity & Progression
                .petName(petStats.getPetName())
                .petType(petStats.getPetType().toString())
                .petColor(petStats.getPetColor())
                .xp(petStats.getXp())
                .level(petStats.getLevel())
                .xpToNextLevel(xpRequired)
                .xpProgress(petStats.getXp()) // Current XP in level (for progress bar)
                .evolutionStage(currentStage.toString())
                .evolutionStageDisplay(getEvolutionStageDisplay(petStats))
                .workoutsCompleted(petStats.getWorkoutsCompleted())
                .canEvolve(canEvolve)
                .nextEvolutionStage(nextStage != null ? nextStage.toString() : null)
                .levelForNextEvolution(currentStage.getLevelForNextStage())

                // Core stats (0-100)
                .fuel(petStats.getFuel())
                .motivation(petStats.getMotivation())
                .fatigue(petStats.getFatigue())
                .cleanliness(petStats.getCleanliness())

                // Stat status strings
                .fuelStatus(petStats.getFuelStatus())
                .motivationStatus(petStats.getMotivationStatus())
                .fatigueStatus(petStats.getFatigueStatus())
                .cleanlinessStatus(petStats.getCleanlinessStatus())

                // Crystal economy
                .crystals(petStats.getCrystals())
                .maxCrystals(15)
                .feedingEfficiency(petStats.getFeedingEfficiency())
                .daysSinceLastWorkout(calculateDaysSinceLastWorkout(petStats))

                // Sleep system
                .isSleeping(petStats.getIsSleeping())
                .sleepTimeRemainingMinutes(calculateSleepTimeRemaining(petStats))

                // Neglect system
                .isNeglected(petStats.getIsNeglected())
                .neglectRecoveryTimeRemainingMinutes(calculateNeglectRecoveryTimeRemaining(petStats))

                // Feature gating
                .canMotivate(petStats.canMotivate())
                .canBathe(petStats.canBathe())
                .canFeed(petStats.canFeed())
                .canInteract(petStats.canInteract())
                .disabledReason(petStats.getDisabledReason())

                // Home selection
                .selectedHome(petStats.getSelectedHome())
                .unlockedHomes(getUnlockedHomes())

                // Bath system
                .bathTier(petStats.getBathTier())

                // Cooldowns
                .motivateCooldownHours(petStats.getMotivateCooldownHours())
                .lastMotivateTime(petStats.getLastMotivateTime())
                .lastBathTime(petStats.getLastBathTime())

                // Metadata
                .lastUpdated(petStats.getLastUpdated())
                .createdAt(petStats.getCreatedAt())
                .build();

        // Calculate and set mood based on current stats
        response.calculateAndSetMood();

        return response;
    }

    /**
     * Calculate XP required for next level
     * Progressive scaling:
     * - Levels 1-5: 100 XP per level
     * - Levels 6-10: 150 XP per level
     * - Levels 11-20: 200 XP per level
     * - Levels 21-30: 300 XP per level
     * - Levels 31-50: 500 XP per level
     * - Levels 51-75: 750 XP per level
     * - Levels 76-100+: 1000 XP per level
     */
    private int calculateXpRequiredForNextLevel(int currentLevel) {
        if (currentLevel <= 5) return 100;
        else if (currentLevel <= 10) return 150;
        else if (currentLevel <= 20) return 200;
        else if (currentLevel <= 30) return 300;
        else if (currentLevel <= 50) return 500;
        else if (currentLevel <= 75) return 750;
        else return 1000; // Levels 76+
    }

    /**
     * Get display name for evolution stage (e.g., "Baby Wolf", "Kid Wolf")
     */
    private String getEvolutionStageDisplay(PetStats petStats) {
        String stageName = petStats.getEvolutionStage().getDisplayName();
        String petTypeName = petStats.getPetType().getDisplayName();
        return stageName + " " + petTypeName;
    }

    /**
     * Calculate days since last workout (for efficiency display)
     */
    private Integer calculateDaysSinceLastWorkout(PetStats petStats) {
        if (petStats.getLastWorkoutTime() == null) {
            return null;
        }
        return (int) ChronoUnit.DAYS.between(petStats.getLastWorkoutTime(), LocalDateTime.now());
    }

    /**
     * Calculate sleep time remaining in minutes
     */
    private Long calculateSleepTimeRemaining(PetStats petStats) {
        if (!petStats.getIsSleeping() || petStats.getSleepEndTime() == null) {
            return null;
        }
        long minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), petStats.getSleepEndTime());
        return Math.max(0, minutes);
    }

    /**
     * Calculate neglect recovery time remaining in minutes
     */
    private Long calculateNeglectRecoveryTimeRemaining(PetStats petStats) {
        if (!petStats.getIsNeglected() || petStats.getNeglectRecoveryTime() == null) {
            return null;
        }
        long minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), petStats.getNeglectRecoveryTime());
        return Math.max(0, minutes);
    }

    /**
     * Get list of unlocked homes (for MVP, all 3 basic homes are unlocked)
     */
    private List<String> getUnlockedHomes() {
        return Arrays.asList("GYM", "NATURE", "COZY");
    }
}