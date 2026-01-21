package com.chidituke.workout_tracker.service.pet;

import com.chidituke.workout_tracker.dto.response.pet.*;
import com.chidituke.workout_tracker.mapper.pet.PetStatsMapper;
import com.chidituke.workout_tracker.model.pet.PetStats;
import com.chidituke.workout_tracker.repository.pet.PetStatsRepository;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing pet statistics and interactions
 * Handles stat decay, interactions, and scheduled maintenance
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PetStatsService {

    private final PetStatsRepository petStatsRepository;
    private final UserRepository userRepository;
    private final PetStatsMapper petStatsMapper;

    // ============================================
    // CORE CRUD OPERATIONS
    // ============================================

    /**
     * Get pet stats for a user (creates new pet if doesn't exist)
     *
     * @param userId The user's ID
     * @return The pet stats
     */
    @Transactional
    public PetStats getPetStats(Long userId) {
        return petStatsRepository.findByUserId(userId)
                .orElseGet(() -> createPetForUser(userId));
    }

    /**
     * Get pet stats for a user (returns empty if doesn't exist)
     *
     * @param userId The user's ID
     * @return Optional containing pet stats if found
     */
    @Transactional(readOnly = true)
    public Optional<PetStats> getPetStatsIfExists(Long userId) {
        return petStatsRepository.findByUserId(userId);
    }

    /**
     * Create a new pet for a user
     *
     * @param userId The user's ID
     * @return The newly created pet stats
     */
    @Transactional
    public PetStats createPetForUser(Long userId) {
        // Check if pet already exists
        if (petStatsRepository.existsByUserId(userId)) {
            throw new IllegalStateException("Pet already exists for user " + userId);
        }

        PetStats petStats = new PetStats();
        petStats.setUserId(userId);

        PetStats saved = petStatsRepository.save(petStats);
        log.info("🐺 Created new pet for user {} with all stats at 100%", userId);

        return saved;
    }

    /**
     * Delete a pet (used when user deletes account or resets pet)
     *
     * @param userId The user's ID
     */
    @Transactional
    public void deletePet(Long userId) {
        petStatsRepository.deleteByUserId(userId);
        log.info("🗑️ Deleted pet for user {}", userId);
    }

    // ============================================
    // WORKOUT COMPLETION (Called from ProgressController)
    // ============================================

    /**
     * Handle workout completion - awards crystals, applies fatigue, updates stats
     * This is called from ProgressController after workout completion
     *
     * @param userId        The user's ID
     * @param exerciseCount Number of exercises completed
     * @return WorkoutCompleteResponse with crystal/fatigue info
     */
    @Transactional
    public WorkoutCompleteResponse handleWorkoutCompletion(Long userId, int exerciseCount) {
        PetStats petStats = getPetStats(userId);

        // Store old values for response
        int oldCrystals = petStats.getCrystals();
        int oldFatigue = petStats.getFatigue();

        // 1. Award crystals (tiered system)
        int wastedCrystals = petStats.earnCrystalsFromWorkout(exerciseCount);
        int crystalsEarned = (petStats.getCrystals() - oldCrystals) + wastedCrystals;

        // 2. Apply workout stat changes
        petStats.applyWorkoutCompletion(exerciseCount);

        // 3. Save
        PetStats saved = petStatsRepository.save(petStats);

        // 4. Build response
        String message = buildWorkoutCompleteMessage(crystalsEarned, wastedCrystals, saved.getIsSleeping());

        log.info("💪 Workout complete for user {}: +{} crystals ({} wasted), fatigue: {}% → {}%, sleeping: {}",
                userId, crystalsEarned, wastedCrystals, oldFatigue, saved.getFatigue(), saved.getIsSleeping());

        return WorkoutCompleteResponse.builder()
                .crystalsEarned(crystalsEarned)
                .wastedCrystals(wastedCrystals)
                .newCrystalBalance(saved.getCrystals())
                .fatigueIncrease(saved.getFatigue() - oldFatigue)
                .newFatigue(saved.getFatigue())
                .isSleeping(saved.getIsSleeping())
                .sleepTimeRemainingMinutes(calculateSleepTimeRemaining(saved))
                .motivationGain(15)
                .newMotivation(saved.getMotivation())
                .cleanlinessDecrease(10)
                .newCleanliness(saved.getCleanliness())
                .message(message)
                .build();
    }

    private String buildWorkoutCompleteMessage(int crystalsEarned, int wastedCrystals, boolean isSleeping) {
        StringBuilder message = new StringBuilder();
        message.append("Earned ").append(crystalsEarned).append(" crystals!");

        if (wastedCrystals > 0) {
            message.append(" (").append(wastedCrystals).append(" wasted - crystal cap reached)");
        }

        if (isSleeping) {
            message.append(" Your pet is exhausted and needs 24 hours of rest!");
        }

        return message.toString();
    }

    private Long calculateSleepTimeRemaining(PetStats petStats) {
        if (!petStats.getIsSleeping() || petStats.getSleepEndTime() == null) {
            return null;
        }
        long minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), petStats.getSleepEndTime());
        return Math.max(0, minutes);
    }

    // ============================================
    // FEEDING (With Crystal Cost & Efficiency)
    // ============================================

    /**
     * Feed pet a snack (costs 1 crystal)
     * Restores +15 fuel × efficiency
     *
     * @param userId The user's ID
     * @return FeedResponse with details
     */
    @Transactional
    public FeedResponse feedSnack(Long userId) {
        return feedWithCost(userId, 15, 1, "SNACK");
    }

    /**
     * Feed pet a meal (costs 3 crystals)
     * Restores +40 fuel × efficiency - BEST VALUE
     *
     * @param userId The user's ID
     * @return FeedResponse with details
     */
    @Transactional
    public FeedResponse feedMeal(Long userId) {
        return feedWithCost(userId, 40, 3, "MEAL");
    }

    /**
     * Feed pet a feast (costs 5 crystals)
     * Restores +60 fuel × efficiency
     *
     * @param userId The user's ID
     * @return FeedResponse with details
     */
    @Transactional
    public FeedResponse feedFeast(Long userId) {
        return feedWithCost(userId, 60, 5, "FEAST");
    }

    private FeedResponse feedWithCost(Long userId, int baseFuel, int crystalCost, String mealType) {
        PetStats petStats = getPetStats(userId);

        // Check if can feed
        if (petStats.getIsSleeping()) {
            throw new IllegalStateException("Cannot feed pet while sleeping");
        }

        // Get efficiency before feeding
        double efficiency = petStats.getFeedingEfficiency();

        // Feed with efficiency
        int actualFuelGained = petStats.feedWithEfficiency(baseFuel, crystalCost);

        if (actualFuelGained == -1) {
            throw new IllegalStateException("Not enough crystals to feed pet");
        }

        // Save
        PetStats saved = petStatsRepository.save(petStats);

        // Build messages
        String message = String.format("Fed your pet a %s! +%d Fuel", mealType, actualFuelGained);
        String efficiencyWarning = efficiency < 1.0
                ? String.format("Efficiency reduced to %.0f%% - workout to improve!", efficiency * 100)
                : null;

        log.info("🍖 User {} fed pet {} (cost: {} crystals): +{} fuel ({}% efficiency)",
                userId, mealType, crystalCost, actualFuelGained, (int) (efficiency * 100));

        return FeedResponse.builder()
                .petStats(petStatsMapper.toResponse(saved))
                .mealType(mealType)
                .crystalsSpent(crystalCost)
                .baseFuel(baseFuel)
                .efficiency(efficiency)
                .actualFuelGained(actualFuelGained)
                .message(message)
                .efficiencyWarning(efficiencyWarning)
                .build();
    }

    /**
     * Preview feeding results without executing
     *
     * @param userId   The user's ID
     * @param mealType "SNACK", "MEAL", or "FEAST"
     * @return FeedPreviewResponse
     */
    @Transactional(readOnly = true)
    public FeedPreviewResponse getFeedPreview(Long userId, String mealType) {
        PetStats petStats = getPetStats(userId);

        int baseFuel;
        int crystalCost;

        switch (mealType.toUpperCase()) {
            case "SNACK" -> {
                baseFuel = 15;
                crystalCost = 1;
            }
            case "MEAL" -> {
                baseFuel = 40;
                crystalCost = 3;
            }
            case "FEAST" -> {
                baseFuel = 60;
                crystalCost = 5;
            }
            default -> throw new IllegalArgumentException("Invalid meal type: " + mealType);
        }

        double efficiency = petStats.getFeedingEfficiency();
        int actualFuel = (int) (baseFuel * efficiency);

        long daysSinceWorkout = petStats.getLastWorkoutTime() != null
                ? ChronoUnit.DAYS.between(petStats.getLastWorkoutTime(), LocalDateTime.now())
                : 999;

        String efficiencyMessage = switch ((int) (efficiency * 100)) {
            case 100 -> "100% efficiency - worked out recently!";
            case 85 -> "85% efficiency - worked out 2 days ago";
            case 70 -> "70% efficiency - worked out 3 days ago";
            case 55 -> "55% efficiency - worked out 4 days ago";
            default -> "40% efficiency - haven't worked out in 5+ days";
        };

        return FeedPreviewResponse.builder()
                .baseFuel(baseFuel)
                .efficiency(efficiency)
                .actualFuel(actualFuel)
                .crystalCost(crystalCost)
                .currentCrystals(petStats.getCrystals())
                .canAfford(petStats.getCrystals() >= crystalCost)
                .daysSinceLastWorkout((int) daysSinceWorkout)
                .efficiencyMessage(efficiencyMessage)
                .build();
    }

    // ============================================
    // INTERACTIONS
    // ============================================

    /**
     * Motivate interaction - Give pet a pep talk
     * Restores +10 motivation
     * Cooldown: 12 hours
     * Requires: fuel >= 40%
     *
     * @param userId The user's ID
     * @return Updated pet stats
     * @throws IllegalStateException if interaction cannot be performed
     */
    @Transactional
    public PetStats motivate(Long userId) {
        PetStats petStats = getPetStats(userId);

        if (!petStats.canMotivate()) {
            throw new IllegalStateException(petStats.getDisabledReason());
        }

        // Check cooldown
        if (petStats.getLastMotivateTime() != null) {
            long hoursSince = ChronoUnit.HOURS.between(petStats.getLastMotivateTime(), LocalDateTime.now());
            if (hoursSince < 12) {
                long hoursRemaining = 12 - hoursSince;
                throw new IllegalStateException("Motivate is on cooldown. " + hoursRemaining + " hours remaining.");
            }
        }

        // Apply motivation
        petStats.setMotivation(Math.min(100, petStats.getMotivation() + 10));
        petStats.setLastMotivateTime(LocalDateTime.now());
        petStats.setLastUpdated(LocalDateTime.now());

        PetStats saved = petStatsRepository.save(petStats);
        log.info("💪 User {} motivated their pet: Motivation +10", userId);

        return saved;
    }

    /**
     * Bath interaction - Clean the pet
     * Tier 1 (60-79%): Deodorant spray +30 cleanliness
     * Tier 2 (40-59%): Sponge +50 cleanliness
     * Tier 3 (0-39%): Sponge + showerhead +60 cleanliness
     * <p>
     * Requires: motivation >= 40% AND fuel >= 20%
     * No cooldown
     *
     * @param userId The user's ID
     * @return BatheResponse containing tier info and updated stats
     * @throws IllegalStateException if pet cannot bathe
     */
    @Transactional
    public BatheResponse bathe(Long userId) {
        PetStats petStats = getPetStats(userId);

        if (!petStats.canBathe()) {
            throw new IllegalStateException(petStats.getDisabledReason());
        }

        // Determine tier and restoration
        int tier = petStats.getBathTier();
        int restoration;
        String tierName;
        String tierDescription;

        switch (tier) {
            case 1 -> {
                restoration = 30;
                tierName = "Deodorant Spray";
                tierDescription = "Quick spray to freshen up";
            }
            case 2 -> {
                restoration = 50;
                tierName = "Sponge Bath";
                tierDescription = "Good scrub with a sponge";
            }
            case 3 -> {
                restoration = 60;
                tierName = "Full Shower";
                tierDescription = "Complete wash with showerhead";
            }
            default -> throw new IllegalStateException("Invalid bath tier: " + tier);
        }

        // Apply bath
        petStats.setCleanliness(Math.min(100, petStats.getCleanliness() + restoration));
        petStats.setLastBathTime(LocalDateTime.now());
        petStats.setLastUpdated(LocalDateTime.now());

        PetStats saved = petStatsRepository.save(petStats);

        log.info("🛁 User {} bathed their pet (Tier {}): Cleanliness +{} using {}",
                userId, tier, restoration, tierName);

        return BatheResponse.builder()
                .petStats(petStatsMapper.toResponse(saved))
                .bathTier(tier)
                .restoration(restoration)
                .tierName(tierName)
                .tierDescription(tierDescription)
                .message("Your pet is sparkling clean!")
                .build();
    }

    // ============================================
    // HOME SELECTION
    // ============================================

    /**
     * Select pet home environment
     *
     * @param userId   The user's ID
     * @param homeType "GYM", "NATURE", or "COZY"
     * @return Updated pet stats
     */
    @Transactional
    public PetStats selectHome(Long userId, String homeType) {
        PetStats petStats = getPetStats(userId);

        // Validate home type
        List<String> validHomes = Arrays.asList("GYM", "NATURE", "COZY", "BEACH", "SPACE", "CYBER");
        if (!validHomes.contains(homeType)) {
            throw new IllegalArgumentException("Invalid home type: " + homeType);
        }

        // TODO: Check if home is unlocked (for premium homes)

        petStats.setSelectedHome(homeType);
        petStats.setLastUpdated(LocalDateTime.now());

        PetStats saved = petStatsRepository.save(petStats);
        log.info("🏠 User {} selected home: {}", userId, homeType);

        return saved;
    }

    /**
     * Get home information
     *
     * @param userId The user's ID
     * @return HomeInfoResponse
     */
    @Transactional(readOnly = true)
    public HomeInfoResponse getHomeInfo(Long userId) {
        PetStats petStats = getPetStats(userId);

        // For MVP: All 3 basic homes are unlocked
        List<String> unlockedHomes = Arrays.asList("GYM", "NATURE", "COZY");
        List<String> lockedHomes = Arrays.asList("BEACH", "SPACE", "CYBER");

        return HomeInfoResponse.builder()
                .selectedHome(petStats.getSelectedHome())
                .unlockedHomes(unlockedHomes)
                .lockedHomes(lockedHomes)
                .build();
    }

    // ============================================
    // SCHEDULED JOBS
    // ============================================

    /**
     * Daily maintenance job - runs every day at 3 AM
     * Applies daily stat decay and checks sleep/neglect status for all pets
     */
    @Scheduled(cron = "0 0 3 * * *") // Every day at 3 AM
    @Transactional
    public void runDailyMaintenance() {
        log.info("🕐 Starting daily pet maintenance job...");

        List<PetStats> allPets = petStatsRepository.findAll();

        int updatedCount = 0;
        for (PetStats petStats : allPets) {
            petStats.applyDailyMaintenance();
            petStatsRepository.save(petStats);
            updatedCount++;
        }

        log.info("✅ Daily maintenance complete: Updated {} pets", updatedCount);
    }

    /**
     * Manual trigger for daily maintenance (for testing)
     *
     * @return Number of pets updated
     */
    @Transactional
    public int triggerDailyDecay() {
        log.info("🔧 Manually triggering daily maintenance...");

        List<PetStats> allPets = petStatsRepository.findAll();

        int updatedCount = 0;
        for (PetStats petStats : allPets) {
            petStats.applyDailyMaintenance();
            petStatsRepository.save(petStats);
            updatedCount++;
        }

        log.info("✅ Manual maintenance complete: Updated {} pets", updatedCount);
        return updatedCount;
    }

    // ============================================
    // ADMIN OPERATIONS
    // ============================================

    /**
     * Force wake a sleeping pet (for testing)
     *
     * @param userId The user's ID
     */
    @Transactional
    public void forceWakePet(Long userId) {
        PetStats petStats = getPetStats(userId);

        if (petStats.getIsSleeping()) {
            petStats.setIsSleeping(false);
            petStats.setFatigue(0);
            petStats.setSleepStartTime(null);
            petStats.setSleepEndTime(null);
            petStats.setLastUpdated(LocalDateTime.now());

            petStatsRepository.save(petStats);
            log.info("⚠️ Admin forced pet awake for user {}", userId);
        }
    }

    /**
     * Clear neglect state (for testing)
     *
     * @param userId The user's ID
     */
    @Transactional
    public void clearNeglect(Long userId) {
        PetStats petStats = getPetStats(userId);

        if (petStats.getIsNeglected()) {
            petStats.setIsNeglected(false);
            petStats.setNeglectRecoveryTime(null);
            petStats.setLastUpdated(LocalDateTime.now());

            petStatsRepository.save(petStats);
            log.info("⚠️ Admin cleared neglect for user {}", userId);
        }
    }

    // ============================================
    // ANALYTICS (for admin/monitoring)
    // ============================================

    /**
     * Get system-wide pet statistics
     *
     * @return SystemStats containing averages and counts
     */
    @Transactional(readOnly = true)
    public SystemStats getSystemStats() {
        return new SystemStats(
                petStatsRepository.getTotalPetCount(),
                petStatsRepository.getAverageFuel(),
                petStatsRepository.getAverageMotivation(),
                petStatsRepository.getAverageFatigue(),
                petStatsRepository.getAverageCleanliness(),
                petStatsRepository.countHealthyPets(),
                petStatsRepository.countSleepingPets()
        );
    }

    /**
     * Find pets that need attention (low stats)
     *
     * @return NeedAttentionStats with lists of pets needing care
     */
    @Transactional(readOnly = true)
    public NeedAttentionStats getPetsNeedingAttention() {
        return new NeedAttentionStats(
                petStatsRepository.findByFuelLessThan(30),
                petStatsRepository.findByMotivationLessThan(40),
                petStatsRepository.findByFatigueGreaterThan(70),
                petStatsRepository.findByCleanlinessLessThan(40),
                petStatsRepository.findSleepingPets(),
                petStatsRepository.findNeglectedPets()
        );
    }

    // ============================================
    // INNER CLASSES - RESPONSE OBJECTS
    // ============================================

    /**
     * System-wide statistics for all pets
     */
    public record SystemStats(
            Long totalPets,
            Double averageFuel,
            Double averageMotivation,
            Double averageFatigue,
            Double averageCleanliness,
            Long healthyPets,
            Long sleepingPets
    ) {
    }

    /**
     * Pets needing attention (low stats)
     */
    public record NeedAttentionStats(
            List<PetStats> lowFuel,
            List<PetStats> lowMotivation,
            List<PetStats> highFatigue,
            List<PetStats> dirty,
            List<PetStats> sleeping,
            List<PetStats> neglected
    ) {
    }
}