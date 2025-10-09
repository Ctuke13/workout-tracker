-- ============================================================================
-- WORKOUT TRACKER - GAMIFICATION SYSTEM v1.0
-- Migration: V016__Create_Achievement_System.sql
-- Description: Creates achievements, user_achievements, and leaderboard tables
-- Author: Workout Tracker Team
-- Date: 2025-01-08
-- Total Achievements: 83 across 10 categories
-- ============================================================================

-- ============================================================================
-- TABLE 1: achievements
-- Purpose: Defines all available achievement badges
-- ============================================================================

CREATE TABLE achievements (
                              achievement_id SERIAL PRIMARY KEY,
                              achievement_key VARCHAR(50) NOT NULL UNIQUE,

    -- Display info
                              name VARCHAR(100) NOT NULL,
                              description TEXT NOT NULL,
                              icon_emoji VARCHAR(10),

    -- Classification
                              category VARCHAR(50) NOT NULL,
                              rarity VARCHAR(20) NOT NULL,

    -- Requirements
                              requirement_type VARCHAR(50) NOT NULL,
                              requirement_value INTEGER NOT NULL,

    -- Rewards
                              bonus_xp INTEGER DEFAULT 0,

    -- Metadata
                              display_order INTEGER NOT NULL DEFAULT 0,
                              is_hidden BOOLEAN DEFAULT FALSE,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT valid_rarity CHECK (rarity IN ('COMMON', 'UNCOMMON', 'RARE', 'EPIC', 'LEGENDARY')),
                              CONSTRAINT valid_category CHECK (category IN (
                                                                            'WORKOUT_MILESTONE', 'STREAK', 'STRENGTH_VOLUME', 'WEEKLY_CHALLENGE',
                                                                            'TIME_BASED', 'SEASONAL_RANK', 'SPECIAL_HIDDEN', 'CARDIO_DISTANCE',
                                                                            'ISOMETRIC_ENDURANCE', 'WORKOUT_DIVERSITY'
                                  ))
);

-- Indexes for performance
CREATE INDEX idx_achievements_category ON achievements(category);
CREATE INDEX idx_achievements_rarity ON achievements(rarity);
CREATE INDEX idx_achievements_key ON achievements(achievement_key);

-- Add comments
COMMENT ON TABLE achievements IS 'Defines all available achievement badges (83 total)';
COMMENT ON COLUMN achievements.achievement_key IS 'Unique identifier for code reference (e.g., FIRST_WORKOUT)';
COMMENT ON COLUMN achievements.rarity IS 'COMMON, UNCOMMON, RARE, EPIC, or LEGENDARY';
COMMENT ON COLUMN achievements.is_hidden IS 'Hidden until unlocked (Easter eggs)';

-- ============================================================================
-- TABLE 2: user_achievements
-- Purpose: Tracks which achievements each user has unlocked
-- Links to: users and achievements tables
-- ============================================================================

