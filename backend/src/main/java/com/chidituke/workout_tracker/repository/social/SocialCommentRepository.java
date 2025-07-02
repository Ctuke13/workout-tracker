package com.chidituke.workout_tracker.repository.social;

import com.chidituke.workout_tracker.model.social.SocialComment;
import com.chidituke.workout_tracker.model.social.SocialPost;
import com.chidituke.workout_tracker.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SocialCommentRepository extends JpaRepository<SocialComment, Long> {

    // ==================== BASIC CRUD OPERATIONS ====================

    Optional<SocialComment> findByIdAndIsActiveTrue(Long id);

    List<SocialComment> findByAuthorAndIsActiveTrueOrderByCreatedAtDesc(User author);

    Page<SocialComment> findByAuthorAndIsActiveTrueOrderByCreatedAtDesc(User author, Pageable pageable);

    // ==================== COMMENT THREAD OPERATIONS ====================

    // Get top-level comments for a post (no parent)
    @Query("SELECT sc FROM SocialComment sc WHERE sc.post = :post " +
            "AND sc.parentComment IS NULL AND sc.isActive = true " +
            "AND sc.moderationStatus = 'APPROVED' " +
            "ORDER BY sc.createdAt ASC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<SocialComment> findTopLevelCommentsByPost(@Param("post") SocialPost post, Pageable pageable);

    // Get direct replies to a comment
    @Query("SELECT sc FROM SocialComment sc WHERE sc.parentComment = :parentComment " +
            "AND sc.isActive = true AND sc.moderationStatus = 'APPROVED' " +
            "ORDER BY sc.createdAt ASC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<SocialComment> findRepliesByParentComment(@Param("parentComment") SocialComment parentComment);

    // Get all comments for a post (flat list)
    @Query("SELECT sc FROM SocialComment sc WHERE sc.post = :post " +
            "AND sc.isActive = true AND sc.moderationStatus = 'APPROVED' " +
            "ORDER BY sc.createdAt ASC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<SocialComment> findAllCommentsByPost(@Param("post") SocialPost post, Pageable pageable);

    // ==================== ENGAGEMENT & POPULARITY ====================

    // Find popular comments (high likes/replies)
    @Query("SELECT sc FROM SocialComment sc WHERE sc.post = :post " +
            "AND sc.isActive = true AND sc.moderationStatus = 'APPROVED' " +
            "AND (sc.likesCount >= :minLikes OR sc.repliesCount >= :minReplies) " +
            "ORDER BY (sc.likesCount + sc.repliesCount * 2) DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<SocialComment> findPopularCommentsByPost(@Param("post") SocialPost post,
                                                  @Param("minLikes") Integer minLikes,
                                                  @Param("minReplies") Integer minReplies,
                                                  Pageable pageable);

    // Recent comments by user
    @Query("SELECT sc FROM SocialComment sc WHERE sc.author = :author " +
            "AND sc.isActive = true " +
            "ORDER BY sc.createdAt DESC")
    Page<SocialComment> findRecentCommentsByAuthor(@Param("author") User author, Pageable pageable);

    // ==================== ANALYTICS QUERIES ====================

    // Count comments by author in date range
    @Query("SELECT COUNT(sc) FROM SocialComment sc WHERE sc.author = :author " +
            "AND sc.isActive = true AND sc.createdAt >= :startDate")
    Long countActiveCommentsByAuthorSince(@Param("author") User author, @Param("startDate") LocalDateTime startDate);

    // Count total comments for a post
    @Query("SELECT COUNT(sc) FROM SocialComment sc WHERE sc.post = :post " +
            "AND sc.isActive = true AND sc.moderationStatus = 'APPROVED'")
    Long countActiveCommentsByPost(@Param("post") SocialPost post);

    // Count replies for a comment
    @Query("SELECT COUNT(sc) FROM SocialComment sc WHERE sc.parentComment = :parentComment " +
            "AND sc.isActive = true AND sc.moderationStatus = 'APPROVED'")
    Long countActiveRepliesByParentComment(@Param("parentComment") SocialComment parentComment);

    // Average engagement for user's comments
    @Query("SELECT AVG(sc.likesCount) FROM SocialComment sc WHERE sc.author = :author " +
            "AND sc.isActive = true AND sc.createdAt >= :startDate")
    Double getAverageLikesForAuthorSince(@Param("author") User author, @Param("startDate") LocalDateTime startDate);

    // Total engagement for author
    @Query("SELECT SUM(sc.likesCount + sc.repliesCount) FROM SocialComment sc " +
            "WHERE sc.author = :author AND sc.isActive = true")
    Long getTotalEngagementForAuthor(@Param("author") User author);

    // ==================== SEARCH & FILTERING ====================

    // Search comments by content
    @Query("SELECT sc FROM SocialComment sc WHERE sc.isActive = true " +
            "AND sc.moderationStatus = 'APPROVED' " +
            "AND LOWER(sc.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "ORDER BY sc.createdAt DESC")
    Page<SocialComment> searchCommentsByContent(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Find comments mentioning a user
    @Query("SELECT sc FROM SocialComment sc WHERE sc.isActive = true " +
            "AND sc.moderationStatus = 'APPROVED' " +
            "AND :userId MEMBER OF sc.mentionedUserIds " +
            "ORDER BY sc.createdAt DESC")
    Page<SocialComment> findCommentsMentioningUser(@Param("userId") Long userId, Pageable pageable);

    // Find comments in date range
    @Query("SELECT sc FROM SocialComment sc WHERE sc.isActive = true " +
            "AND sc.moderationStatus = 'APPROVED' " +
            "AND sc.createdAt >= :startDate AND sc.createdAt <= :endDate " +
            "ORDER BY sc.createdAt DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<SocialComment> findCommentsInDateRange(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate,
                                                Pageable pageable);

    // ==================== MODERATION QUERIES ====================

    // Find comments needing moderation
    @Query("SELECT sc FROM SocialComment sc WHERE sc.moderationStatus = 'PENDING' " +
            "OR sc.moderationStatus = 'UNDER_REVIEW' " +
            "OR sc.flaggedCount >= 2 " +
            "ORDER BY sc.flaggedCount DESC, sc.createdAt ASC")
    Page<SocialComment> findCommentsNeedingModeration(Pageable pageable);

    // Find comments by moderation status
    @Query("SELECT sc FROM SocialComment sc WHERE sc.moderationStatus = :status " +
            "ORDER BY sc.createdAt ASC")
    Page<SocialComment> findCommentsByModerationStatus(@Param("status") SocialComment.ModerationStatus status, Pageable pageable);

    // Find flagged comments
    @Query("SELECT sc FROM SocialComment sc WHERE sc.flaggedCount > 0 " +
            "AND sc.isActive = true " +
            "ORDER BY sc.flaggedCount DESC, sc.createdAt DESC")
    Page<SocialComment> findFlaggedComments(Pageable pageable);

    // ==================== USER INTERACTION QUERIES ====================

    // Check if user has commented on a post
    @Query("SELECT CASE WHEN COUNT(sc) > 0 THEN true ELSE false END FROM SocialComment sc " +
            "WHERE sc.post = :post AND sc.author = :author AND sc.isActive = true")
    boolean hasUserCommentedOnPost(@Param("post") SocialPost post, @Param("author") User author);

    // Get user's comment on a specific post
    @Query("SELECT sc FROM SocialComment sc WHERE sc.post = :post " +
            "AND sc.author = :author AND sc.isActive = true " +
            "ORDER BY sc.createdAt DESC")
    List<SocialComment> findUserCommentsOnPost(@Param("post") SocialPost post, @Param("author") User author);

    // ==================== THREAD DEPTH & COMPLEXITY ====================

    // Find deeply nested comments (for performance monitoring)
    @Query("SELECT sc FROM SocialComment sc WHERE sc.parentComment IS NOT NULL " +
            "AND sc.isActive = true " +
            "AND (SELECT COUNT(parent) FROM SocialComment parent " +
            "     WHERE parent.parentComment = sc.parentComment) > :maxDepth")
    List<SocialComment> findDeeplyNestedComments(@Param("maxDepth") Integer maxDepth);

    // ==================== BULK OPERATIONS ====================

    // Deactivate all comments by author
    @Query("UPDATE SocialComment sc SET sc.isActive = false WHERE sc.author = :author")
    int deactivateAllCommentsByAuthor(@Param("author") User author);

    // Update moderation status for multiple comments
    @Query("UPDATE SocialComment sc SET sc.moderationStatus = :newStatus, sc.moderatedAt = :moderatedAt " +
            "WHERE sc.id IN :commentIds")
    int updateModerationStatusForComments(@Param("commentIds") List<Long> commentIds,
                                          @Param("newStatus") SocialComment.ModerationStatus newStatus,
                                          @Param("moderatedAt") LocalDateTime moderatedAt);

    // ==================== RELATIONSHIP-SPECIFIC QUERIES ====================

    // Find comments visible to user based on relationships
    @Query("SELECT sc FROM SocialComment sc " +
            "JOIN sc.post sp " +
            "WHERE sc.isActive = true AND sc.moderationStatus = 'APPROVED' " +
            "AND (sp.privacyLevel = 'PUBLIC' OR " +
            "     (sp.privacyLevel = 'FRIENDS_ONLY' AND sp.author IN " +
            "      (SELECT ur.following FROM UserRelationship ur WHERE ur.follower = :user " +
            "       AND ur.status = 'ACTIVE' AND ur.relationshipType IN ('FOLLOW', 'FRIEND'))) OR " +
            "     sp.author = :user) " +
            "ORDER BY sc.createdAt DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<SocialComment> findCommentsVisibleToUser(@Param("user") User user, Pageable pageable);

    // ==================== PERFORMANCE & STATISTICS ====================

    // Get comment activity for dashboard
    @Query("SELECT sc.author, COUNT(sc) as commentCount FROM SocialComment sc " +
            "WHERE sc.isActive = true AND sc.createdAt >= :startDate " +
            "GROUP BY sc.author " +
            "ORDER BY commentCount DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<Object[]> getTopCommentersInPeriod(@Param("startDate") LocalDateTime startDate);

    // Check comment existence for performance
    boolean existsByPostAndAuthor(SocialPost post, User author);

    boolean existsByIdAndIsActiveTrue(Long id);

    // Count methods for pagination
    long countByPostAndIsActiveTrueAndModerationStatus(SocialPost post, SocialComment.ModerationStatus status);

    long countByAuthorAndIsActiveTrue(User author);
}