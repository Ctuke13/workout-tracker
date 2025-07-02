package com.chidituke.workout_tracker.model.messaging.enums;

/**
 * Enumeration for participant roles in conversations
 * Supports current direct messaging with future group conversation roles
 */
public enum ParticipantRole {

    /**
     * Regular member of the conversation
     * Default role for all participants in direct messages
     */
    MEMBER("Member", "👤"),

    /**
     * Administrator of group conversations (future feature)
     * Can add/remove participants, change conversation settings
     */
    ADMIN("Admin", "👑"),

    /**
     * Moderator of group conversations (future feature)
     * Can moderate content but not manage membership
     */
    MODERATOR("Moderator", "🛡️"),

    /**
     * Owner/Creator of the conversation (future feature)
     * Full control over conversation, cannot be removed
     */
    OWNER("Owner", "⭐");

    private final String displayName;
    private final String emoji;

    ParticipantRole(String displayName, String emoji) {
        this.displayName = displayName;
        this.emoji = emoji;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getDisplayWithEmoji() {
        return emoji + " " + displayName;
    }

    /**
     * Check if this role can send messages
     */
    public boolean canSendMessages() {
        return true; // All roles can send messages
    }

    /**
     * Check if this role can add participants to group conversations
     */
    public boolean canAddParticipants() {
        return this == ADMIN || this == OWNER;
    }

    /**
     * Check if this role can remove participants from group conversations
     */
    public boolean canRemoveParticipants() {
        return this == ADMIN || this == OWNER;
    }

    /**
     * Check if this role can moderate messages (delete, edit)
     */
    public boolean canModerateMessages() {
        return this == MODERATOR || this == ADMIN || this == OWNER;
    }

    /**
     * Check if this role can change conversation settings
     */
    public boolean canChangeSettings() {
        return this == ADMIN || this == OWNER;
    }

    /**
     * Check if this role can promote other participants
     */
    public boolean canPromoteParticipants() {
        return this == OWNER;
    }

    /**
     * Check if this participant can be removed from the conversation
     */
    public boolean canBeRemoved() {
        return this != OWNER; // Owners cannot be removed
    }

    /**
     * Check if this is an elevated role (admin-level permissions)
     */
    public boolean isElevatedRole() {
        return this == ADMIN || this == MODERATOR || this == OWNER;
    }

    /**
     * Check if this role is appropriate for direct conversations
     */
    public boolean isValidForDirectConversation() {
        return this == MEMBER; // Only members in direct conversations
    }

    /**
     * Check if this role is appropriate for group conversations
     */
    public boolean isValidForGroupConversation() {
        return true; // All roles valid for groups
    }

    /**
     * Get the priority level of this role (higher = more permissions)
     */
    public int getPriorityLevel() {
        return switch (this) {
            case MEMBER -> 1;
            case MODERATOR -> 2;
            case ADMIN -> 3;
            case OWNER -> 4;
        };
    }

    /**
     * Check if this role has higher priority than another role
     */
    public boolean hasHigherPriorityThan(ParticipantRole other) {
        return this.getPriorityLevel() > other.getPriorityLevel();
    }

    /**
     * Get default role for conversation type
     */
    public static ParticipantRole getDefaultRole(ConversationType conversationType) {
        return switch (conversationType) {
            case DIRECT -> MEMBER;    // Direct messages use MEMBER role
            case GROUP -> MEMBER;     // Group conversations default to MEMBER
        };
    }

    /**
     * Get creator role for conversation type
     */
    public static ParticipantRole getCreatorRole(ConversationType conversationType) {
        return switch (conversationType) {
            case DIRECT -> MEMBER;    // Direct messages use MEMBER role
            case GROUP -> OWNER;      // Group conversation creators are OWNERS
        };
    }

    /**
     * Get the database value for storage
     */
    public String getDatabaseValue() {
        return this.name();
    }

    /**
     * Create from database value
     */
    public static ParticipantRole fromDatabaseValue(String value) {
        try {
            return ParticipantRole.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return MEMBER; // Default fallback
        }
    }
}