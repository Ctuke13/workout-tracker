package com.chidituke.workout_tracker.service.workout;

import com.chidituke.workout_tracker.model.social.SocialPost;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.workout.WorkoutSession;
import com.chidituke.workout_tracker.repository.social.SocialPostRepository;
import com.chidituke.workout_tracker.service.notifications.NotificationsService;
import com.chidituke.workout_tracker.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WorkoutSharingService {

    private final SocialPostRepository socialPostRepository;
    private final UserService userService;
    private final NotificationsService notificationsService; // Assume this exists

    // ==================== AUTO-SHARING WORKFLOW ====================

    /**
     * Handle workout completion - check if user wants to share
     */
    public SharingPromptResult handleWorkoutCompletion(WorkoutSession session) {
        User user = session.getUser();

        log.info("Handling workout completion for user {} - workout: {}",
                user.getId(), session.getWorkoutPlan().getWorkoutName());

        // Check if user wants auto-sharing prompts
        if (!user.shouldPromptWorkoutSharing()) {
            log.debug("User {} has disabled workout sharing prompts", user.getId());
            return SharingPromptResult.disabled();
        }

        // Check if this workout was already shared
        if (hasWorkoutBeenShared(session)) {
            log.debug("Workout {} already shared by user {}", session.getId(), user.getId());
            return SharingPromptResult.alreadyShared();
        }

        // Check if workout is shareable (not too old)
        if (!session.canBeSharedToFeed()) {
            log.debug("Workout {} is too old to be shared", session.getId());
            return SharingPromptResult.tooOld();
        }

        // Generate default sharing content
        String defaultContent = generateWorkoutContent(session);

        return SharingPromptResult.builder()
                .shouldPrompt(true)
                .defaultContent(defaultContent)
                .session(session)
                .build();
    }

    /**
     * Create and save workout post when user chooses to share
     */
    public SocialPost shareWorkoutToFeed(WorkoutSession session, String userMessage,
                                         SocialPost.PrivacyLevel privacyOverride) {
        User user = session.getUser();

        log.info("Creating workout post for user {} - session {}", user.getId(), session.getId());

        // Determine privacy level
        SocialPost.PrivacyLevel privacy = privacyOverride != null ?
                privacyOverride :
                user.getDefaultPostPrivacy().toSocialPostPrivacy();

        // Generate content
        String autoContent = generateWorkoutContent(session);
        String finalContent = userMessage != null && !userMessage.trim().isEmpty() ?
                userMessage.trim() + "\n\n" + autoContent :
                autoContent;

        // Create post
        SocialPost post = SocialPost.builder()
                .author(user)
                .postType(SocialPost.PostType.WORKOUT_COMPLETION)
                .workoutSession(session)
                .content(finalContent)
                .privacyLevel(privacy)
                .workoutLocation(session.getLocation())
                .location(getLocationString(session))
                .build();

        // Extract hashtags (done automatically in @PrePersist)
        post = socialPostRepository.save(post);

        // Update user stats
        user.incrementPostsCount();
        userService.save(user);

        // Send notifications to followers if public post
        if (privacy == SocialPost.PrivacyLevel.PUBLIC) {
//            notificationsService.notifyFollowersOfNewPost(user, post);
        }

        log.info("Successfully created workout post {} for user {}", post.getId(), user.getId());
        return post;
    }

    /**
     * Handle "Don't ask me again" option
     */
    public void disableWorkoutSharingForUser(Long userId) {
        User user = userService.getUserById(userId);
        user.disableWorkoutSharingPrompts();
        userService.save(user);

        log.info("Disabled workout sharing prompts for user {}", userId);
    }

    /**
     * Manual sharing of old workout (from workout history)
     */
    public SocialPost shareOldWorkout(Long workoutSessionId, String userMessage,
                                      SocialPost.PrivacyLevel privacy) {
        WorkoutSession session = workoutSessionService.findById(workoutSessionId);
        User user = session.getUser();

        // Verify user owns this workout
        if (!session.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("User cannot share another user's workout");
        }

        // Check if already shared
        if (hasWorkoutBeenShared(session)) {
            throw new IllegalStateException("This workout has already been shared");
        }

        // Check age limit (30 days)
        if (!session.canBeSharedToFeed()) {
            throw new IllegalStateException("This workout is too old to share (30+ days)");
        }

        return shareWorkoutToFeed(session, userMessage, privacy);
    }

    // ==================== CONTENT GENERATION ====================

    /**
     * Generate automatic workout content for sharing
     */
    private String generateWorkoutContent(WorkoutSession session) {
        StringBuilder content = new StringBuilder();

        // Main workout completion message
        content.append("💪 Completed ").append(session.getWorkoutPlan().getWorkoutName());

        // Add date if not today
        if (!session.getDate().equals(LocalDate.now())) {
            content.append(" (").append(session.getDate().format(DateTimeFormatter.ofPattern("MMM d"))).append(")");
        }

        // Add workout stats if user wants them shown
        if (session.getUser().includeWorkoutStatsInPosts()) {
            content.append("\n");

            if (session.getTotalDurationMinutes() != null) {
                content.append("\n⏱️ Duration: ").append(session.getTotalDurationMinutes()).append(" minutes");
            }

            if (session.getEstimatedCalories() != null) {
                content.append("\n🔥 Calories: ").append(session.getEstimatedCalories());
            }

            if (session.getOverallEffort() != null) {
                content.append("\n💯 Effort: ").append(session.getOverallEffort()).append("/10");
            }

            if (session.getDifficultyRating() != null) {
                content.append("\n⭐ Difficulty: ").append(session.getDifficultyRating()).append("/10");
            }
        }

        // Add mood if present
        if (session.getMood() != null) {
            String moodEmoji = getMoodEmoji(session.getMood());
            content.append("\n").append(moodEmoji).append(" Feeling ").append(session.getMood().name().toLowerCase());
        }

        // Add motivational hashtags
        content.append(generateWorkoutHashtags(session));

        return content.toString();
    }

    /**
     * Generate relevant hashtags for workout post
     */
    private String generateWorkoutHashtags(WorkoutSession session) {
        List<String> hashtags = new ArrayList<>();

        // Basic fitness hashtags
        hashtags.add("#workout");
        hashtags.add("#fitness");

        // Workout type specific
        String workoutName = session.getWorkoutPlan().getWorkoutName().toLowerCase();
        if (workoutName.contains("strength") || workoutName.contains("weight")) {
            hashtags.add("#strength");
        }
        if (workoutName.contains("cardio") || workoutName.contains("running")) {
            hashtags.add("#cardio");
        }
        if (workoutName.contains("yoga")) {
            hashtags.add("#yoga");
        }

        // Location based
        if (session.getLocation() != null) {
            switch (session.getLocation()) {
                case GYM -> hashtags.add("#gymworkout");
                case HOME -> hashtags.add("#homeworkout");
                case PARK -> hashtags.add("#outdoorworkout");
                case BEACH -> hashtags.add("#beachworkout");
            }
        }

        // Progress tracking
        hashtags.add("#progress");
        hashtags.add("#consistency");

        // Limit to 5 hashtags
        return "\n\n" + hashtags.stream()
                .limit(5)
                .map(tag -> tag)
                .reduce("", (a, b) -> a + " " + b)
                .trim();
    }

    /**
     * Get emoji for workout mood
     */
    private String getMoodEmoji(WorkoutSession.WorkoutMood mood) {
        return switch (mood) {
            case ENERGETIC -> "⚡";
            case TIRED -> "😴";
            case MOTIVATED -> "🔥";
            case FOCUSED -> "🎯";
            case STRESSED -> "😤";
            case RELAXED -> "😌";
            case PUMPED -> "💪";
            case SLUGGISH -> "🐌";
        };
    }

    /**
     * Get location string for post
     */
    private String getLocationString(WorkoutSession session) {
        if (session.getLocation() == null) return null;

        return switch (session.getLocation()) {
            case HOME -> "Home Workout";
            case GYM -> "At the Gym";
            case PARK -> "Park Workout";
            case OFFICE -> "Office Workout";
            case HOTEL -> "Hotel Workout";
            case BEACH -> "Beach Workout";
            case TRAIL -> "Trail Workout";
            case STUDIO -> "Studio Workout";
            case OTHER -> "Workout Location";
        };
    }

    // ==================== HELPER METHODS ====================

    /**
     * Check if workout has already been shared to social feed
     */
    private boolean hasWorkoutBeenShared(WorkoutSession session) {
        return socialPostRepository.existsByWorkoutSessionAndPostType(
                session, SocialPost.PostType.WORKOUT_COMPLETION);
    }

    /**
     * Get sharing statistics for user
     */
    public SharingStats getUserSharingStats(Long userId) {
        User user = userService.getUserById(userId);

        long totalWorkouts = workoutSessionService.countByUserId(userId);
        long sharedWorkouts = socialPostRepository.countByAuthorAndPostType(
                user, SocialPost.PostType.WORKOUT_COMPLETION);

        double sharingRate = totalWorkouts > 0 ? (double) sharedWorkouts / totalWorkouts * 100 : 0;

        return SharingStats.builder()
                .totalWorkouts(totalWorkouts)
                .sharedWorkouts(sharedWorkouts)
                .sharingRate(sharingRate)
                .build();
    }

    // ==================== INNER CLASSES ====================

    @lombok.Data
    @lombok.Builder
    public static class SharingPromptResult {
        private boolean shouldPrompt;
        private String defaultContent;
        private WorkoutSession session;
        private String reason;

        public static SharingPromptResult disabled() {
            return SharingPromptResult.builder()
                    .shouldPrompt(false)
                    .reason("User has disabled workout sharing prompts")
                    .build();
        }

        public static SharingPromptResult alreadyShared() {
            return SharingPromptResult.builder()
                    .shouldPrompt(false)
                    .reason("Workout already shared")
                    .build();
        }

        public static SharingPromptResult tooOld() {
            return SharingPromptResult.builder()
                    .shouldPrompt(false)
                    .reason("Workout is too old to share")
                    .build();
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class SharingStats {
        private long totalWorkouts;
        private long sharedWorkouts;
        private double sharingRate;
    }

    // ==================== MISSING SERVICE DEPENDENCIES ====================
    // Note: These would be actual @Autowired services in your implementation

    private WorkoutSessionService workoutSessionService; // Assume this exists
}