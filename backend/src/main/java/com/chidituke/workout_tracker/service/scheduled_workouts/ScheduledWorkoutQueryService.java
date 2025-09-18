package com.chidituke.workout_tracker.service.scheduled_workouts;

import com.chidituke.workout_tracker.dto.response.scheduled_workouts.ScheduledWorkoutResponse;
import com.chidituke.workout_tracker.dto.response.scheduled_workouts.CalendarViewResponse;
import com.chidituke.workout_tracker.exceptions.user.UserNotFoundException;
import com.chidituke.workout_tracker.mapper.workout.ScheduledWorkoutMapper;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.workout.ScheduledWorkout;
import com.chidituke.workout_tracker.repository.scheduled_workouts.ScheduledWorkoutRepository;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import com.chidituke.workout_tracker.exceptions.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for read-only scheduled workout operations, calendar views, and workout retrieval.
 * Handles all non-modifying workout queries with performance optimization through caching.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduledWorkoutQueryService {

    private final ScheduledWorkoutRepository scheduledWorkoutRepository;
    private final ScheduledWorkoutMapper scheduledWorkoutMapper;
    private final UserRepository userRepository;

    // ==================== BASIC WORKOUT RETRIEVAL ====================

    public Optional<ScheduledWorkout> findById(Long id) {
        return scheduledWorkoutRepository.findById(id);
    }

    public ScheduledWorkout getById(Long id) {
        return scheduledWorkoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled workout not found with id: " + id));
    }

    public ScheduledWorkoutResponse getWorkoutResponse(Long id) {
        ScheduledWorkout workout = getById(id);
        return scheduledWorkoutMapper.toResponse(workout);
    }

    public boolean existsById(Long id) {
        return scheduledWorkoutRepository.existsById(id);
    }

    // ==================== USER WORKOUT QUERIES ====================

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    public List<ScheduledWorkoutResponse> getUserWorkouts(Long userId) {
        try {
            List<ScheduledWorkout> workouts = scheduledWorkoutRepository.findByUserIdAndDeletedFalseOrderByScheduledDateTimeAsc(userId);
            return workouts.stream()
                    .map(scheduledWorkoutMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving user workouts for user {}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    @Cacheable(value = "user-upcoming-workouts", key = "#userId + '_' + #limit")
    public List<ScheduledWorkoutResponse> getUpcomingWorkouts(Long userId, int limit) {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<ScheduledWorkout> workouts = scheduledWorkoutRepository
                    .findUpcomingWorkoutsByUserId(userId, now)
                    .stream()
                    .limit(limit)
                    .collect(Collectors.toList());

            return workouts.stream()
                    .map(scheduledWorkoutMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving upcoming workouts for user {}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    @Cacheable(value = "user-recent-workouts", key = "#userId + '_' + #limit")
    public List<ScheduledWorkoutResponse> getRecentCompletedWorkouts(Long userId, int limit) {
        try {
            List<ScheduledWorkout> workouts = scheduledWorkoutRepository
                    .findRecentCompletedWorkoutsByUserId(userId)
                    .stream()
                    .limit(limit)
                    .collect(Collectors.toList());

            return workouts.stream()
                    .map(scheduledWorkoutMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving recent completed workouts for user {}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    // ==================== CALENDAR & DATE-BASED QUERIES ====================

    @Cacheable(value = "user-workout-calendar", key = "#userId + '_' + #startDate + '_' + #endDate")
    public CalendarViewResponse getWorkoutCalendar(Long userId, LocalDate startDate, LocalDate endDate) {
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

            List<ScheduledWorkout> workouts = scheduledWorkoutRepository
                    .findWorkoutsByUserAndDateRange(userId, startDateTime, endDateTime);

            List<ScheduledWorkoutResponse> workoutResponses = workouts.stream()
                    .map(scheduledWorkoutMapper::toResponse)
                    .collect(Collectors.toList());

            // Group by date for calendar display (this is what CalendarViewResponse expects)
            Map<LocalDate, List<ScheduledWorkoutResponse>> workoutsByDate = workoutResponses.stream()
                    .collect(Collectors.groupingBy(ScheduledWorkoutResponse::getScheduledDate));

            return CalendarViewResponse.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .workoutsByDate(workoutsByDate)  // Use workoutsByDate, not workouts
                    .totalScheduled(workoutResponses.size())
                    .build();
        } catch (Exception e) {
            log.error("Error retrieving workout calendar for user {} between {} and {}: {}",
                    userId, startDate, endDate, e.getMessage());
            return CalendarViewResponse.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .workoutsByDate(Map.of())  // Empty map for error case
                    .totalScheduled(0)
                    .build();
        }
    }

    public List<ScheduledWorkoutResponse> getWorkoutsForDate(Long userId, LocalDate date) {
        try {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);

            List<ScheduledWorkout> workouts = scheduledWorkoutRepository
                    .findWorkoutsByUserAndDateRange(userId, startOfDay, endOfDay);

            return workouts.stream()
                    .map(scheduledWorkoutMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving workouts for user {} on date {}: {}", userId, date, e.getMessage());
            return List.of();
        }
    }

    @Cacheable(value = "user-workout-week", key = "#userId + '_' + #weekStart")
    public List<ScheduledWorkoutResponse> getWorkoutsForWeek(Long userId, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        return getWorkoutsForDateRange(userId, weekStart, weekEnd);
    }

    @Cacheable(value = "user-workout-month", key = "#userId + '_' + #month + '_' + #year")
    public List<ScheduledWorkoutResponse> getWorkoutsForMonth(Long userId, int month, int year) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
        return getWorkoutsForDateRange(userId, monthStart, monthEnd);
    }

    public List<ScheduledWorkoutResponse> getWorkoutsForDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        try {
            // Get the User object first (since repository methods need User, not userId)
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

            // Use the existing repository method that actually exists
            List<ScheduledWorkout> workouts = scheduledWorkoutRepository
                    .findByUserAndScheduledDateBetweenOrderByScheduledDateAsc(user, startDate, endDate);

            return workouts.stream()
                    .map(scheduledWorkoutMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving workouts for user {} between {} and {}: {}",
                    userId, startDate, endDate, e.getMessage());
            return List.of();
        }
    }

    // ==================== STATUS-BASED QUERIES ====================

    public List<ScheduledWorkoutResponse> getWorkoutsByStatus(Long userId, ScheduledWorkout.ScheduleStatus status) {
        try {
            List<ScheduledWorkout> workouts = scheduledWorkoutRepository.findByUserIdAndStatusAndDeletedFalse(userId, status);
            return workouts.stream()
                    .map(scheduledWorkoutMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving workouts with status {} for user {}: {}", status, userId, e.getMessage());
            return List.of();
        }
    }

    public List<ScheduledWorkoutResponse> getPendingWorkouts(Long userId) {
        return getWorkoutsByStatus(userId, ScheduledWorkout.ScheduleStatus.SCHEDULED);
    }

    public List<ScheduledWorkoutResponse> getCompletedWorkouts(Long userId) {
        return getWorkoutsByStatus(userId, ScheduledWorkout.ScheduleStatus.COMPLETED);
    }

    public List<ScheduledWorkoutResponse> getSkippedWorkouts(Long userId) {
        return getWorkoutsByStatus(userId, ScheduledWorkout.ScheduleStatus.SKIPPED);
    }

    // ==================== SEARCH & FILTERING ====================

    public Page<ScheduledWorkoutResponse> searchUserWorkouts(Long userId, String searchTerm, Pageable pageable) {
        try {
            Page<ScheduledWorkout> workouts = scheduledWorkoutRepository
                    .searchUserWorkouts(userId, searchTerm, pageable);

            List<ScheduledWorkoutResponse> responses = workouts.getContent().stream()
                    .map(scheduledWorkoutMapper::toResponse)
                    .collect(Collectors.toList());

            return new PageImpl<>(responses, pageable, workouts.getTotalElements());
        } catch (Exception e) {
            log.error("Error searching workouts for user {} with term '{}': {}", userId, searchTerm, e.getMessage());
            return new PageImpl<>(List.of(), pageable, 0);
        }
    }

    public List<ScheduledWorkoutResponse> getWorkoutsByExerciseId(Long userId, Long exerciseId) {
        try {
            List<ScheduledWorkout> workouts = scheduledWorkoutRepository
                    .findWorkoutsContainingExercise(userId, exerciseId);

            return workouts.stream()
                    .map(scheduledWorkoutMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving workouts containing exercise {} for user {}: {}", exerciseId, userId, e.getMessage());
            return List.of();
        }
    }

    // ==================== STREAK & CONSISTENCY QUERIES ====================

    @Cacheable(value = "user-workout-streak", key = "#userId")
    public int getCurrentWorkoutStreak(Long userId) {
        try {
            return scheduledWorkoutRepository.calculateCurrentStreak(userId);
        } catch (Exception e) {
            log.error("Error calculating current streak for user {}: {}", userId, e.getMessage());
            return 0;
        }
    }

    @Cacheable(value = "user-workout-consistency", key = "#userId + '_' + #days")
    public double getWorkoutConsistency(Long userId, int days) {
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(days);
            List<ScheduledWorkout> scheduledWorkouts = scheduledWorkoutRepository
                    .findWorkoutsSince(userId, since);

            if (scheduledWorkouts.isEmpty()) return 0.0;

            long completedCount = scheduledWorkouts.stream()
                    .filter(w -> "COMPLETED".equals(w.getStatus()))
                    .count();

            return (double) completedCount / scheduledWorkouts.size() * 100;
        } catch (Exception e) {
            log.error("Error calculating workout consistency for user {}: {}", userId, e.getMessage());
            return 0.0;
        }
    }

    // ==================== VALIDATION & CONFLICT CHECKS ====================

    public boolean hasWorkoutConflict(Long userId, LocalDateTime scheduledDateTime, Long excludeWorkoutId) {
        try {
            LocalDateTime startWindow = scheduledDateTime.minusMinutes(30);
            LocalDateTime endWindow = scheduledDateTime.plusMinutes(30);

            List<ScheduledWorkout> conflictingWorkouts = scheduledWorkoutRepository
                    .findConflictingWorkouts(userId, startWindow, endWindow);

            if (excludeWorkoutId != null) {
                conflictingWorkouts = conflictingWorkouts.stream()
                        .filter(w -> !w.getId().equals(excludeWorkoutId))
                        .collect(Collectors.toList());
            }

            return !conflictingWorkouts.isEmpty();
        } catch (Exception e) {
            log.error("Error checking workout conflicts for user {} at {}: {}", userId, scheduledDateTime, e.getMessage());
            return false;
        }
    }

    public List<ScheduledWorkoutResponse> getConflictingWorkouts(Long userId, LocalDateTime scheduledDateTime) {
        try {
            LocalDateTime startWindow = scheduledDateTime.minusMinutes(30);
            LocalDateTime endWindow = scheduledDateTime.plusMinutes(30);

            List<ScheduledWorkout> conflicts = scheduledWorkoutRepository
                    .findConflictingWorkouts(userId, startWindow, endWindow);

            return conflicts.stream()
                    .map(scheduledWorkoutMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving conflicting workouts for user {} at {}: {}", userId, scheduledDateTime, e.getMessage());
            return List.of();
        }
    }

    // ==================== PAGINATION HELPERS ====================

    public Page<ScheduledWorkoutResponse> getUserWorkoutsPaginated(Long userId, Pageable pageable) {
        try {
            Page<ScheduledWorkout> workouts = scheduledWorkoutRepository
                    .findByUserIdAndDeletedFalseOrderByScheduledDateTimeDesc(userId, pageable);

            List<ScheduledWorkoutResponse> responses = workouts.getContent().stream()
                    .map(scheduledWorkoutMapper::toResponse)
                    .collect(Collectors.toList());

            return new PageImpl<>(responses, pageable, workouts.getTotalElements());
        } catch (Exception e) {
            log.error("Error retrieving paginated workouts for user {}: {}", userId, e.getMessage());
            return new PageImpl<>(List.of(), pageable, 0);
        }
    }
}