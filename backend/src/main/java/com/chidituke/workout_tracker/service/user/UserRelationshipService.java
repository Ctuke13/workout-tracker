package com.chidituke.workout_tracker.service.user;

import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.user.UserRelationship;
import com.chidituke.workout_tracker.repository.user.UserRelationshipRepository;
import com.chidituke.workout_tracker.service.notifications.NotificationsService;
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
public class UserRelationshipService {

    private final UserRelationshipRepository userRelationshipRepository;
    private final UserService userService;
    private final NotificationsService notificationsService;

    // ==================== FOLLOW OPERATIONS ====================

    @Transactional
    public RelationshipResult followUser(String followerUsername, String followingUsername) {
        User follower = userService.getUserByUsername(followerUsername);
        User following = userService.getUserByUsername(followingUsername);

        // Validate follow request
        validateFollowRequest(follower, following);

        // Check if relationship already exists
        Optional<UserRelationship> existingRelationship =
                userRelationshipRepository.findByFollowerAndFollowing(follower, following);

        if (existingRelationship.isPresent()) {
            UserRelationship relationship = existingRelationship.get();

            // Handle existing relationship based on current state
            return handleExistingRelationship(relationship, follower, following);
        }

        // Create new follow relationship
        UserRelationship newRelationship = UserRelationship.createFollowRelationship(follower, following);
        userRelationshipRepository.save(newRelationship);

        // Update user counters
        follower.updateFollowingCount(1);
        following.updateFollowersCount(1);
        userService.save(follower);
        userService.save(following);

        // Send notification
        notificationsService.notifyNewFollower(follower, following);

        log.info("User {} started following {}", followerUsername, followingUsername);

        return RelationshipResult.builder()
                .success(true)
                .relationshipType(UserRelationship.RelationshipType.FOLLOW)
                .status(UserRelationship.RelationshipStatus.ACTIVE)
                .message("Successfully followed user")
                .build();
    }

    @Transactional
    public RelationshipResult unfollowUser(String followerUsername, String followingUsername) {
        User follower = userService.getUserByUsername(followerUsername);
        User following = userService.getUserByUsername(followingUsername);

        UserRelationship relationship = findActiveRelationship(follower, following);

        if (relationship == null) {
            return RelationshipResult.builder()
                    .success(false)
                    .message("No active relationship to unfollow")
                    .build();
        }

        // Remove relationship
        userRelationshipRepository.delete(relationship);

        // Update user counters
        follower.updateFollowingCount(-1);
        following.updateFollowersCount(-1);
        userService.save(follower);
        userService.save(following);

        log.info("User {} unfollowed {}", followerUsername, followingUsername);

        return RelationshipResult.builder()
                .success(true)
                .message("Successfully unfollowed user")
                .build();
    }

    // ==================== FRIEND REQUEST OPERATIONS ====================

    @Transactional
    public RelationshipResult sendFriendRequest(String requesterUsername, String targetUsername) {
        User requester = userService.getUserByUsername(requesterUsername);
        User target = userService.getUserByUsername(targetUsername);

        // Validate friend request
        validateFriendRequest(requester, target);

        // Check if relationship already exists
        Optional<UserRelationship> existingRelationship =
                userRelationshipRepository.findByFollowerAndFollowing(requester, target);

        if (existingRelationship.isPresent()) {
            return RelationshipResult.builder()
                    .success(false)
                    .message("Relationship already exists")
                    .build();
        }

        // Create friend request
        UserRelationship friendRequest = UserRelationship.createFriendRequest(requester, target);
        userRelationshipRepository.save(friendRequest);

        // Send notification
        notificationsService.notifyFriendRequest(requester, target);

        log.info("Friend request sent from {} to {}", requesterUsername, targetUsername);

        return RelationshipResult.builder()
                .success(true)
                .relationshipType(UserRelationship.RelationshipType.FRIEND)
                .status(UserRelationship.RelationshipStatus.PENDING)
                .message("Friend request sent successfully")
                .build();
    }

