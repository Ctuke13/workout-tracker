package com.chidituke.workout_tracker.service.progress;

import com.chidituke.workout_tracker.model.progress.Season;
import com.chidituke.workout_tracker.model.progress.enums.SeasonType;
import com.chidituke.workout_tracker.repository.progress.SeasonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Service for automatic season transitions.
 * <p>
 * Runs daily at 2 AM to check if current season has ended
 * and automatically transitions to the next season.
 * <p>
 * Season Transition Process:
 * 1. Check if current season has ended
 * 2. Create next season if it doesn't exist
 * 3. Activate next season (deactivates current automatically)
 * 4. Reset all users' seasonal stats with 3-tier drop
 *
 * @author Workout Tracker Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SeasonTransitionService {

    private final SeasonRepository seasonRepository;
    private final SeasonService seasonService;
    private final UserProgressionService userProgressionService;

    /**
     * Check for season transition daily at 2 AM.
     * <p>
     * Cron: "0 0 2 * * *" = Every day at 2:00 AM
     * <p>
     * Why 2 AM?
     * - Low traffic time (most users asleep)
     * - Gives time to complete before users wake up
     * - Minimizes disruption to active users
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void checkAndTransitionSeason() {
        log.info("🕐 Running scheduled season transition check...");

        try {
            // Get current active season
            Optional<Season> activeSeasonOpt = seasonService.findActiveSeason();

            if (activeSeasonOpt.isEmpty()) {
                log.warn("⚠️ No active season found! Cannot perform transition check.");
                return;
            }

            Season activeSeason = activeSeasonOpt.get();
            LocalDate today = LocalDate.now();

            // Check if season has ended
            if (today.isAfter(activeSeason.getEndDate())) {
                log.warn("⚠️ Current season '{}' has ended! Starting transition...",
                        activeSeason.getSeasonName());
                transitionToNextSeason(activeSeason);
            } else {
                long daysRemaining = java.time.temporal.ChronoUnit.DAYS
                        .between(today, activeSeason.getEndDate());
                log.info("✅ Season '{}' is still active. Days remaining: {}",
                        activeSeason.getSeasonName(), daysRemaining);
            }

        } catch (Exception e) {
            log.error("❌ Error during season transition check", e);
            // Don't throw - we don't want to break the schedule
        }
    }

    /**
     * Check and fix season on application startup.
     * Runs once after Spring Boot fully initializes.
     * <p>
     * This ensures we're in the correct season even if:
     * - App was down for a long time
     * - Database was manually modified
     * - First time starting the app
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void checkSeasonOnStartup() {
        log.info("🚀 Application started - checking if season is current...");

        try {
            Optional<Season> activeSeasonOpt = seasonService.findActiveSeason();

            if (activeSeasonOpt.isEmpty()) {
                log.warn("⚠️ No active season found on startup!");
                return;
            }

            Season activeSeason = activeSeasonOpt.get();
            LocalDate today = LocalDate.now();

            // Check if current active season has ended
            if (today.isAfter(activeSeason.getEndDate())) {
                log.warn("⚠️ Active season '{}' ended on {}! Current date: {}",
                        activeSeason.getSeasonName(),
                        activeSeason.getEndDate(),
                        today);
                log.warn("🔄 Triggering catch-up transition...");

                // Trigger transition to catch up to current date
                transitionToCorrectSeason(activeSeason);
            } else {
                log.info("✅ Season '{}' is current (ends {})",
                        activeSeason.getSeasonName(),
                        activeSeason.getEndDate());
            }

        } catch (Exception e) {
            log.error("❌ Error checking season on startup", e);
            // Don't throw - don't want to break app startup
        }
    }

    /**
     * Transition through multiple seasons if needed to reach correct season for today.
     *
     * @param currentSeason The currently active season (might be outdated)
     */
    @Transactional
    public void transitionToCorrectSeason(Season currentSeason) {
        log.info("🔄 Starting catch-up season transition from '{}'...", currentSeason.getSeasonName());

        LocalDate today = LocalDate.now();
        Season activeSeason = currentSeason;
        int transitionCount = 0;

        try {
            // Keep transitioning until we're in the correct season for today
            while (today.isAfter(activeSeason.getEndDate())) {
                LocalDate nextSeasonStart = activeSeason.getEndDate().plusDays(1);
                Season nextSeason = getOrCreateNextSeason(nextSeasonStart);

                transitionCount++;
                log.info("🔄 Transition #{}: {} → {}",
                        transitionCount,
                        activeSeason.getSeasonName(),
                        nextSeason.getSeasonName());

                // Activate next season (deactivates current automatically)
                seasonService.activateSeason(nextSeason.getSeasonId());

                activeSeason = nextSeason;
            }

            // Now reset user stats ONCE for the final season
            if (transitionCount > 0) {
                log.info("🔄 Resetting seasonal stats for all users (final season: {})...",
                        activeSeason.getSeasonName());
                userProgressionService.resetSeasonalStatsForAllUsers(
                        activeSeason.getSeasonId(),
                        activeSeason.getStartDate()
                );
                log.info("✅ User stats reset complete!");
            }

            log.info("🎉 Catch-up transition complete! Skipped {} season(s). Current: {}",
                    transitionCount,
                    activeSeason.getSeasonName());

        } catch (Exception e) {
            log.error("❌ Failed to transition to correct season from '{}'",
                    currentSeason.getSeasonName(), e);
            throw new RuntimeException("Season catch-up failed", e);
        }
    }

    /**
     * Transition from current season to next season.
     * <p>
     * Steps:
     * 1. Get or create next season
     * 2. Activate next season (SeasonService handles deactivation automatically)
     * 3. Reset all users' seasonal stats with 3-tier drop
     * 4. Log successful transition
     *
     * @param currentSeason The season that just ended
     */
    @Transactional
    public void transitionToNextSeason(Season currentSeason) {
        log.info("🔄 Starting season transition from '{}'...", currentSeason.getSeasonName());

        try {
            // 1. Get next season (or create if doesn't exist)
            LocalDate nextSeasonStart = currentSeason.getEndDate().plusDays(1);
            Season nextSeason = getOrCreateNextSeason(nextSeasonStart);

            // 2. Activate next season (deactivates current automatically via SeasonService)
            seasonService.activateSeason(nextSeason.getSeasonId());
            log.info("✅ Activated season: {}", nextSeason.getSeasonName());

            // 3. Reset all users' seasonal stats (3-tier drop + NOVICE I floor)
            log.info("🔄 Resetting seasonal stats for all users...");
            userProgressionService.resetSeasonalStatsForAllUsers(
                    nextSeason.getSeasonId(),
                    nextSeason.getStartDate()
            );
            log.info("✅ User stats reset complete!");

            // 4. Log successful transition
            log.info("🎉 Season transition complete! {} → {}",
                    currentSeason.getSeasonName(),
                    nextSeason.getSeasonName());

        } catch (Exception e) {
            log.error("❌ Failed to transition season from '{}'",
                    currentSeason.getSeasonName(), e);
            throw new RuntimeException("Season transition failed", e);
        }
    }

    /**
     * Get next season or create it if it doesn't exist.
     * <p>
     * Season naming convention:
     * - Winter: January 1 - March 31
     * - Spring: April 1 - June 30
     * - Summer: July 1 - September 30
     * - Fall: October 1 - December 31
     *
     * @param startDate Start date of next season
     * @return Next season (existing or newly created)
     */
    private Season getOrCreateNextSeason(LocalDate startDate) {
        // Check if season already exists
        Optional<Season> existingSeason = seasonRepository.findByStartDate(startDate);

        if (existingSeason.isPresent()) {
            log.info("📅 Found existing next season: {}", existingSeason.get().getSeasonName());
            return existingSeason.get();
        }

        // Create new season
        log.info("📅 Creating new season starting {}", startDate);

        int startMonth = startDate.getMonthValue();
        int year = startDate.getYear();

        SeasonType seasonType;
        String seasonName;
        LocalDate endDate;

        if (startMonth >= 1 && startMonth <= 3) {
            // Winter: Jan-Mar
            seasonType = SeasonType.WINTER;
            seasonName = "Winter " + year;
            endDate = LocalDate.of(year, 3, 31);
        } else if (startMonth >= 4 && startMonth <= 6) {
            // Spring: Apr-Jun
            seasonType = SeasonType.SPRING;
            seasonName = "Spring " + year;
            endDate = LocalDate.of(year, 6, 30);
        } else if (startMonth >= 7 && startMonth <= 9) {
            // Summer: Jul-Sep
            seasonType = SeasonType.SUMMER;
            seasonName = "Summer " + year;
            endDate = LocalDate.of(year, 9, 30);
        } else {
            // Fall: Oct-Dec
            seasonType = SeasonType.FALL;
            seasonName = "Fall " + year;
            endDate = LocalDate.of(year, 12, 31);
        }

        Season newSeason = seasonService.createSeason(seasonName, seasonType, startDate, endDate);
        log.info("✅ Created new season: {} ({} to {})",
                newSeason.getSeasonName(),
                newSeason.getStartDate(),
                newSeason.getEndDate());

        return newSeason;
    }

    /**
     * Manual trigger for admin to force season transition.
     * Useful for testing or emergency transitions.
     * <p>
     * WARNING: This will immediately transition to the next season
     * and reset all users' seasonal stats. Use with caution!
     */
    @Transactional
    public void forceSeasonTransition() {
        log.warn("⚠️ Manual season transition triggered by admin!");
        Season activeSeason = seasonService.getActiveSeason();
        transitionToNextSeason(activeSeason);
    }
}