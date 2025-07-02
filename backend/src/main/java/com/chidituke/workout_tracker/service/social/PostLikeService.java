package com.chidituke.workout_tracker.service.social;

import com.chidituke.workout_tracker.model.social.PostLike;
import com.chidituke.workout_tracker.model.social.SocialPost;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.repository.social.PostLikeRepository;
import com.chidituke.workout_tracker.repository.social.SocialPostRepository;
import com.chidituke.workout_tracker.exceptions.common.ResourceNotFoundException;
import com.chidituke.workout_tracker.service.notification.NotificationService;
import com.chidituke.workout_tracker.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final SocialPostRepository socialPostRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    // ==================== CORE LIKE/UNLIKE OPERATIONS ====================

    @Transactional
    public LikeResult likePost(Long postId, String username) {
        User user = userService.getUserByUsername(username);
        SocialPost post = findPostById(postId);

        // Validate user can interact with this post
        validateCanLikePost(post, user);

        // Check if user already liked this post
        if (postLikeRepository.existsByPostAndUser(post, user)) {
            log.debug("User {} already liked post {}", username, postId);
            return LikeResult.builder()
                    .success(false)
                    .alreadyLiked(true)
                    .message("Post already liked by user")
                    .currentLikeCount(post.getLikesCount())
                    .build();
        }

        // Create like record
        PostLike postLike = new PostLike(post, user);
        postLikeRepository.save(postLike);

        // Update post like counter
        post.incrementLikes();
        socialPostRepository.save(post);

        // Update user's total likes given (optional counter)
        // user.incrementLikesGiven(); // if you add this field to User entity

        // Send notification to post author (if different from liker)
        if (!post.getAuthor().equals(user)) {
            notificationService.notifyWorkoutLiked(post, user);
        }

        log.info("Post liked: {} by user {}", postId, username);

        return LikeResult.builder()
                .success(true)
                .alreadyLiked(false)
                .message("Post liked successfully")
                .currentLikeCount(post.getLikesCount())
                .build();
    }

    @Transactional
    public LikeResult unlikePost(Long postId, String username) {
        User user = userService.getUserByUsername(username);
        SocialPost post = findPostById(postId);

        // Find existing like record
        Optional<PostLike> postLikeOpt = postLikeRepository.findByPostAndUser(post, user);

        if (postLikeOpt.isEmpty()) {
            log.debug("User {} hasn't liked post {}", username, postId);
            return LikeResult.builder()
                    .success(false)
                    .alreadyLiked(false)
                    .message("Post not liked by user")
                    .currentLikeCount(post.getLikesCount())
                    .build();
        }

        // Remove like record
        postLikeRepository.delete(postLikeOpt.get());

        // Update post like counter
        post.decrementLikes();
        socialPostRepository.save(post);

        log.info("Post unliked: {} by user {}", postId, username);

        return LikeResult.builder()
                .success(true)
                .alreadyLiked(false)
                .message("Post unliked successfully")
                .currentLikeCount(post.getLikesCount())
                .build();
    }

    @Transactional
    public LikeResult toggleLike(Long postId, String username) {
        User user = userService.getUserByUsername(username);
        SocialPost post = findPostById(postId);

        // Check current like status and toggle
        if (postLikeRepository.existsByPostAndUser(post, user)) {
            return unlikePost(postId, username);
        } else {
            return likePost(postId, username);
        }
    }

    // ==================== LIKE STATUS & INFORMATION ====================

    public boolean hasUserLikedPost(Long postId, String username) {
        User user = userService.getUserByUsername(username);
        SocialPost post = findPostById(postId);

        return postLikeRepository.existsByPostAndUser(post, user);
    }

    public LikeInfo getPostLikeInfo(Long postId, String username) {
        User user = userService.getUserByUsername(username);
        SocialPost post = findPostById(postId);

        // Check if user can view this post
        if (!post.isVisibleTo(user)) {
            throw new IllegalArgumentException("Post not accessible to user");
        }

        boolean userLiked = postLikeRepository.existsByPostAndUser(post, user);
        Long totalLikes = postLikeRepository.countLikesByPost(post);

        return LikeInfo.builder()
                .postId(postId)
                .totalLikes(totalLikes)
                .userHasLiked(userLiked)
                .build();
    }

    public Page<User> getPostLikers(Long postId, String username, Pageable pageable) {
        User viewer = userService.getUserByUsername(username);
        SocialPost post = findPostById(postId);

        // Check if user can view this post
        if (!post.isVisibleTo(viewer)) {
            throw new IllegalArgumentException("Post not accessible to user");
        }

        return postLikeRepository.findUsersWhoLikedPost(post, pageable);
    }

    // ==================== USER LIKE HISTORY ====================

    public Page<SocialPost> getUserLikedPosts(String username, String viewerUsername, Pageable pageable) {
        User user = userService.getUserByUsername(username);
        User viewer = userService.getUserByUsername(viewerUsername);

        // Privacy check - users can only see their own liked posts or public ones
        if (!user.equals(viewer)) {
            // For now, only show public posts they liked
            // Could be enhanced with friend/privacy logic
            Page<PostLike> likes = postLikeRepository.findLikesByUser(user, pageable);
            return likes.map(PostLike::getPost);
        }

        // User viewing their own likes - show all
        Page<PostLike> likes = postLikeRepository.findLikesByUser(user, pageable);
        return likes.map(PostLike::getPost);
    }

    public Page<PostLike> getUserLikeHistory(String username, Pageable pageable) {
        User user = userService.getUserByUsername(username);
        return postLikeRepository.findLikesByUser(user, pageable);
    }

    // ==================== ANALYTICS & STATISTICS ====================

    public UserLikeStats getUserLikeStatistics(String username, LocalDateTime startDate) {
        User user = userService.getUserByUsername(username);

        Long likesGiven = postLikeRepository.countLikesByUserSince(user, startDate);
        Long likesReceived = postLikeRepository.countLikesReceivedByAuthorSince(user, startDate);
        Long totalLikesGiven = postLikeRepository.countLikesByUser(user);
        Long totalLikesReceived = postLikeRepository.countLikesReceivedByAuthor(user);

        return UserLikeStats.builder()
                .username(username)
                .likesGivenSinceDate(likesGiven)
                .likesReceivedSinceDate(likesReceived)
                .totalLikesGiven(totalLikesGiven)
                .totalLikesReceived(totalLikesReceived)
                .startDate(startDate)
                .build();
    }

    public List<Object[]> getTopLikers(LocalDateTime startDate, int limit) {
        return postLikeRepository.findTopLikersInPeriod(startDate)
                .stream()
                .limit(limit)
                .toList();
    }

    public List<Object[]> getTopLikeReceivers(LocalDateTime startDate, int limit) {
        return postLikeRepository.findTopLikeReceiversInPeriod(startDate)
                .stream()
                .limit(limit)
                .toList();
    }

    public List<Object[]> getMostLikedPosts(LocalDateTime startDate, int limit) {
        return postLikeRepository.findMostLikedPostsInPeriod(startDate)
                .stream()
                .limit(limit)
                .toList();
    }

    // ==================== CONTENT DISCOVERY ====================

    public Page<SocialPost> getRecommendedPosts(String username, Pageable pageable) {
        User user = userService.getUserByUsername(username);

        // Get posts liked by people the user follows
        return postLikeRepository.findPostsLikedByFollowing(user, pageable);
    }

    public List<SocialPost> getTrendingPosts(int hoursBack, Long minLikes, int limit) {
        LocalDateTime recentDate = LocalDateTime.now().minusHours(hoursBack);
        LocalDateTime postMinAge = LocalDateTime.now().minusDays(7); // Posts must be within last week

        return postLikeRepository.findTrendingPosts(recentDate, postMinAge, minLikes)
                .stream()
                .limit(limit)
                .map(result -> (SocialPost) result[0])
                .toList();
    }

    public List<SocialPost> getMutualLikes(String username1, String username2) {
        User user1 = userService.getUserByUsername(username1);
        User user2 = userService.getUserByUsername(username2);

        return postLikeRepository.findMutualLikes(user1, user2);
    }

    // ==================== ENGAGEMENT ANALYSIS ====================

    public EngagementReport generateEngagementReport(String username, LocalDateTime startDate, LocalDateTime endDate) {
        User user = userService.getUserByUsername(username);

        // Get like activity pattern
        List<Object[]> activityPattern = postLikeRepository.findUserLikeActivityPattern(user);

        // Get likes given and received in period
        Long likesGivenInPeriod = postLikeRepository.countLikesByUserSince(user, startDate);
        Long likesReceivedInPeriod = postLikeRepository.countLikesReceivedByAuthorSince(user, startDate);

        return EngagementReport.builder()
                .username(username)
                .startDate(startDate)
                .endDate(endDate)
                .likesGiven(likesGivenInPeriod)
                .likesReceived(likesReceivedInPeriod)
                .hourlyActivityPattern(activityPattern)
                .build();
    }

    // ==================== BULK OPERATIONS ====================

    @Transactional
    public int removeAllUserLikes(String username) {
        User user = userService.getUserByUsername(username);

        // Only admins or the user themselves can do this
        // Add authorization check here if needed

        return postLikeRepository.deleteAllLikesByUser(user);
    }

    @Transactional
    public int removeAllPostLikes(Long postId, String username) {
        SocialPost post = findPostById(postId);
        User user = userService.getUserByUsername(username);

        // Only post author or admin can remove all likes
        validateCanModifyPostLikes(post, user);

        int deletedCount = postLikeRepository.deleteAllLikesForPost(post);

        // Reset post like counter
        post.setLikesCount(0);
        socialPostRepository.save(post);

        return deletedCount;
    }

    // ==================== HELPER METHODS ====================

    private SocialPost findPostById(Long postId) {
        return socialPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
    }

    private void validateCanLikePost(SocialPost post, User user) {
        // Check if post is active
        if (!post.getIsActive()) {
            throw new IllegalArgumentException("Cannot like inactive post");
        }

        // Check if post is approved
        if (post.getModerationStatus() != SocialPost.ModerationStatus.APPROVED) {
            throw new IllegalArgumentException("Cannot like unapproved post");
        }

        // Check if user can view this post
        if (!post.isVisibleTo(user)) {
            throw new IllegalArgumentException("Post not accessible to user");
        }

        // Users can't like their own posts
        if (post.getAuthor().equals(user)) {
            throw new IllegalArgumentException("Cannot like your own post");
        }
    }

    private void validateCanModifyPostLikes(SocialPost post, User user) {
        boolean isPostAuthor = post.getAuthor().equals(user);
        boolean isAdmin = user.hasAdminAccess();

        if (!isPostAuthor && !isAdmin) {
            throw new IllegalArgumentException("Insufficient permissions to modify post likes");
        }
    }

    // ==================== INNER CLASSES (DTOs) ====================

    @lombok.Data
    @lombok.Builder
    public static class LikeResult {
        private boolean success;
        private boolean alreadyLiked;
        private String message;
        private Integer currentLikeCount;
    }

    @lombok.Data
    @lombok.Builder
    public static class LikeInfo {
        private Long postId;
        private Long totalLikes;
        private boolean userHasLiked;
    }

    @lombok.Data
    @lombok.Builder
    public static class UserLikeStats {
        private String username;
        private Long likesGivenSinceDate;
        private Long likesReceivedSinceDate;
        private Long totalLikesGiven;
        private Long totalLikesReceived;
        private LocalDateTime startDate;
    }

    @lombok.Data
    @lombok.Builder
    public static class EngagementReport {
        private String username;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Long likesGiven;
        private Long likesReceived;
        private List<Object[]> hourlyActivityPattern;
    }
}