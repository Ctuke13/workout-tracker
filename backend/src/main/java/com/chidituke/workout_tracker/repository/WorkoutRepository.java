package com.chidituke.workout_tracker.repository;

import com.chidituke.workout_tracker.model.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    List<Workout> findByWorkoutCategory(String category);
    List<Workout> findByWorkoutNameContainingIgnoreCase(String name);
    List<Workout> findByIsCardio(Boolean isCardio);
}
