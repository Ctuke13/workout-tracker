package com.chidituke.workout_tracker.service.user;

import com.chidituke.workout_tracker.dto.request.user.UserSearchRequest;
import com.chidituke.workout_tracker.dto.response.user.UserSearchResponse;
import com.chidituke.workout_tracker.mapper.user.UserMapper;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import com.chidituke.workout_tracker.exceptions.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for read-only user operations, search, and discovery.
 * Handles all non-modifying user queries and public user information.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    // ==================== BASIC USER RETRIEVAL ====================

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

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    public Long getUserIdByUsername(String username) {
        User user = getUserByUsername(username);
        return user.getId();
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    // ==================== USER SEARCH & DISCOVERY ====================

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

        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    // ==================== LOCATION-BASED DISCOVERY ====================

    public List<UserSearchResponse> findUsersNearLocation(String location, int radiusMiles, int limit) {
        try {
            List<User> users = userRepository.findByLocationAndAccountStatus(location, User.AccountStatus.ACTIVE)
                    .stream()
                    .limit(limit)
                    .collect(Collectors.toList());

            return userMapper.mapEntitiesToSearchResponseList(users);
        } catch (Exception e) {
            log.error("Error finding users near location: {}", e.getMessage());
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
            log.error("Error finding users by zipcode: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<UserSearchResponse> findUsersByCity(String city, int limit) {
        try {
            List<User> users = userRepository.findByCityContainingIgnoreCaseAndAccountStatus(city, User.AccountStatus.ACTIVE)
                    .stream()
                    .limit(limit)
                    .collect(Collectors.toList());

            return userMapper.mapEntitiesToSearchResponseList(users);
        } catch (Exception e) {
            log.error("Error finding users by city: {}", e.getMessage());
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
            log.error("Error finding users by state: {}", e.getMessage());
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
            log.error("Error finding users by fitness level: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Cacheable(value = "recently-active-users", key = "#limit")
    public List<UserSearchResponse> findRecentlyActiveUsers(int limit) {
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(7);
            List<User> users = userRepository.findActiveUsersSince(since)
                    .stream()
                    .limit(limit)
                    .collect(Collectors.toList());

            return userMapper.mapEntitiesToSearchResponseList(users);
        } catch (Exception e) {
            log.error("Error finding recently active users: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    // ==================== PROFESSIONAL USER QUERIES ====================

    @Cacheable(value = "verified-professionals", key = "#location + '_' + #limit")
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
            log.error("Error finding verified professionals: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Cacheable(value = "available-professionals", key = "#location + '_' + #limit")
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
            log.error("Error finding available professionals: {}", e.getMessage());
            return findVerifiedProfessionals(location, limit);
        }
    }

    // ==================== ACTIVITY STATUS QUERIES ====================

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

    public User.ActivityLevel getUserActivityLevel(Long userId) {
        User user = getUserById(userId);
        return user.getActivityLevel();
    }

    // ==================== FILTER HELPERS ====================

    private boolean passesFilters(User user, UserSearchRequest request) {
        if (request.getIsCurrentlyActive() != null && request.getIsCurrentlyActive()) {
            if (!isCurrentlyActive(user)) return false;
        }

        if (request.getIsActiveToday() != null && request.getIsActiveToday()) {
            if (!isActiveToday(user)) return false;
        }

        if (request.getFitnessLevel() != null &&
                user.getFitnessLevel() != request.getFitnessLevel()) {
            return false;
        }

        if (request.getActivityLevel() != null &&
                user.getActivityLevel() != request.getActivityLevel()) {
            return false;
        }

        if (request.getIsProfessional() != null && request.getIsProfessional()) {
            if (!user.isProfessional()) return false;

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

        if (request.getHasPaidSubscription() != null && request.getHasPaidSubscription()) {
            if (!user.hasPaidSubscription()) return false;
        }

        if (request.getCanUseAIFeatures() != null && request.getCanUseAIFeatures()) {
            if (!user.canUseAIGeneration()) return false;
        }

        if (request.getMinWorkoutStreak() != null) {
            return user.getCurrentStreak() != null &&
                    user.getCurrentStreak() >= request.getMinWorkoutStreak();
        }

        return true;
    }

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