package com.chidituke.workout_tracker.dto.response.messaging;

import lombok.Builder;
import lombok.Data;

/**
 * Response DTO for message search results
 */
@Data
@Builder
public class MessageSearchResponse {

    private MessageResponse message;
    private ConversationResponse conversation;
    private String searchHighlight; // Highlighted search terms
}
