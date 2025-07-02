package com.chidituke.workout_tracker.dto.response.messaging;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for per-conversation unread counts
 */
@Data
@Builder
public class ConversationUnreadResponse {

    private Long conversationId;
    private String conversationName;
    private long unreadCount;
    private LocalDateTime lastMessageTime;
}
