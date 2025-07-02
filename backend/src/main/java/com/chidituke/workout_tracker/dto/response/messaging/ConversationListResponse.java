package com.chidituke.workout_tracker.dto.response.messaging;

import com.chidituke.workout_tracker.model.messaging.enums.ConversationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for conversation list
 */
@Data
@Builder
public class ConversationListResponse {

    private Long id;
    private ConversationType type;
    private String displayName;
    private String lastMessagePreview;
    private LocalDateTime lastMessageTime;
    private long unreadCount;
    private boolean isStarred;
    private boolean isMuted;
    private List<UserSummaryResponse> otherParticipants; // For quick display
}
