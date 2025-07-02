package com.chidituke.workout_tracker.repository.messaging;

import com.chidituke.workout_tracker.model.messaging.Conversation;
import com.chidituke.workout_tracker.model.messaging.Message;
import com.chidituke.workout_tracker.model.messaging.enums.MessageType;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.workout.WorkoutPlan;
import com.chidituke.workout_tracker.model.workout.WorkoutSession;
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
 * Repository for Message entity operations
 * Handles message loading, search, media queries, and content management
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // ==================== BASIC MESSAGE QUERIES ====================

    /**
     * Find messages in conversation with pagination
     * Core query for conversation message history
     */
    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        WHERE m.conversation = :conversation
        AND m.isFiltered = false
        ORDER BY m.createdAt DESC
        """)
    Page<Message> findByConversation(@Param("conversation") Conversation conversation, Pageable pageable);

    /**
     * Find all messages in conversation (including filtered)
     * Used for moderation and administrative purposes
     */
    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        WHERE m.conversation = :conversation
        ORDER BY m.createdAt DESC
        """)
    Page<Message> findAllByConversation(@Param("conversation") Conversation conversation, Pageable pageable);

    /**
     * Find recent messages in conversation
     * Used for conversation previews and quick loading
     */
    @Query("SELECT m FROM Message m " +
            "JOIN FETCH m.sender " +
            "WHERE m.conversation = :conversation " +
            "AND m.isFiltered = false " +
            "ORDER BY m.createdAt DESC")
    List<Message> findRecentMessages(
            @Param("conversation") Conversation conversation,
            Pageable pageable);

    /**
     * Find the most recent message in conversation
     * Essential for conversation list previews - use with PageRequest.of(0, 1)
     */
    @Query("SELECT m FROM Message m " +
            "WHERE m.conversation = :conversation " +
            "AND m.isFiltered = false " +
            "ORDER BY m.createdAt DESC")
    List<Message> findLatestMessage(@Param("conversation") Conversation conversation, Pageable pageable);

    // ==================== MESSAGE SEARCH ====================

    /**
     * Search messages by content in conversation
     * Core search functionality for message history
     */
    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        WHERE m.conversation = :conversation
        AND m.isFiltered = false
        AND LOWER(m.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        ORDER BY m.createdAt DESC
        """)
    Page<Message> searchInConversation(
            @Param("conversation") Conversation conversation,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    /**
     * Search messages across all user's conversations
     * Global message search functionality
     */
    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        JOIN m.conversation c
        JOIN c.participants p
        WHERE p.user = :user
        AND p.leftAt IS NULL
        AND m.isFiltered = false
        AND LOWER(m.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        ORDER BY m.createdAt DESC
        """)
    Page<Message> searchAcrossUserConversations(
            @Param("user") User user,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    /**
     * Find messages by type in conversation
     * Used for filtering media, workouts, etc.
     */
    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        WHERE m.conversation = :conversation
        AND m.messageType = :messageType
        AND m.isFiltered = false
        ORDER BY m.createdAt DESC
        """)
    Page<Message> findByConversationAndType(
            @Param("conversation") Conversation conversation,
            @Param("messageType") MessageType messageType,
            Pageable pageable);

    // ==================== MEDIA MESSAGE QUERIES ====================

    /**
     * Find media messages in conversation
     * Used for media gallery and file management
     */
    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        WHERE m.conversation = :conversation
        AND m.messageType IN ('IMAGE', 'VIDEO', 'AUDIO', 'FILE')
        AND m.mediaUrl IS NOT NULL
        AND m.isFiltered = false
        ORDER BY m.createdAt DESC
        """)
    Page<Message> findMediaMessages(@Param("conversation") Conversation conversation, Pageable pageable);

    /**
     * Find images in conversation
     * Specific media type filtering
     */
    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        WHERE m.conversation = :conversation
        AND m.messageType = 'IMAGE'
        AND m.mediaUrl IS NOT NULL
        AND m.isFiltered = false
        ORDER BY m.createdAt DESC
        """)
    List<Message> findImageMessages(@Param("conversation") Conversation conversation);

    /**
     * Find videos in conversation
     */
    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        WHERE m.conversation = :conversation
        AND m.messageType = 'VIDEO'
        AND m.mediaUrl IS NOT NULL
        AND m.isFiltered = false
        ORDER BY m.createdAt DESC
        """)
    List<Message> findVideoMessages(@Param("conversation") Conversation conversation);

    /**
     * Calculate total media size for conversation
     * Used for storage analytics and limits
     */
    @Query("""
        SELECT COALESCE(SUM(m.mediaSizeBytes), 0) FROM Message m
        WHERE m.conversation = :conversation
        AND m.mediaSizeBytes IS NOT NULL
        """)
    Long calculateTotalMediaSize(@Param("conversation") Conversation conversation);

    // ==================== WORKOUT INTEGRATION QUERIES ====================

    /**
     * Find messages with shared workout sessions
     * Workout sharing functionality
     */
    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        LEFT JOIN FETCH m.sharedWorkoutSession ws
        LEFT JOIN FETCH ws.workoutPlan
        WHERE m.conversation = :conversation
        AND m.sharedWorkoutSession IS NOT NULL
        AND m.isFiltered = false
        ORDER BY m.createdAt DESC
        """)
    List<Message> findWorkoutMessages(@Param("conversation") Conversation conversation);

    /**
     * Find messages with shared workout plans
     */
    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        LEFT JOIN FETCH m.sharedWorkoutPlan
        WHERE m.conversation = :conversation
        AND m.sharedWorkoutPlan IS NOT NULL
        AND m.isFiltered = false
        ORDER BY m.createdAt DESC
        """)
    List<Message> findWorkoutPlanMessages(@Param("conversation") Conversation conversation);

    /**
     * Find messages sharing specific workout plan
     * Used for tracking workout plan distribution
     */
    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        WHERE m.sharedWorkoutPlan = :workoutPlan
        AND m.isFiltered = false
        ORDER BY m.createdAt DESC
        """)
    List<Message> findBySharedWorkoutPlan(@Param("workoutPlan") WorkoutPlan workoutPlan);

    /**
     * Find messages sharing specific workout session
     */
    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        WHERE m.sharedWorkoutSession = :workoutSession
        AND m.isFiltered = false
        ORDER BY m.createdAt DESC
        """)
    List<Message> findBySharedWorkoutSession(@Param("workoutSession") WorkoutSession workoutSession);

    // ==================== USER MESSAGE QUERIES ====================

    /**
     * Find messages sent by user in conversation
     * Used for user activity tracking
     */
    @Query("""
        SELECT m FROM Message m
        WHERE m.conversation = :conversation
        AND m.sender = :user
        AND m.isFiltered = false
        ORDER BY m.createdAt DESC
        """)
    Page<Message> findByConversationAndSender(
            @Param("conversation") Conversation conversation,
            @Param("user") User user,
            Pageable pageable);

    /**
     * Count messages sent by user in conversation
     */
    @Query("""
        SELECT COUNT(m) FROM Message m
        WHERE m.conversation = :conversation
        AND m.sender = :user
        AND m.isFiltered = false
        """)
    long countMessagesByUserInConversation(
            @Param("conversation") Conversation conversation,
            @Param("user") User user);

    /**
     * Find user's recent messages across all conversations
     * Used for user activity overview
     */
    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.conversation c
        WHERE m.sender = :user
        AND m.createdAt > :since
        AND m.isFiltered = false
        ORDER BY m.createdAt DESC
        """)
    List<Message> findRecentMessagesByUser(
            @Param("user") User user,
            @Param("since") LocalDateTime since);

    // ==================== UNREAD MESSAGE QUERIES ====================

    /**
     * Count unread messages for user in conversation
     * Based on participant's last seen timestamp
     */
    @Query("""
        SELECT COUNT(m) FROM Message m
        JOIN m.conversation c
        JOIN c.participants p
        WHERE c = :conversation
        AND p.user = :user
        AND p.leftAt IS NULL
        AND m.sender != :user
        AND (p.lastSeenAt IS NULL OR m.createdAt > p.lastSeenAt)
        AND m.isFiltered = false
        """)
    long countUnreadMessages(
            @Param("conversation") Conversation conversation,
            @Param("user") User user);

    /**
     * Find unread messages for user in conversation
     */
    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        JOIN m.conversation c
        JOIN c.participants p
        WHERE c = :conversation
        AND p.user = :user
        AND p.leftAt IS NULL
        AND m.sender != :user
        AND (p.lastSeenAt IS NULL OR m.createdAt > p.lastSeenAt)
        AND m.isFiltered = false
        ORDER BY m.createdAt ASC
        """)
    List<Message> findUnreadMessages(
            @Param("conversation") Conversation conversation,
            @Param("user") User user);

    /**
     * Count total unread messages across all user's conversations
     */
    @Query("""
        SELECT COUNT(m) FROM Message m
        JOIN m.conversation c
        JOIN c.participants p
        WHERE p.user = :user
        AND p.leftAt IS NULL
        AND m.sender != :user
        AND (p.lastSeenAt IS NULL OR m.createdAt > p.lastSeenAt)
        AND m.isFiltered = false
        """)
    long countTotalUnreadMessages(@Param("user") User user);

    // ==================== CONTENT MODERATION ====================

    /**
     * Find filtered messages in conversation
     * Used for moderation review
     */
    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        WHERE m.conversation = :conversation
        AND m.isFiltered = true
        ORDER BY m.createdAt DESC
        """)
    Page<Message> findFilteredMessages(@Param("conversation") Conversation conversation, Pageable pageable);

    /**
     * Find messages pending moderation
     * Based on content filtering flags
     */
    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        WHERE m.isFiltered = true
        AND m.filterReason IS NOT NULL
        ORDER BY m.createdAt DESC
        """)
    Page<Message> findPendingModerationMessages(Pageable pageable);

    /**
     * Find messages by filter reason
     */
    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        WHERE m.isFiltered = true
        AND m.filterReason = :reason
        ORDER BY m.createdAt DESC
        """)
    List<Message> findByFilterReason(@Param("reason") String reason);

    // ==================== MESSAGE ANALYTICS ====================

    /**
     * Find messages created in time range
     * Used for activity analytics
     */
    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        WHERE m.conversation = :conversation
        AND m.createdAt BETWEEN :startTime AND :endTime
        AND m.isFiltered = false
        ORDER BY m.createdAt DESC
        """)
    List<Message> findByConversationAndTimeRange(
            @Param("conversation") Conversation conversation,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Count messages by type in conversation
     * Analytics for message type distribution
     */
    @Query("""
        SELECT m.messageType, COUNT(m) FROM Message m
        WHERE m.conversation = :conversation
        AND m.isFiltered = false
        GROUP BY m.messageType
        """)
    List<Object[]> countMessagesByType(@Param("conversation") Conversation conversation);

    /**
     * Find most active senders in conversation
     */
    @Query("""
        SELECT m.sender, COUNT(m) as messageCount FROM Message m
        WHERE m.conversation = :conversation
        AND m.isFiltered = false
        GROUP BY m.sender
        ORDER BY messageCount DESC
        """)
    List<Object[]> findMostActiveSenders(@Param("conversation") Conversation conversation);

    // ==================== BULK OPERATIONS ====================

    /**
     * Update message filter status
     */
    @Modifying
    @Query("UPDATE Message m " +
            "SET m.isFiltered = :isFiltered, m.filterReason = :reason " +
            "WHERE m.id = :messageId")
    int updateFilterStatus(
            @Param("messageId") Long messageId,
            @Param("isFiltered") boolean isFiltered,
            @Param("reason") String reason);

    /**
     * Delete messages older than specified date
     * Used for cleanup operations (respecting 3-year retention policy)
     */
    @Modifying
    @Query("DELETE FROM Message m " +
            "WHERE m.createdAt < :cutoffDate")
    int deleteMessagesOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find messages for conversation cleanup
     * Identify old messages for archival
     */
    @Query("""
        SELECT m.id, m.createdAt, m.messageType, m.mediaSizeBytes 
        FROM Message m
        WHERE m.conversation = :conversation
        AND m.createdAt < :cutoffDate
        ORDER BY m.createdAt ASC
        """)
    List<Object[]> findMessagesForCleanup(
            @Param("conversation") Conversation conversation,
            @Param("cutoffDate") LocalDateTime cutoffDate);

    // ==================== PERFORMANCE OPTIMIZED QUERIES ====================

    /**
     * Find conversation messages with minimal data for listing
     * Optimized for conversation previews
     */
    @Query("""
        SELECT m.id, m.content, m.messageType, m.createdAt, 
               m.sender.firstName, m.sender.lastName, m.isFiltered
        FROM Message m
        WHERE m.conversation = :conversation
        AND m.isFiltered = false
        ORDER BY m.createdAt DESC
        """)
    Page<Object[]> findConversationMessageSummary(
            @Param("conversation") Conversation conversation,
            Pageable pageable);

    /**
     * Check if conversation has any messages
     * Lightweight existence check
     */
    @Query("""
        SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
        FROM Message m
        WHERE m.conversation = :conversation
        """)
    boolean hasMessages(@Param("conversation") Conversation conversation);

    /**
     * Find messages around a specific message (context loading)
     * Used for "jump to message" functionality
     */
    @Query(value = """
        (SELECT * FROM messages 
         WHERE conversation_id = :conversationId 
         AND message_id < :messageId 
         AND is_filtered = false
         ORDER BY created_at DESC 
         LIMIT :beforeCount)
        UNION ALL
        (SELECT * FROM messages 
         WHERE conversation_id = :conversationId 
         AND message_id >= :messageId 
         AND is_filtered = false
         ORDER BY created_at ASC 
         LIMIT :afterCount)
        ORDER BY created_at ASC
        """, nativeQuery = true)
    List<Message> findMessagesAroundMessage(
            @Param("conversationId") Long conversationId,
            @Param("messageId") Long messageId,
            @Param("beforeCount") int beforeCount,
            @Param("afterCount") int afterCount);
}