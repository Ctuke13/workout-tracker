package com.chidituke.workout_tracker.mapper;

import com.chidituke.workout_tracker.model.User;
import com.chidituke.workout_tracker.model.ProfessionalProfile;
import com.chidituke.workout_tracker.dto.response.user.UserProfileResponse;
import com.chidituke.workout_tracker.dto.response.user.UserSearchResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    /**
     * Converts User entity to UserProfileResponse DTO (Performance Optimized)
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
        response.setPhoneNumber(user.getPhoneNumber());
        response.setBio(user.getBio());
        response.setProfileImageUrl(user.getProfileImageUrl());
        response.setUserType(user.getUserType());
        response.setAccountStatus(user.getAccountStatus());
        response.setPrivacySettings(user.getPrivacySettings());
        response.setNotificationSettings(user.getNotificationSettings());

        // ✅ FIXED: Set ALL location fields for ALL users
        response.setZipcode(user.getZipcode());
        response.setCity(user.getCity());
        response.setState(user.getState());
        response.setCountry(user.getCountry());

        // Preferences and fitness information
        response.setMeasurementSystem(user.getMeasurementSystem());
        response.setFitnessLevel(user.getFitnessLevel());
        response.setFitnessGoals(user.getFitnessGoals());
        response.setWorkoutFrequency(user.getWorkoutFrequency());

        // Activity and timestamp information
        response.setLastActive(user.getLastActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setActivityLevel(user.getActivityLevel());

        // ✅ ADDED: Physical measurements
        response.setHeightCm(user.getHeightCm());
        response.setWeightKg(user.getWeightKg());

        // Activity status calculations
        response.setCurrentlyActive(isCurrentlyActive(user));
        response.setActiveToday(isActiveToday(user));

        // Professional profile information (if applicable)
        mapProfessionalInfoToProfileResponse(user, response);

        return response;
    }

    /**
     * Converts User entity to UserSearchResponse DTO (Performance Optimized)
     */
    public UserSearchResponse mapEntityToSearchResponse(User user) {
        UserSearchResponse response = new UserSearchResponse();

        // Basic user information for search
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setProfileImageUrl(user.getProfileImageUrl());
        response.setFitnessLevel(user.getFitnessLevel());

        // ✅ Set location fields for ALL users
        response.setZipcode(user.getZipcode());
        response.setCity(user.getCity());
        response.setState(user.getState());

        // Activity information
        response.setActivityLevel(user.getActivityLevel());
        response.setCurrentlyActive(isCurrentlyActive(user));
        response.setLastActive(user.getLastActive());

        // Professional profile information (if applicable)
        mapProfessionalInfoToSearchResponse(user, response);

        return response;
    }

    /**
     * Converts a list of User entities to UserProfileResponse DTOs (Batch Optimized)
     */
    public List<UserProfileResponse> mapEntitiesToProfileResponseList(List<User> users) {
        return users.stream()
                .map(this::mapEntityToProfileResponse)
                .collect(Collectors.toList());
    }

    /**
     * Converts a list of User entities to UserSearchResponse DTOs (Batch Optimized)
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
        if (user.isProfessional() && user.getProfessionalProfile() != null) {
            ProfessionalProfile profile = user.getProfessionalProfile();
            response.setIsProfessional(true);
            response.setProfessionalDisplayName(profile.getDisplayName());
            response.setProfessionalServiceType(profile.getServiceType());
            response.setProfessionalVerified(profile.getIsVerified());
            response.setProfessionalAcceptingClients(profile.getAcceptsNewClients());
            response.setProfessionalRating(profile.getAverageRating());
        } else {
            response.setIsProfessional(false);
        }
    }

    /**
     * Helper method to map professional profile information to UserSearchResponse
     */
    private void mapProfessionalInfoToSearchResponse(User user, UserSearchResponse response) {
        if (user.isProfessional() && user.getProfessionalProfile() != null) {
            ProfessionalProfile profile = user.getProfessionalProfile();
            response.setIsProfessional(true);
            response.setProfessionalDisplayName(profile.getDisplayName());
            response.setProfessionalServiceType(profile.getServiceType());
            response.setProfessionalVerified(profile.getIsVerified());
            response.setProfessionalAcceptingClients(profile.getAcceptsNewClients());
            response.setProfessionalRating(profile.getAverageRating());
        } else {
            response.setIsProfessional(false);
        }
    }

    // Helper methods for activity status calculation
    private boolean isCurrentlyActive(User user) {
        if (user.getLastActive() == null) return false;
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);
        return user.getLastActive().isAfter(cutoff);
    }

    private boolean isActiveToday(User user) {
        if (user.getLastActive() == null) return false;
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        return user.getLastActive().isAfter(todayStart);
    }
}