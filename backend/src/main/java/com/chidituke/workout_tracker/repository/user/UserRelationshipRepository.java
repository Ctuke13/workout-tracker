package com.chidituke.workout_tracker.repository.user;

import com.chidituke.workout_tracker.model.user.UserRelationship;
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
public interface UserRelationshipRepository extends JpaRepository<UserRelationship, Long> {

    // ==================== BASIC RELATIONSHIP OPERATIONS ====================

    // Find specific relationship between two users
    Optional<UserRelationship> findByFollowerAndFollowing(User follower, User following);

    // Check if relationship exists
    boolean existsByFollowerAndFollowing(User follower, User following);

    // Find active relationships
    @Query("SELECT ur FROM UserRelationship ur WHERE ur.follower = :follower " +
            "AND ur.following = :following AND ur.status = 'ACTIVE'")
    Optional<UserRelationship> findActiveRelationship(@Param("follower") User follower, @Param("following") User following);

    // ==================== FOLLOWING OPERATIONS ====================

    // Get all users that a user is following
    @Query("SELECT ur.following FROM UserRelationship ur WHERE ur.follower = :user " +
            "AND ur.status = 'ACTIVE' AND ur.relationshipType IN ('FOLLOW', 'FRIEND') " +
            "ORDER BY ur.interactionScore DESC, ur.updatedAt DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<User> findUsersFollowedBy(@Param("user") User user, Pageable pageable);

    // Get all followers of a user
    @Query("SELECT ur.follower FROM UserRelationship ur WHERE ur.following = :user " +
            "AND ur.status = 'ACTIVE' AND ur.relationshipType IN ('FOLLOW', 'FRIEND') " +
            "ORDER BY ur.updatedAt DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<User> findFollowersOf(@Param("user") User user, Pageable pageable);

    // Count following
    @Query("SELECT COUNT(ur) FROM UserRelationship ur WHERE ur.follower = :user " +
            "AND ur.status = 'ACTIVE' AND ur.relationshipType IN ('FOLLOW', 'FRIEND')")
    Long countUsersFollowedBy(@Param("user") User user);

    // Count followers
    @Query("SELECT COUNT(ur) FROM UserRelationship ur WHERE ur.following = :user " +
            "AND ur.status = 'ACTIVE' AND ur.relationshipType IN ('FOLLOW', 'FRIEND')")
    Long countFollowersOf(@Param("user") User user);

    // ==================== FRIEND OPERATIONS ====================

    // Get mutual friends (both users follow each other as friends)
    @Query("SELECT ur1.following FROM UserRelationship ur1 " +
            "WHERE ur1.follower = :user AND ur1.relationshipType = 'FRIEND' AND ur1.status = 'ACTIVE' " +
            "AND EXISTS (SELECT ur2 FROM UserRelationship ur2 " +
            "WHERE ur2.follower = ur1.following AND ur2.following = :user " +
            "AND ur2.relationshipType = 'FRIEND' AND ur2.status = 'ACTIVE') " +
            "ORDER BY ur1.interactionScore DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<User> findMutualFriends(@Param("user") User user, Pageable pageable);

    // Get pending friend requests sent by user
    @Query("SELECT ur FROM UserRelationship ur WHERE ur.follower = :user " +
            "AND ur.relationshipType = 'FRIEND' AND ur.status = 'PENDING' " +
            "ORDER BY ur.createdAt DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<UserRelationship> findPendingFriendRequestsSentBy(@Param("user") User user, Pageable pageable);

    // Get pending friend requests received by user
    @Query("SELECT ur FROM UserRelationship ur WHERE ur.following = :user " +
            "AND ur.relationshipType = 'FRIEND' AND ur.status = 'PENDING' " +
            "ORDER BY ur.createdAt DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<UserRelationship> findPendingFriendRequestsReceivedBy(@Param("user") User user, Pageable pageable);

    // Count pending friend requests
    @Query("SELECT COUNT(ur) FROM UserRelationship ur WHERE ur.following = :user " +
            "AND ur.relationshipType = 'FRIEND' AND ur.status = 'PENDING'")
    Long countPendingFriendRequestsFor(@Param("user") User user);

    // ==================== CLOSE FRIENDS OPERATIONS ====================

    // Get close friends (with closeFriend = true)
    @Query("SELECT ur.following FROM UserRelationship ur WHERE ur.follower = :user " +
            "AND ur.status = 'ACTIVE' AND ur.closeFriend = true " +
            "ORDER BY ur.interactionScore DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<User> findCloseFriends(@Param("user") User user, Pageable pageable);

    // Count close friends
    @Query("SELECT COUNT(ur) FROM UserRelationship ur WHERE ur.follower = :user " +
            "AND ur.status = 'ACTIVE' AND ur.closeFriend = true")
    Long countCloseFriends(@Param("user") User user);

    // Find relationships eligible for close friends (active friends with high interaction)
    @Query("SELECT ur FROM UserRelationship ur WHERE ur.follower = :user " +
            "AND ur.status = 'ACTIVE' AND ur.relationshipType = 'FRIEND' " +
            "AND ur.interactionScore >= :minScore AND ur.closeFriend = false " +
            "ORDER BY ur.interactionScore DESC")
    List<UserRelationship> findPotentialCloseFriends(@Param("user") User user, @Param("minScore") Integer minScore);

    // ==================== MUTING & FEED CONTROL ====================

    // Get muted users
    @Query("SELECT ur.following FROM UserRelationship ur WHERE ur.follower = :user " +
            "AND ur.status = 'ACTIVE' AND ur.muted = true " +
            "ORDER BY ur.updatedAt DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<User> findMutedUsers(@Param("user") User user, Pageable pageable);

    // Get users who should appear in feed (not muted, showInFeed = true)
    @Query("SELECT ur.following FROM UserRelationship ur WHERE ur.follower = :user " +
            "AND ur.status = 'ACTIVE' AND ur.showInFeed = true AND ur.muted = false " +
            "ORDER BY ur.interactionScore DESC, ur.lastInteraction DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<User> findUsersForFeed(@Param("user") User user, Pageable pageable);

    // Get feed priority list (for smart feed algorithm)
    @Query("SELECT ur FROM UserRelationship ur WHERE ur.follower = :user " +
            "AND ur.status = 'ACTIVE' AND ur.showInFeed = true AND ur.muted = false " +
            "ORDER BY ur.interactionScore DESC, ur.closeFriend DESC, ur.lastInteraction DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<UserRelationship> findFeedPriorityRelationships(@Param("user") User user);

    // ==================== BLOCKING OPERATIONS ====================

    // Get blocked users
    @Query("SELECT ur.following FROM UserRelationship ur WHERE ur.follower = :user " +
            "AND ur.relationshipType = 'BLOCKED' AND ur.status = 'ACTIVE' " +
            "ORDER BY ur.updatedAt DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<User> findBlockedUsers(@Param("user") User user, Pageable pageable);

    // Check if user is blocked by another user
    @Query("SELECT CASE WHEN COUNT(ur) > 0 THEN true ELSE false END FROM UserRelationship ur " +
            "WHERE ur.follower = :blocker AND ur.following = :target " +
            "AND ur.relationshipType = 'BLOCKED' AND ur.status = 'ACTIVE'")
    boolean isUserBlockedBy(@Param("target") User target, @Param("blocker") User blocker);

    // Find if there's any blocking relationship between two users
    @Query("SELECT ur FROM UserRelationship ur WHERE " +
            "((ur.follower = :user1 AND ur.following = :user2) OR " +
            " (ur.follower = :user2 AND ur.following = :user1)) " +
            "AND ur.relationshipType = 'BLOCKED' AND ur.status = 'ACTIVE'")
    Optional<UserRelationship> findBlockingRelationshipBetween(@Param("user1") User user1, @Param("user2") User user2);

    // ==================== INTERACTION SCORING & ANALYTICS ====================

    // Find relationships needing interaction score updates
    @Query("SELECT ur FROM UserRelationship ur WHERE ur.status = 'ACTIVE' " +
            "AND ur.lastInteraction IS NOT NULL " +
            "AND ur.lastInteraction >= :cutoffDate " +
            "ORDER BY ur.lastInteraction DESC")
    List<UserRelationship> findRecentlyActiveRelationships(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Get top interactive relationships
    @Query("SELECT ur FROM UserRelationship ur WHERE ur.follower = :user " +
            "AND ur.status = 'ACTIVE' AND ur.interactionScore > 0 " +
            "ORDER BY ur.interactionScore DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<UserRelationship> findTopInteractiveRelationships(@Param("user") User user, Pageable pageable);

    // Find relationships with low engagement (for cleanup suggestions)
    @Query("SELECT ur FROM UserRelationship ur WHERE ur.follower = :user " +
            "AND ur.status = 'ACTIVE' AND ur.relationshipType != 'BLOCKED' " +
            "AND (ur.lastInteraction IS NULL OR ur.lastInteraction < :cutoffDate) " +
            "AND ur.interactionScore < :minScore " +
            "ORDER BY ur.interactionScore ASC")
    List<UserRelationship> findLowEngagementRelationships(@Param("user") User user,
                                                          @Param("cutoffDate") LocalDateTime cutoffDate,
                                                          @Param("minScore") Integer minScore);

    // ==================== MUTUAL CONNECTIONS & SUGGESTIONS ====================

    // Find mutual connections between two users
    @Query("SELECT ur1.following FROM UserRelationship ur1 " +
            "WHERE ur1.follower = :user1 AND ur1.status = 'ACTIVE' " +
            "AND EXISTS (SELECT ur2 FROM UserRelationship ur2 " +
            "WHERE ur2.follower = :user2 AND ur2.following = ur1.following AND ur2.status = 'ACTIVE') " +
            "ORDER BY ur1.interactionScore DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<User> findMutualConnections(@Param("user1") User user1, @Param("user2") User user2);

    // Count mutual connections
    @Query("SELECT COUNT(ur1.following) FROM UserRelationship ur1 " +
            "WHERE ur1.follower = :user1 AND ur1.status = 'ACTIVE' " +
            "AND EXISTS (SELECT ur2 FROM UserRelationship ur2 " +
            "WHERE ur2.follower = :user2 AND ur2.following = ur1.following AND ur2.status = 'ACTIVE')")
    Long countMutualConnections(@Param("user1") User user1, @Param("user2") User user2);

    // Find suggested connections (friends of friends)
    @Query("SELECT DISTINCT ur2.following FROM UserRelationship ur1 " +
            "JOIN UserRelationship ur2 ON ur1.following = ur2.follower " +
            "WHERE ur1.follower = :user AND ur1.status = 'ACTIVE' " +
            "AND ur2.status = 'ACTIVE' AND ur2.following != :user " +
            "AND NOT EXISTS (SELECT ur3 FROM UserRelationship ur3 " +
            "WHERE ur3.follower = :user AND ur3.following = ur2.following) " +
            "AND NOT EXISTS (SELECT ur4 FROM UserRelationship ur4 " +
            "WHERE ur4.follower = :user AND ur4.following = ur2.following " +
            "AND ur4.relationshipType = 'BLOCKED')")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<User> findSuggestedConnections(@Param("user") User user, Pageable pageable);

    // ==================== NOTIFICATION PREFERENCES ====================

    // Find users who should receive notifications from this user
    @Query("SELECT ur.following FROM UserRelationship ur WHERE ur.follower = :user " +
            "AND ur.status = 'ACTIVE' AND ur.notificationsEnabled = true")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<User> findUsersToNotify(@Param("user") User user);

    // Find relationships where user should receive notifications
    @Query("SELECT ur FROM UserRelationship ur WHERE ur.following = :user " +
            "AND ur.status = 'ACTIVE' AND ur.notificationsEnabled = true")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<UserRelationship> findNotificationEnabledRelationships(@Param("user") User user);

    // ==================== STATISTICS & ANALYTICS ====================

    // Get relationship statistics for user
    @Query("SELECT ur.relationshipType, ur.status, COUNT(ur) FROM UserRelationship ur " +
            "WHERE ur.follower = :user " +
            "GROUP BY ur.relationshipType, ur.status")
    List<Object[]> getRelationshipStatistics(@Param("user") User user);

    // Get most popular users (most followers)
    @Query("SELECT ur.following, COUNT(ur) as followerCount FROM UserRelationship ur " +
            "WHERE ur.status = 'ACTIVE' AND ur.relationshipType IN ('FOLLOW', 'FRIEND') " +
            "GROUP BY ur.following " +
            "ORDER BY COUNT(ur) DESC")
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<Object[]> findMostPopularUsers(Pageable pageable);

    // Get average interaction scores for user's relationships
    @Query("SELECT AVG(ur.interactionScore) FROM UserRelationship ur " +
            "WHERE ur.follower = :user AND ur.status = 'ACTIVE'")
    Double getAverageInteractionScore(@Param("user") User user);

    // ==================== BULK OPERATIONS ====================

    // Find all relationships for a user (for account deletion)
    @Query("SELECT ur FROM UserRelationship ur WHERE ur.follower = :user OR ur.following = :user")
    List<UserRelationship> findAllRelationshipsForUser(@Param("user") User user);

    // Update interaction scores for user's relationships
    @Query("UPDATE UserRelationship ur SET ur.interactionScore = ur.interactionScore + :increment " +
            "WHERE ur.follower = :user AND ur.status = 'ACTIVE'")
    int incrementInteractionScoresForUser(@Param("user") User user, @Param("increment") Integer increment);

    // Reset interaction scores (for periodic cleanup)
    @Query("UPDATE UserRelationship ur SET ur.interactionScore = 0 " +
            "WHERE ur.lastInteraction < :cutoffDate")
    int resetOldInteractionScores(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ==================== RELATIONSHIP TYPE QUERIES ====================

    // Find by specific relationship type
    @Query("SELECT ur FROM UserRelationship ur WHERE ur.follower = :user " +
            "AND ur.relationshipType = :type AND ur.status = :status " +
            "ORDER BY ur.updatedAt DESC")
    Page<UserRelationship> findByTypeAndStatus(@Param("user") User user,
                                               @Param("type") UserRelationship.RelationshipType type,
                                               @Param("status") UserRelationship.RelationshipStatus status,
                                               Pageable pageable);

    // Count by relationship type
    @Query("SELECT COUNT(ur) FROM UserRelationship ur WHERE ur.follower = :user " +
            "AND ur.relationshipType = :type AND ur.status = :status")
    Long countByTypeAndStatus(@Param("user") User user,
                              @Param("type") UserRelationship.RelationshipType type,
                              @Param("status") UserRelationship.RelationshipStatus status);
}