package com.chidituke.workout_tracker.service.exercise;

import com.chidituke.workout_tracker.dto.request.exercise.ExerciseCreateRequestDTO;
import com.chidituke.workout_tracker.dto.request.exercise.ExerciseSelectionRequestDTO;
import com.chidituke.workout_tracker.dto.response.exercise.ExerciseFiltersDTO;
import com.chidituke.workout_tracker.exceptions.exercise.ExerciseNotFoundException;
import com.chidituke.workout_tracker.exceptions.exercise.InvalidExerciseDataException;
import com.chidituke.workout_tracker.exceptions.user.ProfessionalVerificationException;
import com.chidituke.workout_tracker.exceptions.user.UserNotFoundException;
import com.chidituke.workout_tracker.exceptions.common.UnauthorizedOperationException;
import com.chidituke.workout_tracker.mapper.workout.ExerciseMapper;
import com.chidituke.workout_tracker.model.workout.Exercise;
import com.chidituke.workout_tracker.model.workout.UserExerciseRating;
import com.chidituke.workout_tracker.model.workout.UserExerciseHistory;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.repository.workout.ExerciseRepository;
import com.chidituke.workout_tracker.repository.workout.UserExerciseRatingRepository;
import com.chidituke.workout_tracker.repository.workout.UserExerciseHistoryRepository;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Facade service for backward compatibility.
 * Delegates to specialized services for clean architecture.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseService {

    private final ExerciseUserService exerciseUserService;
    private final ExerciseQueryService exerciseQueryService;

    // ==================== USER OPERATIONS (delegate to ExerciseUserService) ====================

    @Transactional
    public void rateExercise(Long exerciseId, User user, double rating, String comment, List<String> tags) {
        exerciseUserService.rateExercise(exerciseId, user, rating, comment, tags);
    }

    @Transactional
    public void rateExercise(Long exerciseId, User user, double rating) {
        exerciseUserService.rateExercise(exerciseId, user, rating);
    }

    @Transactional
    public void recordWorkoutUsage(Long exerciseId, User user, Integer durationMinutes, String notes) {
        exerciseUserService.recordWorkoutUsage(exerciseId, user, durationMinutes, notes);
    }

    @Transactional
    public void removeExerciseFromFavorites(Long exerciseId, String username) {
        exerciseUserService.removeExerciseFromFavorites(exerciseId, username);
    }

    public boolean isExerciseCreatedByUser(Long exerciseId, Long userId) {
        return exerciseUserService.isExerciseCreatedByUser(exerciseId, userId);
    }

    public List<Exercise> findSuitableExercises(User user, List<String> availableEquipment,
                                                Exercise.DifficultyLevel maxDifficulty) {
        return exerciseUserService.findSuitableExercises(user, availableEquipment, maxDifficulty);
    }

    // ==================== QUERY OPERATIONS (delegate to ExerciseQueryService) ====================

    public Page<Exercise> searchExercises(String searchTerm, List<String> muscleGroups,
                                          List<String> equipment, Exercise.DifficultyLevel difficulty,
                                          Pageable pageable) {
        return exerciseQueryService.searchExercises(searchTerm, muscleGroups, equipment, difficulty, pageable);
    }
}