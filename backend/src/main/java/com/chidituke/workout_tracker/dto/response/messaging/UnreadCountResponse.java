package com.chidituke.workout_tracker.dto.response.messaging;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Response DTO for unread counts
 */
@Data
@Builder
public class UnreadCountResponse {

    private long totalUnreadMessages;
    private long unreadConversations;
    private List<ConversationUnreadResponse> conversationBreakdown;
}