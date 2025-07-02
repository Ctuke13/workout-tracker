package com.chidituke.workout_tracker.mapper.workout;

import com.chidituke.workout_tracker.dto.request.workout_program.WorkoutProgramRequest;
import com.chidituke.workout_tracker.dto.response.workout_program.WorkoutProgramResponse;
import com.chidituke.workout_tracker.model.workout.WorkoutProgram;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WorkoutProgramMapper {

    public WorkoutProgramResponse toResponse(WorkoutProgram entity) {
        if (entity == null) {
            return null;
        }

        return WorkoutProgramResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .programType(entity.getProgramType() != null ? entity.getProgramType().name() : null)
                .difficultyLevel(entity.getDifficultyLevel() != null ? entity.getDifficultyLevel().name() : null)
                .durationWeeks(entity.getDurationWeeks())
                .sessionsPerWeek(entity.getSessionsPerWeek())
                .targetGoals(entity.getTargetGoals())
                .equipmentNeeded(entity.getEquipmentNeeded())
                .createdByUserId(entity.getCreatedByUserId())
                .createdByProfessional(entity.getCreatedByProfessional())
                .isPublished(entity.getIsPublished())
                .isPublic(entity.getIsPublic())
                .enrollmentCount(entity.getEnrollmentCount())
                .completionCount(entity.getCompletionCount())
                .averageRating(entity.getAverageRating())
                .totalRatings(entity.getTotalRatings())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .completionRate(entity.getCompletionRate())
                .isPopular(entity.isPopular())
                .isHighlyRated(entity.isHighlyRated())
                .build();
    }

    public List<WorkoutProgramResponse> toResponseList(List<WorkoutProgram> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void mapRequestToEntity(WorkoutProgramRequest request, WorkoutProgram entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setDurationWeeks(request.getDurationWeeks());
        entity.setSessionsPerWeek(request.getSessionsPerWeek());
        entity.setTargetGoals(request.getTargetGoals());
        entity.setEquipmentNeeded(request.getEquipmentNeeded());
        entity.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : true);

        // Convert program type string to enum
        if (request.getProgramType() != null) {
            try {
                entity.setProgramType(WorkoutProgram.ProgramType.valueOf(request.getProgramType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                entity.setProgramType(WorkoutProgram.ProgramType.STRENGTH);
            }
        }

        // Convert difficulty level string to enum
        if (request.getDifficultyLevel() != null) {
            try {
                entity.setDifficultyLevel(WorkoutProgram.DifficultyLevel.valueOf(request.getDifficultyLevel().toUpperCase()));
            } catch (IllegalArgumentException e) {
                entity.setDifficultyLevel(WorkoutProgram.DifficultyLevel.BEGINNER);
            }
        }
    }
}