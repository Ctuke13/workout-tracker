package com.chidituke.workout_tracker.mapper;

import com.chidituke.workout_tracker.model.User;
import com.chidituke.workout_tracker.model.ProfessionalProfile;
import com.chidituke.workout_tracker.dto.response.user.UserProfileResponse;
import com.chidituke.workout_tracker.dto.response.user.UserSearchResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    /**
     * Converts User entity to UserProfileResponse DTO
     */
    public UserProfileResponse mapEntityToProfileResponse(User user) {
        UserProfileResponse response = new UserProfileResponse();

        // Basic user information
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setDateOfBirth(user.getDateOfBirth());
        response.setGender(user.getGender());
        response.setZipcode(user.getZipcode());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setBio(user.getBio());
        response.setProfilePictureUrl(user.getProfilePictureUrl());

        // Account and role information
        response.setRole(user.getRole());
        response.setAccountStatus(user.getAccountStatus());
        response.setPrivacySettings(user.getPrivacySettings());
        response.setNotificationSettings(user.getNotificationSettings());

        // Preferences and fitness information
        response.setPreferredMeasurementSystem(user.getPreferredMeasurementSystem());
        response.setFitnessLevel(user.getFitnessLevel());
        response.setFitnessGoals(user.getFitnessGoals());
        response.setPreferredWorkoutDuration(user.getPreferredWorkoutDuration());
        response.setWorkoutFrequency(user.getWorkoutFrequency());

        // Activity and timestamp information
        response.setLastActive(user.getLastActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setActivityLevel(user.getActivityLevel());
        response.setCurrentlyActive(user.isCurrentlyActive());
        response.setActiveToday(user.isActiveToday());

        // Professional profile information (if applicable)
        mapProfessionalInfoToProfileResponse(user, response);

        return response;
    }

    /**
     * Converts User entity to UserSearchResponse DTO
     */
    public UserSearchResponse mapEntityToSearchResponse(User user) {
        UserSearchResponse response = new UserSearchResponse();

        // Basic user information for search
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setProfilePictureUrl(user.getProfilePictureUrl());
        response.setFitnessLevel(user.getFitnessLevel());
        response.setZipcode(user.getZipcode());

        // Activity information
        response.setActivityLevel(user.getActivityLevel());
        response.setCurrentlyActive(user.isCurrentlyActive());
        response.setLastActive(user.getLastActive());

        // Professional profile information (if applicable)
        mapProfessionalInfoToSearchResponse(user, response);

        return response;
    }

    /**
     * Converts a list of User entities to UserProfileResponse DTOs
     */
    public List<UserProfileResponse> mapEntitiesToProfileResponseList(List<User> users) {
        return users.stream()
                .map(this::mapEntityToProfileResponse)
                .collect(Collectors.toList());
    }

    /**
     * Converts a list of User entities to UserSearchResponse DTOs
     */
    public List<UserSearchResponse> mapEntitiesToSearchResponseList(List<User> users) {
        return users.stream()
                .map(this::mapEntityToSearchResponse)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to map professional profile information to UserProfileResponse
     */
    private void mapProfessionalInfoToProfileResponse(User user, UserProfileResponse response) {
        if (user.getProfessionalProfile() != null) {
            ProfessionalProfile profile = user.getProfessionalProfile();
            response.setIsProfessional(true);
            response.setProfessionalDisplayName(profile.getDisplayName());
            response.setProfessionalServiceType(profile.getServiceType());
            response.setProfessionalVerified(profile.getIsVerified());
            response.setProfessionalAcceptingClients(profile.isAcceptingClients());
            response.setProfessionalRating(profile.getAverageRating());
        } else {
            response.setIsProfessional(false);
        }
    }

    /**
     * Helper method to map professional profile information to UserSearchResponse
     */
    private void mapProfessionalInfoToSearchResponse(User user, UserSearchResponse response) {
        if (user.getProfessionalProfile() != null) {
            ProfessionalProfile profile = user.getProfessionalProfile();
            response.setIsProfessional(true);
            response.setProfessionalDisplayName(profile.getDisplayName());
            response.setProfessionalServiceType(profile.getServiceType());
            response.setProfessionalVerified(profile.getIsVerified());
            response.setProfessionalAcceptingClients(profile.isAcceptingClients());
            response.setProfessionalRating(profile.getAverageRating());
        } else {
            response.setIsProfessional(false);
        }
    }
}