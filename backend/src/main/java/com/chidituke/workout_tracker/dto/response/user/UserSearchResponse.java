package com.chidituke.workout_tracker.dto.response.user;

import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.user.ProfessionalProfile;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserSearchResponse {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String profileImageUrl;
    private User.FitnessLevel fitnessLevel;
    private String zipcode;
    private String city;
    private String state;

    private User.ActivityLevel activityLevel;
    private Boolean currentlyActive;
    private LocalDateTime lastActive;

    // Professional information (if applicable)
    private Boolean isProfessional;
    private String professionalDisplayName;
    private ProfessionalProfile.ServiceType professionalServiceType;
    private Boolean professionalVerified;
    private Boolean professionalAcceptingClients;
    private Double professionalRating;
}