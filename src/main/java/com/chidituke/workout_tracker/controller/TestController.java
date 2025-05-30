package com.chidituke.workout_tracker.controller;

import com.chidituke.workout_tracker.model.*;
import com.chidituke.workout_tracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private WorkoutLogRepository workoutLogRepository;

    @Autowired
    private PerformanceRecordRepository performanceRecordRepository;

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/user")
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    @GetMapping("/status")
    public String getStatus() {
        return "Workout Tracker API is running";
    }

    @GetMapping("/workouts")
    public List<Workout> getAllWorkouts() {
        return workoutRepository.findAll();
    }

    @PostMapping("/workout")
    public Workout createWorkout(@RequestBody Workout workout) {
        return workoutRepository.save(workout);
    }

    @PostMapping("/create-sample-data")
    public String createSampleData() {
        try {
            // Create a sample user with timestamp to ensure uniqueness
            User user = new User();
            user.setUsername("testuser" + System.currentTimeMillis());
            user.setEmail("test" + System.currentTimeMillis() + "@email.com");
            user.setPassword("password123");
            user = userRepository.save(user);

            // Create sample workouts
            Workout benchPress = new Workout();
            benchPress.setWorkoutName("Bench Press");
            benchPress.setWorkoutDescription("Classic chest exercise");
            benchPress.setWorkoutCategory("Strength");
            benchPress.setCardio(false);
            benchPress = workoutRepository.save(benchPress);

            Workout running = new Workout();
            running.setWorkoutName("Running");
            running.setWorkoutDescription("Cardiovascular exercise");
            running.setWorkoutCategory("Cardio");
            running.setCardio(true);
            running = workoutRepository.save(running);

            // Create a workout log
            WorkoutLog workoutLog = new WorkoutLog();
            workoutLog.setUser(user);
            workoutLog.setWorkout(benchPress);
            workoutLog.setDate(LocalDate.now());
            workoutLog.setNotes("Good session");
            workoutLog = workoutLogRepository.save(workoutLog);

            // Create performance records
            PerformanceRecord set1 = new PerformanceRecord();
            set1.setWorkoutLog(workoutLog);
            set1.setSetNumber(1);
            set1.setReps(10);
            set1.setWeight(135.0);
            performanceRecordRepository.save(set1);

            PerformanceRecord set2 = new PerformanceRecord();
            set2.setWorkoutLog(workoutLog);
            set2.setSetNumber(2);
            set2.setReps(8);
            set2.setWeight(135.0);
            performanceRecordRepository.save(set2);

            return "Sample data created successfully!";
        } catch (Exception e) {
            // Log the error
            System.err.println("Error creating sample data: " + e.getMessage());
            e.printStackTrace();
            return "Error creating sample data: " + e.getMessage();
        }
    }

    @GetMapping("/count")
    public String getCounts() {
        return String.format("Users: %d, Workouts: %d, Logs: %d, Performance Records: %d",
                userRepository.count(),
                workoutRepository.count(),
                workoutLogRepository.count(),
                performanceRecordRepository.count());
    }

}
