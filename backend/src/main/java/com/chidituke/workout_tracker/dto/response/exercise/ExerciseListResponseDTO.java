package com.chidituke.workout_tracker.dto.response.exercise;

import com.chidituke.workout_tracker.model.Exercise;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
public class ExerciseListResponseDTO {
    private List<ExerciseResponseDTO> exercises;
    private long totalCount;
    private int page;
    private int size;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private boolean isFirst;
    private boolean isLast;
    private ExerciseFiltersDTO availableFilters;

    public static ExerciseListResponseDTO fromPage(Page<Exercise> exercisePage) {
        return ExerciseListResponseDTO.builder()
                .exercises(ExerciseResponseDTO.fromEntityList(exercisePage.getContent()))
                .totalCount(exercisePage.getTotalElements())
                .page(exercisePage.getNumber())
                .size(exercisePage.getSize())
                .totalPages(exercisePage.getTotalPages())
                .hasNext(exercisePage.hasNext())
                .hasPrevious(exercisePage.hasPrevious())
                .isFirst(exercisePage.isFirst())
                .isLast(exercisePage.isLast())
                .build();
    }

    public static ExerciseListResponseDTO fromList(List<Exercise> exercises) {
        return ExerciseListResponseDTO.builder()
                .exercises(ExerciseResponseDTO.fromEntityList(exercises))
                .totalCount(exercises.size())
                .page(0)
                .size(exercises.size())
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .isFirst(true)
                .isLast(true)
                .build();
    }

    public void setAvailableFilters(ExerciseFiltersDTO filters) {
        this.availableFilters = filters;
    }
}