package com.chidituke.workout_tracker.model.messaging.enums;

/**
 * Enumeration for message request statuses
 * Handles the lifecycle of message requests from non-connected users
 */
public enum RequestStatus {

    /**
     * Request has been sent and is waiting for response
     */
    PENDING("Pending", "⏳", true, false),

    /**
     * Request was accepted and conversation was created
     */
    ACCEPTED("Accepted", "✅", false, true),

    /**
     * Request was declined by the recipient
     */
    DECLINED("Declined", "❌", false, true),

    /**
     * Request expired after timeout period (30 days)
     */
    EXPIRED("Expired", "⏰", false, true);

    private final String displayName;
    private final String emoji;
    private final boolean isActionable;
    private final boolean isFinal;

    RequestStatus(String displayName, String emoji, boolean isActionable, boolean isFinal) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.isActionable = isActionable;
        this.isFinal = isFinal;
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

    public boolean isActionable() {
        return isActionable;
    }

    public boolean isFinal() {
        return isFinal;
    }

    /**
     * Check if the request is still pending a response
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * Check if the request was successfully accepted
     */
    public boolean wasAccepted() {
        return this == ACCEPTED;
    }

    /**
     * Check if the request was declined or expired
     */
    public boolean wasRejected() {
        return this == DECLINED || this == EXPIRED;
    }

    /**
     * Check if the request can be responded to
     */
    public boolean canRespond() {
        return this == PENDING;
    }

    /**
     * Check if the request can be cancelled by sender
     */
    public boolean canCancel() {
        return this == PENDING;
    }

    /**
     * Get possible next statuses from current status
     */
    public RequestStatus[] getPossibleTransitions() {
        return switch (this) {
            case PENDING -> new RequestStatus[]{ACCEPTED, DECLINED, EXPIRED};
            case ACCEPTED, DECLINED, EXPIRED -> new RequestStatus[]{}; // Final states
        };
    }

    /**
     * Check if transition to another status is valid
     */
    public boolean canTransitionTo(RequestStatus newStatus) {
        RequestStatus[] possible = getPossibleTransitions();
        for (RequestStatus status : possible) {
            if (status == newStatus) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get user-friendly description for status
     */
    public String getDescription() {
        return switch (this) {
            case PENDING -> "Waiting for response";
            case ACCEPTED -> "Request accepted - conversation started";
            case DECLINED -> "Request declined";
            case EXPIRED -> "Request expired - no response after 30 days";
        };
    }

    /**
     * Get color code for UI display
     */
    public String getColorCode() {
        return switch (this) {
            case PENDING -> "#FFA500";   // Orange
            case ACCEPTED -> "#28A745";  // Green
            case DECLINED -> "#DC3545";  // Red
            case EXPIRED -> "#6C757D";   // Gray
        };
    }

    /**
     * Get all active statuses (not final)
     */
    public static RequestStatus[] getActiveStatuses() {
        return new RequestStatus[]{PENDING};
    }

    /**
     * Get all final statuses
     */
    public static RequestStatus[] getFinalStatuses() {
        return new RequestStatus[]{ACCEPTED, DECLINED, EXPIRED};
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
    public static RequestStatus fromDatabaseValue(String value) {
        try {
            return RequestStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDING; // Default fallback
        }
    }
}