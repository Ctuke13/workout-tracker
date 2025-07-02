package com.chidituke.workout_tracker.model.social;

import com.chidituke.workout_tracker.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "social_comments")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"post", "author", "parentComment", "replies"})
@EqualsAndHashCode(exclude = {"post", "author", "parentComment", "replies"})
public class SocialComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_comment_id")
    private Long id;

    // ==================== RELATIONSHIPS ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "social_post_id", nullable = false)
    private SocialPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // Thread support (replies to comments)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private SocialComment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SocialComment> replies = new ArrayList<>();

    // ==================== CONTENT ====================

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Comment content is required")
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String content;

    // ==================== ENGAGEMENT ====================

    @Column(name = "likes_count")
    @Builder.Default
    private Integer likesCount = 0;

    @Column(name = "replies_count")
    @Builder.Default
    private Integer repliesCount = 0;

    // ==================== MODERATION ====================

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "flagged_count")
    @Builder.Default
    private Integer flaggedCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status")
    @Builder.Default
    private ModerationStatus moderationStatus = ModerationStatus.APPROVED;

    @Column(name = "moderation_reason", length = 500)
    private String moderationReason;

    @Column(name = "moderated_by_user_id")
    private Long moderatedByUserId;

    @Column(name = "moderated_at")
    private LocalDateTime moderatedAt;

    // ==================== MENTIONS ====================

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "comment_mentions", joinColumns = @JoinColumn(name = "comment_id"))
    @Column(name = "mentioned_user_id")
    @Size(max = 10, message = "Maximum 10 mentions allowed per comment")
    @Builder.Default
    private List<Long> mentionedUserIds = new ArrayList<>();

    // ==================== TIMESTAMPS ====================

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ==================== BUSINESS LOGIC METHODS ====================

    public boolean isReply() {
        return parentComment != null;
    }

    public boolean hasReplies() {
        return repliesCount != null && repliesCount > 0;
    }

    public boolean isTopLevelComment() {
        return parentComment == null;
    }

    public void incrementLikes() {
        this.likesCount = (this.likesCount == null ? 0 : this.likesCount) + 1;
    }

    public void decrementLikes() {
        this.likesCount = Math.max(0, (this.likesCount == null ? 0 : this.likesCount) - 1);
    }

    public void addReply(SocialComment reply) {
        if (reply == null) return;

        reply.setParentComment(this);
        reply.setPost(this.post); // Ensure reply belongs to same post
        replies.add(reply);
        this.repliesCount = (this.repliesCount == null ? 0 : this.repliesCount) + 1;
        this.updatedAt = LocalDateTime.now();
    }

    public void removeReply(SocialComment reply) {
        if (reply == null || !replies.contains(reply)) return;

        replies.remove(reply);
        this.repliesCount = Math.max(0, (this.repliesCount == null ? 0 : this.repliesCount) - 1);
        this.updatedAt = LocalDateTime.now();
    }

    public void flagForModeration() {
        this.flaggedCount = (this.flaggedCount == null ? 0 : this.flaggedCount) + 1;
        if (this.flaggedCount >= 2) { // Lower threshold for comments
            this.moderationStatus = ModerationStatus.UNDER_REVIEW;
            this.isActive = false;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isVisibleTo(User viewer) {
        // Comments inherit post visibility but can be additionally restricted
        if (!isActive || moderationStatus != ModerationStatus.APPROVED) {
            return author.equals(viewer); // Only author can see inactive/moderated comments
        }

        // Check if parent post is visible to viewer
        return post.isVisibleTo(viewer);
    }

    public String getContentPreview(int maxLength) {
        if (content == null) return "";
        return content.length() <= maxLength ? content : content.substring(0, maxLength) + "...";
    }

    public int getDepthLevel() {
        int depth = 0;
        SocialComment parent = this.parentComment;
        while (parent != null) {
            depth++;
            parent = parent.getParentComment();
        }
        return depth;
    }

    public SocialComment getRootComment() {
        SocialComment root = this;
        while (root.getParentComment() != null) {
            root = root.getParentComment();
        }
        return root;
    }

    public List<SocialComment> getAllReplies() {
        List<SocialComment> allReplies = new ArrayList<>();
        for (SocialComment reply : replies) {
            allReplies.add(reply);
            allReplies.addAll(reply.getAllReplies()); // Recursive to get nested replies
        }
        return allReplies;
    }

    public int getTotalRepliesCount() {
        int total = replies.size();
        for (SocialComment reply : replies) {
            total += reply.getTotalRepliesCount(); // Recursive count
        }
        return total;
    }

    public boolean isPopular() {
        return (likesCount != null && likesCount > 5) ||
                (repliesCount != null && repliesCount > 3);
    }

    // ==================== ENUMS ====================

    public enum ModerationStatus {
        APPROVED("Approved"),
        UNDER_REVIEW("Under Review"),
        REJECTED("Rejected"),
        AUTO_FLAGGED("Auto-flagged");

        private final String description;

        ModerationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public boolean isVisible() {
            return this == APPROVED;
        }

        public boolean needsReview() {
            return this == UNDER_REVIEW || this == AUTO_FLAGGED;
        }
    }

    // ==================== JPA LIFECYCLE ====================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // Increment parent post's comment count
        if (post != null) {
            post.incrementComments();
        }

        // Increment parent comment's reply count
        if (parentComment != null) {
            parentComment.repliesCount = (parentComment.repliesCount == null ? 0 : parentComment.repliesCount) + 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @PreRemove
    protected void onRemove() {
        // Decrement parent post's comment count
        if (post != null) {
            post.decrementComments();
        }

        // Decrement parent comment's reply count
        if (parentComment != null) {
            parentComment.repliesCount = Math.max(0, (parentComment.repliesCount == null ? 0 : parentComment.repliesCount) - 1);
        }
    }
}