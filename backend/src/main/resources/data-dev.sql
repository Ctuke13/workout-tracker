-- =============================================================================
-- Development Test Data - Forces Password Updates
-- =============================================================================

-- Test User 1: Complete profile with all valid enum values
INSERT INTO users (username, email, password, first_name, last_name,
                   nickname, pet_name, onboarding_completed,
                   date_of_birth, gender, zipcode, city, state, country,
                   phone_number, bio, user_type, subscription_tier, account_status,
                   privacy_settings, notification_settings, measurement_system,
                   activity_level, fitness_level, height_cm, weight_kg,
                   workout_frequency, fitness_goals, email_verified, enabled,
                   account_non_expired, account_non_locked, credentials_non_expired,
                   auto_suggest_workout_sharing, default_post_privacy, auto_share_achievements,
                   allow_mentions, show_workout_stats_in_posts, allow_comments_on_posts,
                   moderate_comments, show_activity_status, allow_friend_requests,
                   auto_accept_follow_requests, created_at, updated_at)
VALUES ('testuser', 'test@example.com',
        '$2a$10$ub432U6KDccTEHiJKyKOguBPWSiBE4QuUdjMPnuFFWFljC/3JY0rO',
        'Test', 'User',
        NULL, NULL, false,
        '1990-01-01', 'MALE', '12345', 'Test City', 'NY', 'US',
        '+1234567890', 'Test user for development', 'REGULAR', 'FREE', 'ACTIVE',
        'PUBLIC', 'ALL', 'METRIC',
        'MODERATELY_ACTIVE', 'INTERMEDIATE', 180, 75.0,
        'REGULARLY', 'BUILD_MUSCLE,LOSE_WEIGHT', true, true,
        true, true, true,
        false, 'PUBLIC', true,
        true, true, true,
        false, true, true,
        false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT (username) DO UPDATE SET
    password = EXCLUDED.password,
                                  updated_at = CURRENT_TIMESTAMP;

-- Test User 2: Minimal profile with required fields only
INSERT INTO users (username, email, password, first_name, last_name,
                   nickname, pet_name, onboarding_completed,
                   user_type, subscription_tier, account_status,
                   privacy_settings, notification_settings, measurement_system,
                   email_verified, enabled, account_non_expired,
                   account_non_locked, credentials_non_expired,
                   created_at, updated_at)
VALUES ('simpleuser', 'simple@example.com',
        '$2a$10$ub432U6KDccTEHiJKyKOguBPWSiBE4QuUdjMPnuFFWFljC/3JY0rO',
        'Simple', 'User',
        NULL, NULL, false,
        'REGULAR', 'FREE', 'ACTIVE',
        'PUBLIC', 'ALL', 'METRIC',
        true, true, true, true, true,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT (username) DO UPDATE SET
    password = EXCLUDED.password,
                                  updated_at = CURRENT_TIMESTAMP;

-- Test User 3: Professional user
INSERT INTO users (username, email, password, first_name, last_name,
                   nickname, pet_name, onboarding_completed,
                   subscription_tier, user_type, account_status,
                   privacy_settings, notification_settings, measurement_system,
                   fitness_level, activity_level, height_cm, weight_kg,
                   workout_frequency, fitness_goals, email_verified, enabled,
                   account_non_expired, account_non_locked, credentials_non_expired,
                   created_at, updated_at)
VALUES ('traineruser', 'trainer@example.com',
        '$2a$10$ub432U6KDccTEHiJKyKOguBPWSiBE4QuUdjMPnuFFWFljC/3JY0rO',
        'Professional', 'Trainer',
        NULL, NULL, false,
        'PRO_PROFESSIONAL', 'PROFESSIONAL', 'ACTIVE',
        'PUBLIC', 'ALL', 'METRIC',
        'ADVANCED', 'VERY_ACTIVE', 175, 70.0,
        'DAILY', 'BUILD_MUSCLE,IMPROVE_ENDURANCE', true, true,
        true, true, true,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT (username) DO UPDATE SET
    password = EXCLUDED.password,
                                  updated_at = CURRENT_TIMESTAMP;

-- Development data loaded successfully
SELECT 'Development test data loaded successfully!' as status;