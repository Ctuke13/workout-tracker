package com.chidituke.workout_tracker.controller.messaging;

import com.chidituke.workout_tracker.dto.response.common.ApiResponse;
import com.chidituke.workout_tracker.dto.response.common.PageResponse;
import com.chidituke.workout_tracker.dto.request.messaging.*;
import com.chidituke.workout_tracker.dto.response.messaging.*;
import com.chidituke.workout_tracker.mapper.messaging.MessageMapper;
import com.chidituke.workout_tracker.model.messaging.Message;
import com.chidituke.workout_tracker.model.user.User;

import com.chidituke.workout_tracker.service.messaging.MessageService;
import com.chidituke.workout_tracker.service.user.UserService;
import com.chidituke.workout_tracker.service.workout.WorkoutPlanService;
import com.chidituke.workout_tracker.service.workout.WorkoutSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


/**
 * REST Controller for message operations
 * Handles sending, editing, deleting, and retrieving messages
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Messages", description = "Message operations API")
public class MessagingController {

    private final MessageService messageService;
    private final UserService userService;
    private final WorkoutSessionService workoutSessionService;
    private final WorkoutPlanService workoutPlanService;
    private final MessageMapper messageMapper;

    // ==================== SEND MESSAGES ====================

    @Operation(summary = "Send a text message", description = "Send a text message to a conversation")
    @PostMapping("/conversations/{conversationId}/text")
    public ResponseEntity<ApiResponse<MessageResponse>> sendTextMessage(
            @Parameter(description = "Conversation ID") @PathVariable Long conversationId,
            @Valid @RequestBody SendTextMessageRequest request,
            @AuthenticationPrincipal User currentUser) {

        log.debug("Sending text message to conversation {} by user {}", conversationId, currentUser.getId());

        try {
            Message message = messageService.sendTextMessage(conversationId, currentUser, request.getContent());
            MessageResponse response = messageMapper.toResponse(message, currentUser);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Message sent successfully"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "Get conversation messages", description = "Get paginated messages for a conversation")
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ApiResponse<PageResponse<MessageResponse>>> getConversationMessages(
            @Parameter(description = "Conversation ID") @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {

        log.debug("Getting messages for conversation {} by user {}", conversationId, currentUser.getId());

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Message> messages = messageService.getConversationMessages(conversationId, currentUser, pageable);

            Page<MessageResponse> messageResponses = messages.map(msg -> messageMapper.toResponse(msg, currentUser));
            PageResponse<MessageResponse> response = PageResponse.from(messageResponses);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Add more methods as needed - starting with basic ones
}