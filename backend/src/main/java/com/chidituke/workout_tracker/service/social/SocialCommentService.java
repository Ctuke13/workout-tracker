package com.chidituke.workout_tracker.service.social;

import com.chidituke.workout_tracker.model.social.SocialComment;
import com.chidituke.workout_tracker.model.social.SocialPost;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.repository.social.SocialCommentRepository;
import com.chidituke.workout_tracker.repository.social.SocialPostRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SocialCommentService {

    private final SocialCommentRepository socialCommentRepository;
    private final SocialPostRepository socialPostRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    // ==================== COMMENT CREATION & MANAGEMENT ====================

    @Transactional
    public SocialComment createComment(Long postId, String username, String content, Long parentCommentId) {
        User author = userService.getUserByUsername(username);
        SocialPost post = findPostById(postId);

        // Validate user can comment on this post
        validateCanCommentOnPost(post, author);

        // Validate comment content
        validateCommentContent(content);

        // Handle parent comment if this is a reply
        SocialComment parentComment = null;
        if (parentCommentId != null) {
            parentComment = findCommentById(parentCommentId);
            validateParentComment(parentComment, post);
        }

        // Create comment
        SocialComment comment = SocialComment.builder()
                .post(post)
                .author(author)
                .content(content.trim())
                .parentComment(parentComment)
                .build();

        SocialComment savedComment = socialCommentRepository.save(comment);

        // Send notifications
        sendCommentNotifications(savedComment);

        log.info("Comment created: {} on post {} by user {}",
                savedComment.getId(), postId, username);

        return savedComment;
    }

    @Transactional
    public SocialComment updateComment(Long commentId, String username, String newContent) {
        SocialComment comment = findCommentById(commentId);
        User user = userService.getUserByUsername(username);

        // Validate ownership
        validateCommentOwnership(comment, user);

        // Validate content
        validateCommentContent(newContent);

        comment.setContent(newContent.trim());
        comment.setUpdatedAt(LocalDateTime.now());

        SocialComment savedComment = socialCommentRepository.save(comment);

        log.info("Comment updated: {} by user {}", commentId, username);

        return savedComment;
    }

    @Transactional
    public void deleteComment(Long commentId, String username) {
        SocialComment comment = findCommentById(commentId);
        User user = userService.getUserByUsername(username);

        // Validate ownership or admin access
        validateCommentDeletionRights(comment, user);

        // Soft delete (set inactive)
        comment.setIsActive(false);
        comment.setUpdatedAt(LocalDateTime.now());

        socialCommentRepository.save(comment);

        log.info("Comment deleted: {} by user {}", commentId, username);
    }

    @Transactional
    public void permanentlyDeleteComment(Long commentId, String username) {
        SocialComment comment = findCommentById(commentId);
        User user = userService.getUserByUsername(username);

        // Only admins or comment author can permanently delete
        validateCommentDeletionRights(comment, user);

        // Delete all replies first (cascade should handle this, but being explicit)
        List<SocialComment> replies = comment.getReplies();
        for (SocialComment reply : replies) {
            permanentlyDeleteComment(reply.getId(), username);
        }

        socialCommentRepository.delete(comment);

        log.info("Comment permanently deleted: {} by user {}", commentId, username);
    }

    // ==================== COMMENT RETRIEVAL ====================

    public Page<SocialComment> getPostComments(Long postId, String username, Pageable pageable) {
        SocialPost post = findPostById(postId);
        User viewer = userService.getUserByUsername(username);

        // Check if user can view this post
        if (!post.isVisibleTo(viewer)) {
            throw new IllegalArgumentException("Post not accessible to user");
        }

        return socialCommentRepository.findTopLevelCommentsByPost(post, pageable);
    }

    public List<SocialComment> getCommentReplies(Long commentId, String username) {
        SocialComment comment = findCommentById(commentId);
        User viewer = userService.getUserByUsername(username);

        // Check if user can view the parent post
        if (!comment.getPost().isVisibleTo(viewer)) {
            throw new IllegalArgumentException("Comment not accessible to user");
        }

        return socialCommentRepository.findRepliesByParentComment(comment);
    }

    public Page<SocialComment> getUserComments(String username, String viewerUsername, Pageable pageable) {
        User author = userService.getUserByUsername(username);
        User viewer = userService.getUserByUsername(viewerUsername);

        // Privacy check - only show comments on posts viewer can see
        if (author.equals(viewer)) {
            // User viewing their own comments
            return socialCommentRepository.findByAuthorAndIsActiveTrueOrderByCreatedAtDesc(author, pageable);
        } else {
            // Apply privacy filtering via the relationship-aware query
            return socialCommentRepository.findCommentsVisibleToUser(viewer, pageable);
        }
    }

    public Optional<SocialComment> getCommentById(Long commentId, String username) {
        Optional<SocialComment> commentOpt = socialCommentRepository.findByIdAndIsActiveTrue(commentId);

        if (commentOpt.isEmpty()) {
            return Optional.empty();
        }

        SocialComment comment = commentOpt.get();
        User viewer = userService.getUserByUsername(username);

        // Check visibility
        if (!comment.isVisibleTo(viewer)) {
            return Optional.empty();
        }

        return Optional.of(comment);
    }

    // ==================== ENGAGEMENT FEATURES ====================

    @Transactional
    public void likeComment(Long commentId, String username) {
        SocialComment comment = findCommentById(commentId);
        User user = userService.getUserByUsername(username);

        // Check if user can view this comment
        if (!comment.isVisibleTo(user)) {
            throw new IllegalArgumentException("Comment not accessible to user");
        }

        // TODO: Check if user already liked this comment (implement CommentLike entity)
        // For now, just increment
        comment.incrementLikes();
        socialCommentRepository.save(comment);

        // Send notification to comment author
        if (!comment.getAuthor().equals(user)) {
            notificationService.notifyCommentLiked(comment, user);
        }

        log.info("Comment liked: {} by user {}", commentId, username);
    }

    @Transactional
    public void unlikeComment(Long commentId, String username) {
        SocialComment comment = findCommentById(commentId);
        User user = userService.getUserByUsername(username);

        // TODO: Check if user actually liked this comment
        // For now, just decrement
        comment.decrementLikes();
        socialCommentRepository.save(comment);

        log.info("Comment unliked: {} by user {}", commentId, username);
    }

    @Transactional
    public void flagComment(Long commentId, String username, String reason) {
        SocialComment comment = findCommentById(commentId);
        User user = userService.getUserByUsername(username);

        // Users can't flag their own comments
        if (comment.getAuthor().equals(user)) {
            throw new IllegalArgumentException("Cannot flag your own comment");
        }

        comment.flagForModeration();
        socialCommentRepository.save(comment);

        // TODO: Create ContentReport entry
        log.info("Comment flagged: {} by user {} for reason: {}", commentId, username, reason);
    }

    // ==================== MODERATION FEATURES ====================

    @Transactional
    public void moderateComment(Long commentId, String moderatorUsername,
                                SocialComment.ModerationStatus status, String reason) {
        SocialComment comment = findCommentById(commentId);
        User moderator = userService.getUserByUsername(moderatorUsername);

        // Only admins can moderate
        if (!moderator.hasAdminAccess()) {
            throw new IllegalArgumentException("Insufficient permissions for moderation");
        }

        comment.setModerationStatus(status);
        comment.setModerationReason(reason);
        comment.setModeratedByUserId(moderator.getId());
        comment.setModeratedAt(LocalDateTime.now());

        // If rejected, make inactive
        if (status == SocialComment.ModerationStatus.REJECTED) {
            comment.setIsActive(false);
        }

        socialCommentRepository.save(comment);

        log.info("Comment moderated: {} by {} with status: {}", commentId, moderatorUsername, status);
    }

    public Page<SocialComment> getCommentsNeedingModeration(Pageable pageable) {
        return socialCommentRepository.findCommentsNeedingModeration(pageable);
    }

    public Page<SocialComment> getFlaggedComments(Pageable pageable) {
        return socialCommentRepository.findFlaggedComments(pageable);
    }

    // ==================== ANALYTICS & STATISTICS ====================

    public CommentAnalytics getUserCommentAnalytics(String username, LocalDateTime startDate) {
        User user = userService.getUserByUsername(username);

        Long totalComments = socialCommentRepository.countActiveCommentsByAuthorSince(user, startDate);
        Double averageLikes = socialCommentRepository.getAverageLikesForAuthorSince(user, startDate);
        Long totalEngagement = socialCommentRepository.getTotalEngagementForAuthor(user);

        return CommentAnalytics.builder()
                .username(username)
                .totalComments(totalComments)
                .averageLikes(averageLikes != null ? averageLikes : 0.0)
                .totalEngagement(totalEngagement != null ? totalEngagement : 0L)
                .startDate(startDate)
                .build();
    }

    public Page<SocialComment> getPopularComments(Long postId, Pageable pageable) {
        SocialPost post = findPostById(postId);
        return socialCommentRepository.findPopularCommentsByPost(post, 3, 2, pageable);
    }

    public List<Object[]> getTopCommenters(LocalDateTime startDate, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return socialCommentRepository.getTopCommentersInPeriod(startDate);
    }

    // ==================== SEARCH & DISCOVERY ====================

    public Page<SocialComment> searchComments(String searchTerm, String username, Pageable pageable) {
        User viewer = userService.getUserByUsername(username);

        // Basic search - could be enhanced with privacy filtering
        return socialCommentRepository.searchCommentsByContent(searchTerm, pageable);
    }

    public Page<SocialComment> getCommentsMentioningUser(String username, Pageable pageable) {
        User user = userService.getUserByUsername(username);
        return socialCommentRepository.findCommentsMentioningUser(user.getId(), pageable);
    }

    // ==================== HELPER METHODS ====================

    private SocialPost findPostById(Long postId) {
        return socialPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
    }

    private SocialComment findCommentById(Long commentId) {
        return socialCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
    }

    private void validateCanCommentOnPost(SocialPost post, User author) {
        // Check if post allows comments
        if (!post.getAuthor().allowsCommentsOnPosts()) {
            throw new IllegalArgumentException("Comments are disabled for this post");
        }

        // Check if user can view the post
        if (!post.isVisibleTo(author)) {
            throw new IllegalArgumentException("Post not accessible to user");
        }

        // Check if post is active
        if (!post.getIsActive()) {
            throw new IllegalArgumentException("Cannot comment on inactive post");
        }
    }

    private void validateCommentContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }

        if (content.trim().length() > 500) {
            throw new IllegalArgumentException("Comment content exceeds maximum length of 500 characters");
        }
    }

    private void validateParentComment(SocialComment parentComment, SocialPost post) {
        // Parent comment must belong to the same post
        if (!parentComment.getPost().getId().equals(post.getId())) {
            throw new IllegalArgumentException("Parent comment does not belong to this post");
        }

        // Parent comment must be active
        if (!parentComment.getIsActive()) {
            throw new IllegalArgumentException("Cannot reply to inactive comment");
        }

        // Limit nesting depth to prevent performance issues
        if (parentComment.getDepthLevel() >= 5) {
            throw new IllegalArgumentException("Maximum comment nesting depth exceeded");
        }
    }

    private void validateCommentOwnership(SocialComment comment, User user) {
        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new IllegalArgumentException("User does not own this comment");
        }
    }

    private void validateCommentDeletionRights(SocialComment comment, User user) {
        boolean isOwner = comment.getAuthor().getId().equals(user.getId());
        boolean isAdmin = user.hasAdminAccess();
        boolean isPostAuthor = comment.getPost().getAuthor().getId().equals(user.getId());

        if (!isOwner && !isAdmin && !isPostAuthor) {
            throw new IllegalArgumentException("Insufficient permissions to delete comment");
        }
    }

    private void sendCommentNotifications(SocialComment comment) {
        User commentAuthor = comment.getAuthor();

        // Notify post author (if different from comment author)
        if (!comment.getPost().getAuthor().equals(commentAuthor)) {
            notificationService.notifyWorkoutCommented(comment, commentAuthor);
        }

        // Notify parent comment author (if this is a reply and different from comment author)
        if (comment.getParentComment() != null &&
                !comment.getParentComment().getAuthor().equals(commentAuthor)) {
            notificationService.notifyCommentReply(comment, commentAuthor);
        }

        // TODO: Notify mentioned users
        // for (Long mentionedUserId : comment.getMentionedUserIds()) {
        //     User mentionedUser = userService.getUserById(mentionedUserId);
        //     if (!mentionedUser.equals(commentAuthor)) {
        //         notificationService.notifyMentionInComment(comment, mentionedUser, commentAuthor);
        //     }
        // }
    }

    // ==================== INNER CLASSES ====================

    @lombok.Data
    @lombok.Builder
    public static class CommentAnalytics {
        private String username;
        private Long totalComments;
        private Double averageLikes;
        private Long totalEngagement;
        private LocalDateTime startDate;
    }
}