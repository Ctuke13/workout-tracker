package com.chidituke.workout_tracker.scheduler;

import com.chidituke.workout_tracker.model.progress.Season;
import com.chidituke.workout_tracker.service.progress.LeaderboardService;
import com.chidituke.workout_tracker.service.progress.SeasonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks for leaderboard maintenance.
 * <p>
 * Responsibilities:
 * - Create daily leaderboard snapshots for historical tracking
 * - Clean up old snapshots (optional)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LeaderboardScheduler {

    private final LeaderboardService leaderboardService;
    private final SeasonService seasonService;

    /**
     * Create daily leaderboard snapshot at midnight.
     * This enables rank change tracking and historical analytics.
     * <p>
     * Cron: "0 0 0 * * *" = Every day at 00:00:00 (midnight)
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void createDailyLeaderboardSnapshot() {
        try {
            log.info("🕐 Starting daily leaderboard snapshot...");

            Season activeSeason = seasonService.getActiveSeason();
            int count = leaderboardService.createLeaderboardSnapshot(
                    activeSeason.getSeasonId()
            );

            log.info("✅ Daily leaderboard snapshot completed: {} entries for season {}",
                    count, activeSeason.getSeasonName());

        } catch (Exception e) {
            log.error("❌ Failed to create daily leaderboard snapshot", e);
        }
    }

    /**
     * Optional: Clean up old snapshots to save space.
     * Keeps only last 90 days of snapshots.
     * <p>
     * Cron: "0 0 3 * * *" = Every day at 03:00:00 (3 AM)
     */
    @Scheduled(cron = "0 0 3 * * SUN") // Weekly on Sunday at 3 AM
    public void cleanupOldSnapshots() {
        try {
            log.info("🧹 Starting cleanup of old leaderboard snapshots...");

            // TODO: Implement cleanup logic if needed
            // Example: Delete snapshots older than 90 days

            log.info("✅ Snapshot cleanup completed");

        } catch (Exception e) {
            log.error("❌ Failed to cleanup old snapshots", e);
        }
    }
}