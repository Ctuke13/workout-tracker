package com.chidituke.workout_tracker.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_relationships",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"follower_id", "following_id"})
        })
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"follower", "following"})
@EqualsAndHashCode(exclude = {"follower", "following"})
public class UserRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_relationship_id")
    private Long id;

    // ==================== RELATIONSHIPS ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;  // Person who follows

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private User following; // Person being followed

    // ==================== RELATIONSHIP TYPE ====================

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false)
    @Builder.Default
    private RelationshipType relationshipType = RelationshipType.FOLLOW;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private RelationshipStatus status = RelationshipStatus.ACTIVE;

    // ==================== INTERACTION PREFERENCES ====================

    @Column(name = "notifications_enabled")
    @Builder.Default
    private Boolean notificationsEnabled = true;

    @Column(name = "show_in_feed")
    @Builder.Default
    private Boolean showInFeed = true;

    @Column(name = "close_friend")
    @Builder.Default
    private Boolean closeFriend = false;

    @Column(name = "muted")
    @Builder.Default
    private Boolean muted = false;

    // ==================== INTERACTION TRACKING ====================

    @Column(name = "interaction_score")
    @Builder.Default
    private Integer interactionScore = 0; // Algorithm scoring for feed priority

    @Column(name = "last_post_seen")
    private LocalDateTime lastPostSeen;

    @Column(name = "total_interactions")
    @Builder.Default
    private Integer totalInteractions = 0;

    // ==================== TIMESTAMPS ====================

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "last_interaction")
    private LocalDateTime lastInteraction;

    // ==================== BUSINESS LOGIC METHODS ====================

    public boolean isActive() {
        return status == RelationshipStatus.ACTIVE;
    }

    public boolean isPending() {
        return status == RelationshipStatus.PENDING;
    }

    public boolean isBlocked() {
        return relationshipType == RelationshipType.BLOCKED;
    }

    public boolean isMutualFriend() {
        return relationshipType == RelationshipType.FRIEND && status == RelationshipStatus.ACTIVE;
    }

    public boolean isFollowOnly() {
        return relationshipType == RelationshipType.FOLLOW && status == RelationshipStatus.ACTIVE;
    }

    public boolean isCloseFriend() {
        return Boolean.TRUE.equals(closeFriend) && isActive();
    }

    public boolean isMuted() {
        return Boolean.TRUE.equals(muted);
    }

    public boolean shouldShowInFeed() {
        return Boolean.TRUE.equals(showInFeed) && isActive() && !isMuted();
    }

    public boolean shouldSendNotifications() {
        return Boolean.TRUE.equals(notificationsEnabled) && isActive() && !isMuted();
    }

    public void updateLastInteraction() {
        this.lastInteraction = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.totalInteractions = (this.totalInteractions == null ? 0 : this.totalInteractions) + 1;

        // Update interaction score for feed algorithm
        updateInteractionScore();
    }

    public void markPostAsSeen() {
        this.lastPostSeen = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void toggleMute() {
        this.muted = !Boolean.TRUE.equals(this.muted);
        this.updatedAt = LocalDateTime.now();

        // Reset interaction score when muted
        if (Boolean.TRUE.equals(this.muted)) {
            this.interactionScore = 0;
        }
    }

    public void toggleCloseFriend() {
        this.closeFriend = !Boolean.TRUE.equals(this.closeFriend);
        this.updatedAt = LocalDateTime.now();

        // Boost interaction score for close friends
        if (Boolean.TRUE.equals(this.closeFriend)) {
            this.interactionScore = (this.interactionScore == null ? 0 : this.interactionScore) + 50;
        }
    }

    public void toggleNotifications() {
        this.notificationsEnabled = !Boolean.TRUE.equals(this.notificationsEnabled);
        this.updatedAt = LocalDateTime.now();
    }

    public void block() {
        this.relationshipType = RelationshipType.BLOCKED;
        this.status = RelationshipStatus.ACTIVE; // Blocked relationships are "active" blocks
        this.showInFeed = false;
        this.notificationsEnabled = false;
        this.closeFriend = false;
        this.interactionScore = 0;
        this.updatedAt = LocalDateTime.now();
    }

    public void unblock() {
        if (this.relationshipType == RelationshipType.BLOCKED) {
            this.relationshipType = RelationshipType.FOLLOW; // Default back to follow
            this.showInFeed = true;
            this.notificationsEnabled = true;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void acceptFriendRequest() {
        if (this.status == RelationshipStatus.PENDING) {
            this.status = RelationshipStatus.ACTIVE;
            this.relationshipType = RelationshipType.FRIEND;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void rejectFriendRequest() {
        if (this.status == RelationshipStatus.PENDING) {
            this.status = RelationshipStatus.INACTIVE;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void unfriend() {
        if (this.relationshipType == RelationshipType.FRIEND) {
            this.relationshipType = RelationshipType.FOLLOW;
            this.closeFriend = false;
            this.updatedAt = LocalDateTime.now();
        }
    }

    private void updateInteractionScore() {
        if (this.interactionScore == null) this.interactionScore = 0;

        // Increase score based on recent interactions
        if (this.lastInteraction != null) {
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

            if (this.lastInteraction.isAfter(oneWeekAgo)) {
                this.interactionScore += 10; // Recent interaction bonus
            } else if (this.lastInteraction.isAfter(oneMonthAgo)) {
                this.interactionScore += 5; // Moderate interaction bonus
            }
        }

        // Bonus for close friends
        if (Boolean.TRUE.equals(this.closeFriend)) {
            this.interactionScore += 20;
        }

        // Cap the score
        this.interactionScore = Math.min(this.interactionScore, 1000);
    }

    public int getFeedPriority() {
        if (!shouldShowInFeed()) return 0;

        int priority = this.interactionScore != null ? this.interactionScore : 0;

        // Boost priority for close friends
        if (isCloseFriend()) {
            priority += 100;
        }

        // Boost priority for mutual friends
        if (isMutualFriend()) {
            priority += 50;
        }

        // Reduce priority if no recent interactions
        if (this.lastInteraction == null ||
                this.lastInteraction.isBefore(LocalDateTime.now().minusMonths(3))) {
            priority = Math.max(0, priority - 50);
        }

        return priority;
    }

    public String getRelationshipDescription() {
        if (isBlocked()) {
            return "Blocked";
        }

        if (isPending()) {
            return "Pending " + relationshipType.getDisplayName().toLowerCase();
        }

        if (isMutualFriend()) {
            return isCloseFriend() ? "Close Friend" : "Friend";
        }

        if (isFollowOnly()) {
            return "Following";
        }

        return relationshipType.getDisplayName();
    }

    public boolean canSendMessage() {
        return isActive() && !isBlocked() && !isMuted();
    }

    public boolean canViewPosts() {
        return isActive() && !isBlocked();
    }

    public boolean canViewProfile() {
        return !isBlocked(); // Can view profile even if not following, unless blocked
    }

    // ==================== STATIC HELPER METHODS ====================

    public static UserRelationship createFollowRelationship(User follower, User following) {
        return UserRelationship.builder()
                .follower(follower)
                .following(following)
                .relationshipType(RelationshipType.FOLLOW)
                .status(RelationshipStatus.ACTIVE)
                .build();
    }

    public static UserRelationship createFriendRequest(User requester, User target) {
        return UserRelationship.builder()
                .follower(requester)
                .following(target)
                .relationshipType(RelationshipType.FRIEND)
                .status(RelationshipStatus.PENDING)
                .build();
    }

    public static UserRelationship createBlockRelationship(User blocker, User blocked) {
        return UserRelationship.builder()
                .follower(blocker)
                .following(blocked)
                .relationshipType(RelationshipType.BLOCKED)
                .status(RelationshipStatus.ACTIVE)
                .showInFeed(false)
                .notificationsEnabled(false)
                .build();
    }

    // ==================== ENUMS ====================

    public enum RelationshipType {
        FOLLOW("Follow"),
        FRIEND("Friend"),          // Mutual follow with additional privileges
        BLOCKED("Blocked");

        private final String displayName;

        RelationshipType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean allowsMessaging() {
            return this == FRIEND;
        }

        public boolean allowsNotifications() {
            return this != BLOCKED;
        }

        public boolean showsInFeed() {
            return this != BLOCKED;
        }
    }

    public enum RelationshipStatus {
        ACTIVE("Active"),
        PENDING("Pending"),        // Friend request pending
        INACTIVE("Inactive"),      // Temporarily disabled or rejected
        SUSPENDED("Suspended");    // Temporarily suspended by system

        private final String displayName;

        RelationshipStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean isActionable() {
            return this == PENDING;
        }

        public boolean isEffective() {
            return this == ACTIVE;
        }
    }

    // ==================== JPA LIFECYCLE ====================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // Initialize interaction score
        if (interactionScore == null) {
            interactionScore = relationshipType == RelationshipType.FRIEND ? 50 : 10;
        }

        // Prevent self-following
        if (follower != null && following != null && follower.getId().equals(following.getId())) {
            throw new IllegalStateException("Users cannot follow themselves");
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}