-- =============================================================================
-- V009__Create_Social_System.sql
-- Creates comprehensive social media system for workout tracker
-- Includes: Posts, Comments, Relationships, Engagement, Activity Tracking, Moderation
-- =============================================================================

-- =====================================================
-- SOCIAL POSTS TABLE
-- =====================================================

CREATE TABLE social_posts (
                              social_post_id BIGSERIAL PRIMARY KEY,

    -- Author and relationships
                              author_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                              workout_session_id BIGINT REFERENCES workout_sessions(workout_session_id) ON DELETE SET NULL,

    -- Content
                              post_type VARCHAR(50) NOT NULL DEFAULT 'TEXT',
                              content TEXT,
                              media_url VARCHAR(500),
                              link_url VARCHAR(500),
                              link_title VARCHAR(200),
                              link_description VARCHAR(500),

    -- Privacy and visibility
                              privacy_level VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
                              is_active BOOLEAN NOT NULL DEFAULT TRUE,
                              is_featured BOOLEAN NOT NULL DEFAULT FALSE,

    -- Engagement metrics
                              likes_count INTEGER NOT NULL DEFAULT 0,
                              comments_count INTEGER NOT NULL DEFAULT 0,
                              shares_count INTEGER NOT NULL DEFAULT 0,
                              views_count INTEGER NOT NULL DEFAULT 0,

    -- Content moderation
                              moderation_status VARCHAR(20) NOT NULL DEFAULT 'APPROVED',
                              flagged_count INTEGER NOT NULL DEFAULT 0,
                              moderation_reason VARCHAR(500),
                              moderated_by_user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
                              moderated_at TIMESTAMP WITH TIME ZONE,

    -- Location and context
                              location VARCHAR(100),
                              workout_location VARCHAR(20),

    -- Timestamps
                              created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
                              CONSTRAINT chk_post_type CHECK (post_type IN ('TEXT', 'IMAGE', 'VIDEO', 'WORKOUT_COMPLETION', 'LINK', 'ACHIEVEMENT', 'MOTIVATION', 'PROGRESS_UPDATE', 'CHECK_IN')),
                              CONSTRAINT chk_privacy_level CHECK (privacy_level IN ('PUBLIC', 'FRIENDS_ONLY', 'PRIVATE')),
                              CONSTRAINT chk_moderation_status CHECK (moderation_status IN ('APPROVED', 'UNDER_REVIEW', 'REJECTED', 'AUTO_FLAGGED')),
                              CONSTRAINT chk_workout_location CHECK (workout_location IN ('HOME', 'GYM', 'PARK', 'OFFICE', 'HOTEL', 'BEACH', 'TRAIL', 'STUDIO', 'OTHER')),
                              CONSTRAINT chk_likes_count_positive CHECK (likes_count >= 0),
                              CONSTRAINT chk_comments_count_positive CHECK (comments_count >= 0),
                              CONSTRAINT chk_shares_count_positive CHECK (shares_count >= 0),
                              CONSTRAINT chk_views_count_positive CHECK (views_count >= 0),
                              CONSTRAINT chk_flagged_count_positive CHECK (flagged_count >= 0),
                              CONSTRAINT chk_content_or_media CHECK (
                                  content IS NOT NULL OR
                                  media_url IS NOT NULL OR
                                  link_url IS NOT NULL OR
                                  workout_session_id IS NOT NULL
                                  )
);

-- =====================================================
-- SOCIAL COMMENTS TABLE
-- =====================================================

