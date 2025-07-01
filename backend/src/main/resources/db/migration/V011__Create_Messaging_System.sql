-- ============================================================================
-- V011: Create Messaging System
-- Description: Core messaging tables for 1-on-1 conversations with future group support
-- ============================================================================

-- ============================================================================
-- CONVERSATIONS TABLE
-- Supports both direct (1-on-1) and future group conversations
-- ============================================================================
CREATE TABLE conversations (
    conversation_id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL DEFAULT 'DIRECT', -- 'DIRECT' or 'GROUP'
    name VARCHAR(100), -- NULL for direct chats, name for groups
    created_by_user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- CONVERSATION PARTICIPANTS TABLE
-- Tracks which users are in which conversations (supports any number of users)
-- ============================================================================
CREATE TABLE conversation_participants (
    participant_id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations(conversation_id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER', -- 'ADMIN', 'MEMBER'
    is_starred BOOLEAN NOT NULL DEFAULT false, -- User's starred conversations
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMP NULL, -- NULL = still active participant

    -- Constraints
    CONSTRAINT unique_active_participant UNIQUE(conversation_id, user_id, left_at)
);

-- ============================================================================
-- MESSAGES TABLE
-- Individual messages within conversations
-- ============================================================================
CREATE TABLE messages (
    message_id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations(conversation_id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

    -- Message content
    content TEXT NOT NULL,
    message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT', -- 'TEXT', 'IMAGE', 'VIDEO', 'LINK', 'WORKOUT'
    media_url VARCHAR(500), -- URL for images/videos/files
    media_size_bytes BIGINT, -- File size tracking

    -- Content moderation
    is_filtered BOOLEAN NOT NULL DEFAULT false, -- Auto-filtered content
    filter_reason VARCHAR(100), -- Why it was filtered

    -- Workout/professional context
    shared_workout_session_id BIGINT REFERENCES workout_sessions(workout_session_id) ON DELETE SET NULL,
    shared_workout_plan_id BIGINT REFERENCES workout_plans(workout_plan_id) ON DELETE SET NULL,

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- MESSAGE REQUESTS TABLE
-- Handles message requests from non-connected users
-- ============================================================================
CREATE TABLE message_requests (
    request_id BIGSERIAL PRIMARY KEY,
    from_user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    to_user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    message_content TEXT NOT NULL,
    request_type VARCHAR(20) NOT NULL DEFAULT 'GENERAL', -- 'GENERAL', 'PROFESSIONAL_INQUIRY'
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'ACCEPTED', 'DECLINED', 'EXPIRED'

    -- Auto-expire after 30 days
    expires_at TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL '30 days'),

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT unique_pending_request UNIQUE(from_user_id, to_user_id, status)
        DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT no_self_requests CHECK (from_user_id != to_user_id)
);

-- ============================================================================
-- BLOCKED CONVERSATIONS TABLE
-- Track blocked conversations (extends UserRelationship blocking)
-- ============================================================================
CREATE TABLE blocked_conversations (
    block_id BIGSERIAL PRIMARY KEY,
    blocker_user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    blocked_user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    conversation_id BIGINT REFERENCES conversations(conversation_id) ON DELETE CASCADE,
    reason VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT unique_conversation_block UNIQUE(blocker_user_id, blocked_user_id),
    CONSTRAINT no_self_blocking CHECK (blocker_user_id != blocked_user_id)
);

-- ============================================================================
-- PERFORMANCE INDEXES
-- Optimized for common messaging queries
-- ============================================================================

-- Conversation indexes
CREATE INDEX idx_conversations_created_by ON conversations(created_by_user_id);
CREATE INDEX idx_conversations_updated_desc ON conversations(updated_at DESC);
CREATE INDEX idx_conversations_type ON conversations(type);

-- Participant indexes
CREATE INDEX idx_participants_user_active ON conversation_participants(user_id)
    WHERE left_at IS NULL;
CREATE INDEX idx_participants_conversation ON conversation_participants(conversation_id)
    WHERE left_at IS NULL;
CREATE INDEX idx_participants_starred ON conversation_participants(user_id, is_starred)
    WHERE left_at IS NULL AND is_starred = true;

-- Message indexes (most important for performance)
CREATE INDEX idx_messages_conversation_created ON messages(conversation_id, created_at DESC);
CREATE INDEX idx_messages_sender ON messages(sender_id);
CREATE INDEX idx_messages_type ON messages(message_type);
CREATE INDEX idx_messages_shared_workout ON messages(shared_workout_session_id)
    WHERE shared_workout_session_id IS NOT NULL;

-- Message request indexes
CREATE INDEX idx_message_requests_to_user ON message_requests(to_user_id, status);
CREATE INDEX idx_message_requests_from_user ON message_requests(from_user_id);
CREATE INDEX idx_message_requests_expires ON message_requests(expires_at)
    WHERE status = 'PENDING';

-- Blocked conversation indexes
CREATE INDEX idx_blocked_conversations_blocker ON blocked_conversations(blocker_user_id);
CREATE INDEX idx_blocked_conversations_blocked ON blocked_conversations(blocked_user_id);

-- ============================================================================
-- TRIGGERS FOR AUTOMATIC UPDATES
-- ============================================================================

-- Update conversation.updated_at when new message is sent
CREATE OR REPLACE FUNCTION update_conversation_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE conversations
    SET updated_at = CURRENT_TIMESTAMP
    WHERE conversation_id = NEW.conversation_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_conversation_on_message
    AFTER INSERT ON messages
    FOR EACH ROW
    EXECUTE FUNCTION update_conversation_timestamp();

-- Auto-expire old message requests
CREATE OR REPLACE FUNCTION expire_old_message_requests()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'PENDING' AND NEW.expires_at < CURRENT_TIMESTAMP THEN
        NEW.status = 'EXPIRED';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_expire_message_requests
    BEFORE UPDATE ON message_requests
    FOR EACH ROW
    WHEN (OLD.status = 'PENDING')
    EXECUTE FUNCTION expire_old_message_requests();

-- ============================================================================
-- COMMENTS FOR DOCUMENTATION
-- ============================================================================

COMMENT ON TABLE conversations IS 'Core conversation container - supports both 1-on-1 and group messaging';
COMMENT ON TABLE conversation_participants IS 'Tracks user participation in conversations with roles and starring';
COMMENT ON TABLE messages IS 'Individual messages with support for rich content and workout sharing';
COMMENT ON TABLE message_requests IS 'Handles messaging between non-connected users';
COMMENT ON TABLE blocked_conversations IS 'Conversation-level blocking extending UserRelationship blocking';

COMMENT ON COLUMN conversations.type IS 'DIRECT for 1-on-1, GROUP for multi-user conversations';
COMMENT ON COLUMN conversation_participants.is_starred IS 'User-specific starring for priority conversations';
COMMENT ON COLUMN messages.message_type IS 'TEXT, IMAGE, VIDEO, LINK, WORKOUT for different content types';
COMMENT ON COLUMN message_requests.request_type IS 'GENERAL or PROFESSIONAL_INQUIRY for business messaging';

-- ============================================================================
-- INITIAL DATA / CONSTRAINTS
-- ============================================================================

-- Add constraint to ensure direct conversations have exactly 2 participants
-- (This will be enforced at application level for flexibility)

-- Ensure message requests don't conflict with existing relationships
-- (This will be handled in application logic using UserRelationship checks)