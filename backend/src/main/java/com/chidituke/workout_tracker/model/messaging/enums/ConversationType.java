package com.chidituke.workout_tracker.model.messaging.enums;

/**
 * Enumeration for different types of conversations
 * Supports current 1-on-1 messaging with future group messaging capability
 */
public enum ConversationType {

    /**
     * Direct conversation between exactly 2 users
     * Used for all current 1-on-1 messaging
     */
    DIRECT("Direct Message", "💬", 2, 2),

    /**
     * Group conversation with multiple users (future feature)
     * Reserved for when group messaging is implemented
     */
    GROUP("Group Chat", "👥", 3, 100);

    private final String displayName;
    private final String emoji;
    private final int minParticipants;
    private final int maxParticipants;

    ConversationType(String displayName, String emoji, int minParticipants, int maxParticipants) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.minParticipants = minParticipants;
        this.maxParticipants = maxParticipants;
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

    public int getMinParticipants() {
        return minParticipants;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    /**
     * Check if participant count is valid for this conversation type
     */
    public boolean isValidParticipantCount(int count) {
        return count >= minParticipants && count <= maxParticipants;
    }

    /**
     * Check if this conversation type supports adding more participants
     */
    public boolean canAddParticipants(int currentCount) {
        return currentCount < maxParticipants;
    }

    /**
     * Check if this conversation type supports removing participants
     */
    public boolean canRemoveParticipants(int currentCount) {
        return currentCount > minParticipants;
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
    public static ConversationType fromDatabaseValue(String value) {
        try {
            return ConversationType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DIRECT; // Default fallback
        }
    }

    /**
     * Check if this is a direct (1-on-1) conversation
     */
    public boolean isDirect() {
        return this == DIRECT;
    }

    /**
     * Check if this is a group conversation
     */
    public boolean isGroup() {
        return this == GROUP;
    }
}