CREATE TABLE social_comments (
                                 social_comment_id BIGSERIAL PRIMARY KEY,

    -- Relationships
                                 social_post_id BIGINT NOT NULL REFERENCES social_posts(social_post_id) ON DELETE CASCADE,
                                 author_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                                 parent_comment_id BIGINT REFERENCES social_comments(social_comment_id) ON DELETE CASCADE,

    -- Content
                                 content TEXT NOT NULL,

    -- Engagement
                                 likes_count INTEGER NOT NULL DEFAULT 0,
                                 replies_count INTEGER NOT NULL DEFAULT 0,

    -- Moderation
                                 is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                 flagged_count INTEGER NOT NULL DEFAULT 0,
                                 moderation_status VARCHAR(20) NOT NULL DEFAULT 'APPROVED',
                                 moderation_reason VARCHAR(500),
                                 moderated_by_user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
                                 moderated_at TIMESTAMP WITH TIME ZONE,

    -- Timestamps
                                 created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
                                 CONSTRAINT chk_comment_moderation_status CHECK (moderation_status IN ('APPROVED', 'UNDER_REVIEW', 'REJECTED', 'AUTO_FLAGGED')),
                                 CONSTRAINT chk_comment_likes_count_positive CHECK (likes_count >= 0),
                                 CONSTRAINT chk_comment_replies_count_positive CHECK (replies_count >= 0),
                                 CONSTRAINT chk_comment_flagged_count_positive CHECK (flagged_count >= 0),
                                 CONSTRAINT chk_content_not_empty CHECK (LENGTH(TRIM(content)) > 0),
                                 CONSTRAINT chk_content_max_length CHECK (LENGTH(content) <= 500)
);

-- =====================================================
-- USER RELATIONSHIPS TABLE
-- =====================================================

CREATE TABLE user_relationships (
                                    user_relationship_id BIGSERIAL PRIMARY KEY,

    -- Relationships
                                    follower_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                                    following_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

    -- Relationship type and status
                                    relationship_type VARCHAR(20) NOT NULL DEFAULT 'FOLLOW',
                                    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    -- Interaction preferences
                                    notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
                                    show_in_feed BOOLEAN NOT NULL DEFAULT TRUE,
                                    close_friend BOOLEAN NOT NULL DEFAULT FALSE,
                                    muted BOOLEAN NOT NULL DEFAULT FALSE,

    -- Interaction tracking
                                    interaction_score INTEGER NOT NULL DEFAULT 0,
                                    last_post_seen TIMESTAMP WITH TIME ZONE,
                                    total_interactions INTEGER NOT NULL DEFAULT 0,

    -- Timestamps
                                    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    last_interaction TIMESTAMP WITH TIME ZONE,

    -- Constraints
                                    CONSTRAINT chk_relationship_type CHECK (relationship_type IN ('FOLLOW', 'FRIEND', 'BLOCKED')),
                                    CONSTRAINT chk_relationship_status CHECK (status IN ('ACTIVE', 'PENDING', 'INACTIVE', 'SUSPENDED')),
                                    CONSTRAINT chk_interaction_score_positive CHECK (interaction_score >= 0),
                                    CONSTRAINT chk_total_interactions_positive CHECK (total_interactions >= 0),
                                    CONSTRAINT chk_no_self_follow CHECK (follower_id != following_id),

    -- Unique constraint to prevent duplicate relationships
    UNIQUE(follower_id, following_id)
);

-- =====================================================
-- HASHTAGS AND MENTIONS TABLES
-- =====================================================

-- Post hashtags table (updated to match PostHashtag entity)
CREATE TABLE post_hashtags (
                               post_hashtag_id BIGSERIAL PRIMARY KEY,
                               post_id BIGINT NOT NULL REFERENCES social_posts(social_post_id) ON DELETE CASCADE,
                               hashtag VARCHAR(100) NOT NULL,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT chk_hashtag_length CHECK (LENGTH(hashtag) >= 1 AND LENGTH(hashtag) <= 100),
                               CONSTRAINT chk_hashtag_format CHECK (hashtag ~ '^[a-zA-Z0-9_]+$')
    );

-- Post mentions table
CREATE TABLE social_post_mentions (
                                      social_post_id BIGINT NOT NULL REFERENCES social_posts(social_post_id) ON DELETE CASCADE,
                                      mentioned_user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

                                      PRIMARY KEY (social_post_id, mentioned_user_id)
);

-- Comment mentions table
CREATE TABLE comment_mentions (
                                  comment_id BIGINT NOT NULL REFERENCES social_comments(social_comment_id) ON DELETE CASCADE,
                                  mentioned_user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

                                  PRIMARY KEY (comment_id, mentioned_user_id)
);

