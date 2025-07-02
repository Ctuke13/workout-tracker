package com.chidituke.workout_tracker.dto.response.messaging;

import lombok.Builder;
import lombok.Data;

/**
 * Response DTO for user summary (to avoid circular references)
 */
@Data
@Builder
public class UserSummaryResponse {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;
    private String profilePictureUrl;
    private String userType;
    private boolean isProfessional;
}
