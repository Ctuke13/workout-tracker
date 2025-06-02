package com.chidituke.workout_tracker.service;

import com.chidituke.workout_tracker.dto.PerformanceRequest;
import com.chidituke.workout_tracker.dto.PerformanceResponse;
import com.chidituke.workout_tracker.mapper.PerformanceMapper;
import com.chidituke.workout_tracker.model.PerformanceRecord;
import com.chidituke.workout_tracker.model.User;
import com.chidituke.workout_tracker.model.WorkoutLog;
import com.chidituke.workout_tracker.repository.PerformanceRecordRepository;
import com.chidituke.workout_tracker.repository.UserRepository;
import com.chidituke.workout_tracker.repository.WorkoutLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PerformanceService {

    @Autowired
    private PerformanceRecordRepository performanceRecordRepository;

    @Autowired
    private WorkoutLogRepository workoutLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PerformanceMapper performanceMapper;

    public List<PerformanceResponse> getPerformanceByWorkoutLog(Long workoutLogId, String username) {
        // Verify user owns this workout log
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        WorkoutLog workoutLog = workoutLogRepository.findById(workoutLogId)
                .orElseThrow(() -> new RuntimeException("Workout log not found"));

        // Security check: ensure user owns this workout log
        if (!workoutLog.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: Workout log does not belong to user");
        }

        return performanceRecordRepository.findByWorkoutLogOrderBySetNumber(workoutLog)
                .stream()
                .map(performanceMapper::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    public List<PerformanceResponse> getUserPerformanceByWorkout(String username, Long workoutId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return performanceRecordRepository.findByUserAndWorkoutOrderByDateDesc(user, workoutId)
                .stream()
                .map(performanceMapper::mapEntityToResponse)  // ← Use mapper
                .collect(Collectors.toList());
    }

    public PerformanceResponse createPerformance(String username, PerformanceRequest request) {
        // Verify user owns the workout log
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        WorkoutLog workoutLog = workoutLogRepository.findById(request.getWorkoutLogId())
                .orElseThrow(() -> new RuntimeException("Workout log not found: " + request.getWorkoutLogId()));

        // Security check: ensure user owns this workout log
        if (!workoutLog.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: Workout log does not belong to user");
        }

        // Create new performance record
        PerformanceRecord performanceRecord = new PerformanceRecord();
        performanceRecord.setWorkoutLog(workoutLog);  // ← Set relationship

        // Map request fields to entity using mapper
        performanceMapper.mapRequestToEntity(request, performanceRecord);  // ← Use mapper

        PerformanceRecord savedPerformance = performanceRecordRepository.save(performanceRecord);
        return performanceMapper.mapEntityToResponse(savedPerformance);  // ← Use mapper
    }

    public Optional<PerformanceResponse> updatePerformance(Long id, String username, PerformanceRequest request) {
        return performanceRecordRepository.findById(id)
                .filter(performance -> {
                    // Verify user owns this performance record
                    return performance.getWorkoutLog().getUser().getUsername().equals(username);
                })
                .map(performance -> {
                    performanceMapper.mapRequestToEntity(request, performance);  // ← Use mapper

                    PerformanceRecord savedPerformance = performanceRecordRepository.save(performance);
                    return performanceMapper.mapEntityToResponse(savedPerformance);  // ← Use mapper
                });
    }

    public boolean deletePerformance(Long id, String username) {
        Optional<PerformanceRecord> performance = performanceRecordRepository.findById(id);
        if (performance.isPresent() &&
                performance.get().getWorkoutLog().getUser().getUsername().equals(username)) {
            performanceRecordRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Analytics methods
    public Double getMaxWeightForWorkout(String username, Long workoutId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return performanceRecordRepository.findMaxWeightByUserAndWorkout(user, workoutId);
    }

    public Double getTotalVolumeForDateRange(String username, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return performanceRecordRepository.getTotalVolumeByUserAndDateRange(user, startDate, endDate);
    }
}
