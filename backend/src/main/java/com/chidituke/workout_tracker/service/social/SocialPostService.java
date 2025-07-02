package com.chidituke.workout_tracker.service.social;

import com.chidituke.workout_tracker.model.social.SocialPost;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.user.UserRelationship;
import com.chidituke.workout_tracker.repository.social.SocialPostRepository;
import com.chidituke.workout_tracker.repository.user.UserRelationshipRepository;
import com.chidituke.workout_tracker.exceptions.common.ResourceNotFoundException;
import com.chidituke.workout_tracker.service.notification.NotificationService;
import com.chidituke.workout_tracker.service.user.UserService;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SocialPostService {

    private final SocialPostRepository socialPostRepository;
    private final UserRelationshipRepository userRelationshipRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    // ==================== POST CREATION & MANAGEMENT ====================

    @Transactional
    public SocialPost createPost(String username, CreatePostRequest request) {
        User author = userService.getUserByUsername(username);

        // Validate post content
        validatePostContent(request);

        // Create post
        SocialPost post = SocialPost.builder()
                .author(author)
                .postType(request.getPostType() != null ? request.getPostType() : SocialPost.PostType.TEXT)
                .content(request.getContent())
                .mediaUrl(request.getMediaUrl())
                .linkUrl(request.getLinkUrl())
                .linkTitle(request.getLinkTitle())
                .linkDescription(request.getLinkDescription())
                .privacyLevel(request.getPrivacyLevel() != null ? request.getPrivacyLevel() : author.getDefaultPostPrivacy().toSocialPostPrivacy())
                .location(request.getLocation())
                .build();

        SocialPost savedPost = socialPostRepository.save(post);

        // Update user post count
        author.incrementPostsCount();
        userService.save(author);

        // Send notifications to followers if public post
        if (savedPost.getPrivacyLevel() == SocialPost.PrivacyLevel.PUBLIC) {
            notificationService.notifyFollowersOfNewPost(author, savedPost);
        }

        log.info("Post created: {} by user {}", savedPost.getId(), username);

        return savedPost;
    }

    @Transactional
    public SocialPost updatePost(Long postId, String username, UpdatePostRequest request) {
        SocialPost post = findPostById(postId);
        User user = userService.getUserByUsername(username);

        // Validate ownership
        validatePostOwnership(post, user);

        // Update fields
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        if (request.getPrivacyLevel() != null) {
            post.setPrivacyLevel(request.getPrivacyLevel());
        }
        if (request.getLocation() != null) {
            post.setLocation(request.getLocation());
        }

        post.setUpdatedAt(LocalDateTime.now());

        SocialPost savedPost = socialPostRepository.save(post);

        log.info("Post updated: {} by user {}", postId, username);

        return savedPost;
    }

    @Transactional
    public void deletePost(Long postId, String username) {
        SocialPost post = findPostById(postId);
        User user = userService.getUserByUsername(username);

        // Validate deletion rights
        validatePostDeletionRights(post, user);

        // Soft delete
        post.setIsActive(false);
        post.setUpdatedAt(LocalDateTime.now());
        socialPostRepository.save(post);

        // Update user post count
        User author = post.getAuthor();
        author.decrementPostsCount();
        userService.save(author);

        log.info("Post deleted: {} by user {}", postId, username);
    }

    @Transactional
    public void permanentlyDeletePost(Long postId, String username) {
        SocialPost post = findPostById(postId);
        User user = userService.getUserByUsername(username);

        // Only admins or post author can permanently delete
        validatePostDeletionRights(post, user);

        socialPostRepository.delete(post);

        log.info("Post permanently deleted: {} by user {}", postId, username);
    }

    // ==================== POST RETRIEVAL ====================

    public Optional<SocialPost> getPostById(Long postId, String username) {
        User viewer = userService.getUserByUsername(username);
        Optional<SocialPost> postOpt = socialPostRepository.findByIdAndIsActiveTrue(postId);

        if (postOpt.isEmpty()) {
            return Optional.empty();
        }

        SocialPost post = postOpt.get();

        // Check if user can view this post
        if (!post.isVisibleTo(viewer)) {
            return Optional.empty();
        }

        // Increment view count
        post.incrementViews();
        socialPostRepository.save(post);

        return Optional.of(post);
    }

    public Page<SocialPost> getUserPosts(String targetUsername, String viewerUsername, Pageable pageable) {
        User target = userService.getUserByUsername(targetUsername);
        User viewer = userService.getUserByUsername(viewerUsername);

        // Determine privacy filtering
        boolean includePrivate = target.equals(viewer);

        return socialPostRepository.findRecentPostsByAuthor(target, includePrivate, pageable);
    }

    public Page<SocialPost> getUserPublicPosts(String username, Pageable pageable) {
        User user = userService.getUserByUsername(username);
        return socialPostRepository.findByAuthorAndIsActiveTrueOrderByCreatedAtDesc(user, pageable);
    }

    // ==================== FEED GENERATION ====================

    public Page<SocialPost> getPublicFeed(Pageable pageable) {
        return socialPostRepository.findPublicFeedPosts(pageable);
    }

    public Page<SocialPost> getPersonalizedFeed(String username, Pageable pageable) {
        User user = userService.getUserByUsername(username);
        return socialPostRepository.findPersonalizedFeedPosts(user, pageable);
    }

    public Page<SocialPost> getPriorityFeed(String username, Pageable pageable) {
        User user = userService.getUserByUsername(username);
        return socialPostRepository.findPriorityFeedPosts(user, pageable);
    }

    public Page<SocialPost> getCloseFriendsFeed(String username, Pageable pageable) {
        User user = userService.getUserByUsername(username);
        return socialPostRepository.findCloseFriendsPosts(user, pageable);
    }

    public SmartFeedResult generateSmartFeed(String username, int limit) {
        User user = userService.getUserByUsername(username);

        // Get priority relationships
        List<UserRelationship> priorityRelationships = userRelationshipRepository
                .findFeedPriorityRelationships(user);

        // Get posts from priority users first
        List<SocialPost> smartFeed = priorityRelationships.stream()
                .limit(limit / 2) // Use half the limit for priority users
                .flatMap(relationship -> {
                    Page<SocialPost> userPosts = socialPostRepository
                            .findRecentPostsByAuthor(relationship.getFollowing(), false,
                                    PageRequest.of(0, 3));
                    return userPosts.getContent().stream();
                })
                .collect(Collectors.toList());

        // Fill remaining slots with general personalized feed
        if (smartFeed.size() < limit) {
            Page<SocialPost> generalFeed = getPersonalizedFeed(username,
                    PageRequest.of(0, limit - smartFeed.size()));

            generalFeed.getContent().stream()
                    .filter(post -> !smartFeed.contains(post))
                    .forEach(smartFeed::add);
        }

        return SmartFeedResult.builder()
                .posts(smartFeed.stream().limit(limit).collect(Collectors.toList()))
                .priorityUsersCount(priorityRelationships.size())
                .totalPostsConsidered(smartFeed.size())
                .build();
    }

    // ==================== CONTENT DISCOVERY ====================

    public Page<SocialPost> searchPosts(String searchTerm, String username, Pageable pageable) {
        User viewer = userService.getUserByUsername(username);

        // Basic search - could be enhanced with privacy filtering
        return socialPostRepository.searchPostsByContent(searchTerm, pageable);
    }

    public Page<SocialPost> getPostsByHashtag(String hashtag, Pageable pageable) {
        return socialPostRepository.findPostsByHashtag(hashtag, pageable);
    }

    public Page<SocialPost> getTrendingPosts(Integer minLikes, Pageable pageable) {
        return socialPostRepository.findTrendingPosts(minLikes != null ? minLikes : 5, pageable);
    }

    public Page<SocialPost> getPopularPostsInDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return socialPostRepository.findPopularPostsInDateRange(startDate, endDate, pageable);
    }

    // ==================== POST INTERACTIONS ====================

    @Transactional
    public void sharePost(Long postId, String username, String platform) {
        SocialPost post = findPostById(postId);
        User user = userService.getUserByUsername(username);

        // Check if user can view this post
        if (!post.isVisibleTo(user)) {
            throw new IllegalArgumentException("Post not accessible to user");
        }

        // Increment share count
        post.incrementShares();
        socialPostRepository.save(post);

        // TODO: Record share in post_shares table
        // TODO: Send notification to post author

        log.info("Post shared: {} by user {} to {}", postId, username, platform);
    }

    @Transactional
    public void flagPost(Long postId, String username, String reason) {
        SocialPost post = findPostById(postId);
        User user = userService.getUserByUsername(username);

        // Users can't flag their own posts
        if (post.getAuthor().equals(user)) {
            throw new IllegalArgumentException("Cannot flag your own post");
        }

        post.flagForModeration();
        socialPostRepository.save(post);

        // TODO: Create ContentReport entry
        log.info("Post flagged: {} by user {} for reason: {}", postId, username, reason);
    }

    // ==================== MODERATION ====================

    @Transactional
    public void moderatePost(Long postId, String moderatorUsername,
                             SocialPost.ModerationStatus status, String reason) {
        SocialPost post = findPostById(postId);
        User moderator = userService.getUserByUsername(moderatorUsername);

        // Only admins can moderate
        if (!moderator.hasAdminAccess()) {
            throw new IllegalArgumentException("Insufficient permissions for moderation");
        }

        post.setModerationStatus(status);
        post.setModerationReason(reason);
        post.setModeratedByUserId(moderator.getId());
        post.setModeratedAt(LocalDateTime.now());

        // If rejected, make inactive
        if (status == SocialPost.ModerationStatus.REJECTED) {
            post.setIsActive(false);
        }

        socialPostRepository.save(post);

        log.info("Post moderated: {} by {} with status: {}", postId, moderatorUsername, status);
    }

    public Page<SocialPost> getPostsNeedingModeration(Pageable pageable) {
        return socialPostRepository.findPostsNeedingModeration(pageable);
    }

    public Page<SocialPost> getPostsByModerationStatus(SocialPost.ModerationStatus status, Pageable pageable) {
        return socialPostRepository.findPostsByModerationStatus(status, pageable);
    }

    // ==================== ANALYTICS ====================

    public PostAnalytics getUserPostAnalytics(String username, LocalDateTime startDate) {
        User user = userService.getUserByUsername(username);

        Long totalPosts = socialPostRepository.countActivePostsByAuthorSince(user, startDate);
        Double averageLikes = socialPostRepository.getAverageLikesForAuthorSince(user, startDate);
        Long totalEngagement = socialPostRepository.getTotalEngagementForAuthor(user);

        return PostAnalytics.builder()
                .username(username)
                .totalPosts(totalPosts)
                .averageLikes(averageLikes != null ? averageLikes : 0.0)
                .totalEngagement(totalEngagement != null ? totalEngagement : 0L)
                .startDate(startDate)
                .build();
    }

    public List<SocialPost> getMostPopularPostsForUser(String username, int limit) {
        User user = userService.getUserByUsername(username);
        return socialPostRepository.findByAuthorAndIsActiveTrueOrderByCreatedAtDesc(user)
                .stream()
                .filter(SocialPost::isPopular)
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ==================== BULK OPERATIONS ====================

    @Transactional
    public int deactivateAllUserPosts(String username) {
        User user = userService.getUserByUsername(username);

        // Only admins or the user themselves can do this
        // Add authorization check here if needed

        return socialPostRepository.deactivateAllPostsByAuthor(user);
    }

    // ==================== HELPER METHODS ====================

    private SocialPost findPostById(Long postId) {
        return socialPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
    }

    private void validatePostContent(CreatePostRequest request) {
        // Must have some content
        if ((request.getContent() == null || request.getContent().trim().isEmpty()) &&
                request.getMediaUrl() == null && request.getLinkUrl() == null) {
            throw new IllegalArgumentException("Post must have content, media, or link");
        }

        // Content length validation
        if (request.getContent() != null && request.getContent().length() > 2000) {
            throw new IllegalArgumentException("Post content exceeds maximum length of 2000 characters");
        }
    }

    private void validatePostOwnership(SocialPost post, User user) {
        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new IllegalArgumentException("User does not own this post");
        }
    }

    private void validatePostDeletionRights(SocialPost post, User user) {
        boolean isOwner = post.getAuthor().getId().equals(user.getId());
        boolean isAdmin = user.hasAdminAccess();

        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("Insufficient permissions to delete post");
        }
    }

    // ==================== INNER CLASSES (DTOs) ====================

    @lombok.Data
    @lombok.Builder
    public static class CreatePostRequest {
        private SocialPost.PostType postType;
        private String content;
        private String mediaUrl;
        private String linkUrl;
        private String linkTitle;
        private String linkDescription;
        private SocialPost.PrivacyLevel privacyLevel;
        private String location;
    }

    @lombok.Data
    @lombok.Builder
    public static class UpdatePostRequest {
        private String content;
        private SocialPost.PrivacyLevel privacyLevel;
        private String location;
    }

    @lombok.Data
    @lombok.Builder
    public static class PostAnalytics {
        private String username;
        private Long totalPosts;
        private Double averageLikes;
        private Long totalEngagement;
        private LocalDateTime startDate;
    }

    @lombok.Data
    @lombok.Builder
    public static class SmartFeedResult {
        private List<SocialPost> posts;
        private Integer priorityUsersCount;
        private Integer totalPostsConsidered;
    }
}