    @Transactional
    public RelationshipResult acceptFriendRequest(String targetUsername, String requesterUsername) {
        User target = userService.getUserByUsername(targetUsername);
        User requester = userService.getUserByUsername(requesterUsername);

        UserRelationship request = findPendingFriendRequest(requester, target);

        if (request == null) {
            return RelationshipResult.builder()
                    .success(false)
                    .message("No pending friend request found")
                    .build();
        }

        // Accept the friend request
        request.acceptFriendRequest();
        userRelationshipRepository.save(request);

        // Create reciprocal friend relationship
        UserRelationship reciprocal = UserRelationship.createFriendRequest(target, requester);
        reciprocal.acceptFriendRequest();
        userRelationshipRepository.save(reciprocal);

        // Update user counters
        target.updateFollowingCount(1);
        target.updateFollowersCount(1);
        requester.updateFollowingCount(1);
        requester.updateFollowersCount(1);
        userService.save(target);
        userService.save(requester);

        // Send notification
        notificationsService.notifyFriendRequestAccepted(target, requester);

        log.info("Friend request accepted: {} and {} are now friends", targetUsername, requesterUsername);

        return RelationshipResult.builder()
                .success(true)
                .relationshipType(UserRelationship.RelationshipType.FRIEND)
                .status(UserRelationship.RelationshipStatus.ACTIVE)
                .message("Friend request accepted successfully")
                .build();
    }

    @Transactional
    public RelationshipResult declineFriendRequest(String targetUsername, String requesterUsername) {
        User target = userService.getUserByUsername(targetUsername);
        User requester = userService.getUserByUsername(requesterUsername);

        UserRelationship request = findPendingFriendRequest(requester, target);

        if (request == null) {
            return RelationshipResult.builder()
                    .success(false)
                    .message("No pending friend request found")
                    .build();
        }

        // Decline the friend request
        request.rejectFriendRequest();
        userRelationshipRepository.save(request);

        log.info("Friend request declined: {} declined {}'s request", targetUsername, requesterUsername);

        return RelationshipResult.builder()
                .success(true)
                .message("Friend request declined")
                .build();
    }

    // ==================== CLOSE FRIENDS MANAGEMENT ====================

    @Transactional
    public RelationshipResult toggleCloseFriend(String username, String friendUsername) {
        User user = userService.getUserByUsername(username);
        User friend = userService.getUserByUsername(friendUsername);

        UserRelationship relationship = findActiveRelationship(user, friend);

        if (relationship == null || !relationship.isMutualFriend()) {
            return RelationshipResult.builder()
                    .success(false)
                    .message("Not friends with this user")
                    .build();
        }

        // Toggle close friend status
        relationship.toggleCloseFriend();
        userRelationshipRepository.save(relationship);

        String action = relationship.isCloseFriend() ? "added to" : "removed from";
        log.info("User {} {} close friends by {}", friendUsername, action, username);

        return RelationshipResult.builder()
                .success(true)
                .message(String.format("User %s %s close friends", action, "close friends"))
                .isCloseFriend(relationship.isCloseFriend())
                .build();
    }

    public Page<User> getCloseFriends(String username, Pageable pageable) {
        User user = userService.getUserByUsername(username);
        return userRelationshipRepository.findCloseFriends(user, pageable);
    }

    public List<UserRelationship> getSuggestedCloseFriends(String username, int minInteractionScore) {
        User user = userService.getUserByUsername(username);
        return userRelationshipRepository.findPotentialCloseFriends(user, minInteractionScore);
    }

    // ==================== MUTING OPERATIONS ====================

    @Transactional
    public RelationshipResult toggleMute(String username, String targetUsername) {
        User user = userService.getUserByUsername(username);
        User target = userService.getUserByUsername(targetUsername);

        UserRelationship relationship = findActiveRelationship(user, target);

        if (relationship == null) {
            return RelationshipResult.builder()
                    .success(false)
                    .message("No relationship exists with this user")
                    .build();
        }

        // Toggle mute status
        relationship.toggleMute();
        userRelationshipRepository.save(relationship);

        String action = relationship.isMuted() ? "muted" : "unmuted";
        log.info("User {} {} {}", targetUsername, action, username);

        return RelationshipResult.builder()
                .success(true)
                .message(String.format("User %s", action))
                .isMuted(relationship.isMuted())
                .build();
    }

