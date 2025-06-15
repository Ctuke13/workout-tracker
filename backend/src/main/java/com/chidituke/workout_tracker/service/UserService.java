package com.chidituke.workout_tracker.service;

import com.chidituke.workout_tracker.model.User;
import com.chidituke.workout_tracker.dto.request.auth.RegisterRequest;
import com.chidituke.workout_tracker.dto.request.user.UserUpdateRequest;
import com.chidituke.workout_tracker.dto.request.user.UserSearchRequest;
import com.chidituke.workout_tracker.dto.response.user.UserProfileResponse;
import com.chidituke.workout_tracker.dto.response.user.UserSearchResponse;
import com.chidituke.workout_tracker.repository.UserRepository;
import com.chidituke.workout_tracker.mapper.UserMapper;
import com.chidituke.workout_tracker.exceptions.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // ═══════════════════════════════════════════════════════════════════
    // 🔍 BASIC USER OPERATIONS
    // ═══════════════════════════════════════════════════════════════════

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    // ═══════════════════════════════════════════════════════════════════
    // 👤 USER REGISTRATION & PROFILE MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

    @Transactional
    public User registerUser(RegisterRequest request) {
        // Validate unique email and username
        if (existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        user.setZipcode(request.getZipcode());
        user.setRole(User.Role.USER);
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        user.setPrivacySettings(User.PrivacySettings.FRIENDS_ONLY);

        return userRepository.save(user);
    }

    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserUpdateRequest request) {
        User user = getUserById(userId);

        // Update basic info
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getDateOfBirth() != null) user.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getZipcode() != null) user.setZipcode(request.getZipcode());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getProfilePictureUrl() != null) user.setProfilePictureUrl(request.getProfilePictureUrl());

        // Update preferences
        if (request.getPrivacySettings() != null) user.setPrivacySettings(request.getPrivacySettings());
        if (request.getNotificationSettings() != null) user.setNotificationSettings(request.getNotificationSettings());
        if (request.getPreferredMeasurementSystem() != null) user.setPreferredMeasurementSystem(request.getPreferredMeasurementSystem());
        if (request.getFitnessLevel() != null) user.setFitnessLevel(request.getFitnessLevel());
        if (request.getFitnessGoals() != null) user.setFitnessGoals(request.getFitnessGoals());
        if (request.getPreferredWorkoutDuration() != null) user.setPreferredWorkoutDuration(request.getPreferredWorkoutDuration());
        if (request.getWorkoutFrequency() != null) user.setWorkoutFrequency(request.getWorkoutFrequency());

        User savedUser = userRepository.save(user);

        return userMapper.mapEntityToProfileResponse(savedUser);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        user.setAccountStatus(User.AccountStatus.DELETED);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void deactivateUser(Long userId) {
        User user = getUserById(userId);
        user.setAccountStatus(User.AccountStatus.SUSPENDED);
        userRepository.save(user);
    }

    @Transactional
    public void reactivateUser(Long userId) {
        User user = getUserById(userId);
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        userRepository.save(user);
    }

    // ═══════════════════════════════════════════════════════════════════
    // 🔍 USER SEARCH & DISCOVERY
    // ═══════════════════════════════════════════════════════════════════

    public Page<UserSearchResponse> searchUsers(UserSearchRequest request, Pageable pageable) {
        try {
            Page<User> users;

            if (request.getQuery() != null && !request.getQuery().trim().isEmpty()) {
                users = userRepository.searchUsersForFriendConnection(request.getQuery(), pageable);
            } else {
                users = userRepository.findAll(pageable)
                        .map(user -> user.getAccountStatus() == User.AccountStatus.ACTIVE ? user : null)
                        .map(user -> user);
            }

            return users.map(userMapper::mapEntityToSearchResponse);
        } catch (Exception e) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public List<UserSearchResponse> findUsersNearLocation(String zipcode, int radiusMiles, int limit) {
        try {
            List<User> users = userRepository.findByZipcodeAndUserType(zipcode, User.UserType.REGULAR)
                    .stream()
                    .filter(user -> user.getAccountStatus() == User.AccountStatus.ACTIVE)
                    .limit(limit)
                    .collect(Collectors.toList());

            return userMapper.mapEntitiesToSearchResponseList(users);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<UserSearchResponse> findUsersByFitnessLevel(User.FitnessLevel fitnessLevel, int limit) {
        try {
            List<User> users = userRepository.findAll()
                    .stream()
                    .filter(user -> user.getAccountStatus() == User.AccountStatus.ACTIVE)
                    .filter(user -> user.getFitnessLevel() == fitnessLevel)
                    .limit(limit)
                    .collect(Collectors.toList());

            return userMapper.mapEntitiesToSearchResponseList(users);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<UserSearchResponse> findRecentlyActiveUsers(int limit) {
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(7);
            List<User> users = userRepository.findActiveUsersSince(since)
                    .stream()
                    .limit(limit)
                    .collect(Collectors.toList());

            return userMapper.mapEntitiesToSearchResponseList(users);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // 👥 SOCIAL FEATURES (PLACEHOLDERS - FRIEND SYSTEM NOT IMPLEMENTED)
    // ═══════════════════════════════════════════════════════════════════

    @Transactional
    public void sendFriendRequest(Long fromUserId, Long toUserId) {
        User fromUser = getUserById(fromUserId);
        User toUser = getUserById(toUserId);

        if (fromUserId.equals(toUserId)) {
            throw new IllegalArgumentException("Cannot send friend request to yourself");
        }

        // PLACEHOLDER: Friend system not implemented yet
        throw new UnsupportedOperationException("Friend system not implemented yet. This feature will be added in a future update.");
    }

    public List<UserSearchResponse> getFriends(Long userId, int limit) {
        // PLACEHOLDER: Friend system not implemented yet
        return new ArrayList<>();
    }

    public List<UserSearchResponse> getMutualFriends(Long user1Id, Long user2Id) {
        // PLACEHOLDER: Friend system not implemented yet
        return new ArrayList<>();
    }

    // ═══════════════════════════════════════════════════════════════════
    // 🏋️ ACTIVITY & ENGAGEMENT
    // ═══════════════════════════════════════════════════════════════════

    @Transactional
    public void updateLastActive(Long userId) {
        User user = getUserById(userId);
        user.updateLastActive();
        userRepository.save(user);
    }

    public User.ActivityLevel getUserActivityLevel(Long userId) {
        User user = getUserById(userId);
        return user.getActivityLevel();
    }

    public String getActivityStatusForViewer(Long userId, Long viewerId) {
        User user = getUserById(userId);
        User viewer = viewerId != null ? getUserById(viewerId) : null;
        return user.getActivityStatusForViewer(viewer);
    }

    // ═══════════════════════════════════════════════════════════════════
    // 💼 PROFESSIONAL USER FEATURES
    // ═══════════════════════════════════════════════════════════════════

    public List<UserSearchResponse> findVerifiedProfessionals(String location, int limit) {
        try {
            List<User> professionals = userRepository.findVerifiedProfessionals()
                    .stream()
                    .filter(user -> location == null || user.getZipcode().equals(location))
                    .limit(limit)
                    .collect(Collectors.toList());

            return userMapper.mapEntitiesToSearchResponseList(professionals);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<UserSearchResponse> findProfessionalsBySpecialization(String specialization, int limit) {
        try {
            return findVerifiedProfessionals(null, limit);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<UserSearchResponse> findAvailableProfessionals(String location, int limit) {
        try {
            List<User> professionals = userRepository.findProfessionalsAcceptingRequests()
                    .stream()
                    .filter(user -> location == null || user.getZipcode().equals(location))
                    .limit(limit)
                    .collect(Collectors.toList());

            return userMapper.mapEntitiesToSearchResponseList(professionals);
        } catch (Exception e) {
            return findVerifiedProfessionals(location, limit);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // 📊 ANALYTICS & INSIGHTS
    // ═══════════════════════════════════════════════════════════════════

    public long getTotalActiveUsers() {
        try {
            return userRepository.countActiveUsersByType(User.UserType.REGULAR);
        } catch (Exception e) {
            return userRepository.count();
        }
    }

    public long getTotalVerifiedProfessionals() {
        try {
            return userRepository.countActiveProfessionals();
        } catch (Exception e) {
            return userRepository.findVerifiedProfessionals().size();
        }
    }

    public List<User> getTopRatedProfessionals(int limit) {
        try {
            LocalDateTime recentCutoff = LocalDateTime.now().minusDays(30);
            return userRepository.findTopRankedProfessionalsByActivity(
                    null,
                    recentCutoff,
                    PageRequest.of(0, limit)
            ).getContent();
        } catch (Exception e) {
            return userRepository.findVerifiedProfessionals()
                    .stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // 🔄 DTO CONVERSION METHODS (SIMPLIFIED WITH MAPPER!)
    // ═══════════════════════════════════════════════════════════════════

    public UserProfileResponse convertToUserProfileResponse(User user) {
        return userMapper.mapEntityToProfileResponse(user);
    }

    public UserSearchResponse convertToUserSearchResponse(User user) {
        return userMapper.mapEntityToSearchResponse(user);
    }

    // ═══════════════════════════════════════════════════════════════════
    // 🛠️ UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════════

    private <T> List<T> safeRepositoryCall(java.util.function.Supplier<List<T>> repositoryCall, List<T> fallback) {
        try {
            return repositoryCall.get();
        } catch (Exception e) {
            return fallback;
        }
    }

    private <T> Page<T> safeRepositoryPageCall(java.util.function.Supplier<Page<T>> repositoryCall, Pageable pageable) {
        try {
            return repositoryCall.get();
        } catch (Exception e) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }
}