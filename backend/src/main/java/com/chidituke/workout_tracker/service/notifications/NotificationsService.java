package com.chidituke.workout_tracker.service.notifications;

import com.chidituke.workout_tracker.model.notifications.DeviceToken;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.workout.WorkoutPlan;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Sends push notifications via Firebase Cloud Messaging (FCM).
 * <p>
 * Notification categories map to the 7 boolean preference columns on User:
 * - notifPetHealth          → pet fuel/fatigue/cleanliness/motivation alerts
 * - notifStreakReminders     → streak at risk and workout reminders
 * - notifAchievements        → achievement unlocks, level-ups, personal records
 * - notifRankSeason          → rank changes, season start/end
 * - notifWeeklySummary       → weekly recap digest
 * - notifSocialLeaderboard   → leaderboard position changes
 * - notifReengagement        → lapsed user nudges
 * <p>
 * React Native migration note:
 * This service is entirely backend — no changes needed when moving to React Native.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationsService {

    private final DeviceTokenService deviceTokenService;

    // ── Notification category enum ─────────────────────────────────────────────

    public enum NotificationCategory {
        PET_HEALTH,
        STREAK_REMINDERS,
        ACHIEVEMENTS,
        RANK_SEASON,
        WEEKLY_SUMMARY,
        SOCIAL_LEADERBOARD,
        REENGAGEMENT
    }

    // ── Core send methods ──────────────────────────────────────────────────────

    /**
     * Send a notification to a user if they have that category enabled.
     * Automatically deactivates any invalid/expired tokens reported by FCM.
     *
     * @param user     The recipient user (used to check preferences)
     * @param category The notification category (checked against user prefs)
     * @param title    Notification title
     * @param body     Notification body text
     */
    public void sendToUser(User user, NotificationCategory category,
                           String title, String body) {
        if (!isEnabled(user, category)) {
            log.debug("🔕 Notification suppressed for user {} — category {} is disabled",
                    user.getId(), category);
            return;
        }

        List<DeviceToken> tokens = deviceTokenService.getActiveTokens(user.getId());
        if (tokens.isEmpty()) {
            log.debug("📵 No active device tokens for user {}", user.getId());
            return;
        }

        for (DeviceToken deviceToken : tokens) {
            sendToToken(deviceToken.getToken(), title, body);
        }
    }

    /**
     * Send a notification to a specific FCM token.
     * Deactivates the token if FCM reports it as invalid.
     */
    private void sendToToken(String token, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setWebpushConfig(WebpushConfig.builder()
                            .setNotification(WebpushNotification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .setIcon("/logo192.png")
                                    .build())
                            .build())
                    .build();

            String messageId = FirebaseMessaging.getInstance().send(message);
            log.debug("✅ Notification sent — messageId: {}", messageId);

        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
                    || e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                // Token is no longer valid — deactivate it
                log.warn("🔕 FCM token is invalid/unregistered — deactivating: {}...",
                        token.substring(0, Math.min(20, token.length())));
                deviceTokenService.deactivateToken(token);
            } else {
                log.error("❌ FCM send failed for token {}...: {}",
                        token.substring(0, Math.min(20, token.length())), e.getMessage());
            }
        }
    }

    // ── Preference check ───────────────────────────────────────────────────────

    /**
     * Check if a notification category is enabled for a user.
     * Defaults to true if the preference field is null (safety fallback).
     */
    private boolean isEnabled(User user, NotificationCategory category) {
        return switch (category) {
            case PET_HEALTH -> Boolean.TRUE.equals(user.getNotifPetHealth());
            case STREAK_REMINDERS -> Boolean.TRUE.equals(user.getNotifStreakReminders());
            case ACHIEVEMENTS -> Boolean.TRUE.equals(user.getNotifAchievements());
            case RANK_SEASON -> Boolean.TRUE.equals(user.getNotifRankSeason());
            case WEEKLY_SUMMARY -> Boolean.TRUE.equals(user.getNotifWeeklySummary());
            case SOCIAL_LEADERBOARD -> Boolean.TRUE.equals(user.getNotifSocialLeaderboard());
            case REENGAGEMENT -> Boolean.TRUE.equals(user.getNotifReengagement());
        };
    }

    // ── Convenience methods per category ──────────────────────────────────────
    // These are what other services call — clean, readable, no magic strings.

    // ── Pet Health ────────────────────────────────────────────────────────────

    public void sendFuelLow(User user) {
        sendToUser(user, NotificationCategory.PET_HEALTH,
                "Your pet is getting hungry 🍖",
                "Fuel is running low — feed your pet before the next workout!");
    }

    public void sendFuelCritical(User user) {
        sendToUser(user, NotificationCategory.PET_HEALTH,
                "Your pet is starving! 😰",
                "Fuel is critically low — your pet needs food now!");
    }

    public void sendMotivationLow(User user) {
        sendToUser(user, NotificationCategory.PET_HEALTH,
                "Your pet is feeling unmotivated 😔",
                "Give your pet a motivational boost to keep them going!");
    }

    public void sendMotivationCritical(User user) {
        sendToUser(user, NotificationCategory.PET_HEALTH,
                "Your pet has lost all motivation 💔",
                "Your pet desperately needs a boost — head to the pet page now!");
    }

    public void sendFatigueCritical(User user) {
        sendToUser(user, NotificationCategory.PET_HEALTH,
                "Your pet is exhausted 😴",
                "Your pet is overworked and needs rest — let them sleep it off.");
    }

    public void sendCleanlinesLow(User user) {
        sendToUser(user, NotificationCategory.PET_HEALTH,
                "Your pet needs a bath 🛁",
                "Your pet is getting dirty — give them a quick clean!");
    }

    public void sendCleanlinesCritical(User user) {
        sendToUser(user, NotificationCategory.PET_HEALTH,
                "Your pet is filthy and unhappy 🤢",
                "Cleanliness is critical — bathe your pet right away!");
    }

    public void sendPetFellAsleep(User user) {
        sendToUser(user, NotificationCategory.PET_HEALTH,
                "Your pet just fell asleep 😴",
                "They worked hard today — let them rest and they'll be ready tomorrow!");
    }

    public void sendPetWokeUp(User user) {
        sendToUser(user, NotificationCategory.PET_HEALTH,
                "Your pet is awake and ready! 🐺",
                "Your pet has rested up — time to get back to training!");
    }

    // ── Neglect ────────────────────────────────────────────────────────────────

    public void sendPetNeglected(User user, int daysMissed) {
        sendToUser(user, NotificationCategory.REENGAGEMENT,
                "Your pet misses you 🐾",
                String.format("It's been %d day%s — your pet needs your attention!",
                        daysMissed, daysMissed == 1 ? "" : "s"));
    }

    // ── Streaks ────────────────────────────────────────────────────────────────

    public void sendStreakAtRisk(User user, int currentStreak) {
        sendToUser(user, NotificationCategory.STREAK_REMINDERS,
                "Don't break your streak! 🔥",
                String.format("You're on a %d-day streak — log a workout before midnight to keep it going!",
                        currentStreak));
    }

    public void sendStreakBroken(User user) {
        sendToUser(user, NotificationCategory.STREAK_REMINDERS,
                "Your streak broke 💔",
                "Don't give up — start a new streak today and get back on track!");
    }

    public void sendStreakMilestone(User user, int streakDays) {
        sendToUser(user, NotificationCategory.STREAK_REMINDERS,
                String.format("🔥 %d-day streak!", streakDays),
                "You're on fire — keep it going!");
    }

    // ── Achievements ──────────────────────────────────────────────────────────

    public void sendAchievementUnlocked(User user, String achievementName) {
        sendToUser(user, NotificationCategory.ACHIEVEMENTS,
                "🏆 Achievement Unlocked!",
                String.format("You just earned: %s", achievementName));
    }

    public void sendLevelUp(User user, int newLevel) {
        sendToUser(user, NotificationCategory.ACHIEVEMENTS,
                String.format("Your pet reached Level %d! 🎉", newLevel),
                "Keep training — the next evolution is getting closer!");
    }

    public void sendPetEvolved(User user, String newStage) {
        sendToUser(user, NotificationCategory.ACHIEVEMENTS,
                "Your pet evolved! ✨",
                String.format("Your pet is now a %s — amazing work!", newStage));
    }

    public void sendPersonalRecord(User user, String exerciseName) {
        sendToUser(user, NotificationCategory.ACHIEVEMENTS,
                "🏅 New Personal Record!",
                String.format("You just set a new PR on %s — beast mode!", exerciseName));
    }

    // ── Rank & Season ─────────────────────────────────────────────────────────

    public void sendRankUp(User user, String newRank) {
        sendToUser(user, NotificationCategory.RANK_SEASON,
                "You ranked up! 🏅",
                String.format("You're now %s — keep climbing!", newRank));
    }

    public void sendSeasonEnding(User user, int daysLeft) {
        sendToUser(user, NotificationCategory.RANK_SEASON,
                String.format("Season ends in %d day%s! ⏳", daysLeft, daysLeft == 1 ? "" : "s"),
                "Make your final push before rankings reset!");
    }

    public void sendSeasonOver(User user, String finalRank) {
        sendToUser(user, NotificationCategory.RANK_SEASON,
                "Season over! 🏆",
                String.format("Your final rank: %s — new season starts now!", finalRank));
    }

    // ── Weekly Summary ────────────────────────────────────────────────────────

    public void sendWeeklySummary(User user, int workouts, int xpEarned) {
        sendToUser(user, NotificationCategory.WEEKLY_SUMMARY,
                "Your weekly recap is ready 📊",
                String.format("%d workout%s completed, %d XP earned — great week!",
                        workouts, workouts == 1 ? "" : "s", xpEarned));
    }

    public void sendWeeklyGoalMissed(User user, int completed, int goal) {
        sendToUser(user, NotificationCategory.WEEKLY_SUMMARY,
                "Weekly goal update 📋",
                String.format("You completed %d of %d workouts this week — finish strong!", completed, goal));
    }

    // ── Social / Leaderboard ──────────────────────────────────────────────────

    public void sendPassedOnLeaderboard(User user, String passerName) {
        sendToUser(user, NotificationCategory.SOCIAL_LEADERBOARD,
                "You've been passed on the leaderboard! 👀",
                String.format("%s just overtook you — time to respond!", passerName));
    }

    public void sendCrackedTopTen(User user) {
        sendToUser(user, NotificationCategory.SOCIAL_LEADERBOARD,
                "You cracked the top 10! 🔥",
                "You're in elite territory — keep pushing to stay there!");
    }

    // ── Messaging ─────────────────────────────────────────────────────────────

    public void notifyWorkoutAssignment(User trainer, User client, WorkoutPlan workoutPlan) {
        sendToUser(client, NotificationCategory.SOCIAL_LEADERBOARD,
                "New workout assigned! 📋",
                String.format("%s assigned you: %s",
                        trainer.getFirstName(), workoutPlan.getWorkoutName()));
    }

    // ── Messaging (TODO: implement when messaging system is built) ────────────
    // notifyNewMessage(User sender, User recipient, Conversation conversation, Message message)

    // ── Social / Relationships ────────────────────────────────────────────────

    public void notifyNewFollower(User follower, User following) {
        sendToUser(following, NotificationCategory.SOCIAL_LEADERBOARD,
                String.format("%s started following you! 👀", follower.getFirstName()),
                "Check out their profile and follow back!");
    }

    public void notifyFriendRequest(User requester, User target) {
        sendToUser(target, NotificationCategory.SOCIAL_LEADERBOARD,
                String.format("%s sent you a friend request 🤝", requester.getFirstName()),
                "Tap to accept or decline their request.");
    }

    public void notifyFriendRequestAccepted(User accepter, User requester) {
        sendToUser(requester, NotificationCategory.SOCIAL_LEADERBOARD,
                String.format("%s accepted your friend request! 🎉", accepter.getFirstName()),
                "You're now friends — check out their progress!");
    }
}