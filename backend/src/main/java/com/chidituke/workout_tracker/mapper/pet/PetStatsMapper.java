package com.chidituke.workout_tracker.mapper.pet;

import com.chidituke.workout_tracker.dto.response.pet.PetStatsResponse;
import com.chidituke.workout_tracker.model.pet.PetStats;
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

        return PetStatsResponse.builder()
                // Identifiers
                .petStatsId(petStats.getPetStatsId())
                .userId(petStats.getUserId())

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

    /**
     * Get human-readable tier name for bath tier
     *
     * @param tier The bath tier (1, 2, or 3)
     * @return Human-readable tier name
     */
    private String getTierName(int tier) {
        return switch (tier) {
            case 1 -> "Deodorant Spray";
            case 2 -> "Sponge Bath";
            case 3 -> "Full Shower";
            default -> "Unknown";
        };
    }

    /**
     * Get tier description
     */
    private String getTierDescription(int tier) {
        return switch (tier) {
            case 1 -> "Quick spray to freshen up";
            case 2 -> "Good scrub with a sponge";
            case 3 -> "Complete wash with showerhead";
            default -> "Unknown bath type";
        };
    }
}