    public Page<User> getMutedUsers(String username, Pageable pageable) {
        User user = userService.getUserByUsername(username);
        return userRelationshipRepository.findMutedUsers(user, pageable);
    }

    // ==================== BLOCKING OPERATIONS ====================

    @Transactional
    public RelationshipResult blockUser(String blockerUsername, String targetUsername) {
        User blocker = userService.getUserByUsername(blockerUsername);
        User target = userService.getUserByUsername(targetUsername);

        // Validate block request
        validateBlockRequest(blocker, target);

        // Remove existing relationship if it exists
        Optional<UserRelationship> existingRelationship =
                userRelationshipRepository.findByFollowerAndFollowing(blocker, target);

        if (existingRelationship.isPresent()) {
            userRelationshipRepository.delete(existingRelationship.get());

            // Update counters if it was an active follow/friend relationship
            if (existingRelationship.get().isActive()) {
                blocker.updateFollowingCount(-1);
                target.updateFollowersCount(-1);
            }
        }

        // Create block relationship
        UserRelationship blockRelationship = UserRelationship.createBlockRelationship(blocker, target);
        userRelationshipRepository.save(blockRelationship);

        userService.save(blocker);
        userService.save(target);

        log.info("User {} blocked {}", blockerUsername, targetUsername);

        return RelationshipResult.builder()
                .success(true)
                .relationshipType(UserRelationship.RelationshipType.BLOCKED)
                .status(UserRelationship.RelationshipStatus.ACTIVE)
                .message("User blocked successfully")
                .build();
    }

    @Transactional
    public RelationshipResult unblockUser(String blockerUsername, String targetUsername) {
        User blocker = userService.getUserByUsername(blockerUsername);
        User target = userService.getUserByUsername(targetUsername);

        Optional<UserRelationship> blockRelationship =
                userRelationshipRepository.findByFollowerAndFollowing(blocker, target);

        if (blockRelationship.isEmpty() ||
                !(blockRelationship.get().getRelationshipType() == UserRelationship.RelationshipType.BLOCKED &&
                        blockRelationship.get().getStatus() == UserRelationship.RelationshipStatus.ACTIVE)) {
            return RelationshipResult.builder()
                    .success(false)
                    .message("User is not blocked")
                    .build();
        }

        // Remove block relationship
        userRelationshipRepository.delete(blockRelationship.get());

        log.info("User {} unblocked {}", blockerUsername, targetUsername);

        return RelationshipResult.builder()
                .success(true)
                .message("User unblocked successfully")
                .build();
    }

    public boolean isUserBlocked(String targetUsername, String potentialBlockerUsername) {
        User target = userService.getUserByUsername(targetUsername);
        User blocker = userService.getUserByUsername(potentialBlockerUsername);

        return userRelationshipRepository.isUserBlockedBy(target, blocker);
    }

    public Page<User> getBlockedUsers(String username, Pageable pageable) {
        User user = userService.getUserByUsername(username);
        return userRelationshipRepository.findBlockedUsers(user, pageable);
    }

    // ==================== FEED GENERATION ====================

    public Page<User> getUsersForFeed(String username, Pageable pageable) {
        User user = userService.getUserByUsername(username);
        return userRelationshipRepository.findUsersForFeed(user, pageable);
    }

    public List<UserRelationship> getFeedPriorityRelationships(String username, int limit) {
        User user = userService.getUserByUsername(username);
        List<UserRelationship> relationships = userRelationshipRepository.findFeedPriorityRelationships(user);
        return relationships.stream().limit(limit).toList();
    }

    // ==================== INTERACTION TRACKING ====================

    @Transactional
    public void recordInteraction(String userUsername, String targetUsername) {
        User user = userService.getUserByUsername(userUsername);
        User target = userService.getUserByUsername(targetUsername);

        Optional<UserRelationship> relationshipOpt =
                userRelationshipRepository.findActiveRelationship(user, target);

        if (relationshipOpt.isPresent()) {
            UserRelationship relationship = relationshipOpt.get();
            relationship.updateLastInteraction();
            userRelationshipRepository.save(relationship);

            log.debug("Interaction recorded between {} and {}", userUsername, targetUsername);
        }
    }

