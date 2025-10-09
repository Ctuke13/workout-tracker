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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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

        // 1. Update user progression
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

        // 2. Check for newly unlocked achievements
        List<UserAchievement> newAchievements = achievementService.checkAndUnlockAchievements(userId);

        // 3. Build response
        ProgressionUpdateResponse response = ProgressionUpdateResponse.builder()
                .xpGained(calculateXpGained(progression)) // XP from this workout
                .newSeasonalXp(progression.getSeasonalXp())
                .newLifetimeXp(progression.getLifetimeXp())
                .seasonalRank(progression.getSeasonalRank().name())
                .lifetimeRank(progression.getLifetimeRank().name())
                .currentStreak(progression.getCurrentStreakDays())
                .rankedUp(checkIfRankedUp(progression))
                .streakMilestone(checkStreakMilestone(progression))
                .achievementsUnlocked(newAchievements.stream()
                        .map(UserAchievementDTO::fromEntity)
                        .collect(Collectors.toList()))
                .build();

        log.info("Workout completion processed: +{} XP, {} achievements unlocked",
                response.getXpGained(), response.getAchievementsUnlocked().size());

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
     * Get seasonal leaderboard (current season).
     * <p>
     * GET /api/progress/leaderboard/seasonal
     * Query params: limit (default 100)
     */
    @GetMapping("/leaderboard/seasonal")
    public ResponseEntity<List<LeaderboardEntryDTO>> getSeasonalLeaderboard(
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
     * Calculate XP gained from this specific workout.
     * This is a simplified calculation - actual implementation would track previous XP.
     */
    private int calculateXpGained(UserProgression progression) {
        // TODO: Track XP before/after workout to calculate exact gain
        // For now, return base XP estimate
        return 10; // Placeholder
    }

    /**
     * Check if user ranked up during this workout.
     */
    private boolean checkIfRankedUp(UserProgression progression) {
        // TODO: Track rank before/after workout
        // For now, return false
        return false; // Placeholder
    }

    /**
     * Check if user hit a streak milestone (3, 7, 14, 30 days).
     */
    private boolean checkStreakMilestone(UserProgression progression) {
        int streak = progression.getCurrentStreakDays();
        return streak == 3 || streak == 7 || streak == 14 || streak == 30 ||
                streak == 60 || streak == 100 || streak == 365;
    }
}