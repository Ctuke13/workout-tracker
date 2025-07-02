package com.chidituke.workout_tracker.dto.response.messaging;

import com.chidituke.workout_tracker.model.messaging.enums.MessageType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for messages
 */
@Data
@Builder
public class MessageResponse {

    private Long id;
    private String content;
    private MessageType messageType;
    private String mediaUrl;
    private Long mediaSizeBytes;
    private String formattedFileSize;
    private boolean isFiltered;
    private String filterReason;

    // Sender information
    private UserSummaryResponse sender;

    // Fitness content
    private WorkoutSessionSummaryResponse sharedWorkoutSession;
    private WorkoutPlanSummaryResponse sharedWorkoutPlan;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed fields
    private String contentPreview;
    private String timeAgo;
    private boolean canEdit;
    private boolean canDelete;
    private boolean isRecent;
}
