package com.chidituke.workout_tracker.repository.messaging;

import com.chidituke.workout_tracker.model.messaging.Conversation;
import com.chidituke.workout_tracker.model.messaging.ConversationParticipant;
import com.chidituke.workout_tracker.model.messaging.enums.ParticipantRole;
import com.chidituke.workout_tracker.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ConversationParticipant entity operations
 * Handles user participation in conversations, roles, and settings
 */
@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {

    // ==================== BASIC PARTICIPANT QUERIES ====================

    /**
     * Find active participants in a conversation
     * Core query for loading conversation members
     */
    @Query("""
        SELECT p FROM ConversationParticipant p
        JOIN FETCH p.user
        WHERE p.conversation = :conversation 
        AND p.leftAt IS NULL
        ORDER BY p.joinedAt ASC
        """)
    List<ConversationParticipant> findActiveParticipants(@Param("conversation") Conversation conversation);

    /**
     * Find all participants in a conversation (including left)
     * Used for conversation history and analytics
     */
    @Query("""
        SELECT p FROM ConversationParticipant p
        JOIN FETCH p.user
        WHERE p.conversation = :conversation
        ORDER BY p.joinedAt ASC
        """)
    List<ConversationParticipant> findAllParticipants(@Param("conversation") Conversation conversation);

    /**
     * Find specific participant by user and conversation
     * Essential for checking user participation status
     */
    @Query("""
        SELECT p FROM ConversationParticipant p
        WHERE p.conversation = :conversation 
        AND p.user = :user
        """)
    Optional<ConversationParticipant> findByConversationAndUser(
            @Param("conversation") Conversation conversation,
            @Param("user") User user);

    /**
     * Find active participant by user and conversation
     * Most commonly used participation check
     */
    @Query("""
        SELECT p FROM ConversationParticipant p
        WHERE p.conversation = :conversation 
        AND p.user = :user 
        AND p.leftAt IS NULL
        """)
    Optional<ConversationParticipant> findActiveParticipant(
            @Param("conversation") Conversation conversation,
            @Param("user") User user);

    // ==================== USER PARTICIPATION QUERIES ====================

    /**
     * Find all active participations for a user
     * Used for user's conversation list
     */
    @Query("""
        SELECT p FROM ConversationParticipant p
        JOIN FETCH p.conversation c
        WHERE p.user = :user 
        AND p.leftAt IS NULL
        ORDER BY c.updatedAt DESC
        """)
    List<ConversationParticipant> findActiveParticipationsForUser(@Param("user") User user);

    /**
     * Find starred participations for a user
     * Used for priority conversation display
     */
    @Query("""
        SELECT p FROM ConversationParticipant p
        JOIN FETCH p.conversation c
        WHERE p.user = :user 
        AND p.leftAt IS NULL
        AND p.isStarred = true
        ORDER BY c.updatedAt DESC
        """)
    List<ConversationParticipant> findStarredParticipationsForUser(@Param("user") User user);

    /**
     * Find muted participations for a user
     * Used for notification filtering
     */
    @Query("""
        SELECT p FROM ConversationParticipant p
        WHERE p.user = :user 
        AND p.leftAt IS NULL
        AND p.isMuted = true
        """)
    List<ConversationParticipant> findMutedParticipationsForUser(@Param("user") User user);

    // ==================== PERMISSION AND ROLE QUERIES ====================

    /**
     * Check if user can send messages in conversation
     * Critical for message permission validation
     */
    @Query("""
        SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
        FROM ConversationParticipant p
        WHERE p.conversation = :conversation 
        AND p.user = :user 
        AND p.leftAt IS NULL
        AND p.role IN ('OWNER', 'ADMIN', 'MODERATOR', 'MEMBER')
        """)
    boolean canUserSendMessages(
            @Param("conversation") Conversation conversation,
            @Param("user") User user);

    /**
     * Find participants with specific role in conversation
     * Used for role-based operations
     */
    @Query("""
        SELECT p FROM ConversationParticipant p
        JOIN FETCH p.user
        WHERE p.conversation = :conversation 
        AND p.leftAt IS NULL
        AND p.role = :role
        ORDER BY p.joinedAt ASC
        """)
    List<ConversationParticipant> findParticipantsByRole(
            @Param("conversation") Conversation conversation,
            @Param("role") ParticipantRole role);

    /**
     * Find conversation owners
     * Essential for administrative operations
     */
    @Query("""
        SELECT p FROM ConversationParticipant p
        JOIN FETCH p.user
        WHERE p.conversation = :conversation 
        AND p.leftAt IS NULL
        AND p.role = 'OWNER'
        """)
    List<ConversationParticipant> findOwners(@Param("conversation") Conversation conversation);

    /**
     * Check if user has administrative role in conversation
     */
    @Query("""
        SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
        FROM ConversationParticipant p
        WHERE p.conversation = :conversation 
        AND p.user = :user 
        AND p.leftAt IS NULL
        AND p.role IN ('OWNER', 'ADMIN')
        """)
    boolean hasAdminRole(
            @Param("conversation") Conversation conversation,
            @Param("user") User user);

    // ==================== NOTIFICATION QUERIES ====================

    /**
     * Find participants who should receive notifications for conversation
     * Used for message notification distribution
     */
    @Query("""
        SELECT p FROM ConversationParticipant p
        JOIN FETCH p.user
        WHERE p.conversation = :conversation 
        AND p.leftAt IS NULL
        AND p.notificationsEnabled = true
        AND p.isMuted = false
        AND p.user != :excludeUser
        """)
    List<ConversationParticipant> findNotificationEligibleParticipants(
            @Param("conversation") Conversation conversation,
            @Param("excludeUser") User excludeUser);

    /**
     * Find participants with notifications enabled (regardless of mute)
     * Used for urgent notifications that override mute
     */
    @Query("""
        SELECT p FROM ConversationParticipant p
        JOIN FETCH p.user
        WHERE p.conversation = :conversation 
        AND p.leftAt IS NULL
        AND p.notificationsEnabled = true
        AND p.user != :excludeUser
        """)
    List<ConversationParticipant> findNotificationEnabledParticipants(
            @Param("conversation") Conversation conversation,
            @Param("excludeUser") User excludeUser);

    // ==================== ACTIVITY AND ANALYTICS ====================

    /**
     * Count active participants in conversation
     * Lightweight count for validation
     */
    @Query("""
        SELECT COUNT(p) FROM ConversationParticipant p
        WHERE p.conversation = :conversation 
        AND p.leftAt IS NULL
        """)
    long countActiveParticipants(@Param("conversation") Conversation conversation);

    /**
     * Find participants who joined recently
     * Used for new member notifications
     */
    @Query("""
        SELECT p FROM ConversationParticipant p
        JOIN FETCH p.user
        WHERE p.conversation = :conversation 
        AND p.leftAt IS NULL
        AND p.joinedAt > :since
        ORDER BY p.joinedAt DESC
        """)
    List<ConversationParticipant> findRecentParticipants(
            @Param("conversation") Conversation conversation,
            @Param("since") LocalDateTime since);

    /**
     * Find participants who have never read messages
     * Used for engagement analytics
     */
    @Query("""
        SELECT p FROM ConversationParticipant p
        WHERE p.conversation = :conversation 
        AND p.leftAt IS NULL
        AND p.lastSeenAt IS NULL
        """)
    List<ConversationParticipant> findUnengagedParticipants(@Param("conversation") Conversation conversation);

    /**
     * Find participants with unread messages
     * Based on last seen timestamp vs conversation activity
     */
    @Query("""
        SELECT p FROM ConversationParticipant p
        JOIN FETCH p.user
        WHERE p.conversation = :conversation 
        AND p.leftAt IS NULL
        AND (p.lastSeenAt IS NULL OR p.lastSeenAt < p.conversation.updatedAt)
        """)
    List<ConversationParticipant> findParticipantsWithUnread(@Param("conversation") Conversation conversation);

    // ==================== BULK UPDATE OPERATIONS ====================

    /**
     * Update last seen timestamp for participant
     * Used when user reads messages
     */
    @Modifying
    @Query("""
        UPDATE ConversationParticipant p 
        SET p.lastSeenAt = :timestamp
        WHERE p.conversation = :conversation 
        AND p.user = :user 
        AND p.leftAt IS NULL
        """)
    int updateLastSeen(
            @Param("conversation") Conversation conversation,
            @Param("user") User user,
            @Param("timestamp") LocalDateTime timestamp);

    /**
     * Set starred status for participant
     */
    @Modifying
    @Query("UPDATE ConversationParticipant p " +
            "SET p.isStarred = :starred " +
            "WHERE p.conversation = :conversation " +
            "AND p.user = :user " +
            "AND p.leftAt IS NULL")
    int updateStarred(
            @Param("conversation") Conversation conversation,
            @Param("user") User user,
            @Param("starred") boolean starred);

    /**
     * Set muted status for participant
     */
    @Modifying
    @Query("UPDATE ConversationParticipant p " +
            "SET p.isMuted = :muted " +
            "WHERE p.conversation = :conversation " +
            "AND p.user = :user " +
            "AND p.leftAt IS NULL")
    int updateMuted(
            @Param("conversation") Conversation conversation,
            @Param("user") User user,
            @Param("muted") boolean muted);

    /**
     * Update participant role
     */
    @Modifying
    @Query("""
        UPDATE ConversationParticipant p 
        SET p.role = :newRole
        WHERE p.conversation = :conversation 
        AND p.user = :user 
        AND p.leftAt IS NULL
        """)
    int updateRole(
            @Param("conversation") Conversation conversation,
            @Param("user") User user,
            @Param("newRole") ParticipantRole newRole);

    // ==================== SEARCH AND FILTERING ====================

    /**
     * Search participants by name in conversation
     * Used for member search functionality
     */
    @Query("""
        SELECT p FROM ConversationParticipant p
        JOIN FETCH p.user u
        WHERE p.conversation = :conversation 
        AND p.leftAt IS NULL
        AND (
            LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        )
        ORDER BY u.firstName, u.lastName
        """)
    List<ConversationParticipant> searchParticipants(
            @Param("conversation") Conversation conversation,
            @Param("searchTerm") String searchTerm);

    /**
     * Find participants by conversation type for user
     */
    @Query("""
        SELECT p FROM ConversationParticipant p
        JOIN FETCH p.conversation c
        WHERE p.user = :user 
        AND p.leftAt IS NULL
        AND c.type = :conversationType
        ORDER BY c.updatedAt DESC
        """)
    List<ConversationParticipant> findParticipationsByConversationType(
            @Param("user") User user,
            @Param("conversationType") String conversationType);

    // ==================== CLEANUP OPERATIONS ====================

    /**
     * Find participants who left before a certain date
     * Used for cleanup operations
     */
    @Query("""
        SELECT p FROM ConversationParticipant p
        WHERE p.leftAt IS NOT NULL 
        AND p.leftAt < :cutoffDate
        """)
    List<ConversationParticipant> findLeftParticipantsBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Check if user exists as participant (active or inactive)
     */
    @Query("""
        SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
        FROM ConversationParticipant p
        WHERE p.conversation = :conversation 
        AND p.user = :user
        """)
    boolean existsByConversationAndUser(
            @Param("conversation") Conversation conversation,
            @Param("user") User user);

    /**
     * Find participations with pagination for user
     */
    @Query("""
        SELECT p FROM ConversationParticipant p
        JOIN FETCH p.conversation c
        WHERE p.user = :user 
        AND p.leftAt IS NULL
        ORDER BY c.updatedAt DESC
        """)
    Page<ConversationParticipant> findActiveParticipationsForUser(
            @Param("user") User user,
            Pageable pageable);
}