-- =====================================================
-- ENGAGEMENT TABLES (likes, shares)
-- =====================================================

CREATE TABLE post_likes (
                            post_like_id BIGSERIAL PRIMARY KEY,
                            social_post_id BIGINT NOT NULL REFERENCES social_posts(social_post_id) ON DELETE CASCADE,
                            user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                            created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            UNIQUE(social_post_id, user_id)
);

CREATE TABLE comment_likes (
                               social_comment_id BIGINT NOT NULL REFERENCES social_comments(social_comment_id) ON DELETE CASCADE,
                               user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               PRIMARY KEY (social_comment_id, user_id)
);

CREATE TABLE post_shares (
                             post_share_id BIGSERIAL PRIMARY KEY,
                             social_post_id BIGINT NOT NULL REFERENCES social_posts(social_post_id) ON DELETE CASCADE,
                             shared_by_user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                             shared_to_platform VARCHAR(50) DEFAULT 'INTERNAL',
                             created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT chk_shared_to_platform CHECK (shared_to_platform IN ('INTERNAL', 'FACEBOOK', 'TWITTER', 'INSTAGRAM', 'OTHER'))
);

-- =====================================================
-- CONTENT REPORTS TABLE (for moderation)
-- =====================================================

CREATE TABLE content_reports (
                                 content_report_id BIGSERIAL PRIMARY KEY,

    -- Reporter and moderation
                                 reporter_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                                 reviewed_by_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,

    -- Reported content (one of these must be non-null)
                                 reported_post_id BIGINT REFERENCES social_posts(social_post_id) ON DELETE CASCADE,
                                 reported_comment_id BIGINT REFERENCES social_comments(social_comment_id) ON DELETE CASCADE,

    -- Report details
                                 report_type VARCHAR(50) NOT NULL,
                                 status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
                                 description VARCHAR(1000),
                                 moderator_notes VARCHAR(1000),
                                 reviewed_at TIMESTAMP WITH TIME ZONE,

    -- Timestamps
                                 created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
                                 CONSTRAINT chk_report_type CHECK (report_type IN ('SPAM', 'HARASSMENT', 'HATE_SPEECH', 'VIOLENCE', 'INAPPROPRIATE_CONTENT', 'COPYRIGHT_VIOLATION', 'MISINFORMATION', 'OTHER')),
                                 CONSTRAINT chk_report_status CHECK (status IN ('OPEN', 'UNDER_REVIEW', 'RESOLVED_VALID', 'RESOLVED_INVALID', 'DISMISSED')),
                                 CONSTRAINT chk_reported_content_exists CHECK (
                                     (reported_post_id IS NOT NULL AND reported_comment_id IS NULL) OR
                                     (reported_post_id IS NULL AND reported_comment_id IS NOT NULL)
                                     )
);

-- =====================================================
-- UPDATE USERS TABLE WITH SOCIAL FEATURES
-- =====================================================

