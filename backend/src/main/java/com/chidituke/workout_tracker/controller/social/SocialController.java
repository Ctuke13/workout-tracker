package com.chidituke.workout_tracker.controller.social;

import com.chidituke.workout_tracker.model.social.SocialComment;
import com.chidituke.workout_tracker.model.social.SocialPost;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.user.UserRelationship;
import com.chidituke.workout_tracker.service.social.SocialCommentService;
import com.chidituke.workout_tracker.service.social.PostLikeService;
import com.chidituke.workout_tracker.service.user.UserRelationshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
@Slf4j
public class SocialController {

    private final SocialCommentService socialCommentService;
    private final PostLikeService postLikeService;
    private final UserRelationshipService userRelationshipService;
    // private final SocialPostService socialPostService; // Will be needed when you create this

    // ==================== COMMENT ENDPOINTS ====================

    /**
     * Create a comment on a post
     */
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<SocialComment> createComment(
            @PathVariable Long postId,
            @RequestBody @Valid CommentRequest request,
            Authentication authentication) {

        String username = authentication.getName();

        SocialComment comment = socialCommentService.createComment(
                postId, username, request.getContent(), request.getParentCommentId());

        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    /**
     * Get comments for a post
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Page<SocialComment>> getPostComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);

        Page<SocialComment> comments = socialCommentService.getPostComments(postId, username, pageable);

        return ResponseEntity.ok(comments);
    }

    /**
     * Get replies to a comment
     */
    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<List<SocialComment>> getCommentReplies(
            @PathVariable Long commentId,
            Authentication authentication) {

        String username = authentication.getName();

        List<SocialComment> replies = socialCommentService.getCommentReplies(commentId, username);

        return ResponseEntity.ok(replies);
    }

    /**
     * Update a comment
     */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<SocialComment> updateComment(
            @PathVariable Long commentId,
            @RequestBody @Valid CommentUpdateRequest request,
            Authentication authentication) {

        String username = authentication.getName();

        SocialComment updatedComment = socialCommentService.updateComment(
                commentId, username, request.getContent());

        return ResponseEntity.ok(updatedComment);
    }

    /**
     * Delete a comment
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {

        String username = authentication.getName();

        socialCommentService.deleteComment(commentId, username);

        return ResponseEntity.noContent().build();
    }

    /**
     * Like a comment
     */
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<Map<String, Object>> likeComment(
            @PathVariable Long commentId,
            Authentication authentication) {

        String username = authentication.getName();

        socialCommentService.likeComment(commentId, username);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Comment liked successfully"
        ));
    }

    /**
     * Unlike a comment
     */
    @DeleteMapping("/comments/{commentId}/like")
    public ResponseEntity<Map<String, Object>> unlikeComment(
            @PathVariable Long commentId,
            Authentication authentication) {

        String username = authentication.getName();

        socialCommentService.unlikeComment(commentId, username);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Comment unliked successfully"
        ));
    }

    /**
     * Flag a comment for moderation
     */
    @PostMapping("/comments/{commentId}/flag")
    public ResponseEntity<Map<String, Object>> flagComment(
            @PathVariable Long commentId,
            @RequestBody @Valid FlagRequest request,
            Authentication authentication) {

        String username = authentication.getName();

        socialCommentService.flagComment(commentId, username, request.getReason());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Comment flagged for review"
        ));
    }

    // ==================== POST LIKE ENDPOINTS ====================

    /**
     * Like a post
     */
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<PostLikeService.LikeResult> likePost(
            @PathVariable Long postId,
            Authentication authentication) {

        String username = authentication.getName();

        PostLikeService.LikeResult result = postLikeService.likePost(postId, username);

        HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * Unlike a post
     */
    @DeleteMapping("/posts/{postId}/like")
    public ResponseEntity<PostLikeService.LikeResult> unlikePost(
            @PathVariable Long postId,
            Authentication authentication) {

        String username = authentication.getName();

        PostLikeService.LikeResult result = postLikeService.unlikePost(postId, username);

        HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * Toggle like on a post
     */
    @PostMapping("/posts/{postId}/toggle-like")
    public ResponseEntity<PostLikeService.LikeResult> toggleLike(
            @PathVariable Long postId,
            Authentication authentication) {

        String username = authentication.getName();

        PostLikeService.LikeResult result = postLikeService.toggleLike(postId, username);

        return ResponseEntity.ok(result);
    }

    /**
     * Get post like information
     */
    @GetMapping("/posts/{postId}/likes")
    public ResponseEntity<PostLikeService.LikeInfo> getPostLikeInfo(
            @PathVariable Long postId,
            Authentication authentication) {

        String username = authentication.getName();

        PostLikeService.LikeInfo likeInfo = postLikeService.getPostLikeInfo(postId, username);

        return ResponseEntity.ok(likeInfo);
    }

    /**
     * Get users who liked a post
     */
    @GetMapping("/posts/{postId}/likers")
    public ResponseEntity<Page<User>> getPostLikers(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);

        Page<User> likers = postLikeService.getPostLikers(postId, username, pageable);

        return ResponseEntity.ok(likers);
    }

    /**
     * Get posts liked by a user
     */
    @GetMapping("/users/{username}/liked-posts")
    public ResponseEntity<Page<SocialPost>> getUserLikedPosts(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        String viewerUsername = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);

        Page<SocialPost> likedPosts = postLikeService.getUserLikedPosts(username, viewerUsername, pageable);

        return ResponseEntity.ok(likedPosts);
    }

    // ==================== USER RELATIONSHIP ENDPOINTS ====================

    /**
     * Follow a user
     */
    @PostMapping("/users/{username}/follow")
    public ResponseEntity<UserRelationshipService.RelationshipResult> followUser(
            @PathVariable String username,
            Authentication authentication) {

        String followerUsername = authentication.getName();

        UserRelationshipService.RelationshipResult result =
                userRelationshipService.followUser(followerUsername, username);

        HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * Unfollow a user
     */
    @DeleteMapping("/users/{username}/follow")
    public ResponseEntity<UserRelationshipService.RelationshipResult> unfollowUser(
            @PathVariable String username,
            Authentication authentication) {

        String followerUsername = authentication.getName();

        UserRelationshipService.RelationshipResult result =
                userRelationshipService.unfollowUser(followerUsername, username);

        HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * Send friend request
     */
    @PostMapping("/users/{username}/friend-request")
    public ResponseEntity<UserRelationshipService.RelationshipResult> sendFriendRequest(
            @PathVariable String username,
            Authentication authentication) {

        String requesterUsername = authentication.getName();

        UserRelationshipService.RelationshipResult result =
                userRelationshipService.sendFriendRequest(requesterUsername, username);

        HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * Accept friend request
     */
    @PostMapping("/friend-requests/{username}/accept")
    public ResponseEntity<UserRelationshipService.RelationshipResult> acceptFriendRequest(
            @PathVariable String username,
            Authentication authentication) {

        String targetUsername = authentication.getName();

        UserRelationshipService.RelationshipResult result =
                userRelationshipService.acceptFriendRequest(targetUsername, username);

        HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * Decline friend request
     */
    @PostMapping("/friend-requests/{username}/decline")
    public ResponseEntity<UserRelationshipService.RelationshipResult> declineFriendRequest(
            @PathVariable String username,
            Authentication authentication) {

        String targetUsername = authentication.getName();

        UserRelationshipService.RelationshipResult result =
                userRelationshipService.declineFriendRequest(targetUsername, username);

        HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * Block a user
     */
    @PostMapping("/users/{username}/block")
    public ResponseEntity<UserRelationshipService.RelationshipResult> blockUser(
            @PathVariable String username,
            Authentication authentication) {

        String blockerUsername = authentication.getName();

        UserRelationshipService.RelationshipResult result =
                userRelationshipService.blockUser(blockerUsername, username);

        HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * Unblock a user
     */
    @DeleteMapping("/users/{username}/block")
    public ResponseEntity<UserRelationshipService.RelationshipResult> unblockUser(
            @PathVariable String username,
            Authentication authentication) {

        String blockerUsername = authentication.getName();

        UserRelationshipService.RelationshipResult result =
                userRelationshipService.unblockUser(blockerUsername, username);

        HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * Toggle close friend status
     */
    @PostMapping("/users/{username}/close-friend")
    public ResponseEntity<UserRelationshipService.RelationshipResult> toggleCloseFriend(
            @PathVariable String username,
            Authentication authentication) {

        String userUsername = authentication.getName();

        UserRelationshipService.RelationshipResult result =
                userRelationshipService.toggleCloseFriend(userUsername, username);

        HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * Toggle mute status
     */
    @PostMapping("/users/{username}/mute")
    public ResponseEntity<UserRelationshipService.RelationshipResult> toggleMute(
            @PathVariable String username,
            Authentication authentication) {

        String userUsername = authentication.getName();

        UserRelationshipService.RelationshipResult result =
                userRelationshipService.toggleMute(userUsername, username);

        HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(result);
    }

    /**
     * Get relationship information between users
     */
    @GetMapping("/users/{username}/relationship")
    public ResponseEntity<UserRelationshipService.RelationshipInfo> getRelationshipInfo(
            @PathVariable String username,
            Authentication authentication) {

        String userUsername = authentication.getName();

        UserRelationshipService.RelationshipInfo relationshipInfo =
                userRelationshipService.getRelationshipInfo(userUsername, username);

        return ResponseEntity.ok(relationshipInfo);
    }

    // ==================== USER LISTS ENDPOINTS ====================

    /**
     * Get user's followers
     */
    @GetMapping("/users/{username}/followers")
    public ResponseEntity<Page<User>> getUserFollowers(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<User> followers = userRelationshipService.getFollowers(username, pageable);

        return ResponseEntity.ok(followers);
    }

    /**
     * Get users that a user is following
     */
    @GetMapping("/users/{username}/following")
    public ResponseEntity<Page<User>> getUserFollowing(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<User> following = userRelationshipService.getFollowing(username, pageable);

        return ResponseEntity.ok(following);
    }

    /**
     * Get user's mutual friends
     */
    @GetMapping("/users/{username}/mutual-friends")
    public ResponseEntity<Page<User>> getUserMutualFriends(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<User> mutualFriends = userRelationshipService.getMutualFriends(username, pageable);

        return ResponseEntity.ok(mutualFriends);
    }

    /**
     * Get user's close friends
     */
    @GetMapping("/users/{username}/close-friends")
    public ResponseEntity<Page<User>> getUserCloseFriends(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        // Only allow users to see their own close friends list
        String requestingUser = authentication.getName();
        if (!requestingUser.equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<User> closeFriends = userRelationshipService.getCloseFriends(username, pageable);

        return ResponseEntity.ok(closeFriends);
    }

    /**
     * Get pending friend requests
     */
    @GetMapping("/friend-requests")
    public ResponseEntity<Page<UserRelationship>> getPendingFriendRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);

        Page<UserRelationship> pendingRequests =
                userRelationshipService.getPendingFriendRequests(username, pageable);

        return ResponseEntity.ok(pendingRequests);
    }

    /**
     * Get suggested connections
     */
    @GetMapping("/suggested-connections")
    public ResponseEntity<Page<User>> getSuggestedConnections(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);

        Page<User> suggestions = userRelationshipService.getSuggestedConnections(username, pageable);

        return ResponseEntity.ok(suggestions);
    }

    /**
     * Get blocked users
     */
    @GetMapping("/blocked-users")
    public ResponseEntity<Page<User>> getBlockedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);

        Page<User> blockedUsers = userRelationshipService.getBlockedUsers(username, pageable);

        return ResponseEntity.ok(blockedUsers);
    }

    // ==================== ANALYTICS & STATISTICS ENDPOINTS ====================

    /**
     * Get user's social statistics
     */
    @GetMapping("/users/{username}/stats")
    public ResponseEntity<Map<String, Object>> getUserSocialStats(
            @PathVariable String username,
            Authentication authentication) {

        // Get relationship statistics
        UserRelationshipService.UserRelationshipStats relationshipStats =
                userRelationshipService.getRelationshipStatistics(username);

        // Get like statistics
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        PostLikeService.UserLikeStats likeStats =
                postLikeService.getUserLikeStatistics(username, startDate);

        // Get comment analytics
        SocialCommentService.CommentAnalytics commentAnalytics =
                socialCommentService.getUserCommentAnalytics(username, startDate);

        Map<String, Object> stats = Map.of(
                "relationships", relationshipStats,
                "likes", likeStats,
                "comments", commentAnalytics
        );

        return ResponseEntity.ok(stats);
    }

    /**
     * Get trending posts
     */
    @GetMapping("/trending-posts")
    public ResponseEntity<List<SocialPost>> getTrendingPosts(
            @RequestParam(defaultValue = "24") int hoursBack,
            @RequestParam(defaultValue = "5") Long minLikes,
            @RequestParam(defaultValue = "10") int limit) {

        List<SocialPost> trendingPosts = postLikeService.getTrendingPosts(hoursBack, minLikes, limit);

        return ResponseEntity.ok(trendingPosts);
    }

    /**
     * Get recommended posts for user
     */
    @GetMapping("/recommended-posts")
    public ResponseEntity<Page<SocialPost>> getRecommendedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);

        Page<SocialPost> recommendedPosts = postLikeService.getRecommendedPosts(username, pageable);

        return ResponseEntity.ok(recommendedPosts);
    }

    // ==================== FEED ENDPOINTS ====================

    /**
     * Get users for personalized feed generation
     */
    @GetMapping("/feed/users")
    public ResponseEntity<Page<User>> getFeedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication authentication) {

        String username = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);

        Page<User> feedUsers = userRelationshipService.getUsersForFeed(username, pageable);

        return ResponseEntity.ok(feedUsers);
    }

    /**
     * Get priority relationships for smart feed
     */
    @GetMapping("/feed/priority-relationships")
    public ResponseEntity<List<UserRelationship>> getFeedPriorityRelationships(
            @RequestParam(defaultValue = "20") int limit,
            Authentication authentication) {

        String username = authentication.getName();

        List<UserRelationship> priorityRelationships =
                userRelationshipService.getFeedPriorityRelationships(username, limit);

        return ResponseEntity.ok(priorityRelationships);
    }

    // ==================== INNER CLASSES (DTOs) ====================

    public static class CommentRequest {
        @NotBlank(message = "Comment content is required")
        @Size(max = 500, message = "Comment cannot exceed 500 characters")
        private String content;

        private Long parentCommentId;

        // Getters and setters
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Long getParentCommentId() { return parentCommentId; }
        public void setParentCommentId(Long parentCommentId) { this.parentCommentId = parentCommentId; }
    }

    public static class CommentUpdateRequest {
        @NotBlank(message = "Comment content is required")
        @Size(max = 500, message = "Comment cannot exceed 500 characters")
        private String content;

        // Getters and setters
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class FlagRequest {
        @NotBlank(message = "Reason is required")
        @Size(max = 200, message = "Reason cannot exceed 200 characters")
        private String reason;

        // Getters and setters
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}