    @Transactional
    public void batchUpdateInteractionScores() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusWeeks(1);
        List<UserRelationship> activeRelationships =
                userRelationshipRepository.findRecentlyActiveRelationships(cutoffDate);

        for (UserRelationship relationship : activeRelationships) {
            // Update interaction score based on your algorithm
            // This is handled by the entity's updateInteractionScore method
            userRelationshipRepository.save(relationship);
        }

        log.info("Updated interaction scores for {} relationships", activeRelationships.size());
    }

    // ==================== RELATIONSHIP STATUS & INFO ====================

    public RelationshipInfo getRelationshipInfo(String userUsername, String targetUsername) {
        User user = userService.getUserByUsername(userUsername);
        User target = userService.getUserByUsername(targetUsername);

        Optional<UserRelationship> relationshipOpt =
                userRelationshipRepository.findByFollowerAndFollowing(user, target);

        Optional<UserRelationship> reverseRelationshipOpt =
                userRelationshipRepository.findByFollowerAndFollowing(target, user);

        return RelationshipInfo.builder()
                .userFollowsTarget(relationshipOpt.isPresent() && relationshipOpt.get().isActive())
                .targetFollowsUser(reverseRelationshipOpt.isPresent() && reverseRelationshipOpt.get().isActive())
                .areFriends(relationshipOpt.isPresent() && relationshipOpt.get().isMutualFriend() &&
                        reverseRelationshipOpt.isPresent() && reverseRelationshipOpt.get().isMutualFriend())
                .isCloseFriend(relationshipOpt.map(UserRelationship::isCloseFriend).orElse(false))
                .isMuted(relationshipOpt.map(UserRelationship::isMuted).orElse(false))
                .isBlocked(userRelationshipRepository.isUserBlockedBy(target, user))
                .isBlockedBy(userRelationshipRepository.isUserBlockedBy(user, target))
                .hasPendingFriendRequest(relationshipOpt.isPresent() && relationshipOpt.get().isPending())
                .mutualConnectionCount(userRelationshipRepository.countMutualConnections(user, target))
                .build();
    }

    // ==================== USER LISTS ====================

    public Page<User> getFollowers(String username, Pageable pageable) {
        User user = userService.getUserByUsername(username);
        return userRelationshipRepository.findFollowersOf(user, pageable);
    }

    public Page<User> getFollowing(String username, Pageable pageable) {
        User user = userService.getUserByUsername(username);
        return userRelationshipRepository.findUsersFollowedBy(user, pageable);
    }

    public Page<User> getMutualFriends(String username, Pageable pageable) {
        User user = userService.getUserByUsername(username);
        return userRelationshipRepository.findMutualFriends(user, pageable);
    }

    public Page<UserRelationship> getPendingFriendRequests(String username, Pageable pageable) {
        User user = userService.getUserByUsername(username);
        return userRelationshipRepository.findPendingFriendRequestsReceivedBy(user, pageable);
    }

    public Page<User> getSuggestedConnections(String username, Pageable pageable) {
        User user = userService.getUserByUsername(username);
        return userRelationshipRepository.findSuggestedConnections(user, pageable);
    }

    // ==================== ANALYTICS & STATISTICS ====================

    public UserRelationshipStats getRelationshipStatistics(String username) {
        User user = userService.getUserByUsername(username);

        Long followersCount = userRelationshipRepository.countFollowersOf(user);
        Long followingCount = userRelationshipRepository.countUsersFollowedBy(user);
        Long closeFriendsCount = userRelationshipRepository.countCloseFriends(user);
        Long pendingRequestsCount = userRelationshipRepository.countPendingFriendRequestsFor(user);
        Double averageInteractionScore = userRelationshipRepository.getAverageInteractionScore(user);

        return UserRelationshipStats.builder()
                .username(username)
                .followersCount(followersCount)
                .followingCount(followingCount)
                .closeFriendsCount(closeFriendsCount)
                .pendingFriendRequestsCount(pendingRequestsCount)
                .averageInteractionScore(averageInteractionScore != null ? averageInteractionScore : 0.0)
                .build();
    }

    public List<UserRelationship> getLowEngagementRelationships(String username, int days, int minScore) {
        User user = userService.getUserByUsername(username);
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return userRelationshipRepository.findLowEngagementRelationships(user, cutoffDate, minScore);
    }

    // ==================== HELPER METHODS ====================

    private UserRelationship findActiveRelationship(User follower, User following) {
        return userRelationshipRepository.findActiveRelationship(follower, following).orElse(null);
    }

    private UserRelationship findPendingFriendRequest(User requester, User target) {
        Optional<UserRelationship> relationship =
                userRelationshipRepository.findByFollowerAndFollowing(requester, target);

        if (relationship.isPresent() && relationship.get().isPending() &&
                relationship.get().getRelationshipType() == UserRelationship.RelationshipType.FRIEND) {
            return relationship.get();
        }
        return null;
    }

    private RelationshipResult handleExistingRelationship(UserRelationship relationship, User follower, User following) {
        if (relationship.getRelationshipType() == UserRelationship.RelationshipType.BLOCKED &&
                relationship.getStatus() == UserRelationship.RelationshipStatus.ACTIVE) {
            return RelationshipResult.builder()
                    .success(false)
                    .message("Cannot follow blocked user")
                    .build();
        }

        if (relationship.isActive()) {
            return RelationshipResult.builder()
                    .success(false)
                    .message("Already following this user")
                    .build();
        }

        // Reactivate relationship by setting status back to ACTIVE
        relationship.setStatus(UserRelationship.RelationshipStatus.ACTIVE);
        relationship.setUpdatedAt(LocalDateTime.now());
        userRelationshipRepository.save(relationship);

        return RelationshipResult.builder()
                .success(true)
                .message("Reactivated relationship")
                .build();
    }

    private void validateFollowRequest(User follower, User following) {
        if (follower.equals(following)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }

        if (userRelationshipRepository.isUserBlockedBy(follower, following)) {
            throw new IllegalArgumentException("Cannot follow user who has blocked you");
        }

        if (userRelationshipRepository.isUserBlockedBy(following, follower)) {
            throw new IllegalArgumentException("Cannot follow blocked user");
        }
    }

    private void validateFriendRequest(User requester, User target) {
        if (requester.equals(target)) {
            throw new IllegalArgumentException("Cannot send friend request to yourself");
        }

        if (!target.acceptsFriendRequests()) {
            throw new IllegalArgumentException("User does not accept friend requests");
        }

        if (userRelationshipRepository.isUserBlockedBy(requester, target) ||
                userRelationshipRepository.isUserBlockedBy(target, requester)) {
            throw new IllegalArgumentException("Cannot send friend request to blocked user");
        }
    }

    private void validateBlockRequest(User blocker, User target) {
        if (blocker.equals(target)) {
            throw new IllegalArgumentException("Cannot block yourself");
        }
    }

    // ==================== INNER CLASSES (DTOs) ====================

    @lombok.Data
    @lombok.Builder
    public static class RelationshipResult {
        private boolean success;
        private String message;
        private UserRelationship.RelationshipType relationshipType;
        private UserRelationship.RelationshipStatus status;
        private Boolean isCloseFriend;
        private Boolean isMuted;
    }

    @lombok.Data
    @lombok.Builder
    public static class RelationshipInfo {
        private boolean userFollowsTarget;
        private boolean targetFollowsUser;
        private boolean areFriends;
        private boolean isCloseFriend;
        private boolean isMuted;
        private boolean isBlocked;
        private boolean isBlockedBy;
        private boolean hasPendingFriendRequest;
        private Long mutualConnectionCount;
    }

    @lombok.Data
    @lombok.Builder
    public static class UserRelationshipStats {
        private String username;
        private Long followersCount;
        private Long followingCount;
        private Long closeFriendsCount;
        private Long pendingFriendRequestsCount;
        private Double averageInteractionScore;
    }
}