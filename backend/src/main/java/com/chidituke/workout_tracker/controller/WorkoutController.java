package com.chidituke.workout_tracker.controller;

import com.chidituke.workout_tracker.dto.WorkoutRequest;
import com.chidituke.workout_tracker.dto.WorkoutResponse;
import com.chidituke.workout_tracker.service.WorkoutService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {
    @Autowired
    private WorkoutService workoutService;

    /**
     * Get all available workouts
     * GET /api/workouts
     */
    @GetMapping
    public ResponseEntity<List<WorkoutResponse>> getAllWorkouts() {
      List<WorkoutResponse> workouts = workoutService.getAllWorkouts();
      return ResponseEntity.ok(workouts);
    };

    /**
     * Get workout by ID
     * GET /api/workouts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<WorkoutResponse> getWorkoutById(@PathVariable long id) {
        Optional<WorkoutResponse> workout = workoutService.getWorkoutById(id);
        return workout.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search workouts by name
     * GET /api/workouts/search?name=bench
     */
    @GetMapping("/search")
    public ResponseEntity<List<WorkoutResponse>> searchWorkouts(@RequestParam String name) {
        List<WorkoutResponse> workouts = workoutService.searchWorkoutsByName(name);
        return ResponseEntity.ok(workouts);
    }

    /**
     * Get workouts by category
     * GET /api/workouts/category/Chest
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<WorkoutResponse>> getWorkoutsByCategory(@PathVariable String category) {
        List<WorkoutResponse> workouts = workoutService.getWorkoutsByWorkoutCategory(category);
        return ResponseEntity.ok(workouts);
    }

    /**
     * Get workouts by type (cardio or strength)
     * GET /api/workouts/type?cardio=true
     */
    @GetMapping("/type")
    public ResponseEntity<List<WorkoutResponse>> getWorkoutsByType(@RequestParam Boolean cardio) {
        List<WorkoutResponse> workouts = workoutService.getWorkoutsByWorkoutType(cardio);
        return ResponseEntity.ok(workouts);
    }

    /**
     * Create a new workout template
     * POST /api/workouts
     */
    @PostMapping
    public ResponseEntity<WorkoutResponse> createWorkout(@RequestBody @Valid WorkoutRequest workoutRequest) {
        WorkoutResponse createdWorkout = workoutService.createWorkout(workoutRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdWorkout);
    }

    /**
     * Update an existing workout template
     * PUT /api/workouts/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<WorkoutResponse> updateWorkout(@PathVariable long id, @RequestBody @Valid WorkoutRequest workoutRequest) {
        Optional<WorkoutResponse> updatedWorkout = workoutService.updateWorkout(id, workoutRequest);
        return updatedWorkout.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a workout template
     * DELETE /api/workouts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<WorkoutResponse> deleteWorkout(@PathVariable long id) {
        boolean deleted = workoutService.deleteWorkout(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
