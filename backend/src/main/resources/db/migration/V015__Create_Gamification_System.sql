-- WORKOUT TRACKER - GAMIFICATION SYSTEM v1.0
-- Migration: V015__Create_Gamification_System.sql
-- Description: Creates seasons, user progression, and season history tables
-- Author: Workout Tracker Team
-- Date: 2025-01-08
-- Updated: 2025-10-08 - Added achievement tracking fields
-- TABLE 1: seasons
-- Purpose: Manages 3-month seasonal competitions and resets
CREATE TABLE seasons (
                         season_id SERIAL PRIMARY KEY,
                         season_name VARCHAR(50) NOT NULL,
                         season_type VARCHAR(20) NOT NULL,
                         start_date DATE NOT NULL,
                         end_date DATE NOT NULL,
                         is_active BOOLEAN NOT NULL DEFAULT FALSE,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         CONSTRAINT unique_season_dates UNIQUE(start_date, end_date),
                         CONSTRAINT valid_dates CHECK (end_date > start_date),
                         CONSTRAINT valid_season_type CHECK (season_type IN ('WINTER', 'SPRING', 'SUMMER', 'FALL'))
);
-- Indexes for performance
CREATE INDEX idx_seasons_active ON seasons(is_active);
CREATE INDEX idx_seasons_dates ON seasons(start_date, end_date);
-- Add comments
COMMENT ON TABLE seasons IS 'Manages 3-month seasonal competition cycles';
COMMENT ON COLUMN seasons.season_type IS 'WINTER, SPRING, SUMMER, or FALL';
COMMENT ON COLUMN seasons.is_active IS 'Only one season should be active at a time';
-- TABLE 2: user_progression
-- Purpose: Tracks all user XP, ranks, streaks, and workout statistics
-- Links to: users table (foreign key on user_id)
CREATE TABLE user_progression (
                                  user_progression_id BIGSERIAL PRIMARY KEY,
                                  user_id BIGINT NOT NULL UNIQUE,
-- SEASONAL PROGRESSION (Resets every 3 months)
                                  seasonal_xp INTEGER NOT NULL DEFAULT 0,
                                  seasonal_rank VARCHAR(20) NOT NULL DEFAULT 'NOVICE',
                                  seasonal_tier INTEGER NOT NULL DEFAULT 3,
                                  current_season_id INTEGER NOT NULL,
                                  season_start_date DATE NOT NULL,

-- LIFETIME PROGRESSION (Never resets)
                                  lifetime_xp INTEGER NOT NULL DEFAULT 0,
                                  lifetime_rank VARCHAR(20) NOT NULL DEFAULT 'NOVICE',
                                  lifetime_tier INTEGER NOT NULL DEFAULT 3,

-- STREAK TRACKING
                                  current_streak_days INTEGER NOT NULL DEFAULT 0,
                                  longest_streak_days INTEGER NOT NULL DEFAULT 0,
                                  last_workout_date DATE,

-- CORE STATISTICS
                                  total_workouts_completed INTEGER NOT NULL DEFAULT 0,
                                  total_sets_completed INTEGER NOT NULL DEFAULT 0,
                                  total_volume_lifted DECIMAL(12,2) NOT NULL DEFAULT 0,
                                  total_workout_minutes INTEGER NOT NULL DEFAULT 0,

-- ACHIEVEMENT TRACKING FIELDS
                                  total_distance_km DECIMAL(10,2) NOT NULL DEFAULT 0,
                                  total_hold_seconds INTEGER NOT NULL DEFAULT 0,
                                  unique_exercises_tried INTEGER NOT NULL DEFAULT 0,
                                  cardio_workouts_completed INTEGER NOT NULL DEFAULT 0,
                                  strength_workouts_completed INTEGER NOT NULL DEFAULT 0,
                                  isometric_workouts_completed INTEGER NOT NULL DEFAULT 0,
                                  first_of_month_count INTEGER NOT NULL DEFAULT 0,
                                  weekend_workout_count INTEGER NOT NULL DEFAULT 0,

-- WEEKLY TRACKING (For streak bonuses)
                                  weekly_workout_count INTEGER NOT NULL DEFAULT 0,
                                  week_start_date DATE NOT NULL DEFAULT CURRENT_DATE,

-- METADATA
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

-- CORE CONSTRAINTS
                                  CONSTRAINT valid_seasonal_xp CHECK (seasonal_xp >= 0),
                                  CONSTRAINT valid_lifetime_xp CHECK (lifetime_xp >= 0),
                                  CONSTRAINT valid_seasonal_tier CHECK (seasonal_tier BETWEEN 1 AND 3),
                                  CONSTRAINT valid_lifetime_tier CHECK (lifetime_tier BETWEEN 1 AND 3),
                                  CONSTRAINT valid_streak CHECK (current_streak_days >= 0),
                                  CONSTRAINT valid_seasonal_rank CHECK (seasonal_rank IN (
                                                                                          'NOVICE', 'APPRENTICE', 'DEVOTEE', 'WARRIOR', 'CHAMPION',
                                                                                          'ELITE', 'MASTER', 'LEGEND', 'ICON', 'IMMORTAL'
                                      )),
                                  CONSTRAINT valid_lifetime_rank CHECK (lifetime_rank IN (
                                                                                          'NOVICE', 'APPRENTICE', 'DEVOTEE', 'WARRIOR', 'CHAMPION',
                                                                                          'ELITE', 'MASTER', 'LEGEND', 'ICON', 'IMMORTAL'
                                      )),

-- ACHIEVEMENT TRACKING CONSTRAINTS
                                  CONSTRAINT valid_distance_km CHECK (total_distance_km >= 0),
                                  CONSTRAINT valid_hold_seconds CHECK (total_hold_seconds >= 0),
                                  CONSTRAINT valid_unique_exercises CHECK (unique_exercises_tried >= 0),
                                  CONSTRAINT valid_cardio_workouts CHECK (cardio_workouts_completed >= 0),
                                  CONSTRAINT valid_strength_workouts CHECK (strength_workouts_completed >= 0),
                                  CONSTRAINT valid_isometric_workouts CHECK (isometric_workouts_completed >= 0),
                                  CONSTRAINT valid_first_month_count CHECK (first_of_month_count >= 0),
                                  CONSTRAINT valid_weekend_count CHECK (weekend_workout_count >= 0),

-- FOREIGN KEYS
                                  CONSTRAINT fk_user_progression_user
                                      FOREIGN KEY (user_id)
                                          REFERENCES users(user_id)
                                          ON DELETE CASCADE,
                                  CONSTRAINT fk_user_progression_season
                                      FOREIGN KEY (current_season_id)
                                          REFERENCES seasons(season_id)
);
-- Core indexes for performance
CREATE INDEX idx_user_progression_user_id ON user_progression(user_id);
CREATE INDEX idx_user_progression_seasonal_rank ON user_progression(seasonal_rank, seasonal_tier);
CREATE INDEX idx_user_progression_lifetime_rank ON user_progression(lifetime_rank, lifetime_tier);
CREATE INDEX idx_user_progression_season ON user_progression(current_season_id);
CREATE INDEX idx_user_progression_seasonal_xp ON user_progression(seasonal_xp DESC);
CREATE INDEX idx_user_progression_lifetime_xp ON user_progression(lifetime_xp DESC);
-- Achievement tracking indexes
CREATE INDEX idx_user_progression_distance ON user_progression(total_distance_km);
CREATE INDEX idx_user_progression_hold_seconds ON user_progression(total_hold_seconds);
CREATE INDEX idx_user_progression_unique_exercises ON user_progression(unique_exercises_tried);
CREATE INDEX idx_user_progression_workout_types ON user_progression(cardio_workouts_completed, strength_workouts_completed, isometric_workouts_completed);
-- Add comments
COMMENT ON TABLE user_progression IS 'Tracks user XP, ranks, streaks, and workout statistics';
COMMENT ON COLUMN user_progression.seasonal_xp IS 'XP earned in current season (resets every 3 months)';
COMMENT ON COLUMN user_progression.lifetime_xp IS 'Total XP earned across all seasons (never resets)';
COMMENT ON COLUMN user_progression.seasonal_tier IS '3=III (lowest), 2=II, 1=I (highest)';
COMMENT ON COLUMN user_progression.current_streak_days IS 'Current consecutive workout days';
COMMENT ON COLUMN user_progression.weekly_workout_count IS 'Workouts completed this week (for streak bonuses)';
COMMENT ON COLUMN user_progression.total_distance_km IS 'Total cardio distance for Cardio Distance achievements';
COMMENT ON COLUMN user_progression.total_hold_seconds IS 'Total isometric hold time for Endurance achievements';
COMMENT ON COLUMN user_progression.unique_exercises_tried IS 'Count of different exercises tried for Diversity achievements';
-- TABLE 3: season_history
-- Purpose: Archives completed seasons for each user
-- Links to: users and seasons tables
CREATE TABLE season_history (
                                season_history_id BIGSERIAL PRIMARY KEY,
                                user_id BIGINT NOT NULL,
                                season_id INTEGER NOT NULL,
-- Final season stats
                                final_seasonal_xp INTEGER NOT NULL,
                                final_seasonal_rank VARCHAR(20) NOT NULL,
                                final_seasonal_tier INTEGER NOT NULL,

-- Percentile ranking
                                final_percentile DECIMAL(5,2),

-- Season achievements
                                total_workouts_this_season INTEGER NOT NULL DEFAULT 0,
                                highest_streak_this_season INTEGER NOT NULL DEFAULT 0,
                                perfect_weeks_this_season INTEGER NOT NULL DEFAULT 0,

                                completed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT unique_user_season UNIQUE(user_id, season_id),
                                CONSTRAINT valid_percentile CHECK (final_percentile >= 0 AND final_percentile <= 100),

-- FOREIGN KEYS
                                CONSTRAINT fk_season_history_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES users(user_id)
                                        ON DELETE CASCADE,
                                CONSTRAINT fk_season_history_season
                                    FOREIGN KEY (season_id)
                                        REFERENCES seasons(season_id)
);
-- Indexes for performance
CREATE INDEX idx_season_history_user ON season_history(user_id);
CREATE INDEX idx_season_history_season ON season_history(season_id);
CREATE INDEX idx_season_history_percentile ON season_history(season_id, final_percentile DESC);
-- Add comments
COMMENT ON TABLE season_history IS 'Archives completed seasons for each user';
COMMENT ON COLUMN season_history.final_percentile IS 'Final ranking percentile (95.50 = top 4.5%)';
COMMENT ON COLUMN season_history.perfect_weeks_this_season IS 'Weeks with 7/7 workouts completed';
-- TRIGGER: Auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_user_progression_timestamp()
RETURNS TRIGGER AS $$
BEGIN
NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trigger_user_progression_updated_at
    BEFORE UPDATE ON user_progression
    FOR EACH ROW
    EXECUTE FUNCTION update_user_progression_timestamp();
COMMENT ON FUNCTION update_user_progression_timestamp() IS 'Auto-updates updated_at timestamp on user_progression changes';
-- INITIAL DATA: Pre-populate 2025 seasons
INSERT INTO seasons (season_name, season_type, start_date, end_date, is_active) VALUES
                                                                                    ('Winter 2025', 'WINTER', '2025-01-01', '2025-03-31', TRUE),
                                                                                    ('Spring 2025', 'SPRING', '2025-04-01', '2025-06-30', FALSE),
                                                                                    ('Summer 2025', 'SUMMER', '2025-07-01', '2025-09-30', FALSE),
                                                                                    ('Fall 2025', 'FALL', '2025-10-01', '2025-12-31', FALSE);