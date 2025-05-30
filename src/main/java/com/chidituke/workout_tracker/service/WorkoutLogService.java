package com.chidituke.workout_tracker.service;

import com.chidituke.workout_tracker.dto.WorkoutLogRequest;
import com.chidituke.workout_tracker.dto.WorkoutLogResponse;
import com.chidituke.workout_tracker.mapper.WorkoutLogMapper;
import com.chidituke.workout_tracker.model.User;
import com.chidituke.workout_tracker.model.Workout;
import com.chidituke.workout_tracker.model.WorkoutLog;
import com.chidituke.workout_tracker.repository.UserRepository;
import com.chidituke.workout_tracker.repository.WorkoutLogRepository;
import com.chidituke.workout_tracker.repository.WorkoutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WorkoutLogService {

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkoutLogRepository workoutLogRepository;

    @Autowired
    private WorkoutLogMapper workoutLogMapper;

    public List<WorkoutLogResponse> getUserWorkoutHistory(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return workoutLogRepository.findByUserOrderByDateDesc(user)
                .stream()
                .map(workoutLogMapper::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    public List<WorkoutLogResponse> getWorkoutLogsByDate(String username, LocalDate date) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return workoutLogRepository.findByUserAndDate(user, date)
                .stream()
                .map(workoutLogMapper::mapEntityToResponse)
                .collect(Collectors.toList());
    };

    public List<WorkoutLogResponse> getUserWorkoutLogsByDateRange(String username, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return workoutLogRepository.findByUserAndDateBetween(user, startDate, endDate)
                .stream()
                .map(workoutLogMapper::mapEntityToResponse)
                .collect(Collectors.toList());
    };

    public Optional<WorkoutLogResponse> getWorkoutLogById(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return workoutLogRepository.findById(id)
                .filter(workoutLog -> workoutLog.getUser().getId().equals(user.getId()))
                .map(workoutLogMapper::mapEntityToResponse);
    }

    public WorkoutLogResponse createWorkoutLog(String username, WorkoutLogRequest workoutLogRequest) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Workout workout = workoutRepository.findById(workoutLogRequest.getWorkoutId())
                .orElseThrow(() -> new RuntimeException("Workout not found: " + workoutLogRequest.getWorkoutId()));

        WorkoutLog newWorkoutLog = new WorkoutLog();
        newWorkoutLog.setUser(user);
        newWorkoutLog.setWorkout(workout);

        workoutLogMapper.mapRequestToEntity(workoutLogRequest, newWorkoutLog);

        WorkoutLog savedLog = workoutLogRepository.save(newWorkoutLog);
        return workoutLogMapper.mapEntityToResponse(savedLog);
    };

    public Optional<WorkoutLogResponse> updateWorkoutLog(Long id, String username, WorkoutLogRequest workoutLogRequest) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return workoutLogRepository.findById(id)
                .filter(workoutLog -> workoutLog.getUser().getId().equals(user.getId()))
                .map(workoutLog -> {
                    workoutLogMapper.mapRequestToEntity(workoutLogRequest, workoutLog);

                    WorkoutLog savedLog = workoutLogRepository.save(workoutLog);
                    return workoutLogMapper.mapEntityToResponse(savedLog);
                });
    }

    public boolean deleteWorkoutLog(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Optional<WorkoutLog> workoutLog = workoutLogRepository.findById(id);
        if (workoutLog.isPresent() && workoutLog.get().getUser().getId().equals(user.getId())) {
            workoutLogRepository.deleteById(id);
            return true;
        }
        return false;
    };

    public WorkoutLogResponse startWorkoutSession(String username, WorkoutLogRequest workoutLogRequest) {
        return createWorkoutLog(username, workoutLogRequest);
    }

    public List<WorkoutLogResponse> getRecentWorkoutLogs(String username, int limit) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return workoutLogRepository.findByUserOrderByDateDesc(user)
                .stream()
                .limit(limit)
                .map(workoutLogMapper::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    public long getTotalWorkoutCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return workoutLogRepository.countByUser(user);
    }

    public Long getWorkoutCountForUser(String username, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return workoutLogRepository.countByUserAndDateRange(user, startDate, endDate);
    }
}
