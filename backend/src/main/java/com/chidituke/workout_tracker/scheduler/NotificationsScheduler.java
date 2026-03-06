package com.chidituke.workout_tracker.scheduler;

import com.chidituke.workout_tracker.model.pet.PetStats;
import com.chidituke.workout_tracker.model.progress.UserProgression;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.repository.pet.PetStatsRepository;
import com.chidituke.workout_tracker.repository.progress.UserProgressionRepository;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import com.chidituke.workout_tracker.service.notifications.NotificationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Scheduled jobs that check conditions and fire push notifications.
 * <p>
 * Schedule overview:
 * - Pet health check       → every 3 hours
 * - Streak risk check      → daily at 8:00 PM (gives user 4 hours before midnight)
 * - Re-engagement check    → daily at 10:00 AM
 * - Weekly summary         → every Monday at 9:00 AM
 * <p>
 * Pet stat thresholds (mirrors frontend warning levels):
 * Fuel        : LOW ≤ 25,  CRITICAL ≤ 10
 * Motivation  : LOW ≤ 25,  CRITICAL ≤ 10
 * Cleanliness : LOW ≤ 25,  CRITICAL ≤ 10
 * Fatigue     : HIGH ≥ 75, CRITICAL ≥ 90
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationsScheduler {

    private final PetStatsRepository petStatsRepository;
    private final UserProgressionRepository userProgressionRepository;
    private final UserRepository userRepository;
    private final NotificationsService notificationsService;

    // ── Thresholds ─────────────────────────────────────────────────────────────
    private static final int FUEL_LOW = 25;
    private static final int FUEL_CRITICAL = 10;
    private static final int MOTIVATION_LOW = 25;
    private static final int MOTIVATION_CRITICAL = 10;
    private static final int CLEANLINESS_LOW = 25;
    private static final int CLEANLINESS_CRITICAL = 10;
    private static final int FATIGUE_HIGH = 75;
    private static final int FATIGUE_CRITICAL = 90;

    // Re-engagement thresholds (days since last workout)
    private static final int REENGAGEMENT_WARN = 3;
    private static final int REENGAGEMENT_NUDGE = 7;

    // ── Pet Health Check — every 3 hours ───────────────────────────────────────

    @Scheduled(fixedRate = 3 * 60 * 60 * 1000) // every 3 hours
    public void checkPetHealth() {
        log.info("🔔 Running pet health notification check...");
        int notified = 0;

        List<PetStats> allPets = petStatsRepository.findAll();

        for (PetStats pet : allPets) {
            Optional<User> userOpt = userRepository.findById(pet.getUserId());
            if (userOpt.isEmpty()) continue;
            User user = userOpt.get();

            // Skip sleeping pets — their stats don't decay while asleep
            if (Boolean.TRUE.equals(pet.getIsSleeping())) continue;

            // ── Fuel ───────────────────────────────────────────────────────────
            if (pet.getFuel() <= FUEL_CRITICAL) {
                notificationsService.sendFuelCritical(user);
                notified++;
            } else if (pet.getFuel() <= FUEL_LOW) {
                notificationsService.sendFuelLow(user);
                notified++;
            }

            // ── Motivation ─────────────────────────────────────────────────────
            if (pet.getMotivation() <= MOTIVATION_CRITICAL) {
                notificationsService.sendMotivationCritical(user);
                notified++;
            } else if (pet.getMotivation() <= MOTIVATION_LOW) {
                notificationsService.sendMotivationLow(user);
                notified++;
            }

            // ── Cleanliness ────────────────────────────────────────────────────
            if (pet.getCleanliness() <= CLEANLINESS_CRITICAL) {
                notificationsService.sendCleanlinesCritical(user);
                notified++;
            } else if (pet.getCleanliness() <= CLEANLINESS_LOW) {
                notificationsService.sendCleanlinesLow(user);
                notified++;
            }

            // ── Fatigue ────────────────────────────────────────────────────────
            if (pet.getFatigue() >= FATIGUE_CRITICAL) {
                notificationsService.sendFatigueCritical(user);
                notified++;
            }
        }

        log.info("🔔 Pet health check complete — {} notifications sent", notified);
    }

    // ── Streak Risk Check — daily at 8:00 PM ──────────────────────────────────

    @Scheduled(cron = "0 0 20 * * *") // 8:00 PM every day
    public void checkStreakRisk() {
        log.info("🔥 Running streak risk notification check...");
        int notified = 0;

        LocalDate today = LocalDate.now();
        List<UserProgression> allProgressions = userProgressionRepository.findAll();

        for (UserProgression progression : allProgressions) {
            // Only warn users with an active streak
            if (progression.getCurrentStreakDays() < 1) continue;

            Optional<User> userOpt = userRepository.findById(progression.getUserId());
            if (userOpt.isEmpty()) continue;
            User user = userOpt.get();

            LocalDate lastWorkout = progression.getLastWorkoutDate();

            // If last workout was NOT today, streak is at risk
            if (lastWorkout == null || !lastWorkout.isEqual(today)) {
                notificationsService.sendStreakAtRisk(user, progression.getCurrentStreakDays());
                notified++;
            }
        }

        log.info("🔥 Streak check complete — {} at-risk notifications sent", notified);
    }

    // ── Re-engagement Check — daily at 10:00 AM ───────────────────────────────

    @Scheduled(cron = "0 0 10 * * *") // 10:00 AM every day
    public void checkReengagement() {
        log.info("👋 Running re-engagement notification check...");
        int notified = 0;

        LocalDate today = LocalDate.now();
        List<UserProgression> allProgressions = userProgressionRepository.findAll();

        for (UserProgression progression : allProgressions) {
            LocalDate lastWorkout = progression.getLastWorkoutDate();
            if (lastWorkout == null) continue;

            long daysSince = today.toEpochDay() - lastWorkout.toEpochDay();

            // Only notify on exactly day 3 or day 7 to avoid spamming
            if (daysSince != REENGAGEMENT_WARN && daysSince != REENGAGEMENT_NUDGE) continue;

            Optional<User> userOpt = userRepository.findById(progression.getUserId());
            if (userOpt.isEmpty()) continue;
            User user = userOpt.get();

            notificationsService.sendPetNeglected(user, (int) daysSince);
            notified++;
        }

        log.info("👋 Re-engagement check complete — {} notifications sent", notified);
    }

    // ── Weekly Summary — every Monday at 9:00 AM ──────────────────────────────

    @Scheduled(cron = "0 0 9 * * MON") // 9:00 AM every Monday
    public void sendWeeklySummaries() {
        log.info("📊 Running weekly summary notifications...");
        int notified = 0;

        List<UserProgression> allProgressions = userProgressionRepository.findAll();

        for (UserProgression progression : allProgressions) {
            Optional<User> userOpt = userRepository.findById(progression.getUserId());
            if (userOpt.isEmpty()) continue;
            User user = userOpt.get();

            // Use weekly workout count from progression
            // Note: no weekly XP field exists — seasonal XP used as context
            int weeklyWorkouts = progression.getWeeklyWorkoutCount() != null
                    ? progression.getWeeklyWorkoutCount() : 0;
            int seasonalXp = progression.getSeasonalXp() != null
                    ? progression.getSeasonalXp() : 0;

            if (weeklyWorkouts > 0) {
                notificationsService.sendWeeklySummary(user, weeklyWorkouts, seasonalXp);
            } else {
                // Zero workouts — send the missed goal nudge instead
                notificationsService.sendWeeklyGoalMissed(user, 0, 3); // assume 3 as default goal
            }
            notified++;
        }

        log.info("📊 Weekly summaries sent to {} users", notified);
    }
}