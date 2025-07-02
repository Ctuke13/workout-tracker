package com.chidituke.workout_tracker.util;

import com.chidituke.workout_tracker.model.workout.WorkoutPlan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility to help identify correct method names in WorkoutPlan entity
 * Use this temporarily to find the right method names for your entities
 */
@Component
@Slf4j
public class WorkoutPlanMethodFinder {

    /**
     * Print all available getter methods in WorkoutPlan entity
     * Call this method once to see what methods are actually available
     */
    public void printWorkoutPlanMethods() {
        log.info("=== WorkoutPlan Available Methods ===");

        Method[] methods = WorkoutPlan.class.getDeclaredMethods();

        List<String> getterMethods = Arrays.stream(methods)
                .filter(method -> method.getName().startsWith("get"))
                .map(Method::getName)
                .sorted()
                .collect(Collectors.toList());

        getterMethods.forEach(methodName -> log.info("Method: {}", methodName));

        log.info("=== End WorkoutPlan Methods ===");
    }

    /**
     * Print methods that might be related to exercises
     */
    public void printExerciseRelatedMethods() {
        log.info("=== Exercise-Related Methods ===");

        Method[] methods = WorkoutPlan.class.getDeclaredMethods();

        Arrays.stream(methods)
                .filter(method -> method.getName().toLowerCase().contains("exercise") ||
                        method.getName().toLowerCase().contains("plan"))
                .forEach(method -> log.info("Exercise-related method: {} -> Returns: {}",
                        method.getName(), method.getReturnType().getSimpleName()));

        log.info("=== End Exercise-Related Methods ===");
    }

    /**
     * Check specific method names you're looking for
     */
    public void checkSpecificMethods() {
        log.info("=== Checking Specific Methods ===");

        String[] methodsToCheck = {
                "getName", "getWorkoutName", "getTitle",
                "getDescription",
                "getDifficulty", "getDifficultyLevel",
                "getEstimatedDuration", "getDuration",
                "getPlanExercises", "getWorkoutPlanExercises", "getExercises"
        };

        for (String methodName : methodsToCheck) {
            try {
                Method method = WorkoutPlan.class.getMethod(methodName);
                log.info("✅ {} exists -> Returns: {}", methodName, method.getReturnType().getSimpleName());
            } catch (NoSuchMethodException e) {
                log.info("❌ {} does not exist", methodName);
            }
        }

        log.info("=== End Specific Methods Check ===");
    }

    /**
     * Call this method from a controller or service to identify methods
     */
    public void analyzeWorkoutPlan() {
        printWorkoutPlanMethods();
        printExerciseRelatedMethods();
        checkSpecificMethods();
    }
}