package com.chidituke.workout_tracker.service.messaging;

import com.chidituke.workout_tracker.model.messaging.Conversation;
import com.chidituke.workout_tracker.model.messaging.ConversationParticipant;
import com.chidituke.workout_tracker.model.messaging.Message;
import com.chidituke.workout_tracker.model.messaging.enums.MessageType;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.workout.WorkoutPlan;
import com.chidituke.workout_tracker.model.workout.WorkoutSession;
import com.chidituke.workout_tracker.repository.messaging.MessageRepository;
import com.chidituke.workout_tracker.service.notifications.NotificationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service for managing messages within conversations
 * Handles message sending, content validation, media handling, and workout sharing
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationService conversationService;
    private final NotificationsService notificationsService;

    // Constants for content validation
    private static final int MAX_MESSAGE_LENGTH = 2000;
    private static final long MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB
    private static final long MAX_VIDEO_SIZE_BYTES = 25 * 1024 * 1024; // 25MB
    private static final long MAX_AUDIO_SIZE_BYTES = 5 * 1024 * 1024;  // 5MB
    private static final long MAX_FILE_SIZE_BYTES = 15 * 1024 * 1024;  // 15MB

    // Content filtering patterns
    private static final Pattern SPAM_PATTERN = Pattern.compile(
            "(?i).*(click here|buy now|free money|win now|urgent|limited time).*"
    );
    private static final Pattern INAPPROPRIATE_PATTERN = Pattern.compile(
            "(?i).*(explicit inappropriate content patterns would go here).*"
    );

    // ==================== CORE MESSAGE SENDING ====================

    /**
     * Send a text message in a conversation
     */
    public Message sendTextMessage(Long conversationId, User sender, String content) {
        log.debug("Sending text message from user {} to conversation {}", sender.getId(), conversationId);

        // Validate inputs
        validateMessageContent(content);
        Conversation conversation = validateAndGetConversation(conversationId, sender);

        // Create and save message
        Message message = Message.createTextMessage(conversation, sender, content.trim());

        // Apply content filtering
        applyContentFiltering(message);

        message = messageRepository.save(message);

        // Mark conversation as updated and send notifications
        handlePostMessageActions(conversation, message, sender);

        log.info("Text message {} sent by user {} to conversation {}",
                message.getId(), sender.getId(), conversationId);

        return message;
    }

    /**
     * Send a media message with file upload
     */
    public Message sendMediaMessage(Long conversationId, User sender, String content,
                                    MessageType messageType, MultipartFile file) {
        log.debug("Sending {} message from user {} to conversation {}",
                messageType, sender.getId(), conversationId);

        // Validate inputs
        validateMediaMessage(content, messageType, file);
        Conversation conversation = validateAndGetConversation(conversationId, sender);

        // Handle file upload (this would integrate with your file storage service)
        String mediaUrl = handleFileUpload(file, messageType);

        // Create and save message
        Message message = Message.createMediaMessage(
                conversation, sender, content != null ? content.trim() : "",
                messageType, mediaUrl, file.getSize()
        );

        // Apply content filtering
        applyContentFiltering(message);

        message = messageRepository.save(message);

        // Mark conversation as updated and send notifications
        handlePostMessageActions(conversation, message, sender);

        log.info("{} message {} sent by user {} to conversation {}",
                messageType, message.getId(), sender.getId(), conversationId);

        return message;
    }

    /**
     * Send a workout session sharing message
     */
    public Message sendWorkoutMessage(Long conversationId, User sender, String content,
                                      WorkoutSession workoutSession) {
        log.debug("Sending workout message from user {} to conversation {}", sender.getId(), conversationId);

        // Validate inputs
        if (workoutSession == null) {
            throw new IllegalArgumentException("Workout session is required for workout messages");
        }

        Conversation conversation = validateAndGetConversation(conversationId, sender);

        // Use workout name as default content if none provided
        String messageContent = content != null && !content.trim().isEmpty()
                ? content.trim()
                : "Shared a workout: " + getWorkoutDisplayName(workoutSession);

        // Create and save message
        Message message = Message.createWorkoutMessage(conversation, sender, messageContent, workoutSession);
        message = messageRepository.save(message);

        // Mark conversation as updated and send notifications
        handlePostMessageActions(conversation, message, sender);

        log.info("Workout message {} sent by user {} to conversation {}",
                message.getId(), sender.getId(), conversationId);

        return message;
    }

    /**
     * Send a workout plan sharing message
     */
    public Message sendWorkoutPlanMessage(Long conversationId, User sender, String content,
                                          WorkoutPlan workoutPlan) {
        log.debug("Sending workout plan message from user {} to conversation {}", sender.getId(), conversationId);

        // Validate inputs
        if (workoutPlan == null) {
            throw new IllegalArgumentException("Workout plan is required for workout plan messages");
        }

        Conversation conversation = validateAndGetConversation(conversationId, sender);

        // Use workout plan name as default content if none provided
        String messageContent = content != null && !content.trim().isEmpty()
                ? content.trim()
                : "Shared a workout plan: " + workoutPlan.getWorkoutName();

        // Create and save message
        Message message = Message.createWorkoutPlanMessage(conversation, sender, messageContent, workoutPlan);
        message = messageRepository.save(message);

        // Mark conversation as updated and send notifications
        handlePostMessageActions(conversation, message, sender);

        log.info("Workout plan message {} sent by user {} to conversation {}",
                message.getId(), sender.getId(), conversationId);

        return message;
    }

    // ==================== MESSAGE MANAGEMENT ====================

    /**
     * Edit a message (within time limit)
     */
    public Message editMessage(Long messageId, User editor, String newContent) {
        log.debug("Editing message {} by user {}", messageId, editor.getId());

        // Validate inputs
        validateMessageContent(newContent);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        // Validate edit permissions
        if (!message.canEdit(editor)) {
            throw new IllegalStateException("User cannot edit this message or edit time has expired");
        }

        // Edit content
        message.editContent(newContent.trim(), editor);

        // Reapply content filtering
        applyContentFiltering(message);

        message = messageRepository.save(message);

        log.info("Message {} edited by user {}", messageId, editor.getId());

        return message;
    }

    /**
     * Delete a message
     */
    public void deleteMessage(Long messageId, User user) {
        log.debug("Deleting message {} by user {}", messageId, user.getId());

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        // Validate delete permissions
        if (!message.canDelete(user)) {
            throw new IllegalStateException("User cannot delete this message");
        }

        messageRepository.delete(message);

        log.info("Message {} deleted by user {}", messageId, user.getId());
    }

    // ==================== MESSAGE RETRIEVAL ====================

    /**
     * Get messages in conversation with pagination
     */
    @Transactional(readOnly = true)
    public Page<Message> getConversationMessages(Long conversationId, User user, Pageable pageable) {
        log.debug("Fetching messages for conversation {} by user {}", conversationId, user.getId());

        // Validate user can access conversation
        Conversation conversation = validateAndGetConversation(conversationId, user);

        // Mark conversation as read
        conversationService.markConversationAsRead(conversationId, user);

        return messageRepository.findByConversation(conversation, pageable);
    }

    /**
     * Get recent messages for conversation preview
     */
    @Transactional(readOnly = true)
    public List<Message> getRecentMessages(Long conversationId, User user, int limit) {
        log.debug("Fetching {} recent messages for conversation {} by user {}",
                limit, conversationId, user.getId());

        Conversation conversation = validateAndGetConversation(conversationId, user);

        Pageable pageable = PageRequest.of(0, limit);
        return messageRepository.findRecentMessages(conversation, pageable);
    }

    /**
     * Get the latest message in conversation
     */
    @Transactional(readOnly = true)
    public Optional<Message> getLatestMessage(Long conversationId, User user) {
        log.debug("Fetching latest message for conversation {} by user {}", conversationId, user.getId());

        Conversation conversation = validateAndGetConversation(conversationId, user);

        List<Message> messages = messageRepository.findLatestMessage(conversation, PageRequest.of(0, 1));
        return messages.isEmpty() ? Optional.empty() : Optional.of(messages.get(0));
    }

    /**
     * Search messages in conversation
     */
    @Transactional(readOnly = true)
    public Page<Message> searchMessagesInConversation(Long conversationId, User user,
                                                      String searchTerm, Pageable pageable) {
        log.debug("Searching messages in conversation {} for term '{}' by user {}",
                conversationId, searchTerm, user.getId());

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term is required");
        }

        Conversation conversation = validateAndGetConversation(conversationId, user);

        return messageRepository.searchInConversation(conversation, searchTerm.trim(), pageable);
    }

    /**
     * Search messages across all user's conversations
     */
    @Transactional(readOnly = true)
    public Page<Message> searchMessagesGlobally(User user, String searchTerm, Pageable pageable) {
        log.debug("Searching messages globally for user {} with term '{}'", user.getId(), searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term is required");
        }

        return messageRepository.searchAcrossUserConversations(user, searchTerm.trim(), pageable);
    }

    // ==================== MEDIA AND CONTENT QUERIES ====================

    /**
     * Get media messages in conversation
     */
    @Transactional(readOnly = true)
    public Page<Message> getMediaMessages(Long conversationId, User user, Pageable pageable) {
        log.debug("Fetching media messages for conversation {} by user {}", conversationId, user.getId());

        Conversation conversation = validateAndGetConversation(conversationId, user);

        return messageRepository.findMediaMessages(conversation, pageable);
    }

    /**
     * Get workout messages in conversation
     */
    @Transactional(readOnly = true)
    public List<Message> getWorkoutMessages(Long conversationId, User user) {
        log.debug("Fetching workout messages for conversation {} by user {}", conversationId, user.getId());

        Conversation conversation = validateAndGetConversation(conversationId, user);

        return messageRepository.findWorkoutMessages(conversation);
    }

    /**
     * Get workout plan messages in conversation
     */
    @Transactional(readOnly = true)
    public List<Message> getWorkoutPlanMessages(Long conversationId, User user) {
        log.debug("Fetching workout plan messages for conversation {} by user {}", conversationId, user.getId());

        Conversation conversation = validateAndGetConversation(conversationId, user);

        return messageRepository.findWorkoutPlanMessages(conversation);
    }

    // ==================== UNREAD MESSAGE MANAGEMENT ====================

    /**
     * Get unread message count for conversation
     */
    @Transactional(readOnly = true)
    public long getUnreadMessageCount(Long conversationId, User user) {
        log.debug("Getting unread count for conversation {} by user {}", conversationId, user.getId());

        Conversation conversation = validateAndGetConversation(conversationId, user);

        return messageRepository.countUnreadMessages(conversation, user);
    }

    /**
     * Get total unread message count across all conversations
     */
    @Transactional(readOnly = true)
    public long getTotalUnreadMessageCount(User user) {
        log.debug("Getting total unread count for user {}", user.getId());

        return messageRepository.countTotalUnreadMessages(user);
    }

    /**
     * Get unread messages for conversation
     */
    @Transactional(readOnly = true)
    public List<Message> getUnreadMessages(Long conversationId, User user) {
        log.debug("Getting unread messages for conversation {} by user {}", conversationId, user.getId());

        Conversation conversation = validateAndGetConversation(conversationId, user);

        return messageRepository.findUnreadMessages(conversation, user);
    }

    // ==================== CONTENT MODERATION ====================

    /**
     * Filter a message manually
     */
    public void filterMessage(Long messageId, String reason, User moderator) {
        log.debug("Filtering message {} with reason '{}' by moderator {}",
                messageId, reason, moderator.getId());

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        // Validate moderator permissions (you'd implement this based on your user roles)
        if (!canModerateMessage(message, moderator)) {
            throw new IllegalStateException("User does not have moderation permissions");
        }

        message.filter(reason);
        messageRepository.save(message);

        log.info("Message {} filtered by moderator {} with reason: {}",
                messageId, moderator.getId(), reason);
    }

    /**
     * Unfilter a message
     */
    public void unfilterMessage(Long messageId, User moderator) {
        log.debug("Unfiltering message {} by moderator {}", messageId, moderator.getId());

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        // Validate moderator permissions
        if (!canModerateMessage(message, moderator)) {
            throw new IllegalStateException("User does not have moderation permissions");
        }

        message.unfilter();
        messageRepository.save(message);

        log.info("Message {} unfiltered by moderator {}", messageId, moderator.getId());
    }

    // ==================== PROFESSIONAL FEATURES ====================

    /**
     * Send a workout assignment message (professional feature)
     */
    public Message sendWorkoutAssignment(Long conversationId, User trainer, User client,
                                         WorkoutPlan workoutPlan, String instructions) {
        log.debug("Sending workout assignment from trainer {} to client {} in conversation {}",
                trainer.getId(), client.getId(), conversationId);

        // Validate trainer is professional
        if (!isProfessional(trainer)) {
            throw new IllegalStateException("Only professionals can send workout assignments");
        }

        // Validate conversation includes both trainer and client
        Conversation conversation = validateAndGetConversation(conversationId, trainer);
        if (!conversationService.isParticipant(conversation, client)) {
            throw new IllegalStateException("Client is not a participant in this conversation");
        }

        // Create assignment message
        String content = String.format("📋 Workout Assignment: %s%s",
                workoutPlan.getWorkoutName(),
                instructions != null && !instructions.trim().isEmpty()
                        ? "\n\nInstructions: " + instructions.trim() : "");

        Message message = Message.createWorkoutPlanMessage(conversation, trainer, content, workoutPlan);
        message = messageRepository.save(message);

        // Send special notification for workout assignments
        notificationsService.notifyWorkoutAssignment(trainer, client, workoutPlan);

        // Mark conversation as updated and send notifications
        handlePostMessageActions(conversation, message, trainer);

        log.info("Workout assignment {} sent from trainer {} to client {}",
                message.getId(), trainer.getId(), client.getId());

        return message;
    }

    /**
     * Request progress check-in (professional feature)
     */
    public Message requestProgressCheckIn(Long conversationId, User trainer, User client, String message) {
        log.debug("Requesting progress check-in from trainer {} to client {} in conversation {}",
                trainer.getId(), client.getId(), conversationId);

        // Validate trainer is professional
        if (!isProfessional(trainer)) {
            throw new IllegalStateException("Only professionals can request progress check-ins");
        }

        // Send check-in request message
        String content = "📊 Progress Check-In Request\n\n" +
                (message != null && !message.trim().isEmpty() ? message.trim() :
                        "How's your progress going? Please share an update on your recent workouts and how you're feeling!");

        return sendTextMessage(conversationId, trainer, content);
    }

    // ==================== VALIDATION HELPERS ====================

    private void validateMessageContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }

        if (content.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("Message content cannot exceed %d characters", MAX_MESSAGE_LENGTH));
        }
    }

    private void validateMediaMessage(String content, MessageType messageType, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required for media messages");
        }

        if (!messageType.isMediaType()) {
            throw new IllegalArgumentException("Invalid message type for media message: " + messageType);
        }

        // Validate file size based on type
        long maxSize = getMaxFileSizeForType(messageType);
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException(
                    String.format("File size exceeds maximum allowed for %s (%.1f MB)",
                            messageType, maxSize / (1024.0 * 1024.0)));
        }

        // Validate content if provided
        if (content != null && content.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("Message content cannot exceed %d characters", MAX_MESSAGE_LENGTH));
        }
    }

    private Conversation validateAndGetConversation(Long conversationId, User user) {
        return conversationService.getConversation(conversationId, user)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found or access denied"));
    }

    private long getMaxFileSizeForType(MessageType messageType) {
        return switch (messageType) {
            case IMAGE -> MAX_IMAGE_SIZE_BYTES;
            case VIDEO -> MAX_VIDEO_SIZE_BYTES;
            case AUDIO -> MAX_AUDIO_SIZE_BYTES;
            case FILE -> MAX_FILE_SIZE_BYTES;
            default -> MAX_FILE_SIZE_BYTES;
        };
    }

    // ==================== CONTENT FILTERING ====================

    private void applyContentFiltering(Message message) {
        String content = message.getContent();
        if (content == null) return;

        // Check for spam patterns
        if (SPAM_PATTERN.matcher(content).matches()) {
            message.filter("Potential spam content detected");
            log.warn("Message {} filtered for spam content", message.getId());
            return;
        }

        // Check for inappropriate content
        if (INAPPROPRIATE_PATTERN.matcher(content).matches()) {
            message.filter("Inappropriate content detected");
            log.warn("Message {} filtered for inappropriate content", message.getId());
            return;
        }

        // Additional filtering logic can be added here
        // - Profanity detection
        // - Harassment detection
        // - External link validation
    }

    // ==================== HELPER METHODS ====================

    private void handlePostMessageActions(Conversation conversation, Message message, User sender) {
        // Mark conversation as updated
        conversation.markAsUpdated();

        // Send notifications to other participants
        sendMessageNotifications(conversation, message, sender);
    }

    private void sendMessageNotifications(Conversation conversation, Message message, User sender) {
        // Get participants who should receive notifications
        List<ConversationParticipant> notificationTargets = conversation.getActiveParticipants()
                .stream()
                .filter(p -> !p.getUser().equals(sender)) // Exclude sender
                .filter(ConversationParticipant::hasNotificationsEnabled) // Check notification settings
                .toList();

        for (ConversationParticipant participant : notificationTargets) {
            //      notificationsService.notifyNewMessage(sender, participant.getUser(), conversation, message);
        }
    }

    private String handleFileUpload(MultipartFile file, MessageType messageType) {
        // This would integrate with your file storage service (AWS S3, etc.)
        // For now, return a placeholder URL
        // In a real implementation, you'd:
        // 1. Generate unique filename
        // 2. Upload to cloud storage
        // 3. Return the public URL

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        return "/api/files/" + messageType.name().toLowerCase() + "/" + fileName;
    }

    private String getWorkoutDisplayName(WorkoutSession workoutSession) {
        if (workoutSession.getWorkoutPlan() != null) {
            return workoutSession.getWorkoutPlan().getWorkoutName();
        }
        return "Workout Session";
    }

    private boolean isProfessional(User user) {
        return user.getUserType() != null && user.getUserType().name().equals("PRO_PROFESSIONAL");
    }

    private boolean canModerateMessage(Message message, User moderator) {
        // Check if user has moderation permissions
        // This would be based on your user role system
        Conversation conversation = message.getConversation();
        return conversationService.getActiveParticipant(conversation, moderator)
                .map(ConversationParticipant::canModerateMessages)
                .orElse(false);
    }
}