package com.chidituke.workout_tracker.service;

import com.chidituke.workout_tracker.model.User;
import com.chidituke.workout_tracker.model.UserType;
import com.chidituke.workout_tracker.dto.request.auth.RegisterRequest;
import com.chidituke.workout_tracker.dto.request.user.UserUpdateRequest;
import com.chidituke.workout_tracker.dto.request.user.UserSearchRequest;
import com.chidituke.workout_tracker.dto.response.user.UserProfileResponse;
import com.chidituke.workout_tracker.dto.response.user.UserSearchResponse;
import com.chidituke.workout_tracker.repository.UserRepository;
import com.chidituke.workout_tracker.mapper.UserMapper;
import com.chidituke.workout_tracker.exceptions.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // ═══════════════════════════════════════════════════════════════════
    // 🔍 BASIC USER OPERATIONS (PERFORMANCE OPTIMIZED)
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

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .userType(UserType.REGULAR)
                .accountStatus(User.AccountStatus.ACTIVE)
                .privacySettings(User.PrivacySettings.PUBLIC)
                .build();

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
        if (request.getPreferredMeasurementSystem() != null) user.setMeasurementSystem(request.getPreferredMeasurementSystem());
        if (request.getFitnessLevel() != null) user.setFitnessLevel(request.getFitnessLevel());
        if (request.getWorkoutFrequency() != null) user.setWorkoutFrequency(request.getWorkoutFrequency());
        if (request.getFitnessGoals() != null && !request.getFitnessGoals().isEmpty()) {
            user.setFitnessGoals(String.join(", ", request.getFitnessGoals()));
        }
        if (request.getHeightCm() != null) user.setHeightCm(request.getHeightCm());
        if (request.getWeightKg() != null) user.setWeightKg(request.getWeightKg());
        if (request.getActivityLevel() != null) user.setActivityLevel(request.getActivityLevel());

        User savedUser = userRepository.save(user);
        return userMapper.mapEntityToProfileResponse(savedUser);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        user.setAccountStatus(User.AccountStatus.SUSPENDED);
        user.setUpdatedAt(LocalDateTime.now());
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
    // 🔍 USER SEARCH & DISCOVERY (PERFORMANCE OPTIMIZED)
    // ═══════════════════════════════════════════════════════════════════

    public Page<UserSearchResponse> searchUsers(UserSearchRequest request, Pageable pageable) {
        try {
            Page<User> users;

            // Text-based search takes priority
            if (request.getQuery() != null && !request.getQuery().trim().isEmpty()) {
                users = userRepository.searchUsersForConnection(request.getQuery(), pageable);
            }
            // Location-specific search
            else if (request.hasLocationFilter()) {
                users = searchUsersByLocation(request, pageable);
            }
            // Filter-based search
            else {
                users = userRepository.findUsersWithFilters(
                        request.getUserType(),
                        null, // subscription tier filter
                        null, // search term
                        pageable
                );
            }

            // ✅ FIXED: Convert Page to List, apply filters, then back to Page
            List<User> userList = users.getContent();
            List<UserSearchResponse> filteredResults = userList.stream()
                    .filter(user -> passesFilters(user, request))
                    .map(userMapper::mapEntityToSearchResponse)
                    .collect(Collectors.toList());

            return new PageImpl<>(filteredResults, pageable, users.getTotalElements());

        } catch (Exception e) {
            log.error("Error searching users: ", e);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    /**
     * Search users by location with enhanced filtering
     */
    private Page<User> searchUsersByLocation(UserSearchRequest request, Pageable pageable) {
        // Exact zipcode search
        if (request.getZipcode() != null) {
            List<User> users = userRepository.findByZipcodeAndAccountStatus(
                    request.getZipcode(), User.AccountStatus.ACTIVE);
            return new PageImpl<>(users, pageable, users.size());
        }

        // City and state search
        if (request.getCity() != null && request.getState() != null) {
            List<User> users = userRepository.findByCityContainingIgnoreCaseAndAccountStatus(
                            request.getCity(), User.AccountStatus.ACTIVE)
                    .stream()
                    .filter(user -> user.getState() != null &&
                            user.getState().equalsIgnoreCase(request.getState()))
                    .collect(Collectors.toList());
            return new PageImpl<>(users, pageable, users.size());
        }

        // City-only search
        if (request.getCity() != null) {
            List<User> users = userRepository.findByCityContainingIgnoreCaseAndAccountStatus(
                    request.getCity(), User.AccountStatus.ACTIVE);
            return new PageImpl<>(users, pageable, users.size());
        }

        // State-only search
        if (request.getState() != null) {
            List<User> users = userRepository.findByStateIgnoreCaseAndAccountStatus(
                    request.getState(), User.AccountStatus.ACTIVE);
            return new PageImpl<>(users, pageable, users.size());
        }

        // Fallback to general location search
        String locationString = request.getLocationString();
        if (locationString != null) {
            List<User> users = userRepository.findByLocationAndAccountStatus(
                    locationString, User.AccountStatus.ACTIVE);
            return new PageImpl<>(users, pageable, users.size());
        }

        // No location filters, return empty
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    // Helper methods for activity checks
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

    public List<UserSearchResponse> findUsersNearLocation(String location, int radiusMiles, int limit) {
        try {
            List<User> users = userRepository.findByLocationAndAccountStatus(location, User.AccountStatus.ACTIVE)
                    .stream()
                    .limit(limit)
                    .collect(Collectors.toList());

            return userMapper.mapEntitiesToSearchResponseList(users);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<UserSearchResponse> findUsersByZipcode(String zipcode, int limit) {
        try {
            List<User> users = userRepository.findByZipcodeAndAccountStatus(zipcode, User.AccountStatus.ACTIVE)
                    .stream()
                    .limit(limit)
                    .collect(Collectors.toList());

            return userMapper.mapEntitiesToSearchResponseList(users);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private boolean passesFilters(User user, UserSearchRequest request) {
        // Apply activity filters
        if (request.getIsCurrentlyActive() != null && request.getIsCurrentlyActive()) {
            if (!isCurrentlyActive(user)) return false;
        }

        if (request.getIsActiveToday() != null && request.getIsActiveToday()) {
            if (!isActiveToday(user)) return false;
        }

        // Apply fitness level filter
        if (request.getFitnessLevel() != null &&
                user.getFitnessLevel() != request.getFitnessLevel()) {
            return false;
        }

        // Apply activity level filter
        if (request.getActivityLevel() != null &&
                user.getActivityLevel() != request.getActivityLevel()) {
            return false;
        }

        // Apply professional filters
        if (request.getIsProfessional() != null && request.getIsProfessional()) {
            if (!user.isProfessional()) return false;

            // Additional professional filters
            if (request.getIsVerified() != null && request.getIsVerified()) {
                if (!user.isProfessionalVerified()) return false;
            }

            if (request.getIsAcceptingClients() != null && request.getIsAcceptingClients()) {
                if (user.getProfessionalProfile() == null ||
                        !user.getProfessionalProfile().getAcceptsNewClients()) return false;
            }

            if (request.getOffersVirtual() != null && request.getOffersVirtual()) {
                if (user.getProfessionalProfile() == null ||
                        !user.getProfessionalProfile().getOffersVirtualSessions()) return false;
            }
        }

        // Apply subscription filters
        if (request.getHasPaidSubscription() != null && request.getHasPaidSubscription()) {
            if (!user.hasPaidSubscription()) return false;
        }

        if (request.getCanUseAIFeatures() != null && request.getCanUseAIFeatures()) {
            if (!user.canUseAIGeneration()) return false;
        }

        // Apply workout streak filter
        if (request.getMinWorkoutStreak() != null) {
            return user.getCurrentStreak() != null &&
                    user.getCurrentStreak() >= request.getMinWorkoutStreak();
        }

        return true;
    }


    public List<UserSearchResponse> findUsersByCity(String city, int limit) {
        try {
            List<User> users = userRepository.findByCityContainingIgnoreCaseAndAccountStatus(city, User.AccountStatus.ACTIVE)
                    .stream()
                    .limit(limit)
                    .collect(Collectors.toList());

            return userMapper.mapEntitiesToSearchResponseList(users);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<UserSearchResponse> findUsersByState(String state, int limit) {
        try {
            List<User> users = userRepository.findByStateIgnoreCaseAndAccountStatus(state, User.AccountStatus.ACTIVE)
                    .stream()
                    .limit(limit)
                    .collect(Collectors.toList());

            return userMapper.mapEntitiesToSearchResponseList(users);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<UserSearchResponse> findUsersByFitnessLevel(User.FitnessLevel fitnessLevel, int limit) {
        try {
            List<User> users = userRepository.findUsersWithFilters(null, null, null,
                            PageRequest.of(0, limit))
                    .stream()
                    .filter(user -> user.getFitnessLevel() == fitnessLevel)
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
    // 🏋️ ACTIVITY & ENGAGEMENT (PERFORMANCE OPTIMIZED)
    // ═══════════════════════════════════════════════════════════════════

    @Transactional
    public void updateLastActive(Long userId) {
        User user = getUserById(userId);
        user.setLastActive(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public User.ActivityLevel getUserActivityLevel(Long userId) {
        User user = getUserById(userId);
        return user.getActivityLevel();
    }

    public String getActivityStatus(Long userId) {
        User user = getUserById(userId);
        if (user.getLastActive() != null) {
            LocalDateTime hourAgo = LocalDateTime.now().minusHours(1);
            if (user.getLastActive().isAfter(hourAgo)) {
                return "Active";
            }
        }
        return "Inactive";
    }

    // ═══════════════════════════════════════════════════════════════════
    // 💼 PROFESSIONAL USER FEATURES (PERFORMANCE OPTIMIZED)
    // ═══════════════════════════════════════════════════════════════════

    public List<UserSearchResponse> findVerifiedProfessionals(String location, int limit) {
        try {
            List<User> professionals = userRepository.findVerifiedProfessionals()
                    .stream()
                    .filter(user -> location == null ||
                            (user.getProfessionalProfile() != null &&
                                    location.equals(user.getProfessionalProfile().getBaseZipcode())))
                    .limit(limit)
                    .collect(Collectors.toList());

            return userMapper.mapEntitiesToSearchResponseList(professionals);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<UserSearchResponse> findAvailableProfessionals(String location, int limit) {
        try {
            List<User> professionals = userRepository.findAvailableProfessionals()
                    .stream()
                    .filter(user -> location == null ||
                            (user.getProfessionalProfile() != null &&
                                    location.equals(user.getProfessionalProfile().getBaseZipcode())))
                    .limit(limit)
                    .collect(Collectors.toList());

            return userMapper.mapEntitiesToSearchResponseList(professionals);
        } catch (Exception e) {
            return findVerifiedProfessionals(location, limit);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // 📊 ANALYTICS & INSIGHTS (PERFORMANCE OPTIMIZED)
    // ═══════════════════════════════════════════════════════════════════

    public long getTotalActiveUsers() {
        try {
            return userRepository.countActiveUsersByType(UserType.REGULAR);
        } catch (Exception e) {
            return userRepository.count();
        }
    }

    public long getTotalVerifiedProfessionals() {
        try {
            return userRepository.countActiveUsersByType(UserType.PROFESSIONAL);
        } catch (Exception e) {
            return userRepository.findVerifiedProfessionals().size();
        }
    }

    public List<User> getTopRatedProfessionals(int limit) {
        try {
            return userRepository.findVerifiedProfessionals()
                    .stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // 🔄 DTO CONVERSION METHODS (PERFORMANCE OPTIMIZED)
    // ═══════════════════════════════════════════════════════════════════

    public UserProfileResponse convertToUserProfileResponse(User user) {
        return userMapper.mapEntityToProfileResponse(user);
    }

    public UserSearchResponse convertToUserSearchResponse(User user) {
        return userMapper.mapEntityToSearchResponse(user);
    }
}