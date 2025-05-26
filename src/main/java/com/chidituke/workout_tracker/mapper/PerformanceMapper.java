package com.chidituke.workout_tracker.mapper;

import com.chidituke.workout_tracker.dto.PerformanceRequest;
import com.chidituke.workout_tracker.dto.PerformanceResponse;
import com.chidituke.workout_tracker.model.PerformanceRecord;
import com.chidituke.workout_tracker.service.PerformanceService;
import org.springframework.stereotype.Component;

@Component
public class PerformanceMapper {

    private final PerformanceService performanceService;

    public PerformanceMapper(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    /**
     * Maps PerformanceRequest DTO to PerformanceRecord entity
     * Note: WorkoutLog relationship is set separately in the service
     */
    public void mapRequestToEntity(PerformanceRequest request, PerformanceRecord performanceRecord){
        performanceRecord.setSetNumber(request.getSetNumber());
        performanceRecord.setReps(request.getReps());
        performanceRecord.setWeight(request.getWeight());
        performanceRecord.setDurationMinutes(request.getDurationMinutes());
        performanceRecord.setDurationSeconds(request.getDurationSeconds());
        performanceRecord.setDistanceKm(request.getDistanceKm());
        performanceRecord.setCaloriesBurned(request.getCaloriesBurned());
        performanceRecord.setNotes(request.getNotes());
    }

    /**
     * Converts PerformanceRecord entity to PerformanceResponse DTO
     */
    public PerformanceResponse mapEntityToResponse(PerformanceRecord performanceRecord){
        return new PerformanceResponse(
          performanceRecord.getId(),
          performanceRecord.getWorkoutLog().getId(),
          performanceRecord.getWorkoutLog().getDate(),
          performanceRecord.getWorkoutLog().getWorkout().getWorkoutName(),
          performanceRecord.getWorkoutLog().getWorkout().getWorkoutCategory(),
          performanceRecord.getWorkoutLog().getWorkout().isCardio(),
          performanceRecord.getSetNumber(),
          performanceRecord.getReps(),
          performanceRecord.getWeight(),
          performanceRecord.getDurationMinutes(),
                performanceRecord.getDurationSeconds(),
                performanceRecord.getDistanceKm(),
                performanceRecord.getCaloriesBurned(),
                performanceRecord.getNotes()
        );
    }
}
