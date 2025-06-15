package com.chidituke.workout_tracker.dto.request.user;

import com.chidituke.workout_tracker.model.User;
import lombok.Data;

@Data
public class UserSearchRequest {
    private String query; // Search by name, username, email
    private String zipcode; // Location-based search
    private Integer radiusMiles; // Search radius
    private User.FitnessLevel fitnessLevel;
    private User.Role role;
    private Boolean isProfessional;
    private Boolean isVerified; // For professionals
    private Boolean isAcceptingClients; // For professionals
    private String specialization; // For professional search
}