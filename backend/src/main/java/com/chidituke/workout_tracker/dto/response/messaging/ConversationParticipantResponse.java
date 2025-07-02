package com.chidituke.workout_tracker.dto.response.messaging;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for conversation participants
 */
@Data
@Builder
public class ConversationParticipantResponse {

    private Long id;
    private UserSummaryResponse user;
    private String role;
    private boolean isStarred;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private LocalDateTime lastSeenAt;
    private boolean isActive;
    private boolean hasNotificationsEnabled;
    private boolean isMuted;

    // Permission flags
    private boolean canSendMessages;
    private boolean canAddParticipants;
    private boolean canRemoveParticipants;
    private boolean canModerateMessages;
}
