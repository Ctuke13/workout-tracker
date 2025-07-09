package com.chidituke.workout_tracker.model.workout;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for ExerciseGoalMapping entity
 * Required for @IdClass annotation in many-to-many mapping
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseGoalMappingId implements Serializable {

    private Long exerciseId;
    private Integer goalId;

    // 🔧 EQUALS & HASHCODE (Required for composite keys)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExerciseGoalMappingId that = (ExerciseGoalMappingId) o;
        return Objects.equals(exerciseId, that.exerciseId) &&
                Objects.equals(goalId, that.goalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exerciseId, goalId);
    }
}