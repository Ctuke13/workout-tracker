package com.chidituke.workout_tracker.repository.messaging;

import com.chidituke.workout_tracker.model.messaging.Conversation;
import com.chidituke.workout_tracker.model.messaging.enums.ConversationType;
import com.chidituke.workout_tracker.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Conversation entity operations
 * Provides optimized queries for conversation management and retrieval
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // ==================== BASIC CONVERSATION QUERIES ====================

    /**
     * Find all conversations where user is an active participant
     * Ordered by most recent activity first
     */
    @Query("""
        SELECT DISTINCT c FROM Conversation c
        JOIN c.participants p
        WHERE p.user = :user 
        AND p.leftAt IS NULL
        ORDER BY c.updatedAt DESC
        """)
    List<Conversation> findActiveConversationsForUser(@Param("user") User user);

    /**
     * Find conversations for user with pagination
     * Optimized for conversation list display
     */
    @Query("""
        SELECT DISTINCT c FROM Conversation c
        JOIN FETCH c.participants p
        LEFT JOIN FETCH c.messages m
        WHERE p.user = :user 
        AND p.leftAt IS NULL
        ORDER BY c.updatedAt DESC
        """)
    Page<Conversation> findActiveConversationsForUser(@Param("user") User user, Pageable pageable);

    /**
     * Find starred conversations for a user
     * Used for priority conversation display
     */
    @Query("""
        SELECT DISTINCT c FROM Conversation c
        JOIN c.participants p
        WHERE p.user = :user 
        AND p.leftAt IS NULL
        AND p.isStarred = true
        ORDER BY c.updatedAt DESC
        """)
    List<Conversation> findStarredConversationsForUser(@Param("user") User user);

    // ==================== DIRECT CONVERSATION QUERIES ====================

    /**
     * Find existing direct conversation between two users
     * Essential for preventing duplicate direct conversations
     */
    @Query("""
        SELECT c FROM Conversation c
        WHERE c.type = 'DIRECT'
        AND c.id IN (
            SELECT p1.conversation.id FROM ConversationParticipant p1
            WHERE p1.user = :user1 AND p1.leftAt IS NULL
        )
        AND c.id IN (
            SELECT p2.conversation.id FROM ConversationParticipant p2
            WHERE p2.user = :user2 AND p2.leftAt IS NULL
        )
        """)
    Optional<Conversation> findDirectConversationBetweenUsers(
            @Param("user1") User user1,
            @Param("user2") User user2);

    /**
     * Check if direct conversation exists between users
     * Lightweight existence check
     */
    @Query("""
        SELECT COUNT(c) > 0 FROM Conversation c
        WHERE c.type = 'DIRECT'
        AND c.id IN (
            SELECT p1.conversation.id FROM ConversationParticipant p1
            WHERE p1.user = :user1 AND p1.leftAt IS NULL
        )
        AND c.id IN (
            SELECT p2.conversation.id FROM ConversationParticipant p2
            WHERE p2.user = :user2 AND p2.leftAt IS NULL
        )
        """)
    boolean existsDirectConversationBetweenUsers(
            @Param("user1") User user1,
            @Param("user2") User user2);

    // ==================== GROUP CONVERSATION QUERIES ====================

    /**
     * Find group conversations where user is active participant
     */
    @Query("""
        SELECT DISTINCT c FROM Conversation c
        JOIN c.participants p
        WHERE c.type = 'GROUP'
        AND p.user = :user 
        AND p.leftAt IS NULL
        ORDER BY c.updatedAt DESC
        """)
    List<Conversation> findGroupConversationsForUser(@Param("user") User user);

    /**
     * Find group conversations by name pattern
     * Used for group search functionality
     */
    @Query("""
        SELECT DISTINCT c FROM Conversation c
        JOIN c.participants p
        WHERE c.type = 'GROUP'
        AND p.user = :user 
        AND p.leftAt IS NULL
        AND LOWER(c.name) LIKE LOWER(CONCAT('%', :namePattern, '%'))
        ORDER BY c.updatedAt DESC
        """)
    List<Conversation> findGroupConversationsByName(
            @Param("user") User user,
            @Param("namePattern") String namePattern);

    // ==================== CONVERSATION DETAILS ====================

    /**
     * Find conversation with all participants loaded
     * Optimized for conversation detail views
     */
    @Query("""
        SELECT c FROM Conversation c
        LEFT JOIN FETCH c.participants p
        LEFT JOIN FETCH p.user
        WHERE c.id = :conversationId
        """)
    Optional<Conversation> findWithParticipants(@Param("conversationId") Long conversationId);

    /**
     * Find conversation with recent messages
     * Used for conversation preview with message history
     */
    @Query("""
        SELECT c FROM Conversation c
        LEFT JOIN FETCH c.participants p
        LEFT JOIN FETCH c.messages m
        WHERE c.id = :conversationId
        ORDER BY m.createdAt DESC
        """)
    Optional<Conversation> findWithRecentMessages(@Param("conversationId") Long conversationId);

    // ==================== ACTIVITY AND ANALYTICS ====================

    /**
     * Find conversations with recent activity
     * Used for notification and active conversation tracking
     */
    @Query("""
        SELECT DISTINCT c FROM Conversation c
        JOIN c.participants p
        WHERE p.user = :user 
        AND p.leftAt IS NULL
        AND c.updatedAt > :since
        ORDER BY c.updatedAt DESC
        """)
    List<Conversation> findConversationsWithActivitySince(
            @Param("user") User user,
            @Param("since") LocalDateTime since);

    /**
     * Count unread conversations for user
     * Based on participant's last seen timestamp vs conversation activity
     */
    @Query("""
        SELECT COUNT(DISTINCT c) FROM Conversation c
        JOIN c.participants p
        WHERE p.user = :user 
        AND p.leftAt IS NULL
        AND (p.lastSeenAt IS NULL OR c.updatedAt > p.lastSeenAt)
        """)
    long countUnreadConversationsForUser(@Param("user") User user);

    /**
     * Find conversations by type for user
     */
    @Query("""
        SELECT DISTINCT c FROM Conversation c
        JOIN c.participants p
        WHERE p.user = :user 
        AND p.leftAt IS NULL
        AND c.type = :type
        ORDER BY c.updatedAt DESC
        """)
    List<Conversation> findConversationsByTypeForUser(
            @Param("user") User user,
            @Param("type") ConversationType type);

    // ==================== PROFESSIONAL QUERIES ====================

    /**
     * Find conversations where user has professional context
     * Used for professional inbox organization
     */
    @Query("SELECT DISTINCT c FROM Conversation c " +
            "JOIN c.participants p1 " +
            "JOIN c.participants p2 " +
            "WHERE p1.user = :professional " +
            "AND p1.leftAt IS NULL " +
            "AND p2.user != :professional " +
            "AND p2.leftAt IS NULL " +
            "AND p2.user.userType = 'PRO_PROFESSIONAL' " +
            "ORDER BY c.updatedAt DESC")
    List<Conversation> findProfessionalConversationsForUser(@Param("professional") User professional);

    // ==================== SEARCH AND FILTERING ====================

    /**
     * Search conversations by participant name or conversation name
     */
    @Query("""
        SELECT DISTINCT c FROM Conversation c
        JOIN c.participants p
        LEFT JOIN c.participants otherP ON otherP.conversation = c AND otherP.user != :user
        WHERE p.user = :user 
        AND p.leftAt IS NULL
        AND (
            LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(otherP.user.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(otherP.user.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        )
        ORDER BY c.updatedAt DESC
        """)
    List<Conversation> searchConversations(
            @Param("user") User user,
            @Param("searchTerm") String searchTerm);

    /**
     * Find conversations with media messages
     * Used for media gallery features
     */
    @Query("""
        SELECT DISTINCT c FROM Conversation c
        JOIN c.participants p
        WHERE p.user = :user 
        AND p.leftAt IS NULL
        AND EXISTS (
            SELECT 1 FROM Message m 
            WHERE m.conversation = c 
            AND m.messageType IN ('IMAGE', 'VIDEO', 'AUDIO')
        )
        ORDER BY c.updatedAt DESC
        """)
    List<Conversation> findConversationsWithMedia(@Param("user") User user);

    // ==================== CLEANUP AND MAINTENANCE ====================

    /**
     * Find inactive conversations (no activity for specified days)
     * Used for cleanup and archival processes
     */
    @Query("""
        SELECT c FROM Conversation c
        WHERE c.updatedAt < :cutoffDate
        ORDER BY c.updatedAt ASC
        """)
    List<Conversation> findInactiveConversations(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find conversations with no active participants
     * Used for cleanup of abandoned conversations
     */
    @Query("""
        SELECT c FROM Conversation c
        WHERE NOT EXISTS (
            SELECT 1 FROM ConversationParticipant p 
            WHERE p.conversation = c AND p.leftAt IS NULL
        )
        """)
    List<Conversation> findAbandonedConversations();

    // ==================== BATCH OPERATIONS ====================

    /**
     * Find conversations for multiple users (batch operation)
     */
    @Query("""
        SELECT DISTINCT c FROM Conversation c
        JOIN c.participants p
        WHERE p.user IN :users 
        AND p.leftAt IS NULL
        ORDER BY c.updatedAt DESC
        """)
    List<Conversation> findConversationsForUsers(@Param("users") List<User> users);

    /**
     * Count total conversations for user
     */
    @Query("""
        SELECT COUNT(DISTINCT c) FROM Conversation c
        JOIN c.participants p
        WHERE p.user = :user AND p.leftAt IS NULL
        """)
    long countConversationsForUser(@Param("user") User user);
}