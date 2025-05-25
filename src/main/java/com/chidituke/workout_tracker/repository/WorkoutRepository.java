package com.chidituke.workout_tracker.repository;

import com.chidituke.workout_tracker.model.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    List<Workout> findByWorkoutType(String category);
    List<Workout> findByNameContainingIgnoreCase(String name);
    List<Workout> findByIsCardio(Boolean isCardio);
}
