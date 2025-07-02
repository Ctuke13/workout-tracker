package com.chidituke.workout_tracker.model.messaging;

import com.chidituke.workout_tracker.model.messaging.Conversation;
import com.chidituke.workout_tracker.model.messaging.enums.MessageType;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.workout.WorkoutPlan;
import com.chidituke.workout_tracker.model.workout.WorkoutSession;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Individual message entity within conversations
 * Supports text, media, and fitness-specific content sharing
 * Maps to messages table created by V011 migration
 */
@Data
@Entity
@Table(name = "messages")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"conversation", "sender", "sharedWorkoutSession", "sharedWorkoutPlan"})
@EqualsAndHashCode(exclude = {"conversation", "sender", "sharedWorkoutSession", "sharedWorkoutPlan"})
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    // ==================== RELATIONSHIPS ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    @NotNull(message = "Conversation is required")
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    @NotNull(message = "Sender is required")
    private User sender;

    // ==================== MESSAGE CONTENT ====================

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Message content is required")
    @Size(max = 2000, message = "Message content cannot exceed 2000 characters")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    // ==================== MEDIA CONTENT ====================

    @Column(name = "media_url", length = 500)
    @Size(max = 500, message = "Media URL cannot exceed 500 characters")
    private String mediaUrl;

    @Column(name = "media_size_bytes")
    @Min(value = 0, message = "Media size cannot be negative")
    private Long mediaSizeBytes;

    // ==================== CONTENT MODERATION ====================

    @Column(name = "is_filtered", nullable = false)
    @Builder.Default
    private Boolean isFiltered = false;

    @Column(name = "filter_reason", length = 100)
    @Size(max = 100, message = "Filter reason cannot exceed 100 characters")
    private String filterReason;

    // ==================== FITNESS CONTENT INTEGRATION ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_workout_session_id")
    private WorkoutSession sharedWorkoutSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_workout_plan_id")
    private WorkoutPlan sharedWorkoutPlan;

    // ==================== TIMESTAMPS ====================

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * Check if this is a text message
     */
    public boolean isTextMessage() {
        return messageType == MessageType.TEXT;
    }

    /**
     * Check if this message contains media content
     */
    public boolean hasMedia() {
        return messageType.isMediaType() && mediaUrl != null && !mediaUrl.trim().isEmpty();
    }

    /**
     * Check if this message shares fitness content
     */
    public boolean sharesFitnessContent() {
        return messageType.isFitnessContent() || hasSharedWorkout() || hasSharedWorkoutPlan();
    }

    /**
     * Check if message shares a workout session
     */
    public boolean hasSharedWorkout() {
        return sharedWorkoutSession != null;
    }

    /**
     * Check if message shares a workout plan
     */
    public boolean hasSharedWorkoutPlan() {
        return sharedWorkoutPlan != null;
    }

    /**
     * Check if message has been filtered by content moderation
     */
    public boolean isFiltered() {
        return Boolean.TRUE.equals(isFiltered);
    }

    /**
     * Check if message can be shared externally
     */
    public boolean canShareExternally() {
        return messageType.canShareExternally() && !isFiltered();
    }

    /**
     * Check if user can edit this message
     */
    public boolean canEdit(User user) {
        // Only sender can edit, and only within 15 minutes
        if (!sender.equals(user)) {
            return false;
        }

        LocalDateTime editDeadline = createdAt.plusMinutes(15);
        return LocalDateTime.now().isBefore(editDeadline);
    }

    /**
     * Check if user can delete this message
     */
    public boolean canDelete(User user) {
        // Sender can always delete
        if (sender.equals(user)) {
            return true;
        }

        // Check if user has moderation permissions in the conversation
        ConversationParticipant participant = conversation.getActiveParticipant(user);
        return participant != null && participant.canModerateMessages();
    }

    /**
     * Get content preview (truncated for display)
     */
    public String getContentPreview(int maxLength) {
        if (isFiltered()) {
            return "[Message filtered: " + (filterReason != null ? filterReason : "inappropriate content") + "]";
        }

        if (hasMedia()) {
            return messageType.getEmoji() + " " + messageType.getDisplayName();
        }

        if (sharesFitnessContent()) {
            return getWorkoutSharePreview();
        }

        if (content == null) {
            return "";
        }

        return content.length() <= maxLength ? content : content.substring(0, maxLength) + "...";
    }

    /**
     * Get workout sharing preview text
     */
    private String getWorkoutSharePreview() {
        if (hasSharedWorkout()) {
            return "🏋️ Shared workout: " +
                    (sharedWorkoutSession.getWorkoutPlan() != null ?
                            sharedWorkoutSession.getWorkoutPlan().getWorkoutName() : "Workout");
        }

        if (hasSharedWorkoutPlan()) {
            return "📋 Shared workout plan: " + sharedWorkoutPlan.getWorkoutName();
        }

        return messageType.getEmoji() + " " + messageType.getDisplayName();
    }

    /**
     * Get media file size in human-readable format
     */
    public String getFormattedFileSize() {
        if (mediaSizeBytes == null || mediaSizeBytes == 0) {
            return "";
        }

        if (mediaSizeBytes < 1024) {
            return mediaSizeBytes + " B";
        } else if (mediaSizeBytes < 1024 * 1024) {
            return String.format("%.1f KB", mediaSizeBytes / 1024.0);
        } else {
            return String.format("%.1f MB", mediaSizeBytes / (1024.0 * 1024.0));
        }
    }

    /**
     * Check if message is within recent timeframe
     */
    public boolean isRecent() {
        return createdAt.isAfter(LocalDateTime.now().minusHours(24));
    }

    /**
     * Get time since message was sent
     */
    public String getTimeAgo() {
        LocalDateTime now = LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.between(createdAt, now);

        if (duration.toMinutes() < 1) {
            return "Just now";
        } else if (duration.toHours() < 1) {
            return duration.toMinutes() + "m ago";
        } else if (duration.toDays() < 1) {
            return duration.toHours() + "h ago";
        } else {
            return duration.toDays() + "d ago";
        }
    }

    // ==================== MODERATION ACTIONS ====================

    /**
     * Filter this message for inappropriate content
     */
    public void filter(String reason) {
        this.isFiltered = true;
        this.filterReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Unfilter this message (restore from moderation)
     */
    public void unfilter() {
        this.isFiltered = false;
        this.filterReason = null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Edit message content (within time limit)
     */
    public void editContent(String newContent, User editor) {
        if (!canEdit(editor)) {
            throw new IllegalStateException("User cannot edit this message");
        }

        if (newContent == null || newContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }

        this.content = newContent.trim();
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== STATIC FACTORY METHODS ====================

    /**
     * Create a text message
     */
    public static Message createTextMessage(Conversation conversation, User sender, String content) {
        return Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content)
                .messageType(MessageType.TEXT)
                .build();
    }

    /**
     * Create a media message
     */
    public static Message createMediaMessage(Conversation conversation, User sender, String content,
                                             MessageType messageType, String mediaUrl, Long mediaSizeBytes) {
        if (!messageType.isMediaType()) {
            throw new IllegalArgumentException("Message type must be media type");
        }

        return Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content)
                .messageType(messageType)
                .mediaUrl(mediaUrl)
                .mediaSizeBytes(mediaSizeBytes)
                .build();
    }

    /**
     * Create a workout sharing message
     */
    public static Message createWorkoutMessage(Conversation conversation, User sender, String content,
                                               WorkoutSession workoutSession) {
        return Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content)
                .messageType(MessageType.WORKOUT)
                .sharedWorkoutSession(workoutSession)
                .build();
    }

    /**
     * Create a workout plan sharing message
     */
    public static Message createWorkoutPlanMessage(Conversation conversation, User sender, String content,
                                                   WorkoutPlan workoutPlan) {
        return Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content)
                .messageType(MessageType.WORKOUT_PLAN)
                .sharedWorkoutPlan(workoutPlan)
                .build();
    }

    // ==================== JPA LIFECYCLE METHODS ====================

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        validateMessage();

        // Update conversation timestamp (will be handled by database trigger)
        if (conversation != null) {
            conversation.markAsUpdated();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        validateMessage();
    }

    /**
     * Validate message state
     */
    private void validateMessage() {
        // Validate content based on message type
        if (messageType.requiresMedia() && (mediaUrl == null || mediaUrl.trim().isEmpty())) {
            throw new IllegalStateException("Media URL is required for " + messageType + " messages");
        }

        // Validate file size for media types
        if (hasMedia() && mediaSizeBytes != null) {
            long maxSize = messageType.getMaxFileSizeBytes();
            if (maxSize > 0 && mediaSizeBytes > maxSize) {
                throw new IllegalStateException("File size exceeds maximum allowed for " + messageType +
                        " (" + messageType.getFileSizeDescription() + ")");
            }
        }

        // Validate fitness content
        if (messageType == MessageType.WORKOUT && sharedWorkoutSession == null) {
            throw new IllegalStateException("Shared workout session is required for WORKOUT messages");
        }

        if (messageType == MessageType.WORKOUT_PLAN && sharedWorkoutPlan == null) {
            throw new IllegalStateException("Shared workout plan is required for WORKOUT_PLAN messages");
        }

        // Validate content length
        if (content != null && content.length() > 2000) {
            throw new IllegalStateException("Message content cannot exceed 2000 characters");
        }
    }
}