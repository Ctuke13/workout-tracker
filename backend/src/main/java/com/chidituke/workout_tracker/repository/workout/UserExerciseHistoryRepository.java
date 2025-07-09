package com.chidituke.workout_tracker.repository.workout;

import com.chidituke.workout_tracker.model.workout.UserExerciseHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserExerciseHistoryRepository extends JpaRepository<UserExerciseHistory, Long> {

    // 🎯 CORE QUERIES
    List<UserExerciseHistory> findByUserId(Long userId);

    List<UserExerciseHistory> findByUserIdOrderByUsedAtDesc(Long userId);

    List<UserExerciseHistory> findByExerciseId(Long exerciseId);

    Page<UserExerciseHistory> findByUserIdOrderByUsedAtDesc(Long userId, Pageable pageable);

    // ⏰ TIME-BASED QUERIES
    @Query("SELECT h FROM UserExerciseHistory h WHERE h.user.id = :userId AND h.usedAt >= :since ORDER BY h.usedAt DESC")
    List<UserExerciseHistory> findUserRecentHistory(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT h FROM UserExerciseHistory h WHERE h.user.id = :userId AND h.usedAt >= :since AND h.context = :context ORDER BY h.usedAt DESC")
    List<UserExerciseHistory> findUserRecentHistoryByContext(@Param("userId") Long userId, @Param("since") LocalDateTime since, @Param("context") String context);

    // 💪 MUSCLE GROUP PREFERENCES (for recommendations)
    @Query("SELECT mg, COUNT(h) FROM UserExerciseHistory h " +
            "JOIN h.exercise.targetMuscleGroups mg " +
            "WHERE h.user.id = :userId AND h.usedAt >= :since " +
            "GROUP BY mg ORDER BY COUNT(h) DESC")
    List<Object[]> getUserPreferredMuscleGroups(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT h.exercise.exerciseType, COUNT(h) FROM UserExerciseHistory h " +
            "WHERE h.user.id = :userId AND h.usedAt >= :since " +
            "GROUP BY h.exercise.exerciseType ORDER BY COUNT(h) DESC")
    List<Object[]> getUserPreferredExerciseTypes(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    // 🏋️ WORKOUT PATTERNS
    @Query("SELECT h FROM UserExerciseHistory h WHERE h.user.id = :userId AND h.context = 'workout' ORDER BY h.usedAt DESC")
    List<UserExerciseHistory> findUserWorkoutHistory(@Param("userId") Long userId);

    @Query("SELECT COUNT(h) FROM UserExerciseHistory h WHERE h.user.id = :userId AND h.context = 'workout' AND h.usedAt >= :since")
    Long countUserWorkouts(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    // 📊 ANALYTICS
    @Query("SELECT DATE(h.usedAt), COUNT(h) FROM UserExerciseHistory h " +
            "WHERE h.user.id = :userId AND h.usedAt >= :since " +
            "GROUP BY DATE(h.usedAt) ORDER BY DATE(h.usedAt)")
    List<Object[]> getUserActivityByDate(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT h.exercise.id, COUNT(h) FROM UserExerciseHistory h " +
            "WHERE h.user.id = :userId " +
            "GROUP BY h.exercise.id ORDER BY COUNT(h) DESC")
    List<Object[]> getUserMostUsedExercises(@Param("userId") Long userId);

    // 🔍 CONTEXT-BASED QUERIES
    List<UserExerciseHistory> findByUserIdAndContext(Long userId, String context);

    @Query("SELECT COUNT(h) FROM UserExerciseHistory h WHERE h.user.id = :userId AND h.context = :context")
    Long countByUserIdAndContext(@Param("userId") Long userId, @Param("context") String context);

    // 📅 RECENT ACTIVITY
    @Query("SELECT h FROM UserExerciseHistory h WHERE h.user.id = :userId ORDER BY h.usedAt DESC LIMIT :limit")
    List<UserExerciseHistory> findUserRecentActivity(@Param("userId") Long userId, @Param("limit") int limit);

    // Check if user has used an exercise recently (for recommendations)
    @Query("SELECT COUNT(h) > 0 FROM UserExerciseHistory h " +
            "WHERE h.user.id = :userId AND h.exercise.id = :exerciseId AND h.usedAt >= :since")
    boolean hasUserUsedExerciseRecently(@Param("userId") Long userId, @Param("exerciseId") Long exerciseId, @Param("since") LocalDateTime since);

    // 🔧 ADMIN QUERIES
    @Query("SELECT COUNT(h) FROM UserExerciseHistory h WHERE h.usedAt >= :since")
    Long countUsageSince(@Param("since") LocalDateTime since);

    @Query("SELECT h.exercise.id, COUNT(h) FROM UserExerciseHistory h " +
            "WHERE h.usedAt >= :since " +
            "GROUP BY h.exercise.id ORDER BY COUNT(h) DESC")
    List<Object[]> getMostUsedExercises(@Param("since") LocalDateTime since);

    @Query("SELECT h.context, COUNT(h) FROM UserExerciseHistory h " +
            "WHERE h.usedAt >= :since " +
            "GROUP BY h.context ORDER BY COUNT(h) DESC")
    List<Object[]> getUsageByContext(@Param("since") LocalDateTime since);
}