-- Add social fields to users table
DO $$
BEGIN
    -- Social sharing preferences
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'auto_suggest_workout_sharing') THEN
ALTER TABLE users ADD COLUMN auto_suggest_workout_sharing BOOLEAN DEFAULT TRUE;
END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'default_post_privacy') THEN
ALTER TABLE users ADD COLUMN default_post_privacy VARCHAR(20) DEFAULT 'PUBLIC';
END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'auto_share_achievements') THEN
ALTER TABLE users ADD COLUMN auto_share_achievements BOOLEAN DEFAULT TRUE;
END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'allow_mentions') THEN
ALTER TABLE users ADD COLUMN allow_mentions BOOLEAN DEFAULT TRUE;
END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'show_workout_stats_in_posts') THEN
ALTER TABLE users ADD COLUMN show_workout_stats_in_posts BOOLEAN DEFAULT TRUE;
END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'allow_comments_on_posts') THEN
ALTER TABLE users ADD COLUMN allow_comments_on_posts BOOLEAN DEFAULT TRUE;
END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'moderate_comments') THEN
ALTER TABLE users ADD COLUMN moderate_comments BOOLEAN DEFAULT FALSE;
END IF;

    -- Social counters
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'followers_count') THEN
ALTER TABLE users ADD COLUMN followers_count INTEGER DEFAULT 0;
END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'following_count') THEN
ALTER TABLE users ADD COLUMN following_count INTEGER DEFAULT 0;
END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'posts_count') THEN
ALTER TABLE users ADD COLUMN posts_count INTEGER DEFAULT 0;
END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'total_likes_received') THEN
ALTER TABLE users ADD COLUMN total_likes_received INTEGER DEFAULT 0;
END IF;

    -- Social engagement settings
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'show_activity_status') THEN
ALTER TABLE users ADD COLUMN show_activity_status BOOLEAN DEFAULT TRUE;
END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'allow_friend_requests') THEN
ALTER TABLE users ADD COLUMN allow_friend_requests BOOLEAN DEFAULT TRUE;
END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'auto_accept_follow_requests') THEN
ALTER TABLE users ADD COLUMN auto_accept_follow_requests BOOLEAN DEFAULT TRUE;
END IF;
END $$;

-- Add constraints for new user fields
DO $$
BEGIN
    -- Add constraints only if they don't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'chk_default_post_privacy') THEN
ALTER TABLE users ADD CONSTRAINT chk_default_post_privacy CHECK (default_post_privacy IN ('PUBLIC', 'FRIENDS_ONLY', 'PRIVATE'));
END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'chk_followers_count_positive') THEN
ALTER TABLE users ADD CONSTRAINT chk_followers_count_positive CHECK (followers_count >= 0);
END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'chk_following_count_positive') THEN
ALTER TABLE users ADD CONSTRAINT chk_following_count_positive CHECK (following_count >= 0);
END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'chk_posts_count_positive') THEN
ALTER TABLE users ADD CONSTRAINT chk_posts_count_positive CHECK (posts_count >= 0);
END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'chk_total_likes_received_positive') THEN
ALTER TABLE users ADD CONSTRAINT chk_total_likes_received_positive CHECK (total_likes_received >= 0);
END IF;
END $$;

-- =====================================================
-- CREATE INDEXES FOR PERFORMANCE
-- =====================================================

