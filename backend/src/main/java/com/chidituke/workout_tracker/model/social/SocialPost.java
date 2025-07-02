package com.chidituke.workout_tracker.model.social;

import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.workout.WorkoutSession;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Entity
@Table(name = "social_posts")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"author", "workoutSession", "hashtags", "postLikes"})
@EqualsAndHashCode(exclude = {"author", "workoutSession", "hashtags", "postLikes"})
public class SocialPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_post_id")
    private Long id;

    // ==================== AUTHOR & RELATIONSHIPS ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // Workout-specific relationship (when postType = WORKOUT_COMPLETION)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_session_id")
    private WorkoutSession workoutSession;

    // ==================== CONTENT ====================

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false)
    @Builder.Default
    private PostType postType = PostType.TEXT;

    @Column(name = "content", columnDefinition = "TEXT")
    @Size(max = 2000, message = "Post content cannot exceed 2000 characters")
    private String content;

    @Column(name = "media_url", length = 500)
    private String mediaUrl;  // Image/video URL

    @Column(name = "link_url", length = 500)
    private String linkUrl;   // Shared link URL

    @Column(name = "link_title", length = 200)
    private String linkTitle; // Title of shared link

    @Column(name = "link_description", length = 500)
    private String linkDescription; // Description of shared link

    // ==================== PRIVACY & VISIBILITY ====================

    @Enumerated(EnumType.STRING)
    @Column(name = "privacy_level", nullable = false)
    @Builder.Default
    private PrivacyLevel privacyLevel = PrivacyLevel.PUBLIC;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    // ==================== ENGAGEMENT METRICS ====================

    @Column(name = "likes_count")
    @Builder.Default
    private Integer likesCount = 0;

    @Column(name = "comments_count")
    @Builder.Default
    private Integer commentsCount = 0;

    @Column(name = "shares_count")
    @Builder.Default
    private Integer sharesCount = 0;

    @Column(name = "views_count")
    @Builder.Default
    private Integer viewsCount = 0;

    // ==================== CONTENT MODERATION ====================

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status")
    @Builder.Default
    private ModerationStatus moderationStatus = ModerationStatus.APPROVED;

    @Column(name = "flagged_count")
    @Builder.Default
    private Integer flaggedCount = 0;

    @Column(name = "moderation_reason", length = 500)
    private String moderationReason;

    @Column(name = "moderated_by_user_id")
    private Long moderatedByUserId;

    @Column(name = "moderated_at")
    private LocalDateTime moderatedAt;

    // ==================== HASHTAGS & MENTIONS - USING ENTITY RELATIONSHIPS ====================

    // Use proper entity relationship instead of @ElementCollection
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<PostHashtag> hashtags = new ArrayList<>();

    // Use proper entity relationship for likes
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<PostLike> postLikes = new ArrayList<>();

    // Mentions using the correct table structure from V009 migration
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "social_post_mentions", joinColumns = @JoinColumn(name = "social_post_id"))
    @Column(name = "mentioned_user_id")
    @Size(max = 20, message = "Maximum 20 mentions allowed")
    @Builder.Default
    private List<Long> mentionedUserIds = new ArrayList<>();

    // ==================== LOCATION & CONTEXT ====================

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "workout_location") // For workout posts
    @Enumerated(EnumType.STRING)
    private WorkoutSession.WorkoutLocation workoutLocation;

    // ==================== TIMESTAMPS ====================

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ==================== BUSINESS LOGIC METHODS ====================

    public boolean isWorkoutPost() {
        return postType == PostType.WORKOUT_COMPLETION && workoutSession != null;
    }

    public boolean hasMedia() {
        return mediaUrl != null && !mediaUrl.trim().isEmpty();
    }

    public boolean hasLink() {
        return linkUrl != null && !linkUrl.trim().isEmpty();
    }

    public boolean isPublic() {
        return privacyLevel == PrivacyLevel.PUBLIC;
    }

    public boolean isVisibleTo(User viewer) {
        if (!isActive || moderationStatus != ModerationStatus.APPROVED) {
            return author.equals(viewer); // Only author can see inactive/moderated posts
        }

        return switch (privacyLevel) {
            case PUBLIC -> true;
            case FRIENDS_ONLY -> areUsersFriends(author, viewer);
            case PRIVATE -> author.equals(viewer);
        };
    }

    private boolean areUsersFriends(User user1, User user2) {
        // This would check UserRelationship entity - simplified for now
        // TODO: Implement proper friend checking logic via UserRelationshipService
        return true;
    }

    public String getContentPreview(int maxLength) {
        if (content == null) return "";
        return content.length() <= maxLength ? content : content.substring(0, maxLength) + "...";
    }

    public void incrementLikes() {
        this.likesCount = (this.likesCount == null ? 0 : this.likesCount) + 1;
    }

    public void decrementLikes() {
        this.likesCount = Math.max(0, (this.likesCount == null ? 0 : this.likesCount) - 1);
    }

    public void incrementComments() {
        this.commentsCount = (this.commentsCount == null ? 0 : this.commentsCount) + 1;
    }

    public void decrementComments() {
        this.commentsCount = Math.max(0, (this.commentsCount == null ? 0 : this.commentsCount) - 1);
    }

    public void incrementViews() {
        this.viewsCount = (this.viewsCount == null ? 0 : this.viewsCount) + 1;
    }

    public void incrementShares() {
        this.sharesCount = (this.sharesCount == null ? 0 : this.sharesCount) + 1;
    }

    public void flagForModeration() {
        this.flaggedCount = (this.flaggedCount == null ? 0 : this.flaggedCount) + 1;
        if (this.flaggedCount >= 3) { // Auto-hide after 3 flags
            this.moderationStatus = ModerationStatus.UNDER_REVIEW;
        }
    }

    public boolean isPopular() {
        int totalEngagement = (likesCount != null ? likesCount : 0) +
                (commentsCount != null ? commentsCount : 0) +
                (sharesCount != null ? sharesCount : 0);
        return totalEngagement > 10; // Configurable threshold
    }

    public double getEngagementRate() {
        if (viewsCount == null || viewsCount == 0) return 0.0;

        int totalEngagement = (likesCount != null ? likesCount : 0) +
                (commentsCount != null ? commentsCount : 0) +
                (sharesCount != null ? sharesCount : 0);

        return (double) totalEngagement / viewsCount * 100;
    }

    // ==================== HASHTAG HELPER METHODS ====================

    public void addHashtag(String hashtag) {
        String normalizedHashtag = PostHashtag.normalizeHashtagStatic(hashtag);
        if (normalizedHashtag != null && !hasHashtag(normalizedHashtag)) {
            PostHashtag postHashtag = new PostHashtag(this, normalizedHashtag);
            this.hashtags.add(postHashtag);
        }
    }

    public void removeHashtag(String hashtag) {
        String normalizedHashtag = PostHashtag.normalizeHashtagStatic(hashtag);
        if (normalizedHashtag != null) {
            this.hashtags.removeIf(ph -> normalizedHashtag.equals(ph.getHashtag()));
        }
    }

    public boolean hasHashtag(String hashtag) {
        String normalizedHashtag = PostHashtag.normalizeHashtagStatic(hashtag);
        return normalizedHashtag != null &&
                this.hashtags.stream().anyMatch(ph -> normalizedHashtag.equals(ph.getHashtag()));
    }

    public List<String> getHashtagStrings() {
        return this.hashtags.stream()
                .map(PostHashtag::getHashtag)
                .collect(Collectors.toList());
    }

    // ==================== LIKE HELPER METHODS ====================

    public boolean isLikedBy(User user) {
        return this.postLikes.stream()
                .anyMatch(like -> like.getUser().equals(user));
    }

    public void addLike(User user) {
        if (!isLikedBy(user)) {
            PostLike like = new PostLike(this, user);
            this.postLikes.add(like);
            incrementLikes();
        }
    }

    public void removeLike(User user) {
        boolean removed = this.postLikes.removeIf(like -> like.getUser().equals(user));
        if (removed) {
            decrementLikes();
        }
    }

    // ==================== ENUMS ====================

    public enum PostType {
        TEXT("Text Post", "📝"),
        IMAGE("Image Post", "📸"),
        VIDEO("Video Post", "🎥"),
        WORKOUT_COMPLETION("Workout Completion", "🏋️"),
        LINK("Shared Link", "🔗"),
        ACHIEVEMENT("Achievement", "🏆"),
        MOTIVATION("Motivation", "💪"),
        PROGRESS_UPDATE("Progress Update", "📈"),
        CHECK_IN("Gym Check-in", "📍");

        private final String displayName;
        private final String emoji;

        PostType(String displayName, String emoji) {
            this.displayName = displayName;
            this.emoji = emoji;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getEmoji() {
            return emoji;
        }

        public String getDisplayWithEmoji() {
            return emoji + " " + displayName;
        }
    }

    public enum PrivacyLevel {
        PUBLIC("Public - Visible to everyone"),
        FRIENDS_ONLY("Friends Only - Visible to connections"),
        PRIVATE("Private - Only visible to you");

        private final String description;

        PrivacyLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

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

        // Extract hashtags from content
        extractHashtags();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private void extractHashtags() {
        if (content == null) return;

        // Clear existing hashtags to avoid duplicates
        this.hashtags.clear();

        String[] words = content.split("\\s+");
        for (String word : words) {
            if (word.startsWith("#") && word.length() > 1) {
                String hashtag = word.substring(1).toLowerCase().replaceAll("[^a-zA-Z0-9_]", "");
                if (!hashtag.isEmpty() && hashtag.length() <= 30) {
                    addHashtag(hashtag);
                    // Limit to 10 hashtags
                    if (this.hashtags.size() >= 10) {
                        break;
                    }
                }
            }
        }
    }
}