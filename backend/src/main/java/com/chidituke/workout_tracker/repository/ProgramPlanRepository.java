package com.chidituke.workout_tracker.repository;

import com.chidituke.workout_tracker.model.ProgramPlan;
import com.chidituke.workout_tracker.model.WorkoutPlan;
import com.chidituke.workout_tracker.model.WorkoutProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramPlanRepository extends JpaRepository<ProgramPlan, Long> {

    // ===== EXISTING METHODS (KEEP THESE) =====

    // Core Queries
    List<ProgramPlan> findByProgramIdOrderByWeekNumberAscDayNumberAsc(Long programId);

    List<ProgramPlan> findByProgramAndWeekNumberOrderByDayNumberAsc(WorkoutProgram program, Integer weekNumber);

    Optional<ProgramPlan> findByProgramAndWeekNumberAndDayNumber(
            WorkoutProgram program, Integer weekNumber, Integer dayNumber);

    // Program Structure Queries
    @Query("SELECT DISTINCT pp.weekNumber FROM ProgramPlan pp WHERE pp.program.id = :programId ORDER BY pp.weekNumber")
    List<Integer> findWeekNumbersByProgramId(@Param("programId") Long programId);

    @Query("SELECT COUNT(pp) FROM ProgramPlan pp WHERE pp.program.id = :programId")
    Long countByProgramId(@Param("programId") Long programId);

    @Query("SELECT COUNT(pp) FROM ProgramPlan pp WHERE pp.program.id = :programId AND pp.isRestDay = false")
    Long countWorkoutDaysByProgramId(@Param("programId") Long programId);

    // Workout Plan Usage
    List<ProgramPlan> findByWorkoutPlan(WorkoutPlan workoutPlan);

    @Query("SELECT COUNT(pp) FROM ProgramPlan pp WHERE pp.workoutPlan.id = :workoutPlanId")
    Long countUsageByWorkoutPlanId(@Param("workoutPlanId") Long workoutPlanId);

    // Phase and Intensity Queries
    List<ProgramPlan> findByProgramAndPhaseType(WorkoutProgram program, ProgramPlan.PhaseType phaseType);

    @Query("SELECT pp FROM ProgramPlan pp WHERE pp.program.id = :programId AND pp.targetIntensity >= :minIntensity ORDER BY pp.targetIntensity DESC")
    List<ProgramPlan> findHighIntensityWorkouts(@Param("programId") Long programId,
                                                @Param("minIntensity") BigDecimal minIntensity);

    // Validation Queries
    @Query("SELECT CASE WHEN COUNT(pp) > 0 THEN true ELSE false END FROM ProgramPlan pp WHERE pp.program.id = :programId AND pp.weekNumber = :weekNumber AND pp.dayNumber = :dayNumber AND pp.id != :excludeId")
    boolean existsConflictingSchedule(@Param("programId") Long programId,
                                      @Param("weekNumber") Integer weekNumber,
                                      @Param("dayNumber") Integer dayNumber,
                                      @Param("excludeId") Long excludeId);

    // Rest Day Management
    @Query("SELECT pp FROM ProgramPlan pp WHERE pp.program.id = :programId AND pp.isRestDay = true ORDER BY pp.weekNumber, pp.dayNumber")
    List<ProgramPlan> findRestDaysByProgram(@Param("programId") Long programId);

    // Optional Workouts
    List<ProgramPlan> findByProgramAndIsOptionalTrue(WorkoutProgram program);

    // Bulk Operations
    @Modifying
    @Query("UPDATE ProgramPlan pp SET pp.displayOrder = :newOrder WHERE pp.id = :id")
    void updateDisplayOrder(@Param("id") Long id, @Param("newOrder") Integer newOrder);

    @Modifying
    @Query("DELETE FROM ProgramPlan pp WHERE pp.program.id = :programId")
    void deleteByProgramId(@Param("programId") Long programId);

    // Analytics
    @Query("SELECT AVG(pp.targetIntensity) FROM ProgramPlan pp WHERE pp.program.id = :programId AND pp.isRestDay = false")
    Optional<BigDecimal> findAverageIntensityByProgram(@Param("programId") Long programId);

    @Query("SELECT pp.phaseType, COUNT(pp) FROM ProgramPlan pp WHERE pp.program.id = :programId GROUP BY pp.phaseType")
    List<Object[]> getPhaseDistribution(@Param("programId") Long programId);

    // ===== MISSING METHODS NEEDED BY SERVICE =====

    // Alternative method names that service expects
    @Query("SELECT pp FROM ProgramPlan pp WHERE pp.program = :program ORDER BY pp.weekNumber ASC, pp.dayNumber ASC, pp.displayOrder ASC")
    List<ProgramPlan> findByProgramOrderByWeekNumberAscDayOfWeekAscOrderInWeekAsc(@Param("program") WorkoutProgram program);

    @Query("SELECT pp FROM ProgramPlan pp WHERE pp.program = :program AND pp.weekNumber = :weekNumber ORDER BY pp.dayNumber ASC, pp.displayOrder ASC")
    List<ProgramPlan> findByProgramAndWeekNumberOrderByDayOfWeekAscOrderInWeekAsc(
            @Param("program") WorkoutProgram program,
            @Param("weekNumber") Integer weekNumber);

    // Simple findByProgram method
    List<ProgramPlan> findByProgram(WorkoutProgram program);

    // Count methods service needs
    @Query("SELECT COUNT(pp) FROM ProgramPlan pp WHERE pp.program = :program")
    Long countByProgram(@Param("program") WorkoutProgram program);

    // Weekly workout count for analytics
    @Query("SELECT pp.weekNumber, COUNT(pp) FROM ProgramPlan pp WHERE pp.program = :program AND pp.isRestDay = false GROUP BY pp.weekNumber ORDER BY pp.weekNumber")
    List<Object[]> countWorkoutsByWeek(@Param("program") WorkoutProgram program);

    // Program structure overview
    @Query("SELECT pp.weekNumber, pp.dayNumber, pp.workoutPlan.name, pp.isRestDay, pp.phaseType FROM ProgramPlan pp WHERE pp.program = :program ORDER BY pp.weekNumber, pp.dayNumber")
    List<Object[]> getProgramStructure(@Param("program") WorkoutProgram program);

    // Additional useful methods for the service
    @Query("SELECT pp FROM ProgramPlan pp WHERE pp.program = :program AND pp.weekNumber = :weekNumber")
    List<ProgramPlan> findByProgramAndWeekNumber(@Param("program") WorkoutProgram program, @Param("weekNumber") Integer weekNumber);

    @Query("SELECT MAX(pp.weekNumber) FROM ProgramPlan pp WHERE pp.program = :program")
    Optional<Integer> findMaxWeekNumberByProgram(@Param("program") WorkoutProgram program);

    @Query("SELECT COUNT(DISTINCT pp.weekNumber) FROM ProgramPlan pp WHERE pp.program = :program")
    Long countDistinctWeeksByProgram(@Param("program") WorkoutProgram program);

    // Program completion tracking
    @Query("SELECT pp FROM ProgramPlan pp WHERE pp.program = :program AND pp.isRestDay = false ORDER BY pp.weekNumber, pp.dayNumber")
    List<ProgramPlan> findWorkoutPlansByProgram(@Param("program") WorkoutProgram program);

    @Query("SELECT COUNT(pp) FROM ProgramPlan pp WHERE pp.program = :program AND pp.weekNumber <= :weekNumber AND pp.isRestDay = false")
    Long countWorkoutsUpToWeek(@Param("program") WorkoutProgram program, @Param("weekNumber") Integer weekNumber);

    // For program management
    @Query("SELECT pp FROM ProgramPlan pp WHERE pp.program = :program ORDER BY pp.weekNumber, pp.dayNumber, pp.displayOrder")
    List<ProgramPlan> findByProgramOrderedBySchedule(@Param("program") WorkoutProgram program);

    // Batch operations
    @Modifying
    @Query("DELETE FROM ProgramPlan pp WHERE pp.program = :program")
    void deleteByProgram(@Param("program") WorkoutProgram program);

    @Modifying
    @Query("DELETE FROM ProgramPlan pp WHERE pp.program = :program AND pp.weekNumber = :weekNumber")
    void deleteByProgramAndWeekNumber(@Param("program") WorkoutProgram program, @Param("weekNumber") Integer weekNumber);
}