-- Social posts indexes
CREATE INDEX IF NOT EXISTS idx_social_posts_author_created ON social_posts(author_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_social_posts_type ON social_posts(post_type);
CREATE INDEX IF NOT EXISTS idx_social_posts_privacy ON social_posts(privacy_level);
CREATE INDEX IF NOT EXISTS idx_social_posts_active ON social_posts(is_active);
CREATE INDEX IF NOT EXISTS idx_social_posts_workout_session ON social_posts(workout_session_id);
CREATE INDEX IF NOT EXISTS idx_social_posts_moderation ON social_posts(moderation_status);
CREATE INDEX IF NOT EXISTS idx_social_posts_featured ON social_posts(is_featured, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_social_posts_feed_query ON social_posts(author_id, privacy_level, is_active, created_at DESC);

-- Social comments indexes
CREATE INDEX IF NOT EXISTS idx_social_comments_post ON social_comments(social_post_id, created_at);
CREATE INDEX IF NOT EXISTS idx_social_comments_author ON social_comments(author_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_social_comments_parent ON social_comments(parent_comment_id);
CREATE INDEX IF NOT EXISTS idx_social_comments_active ON social_comments(is_active);

-- User relationships indexes
CREATE INDEX IF NOT EXISTS idx_user_relationships_follower ON user_relationships(follower_id, status);
CREATE INDEX IF NOT EXISTS idx_user_relationships_following ON user_relationships(following_id, status);
CREATE INDEX IF NOT EXISTS idx_user_relationships_type ON user_relationships(relationship_type, status);
CREATE INDEX IF NOT EXISTS idx_user_relationships_feed ON user_relationships(following_id, show_in_feed, status);
CREATE INDEX IF NOT EXISTS idx_user_relationships_interaction_score ON user_relationships(interaction_score DESC);

-- Post hashtags indexes (updated for new table structure)
CREATE INDEX IF NOT EXISTS idx_post_hashtags_post ON post_hashtags(post_id);
CREATE INDEX IF NOT EXISTS idx_post_hashtags_hashtag ON post_hashtags(hashtag);
CREATE INDEX IF NOT EXISTS idx_post_hashtags_hashtag_created ON post_hashtags(hashtag, created_at);

-- Post and comment mentions indexes
CREATE INDEX IF NOT EXISTS idx_social_post_mentions_user ON social_post_mentions(mentioned_user_id);
CREATE INDEX IF NOT EXISTS idx_comment_mentions_comment ON comment_mentions(comment_id);
CREATE INDEX IF NOT EXISTS idx_comment_mentions_user ON comment_mentions(mentioned_user_id);

-- Engagement indexes
CREATE INDEX IF NOT EXISTS idx_post_likes_user ON post_likes(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_comment_likes_user ON comment_likes(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_post_shares_user ON post_shares(shared_by_user_id, created_at DESC);

-- Content reports indexes
CREATE INDEX IF NOT EXISTS idx_content_reports_reporter ON content_reports(reporter_id);
CREATE INDEX IF NOT EXISTS idx_content_reports_post ON content_reports(reported_post_id);
CREATE INDEX IF NOT EXISTS idx_content_reports_comment ON content_reports(reported_comment_id);
CREATE INDEX IF NOT EXISTS idx_content_reports_status ON content_reports(status);
CREATE INDEX IF NOT EXISTS idx_content_reports_type_status ON content_reports(report_type, status);

-- Activity status indexes
CREATE INDEX IF NOT EXISTS idx_users_last_active ON users(last_active DESC) WHERE show_activity_status = TRUE;

-- =====================================================
-- TRIGGERS FOR AUTOMATIC COUNTER UPDATES
-- =====================================================

-- Function to update post engagement counters
CREATE OR REPLACE FUNCTION update_post_counters() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        -- Update likes count
        IF TG_TABLE_NAME = 'post_likes' THEN
UPDATE social_posts SET likes_count = likes_count + 1 WHERE social_post_id = NEW.social_post_id;
-- Update user's total likes received
UPDATE users SET total_likes_received = total_likes_received + 1
WHERE user_id = (SELECT author_id FROM social_posts WHERE social_post_id = NEW.social_post_id);
END IF;

        -- Update shares count
        IF TG_TABLE_NAME = 'post_shares' THEN
UPDATE social_posts SET shares_count = shares_count + 1 WHERE social_post_id = NEW.social_post_id;
END IF;

RETURN NEW;
ELSIF TG_OP = 'DELETE' THEN
        -- Update likes count
        IF TG_TABLE_NAME = 'post_likes' THEN
UPDATE social_posts SET likes_count = GREATEST(0, likes_count - 1) WHERE social_post_id = OLD.social_post_id;
-- Update user's total likes received
UPDATE users SET total_likes_received = GREATEST(0, total_likes_received - 1)
WHERE user_id = (SELECT author_id FROM social_posts WHERE social_post_id = OLD.social_post_id);
END IF;

        -- Update shares count
        IF TG_TABLE_NAME = 'post_shares' THEN
UPDATE social_posts SET shares_count = GREATEST(0, shares_count - 1) WHERE social_post_id = OLD.social_post_id;
END IF;

RETURN OLD;
END IF;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Function to update comment counters
CREATE OR REPLACE FUNCTION update_comment_counters() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        -- Update post comment count when new comment is added
        IF TG_TABLE_NAME = 'social_comments' THEN
UPDATE social_posts SET comments_count = comments_count + 1 WHERE social_post_id = NEW.social_post_id;

-- Update parent comment reply count if this is a reply
IF NEW.parent_comment_id IS NOT NULL THEN
UPDATE social_comments SET replies_count = replies_count + 1 WHERE social_comment_id = NEW.parent_comment_id;
END IF;
END IF;

        -- Update comment likes count
        IF TG_TABLE_NAME = 'comment_likes' THEN
UPDATE social_comments SET likes_count = likes_count + 1 WHERE social_comment_id = NEW.social_comment_id;
END IF;

RETURN NEW;
ELSIF TG_OP = 'DELETE' THEN
        -- Update post comment count when comment is deleted
        IF TG_TABLE_NAME = 'social_comments' THEN
UPDATE social_posts SET comments_count = GREATEST(0, comments_count - 1) WHERE social_post_id = OLD.social_post_id;

-- Update parent comment reply count if this was a reply
IF OLD.parent_comment_id IS NOT NULL THEN
UPDATE social_comments SET replies_count = GREATEST(0, replies_count - 1) WHERE social_comment_id = OLD.parent_comment_id;
END IF;
END IF;

        -- Update comment likes count
        IF TG_TABLE_NAME = 'comment_likes' THEN
UPDATE social_comments SET likes_count = GREATEST(0, likes_count - 1) WHERE social_comment_id = OLD.social_comment_id;
END IF;

RETURN OLD;
END IF;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Function to update user relationship counters
CREATE OR REPLACE FUNCTION update_user_counters() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        -- Update follower/following counts
        IF TG_TABLE_NAME = 'user_relationships' AND NEW.relationship_type = 'FOLLOW' AND NEW.status = 'ACTIVE' THEN
UPDATE users SET following_count = following_count + 1 WHERE user_id = NEW.follower_id;
UPDATE users SET followers_count = followers_count + 1 WHERE user_id = NEW.following_id;
END IF;

        -- Update posts count
        IF TG_TABLE_NAME = 'social_posts' AND NEW.is_active = TRUE THEN
UPDATE users SET posts_count = posts_count + 1 WHERE user_id = NEW.author_id;
END IF;

RETURN NEW;
ELSIF TG_OP = 'DELETE' THEN
        -- Update follower/following counts
        IF TG_TABLE_NAME = 'user_relationships' AND OLD.relationship_type = 'FOLLOW' AND OLD.status = 'ACTIVE' THEN
UPDATE users SET following_count = GREATEST(0, following_count - 1) WHERE user_id = OLD.follower_id;
UPDATE users SET followers_count = GREATEST(0, followers_count - 1) WHERE user_id = OLD.following_id;
END IF;

        -- Update posts count
        IF TG_TABLE_NAME = 'social_posts' AND OLD.is_active = TRUE THEN
UPDATE users SET posts_count = GREATEST(0, posts_count - 1) WHERE user_id = OLD.author_id;
END IF;

RETURN OLD;
ELSIF TG_OP = 'UPDATE' THEN
        -- Handle status changes in relationships
        IF TG_TABLE_NAME = 'user_relationships' AND OLD.relationship_type = 'FOLLOW' THEN
            -- If relationship became active
            IF OLD.status != 'ACTIVE' AND NEW.status = 'ACTIVE' THEN
UPDATE users SET following_count = following_count + 1 WHERE user_id = NEW.follower_id;
UPDATE users SET followers_count = followers_count + 1 WHERE user_id = NEW.following_id;
-- If relationship became inactive
ELSIF OLD.status = 'ACTIVE' AND NEW.status != 'ACTIVE' THEN
UPDATE users SET following_count = GREATEST(0, following_count - 1) WHERE user_id = NEW.follower_id;
UPDATE users SET followers_count = GREATEST(0, followers_count - 1) WHERE user_id = NEW.following_id;
END IF;
END IF;

        -- Handle post activation/deactivation
        IF TG_TABLE_NAME = 'social_posts' THEN
            IF OLD.is_active = FALSE AND NEW.is_active = TRUE THEN
UPDATE users SET posts_count = posts_count + 1 WHERE user_id = NEW.author_id;
ELSIF OLD.is_active = TRUE AND NEW.is_active = FALSE THEN
UPDATE users SET posts_count = GREATEST(0, posts_count - 1) WHERE user_id = NEW.author_id;
END IF;
END IF;

RETURN NEW;
END IF;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Create triggers
DROP TRIGGER IF EXISTS trigger_post_likes_counter ON post_likes;
CREATE TRIGGER trigger_post_likes_counter AFTER INSERT OR DELETE ON post_likes
    FOR EACH ROW EXECUTE FUNCTION update_post_counters();

DROP TRIGGER IF EXISTS trigger_post_shares_counter ON post_shares;
CREATE TRIGGER trigger_post_shares_counter AFTER INSERT OR DELETE ON post_shares
    FOR EACH ROW EXECUTE FUNCTION update_post_counters();

DROP TRIGGER IF EXISTS trigger_comment_likes_counter ON comment_likes;
CREATE TRIGGER trigger_comment_likes_counter AFTER INSERT OR DELETE ON comment_likes
    FOR EACH ROW EXECUTE FUNCTION update_comment_counters();

DROP TRIGGER IF EXISTS trigger_comment_counters ON social_comments;
CREATE TRIGGER trigger_comment_counters AFTER INSERT OR DELETE ON social_comments
    FOR EACH ROW EXECUTE FUNCTION update_comment_counters();

DROP TRIGGER IF EXISTS trigger_user_relationships_counter ON user_relationships;
CREATE TRIGGER trigger_user_relationships_counter AFTER INSERT OR UPDATE OR DELETE ON user_relationships
    FOR EACH ROW EXECUTE FUNCTION update_user_counters();

DROP TRIGGER IF EXISTS trigger_social_posts_counter ON social_posts;
CREATE TRIGGER trigger_social_posts_counter AFTER INSERT OR UPDATE OR DELETE ON social_posts
    FOR EACH ROW EXECUTE FUNCTION update_user_counters();

-- =====================================================
-- DATA VALIDATION AND INTEGRITY CHECKS
-- =====================================================

-- Function to validate data integrity
CREATE OR REPLACE FUNCTION validate_social_system_integrity() RETURNS BOOLEAN AS $$
DECLARE
integrity_issues INTEGER := 0;
BEGIN
    -- Check for orphaned data
    PERFORM 1 FROM social_posts WHERE author_id NOT IN (SELECT user_id FROM users);
    IF FOUND THEN
        integrity_issues := integrity_issues + 1;
        RAISE WARNING 'Found orphaned social posts without valid authors';
END IF;

    PERFORM 1 FROM social_comments WHERE social_post_id NOT IN (SELECT social_post_id FROM social_posts);
    IF FOUND THEN
        integrity_issues := integrity_issues + 1;
        RAISE WARNING 'Found orphaned comments without valid posts';
END IF;

    PERFORM 1 FROM user_relationships WHERE follower_id = following_id;
    IF FOUND THEN
        integrity_issues := integrity_issues + 1;
        RAISE WARNING 'Found self-follow relationships';
END IF;

RETURN integrity_issues = 0;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- INITIAL DATA SETUP
-- =====================================================

-- Update table statistics for query optimization
ANALYZE social_posts;
ANALYZE social_comments;
ANALYZE user_relationships;
ANALYZE users;

-- Create a welcome post if admin user exists
INSERT INTO social_posts (author_id, post_type, content, privacy_level, is_active)
SELECT user_id, 'TEXT', '🎉 Welcome to the new social features! Share your fitness journey with friends and get motivated together!', 'PUBLIC', TRUE
FROM users
WHERE username = 'admin' OR user_id = 1
    LIMIT 1;

-- Migration completed successfully
DO $$
BEGIN
    RAISE NOTICE 'V009 Social System Migration completed successfully!';
    RAISE NOTICE 'Created: social_posts, social_comments, user_relationships, post_hashtags, content_reports tables';
    RAISE NOTICE 'Added: engagement tracking, automatic counters, performance indexes';
    RAISE NOTICE 'Updated: users table with social features';

    -- Validate integrity
    IF validate_social_system_integrity() THEN
        RAISE NOTICE 'Social system integrity validation: PASSED ✅';
ELSE
        RAISE WARNING 'Social system integrity validation: ISSUES FOUND ⚠️';
END IF;
END $$;