CREATE TABLE user_achievements (
                                   user_achievement_id BIGSERIAL PRIMARY KEY,
                                   user_id BIGINT NOT NULL,
                                   achievement_id INTEGER NOT NULL,

    -- Unlock info
                                   unlocked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   progress_when_unlocked INTEGER,

    -- Display
                                   is_featured BOOLEAN DEFAULT FALSE,

                                   CONSTRAINT unique_user_achievement UNIQUE(user_id, achievement_id),

    -- FOREIGN KEYS
                                   CONSTRAINT fk_user_achievement_user
                                       FOREIGN KEY (user_id)
                                           REFERENCES users(user_id)
                                           ON DELETE CASCADE,
                                   CONSTRAINT fk_user_achievement_achievement
                                       FOREIGN KEY (achievement_id)
                                           REFERENCES achievements(achievement_id)
                                           ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_user_achievements_user ON user_achievements(user_id);
CREATE INDEX idx_user_achievements_unlocked ON user_achievements(unlocked_at DESC);
CREATE INDEX idx_user_achievements_featured ON user_achievements(user_id, is_featured) WHERE is_featured = TRUE;

-- Add comments
COMMENT ON TABLE user_achievements IS 'Tracks unlocked achievements per user';
COMMENT ON COLUMN user_achievements.is_featured IS 'User can feature 3 badges on their profile';
COMMENT ON COLUMN user_achievements.progress_when_unlocked IS 'e.g., unlocked at 50 workouts';

-- ============================================================================
-- TABLE 3: leaderboard_entries
-- Purpose: Stores daily/weekly snapshots of leaderboard rankings
-- Links to: users and seasons tables
-- ============================================================================

CREATE TABLE leaderboard_entries (
                                     entry_id BIGSERIAL PRIMARY KEY,
                                     user_id BIGINT NOT NULL,
                                     season_id INTEGER NOT NULL,

    -- Rankings
                                     seasonal_xp INTEGER NOT NULL,
                                     seasonal_rank VARCHAR(20) NOT NULL,
                                     lifetime_xp INTEGER NOT NULL,

    -- Position
                                     seasonal_position INTEGER,
                                     percentile DECIMAL(5,2),

    -- Snapshot date
                                     snapshot_date DATE NOT NULL DEFAULT CURRENT_DATE,

                                     CONSTRAINT unique_user_season_snapshot UNIQUE(user_id, season_id, snapshot_date),

    -- FOREIGN KEYS
                                     CONSTRAINT fk_leaderboard_user
                                         FOREIGN KEY (user_id)
                                             REFERENCES users(user_id)
                                             ON DELETE CASCADE,
                                     CONSTRAINT fk_leaderboard_season
                                         FOREIGN KEY (season_id)
                                             REFERENCES seasons(season_id)
);

-- Indexes for performance
CREATE INDEX idx_leaderboard_season ON leaderboard_entries(season_id, seasonal_xp DESC);
CREATE INDEX idx_leaderboard_user ON leaderboard_entries(user_id);
CREATE INDEX idx_leaderboard_snapshot ON leaderboard_entries(season_id, snapshot_date);
CREATE INDEX idx_leaderboard_position ON leaderboard_entries(season_id, seasonal_position);

-- Add comments
COMMENT ON TABLE leaderboard_entries IS 'Daily/weekly snapshots of user rankings';
COMMENT ON COLUMN leaderboard_entries.seasonal_position IS 'Rank position (1 = first place)';
COMMENT ON COLUMN leaderboard_entries.percentile IS 'Percentile ranking (95.50 = top 4.5%)';

-- ============================================================================
-- INITIAL DATA: Seed All 83 Achievements
-- ============================================================================

-- ============================================================================
-- CATEGORY 1: WORKOUT MILESTONES (10 achievements)
-- ============================================================================
INSERT INTO achievements (achievement_key, name, description, icon_emoji, category, rarity, requirement_type, requirement_value, bonus_xp, display_order) VALUES
                                                                                                                                                              ('FIRST_WORKOUT', 'First Step', 'Complete your first workout', '🌱', 'WORKOUT_MILESTONE', 'COMMON', 'WORKOUT_COUNT', 1, 50, 1),
                                                                                                                                                              ('5_WORKOUTS', 'Ignition', 'Complete 5 workouts', '🔥', 'WORKOUT_MILESTONE', 'COMMON', 'WORKOUT_COUNT', 5, 100, 2),
                                                                                                                                                              ('10_WORKOUTS', 'Momentum', 'Complete 10 workouts', '💪', 'WORKOUT_MILESTONE', 'COMMON', 'WORKOUT_COUNT', 10, 150, 3),
                                                                                                                                                              ('25_WORKOUTS', 'Active', 'Complete 25 workouts', '🏃', 'WORKOUT_MILESTONE', 'UNCOMMON', 'WORKOUT_COUNT', 25, 250, 4),
                                                                                                                                                              ('50_WORKOUTS', 'Consistent', 'Complete 50 workouts', '⚡', 'WORKOUT_MILESTONE', 'UNCOMMON', 'WORKOUT_COUNT', 50, 500, 5),
                                                                                                                                                              ('100_WORKOUTS', 'Dedicated', 'Complete 100 workouts', '🎯', 'WORKOUT_MILESTONE', 'UNCOMMON', 'WORKOUT_COUNT', 100, 1000, 6),
                                                                                                                                                              ('250_WORKOUTS', 'Veteran', 'Complete 250 workouts', '🏆', 'WORKOUT_MILESTONE', 'RARE', 'WORKOUT_COUNT', 250, 2500, 7),
                                                                                                                                                              ('500_WORKOUTS', 'Champion', 'Complete 500 workouts', '⭐', 'WORKOUT_MILESTONE', 'RARE', 'WORKOUT_COUNT', 500, 5000, 8),
                                                                                                                                                              ('1000_WORKOUTS', 'Legend', 'Complete 1,000 workouts', '👑', 'WORKOUT_MILESTONE', 'EPIC', 'WORKOUT_COUNT', 1000, 10000, 9),
                                                                                                                                                              ('2500_WORKOUTS', 'Icon', 'Complete 2,500 workouts', '🌟', 'WORKOUT_MILESTONE', 'LEGENDARY', 'WORKOUT_COUNT', 2500, 25000, 10);

-- ============================================================================
-- CATEGORY 2: STREAK ACHIEVEMENTS (10 achievements)
-- ============================================================================
INSERT INTO achievements (achievement_key, name, description, icon_emoji, category, rarity, requirement_type, requirement_value, bonus_xp, display_order) VALUES
                                                                                                                                                              ('STREAK_3', 'Hot Start', '3-day workout streak', '🔥', 'STREAK', 'COMMON', 'STREAK_DAYS', 3, 100, 11),
                                                                                                                                                              ('STREAK_7', 'Week Warrior', '7-day workout streak', '💥', 'STREAK', 'COMMON', 'STREAK_DAYS', 7, 200, 12),
                                                                                                                                                              ('STREAK_14', 'Two Weeks Strong', '14-day workout streak', '🌊', 'STREAK', 'UNCOMMON', 'STREAK_DAYS', 14, 400, 13),
                                                                                                                                                              ('STREAK_21', 'Three Week Thunder', '21-day workout streak', '⚡', 'STREAK', 'UNCOMMON', 'STREAK_DAYS', 21, 600, 14),
                                                                                                                                                              ('STREAK_30', 'Monthly Grinder', '30-day workout streak', '📅', 'STREAK', 'UNCOMMON', 'STREAK_DAYS', 30, 1000, 15),
                                                                                                                                                              ('STREAK_60', '60 Days Deep', '60-day workout streak', '💎', 'STREAK', 'RARE', 'STREAK_DAYS', 60, 2000, 16),
                                                                                                                                                              ('STREAK_90', 'Quarter Burner', '90-day workout streak', '🔥', 'STREAK', 'RARE', 'STREAK_DAYS', 90, 3000, 17),
                                                                                                                                                              ('STREAK_180', 'Half Year Hero', '180-day workout streak', '👑', 'STREAK', 'EPIC', 'STREAK_DAYS', 180, 6000, 18),
                                                                                                                                                              ('STREAK_365', 'Year Long Legend', '365-day workout streak', '🏆', 'STREAK', 'EPIC', 'STREAK_DAYS', 365, 12000, 19),
                                                                                                                                                              ('STREAK_500', 'The Unbreakable', '500-day workout streak', '🌟', 'STREAK', 'LEGENDARY', 'STREAK_DAYS', 500, 20000, 20);

-- ============================================================================
-- CATEGORY 3: STRENGTH VOLUME (6 achievements)
-- ============================================================================
INSERT INTO achievements (achievement_key, name, description, icon_emoji, category, rarity, requirement_type, requirement_value, bonus_xp, display_order) VALUES
                                                                                                                                                              ('VOLUME_10K', 'Lifter', 'Lift 10,000 lbs total volume', '🏋️', 'STRENGTH_VOLUME', 'COMMON', 'VOLUME_LIFTED', 10000, 100, 21),
                                                                                                                                                              ('VOLUME_50K', 'Strong', 'Lift 50,000 lbs total volume', '💪', 'STRENGTH_VOLUME', 'COMMON', 'VOLUME_LIFTED', 50000, 200, 22),
                                                                                                                                                              ('VOLUME_100K', 'Powerhouse', 'Lift 100,000 lbs total volume', '⚡', 'STRENGTH_VOLUME', 'UNCOMMON', 'VOLUME_LIFTED', 100000, 500, 23),
                                                                                                                                                              ('VOLUME_250K', 'Iron Warrior', 'Lift 250,000 lbs total volume', '🛡️', 'STRENGTH_VOLUME', 'UNCOMMON', 'VOLUME_LIFTED', 250000, 1000, 24),
                                                                                                                                                              ('VOLUME_500K', 'Steel Giant', 'Lift 500,000 lbs total volume', '🏆', 'STRENGTH_VOLUME', 'RARE', 'VOLUME_LIFTED', 500000, 2500, 25),
                                                                                                                                                              ('VOLUME_1M', 'Million Pound Club', 'Lift 1,000,000 lbs total volume', '👑', 'STRENGTH_VOLUME', 'RARE', 'VOLUME_LIFTED', 1000000, 5000, 26);

-- ============================================================================
-- CATEGORY 4: WEEKLY CHALLENGES (5 achievements)
-- ============================================================================
INSERT INTO achievements (achievement_key, name, description, icon_emoji, category, rarity, requirement_type, requirement_value, bonus_xp, display_order) VALUES
                                                                                                                                                              ('WEEK_4_DAYS', 'Solid Week', '4 workouts in one week', '📅', 'WEEKLY_CHALLENGE', 'COMMON', 'WEEKLY_WORKOUTS', 4, 100, 27),
                                                                                                                                                              ('WEEK_5_DAYS', 'Strong Week', '5 workouts in one week', '💪', 'WEEKLY_CHALLENGE', 'COMMON', 'WEEKLY_WORKOUTS', 5, 150, 28),
                                                                                                                                                              ('WEEK_6_DAYS', 'Power Week', '6 workouts in one week', '⚡', 'WEEKLY_CHALLENGE', 'UNCOMMON', 'WEEKLY_WORKOUTS', 6, 250, 29),
                                                                                                                                                              ('WEEK_7_DAYS', 'Perfect Week', '7 workouts in one week', '🏆', 'WEEKLY_CHALLENGE', 'UNCOMMON', 'WEEKLY_WORKOUTS', 7, 500, 30),
                                                                                                                                                              ('PERFECT_MONTH', 'Perfect Month', 'Complete 4 perfect weeks in 30 days', '👑', 'WEEKLY_CHALLENGE', 'RARE', 'PERFECT_WEEKS', 4, 2000, 31);

-- ============================================================================
-- CATEGORY 5: TIME-BASED (5 achievements)
-- ============================================================================
INSERT INTO achievements (achievement_key, name, description, icon_emoji, category, rarity, requirement_type, requirement_value, bonus_xp, display_order) VALUES
                                                                                                                                                              ('TIME_10_HOURS', 'Time Warrior', 'Accumulate 10 hours of workout time', '⏱️', 'TIME_BASED', 'COMMON', 'WORKOUT_MINUTES', 600, 100, 32),
                                                                                                                                                              ('TIME_50_HOURS', 'Time Champion', 'Accumulate 50 hours of workout time', '⏰', 'TIME_BASED', 'COMMON', 'WORKOUT_MINUTES', 3000, 200, 33),
                                                                                                                                                              ('TIME_100_HOURS', 'Centurion', 'Accumulate 100 hours of workout time', '💯', 'TIME_BASED', 'UNCOMMON', 'WORKOUT_MINUTES', 6000, 500, 34),
                                                                                                                                                              ('TIME_250_HOURS', 'Time Lord', 'Accumulate 250 hours of workout time', '🕐', 'TIME_BASED', 'UNCOMMON', 'WORKOUT_MINUTES', 15000, 1000, 35),
                                                                                                                                                              ('TIME_500_HOURS', 'Eternal', 'Accumulate 500 hours of workout time', '♾️', 'TIME_BASED', 'RARE', 'WORKOUT_MINUTES', 30000, 2500, 36);

-- ============================================================================
-- CATEGORY 6: SEASONAL RANK (6 achievements)
-- ============================================================================
INSERT INTO achievements (achievement_key, name, description, icon_emoji, category, rarity, requirement_type, requirement_value, bonus_xp, display_order) VALUES
                                                                                                                                                              ('SEASON_DEVOTEE', 'Seasonal Devotee', 'Reach Devotee rank in a season', '🟡', 'SEASONAL_RANK', 'UNCOMMON', 'SEASONAL_RANK', 3, 500, 37),
                                                                                                                                                              ('SEASON_WARRIOR', 'Seasonal Warrior', 'Reach Warrior rank in a season', '🟢', 'SEASONAL_RANK', 'UNCOMMON', 'SEASONAL_RANK', 4, 1000, 38),
                                                                                                                                                              ('SEASON_CHAMPION', 'Seasonal Champion', 'Reach Champion rank in a season', '🔵', 'SEASONAL_RANK', 'RARE', 'SEASONAL_RANK', 5, 2000, 39),
                                                                                                                                                              ('SEASON_ELITE', 'Seasonal Elite', 'Reach Elite rank in a season', '💜', 'SEASONAL_RANK', 'RARE', 'SEASONAL_RANK', 6, 3000, 40),
                                                                                                                                                              ('SEASON_MASTER', 'Seasonal Master', 'Reach Master rank in a season', '🔴', 'SEASONAL_RANK', 'EPIC', 'SEASONAL_RANK', 7, 5000, 41),
                                                                                                                                                              ('SEASON_LEGEND', 'Seasonal Legend', 'Reach Legend rank in a season', '⚪', 'SEASONAL_RANK', 'EPIC', 'SEASONAL_RANK', 8, 10000, 42);

-- ============================================================================
-- CATEGORY 7: SPECIAL/HIDDEN (22 achievements)
-- ============================================================================

-- Time-Based (7)
INSERT INTO achievements (achievement_key, name, description, icon_emoji, category, rarity, requirement_type, requirement_value, bonus_xp, display_order, is_hidden) VALUES
                                                                                                                                                                         ('NEW_YEAR', 'New Year, New Me', 'Complete a workout on January 1st', '🎆', 'SPECIAL_HIDDEN', 'UNCOMMON', 'SPECIAL_EVENT', 1, 500, 43, TRUE),
                                                                                                                                                                         ('NIGHT_OWL', 'Night Owl', 'Complete a workout after 10pm', '🦉', 'SPECIAL_HIDDEN', 'COMMON', 'SPECIAL_EVENT', 1, 250, 44, TRUE),
                                                                                                                                                                         ('LUNCH_GAINS', 'Lunch Break Gains', 'Complete a workout between 11am-2pm', '🍱', 'SPECIAL_HIDDEN', 'COMMON', 'SPECIAL_EVENT', 1, 300, 45, TRUE),
                                                                                                                                                                         ('EARLY_BIRD', 'Early Bird', 'Complete a workout before 6am', '🌅', 'SPECIAL_HIDDEN', 'UNCOMMON', 'SPECIAL_EVENT', 1, 300, 46, TRUE),
                                                                                                                                                                         ('SUNRISE_WARRIOR', 'Sunrise Warrior', 'Complete a workout between 5am-7am', '🌄', 'SPECIAL_HIDDEN', 'UNCOMMON', 'SPECIAL_EVENT', 1, 400, 47, TRUE),
                                                                                                                                                                         ('MIDNIGHT_WARRIOR', 'Midnight Warrior', 'Complete a workout between 12am-1am', '🌙', 'SPECIAL_HIDDEN', 'UNCOMMON', 'SPECIAL_EVENT', 1, 500, 48, TRUE),
                                                                                                                                                                         ('GRAVEYARD_SHIFT', 'The Graveyard Shift', 'Complete a workout between 1am-5am', '⚰️', 'SPECIAL_HIDDEN', 'RARE', 'SPECIAL_EVENT', 1, 800, 49, TRUE);

-- Special Days (6)
INSERT INTO achievements (achievement_key, name, description, icon_emoji, category, rarity, requirement_type, requirement_value, bonus_xp, display_order, is_hidden) VALUES
                                                                                                                                                                         ('VALENTINES_SWEAT', 'Valentine''s Sweat', 'Complete a workout on Valentine''s Day', '💝', 'SPECIAL_HIDDEN', 'COMMON', 'SPECIAL_EVENT', 1, 300, 50, TRUE),
                                                                                                                                                                         ('INDEPENDENCE_GAINS', 'Independence Gains', 'Complete a workout on July 4th', '🎇', 'SPECIAL_HIDDEN', 'COMMON', 'SPECIAL_EVENT', 1, 300, 51, TRUE),
                                                                                                                                                                         ('SPOOKY_SEASON', 'Spooky Season', 'Complete a workout on Halloween', '🎃', 'SPECIAL_HIDDEN', 'COMMON', 'SPECIAL_EVENT', 1, 300, 52, TRUE),
                                                                                                                                                                         ('BIRTHDAY_GAINS', 'Birthday Gains', 'Complete a workout on your birthday', '🎂', 'SPECIAL_HIDDEN', 'UNCOMMON', 'SPECIAL_EVENT', 1, 500, 53, TRUE),
                                                                                                                                                                         ('TURKEY_BURNER', 'Turkey Burner', 'Complete a workout on Thanksgiving', '🦃', 'SPECIAL_HIDDEN', 'UNCOMMON', 'SPECIAL_EVENT', 1, 500, 54, TRUE),
                                                                                                                                                                         ('CHRISTMAS_CRUSHER', 'Christmas Crusher', 'Complete a workout on Christmas Day', '🎄', 'SPECIAL_HIDDEN', 'UNCOMMON', 'SPECIAL_EVENT', 1, 500, 55, TRUE);

-- Number Patterns (4)
INSERT INTO achievements (achievement_key, name, description, icon_emoji, category, rarity, requirement_type, requirement_value, bonus_xp, display_order, is_hidden) VALUES
                                                                                                                                                                         ('PERFECT_TEN', 'Perfect Ten', 'Complete your 10th workout milestone', '🔟', 'SPECIAL_HIDDEN', 'COMMON', 'WORKOUT_COUNT', 10, 100, 56, TRUE),
                                                                                                                                                                         ('CENTURY_CLUB', 'Century Club', 'Complete your 100th workout milestone', '💯', 'SPECIAL_HIDDEN', 'UNCOMMON', 'WORKOUT_COUNT', 100, 500, 57, TRUE),
                                                                                                                                                                         ('THOUSAND_STRONG', 'Thousand Strong', 'Complete your 1,000th workout milestone', '👑', 'SPECIAL_HIDDEN', 'RARE', 'WORKOUT_COUNT', 1000, 2000, 58, TRUE),
                                                                                                                                                                         ('TRIPLE_THREAT', 'Triple Threat', 'Complete 3 workouts on 3/3, 6/6, 9/9, or 12/12', '3️⃣', 'SPECIAL_HIDDEN', 'RARE', 'SPECIAL_EVENT', 3, 800, 59, TRUE);

-- Speed & Duration (2)
INSERT INTO achievements (achievement_key, name, description, icon_emoji, category, rarity, requirement_type, requirement_value, bonus_xp, display_order, is_hidden) VALUES
                                                                                                                                                                         ('SPEED_DEMON', 'Speed Demon', 'Complete a workout in under 15 minutes', '⚡', 'SPECIAL_HIDDEN', 'COMMON', 'SPECIAL_EVENT', 1, 200, 60, TRUE),
                                                                                                                                                                         ('MARATHON_SESSION', 'Marathon Session', 'Complete a workout over 2 hours', '⏰', 'SPECIAL_HIDDEN', 'UNCOMMON', 'SPECIAL_EVENT', 1, 600, 61, TRUE);

-- Random & Fun (3)
INSERT INTO achievements (achievement_key, name, description, icon_emoji, category, rarity, requirement_type, requirement_value, bonus_xp, display_order, is_hidden) VALUES
                                                                                                                                                                         ('FIRST_OF_MONTH', 'First of the Month', 'Workout on the 1st of any month, 12 times', '📅', 'SPECIAL_HIDDEN', 'RARE', 'SPECIAL_EVENT', 12, 1200, 62, TRUE),
                                                                                                                                                                         ('WEEKEND_WARRIOR', 'Weekend Warrior', 'Complete 52 weekend workouts in a year', '🎉', 'SPECIAL_HIDDEN', 'RARE', 'SPECIAL_EVENT', 52, 1500, 63, TRUE),
                                                                                                                                                                         ('COMEBACK_KID', 'Comeback Kid', 'Return to workout after 30+ day break', '🔙', 'SPECIAL_HIDDEN', 'UNCOMMON', 'SPECIAL_EVENT', 1, 500, 64, TRUE);

-- ============================================================================
-- CATEGORY 8: CARDIO DISTANCE (6 achievements)
-- ============================================================================
INSERT INTO achievements (achievement_key, name, description, icon_emoji, category, rarity, requirement_type, requirement_value, bonus_xp, display_order) VALUES
                                                                                                                                                              ('DISTANCE_5K', 'First Mile', 'Run/bike 5 km total distance', '🏃', 'CARDIO_DISTANCE', 'COMMON', 'DISTANCE_KM', 5, 100, 65),
                                                                                                                                                              ('DISTANCE_25K', 'Road Warrior', 'Run/bike 25 km total distance', '🛣️', 'CARDIO_DISTANCE', 'COMMON', 'DISTANCE_KM', 25, 200, 66),
                                                                                                                                                              ('DISTANCE_100K', 'Distance Seeker', 'Run/bike 100 km total distance', '🗺️', 'CARDIO_DISTANCE', 'UNCOMMON', 'DISTANCE_KM', 100, 500, 67),
                                                                                                                                                              ('DISTANCE_250K', 'Marathon Ready', 'Run/bike 250 km total distance', '🏃', 'CARDIO_DISTANCE', 'UNCOMMON', 'DISTANCE_KM', 250, 1000, 68),
                                                                                                                                                              ('DISTANCE_500K', 'Ultra Runner', 'Run/bike 500 km total distance', '🏆', 'CARDIO_DISTANCE', 'RARE', 'DISTANCE_KM', 500, 2500, 69),
                                                                                                                                                              ('DISTANCE_1000K', 'Around the World', 'Run/bike 1,000 km total distance', '🌍', 'CARDIO_DISTANCE', 'RARE', 'DISTANCE_KM', 1000, 5000, 70);

-- ============================================================================
-- CATEGORY 9: ISOMETRIC ENDURANCE (6 achievements)
-- ============================================================================
INSERT INTO achievements (achievement_key, name, description, icon_emoji, category, rarity, requirement_type, requirement_value, bonus_xp, display_order) VALUES
                                                                                                                                                              ('HOLD_10MIN', 'Steady Holder', 'Accumulate 10 minutes total hold time', '🧘', 'ISOMETRIC_ENDURANCE', 'COMMON', 'HOLD_SECONDS', 600, 100, 71),
                                                                                                                                                              ('HOLD_1HOUR', 'Plank Master', 'Accumulate 1 hour total hold time', '💎', 'ISOMETRIC_ENDURANCE', 'COMMON', 'HOLD_SECONDS', 3600, 200, 72),
                                                                                                                                                              ('HOLD_5HOURS', 'Iron Will', 'Accumulate 5 hours total hold time', '🛡️', 'ISOMETRIC_ENDURANCE', 'UNCOMMON', 'HOLD_SECONDS', 18000, 500, 73),
                                                                                                                                                              ('HOLD_10HOURS', 'Statue', 'Accumulate 10 hours total hold time', '🗿', 'ISOMETRIC_ENDURANCE', 'UNCOMMON', 'HOLD_SECONDS', 36000, 1000, 74),
                                                                                                                                                              ('HOLD_25HOURS', 'Immovable', 'Accumulate 25 hours total hold time', '⚓', 'ISOMETRIC_ENDURANCE', 'RARE', 'HOLD_SECONDS', 90000, 2500, 75),
                                                                                                                                                              ('HOLD_50HOURS', 'The Pillar', 'Accumulate 50 hours total hold time', '🏛️', 'ISOMETRIC_ENDURANCE', 'RARE', 'HOLD_SECONDS', 180000, 5000, 76);

-- ============================================================================
-- CATEGORY 10: WORKOUT DIVERSITY (7 achievements)
-- ============================================================================
INSERT INTO achievements (achievement_key, name, description, icon_emoji, category, rarity, requirement_type, requirement_value, bonus_xp, display_order) VALUES
                                                                                                                                                              ('DIVERSITY_10', 'Explorer', 'Try 10 different exercises', '🗺️', 'WORKOUT_DIVERSITY', 'COMMON', 'UNIQUE_EXERCISES', 10, 200, 77),
                                                                                                                                                              ('DIVERSITY_25', 'Versatile', 'Try 25 different exercises', '🎨', 'WORKOUT_DIVERSITY', 'UNCOMMON', 'UNIQUE_EXERCISES', 25, 500, 78),
                                                                                                                                                              ('DIVERSITY_50', 'Renaissance Athlete', 'Try 50 different exercises', '🎭', 'WORKOUT_DIVERSITY', 'RARE', 'UNIQUE_EXERCISES', 50, 1500, 79),
                                                                                                                                                              ('CARDIO_SPECIALIST', 'Cardio Enthusiast', 'Complete 25 cardio workouts', '🏃', 'WORKOUT_DIVERSITY', 'UNCOMMON', 'CARDIO_WORKOUTS', 25, 500, 80),
                                                                                                                                                              ('STRENGTH_SPECIALIST', 'Strength Specialist', 'Complete 100 strength workouts', '💪', 'WORKOUT_DIVERSITY', 'RARE', 'STRENGTH_WORKOUTS', 100, 2000, 81),
                                                                                                                                                              ('BALANCE_SPECIALIST', 'Balance Master', 'Complete 25 isometric workouts', '🧘', 'WORKOUT_DIVERSITY', 'UNCOMMON', 'ISOMETRIC_WORKOUTS', 25, 500, 82),
                                                                                                                                                              ('JACK_OF_TRADES', 'Jack of All Trades', 'Complete 10+ strength, 10+ cardio, 10+ isometric', '🎯', 'WORKOUT_DIVERSITY', 'RARE', 'SPECIAL_EVENT', 1, 2000, 83);

-- ============================================================================
-- VERIFICATION QUERIES (Run these after migration to verify)
-- ============================================================================

-- Check that all tables were created
-- SELECT table_name FROM information_schema.tables
-- WHERE table_schema = 'public'
-- AND table_name IN ('achievements', 'user_achievements', 'leaderboard_entries')
-- ORDER BY table_name;

-- Check total achievements count
-- SELECT COUNT(*) as total_achievements FROM achievements;
-- Expected: 83

-- Check achievements by category
-- SELECT category, COUNT(*) as count
-- FROM achievements
-- GROUP BY category
-- ORDER BY category;

-- Check achievements by rarity
-- SELECT rarity, COUNT(*) as count
-- FROM achievements
-- GROUP BY rarity
-- ORDER BY
--     CASE rarity
--         WHEN 'COMMON' THEN 1
--         WHEN 'UNCOMMON' THEN 2
--         WHEN 'RARE' THEN 3
--         WHEN 'EPIC' THEN 4
--         WHEN 'LEGENDARY' THEN 5
--     END;

-- Check hidden achievements
-- SELECT COUNT(*) as hidden_count FROM achievements WHERE is_hidden = TRUE;
-- Expected: 22

-- ============================================================================
-- END OF MIGRATION
-- ============================================================================