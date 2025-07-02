package com.chidituke.workout_tracker.model.messaging;

import com.chidituke.workout_tracker.model.messaging.enums.ParticipantRole;
import com.chidituke.workout_tracker.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Join entity representing user participation in conversations
 * Handles participant roles, starring, and participation lifecycle
 * Maps to conversation_participants table created by V011 migration
 */
@Data
@Entity
@Table(name = "conversation_participants")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"conversation", "user"})
@EqualsAndHashCode(exclude = {"conversation", "user"})
public class ConversationParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long id;

    // ==================== RELATIONSHIPS ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    @NotNull(message = "Conversation is required")
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    // ==================== PARTICIPANT SETTINGS ====================

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private ParticipantRole role = ParticipantRole.MEMBER;

    @Column(name = "is_starred", nullable = false)
    @Builder.Default
    private Boolean isStarred = false;

    // ==================== PARTICIPATION LIFECYCLE ====================

    @Column(name = "joined_at", nullable = false)
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Column(name = "left_at")
    private LocalDateTime leftAt; // NULL = still active participant

    // ==================== CONVERSATION STATE ====================

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt; // When user last read messages

    @Column(name = "notifications_enabled")
    @Builder.Default
    private Boolean notificationsEnabled = true;

    @Column(name = "is_muted")
    @Builder.Default
    private Boolean isMuted = false;

    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * Check if participant is currently active (not left)
     */
    public boolean isActive() {
        return leftAt == null;
    }

    /**
     * Check if participant has left the conversation
     */
    public boolean hasLeft() {
        return leftAt != null;
    }

    /**
     * Check if conversation is starred by this participant
     */
    public boolean isStarred() {
        return Boolean.TRUE.equals(isStarred);
    }

    /**
     * Check if notifications are enabled for this participant
     */
    public boolean hasNotificationsEnabled() {
        return Boolean.TRUE.equals(notificationsEnabled) && !Boolean.TRUE.equals(isMuted);
    }

    /**
     * Check if conversation is muted by this participant
     */
    public boolean isMuted() {
        return Boolean.TRUE.equals(isMuted);
    }

    /**
     * Check if participant can send messages
     */
    public boolean canSendMessages() {
        return isActive() && role.canSendMessages();
    }

    /**
     * Check if participant can add other participants (group conversations)
     */
    public boolean canAddParticipants() {
        return isActive() && role.canAddParticipants();
    }

    /**
     * Check if participant can remove other participants (group conversations)
     */
    public boolean canRemoveParticipants() {
        return isActive() && role.canRemoveParticipants();
    }

    /**
     * Check if participant can moderate messages
     */
    public boolean canModerateMessages() {
        return isActive() && role.canModerateMessages();
    }

    /**
     * Check if participant can change conversation settings
     */
    public boolean canChangeSettings() {
        return isActive() && role.canChangeSettings();
    }

    /**
     * Check if this participant can be removed by another participant
     */
    public boolean canBeRemovedBy(ConversationParticipant other) {
        if (!isActive() || !other.isActive()) {
            return false;
        }

        // Check if other participant has permission to remove
        if (!other.canRemoveParticipants()) {
            return false;
        }

        // Check if this participant can be removed based on role
        return role.canBeRemoved() && other.role.hasHigherPriorityThan(this.role);
    }

    // ==================== PARTICIPANT ACTIONS ====================

    /**
     * Star this conversation for priority access
     */
    public void star() {
        this.isStarred = true;
    }

    /**
     * Unstar this conversation
     */
    public void unstar() {
        this.isStarred = false;
    }

    /**
     * Toggle starred status
     */
    public void toggleStar() {
        this.isStarred = !Boolean.TRUE.equals(this.isStarred);
    }

    /**
     * Mute notifications for this conversation
     */
    public void mute() {
        this.isMuted = true;
    }

    /**
     * Unmute notifications for this conversation
     */
    public void unmute() {
        this.isMuted = false;
    }

    /**
     * Toggle mute status
     */
    public void toggleMute() {
        this.isMuted = !Boolean.TRUE.equals(this.isMuted);
    }

    /**
     * Enable notifications for this conversation
     */
    public void enableNotifications() {
        this.notificationsEnabled = true;
    }

    /**
     * Disable notifications for this conversation
     */
    public void disableNotifications() {
        this.notificationsEnabled = false;
    }

    /**
     * Mark as seen (update last seen timestamp)
     */
    public void markAsSeen() {
        this.lastSeenAt = LocalDateTime.now();
    }

    /**
     * Leave the conversation
     */
    public void leave() {
        if (!isActive()) {
            throw new IllegalStateException("Participant has already left the conversation");
        }

        this.leftAt = LocalDateTime.now();
    }

    /**
     * Rejoin the conversation (if previously left)
     */
    public void rejoin() {
        if (isActive()) {
            throw new IllegalStateException("Participant is already active in the conversation");
        }

        this.leftAt = null;
        this.joinedAt = LocalDateTime.now();
    }

    /**
     * Change participant role (requires appropriate permissions)
     */
    public void changeRole(ParticipantRole newRole) {
        if (newRole == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        // Validate role for conversation type
        if (conversation.isDirect() && !newRole.isValidForDirectConversation()) {
            throw new IllegalStateException("Role " + newRole + " is not valid for direct conversations");
        }

        if (conversation.isGroup() && !newRole.isValidForGroupConversation()) {
            throw new IllegalStateException("Role " + newRole + " is not valid for group conversations");
        }

        this.role = newRole;
    }

    // ==================== CONVERSATION CONTEXT ====================

    /**
     * Get participation duration in the conversation
     */
    public long getParticipationDurationMinutes() {
        LocalDateTime endTime = hasLeft() ? leftAt : LocalDateTime.now();
        return java.time.Duration.between(joinedAt, endTime).toMinutes();
    }

    /**
     * Check if participant joined recently (within last hour)
     */
    public boolean isNewParticipant() {
        return joinedAt.isAfter(LocalDateTime.now().minusHours(1));
    }

    /**
     * Get display information about this participant
     */
    public String getDisplayInfo() {
        StringBuilder info = new StringBuilder();
        info.append(user.getFullName());

        if (role != ParticipantRole.MEMBER) {
            info.append(" (").append(role.getDisplayName()).append(")");
        }

        if (hasLeft()) {
            info.append(" - Left");
        } else if (isStarred()) {
            info.append(" ⭐");
        }

        return info.toString();
    }

    // ==================== STATIC FACTORY METHODS ====================

    /**
     * Create a new participant for a conversation
     */
    public static ConversationParticipant create(Conversation conversation, User user, ParticipantRole role) {
        if (conversation == null || user == null || role == null) {
            throw new IllegalArgumentException("Conversation, user, and role are required");
        }

        return ConversationParticipant.builder()
                .conversation(conversation)
                .user(user)
                .role(role)
                .build();
    }

    /**
     * Create a participant with default member role
     */
    public static ConversationParticipant createMember(Conversation conversation, User user) {
        return create(conversation, user, ParticipantRole.MEMBER);
    }

    // ==================== JPA LIFECYCLE METHODS ====================

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
        validateParticipant();
    }

    @PreUpdate
    protected void onUpdate() {
        validateParticipant();
    }

    /**
     * Validate participant state
     */
    private void validateParticipant() {
        // Validate role for conversation type
        if (conversation != null) {
            if (conversation.isDirect() && !role.isValidForDirectConversation()) {
                throw new IllegalStateException("Invalid role for direct conversation: " + role);
            }
            if (conversation.isGroup() && !role.isValidForGroupConversation()) {
                throw new IllegalStateException("Invalid role for group conversation: " + role);
            }
        }

        // Validate timeline
        if (leftAt != null && leftAt.isBefore(joinedAt)) {
            throw new IllegalStateException("Left time cannot be before joined time");
        }

        if (lastSeenAt != null && lastSeenAt.isBefore(joinedAt)) {
            throw new IllegalStateException("Last seen time cannot be before joined time");
        }
    }
}