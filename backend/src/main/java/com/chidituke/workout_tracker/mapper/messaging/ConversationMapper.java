package com.chidituke.workout_tracker.mapper.messaging;

import com.chidituke.workout_tracker.dto.response.messaging.*;
import com.chidituke.workout_tracker.model.messaging.Conversation;
import com.chidituke.workout_tracker.model.messaging.ConversationParticipant;
import com.chidituke.workout_tracker.model.messaging.Message;
import com.chidituke.workout_tracker.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConversationMapper {

    private final MessageMapper messageMapper;

    public ConversationResponse toResponse(Conversation conversation, User currentUser) {
        if (conversation == null) {
            return null;
        }

        ConversationParticipant userParticipant = conversation.getActiveParticipant(currentUser);

        return ConversationResponse.builder()
                .id(conversation.getId())
                .type(conversation.getType())
                .name(conversation.getName())
                .displayName(conversation.getDisplayName(currentUser))
                .createdBy(toUserSummary(conversation.getCreatedBy()))
                .participants(conversation.getActiveParticipants().stream()
                        .map(this::toParticipantResponse)
                        .toList())
                .activeParticipantCount(conversation.getActiveParticipantCount())
                .lastMessage(messageMapper.toResponse(conversation.getLastMessage(), currentUser))
                .isStarred(userParticipant != null && userParticipant.isStarred())
                .isMuted(userParticipant != null && userParticipant.isMuted())
                .unreadCount(conversation.getUnreadCount(currentUser))
                .hasNotificationsEnabled(userParticipant != null && userParticipant.hasNotificationsEnabled())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .isValid(conversation.isValid())
                .build();
    }

    public ConversationListResponse toListResponse(Conversation conversation, User currentUser) {
        if (conversation == null) {
            return null;
        }

        ConversationParticipant userParticipant = conversation.getActiveParticipant(currentUser);
        Message lastMessage = conversation.getLastMessage();

        List<UserSummaryResponse> otherParticipants = conversation.getActiveParticipants().stream()
                .filter(p -> !p.getUser().equals(currentUser))
                .map(p -> toUserSummary(p.getUser()))
                .toList();

        return ConversationListResponse.builder()
                .id(conversation.getId())
                .type(conversation.getType())
                .displayName(conversation.getDisplayName(currentUser))
                .lastMessagePreview(lastMessage != null ? lastMessage.getContentPreview(50) : null)
                .lastMessageTime(lastMessage != null ? lastMessage.getCreatedAt() : conversation.getUpdatedAt())
                .unreadCount(conversation.getUnreadCount(currentUser))
                .isStarred(userParticipant != null && userParticipant.isStarred())
                .isMuted(userParticipant != null && userParticipant.isMuted())
                .otherParticipants(otherParticipants)
                .build();
    }

    public ConversationParticipantResponse toParticipantResponse(ConversationParticipant participant) {
        if (participant == null) {
            return null;
        }

        return ConversationParticipantResponse.builder()
                .id(participant.getId())
                .user(toUserSummary(participant.getUser()))
                .role(participant.getRole().name())
                .isStarred(participant.isStarred())
                .joinedAt(participant.getJoinedAt())
                .leftAt(participant.getLeftAt())
                .lastSeenAt(participant.getLastSeenAt())
                .isActive(participant.isActive())
                .hasNotificationsEnabled(participant.hasNotificationsEnabled())
                .isMuted(participant.isMuted())
                .canSendMessages(participant.canSendMessages())
                .canAddParticipants(participant.canAddParticipants())
                .canRemoveParticipants(participant.canRemoveParticipants())
                .canModerateMessages(participant.canModerateMessages())
                .build();
    }

    private UserSummaryResponse toUserSummary(User user) {
        if (user == null) {
            return null;
        }

        return UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .profilePictureUrl(user.getProfileImageUrl())
                .userType(user.getUserType() != null ? user.getUserType().name() : null)
                .isProfessional(isProfessional(user))
                .build();
    }

    private boolean isProfessional(User user) {
        return user.getUserType() != null && user.getUserType().name().equals("PRO_PROFESSIONAL");
    }
}