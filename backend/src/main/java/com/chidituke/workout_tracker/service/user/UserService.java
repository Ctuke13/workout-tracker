package com.chidituke.workout_tracker.service.user;

import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.user.enums.UserType;
import com.chidituke.workout_tracker.dto.request.auth.RegisterRequest;
import com.chidituke.workout_tracker.dto.request.user.UserUpdateRequest;
import com.chidituke.workout_tracker.dto.request.user.UserSearchRequest;
import com.chidituke.workout_tracker.dto.response.user.UserProfileResponse;
import com.chidituke.workout_tracker.dto.response.user.UserSearchResponse;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import com.chidituke.workout_tracker.mapper.user.UserMapper;
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
    private final UserProfileService userProfileService;
    private final UserActivityService userActivityService;
    private final UserAdminService userAdminService;
    private final UserQueryService userQueryService;

    // ═══════════════════════════════════════════════════════════════════
    // 🔍 BASIC USER OPERATIONS (PERFORMANCE OPTIMIZED)
    // ═══════════════════════════════════════════════════════════════════

    public Optional<User> findByEmail(String email) {
        return userQueryService.findByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return userQueryService.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userQueryService.findById(id);
    }

    public User getUserById(Long id) {
        return userQueryService.getUserById(id); // Use query service
    }

    public User getUserByEmail(String email) {
        return userQueryService.getUserByEmail(email); // Use query service
    }

    /**
     * Get user ID by username (ADDED FOR PERFORMANCE CONTROLLER)
     */
    public Long getUserIdByUsername(String username) {
        return userQueryService.getUserIdByUsername(username); // Use query service
    }

    @Transactional
    public User save(User user) {
        return userProfileService.save(user);
    }

    /**
     * Get user by username (ADDED FOR CONVENIENCE)
     */
    public User getUserByUsername(String username) {
        return userQueryService.getUserByUsername(username); // Use query service
    }

    public boolean existsByEmail(String email) {
        return userQueryService.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userQueryService.existsByUsername(username);
    }

    // ═══════════════════════════════════════════════════════════════════
    // 👤 USER REGISTRATION & PROFILE MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

    @Transactional
    public User registerUser(RegisterRequest request) {
        return userProfileService.registerUser(request);
    }

    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserUpdateRequest request) {
        return userProfileService.updateUserProfile(userId, request);
    }

    @Transactional
    public void deleteUser(Long userId) {
        userProfileService.deleteUser(userId);
    }

    @Transactional
    public void deactivateUser(Long userId) {
        userProfileService.deactivateUser(userId);
    }

    @Transactional
    public void reactivateUser(Long userId) {
        userProfileService.reactivateUser(userId);
    }

    // ═══════════════════════════════════════════════════════════════════
    // 🔍 USER SEARCH & DISCOVERY (PERFORMANCE OPTIMIZED)
    // ═══════════════════════════════════════════════════════════════════

    public Page<UserSearchResponse> searchUsers(UserSearchRequest request, Pageable pageable) {
        return userQueryService.searchUsers(request, pageable);
    }


    public List<UserSearchResponse> findUsersNearLocation(String location, int radiusMiles, int limit) {
        return userQueryService.findUsersNearLocation(location, radiusMiles, limit);
    }

    public List<UserSearchResponse> findUsersByZipcode(String zipcode, int limit) {
        return userQueryService.findUsersByZipcode(zipcode, limit);
    }

    public List<UserSearchResponse> findUsersByCity(String city, int limit) {
        return userQueryService.findUsersByCity(city, limit);
    }

    public List<UserSearchResponse> findUsersByState(String state, int limit) {
        return userQueryService.findUsersByState(state, limit);
    }

    public List<UserSearchResponse> findUsersByFitnessLevel(User.FitnessLevel fitnessLevel, int limit) {
        return userQueryService.findUsersByFitnessLevel(fitnessLevel, limit);
    }

    public List<UserSearchResponse> findRecentlyActiveUsers(int limit) {
        return userQueryService.findRecentlyActiveUsers(limit);
    }


    // ═══════════════════════════════════════════════════════════════════
    // 🏋️ ACTIVITY & ENGAGEMENT (PERFORMANCE OPTIMIZED)
    // ═══════════════════════════════════════════════════════════════════

    @Transactional
    public void updateLastActive(Long userId) {
        userActivityService.updateLastActive(userId);
    }

    public User.ActivityLevel getUserActivityLevel(Long userId) {
        return userQueryService.getUserActivityLevel(userId);
    }

    public String getActivityStatus(Long userId) {
        return userQueryService.getActivityStatus(userId);
    }

    // ═══════════════════════════════════════════════════════════════════
    // 💼 PROFESSIONAL USER FEATURES (PERFORMANCE OPTIMIZED)
    // ═══════════════════════════════════════════════════════════════════

    public List<UserSearchResponse> findVerifiedProfessionals(String location, int limit) {
        return userQueryService.findVerifiedProfessionals(location, limit);
    }

    public List<UserSearchResponse> findAvailableProfessionals(String location, int limit) {
        return userQueryService.findAvailableProfessionals(location, limit);
    }

    // ═══════════════════════════════════════════════════════════════════
    // 📊 ANALYTICS & INSIGHTS (PERFORMANCE OPTIMIZED)
    // ═══════════════════════════════════════════════════════════════════

    public long getTotalActiveUsers() {
        return userAdminService.getTotalActiveUsers();
    }

    public long getTotalVerifiedProfessionals() {
        return userAdminService.getTotalVerifiedProfessionals();
    }

    public List<User> getTopRatedProfessionals(int limit) {
        return userAdminService.getTopRatedProfessionals(limit);
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