package com.chidituke.workout_tracker.repository;

import com.chidituke.workout_tracker.model.PostLike;
import com.chidituke.workout_tracker.model.SocialPost;
import com.chidituke.workout_tracker.model.User;
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
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    // ==================== BASIC LIKE OPERATIONS ====================

    // Check if user liked a specific post
    boolean existsByPostAndUser(SocialPost post, User user);

    // Find specific like record
    Optional<PostLike> findByPostAndUser(SocialPost post, User user);

    // Get all likes for a post
    @Query("SELECT pl FROM PostLike pl WHERE pl.post = :post ORDER BY pl.createdAt DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<PostLike> findLikesByPost(@Param("post") SocialPost post, Pageable pageable);

    // Get all posts liked by a user
    @Query("SELECT pl FROM PostLike pl WHERE pl.user = :user " +
            "AND pl.post.isActive = true AND pl.post.moderationStatus = 'APPROVED' " +
            "ORDER BY pl.createdAt DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<PostLike> findLikesByUser(@Param("user") User user, Pageable pageable);

    // ==================== ANALYTICS QUERIES ====================

    // Count total likes for a post
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post = :post")
    Long countLikesByPost(@Param("post") SocialPost post);

    // Count total likes given by a user
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.user = :user")
    Long countLikesByUser(@Param("user") User user);

    // Count likes received by user's posts
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.author = :author")
    Long countLikesReceivedByAuthor(@Param("author") User author);

    // Get likes in date range
    @Query("SELECT pl FROM PostLike pl WHERE pl.createdAt >= :startDate " +
            "AND pl.createdAt <= :endDate ORDER BY pl.createdAt DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<PostLike> findLikesInDateRange(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate,
                                        Pageable pageable);

    // Count likes for user's posts since date
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.author = :author " +
            "AND pl.createdAt >= :startDate")
    Long countLikesReceivedByAuthorSince(@Param("author") User author, @Param("startDate") LocalDateTime startDate);

    // Count likes given by user since date
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.user = :user " +
            "AND pl.createdAt >= :startDate")
    Long countLikesByUserSince(@Param("user") User user, @Param("startDate") LocalDateTime startDate);

    // ==================== ENGAGEMENT PATTERNS ====================

    // Find users who liked a specific post
    @Query("SELECT pl.user FROM PostLike pl WHERE pl.post = :post " +
            "ORDER BY pl.createdAt DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<User> findUsersWhoLikedPost(@Param("post") SocialPost post, Pageable pageable);

    // Find posts liked by user's followers (for recommendations)
    @Query("SELECT pl.post FROM PostLike pl " +
            "WHERE pl.user IN (SELECT ur.following FROM UserRelationship ur " +
            "WHERE ur.follower = :user AND ur.status = 'ACTIVE') " +
            "AND pl.post.isActive = true AND pl.post.moderationStatus = 'APPROVED' " +
            "AND pl.post.author != :user " +
            "ORDER BY pl.createdAt DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<SocialPost> findPostsLikedByFollowing(@Param("user") User user, Pageable pageable);

    // Find mutual likes (posts both users liked)
    @Query("SELECT pl1.post FROM PostLike pl1 " +
            "WHERE pl1.user = :user1 " +
            "AND EXISTS (SELECT pl2 FROM PostLike pl2 WHERE pl2.post = pl1.post AND pl2.user = :user2) " +
            "ORDER BY pl1.createdAt DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<SocialPost> findMutualLikes(@Param("user1") User user1, @Param("user2") User user2);

    // ==================== TRENDING & POPULAR CONTENT ====================

    // Find posts with most likes in time period
    @Query("SELECT pl.post, COUNT(pl) as likeCount FROM PostLike pl " +
            "WHERE pl.createdAt >= :startDate " +
            "AND pl.post.isActive = true AND pl.post.moderationStatus = 'APPROVED' " +
            "GROUP BY pl.post " +
            "ORDER BY COUNT(pl) DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<Object[]> findMostLikedPostsInPeriod(@Param("startDate") LocalDateTime startDate);

    // Find posts with rapid like growth
    @Query("SELECT pl.post, COUNT(pl) as recentLikes FROM PostLike pl " +
            "WHERE pl.createdAt >= :recentDate " +
            "AND pl.post.createdAt >= :postMinAge " +
            "AND pl.post.isActive = true AND pl.post.moderationStatus = 'APPROVED' " +
            "GROUP BY pl.post " +
            "HAVING COUNT(pl) >= :minLikes " +
            "ORDER BY COUNT(pl) DESC")
    List<Object[]> findTrendingPosts(@Param("recentDate") LocalDateTime recentDate,
                                     @Param("postMinAge") LocalDateTime postMinAge,
                                     @Param("minLikes") Long minLikes);

    // ==================== USER BEHAVIOR ANALYSIS ====================

    // Find most active likers
    @Query("SELECT pl.user, COUNT(pl) as likeCount FROM PostLike pl " +
            "WHERE pl.createdAt >= :startDate " +
            "GROUP BY pl.user " +
            "ORDER BY COUNT(pl) DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<Object[]> findTopLikersInPeriod(@Param("startDate") LocalDateTime startDate);

    // Find users who received most likes on their posts
    @Query("SELECT pl.post.author, COUNT(pl) as likesReceived FROM PostLike pl " +
            "WHERE pl.createdAt >= :startDate " +
            "GROUP BY pl.post.author " +
            "ORDER BY COUNT(pl) DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<Object[]> findTopLikeReceiversInPeriod(@Param("startDate") LocalDateTime startDate);

    // Find user's like activity pattern (hourly distribution)
    @Query("SELECT EXTRACT(HOUR FROM pl.createdAt) as hour, COUNT(pl) as likeCount " +
            "FROM PostLike pl WHERE pl.user = :user " +
            "GROUP BY EXTRACT(HOUR FROM pl.createdAt) " +
            "ORDER BY EXTRACT(HOUR FROM pl.createdAt)")
    List<Object[]> findUserLikeActivityPattern(@Param("user") User user);

    // ==================== CONTENT DISCOVERY ====================

    // Find posts liked by similar users (users who liked similar content)
    @Query("SELECT pl2.post, COUNT(pl2) as commonLikes FROM PostLike pl1 " +
            "JOIN PostLike pl2 ON pl1.user = pl2.user " +
            "WHERE pl1.post IN :userLikedPosts " +
            "AND pl2.post NOT IN :userLikedPosts " +
            "AND pl2.post.author != :targetUser " +
            "AND pl2.post.isActive = true AND pl2.post.moderationStatus = 'APPROVED' " +
            "GROUP BY pl2.post " +
            "ORDER BY COUNT(pl2) DESC")
    List<Object[]> findRecommendedPostsBasedOnLikes(@Param("userLikedPosts") List<SocialPost> userLikedPosts,
                                                    @Param("targetUser") User targetUser);

    // ==================== PRIVACY & RELATIONSHIP AWARE ====================

    // Find likes visible to user (respecting privacy)
    @Query("SELECT pl FROM PostLike pl " +
            "WHERE (pl.post.privacyLevel = 'PUBLIC' OR " +
            "       (pl.post.privacyLevel = 'FRIENDS_ONLY' AND pl.post.author IN " +
            "        (SELECT ur.following FROM UserRelationship ur WHERE ur.follower = :user " +
            "         AND ur.status = 'ACTIVE')) OR " +
            "       pl.post.author = :user) " +
            "AND pl.post.isActive = true AND pl.post.moderationStatus = 'APPROVED' " +
            "ORDER BY pl.createdAt DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<PostLike> findLikesVisibleToUser(@Param("user") User user, Pageable pageable);

    // ==================== PERFORMANCE & MAINTENANCE ====================

    // Check for orphaned likes (posts that no longer exist)
    @Query("SELECT pl FROM PostLike pl WHERE pl.post IS NULL")
    List<PostLike> findOrphanedLikes();

    // Bulk delete likes by user (for account deletion)
    @Query("DELETE FROM PostLike pl WHERE pl.user = :user")
    int deleteAllLikesByUser(@Param("user") User user);

    // Bulk delete likes for post (for post deletion)
    @Query("DELETE FROM PostLike pl WHERE pl.post = :post")
    int deleteAllLikesForPost(@Param("post") SocialPost post);

    // ==================== SIMPLE COUNT METHODS ====================

    // Basic count methods for pagination and statistics
    long countByPost(SocialPost post);
    long countByUser(User user);
    long countByPostAuthor(User author);

    // Check existence methods
    boolean existsByPost(SocialPost post);
    boolean existsByUser(User user);
}