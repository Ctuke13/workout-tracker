package com.chidituke.workout_tracker.dto.response.messaging;

import com.chidituke.workout_tracker.model.messaging.enums.ConversationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for conversations
 */
@Data
@Builder
public class ConversationResponse {

    private Long id;
    private ConversationType type;
    private String name; // For group conversations
    private String displayName; // Computed display name

    // Creator information
    private UserSummaryResponse createdBy;

    // Participants
    private List<ConversationParticipantResponse> participants;
    private int activeParticipantCount;

    // Latest message
    private MessageResponse lastMessage;

    // User-specific information
    private boolean isStarred;
    private boolean isMuted;
    private long unreadCount;
    private boolean hasNotificationsEnabled;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed fields
    private boolean isValid;
}
