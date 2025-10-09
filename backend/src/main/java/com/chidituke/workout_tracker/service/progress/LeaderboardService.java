package com.chidituke.workout_tracker.service.progress;

import com.chidituke.workout_tracker.model.progress.*;
import com.chidituke.workout_tracker.repository.progress.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for managing leaderboards and season history.
 * <p>
 * Responsibilities:
 * 1. Create leaderboard snapshots (weekly/monthly/end of season)
 * 2. Archive seasons when they end
 * 3. Calculate percentile rankings
 * 4. Track rank changes over time
 * 5. Provide historical leaderboard data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LeaderboardService {

    private final LeaderboardEntryRepository leaderboardEntryRepository;
    private final SeasonHistoryRepository seasonHistoryRepository;
    private final UserProgressionRepository userProgressionRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final SeasonRepository seasonRepository;

    // ========== LEADERBOARD SNAPSHOTS ==========

    /**
     * Create a leaderboard snapshot for the current active season.
     * This should be run weekly or monthly to track progression.
     *
     * @param seasonId Season ID to snapshot
     * @return Count of leaderboard entries created
     */
    @Transactional
    public int createLeaderboardSnapshot(Integer seasonId) {
        LocalDate today = LocalDate.now();

        // Check if snapshot already exists for today
        if (leaderboardEntryRepository.existsBySeasonIdAndSnapshotDate(seasonId, today)) {
            log.warn("Leaderboard snapshot already exists for season {} on {}", seasonId, today);
            return 0;
        }

        // Get all users in this season ordered by seasonal XP
        List<UserProgression> users = userProgressionRepository.findByCurrentSeasonId(seasonId);
        users.sort((a, b) -> b.getSeasonalXp().compareTo(a.getSeasonalXp()));

        if (users.isEmpty()) {
            log.warn("No users found in season {}", seasonId);
            return 0;
        }

        // Calculate total users for percentile calculation
        int totalUsers = users.size();

        List<LeaderboardEntry> entries = new ArrayList<>();

        for (int i = 0; i < users.size(); i++) {
            UserProgression user = users.get(i);
            int rankPosition = i + 1;

            // Calculate percentile (higher is better)
            double percentile = calculatePercentile(rankPosition, totalUsers);

            // Get user's achievement count
            Long achievementCount = userAchievementRepository.countByUserId(user.getUserId());

            // Create leaderboard entry
            LeaderboardEntry entry = LeaderboardEntry.fromUserProgression(
                    user,
                    seasonId,
                    today,
                    rankPosition,
                    achievementCount.intValue(),
                    percentile
            );

            // Calculate rank change if previous snapshot exists
            calculateRankChange(entry, user.getUserId(), seasonId);

            entries.add(entry);
        }

        leaderboardEntryRepository.saveAll(entries);
        log.info("Created leaderboard snapshot for season {} with {} entries", seasonId, entries.size());

        return entries.size();
    }

    /**
     * Calculate percentile ranking.
     * Formula: ((totalUsers - rank + 1) / totalUsers) * 100
     *
     * @param rankPosition User's rank position (1-based)
     * @param totalUsers   Total number of users
     * @return Percentile (0-100, where 100 = top 1%)
     */
    private double calculatePercentile(int rankPosition, int totalUsers) {
        if (totalUsers == 0) return 0.0;
        double percentile = ((totalUsers - rankPosition + 1.0) / totalUsers) * 100.0;
        return Math.round(percentile * 100.0) / 100.0; // Round to 2 decimal places
    }

    /**
     * Calculate rank change since last snapshot.
     *
     * @param currentEntry Current leaderboard entry
     * @param userId       User ID
     * @param seasonId     Season ID
     */
    private void calculateRankChange(LeaderboardEntry currentEntry, Long userId, Integer seasonId) {
        List<LeaderboardEntry> history = leaderboardEntryRepository
                .findByUserIdAndSeasonIdOrderBySnapshotDateDesc(userId, seasonId);

        if (history.size() > 1) {
            // Compare to previous snapshot (second in list since it's DESC order)
            LeaderboardEntry previous = history.get(1);
            int rankChange = previous.getRankPosition() - currentEntry.getRankPosition();
            currentEntry.setRankChange(rankChange);
        } else {
            currentEntry.setRankChange(0); // First snapshot, no change
        }
    }

    /**
     * Get current leaderboard (most recent snapshot).
     *
     * @param seasonId Season ID
     * @param limit    Max results (0 for all)
     * @return List of leaderboard entries
     */
    @Transactional(readOnly = true)
    public List<LeaderboardEntry> getCurrentLeaderboard(Integer seasonId, int limit) {
        if (limit > 0) {
            return leaderboardEntryRepository.findTopNFromRecentSnapshot(seasonId, limit);
        }
        return leaderboardEntryRepository.findMostRecentForSeason(seasonId);
    }

    /**
     * Get user's rank history for a season.
     * Shows how user's position changed over time.
     *
     * @param userId   User ID
     * @param seasonId Season ID
     * @return List of user's leaderboard entries over time
     */
    @Transactional(readOnly = true)
    public List<LeaderboardEntry> getUserRankHistory(Long userId, Integer seasonId) {
        return leaderboardEntryRepository.findByUserIdAndSeasonIdOrderBySnapshotDateDesc(userId, seasonId);
    }

    /**
     * Get all snapshot dates for a season.
     *
     * @param seasonId Season ID
     * @return List of dates when snapshots were taken
     */
    @Transactional(readOnly = true)
    public List<LocalDate> getSnapshotDates(Integer seasonId) {
        return leaderboardEntryRepository.findSnapshotDatesForSeason(seasonId);
    }

    /**
     * Get leaderboard for a specific date.
     *
     * @param seasonId Season ID
     * @param date     Snapshot date
     * @return List of leaderboard entries for that date
     */
    @Transactional(readOnly = true)
    public List<LeaderboardEntry> getLeaderboardForDate(Integer seasonId, LocalDate date) {
        return leaderboardEntryRepository.findBySeasonIdAndSnapshotDateOrderByRankPositionAsc(seasonId, date);
    }

    // ========== SEASON ARCHIVING ==========

    /**
     * Archive a season when it ends.
     * Creates SeasonHistory records for all participants.
     * Should be called when transitioning to a new season.
     * <p>
     * Data Flow:
     * 1. Get all users in season
     * 2. Calculate final percentiles
     * 3. Create season history for each user
     * 4. Create final leaderboard snapshot
     *
     * @param seasonId Season ID to archive
     * @return Count of season history records created
     */
    @Transactional
    public int archiveSeason(Integer seasonId) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new IllegalArgumentException("Season not found: " + seasonId));

        if (season.getIsActive()) {
            log.warn("Cannot archive active season: {}", seasonId);
            return 0;
        }

        // Create final leaderboard snapshot if not exists
        LocalDate endDate = season.getEndDate();
        if (!leaderboardEntryRepository.existsBySeasonIdAndSnapshotDate(seasonId, endDate)) {
            createLeaderboardSnapshot(seasonId);
        }

        // Get all users in season
        List<UserProgression> users = userProgressionRepository.findByCurrentSeasonId(seasonId);
        users.sort((a, b) -> b.getSeasonalXp().compareTo(a.getSeasonalXp()));

        int totalUsers = users.size();
        List<SeasonHistory> histories = new ArrayList<>();

        for (int i = 0; i < users.size(); i++) {
            UserProgression user = users.get(i);
            int rankPosition = i + 1;

            // Skip if history already exists
            if (seasonHistoryRepository.existsByUserIdAndSeasonId(user.getUserId(), seasonId)) {
                log.debug("Season history already exists for user {} in season {}",
                        user.getUserId(), seasonId);
                continue;
            }

            // Calculate percentile
            double percentile = calculatePercentile(rankPosition, totalUsers);
            BigDecimal percentileBd = BigDecimal.valueOf(percentile)
                    .setScale(2, RoundingMode.HALF_UP);

            // Calculate season-specific stats
            Integer workoutsThisSeason = user.getTotalWorkoutsCompleted(); // TODO: Track season-specific count
            Integer perfectWeeks = calculatePerfectWeeks(user.getUserId(), season);

            // Create season history
            SeasonHistory history = SeasonHistory.fromUserProgression(
                    user,
                    seasonId,
                    percentileBd,
                    workoutsThisSeason,
                    perfectWeeks
            );

            histories.add(history);
        }

        seasonHistoryRepository.saveAll(histories);
        log.info("Archived season {} with {} history records", seasonId, histories.size());

        return histories.size();
    }

    /**
     * Calculate number of perfect weeks (7/7 workouts) for user in season.
     * This is a simplified calculation - would need workout session dates for accuracy.
     *
     * @param userId User ID
     * @param season Season
     * @return Number of perfect weeks
     */
    private Integer calculatePerfectWeeks(Long userId, Season season) {
        // TODO: Query workout_sessions table to count perfect weeks
        // For now, return 0 as placeholder
        return 0;
    }

    // ========== SEASON HISTORY QUERIES ==========

    /**
     * Get user's season history.
     *
     * @param userId User ID
     * @return List of user's past seasons
     */
    @Transactional(readOnly = true)
    public List<SeasonHistory> getUserSeasonHistory(Long userId) {
        return seasonHistoryRepository.findByUserIdOrderByCompletedAtDesc(userId);
    }

    /**
     * Get user's history for a specific season.
     *
     * @param userId   User ID
     * @param seasonId Season ID
     * @return Optional containing season history if found
     */
    @Transactional(readOnly = true)
    public Optional<SeasonHistory> getUserSeasonHistoryForSeason(Long userId, Integer seasonId) {
        return seasonHistoryRepository.findByUserIdAndSeasonId(userId, seasonId);
    }

    /**
     * Get top performers for a season.
     *
     * @param seasonId Season ID
     * @param limit    Max results
     * @return List of top season histories
     */
    @Transactional(readOnly = true)
    public List<SeasonHistory> getTopPerformersForSeason(Integer seasonId, int limit) {
        return seasonHistoryRepository.findTopPerformersForSeason(seasonId, limit);
    }

    /**
     * Get all history for a season.
     *
     * @param seasonId Season ID
     * @return List of all users' history for that season
     */
    @Transactional(readOnly = true)
    public List<SeasonHistory> getSeasonHistory(Integer seasonId) {
        return seasonHistoryRepository.findBySeasonIdOrderByFinalSeasonalXpDesc(seasonId);
    }

    /**
     * Get user's best season.
     *
     * @param userId User ID
     * @return Optional containing user's best season
     */
    @Transactional(readOnly = true)
    public Optional<SeasonHistory> getUserBestSeason(Long userId) {
        return seasonHistoryRepository.findUserBestSeason(userId);
    }

    /**
     * Get user's season progression over time.
     *
     * @param userId User ID
     * @return List of seasons in chronological order
     */
    @Transactional(readOnly = true)
    public List<SeasonHistory> getUserSeasonProgression(Long userId) {
        return seasonHistoryRepository.findUserSeasonProgression(userId);
    }

    /**
     * Get season statistics.
     *
     * @param seasonId Season ID
     * @return Season statistics
     */
    @Transactional(readOnly = true)
    public SeasonStats getSeasonStats(Integer seasonId) {
        Long participantCount = seasonHistoryRepository.countBySeasonId(seasonId);
        Double averageXp = seasonHistoryRepository.getAverageXpForSeason(seasonId);

        List<SeasonHistory> topPerformers = seasonHistoryRepository
                .findTopPerformersForSeason(seasonId, 3);

        return new SeasonStats(
                seasonId,
                participantCount,
                averageXp != null ? averageXp : 0.0,
                topPerformers
        );
    }

    // ========== HELPER CLASSES ==========

    /**
     * DTO for season statistics.
     */
    public record SeasonStats(
            Integer seasonId,
            Long totalParticipants,
            Double averageXp,
            List<SeasonHistory> topPerformers
    ) {
    }
}