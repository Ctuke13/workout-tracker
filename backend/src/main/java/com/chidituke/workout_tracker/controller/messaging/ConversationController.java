package com.chidituke.workout_tracker.controller.messaging;

import com.chidituke.workout_tracker.dto.response.common.ApiResponse;
import com.chidituke.workout_tracker.dto.response.common.PageResponse;
import com.chidituke.workout_tracker.dto.request.messaging.*;
import com.chidituke.workout_tracker.dto.response.messaging.*;
import com.chidituke.workout_tracker.mapper.messaging.ConversationMapper;
import com.chidituke.workout_tracker.model.messaging.Conversation;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.service.messaging.ConversationService;
import com.chidituke.workout_tracker.service.messaging.MessageService;
import com.chidituke.workout_tracker.service.user.UserService;
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
 * REST Controller for conversation operations
 * Handles conversation creation, management, and user settings
 */
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Conversations", description = "Conversation management API")
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService messageService;
    private final UserService userService;
    private final ConversationMapper conversationMapper;

    @Operation(summary = "Create or find direct conversation", description = "Create a new direct conversation or return existing one")
    @PostMapping("/direct")
    public ResponseEntity<ApiResponse<ConversationResponse>> createDirectConversation(
            @Valid @RequestBody CreateConversationRequest request,
            @AuthenticationPrincipal User currentUser) {

        log.debug("Creating direct conversation between users {} and {}",
                currentUser.getId(), request.getOtherUserId());

        try {
            User otherUser = userService.getUserById(request.getOtherUserId());

            boolean isNewConversation = !conversationService.directConversationExists(currentUser, otherUser);

            Conversation conversation = conversationService.createOrFindDirectConversation(currentUser, otherUser);

            if (isNewConversation && request.getInitialMessage() != null && !request.getInitialMessage().trim().isEmpty()) {
                messageService.sendTextMessage(conversation.getId(), currentUser, request.getInitialMessage());
            }

            ConversationResponse response = conversationMapper.toResponse(conversation, currentUser);

            HttpStatus status = isNewConversation ? HttpStatus.CREATED : HttpStatus.OK;
            String message = isNewConversation ? "Conversation created successfully" : "Conversation already exists";

            return ResponseEntity.status(status)
                    .body(ApiResponse.success(response, message));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(summary = "Get user conversations", description = "Get paginated list of user's conversations")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ConversationListResponse>>> getConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {

        log.debug("Getting conversations for user {} (page: {}, size: {})",
                currentUser.getId(), page, size);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
            Page<Conversation> conversations = conversationService.getConversationsForUser(currentUser, pageable);

            Page<ConversationListResponse> conversationResponses = conversations
                    .map(conv -> conversationMapper.toListResponse(conv, currentUser));

            PageResponse<ConversationListResponse> response = PageResponse.from(conversationResponses);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("Error fetching conversations for user {}", currentUser.getId(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to fetch conversations"));
        }
    }

    // Add more methods as needed
}