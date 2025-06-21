package com.chidituke.workout_tracker.service;

import com.chidituke.workout_tracker.dto.request.workout_program.WorkoutProgramRequest;
import com.chidituke.workout_tracker.dto.request.workout_program.ProgramEnrollmentRequest;
import com.chidituke.workout_tracker.dto.response.workout_program.WorkoutProgramResponse;
import com.chidituke.workout_tracker.dto.response.workout_program.ProgramEnrollmentResponse;
import com.chidituke.workout_tracker.dto.response.workout_program.ProgramProgressResponse;
import com.chidituke.workout_tracker.dto.response.workout_program.ProgramAnalyticsResponse;
import com.chidituke.workout_tracker.exceptions.user.UserNotFoundException;
import com.chidituke.workout_tracker.exceptions.user.ProfessionalVerificationException;
import com.chidituke.workout_tracker.exceptions.workout_program.WorkoutProgramNotFoundException;
import com.chidituke.workout_tracker.exceptions.common.UnauthorizedOperationException;
import com.chidituke.workout_tracker.mapper.WorkoutProgramMapper;
import com.chidituke.workout_tracker.model.*;
import com.chidituke.workout_tracker.model.WorkoutProgram.ProgramType;
import com.chidituke.workout_tracker.model.WorkoutProgram.DifficultyLevel;
import com.chidituke.workout_tracker.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutProgramService {

    private final WorkoutProgramRepository workoutProgramRepository;
    private final UserRepository userRepository;
    private final ProgramPlanRepository programPlanRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final ScheduledWorkoutRepository scheduledWorkoutRepository;
    private final WorkoutProgramMapper workoutProgramMapper;

    // =======================
    // PROGRAM DISCOVERY & SEARCH
    // =======================

    @Cacheable(value = "public-programs", unless = "#result.isEmpty()")
    public List<WorkoutProgramResponse> getAllPublishedPrograms() {
        List<WorkoutProgram> programs = workoutProgramRepository
                .findByIsPublishedTrueAndIsPublicTrueOrderByEnrollmentCountDesc();
        return workoutProgramMapper.toResponseList(programs);
    }

    public Optional<WorkoutProgramResponse> getProgramById(Long id) {
        return workoutProgramRepository.findById(id)
                .filter(program -> program.getIsPublished() && program.getIsPublic())
                .map(workoutProgramMapper::toResponse);
    }

    public Optional<WorkoutProgramResponse> getProgramById(Long id, String username) {
        Optional<WorkoutProgram> program = workoutProgramRepository.findById(id);

        if (program.isEmpty()) {
            return Optional.empty();
        }

        WorkoutProgram prog = program.get();

        // Allow access if published and public OR if user created it
        if ((prog.getIsPublished() && prog.getIsPublic()) || isCreator(prog, username)) {
            return Optional.of(workoutProgramMapper.toResponse(prog));
        }

        return Optional.empty();
    }

    public List<WorkoutProgramResponse> getProgramsByType(ProgramType programType) {
        List<WorkoutProgram> programs = workoutProgramRepository
                .findByProgramTypeAndIsPublishedTrueAndIsPublicTrue(programType);
        return workoutProgramMapper.toResponseList(programs);
    }

    public List<WorkoutProgramResponse> getProgramsByDifficulty(DifficultyLevel difficultyLevel) {
        List<WorkoutProgram> programs = workoutProgramRepository
                .findByDifficultyLevelAndIsPublishedTrueAndIsPublicTrue(difficultyLevel);
        return workoutProgramMapper.toResponseList(programs);
    }

    public List<WorkoutProgramResponse> getProgramsByDuration(Integer minWeeks, Integer maxWeeks) {
        List<WorkoutProgram> programs = workoutProgramRepository
                .findByDurationWeeksBetweenAndIsPublishedTrueAndIsPublicTrue(minWeeks, maxWeeks);
        return workoutProgramMapper.toResponseList(programs);
    }

    public Page<WorkoutProgramResponse> searchPrograms(String searchTerm, Pageable pageable) {
        Page<WorkoutProgram> programs = workoutProgramRepository
                .findByNameContainingIgnoreCaseAndIsPublishedTrueAndIsPublicTrue(searchTerm, pageable);
        return programs.map(workoutProgramMapper::toResponse);
    }

    public Page<WorkoutProgramResponse> searchProgramsWithFilters(
            ProgramType programType, DifficultyLevel difficultyLevel,
            Integer minWeeks, Integer maxWeeks, Integer minSessions, Integer maxSessions,
            Pageable pageable) {

        Page<WorkoutProgram> programs = workoutProgramRepository.findWithFilters(
                programType, difficultyLevel, minWeeks, maxWeeks, minSessions, maxSessions, pageable);
        return programs.map(workoutProgramMapper::toResponse);
    }

    // =======================
    // POPULAR & RECOMMENDED PROGRAMS
    // =======================

    @Cacheable(value = "popular-programs", key = "#limit")
    public List<WorkoutProgramResponse> getMostPopularPrograms(int limit) {
        List<WorkoutProgram> programs = workoutProgramRepository
                .findTop10ByIsPublishedTrueAndIsPublicTrueOrderByEnrollmentCountDesc();
        return programs.stream()
                .limit(limit)
                .map(workoutProgramMapper::toResponse)
                .toList();
    }

    @Cacheable(value = "highly-rated-programs", key = "#minRating + '-' + #minReviews")
    public List<WorkoutProgramResponse> getHighlyRatedPrograms(Double minRating, Integer minReviews) {
        List<WorkoutProgram> programs = workoutProgramRepository
                .findHighlyRatedPrograms(minRating, minReviews);
        return workoutProgramMapper.toResponseList(programs);
    }

    public List<WorkoutProgramResponse> getTrendingPrograms(Integer minEnrollment) {
        List<WorkoutProgram> programs = workoutProgramRepository.findTrendingPrograms(minEnrollment);
        return workoutProgramMapper.toResponseList(programs);
    }

    public List<WorkoutProgramResponse> getHighCompletionRatePrograms(Double minCompletionRate) {
        List<WorkoutProgram> programs = workoutProgramRepository
                .findProgramsWithHighCompletionRate(minCompletionRate);
        return workoutProgramMapper.toResponseList(programs);
    }

    // =======================
    // PROFESSIONAL PROGRAM CREATION
    // =======================

    @Transactional
    @CacheEvict(value = {"public-programs", "popular-programs"}, allEntries = true)
    public WorkoutProgramResponse createProgram(String username, WorkoutProgramRequest request) {
        User user = findUserByUsername(username);
        validateProfessionalCanCreatePrograms(user);

        WorkoutProgram program = new WorkoutProgram();
        program.setCreatedByUserId(user.getId());
        program.setCreatedByProfessional(user.hasRole("PROFESSIONAL"));

        // Map request to entity
        workoutProgramMapper.mapRequestToEntity(request, program);

        // Professional programs require approval
        if (user.hasRole("PROFESSIONAL")) {
            program.setIsPublished(false); // Requires admin approval
        }

        WorkoutProgram saved = workoutProgramRepository.save(program);

        log.info("Workout program created: '{}' by user {} (professional: {})",
                saved.getName(), username, user.hasRole("PROFESSIONAL"));

        return workoutProgramMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = {"public-programs", "popular-programs"}, allEntries = true)
    public WorkoutProgramResponse updateProgram(Long id, String username, WorkoutProgramRequest request) {
        WorkoutProgram program = findProgramById(id);
        validateCreatorAccess(program, username);

        // Map request to entity
        workoutProgramMapper.mapRequestToEntity(request, program);

        WorkoutProgram saved = workoutProgramRepository.save(program);

        log.info("Workout program updated: {} by user {}", id, username);

        return workoutProgramMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = {"public-programs", "popular-programs"}, allEntries = true)
    public void deleteProgram(Long id, String username) {
        WorkoutProgram program = findProgramById(id);
        validateCreatorAccess(program, username);

        // Check if program has any enrollments/sessions
        long sessionCount = workoutSessionRepository.countByProgram(program);
        if (sessionCount > 0) {
            throw new IllegalStateException(
                    "Cannot delete program that has active enrollments or completed sessions");
        }

        // Delete program plans first (FIXED: renamed from programWorkouts to programPlans)
        List<ProgramPlan> programPlans = programPlanRepository.findByProgram(program);
        programPlanRepository.deleteAll(programPlans);

        workoutProgramRepository.delete(program);

        log.info("Workout program deleted: {} by user {}", id, username);
    }

    // =======================
    // PROGRAM ENROLLMENT & TRACKING
    // =======================

    @Transactional
    public ProgramEnrollmentResponse enrollInProgram(String username, Long programId,
                                                     ProgramEnrollmentRequest request) {
        User user = findUserByUsername(username);
        WorkoutProgram program = findProgramById(programId);

        // Verify program is accessible
        if (!program.getIsPublished() || !program.getIsPublic()) {
            throw new IllegalArgumentException("Program is not available for enrollment");
        }

        // Check if user is already enrolled (has recent sessions)
        LocalDate cutoffDate = LocalDate.now().minusWeeks(program.getDurationWeeks() + 4);
        boolean alreadyEnrolled = workoutSessionRepository.existsByUserAndProgramAndDateAfter(
                user, program, cutoffDate);

        if (alreadyEnrolled) {
            throw new IllegalStateException("User is already enrolled in this program");
        }

        // Increment enrollment count
        program.incrementEnrollment();
        workoutProgramRepository.save(program);

        // Create initial program tracking record (this could be a separate ProgramEnrollment entity)
        log.info("User {} enrolled in program {}", username, program.getName());

        return ProgramEnrollmentResponse.builder()
                .programId(programId)
                .programName(program.getName())
                .enrollmentDate(LocalDate.now())
                .durationWeeks(program.getDurationWeeks())
                .sessionsPerWeek(program.getSessionsPerWeek())
                .estimatedCompletionDate(LocalDate.now().plusWeeks(program.getDurationWeeks()))
                .status("ENROLLED")
                .build();
    }

    public ProgramProgressResponse getProgramProgress(String username, Long programId) {
        User user = findUserByUsername(username);
        WorkoutProgram program = findProgramById(programId);

        // Get user's sessions for this program
        List<WorkoutSession> programSessions = workoutSessionRepository
                .findByUserAndProgramOrderByWeekNumberAscDateAsc(user, program);

        Long completedWeeks = workoutSessionRepository
                .countCompletedWeeksByUserAndProgram(user, program);

        Optional<Integer> currentWeek = workoutSessionRepository
                .findMaxWeekNumberByUserAndProgram(user, program);

        // Calculate completion percentage
        double progressPercentage = (completedWeeks.doubleValue() / program.getDurationWeeks()) * 100;
        boolean isCompleted = completedWeeks >= program.getDurationWeeks();

        // Get next scheduled workout
        LocalDate nextWorkoutDate = findNextScheduledWorkout(user, program);

        return ProgramProgressResponse.builder()
                .programId(programId)
                .programName(program.getName())
                .totalWeeks(program.getDurationWeeks())
                .completedWeeks(completedWeeks.intValue())
                .currentWeek(currentWeek.orElse(0))
                .progressPercentage(Math.min(progressPercentage, 100.0))
                .isCompleted(isCompleted)
                .totalSessions(programSessions.size())
                .nextWorkoutDate(nextWorkoutDate)
                .estimatedCompletionDate(calculateEstimatedCompletion(user, program))
                .build();
    }

    @Transactional
    public void markProgramCompleted(String username, Long programId) {
        User user = findUserByUsername(username);
        WorkoutProgram program = findProgramById(programId);

        // Verify user has completed enough of the program
        Long completedWeeks = workoutSessionRepository
                .countCompletedWeeksByUserAndProgram(user, program);

        if (completedWeeks < program.getDurationWeeks()) {
            throw new IllegalStateException("Program is not yet completed");
        }

        // Increment program completion count
        program.incrementCompletion();
        workoutProgramRepository.save(program);

        log.info("User {} completed program {}", username, program.getName());
    }

    // =======================
    // PROGRAM STRUCTURE MANAGEMENT (FIXED METHOD CALLS)
    // =======================

    public List<ProgramPlan> getProgramStructure(Long programId) {
        WorkoutProgram program = findProgramById(programId);
        return programPlanRepository.findByProgramOrderByWeekNumberAscDayOfWeekAscOrderInWeekAsc(program);
    }

    public List<ProgramPlan> getProgramWeekStructure(Long programId, Integer weekNumber) {
        WorkoutProgram program = findProgramById(programId);
        return programPlanRepository.findByProgramAndWeekNumberOrderByDayOfWeekAscOrderInWeekAsc(
                program, weekNumber);
    }

    public Map<String, Object> getProgramScheduleOverview(Long programId) {
        WorkoutProgram program = findProgramById(programId);

        List<Object[]> weekCounts = programPlanRepository.countWorkoutsByWeek(program);
        List<Object[]> programStructure = programPlanRepository.getProgramStructure(program);
        long totalWorkouts = programPlanRepository.countByProgram(program);

        return Map.of(
                "programId", programId,
                "programName", program.getName(),
                "totalWeeks", program.getDurationWeeks(),
                "totalWorkouts", totalWorkouts,
                "weeklyWorkoutCounts", weekCounts,
                "programStructure", programStructure
        );
    }

    // =======================
    // PROFESSIONAL FEATURES
    // =======================

    public List<WorkoutProgramResponse> getProfessionalPrograms(String username) {
        User user = findUserByUsername(username);
        validateProfessional(user);

        List<WorkoutProgram> programs = workoutProgramRepository
                .findByCreatedByUserIdAndCreatedByProfessionalTrueOrderByCreatedAtDesc(user.getId());
        return workoutProgramMapper.toResponseList(programs);
    }

    public List<WorkoutProgramResponse> getProfessionalPublishedPrograms(String username) {
        User user = findUserByUsername(username);
        validateProfessional(user);

        List<WorkoutProgram> programs = workoutProgramRepository
                .findByCreatedByUserIdAndCreatedByProfessionalTrueAndIsPublishedTrue(user.getId());
        return workoutProgramMapper.toResponseList(programs);
    }

    public long getProfessionalProgramCount(String username) {
        User user = findUserByUsername(username);
        validateProfessional(user);

        return workoutProgramRepository.countByCreatedByUserIdAndCreatedByProfessionalTrue(user.getId());
    }

    // =======================
    // ADMIN FEATURES
    // =======================

    @Transactional
    @CacheEvict(value = {"public-programs", "popular-programs"}, allEntries = true)
    public void approveProgram(Long programId, String adminUsername) {
        WorkoutProgram program = findProgramById(programId);
        User admin = findUserByUsername(adminUsername);

        validateAdminAccess(admin);

        program.setIsPublished(true);
        workoutProgramRepository.save(program);

        log.info("Program approved: {} by admin {}", programId, adminUsername);
    }

    public List<WorkoutProgramResponse> getProgramsAwaitingReview() {
        List<WorkoutProgram> programs = workoutProgramRepository.findProgramsAwaitingReview();
        return workoutProgramMapper.toResponseList(programs);
    }

    // =======================
    // ANALYTICS & STATISTICS
    // =======================

    public ProgramAnalyticsResponse getProgramAnalytics(Long programId, String username) {
        WorkoutProgram program = findProgramById(programId);

        // Only allow analytics for creators or admins
        if (!isCreator(program, username)) {
            User user = findUserByUsername(username);
            if (!user.hasRole("ADMIN")) {
                throw new UnauthorizedOperationException("Cannot access analytics for this program");
            }
        }

        return ProgramAnalyticsResponse.builder()
                .programId(programId)
                .programName(program.getName())
                .enrollmentCount(program.getEnrollmentCount())
                .completionCount(program.getCompletionCount())
                .completionRate(program.getCompletionRate())
                .averageRating(program.getAverageRating())
                .totalRatings(program.getTotalRatings())
                .isPopular(program.isPopular())
                .isHighlyRated(program.isHighlyRated())
                .build();
    }

    public Map<String, Object> getOverallProgramStatistics() {
        List<Object[]> typeStats = workoutProgramRepository.countByProgramType();
        List<Object[]> averageStats = workoutProgramRepository.getAverageStatistics();

        return Map.of(
                "programTypeCounts", typeStats,
                "averageStatistics", averageStats,
                "totalPrograms", workoutProgramRepository.count()
        );
    }

    // =======================
    // HELPER METHODS
    // =======================

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    private WorkoutProgram findProgramById(Long id) {
        return workoutProgramRepository.findById(id)
                .orElseThrow(() -> new WorkoutProgramNotFoundException(id));
    }

    private boolean isCreator(WorkoutProgram program, String username) {
        if (program.getCreatedByUserId() == null) {
            return false;
        }

        User user = findUserByUsername(username);
        return program.getCreatedByUserId().equals(user.getId());
    }

    private void validateCreatorAccess(WorkoutProgram program, String username) {
        if (!isCreator(program, username)) {
            throw new UnauthorizedOperationException("User did not create this program");
        }
    }

    private void validateProfessional(User user) {
        if (!user.hasRole("PROFESSIONAL") && !user.hasRole("ADMIN")) {
            throw new ProfessionalVerificationException("Professional role required");
        }
    }

    private void validateProfessionalCanCreatePrograms(User user) {
        if (!user.hasRole("PROFESSIONAL") && !user.hasRole("ADMIN")) {
            throw new ProfessionalVerificationException("create workout programs");
        }
    }

    private void validateAdminAccess(User user) {
        if (!user.hasRole("ADMIN")) {
            throw new UnauthorizedOperationException("Admin role required");
        }
    }

    private LocalDate findNextScheduledWorkout(User user, WorkoutProgram program) {
        List<ScheduledWorkout> upcomingWorkouts = scheduledWorkoutRepository
                .findByUserAndProgramAndStatusOrderByScheduledDateAsc(
                        user, program, ScheduledWorkout.ScheduleStatus.SCHEDULED);

        return upcomingWorkouts.isEmpty() ? null : upcomingWorkouts.get(0).getScheduledDate();
    }

    private LocalDate calculateEstimatedCompletion(User user, WorkoutProgram program) {
        // Get user's current progress
        Optional<Integer> currentWeek = workoutSessionRepository
                .findMaxWeekNumberByUserAndProgram(user, program);

        if (currentWeek.isEmpty()) {
            return LocalDate.now().plusWeeks(program.getDurationWeeks());
        }

        int remainingWeeks = program.getDurationWeeks() - currentWeek.get();
        return LocalDate.now().plusWeeks(Math.max(remainingWeeks, 0));
    }

    public boolean programExists(Long id) {
        return workoutProgramRepository.existsById(id);
    }

    @Transactional
    public void updateEnrollmentCount(Long programId) {
        workoutProgramRepository.incrementEnrollmentCount(programId);
    }

    @Transactional
    public void updateCompletionCount(Long programId) {
        workoutProgramRepository.incrementCompletionCount(programId);
    }
}