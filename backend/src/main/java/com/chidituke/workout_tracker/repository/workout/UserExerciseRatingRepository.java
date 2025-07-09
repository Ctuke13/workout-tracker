package com.chidituke.workout_tracker.repository.workout;

import com.chidituke.workout_tracker.model.workout.UserExerciseRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserExerciseRatingRepository extends JpaRepository<UserExerciseRating, Long> {

    // 🎯 CORE QUERIES
    Optional<UserExerciseRating> findByUserIdAndExerciseId(Long userId, Long exerciseId);

    boolean existsByUserIdAndExerciseId(Long userId, Long exerciseId);

    List<UserExerciseRating> findByUserId(Long userId);

    List<UserExerciseRating> findByExerciseId(Long exerciseId);

    // 📊 RATING ANALYTICS
    @Query("SELECT AVG(r.rating) FROM UserExerciseRating r WHERE r.exercise.id = :exerciseId")
    Double getAverageRatingForExercise(@Param("exerciseId") Long exerciseId);

    @Query("SELECT COUNT(r) FROM UserExerciseRating r WHERE r.exercise.id = :exerciseId")
    Long getTotalRatingsForExercise(@Param("exerciseId") Long exerciseId);

    @Query("SELECT COUNT(r) FROM UserExerciseRating r WHERE r.exercise.id = :exerciseId AND r.rating >= :minRating")
    Long getPositiveRatingsCount(@Param("exerciseId") Long exerciseId, @Param("minRating") Double minRating);

    // 👤 USER PREFERENCES
    @Query("SELECT r FROM UserExerciseRating r WHERE r.user.id = :userId AND r.rating >= :minRating ORDER BY r.ratedAt DESC")
    List<UserExerciseRating> findUserHighRatedExercises(@Param("userId") Long userId, @Param("minRating") Double minRating);

    @Query("SELECT r.exercise.exerciseType, AVG(r.rating) FROM UserExerciseRating r " +
            "WHERE r.user.id = :userId GROUP BY r.exercise.exerciseType ORDER BY AVG(r.rating) DESC")
    List<Object[]> getUserPreferredExerciseTypes(@Param("userId") Long userId);

    // 🔍 SEARCH & FILTER
    @Query("SELECT r FROM UserExerciseRating r WHERE r.user.id = :userId " +
            "AND (:hasComment IS NULL OR (:hasComment = true AND r.comment IS NOT NULL) OR (:hasComment = false AND r.comment IS NULL)) " +
            "ORDER BY r.ratedAt DESC")
    List<UserExerciseRating> findUserRatingsWithCommentFilter(@Param("userId") Long userId, @Param("hasComment") Boolean hasComment);

    @Query("SELECT r FROM UserExerciseRating r WHERE r.ratedAt >= :since ORDER BY r.ratedAt DESC")
    List<UserExerciseRating> findRecentRatings(@Param("since") LocalDateTime since);

    // 🏆 TOP RATED
    @Query("SELECT r.exercise.id, AVG(r.rating) as avgRating FROM UserExerciseRating r " +
            "GROUP BY r.exercise.id HAVING COUNT(r) >= :minRatings ORDER BY avgRating DESC")
    List<Object[]> findTopRatedExercises(@Param("minRatings") Long minRatings);

    // 🔧 ADMIN QUERIES
    @Query("SELECT COUNT(r) FROM UserExerciseRating r WHERE r.ratedAt >= :since")
    Long countRatingsSince(@Param("since") LocalDateTime since);

    @Query("SELECT r.exercise.id, COUNT(r) FROM UserExerciseRating r GROUP BY r.exercise.id ORDER BY COUNT(r) DESC")
    List<Object[]> getMostRatedExercises();
}