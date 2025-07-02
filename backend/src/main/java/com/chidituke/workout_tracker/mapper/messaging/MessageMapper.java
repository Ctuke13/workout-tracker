package com.chidituke.workout_tracker.mapper.messaging;

import com.chidituke.workout_tracker.dto.response.messaging.*;
import com.chidituke.workout_tracker.model.messaging.Message;
import com.chidituke.workout_tracker.model.user.User;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public MessageResponse toResponse(Message message, User currentUser) {
        if (message == null) {
            return null;
        }

        return MessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .mediaUrl(message.getMediaUrl())
                .mediaSizeBytes(message.getMediaSizeBytes())
                .formattedFileSize(message.getFormattedFileSize())
                .isFiltered(message.isFiltered())
                .filterReason(message.getFilterReason())
                .sender(toUserSummary(message.getSender()))
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .contentPreview(message.getContentPreview(100))
                .timeAgo(message.getTimeAgo())
                .canEdit(message.canEdit(currentUser))
                .canDelete(message.canDelete(currentUser))
                .isRecent(message.isRecent())
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