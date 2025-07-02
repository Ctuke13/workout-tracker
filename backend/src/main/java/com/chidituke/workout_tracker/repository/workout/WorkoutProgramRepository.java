package com.chidituke.workout_tracker.repository.workout;

import com.chidituke.workout_tracker.model.workout.WorkoutProgram;
import com.chidituke.workout_tracker.model.workout.WorkoutProgram.ProgramType;
import com.chidituke.workout_tracker.model.workout.WorkoutProgram.DifficultyLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutProgramRepository extends JpaRepository<WorkoutProgram, Long> {

    // =======================
    // PRO USER CREATION ENFORCEMENT
    // =======================

    // Find programs created by professional users only
    List<WorkoutProgram> findByCreatedByProfessionalTrueAndIsPublishedTrueOrderByCreatedAtDesc();

    // Verify user can create programs (should be PRO tier)
    @Query("SELECT COUNT(wp) > 0 FROM WorkoutProgram wp WHERE wp.createdByUserId = :userId")
    boolean hasCreatedPrograms(@Param("userId") Long userId);

    // =======================
    // PUBLIC PROGRAM DISCOVERY
    // =======================

    // Find all published public programs
    List<WorkoutProgram> findByIsPublishedTrueAndIsPublicTrueOrderByEnrollmentCountDesc();

    // Find programs by type
    List<WorkoutProgram> findByProgramTypeAndIsPublishedTrueAndIsPublicTrue(ProgramType programType);

    // Find programs by difficulty
    List<WorkoutProgram> findByDifficultyLevelAndIsPublishedTrueAndIsPublicTrue(DifficultyLevel difficultyLevel);

    // Search programs by name
    Page<WorkoutProgram> findByNameContainingIgnoreCaseAndIsPublishedTrueAndIsPublicTrue(String name, Pageable pageable);

    // =======================
    // PROGRAM FILTERING
    // =======================

    // Find programs by duration range
    List<WorkoutProgram> findByDurationWeeksBetweenAndIsPublishedTrueAndIsPublicTrue(Integer minWeeks, Integer maxWeeks);

    // Find programs by sessions per week
    List<WorkoutProgram> findBySessionsPerWeekAndIsPublishedTrueAndIsPublicTrue(Integer sessionsPerWeek);

    // Multi-filter search
    @Query("SELECT wp FROM WorkoutProgram wp WHERE wp.isPublished = true AND wp.isPublic = true " +
            "AND (:programType IS NULL OR wp.programType = :programType) " +
            "AND (:difficultyLevel IS NULL OR wp.difficultyLevel = :difficultyLevel) " +
            "AND (:minWeeks IS NULL OR wp.durationWeeks >= :minWeeks) " +
            "AND (:maxWeeks IS NULL OR wp.durationWeeks <= :maxWeeks) " +
            "AND (:minSessions IS NULL OR wp.sessionsPerWeek >= :minSessions) " +
            "AND (:maxSessions IS NULL OR wp.sessionsPerWeek <= :maxSessions) " +
            "ORDER BY wp.enrollmentCount DESC")
    Page<WorkoutProgram> findWithFilters(@Param("programType") ProgramType programType,
                                         @Param("difficultyLevel") DifficultyLevel difficultyLevel,
                                         @Param("minWeeks") Integer minWeeks,
                                         @Param("maxWeeks") Integer maxWeeks,
                                         @Param("minSessions") Integer minSessions,
                                         @Param("maxSessions") Integer maxSessions,
                                         Pageable pageable);

    // =======================
    // PROFESSIONAL CREATOR QUERIES
    // =======================

    // Find programs by professional creator
    List<WorkoutProgram> findByCreatedByUserIdAndCreatedByProfessionalTrueOrderByCreatedAtDesc(Long professionalUserId);

    // Find professional's published programs
    List<WorkoutProgram> findByCreatedByUserIdAndCreatedByProfessionalTrueAndIsPublishedTrue(Long professionalUserId);

    // Count programs created by professional
    long countByCreatedByUserIdAndCreatedByProfessionalTrue(Long professionalUserId);

    // =======================
    // ENROLLMENT & POPULARITY
    // =======================

    // Find most popular programs
    List<WorkoutProgram> findTop10ByIsPublishedTrueAndIsPublicTrueOrderByEnrollmentCountDesc();

    // Find highly rated programs
    @Query("SELECT wp FROM WorkoutProgram wp WHERE wp.isPublished = true AND wp.isPublic = true " +
            "AND wp.averageRating >= :minRating AND wp.totalRatings >= :minReviews " +
            "ORDER BY wp.averageRating DESC")
    List<WorkoutProgram> findHighlyRatedPrograms(@Param("minRating") Double minRating,
                                                 @Param("minReviews") Integer minReviews);

    // Find trending programs (high enrollment recently)
    @Query("SELECT wp FROM WorkoutProgram wp WHERE wp.isPublished = true AND wp.isPublic = true " +
            "AND wp.enrollmentCount >= :minEnrollment ORDER BY wp.enrollmentCount DESC")
    List<WorkoutProgram> findTrendingPrograms(@Param("minEnrollment") Integer minEnrollment);

    // =======================
    // PROGRAM ANALYTICS
    // =======================

    // Programs with high completion rates
    @Query("SELECT wp FROM WorkoutProgram wp WHERE wp.isPublished = true " +
            "AND wp.enrollmentCount > 0 " +
            "AND (CAST(wp.completionCount AS double) / wp.enrollmentCount) >= :minCompletionRate")
    List<WorkoutProgram> findProgramsWithHighCompletionRate(@Param("minCompletionRate") Double minCompletionRate);

    // Count programs by type
    @Query("SELECT wp.programType, COUNT(wp) FROM WorkoutProgram wp " +
            "WHERE wp.isPublished = true AND wp.isPublic = true GROUP BY wp.programType")
    List<Object[]> countByProgramType();

    // Average program statistics
    @Query("SELECT AVG(wp.durationWeeks), AVG(wp.sessionsPerWeek), AVG(wp.enrollmentCount) " +
            "FROM WorkoutProgram wp WHERE wp.isPublished = true AND wp.isPublic = true")
    List<Object[]> getAverageStatistics();

    // =======================
    // PROGRAM MANAGEMENT
    // =======================

    // Update enrollment count
    @Query("UPDATE WorkoutProgram wp SET wp.enrollmentCount = wp.enrollmentCount + 1 WHERE wp.id = :programId")
    void incrementEnrollmentCount(@Param("programId") Long programId);

    // Update completion count
    @Query("UPDATE WorkoutProgram wp SET wp.completionCount = wp.completionCount + 1 WHERE wp.id = :programId")
    void incrementCompletionCount(@Param("programId") Long programId);

    // Find programs needing review (newly created by professionals)
    @Query("SELECT wp FROM WorkoutProgram wp WHERE wp.createdByProfessional = true " +
            "AND wp.isPublished = false ORDER BY wp.createdAt DESC")
    List<WorkoutProgram> findProgramsAwaitingReview();
}