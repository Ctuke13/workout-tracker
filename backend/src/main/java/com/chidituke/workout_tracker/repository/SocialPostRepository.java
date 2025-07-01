package com.chidituke.workout_tracker.repository;

import com.chidituke.workout_tracker.model.SocialPost;
import com.chidituke.workout_tracker.model.User;
import com.chidituke.workout_tracker.model.WorkoutSession;
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
public interface SocialPostRepository extends JpaRepository<SocialPost, Long> {

    // Basic CRUD operations following your patterns
    Optional<SocialPost> findByIdAndIsActiveTrue(Long id);

    List<SocialPost> findByAuthorAndIsActiveTrueOrderByCreatedAtDesc(User author);

    Page<SocialPost> findByAuthorAndIsActiveTrueOrderByCreatedAtDesc(User author, Pageable pageable);

    // Public feed posts (following your subscription-aware patterns)
    @Query("SELECT sp FROM SocialPost sp WHERE sp.privacyLevel = 'PUBLIC' " +
            "AND sp.isActive = true AND sp.moderationStatus = 'APPROVED' " +
            "ORDER BY sp.createdAt DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<SocialPost> findPublicFeedPosts(Pageable pageable);

    // Personalized feed posts using your sophisticated UserRelationship features
    @Query("SELECT sp FROM SocialPost sp WHERE sp.privacyLevel IN ('PUBLIC', 'FRIENDS') " +
            "AND sp.isActive = true AND sp.moderationStatus = 'APPROVED' " +
            "AND (sp.privacyLevel = 'PUBLIC' OR sp.author.id IN " +
            "(SELECT ur.following.id FROM UserRelationship ur WHERE ur.follower = :user " +
            "AND ur.status = 'ACTIVE' AND ur.relationshipType IN ('FOLLOW', 'FRIEND') " +
            "AND ur.showInFeed = true AND ur.muted = false)) " +
            "ORDER BY sp.createdAt DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<SocialPost> findPersonalizedFeedPosts(@Param("user") User user, Pageable pageable);

    // Smart feed with priority based on your interaction scoring
    @Query("SELECT sp FROM SocialPost sp " +
            "JOIN UserRelationship ur ON ur.following = sp.author " +
            "WHERE ur.follower = :user " +
            "AND ur.status = 'ACTIVE' AND ur.showInFeed = true AND ur.muted = false " +
            "AND sp.isActive = true AND sp.moderationStatus = 'APPROVED' " +
            "AND (sp.privacyLevel = 'PUBLIC' OR " +
            "(sp.privacyLevel = 'FRIENDS' AND ur.relationshipType IN ('FRIEND', 'FOLLOW'))) " +
            "ORDER BY ur.interactionScore DESC, sp.createdAt DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<SocialPost> findPriorityFeedPosts(@Param("user") User user, Pageable pageable);

    // Close friends posts only
    @Query("SELECT sp FROM SocialPost sp " +
            "JOIN UserRelationship ur ON ur.following = sp.author " +
            "WHERE ur.follower = :user " +
            "AND ur.status = 'ACTIVE' AND ur.closeFriend = true " +
            "AND sp.isActive = true AND sp.moderationStatus = 'APPROVED' " +
            "ORDER BY sp.createdAt DESC")
    Page<SocialPost> findCloseFriendsPosts(@Param("user") User user, Pageable pageable);

    // Posts by privacy level (following your filtering patterns)
    Page<SocialPost> findByPrivacyLevelAndIsActiveTrueAndModerationStatusOrderByCreatedAtDesc(
            SocialPost.PrivacyLevel privacyLevel,
            SocialPost.ModerationStatus moderationStatus,
            Pageable pageable);

    // Search posts by content with hashtag support
    @Query("SELECT sp FROM SocialPost sp WHERE sp.isActive = true " +
            "AND sp.moderationStatus = 'APPROVED' " +
            "AND (LOWER(sp.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR EXISTS (SELECT ph FROM PostHashtag ph WHERE ph.post = sp " +
            "AND LOWER(ph.hashtag) LIKE LOWER(CONCAT('%', :searchTerm, '%')))) " +
            "ORDER BY sp.createdAt DESC")
    Page<SocialPost> searchPostsByContent(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Analytics queries (following your analytics patterns)
    @Query("SELECT COUNT(sp) FROM SocialPost sp WHERE sp.author = :author " +
            "AND sp.isActive = true AND sp.createdAt >= :startDate")
    Long countActivePostsByAuthorSince(@Param("author") User author, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT sp FROM SocialPost sp WHERE sp.isActive = true " +
            "AND sp.moderationStatus = 'APPROVED' " +
            "AND sp.likesCount >= :minLikes " +
            "ORDER BY sp.likesCount DESC, sp.createdAt DESC")
    Page<SocialPost> findTrendingPosts(@Param("minLikes") Integer minLikes, Pageable pageable);

    // Moderation queries (following your comprehensive patterns)
    @Query("SELECT sp FROM SocialPost sp WHERE sp.moderationStatus = :status " +
            "ORDER BY sp.createdAt ASC")
    Page<SocialPost> findPostsByModerationStatus(@Param("status") SocialPost.ModerationStatus status, Pageable pageable);

    // User's liked posts
    @Query("SELECT pl.post FROM PostLike pl WHERE pl.user = :user " +
            "AND pl.post.isActive = true " +
            "ORDER BY pl.createdAt DESC")
    Page<SocialPost> findPostsLikedByUser(@Param("user") User user, Pageable pageable);

    // Popular posts in time range (following your performance tracking patterns)
    @Query("SELECT sp FROM SocialPost sp WHERE sp.isActive = true " +
            "AND sp.moderationStatus = 'APPROVED' " +
            "AND sp.createdAt >= :startDate AND sp.createdAt <= :endDate " +
            "ORDER BY (sp.likesCount + sp.commentsCount * 2 + sp.sharesCount * 3) DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<SocialPost> findPopularPostsInDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Posts with hashtags
    @Query("SELECT DISTINCT sp FROM SocialPost sp " +
            "JOIN PostHashtag ph ON ph.post = sp WHERE LOWER(ph.hashtag) = LOWER(:hashtag) " +
            "AND sp.isActive = true AND sp.moderationStatus = 'APPROVED' " +
            "ORDER BY sp.createdAt DESC")
    Page<SocialPost> findPostsByHashtag(@Param("hashtag") String hashtag, Pageable pageable);

    // Recent posts by author (for profile pages)
    @Query("SELECT sp FROM SocialPost sp WHERE sp.author = :author " +
            "AND sp.isActive = true " +
            "AND (:includePrivate = true OR sp.privacyLevel = 'PUBLIC') " +
            "ORDER BY sp.createdAt DESC")
    Page<SocialPost> findRecentPostsByAuthor(
            @Param("author") User author,
            @Param("includePrivate") boolean includePrivate,
            Pageable pageable);

    // Engagement statistics (following your analytics patterns)
    @Query("SELECT AVG(sp.likesCount) FROM SocialPost sp WHERE sp.author = :author " +
            "AND sp.isActive = true AND sp.createdAt >= :startDate")
    Double getAverageLikesForAuthorSince(@Param("author") User author, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT SUM(sp.likesCount + sp.commentsCount + sp.sharesCount) FROM SocialPost sp " +
            "WHERE sp.author = :author AND sp.isActive = true")
    Long getTotalEngagementForAuthor(@Param("author") User author);

    // Posts needing moderation
    @Query("SELECT sp FROM SocialPost sp WHERE sp.moderationStatus = 'PENDING' " +
            "OR (sp.moderationStatus = 'APPROVED' AND EXISTS " +
            "(SELECT cr FROM ContentReport cr WHERE cr.reportedPost = sp AND cr.status = 'OPEN')) " +
            "ORDER BY sp.createdAt ASC")
    Page<SocialPost> findPostsNeedingModeration(Pageable pageable);

    // Bulk operations (following your bulk patterns)
    @Query("UPDATE SocialPost sp SET sp.isActive = false WHERE sp.author = :author")
    int deactivateAllPostsByAuthor(@Param("author") User author);

    @Query("UPDATE SocialPost sp SET sp.moderationStatus = :newStatus WHERE sp.id IN :postIds")
    int updateModerationStatusForPosts(@Param("postIds") List<Long> postIds, @Param("newStatus") SocialPost.ModerationStatus newStatus);

    // Missing methods for WorkoutSharingService
    @Query("SELECT CASE WHEN COUNT(sp) > 0 THEN true ELSE false END FROM SocialPost sp " +
            "WHERE sp.workoutSession = :workoutSession AND sp.postType = :postType")
    boolean existsByWorkoutSessionAndPostType(@Param("workoutSession") WorkoutSession workoutSession, @Param("postType") SocialPost.PostType postType);

    @Query("SELECT COUNT(sp) FROM SocialPost sp WHERE sp.author = :author AND sp.postType = :postType")
    long countByAuthorAndPostType(@Param("author") User author, @Param("postType") SocialPost.PostType postType);
}