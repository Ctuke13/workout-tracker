package com.chidituke.workout_tracker.controller.progress;

import com.chidituke.workout_tracker.dto.request.progress.*;
import com.chidituke.workout_tracker.dto.response.progress.*;
import com.chidituke.workout_tracker.model.progress.*;
import com.chidituke.workout_tracker.model.progress.enums.AchievementCategory;
import com.chidituke.workout_tracker.model.progress.enums.Rarity;
import com.chidituke.workout_tracker.model.progress.enums.Rank;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.security.CurrentUser;
import com.chidituke.workout_tracker.security.UserPrincipal;
import com.chidituke.workout_tracker.service.progress.*;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for all progression/gamification endpoints.
 * Base path: /api/progress
 * <p>
 * Endpoints organized by feature:
 * - Seasons: /api/progress/seasons/*
 * - User Progression: /api/progress/me/*
 * - Achievements: /api/progress/achievements/*
 * - Leaderboards: /api/progress/leaderboard/*
 */
@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@Slf4j
public class ProgressController {

    private final SeasonService seasonService;
    private final UserProgressionService userProgressionService;
    private final AchievementService achievementService;
    private final LeaderboardService leaderboardService;
    private final SeasonTransitionService seasonTransitionService;
    private final UserRepository userRepository;

    // ========== HELPER METHOD ==========

