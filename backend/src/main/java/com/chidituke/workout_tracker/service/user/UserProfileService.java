package com.chidituke.workout_tracker.service.user;

import com.chidituke.workout_tracker.dto.request.auth.RegisterRequest;
import com.chidituke.workout_tracker.dto.request.user.UserUpdateRequest;
import com.chidituke.workout_tracker.dto.response.user.UserProfileResponse;
import com.chidituke.workout_tracker.mapper.user.UserMapper;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.user.enums.UserType;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for user profile management and registration operations.
 * Handles all user profile modifications and account lifecycle operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserQueryService userQueryService;

    // ==================== USER REGISTRATION ====================

    public User registerUser(RegisterRequest request) {
        if (userQueryService.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (userQueryService.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .zipcode(request.getZipcode())
                .userType(UserType.REGULAR)
                .accountStatus(User.AccountStatus.ACTIVE)
                .privacySettings(User.PrivacySettings.PUBLIC)
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered: {} ({})", savedUser.getUsername(), savedUser.getEmail());
        return savedUser;
    }

    // ==================== PROFILE MANAGEMENT ====================

    public UserProfileResponse updateUserProfile(Long userId, UserUpdateRequest request) {
        User user = userQueryService.getUserById(userId);

        // Update basic info
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getDateOfBirth() != null) user.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getProfilePictureUrl() != null) user.setProfileImageUrl(request.getProfilePictureUrl());
        if (request.getZipcode() != null) user.setZipcode(request.getZipcode());
        if (request.getCity() != null) user.setCity(request.getCity());
        if (request.getState() != null) user.setState(request.getState());
        if (request.getCountry() != null) user.setCountry(request.getCountry());

        // Update preferences
        if (request.getPrivacySettings() != null) user.setPrivacySettings(request.getPrivacySettings());
        if (request.getNotificationSettings() != null) user.setNotificationSettings(request.getNotificationSettings());
        if (request.getPreferredMeasurementSystem() != null)
            user.setMeasurementSystem(request.getPreferredMeasurementSystem());
        if (request.getFitnessLevel() != null) user.setFitnessLevel(request.getFitnessLevel());
        if (request.getWorkoutFrequency() != null) user.setWorkoutFrequency(request.getWorkoutFrequency());
        if (request.getFitnessGoals() != null && !request.getFitnessGoals().isEmpty()) {
            user.setFitnessGoals(String.join(", ", request.getFitnessGoals()));
        }
        if (request.getHeightCm() != null) user.setHeightCm(request.getHeightCm());
        if (request.getWeightKg() != null) user.setWeightKg(request.getWeightKg());
        if (request.getActivityLevel() != null) user.setActivityLevel(request.getActivityLevel());

        User savedUser = save(user);
        log.info("Profile updated for user: {} ({})", savedUser.getUsername(), savedUser.getId());

        return userMapper.mapEntityToProfileResponse(savedUser);
    }

    public User save(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    // ==================== ACCOUNT MANAGEMENT ====================

    public void deleteUser(Long userId) {
        User user = userQueryService.getUserById(userId);
        user.setAccountStatus(User.AccountStatus.SUSPENDED);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("User account deleted: {} ({})", user.getUsername(), user.getId());
    }

    public void deactivateUser(Long userId) {
        User user = userQueryService.getUserById(userId);
        user.setAccountStatus(User.AccountStatus.SUSPENDED);
        User savedUser = userRepository.save(user);
        log.info("User account deactivated: {} ({})", savedUser.getUsername(), savedUser.getId());
    }

    public void reactivateUser(Long userId) {
        User user = userQueryService.getUserById(userId);
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        User savedUser = userRepository.save(user);
        log.info("User account reactivated: {} ({})", savedUser.getUsername(), savedUser.getId());
    }

    // ==================== DTO CONVERSION ====================

    public UserProfileResponse convertToUserProfileResponse(User user) {
        return userMapper.mapEntityToProfileResponse(user);
    }
}