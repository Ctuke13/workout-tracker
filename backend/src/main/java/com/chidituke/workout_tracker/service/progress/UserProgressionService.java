package com.chidituke.workout_tracker.service.progress;

import com.chidituke.workout_tracker.model.progress.Season;
import com.chidituke.workout_tracker.model.progress.UserProgression;
import com.chidituke.workout_tracker.model.progress.enums.Rank;
import com.chidituke.workout_tracker.repository.progress.UserProgressionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for managing user progression, XP, ranks, and streaks.
 * <p>
 * Data Flow:
 * 1. User completes workout
 * 2. WorkoutCompletionService calls this service
 * 3. XP awarded based on workout type and duration
 * 4. Stats updated (workouts, sets, volume, distance, etc.)
 * 5. Rank recalculated if XP thresholds reached
 * 6. Streak checked and updated
 * 7. Achievement tracking fields updated
 * 8. AchievementService checks for newly unlocked achievements
 * <p>
 * XP Award Rules:
 * - Base XP: 10 XP per workout
 * - Duration Bonus: +1 XP per 5 minutes
 * - Streak Bonus: +5 XP if 3+ day streak
 * - Weekly Bonus: +20 XP for 7 workouts in a week
 * - Perfect Week: +50 XP for 7 consecutive days
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserProgressionService {

    private final UserProgressionRepository userProgressionRepository;
    private final SeasonService seasonService;

    // ========== XP CONSTANTS (from spec v1.0) ==========
    private static final int BASE_WORKOUT_XP = 100;         // Base XP per workout (was 10)
    private static final int XP_PER_SET = 20;               // NEW: 20 XP per set
    private static final int VOLUME_DIVISOR = 100;          // NEW: 1 XP per 100 lbs
    private static final int XP_PER_5_MINUTES = 10;         // Duration bonus (was 1)
    private static final int STREAK_BONUS_XP = 50;          // Streak bonus (was 5)
    private static final int STREAK_BONUS_THRESHOLD = 3;    // Days needed for bonus
    private static final int WEEKLY_7_WORKOUT_BONUS = 200;  // 7-workout bonus (was 20)
    private static final int PERFECT_WEEK_BONUS = 500;      // Perfect week bonus (was 50)

    // Streak multipliers (weekly workout count)
    private static final double STREAK_4_DAYS = 1.10;  // 10% bonus
    private static final double STREAK_5_DAYS = 1.20;  // 20% bonus
    private static final double STREAK_6_DAYS = 1.35;  // 35% bonus
    private static final double STREAK_7_DAYS = 1.50;  // 50% bonus

    // ========== CORE OPERATIONS ==========

    /**
     * Get user progression by user ID.
     *
     * @param userId The user ID
     * @return Optional containing user progression
     */
    @Transactional(readOnly = true)
    public Optional<UserProgression> getUserProgression(Long userId) {
        return userProgressionRepository.findByUserId(userId);
    }

    /**
     * Get or create user progression.
     * If user doesn't have progression record, creates one with current season.
     *
     * @param userId The user ID
     * @return User progression (existing or newly created)
     */
    @Transactional
    public UserProgression getOrCreateUserProgression(Long userId) {
        return userProgressionRepository.findByUserId(userId)
                .orElseGet(() -> createUserProgression(userId));
    }

    /**
     * Create initial progression record for new user.
     *
     * @param userId The user ID
     * @return Newly created user progression
     */
    @Transactional
    public UserProgression createUserProgression(Long userId) {
        Season activeSeason = seasonService.getActiveSeason();

        UserProgression progression = new UserProgression();
        progression.setUserId(userId);
        progression.setCurrentSeasonId(activeSeason.getSeasonId());
        progression.setSeasonStartDate(activeSeason.getStartDate());

        // Initialize with default values (Lombok defaults handle most)
        progression.setSeasonalXp(0);
        progression.setLifetimeXp(0);
        progression.setSeasonalRank(Rank.NOVICE);
        progression.setLifetimeRank(Rank.NOVICE);
        progression.setSeasonalTier(3);
        progression.setLifetimeTier(3);

        UserProgression saved = userProgressionRepository.save(progression);
        log.info("Created progression record for user {} in season {}",
                userId, activeSeason.getSeasonName());

        return saved;
    }

    // ========== WORKOUT COMPLETION HANDLER ==========

    /**
     * Handle workout completion - awards XP, updates stats, checks streaks.
     * This is the MAIN entry point called after every workout.
     * <p>
     * Data Flow:
     * 1. Get/create user progression
     * 2. Calculate XP to award
     * 3. Update workout statistics
     * 4. Update streak
     * 5. Update weekly tracking
     * 6. Recalculate ranks
     * 7. Save changes
     *
     * @param userId                 User ID
     * @param workoutDurationMinutes Workout duration
     * @param setsCompleted          Number of sets
     * @param volumeLifted           Total volume lifted (kg)
     * @param distanceKm             Distance covered (cardio)
     * @param holdSeconds            Hold duration (isometric)
     * @param uniqueExercisesCount   New unique exercises tried
     * @param workoutType            Type: CARDIO, STRENGTH, ISOMETRIC
     * @return Updated user progression
     */
    @Transactional
    public UserProgression handleWorkoutCompletion(
            Long userId,
            int workoutDurationMinutes,
            int setsCompleted,
            BigDecimal volumeLifted,
            BigDecimal distanceKm,
            int holdSeconds,
            int uniqueExercisesCount,
            String workoutType) {

        UserProgression progression = getOrCreateUserProgression(userId);
        LocalDate today = LocalDate.now();

        // 1. Update workout count
        progression.setTotalWorkoutsCompleted(
                progression.getTotalWorkoutsCompleted() + 1
        );

        // 2. Update sets
        progression.setTotalSetsCompleted(
                progression.getTotalSetsCompleted() + setsCompleted
        );

        // 3. Update volume lifted
        progression.setTotalVolumLifted(
                progression.getTotalVolumLifted().add(volumeLifted)
        );

        // 4. Update workout minutes
        progression.setTotalWorkoutMinutes(
                progression.getTotalWorkoutMinutes() + workoutDurationMinutes
        );

        // 5. Update achievement tracking fields
        updateAchievementTrackingFields(progression, distanceKm, holdSeconds,
                uniqueExercisesCount, workoutType, today);

        // 6. Update streak
        updateStreak(progression, today);

        // 7. Update weekly tracking
        updateWeeklyTracking(progression, today);

        // 8. Calculate and award XP
        int xpAwarded = calculateWorkoutXp(progression, workoutDurationMinutes, setsCompleted, volumeLifted);
        progression.addXp(xpAwarded);
        log.info("Awarded {} XP to user {} (seasonal: {}, lifetime: {})",
                xpAwarded, userId, progression.getSeasonalXp(), progression.getLifetimeXp());

        // 9. Recalculate ranks
        recalculateRanks(progression);

        // 10. Save
        UserProgression saved = userProgressionRepository.save(progression);

        log.info("Workout completion processed for user {}: {} workouts, {} XP, {}-day streak",
                userId, saved.getTotalWorkoutsCompleted(), saved.getLifetimeXp(),
                saved.getCurrentStreakDays());

        return saved;
    }

    // ========== XP CALCULATION ==========

    /**
     * Calculate XP to award for a workout (UPDATED to spec v1.0).
     * <p>
     * Formula: (Base + Sets + Volume + Duration) × Streak Multiplier
     * <p>
     * Components:
     * - Base: 100 XP
     * - Sets: 20 XP per set
     * - Volume: 1 XP per 100 lbs lifted
     * - Duration: +10 XP per 5 minutes
     * - Streak Multipliers: 4 days=1.1×, 5 days=1.2×, 6 days=1.35×, 7 days=1.5×
     * - Weekly Bonus: +200 XP for 7th workout
     * - Perfect Week: +500 XP for 7 consecutive days
     *
     * @param progression     User progression
     * @param durationMinutes Workout duration
     * @param setsCompleted   Number of sets completed
     * @param volumeLifted    Total volume lifted (lbs)
     * @return Total XP to award
     */
    private int calculateWorkoutXp(
            UserProgression progression,
            int durationMinutes,
            int setsCompleted,
            BigDecimal volumeLifted) {

        // 1. Base XP
        int xp = BASE_WORKOUT_XP;
        log.debug("Base XP: {}", xp);

        // 2. Set bonus: +20 XP per set
        int setBonus = setsCompleted * XP_PER_SET;
        xp += setBonus;
        log.debug("Set bonus (+{} sets): +{} XP", setsCompleted, setBonus);

        // 3. Volume bonus: +1 XP per 100 lbs
        int volumeBonus = volumeLifted.intValue() / VOLUME_DIVISOR;
        xp += volumeBonus;
        log.debug("Volume bonus ({} lbs): +{} XP", volumeLifted.intValue(), volumeBonus);

        // 4. Duration bonus: +10 XP per 5 minutes
        int durationBonus = (durationMinutes / 5) * XP_PER_5_MINUTES;
        xp += durationBonus;
        log.debug("Duration bonus ({} min): +{} XP", durationMinutes, durationBonus);

        // 5. Weekly streak multiplier
        int weeklyCount = progression.getWeeklyWorkoutCount() + 1; // Include current workout
        double multiplier = getStreakMultiplier(weeklyCount);

        if (multiplier > 1.0) {
            int xpBeforeMultiplier = xp;
            xp = (int) (xp * multiplier);
            log.debug("Streak multiplier ({} workouts): {}× (+{} XP)",
                    weeklyCount, multiplier, xp - xpBeforeMultiplier);
        }

        // 6. Weekly milestone bonuses
        if (weeklyCount == 7) {
            xp += WEEKLY_7_WORKOUT_BONUS;
            log.debug("7-workout weekly bonus: +{} XP", WEEKLY_7_WORKOUT_BONUS);

            // Perfect week bonus (7 consecutive days)
            if (progression.getCurrentStreakDays() >= 6) {
                xp += PERFECT_WEEK_BONUS;
                log.debug("Perfect week bonus: +{} XP", PERFECT_WEEK_BONUS);
            }
        }

        log.info("Total XP calculated: {} (base: {}, sets: {}, volume: {}, duration: {}, multiplier: {}×)",
                xp, BASE_WORKOUT_XP, setBonus, volumeBonus, durationBonus, multiplier);

        return xp;
    }

    /**
     * Get streak multiplier based on weekly workout count.
     *
     * @param weeklyCount Number of workouts this week
     * @return Multiplier (1.0 - 1.5)
     */
    private double getStreakMultiplier(int weeklyCount) {
        return switch (weeklyCount) {
            case 7 -> STREAK_7_DAYS;  // 1.5×
            case 6 -> STREAK_6_DAYS;  // 1.35×
            case 5 -> STREAK_5_DAYS;  // 1.2×
            case 4 -> STREAK_4_DAYS;  // 1.1×
            default -> 1.0;           // No multiplier
        };
    }

    // ========== RANK CALCULATION ==========

    /**
     * Recalculate both seasonal and lifetime ranks based on current XP.
     * Updates rank and tier for both.
     *
     * @param progression User progression
     */
    private void recalculateRanks(UserProgression progression) {
        // Recalculate seasonal rank and tier
        Rank oldSeasonalRank = progression.getSeasonalRank();
        int oldSeasonalTier = progression.getSeasonalTier();

        Rank newSeasonalRank = Rank.fromXp(progression.getSeasonalXp());
        int newSeasonalTier = calculateTier(progression.getSeasonalXp(), newSeasonalRank);

        if (newSeasonalRank != oldSeasonalRank) {
            progression.setSeasonalRank(newSeasonalRank);
            progression.setSeasonalTier(3); // Reset to tier III on rank up
            log.info("User {} ranked up (seasonal): {} → {}",
                    progression.getUserId(), oldSeasonalRank, newSeasonalRank);
        } else if (newSeasonalTier != oldSeasonalTier) {
            // Tier changed within same rank
            progression.setSeasonalTier(newSeasonalTier);
            log.info("User {} tiered up (seasonal): {} Tier {} → Tier {}",
                    progression.getUserId(), newSeasonalRank, oldSeasonalTier, newSeasonalTier);
        }

        // Recalculate lifetime rank and tier
        Rank oldLifetimeRank = progression.getLifetimeRank();
        int oldLifetimeTier = progression.getLifetimeTier();

        Rank newLifetimeRank = Rank.fromXp(progression.getLifetimeXp());
        int newLifetimeTier = calculateTier(progression.getLifetimeXp(), newLifetimeRank);

        if (newLifetimeRank != oldLifetimeRank) {
            progression.setLifetimeRank(newLifetimeRank);
            progression.setLifetimeTier(3); // Reset to tier III on rank up
            log.info("User {} ranked up (lifetime): {} → {}",
                    progression.getUserId(), oldLifetimeRank, newLifetimeRank);
        } else if (newLifetimeTier != oldLifetimeTier) {
            // Tier changed within same rank
            progression.setLifetimeTier(newLifetimeTier);
            log.info("User {} tiered up (lifetime): {} Tier {} → Tier {}",
                    progression.getUserId(), newLifetimeRank, oldLifetimeTier, newLifetimeTier);
        }
    }

    /**
     * Calculate tier (I, II, or III) based on XP within current rank.
     * Delegates to the Rank enum's calculateTier method.
     *
     * @param xp   Current XP
     * @param rank Current rank
     * @return Tier (3=III, 2=II, 1=I)
     */
    private int calculateTier(int xp, Rank rank) {
        return rank.calculateTier(xp);
    }

    // ========== STREAK TRACKING ==========

    /**
     * Update user's workout streak.
     * <p>
     * Rules:
     * - If worked out today: increment streak
     * - If worked out yesterday: maintain streak
     * - If last workout was 2+ days ago: reset streak to 1
     * - Update longest streak if current exceeds it
     *
     * @param progression User progression
     * @param today       Today's date
     */
    private void updateStreak(UserProgression progression, LocalDate today) {
        LocalDate lastWorkout = progression.getLastWorkoutDate();

        if (lastWorkout == null) {
            // First workout ever
            progression.setCurrentStreakDays(1);
            progression.setLastWorkoutDate(today);
            log.debug("Started first streak for user {}", progression.getUserId());
            return;
        }

        if (lastWorkout.equals(today)) {
            // Already worked out today, no streak change
            return;
        }

        LocalDate yesterday = today.minusDays(1);

        if (lastWorkout.equals(yesterday)) {
            // Worked out yesterday, increment streak
            progression.setCurrentStreakDays(progression.getCurrentStreakDays() + 1);
            progression.setLastWorkoutDate(today);

            // Check if new longest streak
            if (progression.getCurrentStreakDays() > progression.getLongestStreakDays()) {
                progression.setLongestStreakDays(progression.getCurrentStreakDays());
                log.info("New longest streak for user {}: {} days",
                        progression.getUserId(), progression.getCurrentStreakDays());
            }

            log.debug("Streak continued for user {}: {} days",
                    progression.getUserId(), progression.getCurrentStreakDays());
        } else {
            // Streak broken, reset to 1
            int oldStreak = progression.getCurrentStreakDays();
            progression.setCurrentStreakDays(1);
            progression.setLastWorkoutDate(today);
            log.info("Streak broken for user {} (was {} days, now 1)",
                    progression.getUserId(), oldStreak);
        }
    }

    // ========== WEEKLY TRACKING ==========

    /**
     * Update weekly workout count.
     * Resets to 1 if new week started.
     *
     * @param progression User progression
     * @param today       Today's date
     */
    private void updateWeeklyTracking(UserProgression progression, LocalDate today) {
        LocalDate weekStart = progression.getWeekStartDate();
        LocalDate thisWeekStart = today.with(DayOfWeek.MONDAY);

        if (weekStart.isBefore(thisWeekStart)) {
            // New week started, reset count
            progression.setWeeklyWorkoutCount(1);
            progression.setWeekStartDate(thisWeekStart);
            log.debug("New week started for user {}, reset weekly count",
                    progression.getUserId());
        } else {
            // Same week, increment
            progression.setWeeklyWorkoutCount(progression.getWeeklyWorkoutCount() + 1);
            log.debug("Weekly workout count for user {}: {}",
                    progression.getUserId(), progression.getWeeklyWorkoutCount());
        }
    }

    // ========== ACHIEVEMENT TRACKING ==========

    /**
     * Update achievement tracking fields based on workout.
     *
     * @param progression          User progression
     * @param distanceKm           Distance covered (cardio)
     * @param holdSeconds          Hold duration (isometric)
     * @param uniqueExercisesCount New unique exercises
     * @param workoutType          Workout type
     * @param today                Today's date
     */
    private void updateAchievementTrackingFields(
            UserProgression progression,
            BigDecimal distanceKm,
            int holdSeconds,
            int uniqueExercisesCount,
            String workoutType,
            LocalDate today) {

        // Update distance for cardio workouts
        if (distanceKm != null && distanceKm.compareTo(BigDecimal.ZERO) > 0) {
            progression.setTotalDistanceKm(
                    progression.getTotalDistanceKm().add(distanceKm)
            );
        }

        // Update hold seconds for isometric workouts
        if (holdSeconds > 0) {
            progression.setTotalHoldSeconds(
                    progression.getTotalHoldSeconds() + holdSeconds
            );
        }

        // Update unique exercises count
        if (uniqueExercisesCount > 0) {
            progression.setUniqueExercisesTried(
                    progression.getUniqueExercisesTried() + uniqueExercisesCount
            );
        }

        // Update workout type counters
        if (workoutType != null) {
            switch (workoutType.toUpperCase()) {
                case "CARDIO":
                    progression.setCardioWorkoutsCompleted(
                            progression.getCardioWorkoutsCompleted() + 1
                    );
                    break;
                case "STRENGTH":
                    progression.setStrengthWorkoutsCompleted(
                            progression.getStrengthWorkoutsCompleted() + 1
                    );
                    break;
                case "ISOMETRIC":
                    progression.setIsometricWorkoutsCompleted(
                            progression.getIsometricWorkoutsCompleted() + 1
                    );
                    break;
            }
        }

        // Check if first day of month
        if (today.getDayOfMonth() == 1) {
            progression.setFirstOfMonthCount(
                    progression.getFirstOfMonthCount() + 1
            );
        }

        // Check if weekend (Saturday or Sunday)
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            progression.setWeekendWorkoutCount(
                    progression.getWeekendWorkoutCount() + 1
            );
        }
    }

    // ========== SEASON TRANSITION ==========

    /**
     * Reset seasonal stats for all users when new season starts.
     * Called by SeasonService when activating new season.
     *
     * @param newSeasonId        New season ID
     * @param newSeasonStartDate New season start date
     */
    @Transactional
    public void resetSeasonalStatsForAllUsers(Integer newSeasonId, LocalDate newSeasonStartDate) {
        List<UserProgression> allUsers = userProgressionRepository.findAll();

        for (UserProgression progression : allUsers) {
            progression.setCurrentSeasonId(newSeasonId);
            progression.setSeasonStartDate(newSeasonStartDate);
            progression.setSeasonalXp(0);
            progression.setSeasonalRank(Rank.NOVICE);
            progression.setSeasonalTier(3);

            // Weekly count resets
            progression.setWeeklyWorkoutCount(0);
            progression.setWeekStartDate(newSeasonStartDate);
        }

        userProgressionRepository.saveAll(allUsers);
        log.info("Reset seasonal stats for {} users (new season: {})",
                allUsers.size(), newSeasonId);
    }

    // ========== LEADERBOARD QUERIES ==========

    /**
     * Get top users by seasonal XP.
     *
     * @param seasonId Season ID
     * @param limit    Max results
     * @return List of top users
     */
    @Transactional(readOnly = true)
    public List<UserProgression> getSeasonalLeaderboard(Integer seasonId, int limit) {
        return userProgressionRepository.findTopBySeasonalXp(seasonId, limit);
    }

    /**
     * Get top users by lifetime XP.
     *
     * @param limit Max results
     * @return List of top users
     */
    @Transactional(readOnly = true)
    public List<UserProgression> getLifetimeLeaderboard(int limit) {
        return userProgressionRepository.findTopByLifetimeXp(limit);
    }

    /**
     * Get user's seasonal rank position.
     *
     * @param seasonId Season ID
     * @param userId   User ID
     * @return Position (1 = first place)
     */
    @Transactional(readOnly = true)
    public Long getUserSeasonalRankPosition(Integer seasonId, Long userId) {
        return userProgressionRepository.findSeasonalRankPosition(seasonId, userId);
    }

    /**
     * Get user's lifetime rank position.
     *
     * @param userId User ID
     * @return Position (1 = first place)
     */
    @Transactional(readOnly = true)
    public Long getUserLifetimeRankPosition(Long userId) {
        return userProgressionRepository.findLifetimeRankPosition(userId);
    }
}