-- ============================================================
-- V017: Add Leaderboard Performance Indexes
-- ============================================================
-- Purpose: Optimize leaderboard queries for better performance
-- Author: System
-- Date: 2025-10-14

-- Index for seasonal leaderboard queries (real-time)
-- Speeds up: ORDER BY seasonal_xp DESC queries
CREATE INDEX IF NOT EXISTS idx_user_progression_seasonal_leaderboard
ON user_progression(current_season_id, seasonal_xp DESC, user_id ASC);

-- Index for lifetime leaderboard queries
-- Speeds up: ORDER BY lifetime_xp DESC queries
CREATE INDEX IF NOT EXISTS idx_user_progression_lifetime_leaderboard
ON user_progression(lifetime_xp DESC, user_id ASC);

-- Index for user lookup by season
-- Speeds up: WHERE current_season_id = ? queries
CREATE INDEX IF NOT EXISTS idx_user_progression_season_lookup
ON user_progression(current_season_id);

-- Index for leaderboard snapshot queries
-- Speeds up: Historical leaderboard lookups
CREATE INDEX IF NOT EXISTS idx_leaderboard_entries_season_date
ON leaderboard_entries(season_id, snapshot_date DESC, rank_position ASC);

-- Index for user rank history
-- Speeds up: User's historical rank tracking
CREATE INDEX IF NOT EXISTS idx_leaderboard_entries_user_history
ON leaderboard_entries(user_id, season_id, snapshot_date DESC);

-- Analyze tables to update statistics
ANALYZE user_progression;
ANALYZE leaderboard_entries;