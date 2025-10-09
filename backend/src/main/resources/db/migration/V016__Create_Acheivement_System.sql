-- ============================================================================
-- WORKOUT TRACKER - GAMIFICATION SYSTEM v1.0
-- Migration: V016__Create_Achievement_System.sql
-- Description: Creates achievements, user_achievements, and leaderboard tables
-- Author: Workout Tracker Team
-- Date: 2025-01-08
-- Updated: 2025-10-09 - Fixed to match Java entity column names
-- Total Achievements: 83 across 10 categories
-- ============================================================================

-- ============================================================================
-- TABLE 1: achievements (FIXED to match Java entity)
-- ============================================================================

CREATE TABLE achievements (
                              achievement_id SERIAL PRIMARY KEY,
                              achievement_key VARCHAR(50) NOT NULL UNIQUE,

    -- Display info
                              name VARCHAR(100) NOT NULL,
                              description TEXT NOT NULL,
                              icon VARCHAR(10),

    -- Classification
                              category VARCHAR(50) NOT NULL,
                              rarity VARCHAR(20) NOT NULL,

    -- Criteria (FIXED: matches Achievement.java exactly)
                              criteria_field VARCHAR(50),
                              criteria_operator VARCHAR(10),
                              criteria_value INTEGER,

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

CREATE INDEX idx_achievements_category ON achievements(category);
CREATE INDEX idx_achievements_rarity ON achievements(rarity);
CREATE INDEX idx_achievements_key ON achievements(achievement_key);

COMMENT ON TABLE achievements IS 'Defines all available achievement badges (83 total)';

-- ============================================================================
-- TABLE 2: user_achievements (FIXED to match Java entity)
-- ============================================================================

CREATE TABLE user_achievements (
                                   user_achievement_id BIGSERIAL PRIMARY KEY,
                                   user_id BIGINT NOT NULL,
                                   achievement_id INTEGER NOT NULL,

    -- Unlock info (FIXED: matches UserAchievement.java)
                                   unlocked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   bonus_xp_awarded INTEGER NOT NULL,
                                   progress_value_at_unlock INTEGER,

                                   CONSTRAINT unique_user_achievement UNIQUE(user_id, achievement_id),

    -- Foreign keys
                                   CONSTRAINT fk_user_achievement_user
                                       FOREIGN KEY (user_id)
                                           REFERENCES users(user_id)
                                           ON DELETE CASCADE,
                                   CONSTRAINT fk_user_achievement_achievement
                                       FOREIGN KEY (achievement_id)
                                           REFERENCES achievements(achievement_id)
                                           ON DELETE CASCADE
);

CREATE INDEX idx_user_achievements_user ON user_achievements(user_id);
CREATE INDEX idx_user_achievements_unlocked ON user_achievements(unlocked_at DESC);

COMMENT ON TABLE user_achievements IS 'Tracks unlocked achievements per user';

-- ============================================================================
-- TABLE 3: leaderboard_entries (FIXED to match Java entity)
-- ============================================================================

CREATE TABLE leaderboard_entries (
                                     leaderboard_entry_id BIGSERIAL PRIMARY KEY,
                                     user_id BIGINT NOT NULL,
                                     season_id INTEGER NOT NULL,

    -- Snapshot info
                                     snapshot_date DATE NOT NULL DEFAULT CURRENT_DATE,

    -- Rankings (FIXED: matches LeaderboardEntry.java)
                                     rank_position INTEGER NOT NULL,
                                     seasonal_xp INTEGER NOT NULL,
                                     seasonal_rank VARCHAR(20) NOT NULL,
                                     seasonal_tier INTEGER NOT NULL,

    -- Stats at snapshot time
                                     workouts_completed INTEGER NOT NULL,
                                     current_streak INTEGER NOT NULL,
                                     achievements_count INTEGER NOT NULL,

    -- Position metrics
                                     percentile DOUBLE PRECISION,
                                     rank_change INTEGER,

    -- Metadata
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                     CONSTRAINT unique_user_season_snapshot UNIQUE(user_id, season_id, snapshot_date),

    -- Foreign keys
                                     CONSTRAINT fk_leaderboard_user
                                         FOREIGN KEY (user_id)
                                             REFERENCES users(user_id)
                                             ON DELETE CASCADE,
                                     CONSTRAINT fk_leaderboard_season
                                         FOREIGN KEY (season_id)
                                             REFERENCES seasons(season_id)
);

CREATE INDEX idx_leaderboard_season ON leaderboard_entries(season_id, seasonal_xp DESC);
CREATE INDEX idx_leaderboard_user ON leaderboard_entries(user_id);
CREATE INDEX idx_leaderboard_snapshot ON leaderboard_entries(season_id, snapshot_date);
CREATE INDEX idx_leaderboard_position ON leaderboard_entries(season_id, rank_position);

COMMENT ON TABLE leaderboard_entries IS 'Daily/weekly snapshots of user rankings';

-- ============================================================================
-- ACHIEVEMENT DATA: All 83 Achievements
-- ============================================================================
-- ============================================================================
-- CATEGORY 1: WORKOUT MILESTONES (10 achievements)
-- ============================================================================
INSERT INTO achievements (achievement_key, name, description, icon, category, rarity, criteria_field, criteria_operator, criteria_value, bonus_xp, display_order) VALUES
                                                                                                                                                                      ('FIRST_WORKOUT', 'First Step', 'Complete your first workout', '🌱', 'WORKOUT_MILESTONE', 'COMMON', 'total_workouts_completed', '>=', 1, 50, 1),
                                                                                                                                                                      ('5_WORKOUTS', 'Ignition', 'Complete 5 workouts', '🔥', 'WORKOUT_MILESTONE', 'COMMON', 'total_workouts_completed', '>=', 5, 100, 2),
                                                                                                                                                                      ('10_WORKOUTS', 'Momentum', 'Complete 10 workouts', '💪', 'WORKOUT_MILESTONE', 'COMMON', 'total_workouts_completed', '>=', 10, 150, 3),
                                                                                                                                                                      ('25_WORKOUTS', 'Active', 'Complete 25 workouts', '🏃', 'WORKOUT_MILESTONE', 'UNCOMMON', 'total_workouts_completed', '>=', 25, 250, 4),
                                                                                                                                                                      ('50_WORKOUTS', 'Consistent', 'Complete 50 workouts', '⚡', 'WORKOUT_MILESTONE', 'UNCOMMON', 'total_workouts_completed', '>=', 50, 500, 5),
                                                                                                                                                                      ('100_WORKOUTS', 'Dedicated', 'Complete 100 workouts', '🎯', 'WORKOUT_MILESTONE', 'UNCOMMON', 'total_workouts_completed', '>=', 100, 1000, 6),
                                                                                                                                                                      ('250_WORKOUTS', 'Veteran', 'Complete 250 workouts', '🏆', 'WORKOUT_MILESTONE', 'RARE', 'total_workouts_completed', '>=', 250, 2500, 7),
                                                                                                                                                                      ('500_WORKOUTS', 'Champion', 'Complete 500 workouts', '⭐', 'WORKOUT_MILESTONE', 'RARE', 'total_workouts_completed', '>=', 500, 5000, 8),
                                                                                                                                                                      ('1000_WORKOUTS', 'Legend', 'Complete 1,000 workouts', '👑', 'WORKOUT_MILESTONE', 'EPIC', 'total_workouts_completed', '>=', 1000, 10000, 9),
                                                                                                                                                                      ('2500_WORKOUTS', 'Icon', 'Complete 2,500 workouts', '🌟', 'WORKOUT_MILESTONE', 'LEGENDARY', 'total_workouts_completed', '>=', 2500, 25000, 10);

-- ============================================================================
-- CATEGORY 2: STREAK ACHIEVEMENTS (10 achievements)
-- ============================================================================
INSERT INTO achievements (achievement_key, name, description, icon, category, rarity, criteria_field, criteria_operator, criteria_value, bonus_xp, display_order) VALUES
                                                                                                                                                                      ('STREAK_3', 'Hot Start', '3-day workout streak', '🔥', 'STREAK', 'COMMON', 'current_streak_days', '>=', 3, 100, 11),
                                                                                                                                                                      ('STREAK_7', 'Week Warrior', '7-day workout streak', '💥', 'STREAK', 'COMMON', 'current_streak_days', '>=', 7, 200, 12),
                                                                                                                                                                      ('STREAK_14', 'Two Weeks Strong', '14-day workout streak', '🌊', 'STREAK', 'UNCOMMON', 'current_streak_days', '>=', 14, 400, 13),
                                                                                                                                                                      ('STREAK_21', 'Three Week Thunder', '21-day workout streak', '⚡', 'STREAK', 'UNCOMMON', 'current_streak_days', '>=', 21, 600, 14),
                                                                                                                                                                      ('STREAK_30', 'Monthly Grinder', '30-day workout streak', '📅', 'STREAK', 'UNCOMMON', 'current_streak_days', '>=', 30, 1000, 15),
                                                                                                                                                                      ('STREAK_60', '60 Days Deep', '60-day workout streak', '💎', 'STREAK', 'RARE', 'current_streak_days', '>=', 60, 2000, 16),
                                                                                                                                                                      ('STREAK_90', 'Quarter Burner', '90-day workout streak', '🔥', 'STREAK', 'RARE', 'current_streak_days', '>=', 90, 3000, 17),
                                                                                                                                                                      ('STREAK_180', 'Half Year Hero', '180-day workout streak', '👑', 'STREAK', 'EPIC', 'current_streak_days', '>=', 180, 6000, 18),
                                                                                                                                                                      ('STREAK_365', 'Year Long Legend', '365-day workout streak', '🏆', 'STREAK', 'EPIC', 'current_streak_days', '>=', 365, 12000, 19),
                                                                                                                                                                      ('STREAK_500', 'The Unbreakable', '500-day workout streak', '🌟', 'STREAK', 'LEGENDARY', 'current_streak_days', '>=', 500, 20000, 20);

-- ============================================================================
-- CATEGORY 3: STRENGTH VOLUME (6 achievements)
-- ============================================================================
INSERT INTO achievements (achievement_key, name, description, icon, category, rarity, criteria_field, criteria_operator, criteria_value, bonus_xp, display_order) VALUES
                                                                                                                                                                      ('VOLUME_10K', 'Lifter', 'Lift 10,000 lbs total volume', '🏋️', 'STRENGTH_VOLUME', 'COMMON', 'total_volume_lifted', '>=', 10000, 100, 21),
                                                                                                                                                                      ('VOLUME_50K', 'Strong', 'Lift 50,000 lbs total volume', '💪', 'STRENGTH_VOLUME', 'COMMON', 'total_volume_lifted', '>=', 50000, 200, 22),
                                                                                                                                                                      ('VOLUME_100K', 'Powerhouse', 'Lift 100,000 lbs total volume', '⚡', 'STRENGTH_VOLUME', 'UNCOMMON', 'total_volume_lifted', '>=', 100000, 500, 23),
                                                                                                                                                                      ('VOLUME_250K', 'Iron Warrior', 'Lift 250,000 lbs total volume', '🛡️', 'STRENGTH_VOLUME', 'UNCOMMON', 'total_volume_lifted', '>=', 250000, 1000, 24),
                                                                                                                                                                      ('VOLUME_500K', 'Steel Giant', 'Lift 500,000 lbs total volume', '🏆', 'STRENGTH_VOLUME', 'RARE', 'total_volume_lifted', '>=', 500000, 2500, 25),
                                                                                                                                                                      ('VOLUME_1M', 'Million Pound Club', 'Lift 1,000,000 lbs total volume', '👑', 'STRENGTH_VOLUME', 'RARE', 'total_volume_lifted', '>=', 1000000, 5000, 26);

-- ============================================================================
-- CATEGORY 4: WEEKLY CHALLENGES (5 achievements)
-- ============================================================================
INSERT INTO achievements (achievement_key, name, description, icon, category, rarity, criteria_field, criteria_operator, criteria_value, bonus_xp, display_order) VALUES
                                                                                                                                                                      ('WEEK_4_DAYS', 'Solid Week', '4 workouts in one week', '📅', 'WEEKLY_CHALLENGE', 'COMMON', 'weekly_workout_count', '>=', 4, 100, 27),
                                                                                                                                                                      ('WEEK_5_DAYS', 'Strong Week', '5 workouts in one week', '💪', 'WEEKLY_CHALLENGE', 'COMMON', 'weekly_workout_count', '>=', 5, 150, 28),
                                                                                                                                                                      ('WEEK_6_DAYS', 'Power Week', '6 workouts in one week', '⚡', 'WEEKLY_CHALLENGE', 'UNCOMMON', 'weekly_workout_count', '>=', 6, 250, 29),
                                                                                                                                                                      ('WEEK_7_DAYS', 'Perfect Week', '7 workouts in one week', '🏆', 'WEEKLY_CHALLENGE', 'UNCOMMON', 'weekly_workout_count', '>=', 7, 500, 30),
                                                                                                                                                                      ('PERFECT_MONTH', 'Perfect Month', 'Complete 4 perfect weeks in 30 days', '👑', 'WEEKLY_CHALLENGE', 'RARE', 'perfect_weeks', '>=', 4, 2000, 31);

-- ============================================================================
-- CATEGORY 5: TIME-BASED (5 achievements)
-- ============================================================================
INSERT INTO achievements (achievement_key, name, description, icon, category, rarity, criteria_field, criteria_operator, criteria_value, bonus_xp, display_order) VALUES
                                                                                                                                                                      ('TIME_10_HOURS', 'Time Warrior', 'Accumulate 10 hours of workout time', '⏱️', 'TIME_BASED', 'COMMON', 'total_workout_minutes', '>=', 600, 100, 32),
                                                                                                                                                                      ('TIME_50_HOURS', 'Time Champion', 'Accumulate 50 hours of workout time', '⏰', 'TIME_BASED', 'COMMON', 'total_workout_minutes', '>=', 3000, 200, 33),
                                                                                                                                                                      ('TIME_100_HOURS', 'Centurion', 'Accumulate 100 hours of workout time', '💯', 'TIME_BASED', 'UNCOMMON', 'total_workout_minutes', '>=', 6000, 500, 34),
                                                                                                                                                                      ('TIME_250_HOURS', 'Time Lord', 'Accumulate 250 hours of workout time', '🕐', 'TIME_BASED', 'UNCOMMON', 'total_workout_minutes', '>=', 15000, 1000, 35),
                                                                                                                                                                      ('TIME_500_HOURS', 'Eternal', 'Accumulate 500 hours of workout time', '♾️', 'TIME_BASED', 'RARE', 'total_workout_minutes', '>=', 30000, 2500, 36);

-- ============================================================================
-- CATEGORY 6: SEASONAL RANK (6 achievements)
-- ============================================================================
INSERT INTO achievements (achievement_key, name, description, icon, category, rarity, criteria_field, criteria_operator, criteria_value, bonus_xp, display_order) VALUES
                                                                                                                                                                      ('SEASON_DEVOTEE', 'Seasonal Devotee', 'Reach Devotee rank in a season', '🟡', 'SEASONAL_RANK', 'UNCOMMON', 'seasonal_rank', '>=', 3, 500, 37),
                                                                                                                                                                      ('SEASON_WARRIOR', 'Seasonal Warrior', 'Reach Warrior rank in a season', '🟢', 'SEASONAL_RANK', 'UNCOMMON', 'seasonal_rank', '>=', 4, 1000, 38),
                                                                                                                                                                      ('SEASON_CHAMPION', 'Seasonal Champion', 'Reach Champion rank in a season', '🔵', 'SEASONAL_RANK', 'RARE', 'seasonal_rank', '>=', 5, 2000, 39),
                                                                                                                                                                      ('SEASON_ELITE', 'Seasonal Elite', 'Reach Elite rank in a season', '💜', 'SEASONAL_RANK', 'RARE', 'seasonal_rank', '>=', 6, 3000, 40),
                                                                                                                                                                      ('SEASON_MASTER', 'Seasonal Master', 'Reach Master rank in a season', '🔴', 'SEASONAL_RANK', 'EPIC', 'seasonal_rank', '>=', 7, 5000, 41),
                                                                                                                                                                      ('SEASON_LEGEND', 'Seasonal Legend', 'Reach Legend rank in a season', '⚪', 'SEASONAL_RANK', 'EPIC', 'seasonal_rank', '>=', 8, 10000, 42);

-- ============================================================================
-- CATEGORY 7: SPECIAL/HIDDEN (22 achievements)
-- ============================================================================

-- Time-Based (7)
INSERT INTO achievements (achievement_key, name, description, icon, category, rarity, criteria_field, criteria_operator, criteria_value, bonus_xp, display_order, is_hidden) VALUES
                                                                                                                                                                                 ('NEW_YEAR', 'New Year, New Me', 'Complete a workout on January 1st', '🎆', 'SPECIAL_HIDDEN', 'UNCOMMON', 'special_event', '=', 1, 500, 43, TRUE),
                                                                                                                                                                                 ('NIGHT_OWL', 'Night Owl', 'Complete a workout after 10pm', '🦉', 'SPECIAL_HIDDEN', 'COMMON', 'special_event', '=', 1, 250, 44, TRUE),
                                                                                                                                                                                 ('LUNCH_GAINS', 'Lunch Break Gains', 'Complete a workout between 11am-2pm', '🍱', 'SPECIAL_HIDDEN', 'COMMON', 'special_event', '=', 1, 300, 45, TRUE),
                                                                                                                                                                                 ('EARLY_BIRD', 'Early Bird', 'Complete a workout before 6am', '🌅', 'SPECIAL_HIDDEN', 'UNCOMMON', 'special_event', '=', 1, 300, 46, TRUE),
                                                                                                                                                                                 ('SUNRISE_WARRIOR', 'Sunrise Warrior', 'Complete a workout between 5am-7am', '🌄', 'SPECIAL_HIDDEN', 'UNCOMMON', 'special_event', '=', 1, 400, 47, TRUE),
                                                                                                                                                                                 ('MIDNIGHT_WARRIOR', 'Midnight Warrior', 'Complete a workout between 12am-1am', '🌙', 'SPECIAL_HIDDEN', 'UNCOMMON', 'special_event', '=', 1, 500, 48, TRUE),
                                                                                                                                                                                 ('GRAVEYARD_SHIFT', 'The Graveyard Shift', 'Complete a workout between 1am-5am', '⚰️', 'SPECIAL_HIDDEN', 'RARE', 'special_event', '=', 1, 800, 49, TRUE);

-- Special Days (6)
INSERT INTO achievements (achievement_key, name, description, icon, category, rarity, criteria_field, criteria_operator, criteria_value, bonus_xp, display_order, is_hidden) VALUES
                                                                                                                                                                                 ('VALENTINES_SWEAT', 'Valentine''s Sweat', 'Complete a workout on Valentine''s Day', '💝', 'SPECIAL_HIDDEN', 'COMMON', 'special_event', '=', 1, 300, 50, TRUE),
                                                                                                                                                                                 ('INDEPENDENCE_GAINS', 'Independence Gains', 'Complete a workout on July 4th', '🎇', 'SPECIAL_HIDDEN', 'COMMON', 'special_event', '=', 1, 300, 51, TRUE),
                                                                                                                                                                                 ('SPOOKY_SEASON', 'Spooky Season', 'Complete a workout on Halloween', '🎃', 'SPECIAL_HIDDEN', 'COMMON', 'special_event', '=', 1, 300, 52, TRUE),
                                                                                                                                                                                 ('BIRTHDAY_GAINS', 'Birthday Gains', 'Complete a workout on your birthday', '🎂', 'SPECIAL_HIDDEN', 'UNCOMMON', 'special_event', '=', 1, 500, 53, TRUE),
                                                                                                                                                                                 ('TURKEY_BURNER', 'Turkey Burner', 'Complete a workout on Thanksgiving', '🦃', 'SPECIAL_HIDDEN', 'UNCOMMON', 'special_event', '=', 1, 500, 54, TRUE),
                                                                                                                                                                                 ('CHRISTMAS_CRUSHER', 'Christmas Crusher', 'Complete a workout on Christmas Day', '🎄', 'SPECIAL_HIDDEN', 'UNCOMMON', 'special_event', '=', 1, 500, 55, TRUE);

-- Number Patterns (4)
INSERT INTO achievements (achievement_key, name, description, icon, category, rarity, criteria_field, criteria_operator, criteria_value, bonus_xp, display_order, is_hidden) VALUES
                                                                                                                                                                                 ('PERFECT_TEN', 'Perfect Ten', 'Complete your 10th workout milestone', '🔟', 'SPECIAL_HIDDEN', 'COMMON', 'total_workouts_completed', '=', 10, 100, 56, TRUE),
                                                                                                                                                                                 ('CENTURY_CLUB', 'Century Club', 'Complete your 100th workout milestone', '💯', 'SPECIAL_HIDDEN', 'UNCOMMON', 'total_workouts_completed', '=', 100, 500, 57, TRUE),
                                                                                                                                                                                 ('THOUSAND_STRONG', 'Thousand Strong', 'Complete your 1,000th workout milestone', '👑', 'SPECIAL_HIDDEN', 'RARE', 'total_workouts_completed', '=', 1000, 2000, 58, TRUE),
                                                                                                                                                                                 ('TRIPLE_THREAT', 'Triple Threat', 'Complete 3 workouts on 3/3, 6/6, 9/9, or 12/12', '3️⃣', 'SPECIAL_HIDDEN', 'RARE', 'special_event', '>=', 3, 800, 59, TRUE);

-- Speed & Duration (2)
INSERT INTO achievements (achievement_key, name, description, icon, category, rarity, criteria_field, criteria_operator, criteria_value, bonus_xp, display_order, is_hidden) VALUES
                                                                                                                                                                                 ('SPEED_DEMON', 'Speed Demon', 'Complete a workout in under 15 minutes', '⚡', 'SPECIAL_HIDDEN', 'COMMON', 'special_event', '=', 1, 200, 60, TRUE),
                                                                                                                                                                                 ('MARATHON_SESSION', 'Marathon Session', 'Complete a workout over 2 hours', '⏰', 'SPECIAL_HIDDEN', 'UNCOMMON', 'special_event', '=', 1, 600, 61, TRUE);

-- Random & Fun (3)
INSERT INTO achievements (achievement_key, name, description, icon, category, rarity, criteria_field, criteria_operator, criteria_value, bonus_xp, display_order, is_hidden) VALUES
                                                                                                                                                                                 ('FIRST_OF_MONTH', 'First of the Month', 'Workout on the 1st of any month, 12 times', '📅', 'SPECIAL_HIDDEN', 'RARE', 'first_of_month_count', '>=', 12, 1200, 62, TRUE),
                                                                                                                                                                                 ('WEEKEND_WARRIOR', 'Weekend Warrior', 'Complete 52 weekend workouts in a year', '🎉', 'SPECIAL_HIDDEN', 'RARE', 'weekend_workout_count', '>=', 52, 1500, 63, TRUE),
                                                                                                                                                                                 ('COMEBACK_KID', 'Comeback Kid', 'Return to workout after 30+ day break', '🔙', 'SPECIAL_HIDDEN', 'UNCOMMON', 'special_event', '=', 1, 500, 64, TRUE);

-- ============================================================================
-- CATEGORY 8: CARDIO DISTANCE (6 achievements)
-- ============================================================================
INSERT INTO achievements (achievement_key, name, description, icon, category, rarity, criteria_field, criteria_operator, criteria_value, bonus_xp, display_order) VALUES
                                                                                                                                                                      ('DISTANCE_5K', 'First Mile', 'Run/bike 5 km total distance', '🏃', 'CARDIO_DISTANCE', 'COMMON', 'total_distance_km', '>=', 5, 100, 65),
                                                                                                                                                                      ('DISTANCE_25K', 'Road Warrior', 'Run/bike 25 km total distance', '🛣️', 'CARDIO_DISTANCE', 'COMMON', 'total_distance_km', '>=', 25, 200, 66),
                                                                                                                                                                      ('DISTANCE_100K', 'Distance Seeker', 'Run/bike 100 km total distance', '🗺️', 'CARDIO_DISTANCE', 'UNCOMMON', 'total_distance_km', '>=', 100, 500, 67),
                                                                                                                                                                      ('DISTANCE_250K', 'Marathon Ready', 'Run/bike 250 km total distance', '🏃', 'CARDIO_DISTANCE', 'UNCOMMON', 'total_distance_km', '>=', 250, 1000, 68),
                                                                                                                                                                      ('DISTANCE_500K', 'Ultra Runner', 'Run/bike 500 km total distance', '🏆', 'CARDIO_DISTANCE', 'RARE', 'total_distance_km', '>=', 500, 2500, 69),
                                                                                                                                                                      ('DISTANCE_1000K', 'Around the World', 'Run/bike 1,000 km total distance', '🌍', 'CARDIO_DISTANCE', 'RARE', 'total_distance_km', '>=', 1000, 5000, 70);

-- ============================================================================
-- CATEGORY 9: ISOMETRIC ENDURANCE (6 achievements)
-- ============================================================================
INSERT INTO achievements (achievement_key, name, description, icon, category, rarity, criteria_field, criteria_operator, criteria_value, bonus_xp, display_order) VALUES
                                                                                                                                                                      ('HOLD_10MIN', 'Steady Holder', 'Accumulate 10 minutes total hold time', '🧘', 'ISOMETRIC_ENDURANCE', 'COMMON', 'total_hold_seconds', '>=', 600, 100, 71),
                                                                                                                                                                      ('HOLD_1HOUR', 'Plank Master', 'Accumulate 1 hour total hold time', '💎', 'ISOMETRIC_ENDURANCE', 'COMMON', 'total_hold_seconds', '>=', 3600, 200, 72),
                                                                                                                                                                      ('HOLD_5HOURS', 'Iron Will', 'Accumulate 5 hours total hold time', '🛡️', 'ISOMETRIC_ENDURANCE', 'UNCOMMON', 'total_hold_seconds', '>=', 18000, 500, 73),
                                                                                                                                                                      ('HOLD_10HOURS', 'Statue', 'Accumulate 10 hours total hold time', '🗿', 'ISOMETRIC_ENDURANCE', 'UNCOMMON', 'total_hold_seconds', '>=', 36000, 1000, 74),
                                                                                                                                                                      ('HOLD_25HOURS', 'Immovable', 'Accumulate 25 hours total hold time', '⚓', 'ISOMETRIC_ENDURANCE', 'RARE', 'total_hold_seconds', '>=', 90000, 2500, 75),
                                                                                                                                                                      ('HOLD_50HOURS', 'The Pillar', 'Accumulate 50 hours total hold time', '🏛️', 'ISOMETRIC_ENDURANCE', 'RARE', 'total_hold_seconds', '>=', 180000, 5000, 76);

-- ============================================================================
-- CATEGORY 10: WORKOUT DIVERSITY (7 achievements)
-- ============================================================================
INSERT INTO achievements (achievement_key, name, description, icon, category, rarity, criteria_field, criteria_operator, criteria_value, bonus_xp, display_order) VALUES
                                                                                                                                                                      ('DIVERSITY_10', 'Explorer', 'Try 10 different exercises', '🗺️', 'WORKOUT_DIVERSITY', 'COMMON', 'unique_exercises_tried', '>=', 10, 200, 77),
                                                                                                                                                                      ('DIVERSITY_25', 'Versatile', 'Try 25 different exercises', '🎨', 'WORKOUT_DIVERSITY', 'UNCOMMON', 'unique_exercises_tried', '>=', 25, 500, 78),
                                                                                                                                                                      ('DIVERSITY_50', 'Renaissance Athlete', 'Try 50 different exercises', '🎭', 'WORKOUT_DIVERSITY', 'RARE', 'unique_exercises_tried', '>=', 50, 1500, 79),
                                                                                                                                                                      ('CARDIO_SPECIALIST', 'Cardio Enthusiast', 'Complete 25 cardio workouts', '🏃', 'WORKOUT_DIVERSITY', 'UNCOMMON', 'cardio_workouts_completed', '>=', 25, 500, 80),
                                                                                                                                                                      ('STRENGTH_SPECIALIST', 'Strength Specialist', 'Complete 100 strength workouts', '💪', 'WORKOUT_DIVERSITY', 'RARE', 'strength_workouts_completed', '>=', 100, 2000, 81),
                                                                                                                                                                      ('BALANCE_SPECIALIST', 'Balance Master', 'Complete 25 isometric workouts', '🧘', 'WORKOUT_DIVERSITY', 'UNCOMMON', 'isometric_workouts_completed', '>=', 25, 500, 82),
                                                                                                                                                                      ('JACK_OF_TRADES', 'Jack of All Trades', 'Complete 10+ strength, 10+ cardio, 10+ isometric', '🎯', 'WORKOUT_DIVERSITY', 'RARE', 'special_event', '=', 1, 2000, 83);

-- ============================================================================
-- END OF MIGRATION
-- ============================================================================