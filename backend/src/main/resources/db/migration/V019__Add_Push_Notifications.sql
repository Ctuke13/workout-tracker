-- =============================================================================
-- V019: Push Notification Support
-- =============================================================================
-- Adds:
--   1. device_tokens table  — stores FCM tokens per user/platform
--   2. Granular notification preference columns on users table
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. DEVICE TOKENS
-- -----------------------------------------------------------------------------
-- Stores one or more FCM tokens per user (they may use multiple devices).
-- platform: WEB | ANDROID | IOS — needed for React Native migration later.
-- active: set to false on logout or token refresh rather than deleting rows,
--         so we keep a clean audit trail.
-- -----------------------------------------------------------------------------

CREATE TABLE device_tokens (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    token        TEXT         NOT NULL,
    platform     VARCHAR(10)  NOT NULL DEFAULT 'WEB'
                                CHECK (platform IN ('WEB', 'ANDROID', 'IOS')),
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW(),

    -- One active token per user per platform (deactivated tokens are kept)
    CONSTRAINT uq_device_token UNIQUE (user_id, token)
);

CREATE INDEX idx_device_tokens_user_id ON device_tokens(user_id);
CREATE INDEX idx_device_tokens_active  ON device_tokens(user_id, active) WHERE active = TRUE;

-- -----------------------------------------------------------------------------
-- 2. GRANULAR NOTIFICATION PREFERENCES ON USERS
-- -----------------------------------------------------------------------------
-- Each column maps to one of the 7 notification categories.
-- Defaults match the plan: social/leaderboard off, everything else on.
-- -----------------------------------------------------------------------------

ALTER TABLE users
    ADD COLUMN notif_pet_health          BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN notif_streak_reminders    BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN notif_achievements        BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN notif_rank_season         BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN notif_weekly_summary      BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN notif_social_leaderboard  BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN notif_reengagement        BOOLEAN NOT NULL DEFAULT TRUE;

-- -----------------------------------------------------------------------------
-- COMMENTS
-- -----------------------------------------------------------------------------

COMMENT ON TABLE  device_tokens                        IS 'FCM device tokens for push notifications. Supports WEB, ANDROID, IOS for React Native migration.';
COMMENT ON COLUMN device_tokens.platform               IS 'WEB | ANDROID | IOS';
COMMENT ON COLUMN device_tokens.active                 IS 'False when user logs out or token is refreshed. Row kept for audit trail.';

COMMENT ON COLUMN users.notif_pet_health               IS 'Pet fuel/fatigue/cleanliness/motivation alerts';
COMMENT ON COLUMN users.notif_streak_reminders         IS 'Streak at risk and workout reminders';
COMMENT ON COLUMN users.notif_achievements             IS 'Achievement unlocks, level-ups, personal records';
COMMENT ON COLUMN users.notif_rank_season              IS 'Rank changes and season start/end alerts';
COMMENT ON COLUMN users.notif_weekly_summary           IS 'Weekly workout recap digest';
COMMENT ON COLUMN users.notif_social_leaderboard       IS 'Leaderboard position changes (off by default)';
COMMENT ON COLUMN users.notif_reengagement             IS 'Re-engagement nudges for lapsed users';