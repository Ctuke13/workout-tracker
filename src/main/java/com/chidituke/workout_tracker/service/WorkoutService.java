package com.chidituke.workout_tracker.service;

import com.chidituke.workout_tracker.mapper.WorkoutMapper;
import com.chidituke.workout_tracker.dto.WorkoutRequest;
import com.chidituke.workout_tracker.dto.WorkoutResponse;
import com.chidituke.workout_tracker.model.Workout;
import com.chidituke.workout_tracker.repository.WorkoutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WorkoutService {

    @Autowired
    private WorkoutRepository workoutRepository;

    private WorkoutMapper workoutMapper;

    public List<WorkoutResponse> getAllWorkouts() {
        return workoutRepository.findAll()
                .stream()
                .map(workoutMapper::mapEntityToResponse)
                .collect(Collectors.toList());
    };

    public Optional<WorkoutResponse> getWorkoutById(Long id) {
        return workoutRepository.findById(id)
                .map(workoutMapper::mapEntityToResponse);
    };

    public List<WorkoutResponse> getWorkoutsByWorkoutType(String workoutType) {
        return workoutRepository.findByWorkoutType(workoutType)
                .stream()
                .map(workoutMapper::mapEntityToResponse)
                .collect(Collectors.toList());
    };

    public List<WorkoutResponse> searchWorkoutsByName(String workoutName) {
        return workoutRepository.findByNameContainingIgnoreCase(workoutName)
                .stream()
                .map(workoutMapper::mapEntityToResponse)
                .collect(Collectors.toList());
    };

    public List<WorkoutResponse> getWorkoutsByWorkoutType(Boolean isCardio) {
        return workoutRepository.findByIsCardio(isCardio)
                .stream()
                .map(workoutMapper::mapEntityToResponse)
                .collect(Collectors.toList());
    };

    public WorkoutResponse createWorkout(WorkoutRequest workoutRequest) {
        Workout workout = workoutMapper.createEntityFromRequest(workoutRequest);

        Workout savedWorkout = workoutRepository.save(workout);
        return workoutMapper.mapEntityToResponse(savedWorkout);
    };

    public Optional<WorkoutResponse> updateWorkout(Long id, WorkoutRequest workoutRequest) {
        return workoutRepository.findById(id)
                .map(workout -> {
                    workoutMapper.mapRequestToEntity(workoutRequest, workout);

                    Workout savedWorkout = workoutRepository.save(workout);
                    return workoutMapper.mapEntityToResponse(savedWorkout);
                });
    }

    public boolean deleteWorkout(Long id) {
        if (workoutRepository.existsById(id)) {
            workoutRepository.deleteById(id);
            return true;
        }
        return false;
    };

    public boolean workoutExist(Long id) {
        return workoutRepository.existsById(id);
    }
}