    /**
     * Extract user ID from UserPrincipal.
     * Uses the same authentication pattern as UserController.
     */
    private Long getUserIdFromAuth(UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new IllegalStateException("User must be authenticated");
        }
        return currentUser.getId();
    }

    // ========================================================================
    // SEASONS ENDPOINTS
    // ========================================================================

    /**
     * Get the currently active season.
     * <p>
     * GET /api/progress/seasons/current
     */
    @GetMapping("/seasons/current")
    public ResponseEntity<SeasonDTO> getCurrentSeason() {
        Season season = seasonService.getActiveSeason();
        return ResponseEntity.ok(SeasonDTO.fromEntity(season));
    }

    /**
     * Get all seasons.
     * <p>
     * GET /api/progress/seasons
     */
    @GetMapping("/seasons")
    public ResponseEntity<List<SeasonDTO>> getAllSeasons() {
        List<Season> seasons = seasonService.getAllSeasons();
        List<SeasonDTO> dtos = seasons.stream()
                .map(SeasonDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get upcoming seasons.
     * <p>
     * GET /api/progress/seasons/upcoming
     */
    @GetMapping("/seasons/upcoming")
    public ResponseEntity<List<SeasonDTO>> getUpcomingSeasons() {
        List<Season> seasons = seasonService.getUpcomingSeasons();
        List<SeasonDTO> dtos = seasons.stream()
                .map(SeasonDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get past seasons.
     * <p>
     * GET /api/progress/seasons/past
     */
    @GetMapping("/seasons/past")
    public ResponseEntity<List<SeasonDTO>> getPastSeasons() {
        List<Season> seasons = seasonService.getPastSeasons();
        List<SeasonDTO> dtos = seasons.stream()
                .map(SeasonDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Force an immediate season transition (ADMIN ONLY).
     * <p>
     * ⚠️ WARNING: This will immediately transition to the next season
     * and reset all users' seasonal stats with the 3-tier drop system.
     * Use with caution - primarily for testing purposes.
     * <p>
     * POST /api/progress/seasons/force-transition
     *
     * @return Success message or error details
     */
    @PostMapping("/seasons/force-transition")
    @PreAuthorize("hasRole('ADMIN')")  // 🆕 ADMIN ONLY
    public ResponseEntity<SeasonTransitionDTO> forceSeasonTransition() {
        try {
            log.warn("🚨 Admin manually triggered season transition!");

            // Get current season before transition
            Season currentSeason = seasonService.getActiveSeason();
            String oldSeasonName = currentSeason.getSeasonName();

            // Force the transition
            seasonTransitionService.forceSeasonTransition();

            // Get new active season after transition
            Season newSeason = seasonService.getActiveSeason();
            String newSeasonName = newSeason.getSeasonName();

            log.info("✅ Manual season transition complete: {} → {}",
                    oldSeasonName, newSeasonName);

            return ResponseEntity.ok(SeasonTransitionDTO.builder()
                    .success(true)
                    .message("Season transition completed successfully")
                    .previousSeason(oldSeasonName)
                    .newSeason(newSeasonName)
                    .transitionDate(LocalDate.now())
                    .build());

        } catch (Exception e) {
            log.error("❌ Failed to force season transition", e);
            return ResponseEntity.internalServerError()
                    .body(SeasonTransitionDTO.builder()
                            .success(false)
                            .message("Season transition failed: " + e.getMessage())
                            .build());
        }
    }

    // ========================================================================
    // USER PROGRESSION ENDPOINTS
    // ========================================================================

    /**
     * Get current user's progression.
     * <p>
     * GET /api/progress/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserProgressionDTO> getMyProgression(
            @CurrentUser UserPrincipal currentUser) {

        Long userId = getUserIdFromAuth(currentUser);
        UserProgression progression = userProgressionService.getOrCreateUserProgression(userId);

        return ResponseEntity.ok(UserProgressionDTO.fromEntity(progression));
    }

    /**
     * Get current user's rank information.
     * <p>
     * GET /api/progress/me/rank
     */
    @GetMapping("/me/rank")
    public ResponseEntity<RankInfoDTO> getMyRank(
            @CurrentUser UserPrincipal currentUser) {

        Long userId = getUserIdFromAuth(currentUser);
        UserProgression progression = userProgressionService.getOrCreateUserProgression(userId);

        Season activeSeason = seasonService.getActiveSeason();
        Long seasonalPosition = userProgressionService.getUserSeasonalRankPosition(
                activeSeason.getSeasonId(), userId);
        Long lifetimePosition = userProgressionService.getUserLifetimeRankPosition(userId);

        return ResponseEntity.ok(RankInfoDTO.fromProgression(
                progression, seasonalPosition, lifetimePosition));
    }

    /**
     * Get current user's streak information.
     * <p>
     * GET /api/progress/me/streak
     */
    @GetMapping("/me/streak")
    public ResponseEntity<StreakInfoDTO> getMyStreak(
            @CurrentUser UserPrincipal currentUser) {

        Long userId = getUserIdFromAuth(currentUser);
        UserProgression progression = userProgressionService.getOrCreateUserProgression(userId);

        return ResponseEntity.ok(StreakInfoDTO.fromProgression(progression));
    }

    /**
     * Get current user's statistics.
     * <p>
     * GET /api/progress/me/stats
     */
    @GetMapping("/me/stats")
    public ResponseEntity<UserStatsDTO> getMyStats(
            @CurrentUser UserPrincipal currentUser) {

        Long userId = getUserIdFromAuth(currentUser);
        UserProgression progression = userProgressionService.getOrCreateUserProgression(userId);

        return ResponseEntity.ok(UserStatsDTO.fromProgression(progression));
    }

    /**
     * Process workout completion and award XP.
     * This is the MAIN endpoint called after every workout.
     * <p>
     * POST /api/progress/workout-completion
     */
    @PostMapping("/workout-completion")
    public ResponseEntity<ProgressionUpdateResponse> processWorkoutCompletion(
            @RequestBody ProgressionUpdateRequest request,
            @CurrentUser UserPrincipal currentUser) {

        Long userId = getUserIdFromAuth(currentUser);

        log.info("Processing workout completion for user {}", userId);

        // 🆕 STEP 1: Get CURRENT progression and save OLD rank
        UserProgression progressionBefore = userProgressionService.getOrCreateUserProgression(userId);
        Rank oldSeasonalRank = progressionBefore.getSeasonalRank();
        int oldSeasonalTier = progressionBefore.getSeasonalTier();
        int oldSeasonalXp = progressionBefore.getSeasonalXp();

        // 🆕 STEP 2: Update user progression (this awards XP and updates rank)
        UserProgression progression = userProgressionService.handleWorkoutCompletion(
                userId,
                request.getDurationMinutes(),
                request.getSetsCompleted(),
                request.getVolumeLifted(),
                request.getDistanceKm(),
                request.getHoldSeconds(),
                request.getUniqueExercisesCount(),
                request.getWorkoutType()
        );

        // 🆕 STEP 3: Calculate XP gained and check if ranked up
        int xpGained = progression.getSeasonalXp() - oldSeasonalXp;
        boolean rankedUp = !progression.getSeasonalRank().equals(oldSeasonalRank);
        boolean tieredUp = !rankedUp && (progression.getSeasonalTier() < oldSeasonalTier);

        if (rankedUp) {
            log.info("🎊 USER RANKED UP! {} → {} (+{} XP)",
                    oldSeasonalRank, progression.getSeasonalRank(), xpGained);
        } else if (tieredUp) {  // 🆕 ADD THIS
            log.info("📈 USER TIERED UP! {} {} → {} {}",
                    oldSeasonalRank, oldSeasonalTier, progression.getSeasonalRank(), progression.getSeasonalTier());
        }

        // STEP 4: Check for newly unlocked achievements
        List<UserAchievement> newAchievements = achievementService.checkAndUnlockAchievements(userId);

        // STEP 5: Build response
        ProgressionUpdateResponse response = ProgressionUpdateResponse.builder()
                .xpGained(xpGained)
                .newSeasonalXp(progression.getSeasonalXp())
                .newLifetimeXp(progression.getLifetimeXp())
                .seasonalRank(progression.getSeasonalRank().name())
                .lifetimeRank(progression.getLifetimeRank().name())
                .currentStreak(progression.getCurrentStreakDays())
                .rankedUp(rankedUp)
                .tieredUp(tieredUp)
                .oldRank(rankedUp ? oldSeasonalRank.name() : null)
                .oldTier(rankedUp || tieredUp ? oldSeasonalTier : null)
                .newSeasonalTier(progression.getSeasonalTier())
                .streakMilestone(checkStreakMilestone(progression))
                .achievementsUnlocked(newAchievements.stream()
                        .map(UserAchievementDTO::fromEntity)
                        .collect(Collectors.toList()))
                .build();

        log.info("Workout completion processed: +{} XP, {} achievements unlocked, ranked up: {}",
                response.getXpGained(), response.getAchievementsUnlocked().size(), rankedUp);

        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // ACHIEVEMENTS ENDPOINTS
    // ========================================================================

    /**
     * Get all visible achievements.
     * <p>
     * GET /api/progress/achievements
     */
    @GetMapping("/achievements")
    public ResponseEntity<List<AchievementDTO>> getAllAchievements() {
        List<Achievement> achievements = achievementService.getVisibleAchievements();
        List<AchievementDTO> dtos = achievements.stream()
                .map(AchievementDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get achievements by category.
     * <p>
     * GET /api/progress/achievements/category/{category}
     */
    @GetMapping("/achievements/category/{category}")
    public ResponseEntity<List<AchievementDTO>> getAchievementsByCategory(
            @PathVariable AchievementCategory category) {

        List<Achievement> achievements = achievementService.getAchievementsByCategory(category);
        List<AchievementDTO> dtos = achievements.stream()
                .map(AchievementDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get current user's unlocked achievements.
     * <p>
     * GET /api/progress/achievements/unlocked
     */
    @GetMapping("/achievements/unlocked")
    public ResponseEntity<List<UserAchievementDTO>> getMyAchievements(
            @CurrentUser UserPrincipal currentUser) {

        Long userId = getUserIdFromAuth(currentUser);
        List<UserAchievement> achievements = achievementService.getUserAchievements(userId);
        List<UserAchievementDTO> dtos = achievements.stream()
                .map(UserAchievementDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get current user's achievement statistics.
     * <p>
     * GET /api/progress/achievements/stats
     */
    @GetMapping("/achievements/stats")
    public ResponseEntity<AchievementStatsDTO> getMyAchievementStats(
            @CurrentUser UserPrincipal currentUser) {

        Long userId = getUserIdFromAuth(currentUser);
        AchievementService.AchievementStats stats = achievementService.getAchievementStats(userId);

        return ResponseEntity.ok(AchievementStatsDTO.fromStats(stats));
    }

    /**
     * Get current user's progress toward a specific achievement.
     * <p>
     * GET /api/progress/achievements/{achievementId}/progress
     */
    @GetMapping("/achievements/{achievementId}/progress")
    public ResponseEntity<AchievementProgressDTO> getAchievementProgress(
            @PathVariable Integer achievementId,
            @CurrentUser UserPrincipal currentUser) {

        Long userId = getUserIdFromAuth(currentUser);
        AchievementService.AchievementProgress progress =
                achievementService.getAchievementProgress(userId, achievementId);

        return ResponseEntity.ok(AchievementProgressDTO.fromProgress(progress));
    }

    /**
     * Get current user's progress for all achievements in a category.
     * <p>
     * GET /api/progress/achievements/category/{category}/progress
     */
    @GetMapping("/achievements/category/{category}/progress")
    public ResponseEntity<List<AchievementProgressDTO>> getCategoryProgress(
            @PathVariable AchievementCategory category,
            @CurrentUser UserPrincipal currentUser) {

        Long userId = getUserIdFromAuth(currentUser);
        List<AchievementService.AchievementProgress> progress =
                achievementService.getCategoryProgress(userId, category);

        List<AchievementProgressDTO> dtos = progress.stream()
                .map(AchievementProgressDTO::fromProgress)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get recently unlocked achievements (last 24 hours).
     * <p>
     * GET /api/progress/achievements/recent
     */
    @GetMapping("/achievements/recent")
    public ResponseEntity<List<UserAchievementDTO>> getRecentAchievements(
            @CurrentUser UserPrincipal currentUser) {

        Long userId = getUserIdFromAuth(currentUser);
        List<UserAchievement> achievements = achievementService.getRecentlyUnlockedAchievements(userId);
        List<UserAchievementDTO> dtos = achievements.stream()
                .map(UserAchievementDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // ========================================================================
    // LEADERBOARD ENDPOINTS
    // ========================================================================

    /**
     * Get seasonal leaderboard (REAL-TIME from user_progression).
     * This is the primary endpoint for current season rankings.
     * Always shows up-to-date positions.
     * <p>
     * GET /api/progress/leaderboard/seasonal
     * Query params: limit (default 100)
     */
    @GetMapping("/leaderboard/seasonal")
    public ResponseEntity<List<LeaderboardEntryDTO>> getSeasonalLeaderboard(
            @RequestParam(defaultValue = "100") int limit) {

        Season activeSeason = seasonService.getActiveSeason();

        // Get top users by seasonal XP
        List<UserProgression> progressions = userProgressionService
                .getSeasonalLeaderboard(activeSeason.getSeasonId(), limit);

        if (progressions.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        // Get total users for percentile calculation
        long totalUsers = userProgressionService.getSeasonUserCount(activeSeason.getSeasonId());

        // Get usernames for all users in bulk
        List<Long> userIds = progressions.stream()
                .map(UserProgression::getUserId)
                .collect(Collectors.toList());

        Map<Long, String> usernameMap = getUsernameMap(userIds);

        // Build DTOs with positions and percentiles
        List<LeaderboardEntryDTO> dtos = new ArrayList<>();
        for (int i = 0; i < progressions.size(); i++) {
            UserProgression progression = progressions.get(i);
            int position = i + 1;

            // Calculate percentile: ((totalUsers - position + 1) / totalUsers) * 100
            double percentile = totalUsers > 0
                    ? Math.round(((totalUsers - position + 1.0) / totalUsers) * 10000.0) / 100.0
                    : 0.0;

            String username = usernameMap.getOrDefault(
                    progression.getUserId(),
                    "User" + progression.getUserId()
            );

            LeaderboardEntryDTO dto = LeaderboardEntryDTO.fromUserProgression(
                    progression, username, position, percentile
            );

            dtos.add(dto);
        }

        log.debug("Returned {} leaderboard entries for season {}",
                dtos.size(), activeSeason.getSeasonId());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get seasonal leaderboard snapshot (HISTORICAL from leaderboard_entries).
     * Shows archived rankings with rank change tracking.
     * <p>
     * GET /api/progress/leaderboard/seasonal/snapshot
     * Query params: limit (default 100)
     */
    @GetMapping("/leaderboard/seasonal/snapshot")
    public ResponseEntity<List<LeaderboardEntryDTO>> getSeasonalLeaderboardSnapshot(
            @RequestParam(defaultValue = "100") int limit) {

        Season activeSeason = seasonService.getActiveSeason();
        List<LeaderboardEntry> entries = leaderboardService.getCurrentLeaderboard(
                activeSeason.getSeasonId(), limit);

        List<LeaderboardEntryDTO> dtos = entries.stream()
                .map(LeaderboardEntryDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Manually create a leaderboard snapshot (ADMIN ONLY).
     * Useful for testing and manual updates.
     * <p>
     * POST /api/progress/leaderboard/snapshot
     */
    @PostMapping("/leaderboard/snapshot")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createLeaderboardSnapshot() {
        try {
            Season activeSeason = seasonService.getActiveSeason();
            int count = leaderboardService.createLeaderboardSnapshot(activeSeason.getSeasonId());

            log.info("✅ Leaderboard snapshot created: {} entries", count);

            return ResponseEntity.ok(
                    String.format("Leaderboard snapshot created successfully with %d entries", count)
            );
        } catch (Exception e) {
            log.error("❌ Failed to create leaderboard snapshot", e);
            return ResponseEntity.internalServerError()
                    .body("Failed to create snapshot: " + e.getMessage());
        }
    }

    /**
     * Helper method to get usernames in bulk.
     */
    private Map<Long, String> getUsernameMap(List<Long> userIds) {
        // This assumes you have a UserRepository injected
        // Add this field at the top of the class if not present:
        // private final UserRepository userRepository;

        try {
            List<Object[]> results = userRepository.findUsernamesByUserIds(userIds);
            return results.stream()
                    .collect(Collectors.toMap(
                            row -> (Long) row[0],
                            row -> (String) row[1]
                    ));
        } catch (Exception e) {
            log.warn("Failed to fetch usernames, using fallback", e);
            return userIds.stream()
                    .collect(Collectors.toMap(
                            id -> id,
                            id -> "User" + id
                    ));
        }
    }

    /**
     * Get lifetime leaderboard (all-time).
     * <p>
     * GET /api/progress/leaderboard/lifetime
     * Query params: limit (default 100)
     */
    @GetMapping("/leaderboard/lifetime")
    public ResponseEntity<List<UserProgressionDTO>> getLifetimeLeaderboard(
            @RequestParam(defaultValue = "100") int limit) {

        List<UserProgression> progressions = userProgressionService.getLifetimeLeaderboard(limit);
        List<UserProgressionDTO> dtos = progressions.stream()
                .map(UserProgressionDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get current user's rank history for current season.
     * <p>
     * GET /api/progress/leaderboard/me/history
     */
    @GetMapping("/leaderboard/me/history")
    public ResponseEntity<List<LeaderboardEntryDTO>> getMyRankHistory(
            @CurrentUser UserPrincipal currentUser) {

        Long userId = getUserIdFromAuth(currentUser);
        Season activeSeason = seasonService.getActiveSeason();

        List<LeaderboardEntry> history = leaderboardService.getUserRankHistory(
                userId, activeSeason.getSeasonId());

        List<LeaderboardEntryDTO> dtos = history.stream()
                .map(LeaderboardEntryDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get leaderboard for a specific date.
     * <p>
     * GET /api/progress/leaderboard/date/{date}
     */
    @GetMapping("/leaderboard/date/{date}")
    public ResponseEntity<List<LeaderboardEntryDTO>> getLeaderboardForDate(
            @PathVariable LocalDate date) {

        Season activeSeason = seasonService.getActiveSeason();
        List<LeaderboardEntry> entries = leaderboardService.getLeaderboardForDate(
                activeSeason.getSeasonId(), date);

        List<LeaderboardEntryDTO> dtos = entries.stream()
                .map(LeaderboardEntryDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // ========================================================================
    // SEASON HISTORY ENDPOINTS
    // ========================================================================

    /**
     * Get current user's season history.
     * <p>
     * GET /api/progress/history
     */
    @GetMapping("/history")
    public ResponseEntity<List<SeasonHistoryDTO>> getMySeasonHistory(
            @CurrentUser UserPrincipal currentUser) {

        Long userId = getUserIdFromAuth(currentUser);
        List<SeasonHistory> history = leaderboardService.getUserSeasonHistory(userId);
        List<SeasonHistoryDTO> dtos = history.stream()
                .map(SeasonHistoryDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get current user's best season.
     * <p>
     * GET /api/progress/history/best
     */
    @GetMapping("/history/best")
    public ResponseEntity<SeasonHistoryDTO> getMyBestSeason(
            @CurrentUser UserPrincipal currentUser) {

        Long userId = getUserIdFromAuth(currentUser);
        return leaderboardService.getUserBestSeason(userId)
                .map(SeasonHistoryDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get top performers for a specific season.
     * <p>
     * GET /api/progress/history/season/{seasonId}/top
     */
    @GetMapping("/history/season/{seasonId}/top")
    public ResponseEntity<List<SeasonHistoryDTO>> getTopPerformersForSeason(
            @PathVariable Integer seasonId,
            @RequestParam(defaultValue = "10") int limit) {

        List<SeasonHistory> topPerformers = leaderboardService.getTopPerformersForSeason(
                seasonId, limit);
        List<SeasonHistoryDTO> dtos = topPerformers.stream()
                .map(SeasonHistoryDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get statistics for a specific season.
     * <p>
     * GET /api/progress/history/season/{seasonId}/stats
     */
    @GetMapping("/history/season/{seasonId}/stats")
    public ResponseEntity<SeasonStatsDTO> getSeasonStats(
            @PathVariable Integer seasonId) {

        LeaderboardService.SeasonStats stats = leaderboardService.getSeasonStats(seasonId);
        return ResponseEntity.ok(SeasonStatsDTO.fromStats(stats));
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================


    /**
     * Check if user hit a streak milestone (3, 7, 14, 30 days).
     */
    private boolean checkStreakMilestone(UserProgression progression) {
        int streak = progression.getCurrentStreakDays();
        return streak == 3 || streak == 7 || streak == 14 || streak == 30 ||
                streak == 60 || streak == 100 || streak == 365;
    }
}