package com.chidituke.workout_tracker.service.messaging;

import com.chidituke.workout_tracker.model.messaging.Conversation;
import com.chidituke.workout_tracker.model.messaging.ConversationParticipant;
import com.chidituke.workout_tracker.model.messaging.enums.ConversationType;
import com.chidituke.workout_tracker.model.messaging.enums.ParticipantRole;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.user.UserRelationship;
import com.chidituke.workout_tracker.repository.messaging.ConversationRepository;
import com.chidituke.workout_tracker.repository.messaging.ConversationParticipantRepository;
import com.chidituke.workout_tracker.service.user.UserRelationshipService;
import com.chidituke.workout_tracker.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing conversations and participants
 * Handles conversation creation, participant management, permissions, and user experience features
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final UserRelationshipService userRelationshipService;
    private final NotificationService notificationService;

    // ==================== CONVERSATION CREATION ====================

    /**
     * Create or find existing direct conversation between two users
     * Validates relationship requirements before creation
     */
    public Conversation createOrFindDirectConversation(User user1, User user2) {
        log.debug("Creating/finding direct conversation between users {} and {}",
                user1.getId(), user2.getId());

        // Check if conversation already exists
        Optional<Conversation> existing = conversationRepository
                .findDirectConversationBetweenUsers(user1, user2);

        if (existing.isPresent()) {
            log.debug("Found existing direct conversation: {}", existing.get().getId());
            return existing.get();
        }

        // Validate users can message each other
        if (!canUsersMessage(user1, user2)) {
            throw new IllegalStateException("Users are not allowed to message each other");
        }

        // Create new direct conversation
        Conversation conversation = Conversation.createDirectConversation(user1, user2);
        conversation = conversationRepository.save(conversation);

        log.info("Created new direct conversation {} between users {} and {}",
                conversation.getId(), user1.getId(), user2.getId());

        return conversation;
    }

    /**
     * Create a new group conversation
     * Only for future group messaging implementation
     */
    public Conversation createGroupConversation(String name, User creator, List<User> initialParticipants) {
        log.debug("Creating group conversation '{}' by user {}", name, creator.getId());

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Group conversation name is required");
        }

        if (initialParticipants.size() < 2) {
            throw new IllegalArgumentException("Group conversations require at least 2 participants besides creator");
        }

        // Create group conversation
        Conversation conversation = Conversation.createGroupConversation(name.trim(), creator);
        conversation = conversationRepository.save(conversation);

        // Add initial participants
        for (User participant : initialParticipants) {
            if (!participant.equals(creator)) {
                addParticipant(conversation.getId(), participant, ParticipantRole.MEMBER);
            }
        }

        log.info("Created group conversation {} with {} participants",
                conversation.getId(), initialParticipants.size() + 1);

        return conversation;
    }

    // ==================== CONVERSATION RETRIEVAL ====================

    /**
     * Get conversations for user with pagination
     * Returns user's conversation list ordered by recent activity
     */
    @Transactional(readOnly = true)
    public Page<Conversation> getConversationsForUser(User user, Pageable pageable) {
        log.debug("Fetching conversations for user {} with pagination", user.getId());
        return conversationRepository.findActiveConversationsForUser(user, pageable);
    }

    /**
     * Get starred conversations for user
     * Returns prioritized conversation list
     */
    @Transactional(readOnly = true)
    public List<Conversation> getStarredConversationsForUser(User user) {
        log.debug("Fetching starred conversations for user {}", user.getId());
        return conversationRepository.findStarredConversationsForUser(user);
    }

    /**
     * Get conversation by ID with permission check
     */
    @Transactional(readOnly = true)
    public Optional<Conversation> getConversation(Long conversationId, User user) {
        log.debug("Fetching conversation {} for user {}", conversationId, user.getId());

        return conversationRepository.findById(conversationId)
                .filter(conversation -> isParticipant(conversation, user));
    }

    /**
     * Get conversation with all participants loaded
     */
    @Transactional(readOnly = true)
    public Optional<Conversation> getConversationWithParticipants(Long conversationId, User user) {
        log.debug("Fetching conversation {} with participants for user {}", conversationId, user.getId());

        Optional<Conversation> conversation = conversationRepository.findWithParticipants(conversationId);

        return conversation.filter(conv -> isParticipant(conv, user));
    }

    /**
     * Search conversations for user
     */
    @Transactional(readOnly = true)
    public List<Conversation> searchConversations(User user, String searchTerm) {
        log.debug("Searching conversations for user {} with term '{}'", user.getId(), searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return List.of();
        }

        return conversationRepository.searchConversations(user, searchTerm.trim());
    }

    // ==================== PARTICIPANT MANAGEMENT ====================

    /**
     * Add a participant to conversation
     * Validates permissions and relationship requirements
     */
    public ConversationParticipant addParticipant(Long conversationId, User userToAdd, ParticipantRole role) {
        log.debug("Adding user {} to conversation {} with role {}",
                userToAdd.getId(), conversationId, role);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        // Validate conversation type supports adding participants
        if (conversation.isDirect()) {
            throw new IllegalStateException("Cannot add participants to direct conversations");
        }

        // Check if user is already a participant
        Optional<ConversationParticipant> existing = participantRepository
                .findByConversationAndUser(conversation, userToAdd);

        if (existing.isPresent() && existing.get().isActive()) {
            throw new IllegalStateException("User is already a participant in this conversation");
        }

        // Rejoin if previously left
        if (existing.isPresent() && existing.get().hasLeft()) {
            existing.get().rejoin();
            participantRepository.save(existing.get());
            log.info("User {} rejoined conversation {}", userToAdd.getId(), conversationId);
            return existing.get();
        }

        // Create new participant
        ConversationParticipant participant = ConversationParticipant.create(conversation, userToAdd, role);
        participant = participantRepository.save(participant);

        // Update conversation timestamp
        conversation.markAsUpdated();
        conversationRepository.save(conversation);

        log.info("Added user {} to conversation {} as {}",
                userToAdd.getId(), conversationId, role);

        return participant;
    }

    /**
     * Remove a participant from conversation
     */
    public void removeParticipant(Long conversationId, User userToRemove, User requestingUser) {
        log.debug("Removing user {} from conversation {} by user {}",
                userToRemove.getId(), conversationId, requestingUser.getId());

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        // Validate requesting user can remove participants
        if (!canUserRemoveParticipants(conversation, requestingUser, userToRemove)) {
            throw new IllegalStateException("User does not have permission to remove participants");
        }

        ConversationParticipant participant = participantRepository
                .findActiveParticipant(conversation, userToRemove)
                .orElseThrow(() -> new IllegalArgumentException("User is not an active participant"));

        // Remove participant
        participant.leave();
        participantRepository.save(participant);

        // Update conversation timestamp
        conversation.markAsUpdated();
        conversationRepository.save(conversation);

        log.info("Removed user {} from conversation {}", userToRemove.getId(), conversationId);
    }

    /**
     * Update participant role
     */
    public void updateParticipantRole(Long conversationId, User targetUser, ParticipantRole newRole, User requestingUser) {
        log.debug("Updating role for user {} in conversation {} to {} by user {}",
                targetUser.getId(), conversationId, newRole, requestingUser.getId());

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        // Validate requesting user can change roles
        if (!canUserChangeRoles(conversation, requestingUser)) {
            throw new IllegalStateException("User does not have permission to change roles");
        }

        ConversationParticipant participant = participantRepository
                .findActiveParticipant(conversation, targetUser)
                .orElseThrow(() -> new IllegalArgumentException("User is not an active participant"));

        participant.changeRole(newRole);
        participantRepository.save(participant);

        log.info("Updated role for user {} in conversation {} to {}",
                targetUser.getId(), conversationId, newRole);
    }

    // ==================== PARTICIPANT SETTINGS ====================

    /**
     * Star/unstar conversation for user
     */
    public void toggleStarredConversation(Long conversationId, User user) {
        log.debug("Toggling starred status for conversation {} by user {}", conversationId, user.getId());

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        ConversationParticipant participant = participantRepository
                .findActiveParticipant(conversation, user)
                .orElseThrow(() -> new IllegalArgumentException("User is not a participant"));

        boolean newStarredStatus = !participant.isStarred();
        participantRepository.updateStarred(conversation, user, newStarredStatus);

        log.info("User {} {} conversation {}",
                user.getId(), newStarredStatus ? "starred" : "unstarred", conversationId);
    }

    /**
     * Mute/unmute conversation for user
     */
    public void toggleMutedConversation(Long conversationId, User user) {
        log.debug("Toggling muted status for conversation {} by user {}", conversationId, user.getId());

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        ConversationParticipant participant = participantRepository
                .findActiveParticipant(conversation, user)
                .orElseThrow(() -> new IllegalArgumentException("User is not a participant"));

        boolean newMutedStatus = !participant.isMuted();
        participantRepository.updateMuted(conversation, user, newMutedStatus);

        log.info("User {} {} conversation {}",
                user.getId(), newMutedStatus ? "muted" : "unmuted", conversationId);
    }

    /**
     * Mark conversation as read for user
     */
    public void markConversationAsRead(Long conversationId, User user) {
        log.debug("Marking conversation {} as read for user {}", conversationId, user.getId());

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        if (!isParticipant(conversation, user)) {
            throw new IllegalArgumentException("User is not a participant");
        }

        participantRepository.updateLastSeen(conversation, user, LocalDateTime.now());

        log.debug("Marked conversation {} as read for user {}", conversationId, user.getId());
    }

    // ==================== PERMISSION CHECKING ====================

    /**
     * Check if user can send messages in conversation
     */
    @Transactional(readOnly = true)
    public boolean canUserSendMessage(Long conversationId, User user) {
        return conversationRepository.findById(conversationId)
                .map(conversation -> canUserSendMessage(conversation, user))
                .orElse(false);
    }

    /**
     * Check if user can send messages in conversation
     */
    @Transactional(readOnly = true)
    public boolean canUserSendMessage(Conversation conversation, User user) {
        return participantRepository.canUserSendMessages(conversation, user);
    }

    /**
     * Check if user is a participant in conversation
     */
    @Transactional(readOnly = true)
    public boolean isParticipant(Conversation conversation, User user) {
        return participantRepository.findActiveParticipant(conversation, user).isPresent();
    }

    /**
     * Check if user can add participants to conversation
     */
    @Transactional(readOnly = true)
    public boolean canUserAddParticipants(Conversation conversation, User user) {
        return participantRepository.findActiveParticipant(conversation, user)
                .map(ConversationParticipant::canAddParticipants)
                .orElse(false);
    }

    /**
     * Check if user can remove participants from conversation
     */
    @Transactional(readOnly = true)
    public boolean canUserRemoveParticipants(Conversation conversation, User requestingUser, User targetUser) {
        // Users can always leave themselves
        if (requestingUser.equals(targetUser)) {
            return true;
        }

        return participantRepository.findActiveParticipant(conversation, requestingUser)
                .map(ConversationParticipant::canRemoveParticipants)
                .orElse(false);
    }

    /**
     * Check if user can change roles in conversation
     */
    @Transactional(readOnly = true)
    public boolean canUserChangeRoles(Conversation conversation, User user) {
        return participantRepository.findActiveParticipant(conversation, user)
                .map(ConversationParticipant::canChangeSettings)
                .orElse(false);
    }

    // ==================== RELATIONSHIP VALIDATION ====================

    /**
     * Check if two users can message each other based on relationships
     * Implements the relationship-based messaging rules we designed
     */
    @Transactional(readOnly = true)
    public boolean canUsersMessage(User user1, User user2) {
        log.debug("Checking if users {} and {} can message each other", user1.getId(), user2.getId());

        // Users cannot message themselves
        if (user1.equals(user2)) {
            return false;
        }

        // Get relationship information between users
        UserRelationshipService.RelationshipInfo relationshipInfo =
                userRelationshipService.getRelationshipInfo(user1.getUsername(), user2.getUsername());

        // Check if users are blocked
        if (relationshipInfo.isBlocked() || relationshipInfo.isBlockedBy()) {
            log.debug("Users {} and {} are blocked from messaging", user1.getId(), user2.getId());
            return false;
        }

        // Professional users (PRO_PROFESSIONAL) can receive business inquiries from anyone
        if (isProfessional(user2)) {
            log.debug("User {} can message professional {}", user1.getId(), user2.getId());
            return true;
        }

        // Check for mutual following relationship
        if (relationshipInfo.isUserFollowsTarget() && relationshipInfo.isTargetFollowsUser()) {
            log.debug("Users {} and {} have mutual following relationship", user1.getId(), user2.getId());
            return true;
        }

        // Check for friend relationship
        if (relationshipInfo.isAreFriends()) {
            log.debug("Users {} and {} are friends", user1.getId(), user2.getId());
            return true;
        }

        log.debug("Users {} and {} do not have required relationship to message", user1.getId(), user2.getId());
        return false;
    }

    /**
     * Check if user is a professional (PRO_PROFESSIONAL tier)
     */
    private boolean isProfessional(User user) {
        return user.getUserType() != null && user.getUserType().name().equals("PRO_PROFESSIONAL");
    }

    // ==================== ANALYTICS AND UTILITIES ====================

    /**
     * Get unread conversation count for user
     */
    @Transactional(readOnly = true)
    public long getUnreadConversationCount(User user) {
        return conversationRepository.countUnreadConversationsForUser(user);
    }

    /**
     * Get total conversation count for user
     */
    @Transactional(readOnly = true)
    public long getTotalConversationCount(User user) {
        return conversationRepository.countConversationsForUser(user);
    }

    /**
     * Get recent conversations with activity since specified time
     */
    @Transactional(readOnly = true)
    public List<Conversation> getConversationsWithRecentActivity(User user, LocalDateTime since) {
        return conversationRepository.findConversationsWithActivitySince(user, since);
    }

    /**
     * Get conversations by type for user
     */
    @Transactional(readOnly = true)
    public List<Conversation> getConversationsByType(User user, ConversationType type) {
        return conversationRepository.findConversationsByTypeForUser(user, type);
    }

    /**
     * Get active participant for conversation
     */
    @Transactional(readOnly = true)
    public Optional<ConversationParticipant> getActiveParticipant(Conversation conversation, User user) {
        return participantRepository.findActiveParticipant(conversation, user);
    }

    /**
     * Get active participants for conversation
     */
    @Transactional(readOnly = true)
    public List<ConversationParticipant> getActiveParticipants(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .map(participantRepository::findActiveParticipants)
                .orElse(List.of());
    }

    /**
     * Check if direct conversation exists between users
     */
    @Transactional(readOnly = true)
    public boolean directConversationExists(User user1, User user2) {
        return conversationRepository.existsDirectConversationBetweenUsers(user1, user2);
    }
}