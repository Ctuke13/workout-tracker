package com.chidituke.workout_tracker.model.messaging;

import com.chidituke.workout_tracker.model.messaging.enums.ConversationType;
import com.chidituke.workout_tracker.model.messaging.enums.ParticipantRole;
import com.chidituke.workout_tracker.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core conversation entity supporting both direct (1-on-1) and group messaging
 * Maps to conversations table created by V011 migration
 */
@Data
@Entity
@Table(name = "conversations")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"participants", "messages"})
@EqualsAndHashCode(exclude = {"participants", "messages"})
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_id")
    private Long id;

    // ==================== CONVERSATION TYPE & METADATA ====================

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @Builder.Default
    private ConversationType type = ConversationType.DIRECT;

    @Column(name = "name", length = 100)
    @Size(max = 100, message = "Conversation name cannot exceed 100 characters")
    private String name; // NULL for direct conversations, required for groups

    // ==================== CREATOR & OWNERSHIP ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    @NotNull(message = "Conversation creator is required")
    private User createdBy;

    // ==================== RELATIONSHIPS ====================

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<ConversationParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    // ==================== TIMESTAMPS ====================

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * Check if this is a direct (1-on-1) conversation
     */
    public boolean isDirect() {
        return type == ConversationType.DIRECT;
    }

    /**
     * Check if this is a group conversation
     */
    public boolean isGroup() {
        return type == ConversationType.GROUP;
    }

    /**
     * Get active participants (not left the conversation)
     */
    public List<ConversationParticipant> getActiveParticipants() {
        return participants.stream()
                .filter(ConversationParticipant::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Get active participant count
     */
    public int getActiveParticipantCount() {
        return getActiveParticipants().size();
    }

    /**
     * Check if user is an active participant
     */
    public boolean hasActiveParticipant(User user) {
        return getActiveParticipants().stream()
                .anyMatch(p -> p.getUser().equals(user));
    }

    /**
     * Get participant record for a specific user
     */
    public ConversationParticipant getParticipant(User user) {
        return participants.stream()
                .filter(p -> p.getUser().equals(user))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get active participant record for a specific user
     */
    public ConversationParticipant getActiveParticipant(User user) {
        return getActiveParticipants().stream()
                .filter(p -> p.getUser().equals(user))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the other user in a direct conversation
     */
    public User getOtherUser(User currentUser) {
        if (!isDirect()) {
            throw new IllegalStateException("Cannot get other user in non-direct conversation");
        }

        return getActiveParticipants().stream()
                .map(ConversationParticipant::getUser)
                .filter(user -> !user.equals(currentUser))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get display name for this conversation
     */
    public String getDisplayName(User currentUser) {
        if (isDirect()) {
            User otherUser = getOtherUser(currentUser);
            return otherUser != null ? otherUser.getFullName() : "Unknown User";
        } else {
            return name != null ? name : "Group Chat";
        }
    }

    /**
     * Get the most recent message
     */
    public Message getLastMessage() {
        return messages.isEmpty() ? null : messages.get(0); // Ordered by createdAt DESC
    }

    /**
     * Get unread message count for a user
     */
    public long getUnreadCount(User user) {
        ConversationParticipant participant = getActiveParticipant(user);
        if (participant == null) return 0;

        LocalDateTime lastSeen = participant.getLastSeenAt();
        if (lastSeen == null) return messages.size();

        return messages.stream()
                .filter(msg -> msg.getCreatedAt().isAfter(lastSeen))
                .filter(msg -> !msg.getSender().equals(user)) // Don't count own messages
                .count();
    }

    /**
     * Add a participant to the conversation
     */
    public ConversationParticipant addParticipant(User user, ParticipantRole role) {
        // Check if user is already a participant
        ConversationParticipant existing = getParticipant(user);
        if (existing != null && existing.isActive()) {
            return existing; // Already active participant
        }

        // Validate participant count
        if (!type.canAddParticipants(getActiveParticipantCount())) {
            throw new IllegalStateException("Cannot add more participants to this conversation type");
        }

        // Create new participant
        ConversationParticipant participant = ConversationParticipant.builder()
                .conversation(this)
                .user(user)
                .role(role)
                .build();

        participants.add(participant);
        return participant;
    }

    /**
     * Remove a participant from the conversation
     */
    public void removeParticipant(User user) {
        ConversationParticipant participant = getActiveParticipant(user);
        if (participant != null) {
            participant.leave();
        }
    }

    /**
     * Check if a user can send messages to this conversation
     */
    public boolean canUserSendMessage(User user) {
        ConversationParticipant participant = getActiveParticipant(user);
        return participant != null && participant.canSendMessages();
    }

    /**
     * Check if conversation is valid (has required participants)
     */
    public boolean isValid() {
        int activeCount = getActiveParticipantCount();
        return type.isValidParticipantCount(activeCount);
    }

    /**
     * Mark conversation as updated (for message ordering)
     */
    public void markAsUpdated() {
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== STATIC FACTORY METHODS ====================

    /**
     * Create a new direct conversation between two users
     */
    public static Conversation createDirectConversation(User user1, User user2) {
        if (user1.equals(user2)) {
            throw new IllegalArgumentException("Cannot create conversation with self");
        }

        Conversation conversation = Conversation.builder()
                .type(ConversationType.DIRECT)
                .createdBy(user1)
                .build();

        // Add both participants
        conversation.addParticipant(user1, ParticipantRole.MEMBER);
        conversation.addParticipant(user2, ParticipantRole.MEMBER);

        return conversation;
    }

    /**
     * Create a new group conversation
     */
    public static Conversation createGroupConversation(String name, User creator) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Group conversation name is required");
        }

        Conversation conversation = Conversation.builder()
                .type(ConversationType.GROUP)
                .name(name.trim())
                .createdBy(creator)
                .build();

        // Add creator as owner
        conversation.addParticipant(creator, ParticipantRole.OWNER);

        return conversation;
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
        validateConversation();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        validateConversation();
    }

    /**
     * Validate conversation state
     */
    private void validateConversation() {
        // Direct conversations should not have names
        if (isDirect() && name != null) {
            throw new IllegalStateException("Direct conversations cannot have names");
        }

        // Group conversations must have names
        if (isGroup() && (name == null || name.trim().isEmpty())) {
            throw new IllegalStateException("Group conversations must have names");
        }
    }
}