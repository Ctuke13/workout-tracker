// src/services/transformers.ts - Fixed with correct imports and updated field names

import {
    // Backend types (what Spring Boot sends) - keep these from api.ts
    BackendExercise,
    ScheduledWorkoutResponse,
    WorkoutSessionResponse,
    PerformanceResponse,
    WorkoutPlanInfo,
    WorkoutStatsResponse,
    WorkoutSessionInfo,
    WorkoutSet,
    WorkoutExercise,

    // Utility types
    DateString,
    DateTimeString
} from '../types/api';

// Import frontend domain types from exercise.ts
import {
    Exercise,
    DifficultyLevel,
    ExerciseType,
    WorkoutStats,
    ScheduledExercise,
    WorkoutSession
} from '../types/exercise';

// ==================== EXERCISE TRANSFORMATIONS ====================

/**
 * Transforms backend exercise data into frontend-friendly format
 * This handles the rich backend data structure and creates the simplified
 * format that your React components currently expect
 */
export const transformBackendExerciseToFrontend = (backendExercise: BackendExercise): Exercise => {
    return {
        // Basic identification and naming fields
        id: backendExercise.id,
        name: backendExercise.name,
        exerciseName: backendExercise.name, // Provide backward compatibility
        emoji: backendExercise.emoji || '💪',
        description: backendExercise.description || '',

        // The critical field that determines workout tracking behavior
        isCardio: backendExercise.isCardio,
        isIsometric: backendExercise.isIsometric,

        // Exercise classification fields - keep these as raw enum values
        exerciseType: backendExercise.exerciseType,
        exerciseTypeDisplay: backendExercise.exerciseTypeDisplay,
        difficultyLevel: backendExercise.difficultyLevel, // Keep as enum, don't transform to string

        // Numerical fields - provide defaults for missing values
        estimatedDurationMinutes: backendExercise.estimatedDurationMinutes || 0,
        estimatedCalories: backendExercise.estimatedCalories || 0,

        // Array fields - provide empty arrays as safe defaults
        targetMuscleGroups: backendExercise.targetMuscleGroups || [],
        equipmentRequired: backendExercise.equipmentRequired || [],
        benefits: backendExercise.benefits || [],
        tips: backendExercise.tips || [],

        // Optional fields
        videoUrl: backendExercise.videoUrl,

        // Rating and popularity fields - use exact field names from Exercise
        averageRating: backendExercise.averageRating,
        totalRatings: backendExercise.totalRatings,
        usageCount: backendExercise.usageCount,
        isPopular: backendExercise.isPopular,
        isHighlyRated: backendExercise.isHighlyRated,

        // Feature flags that affect UI behavior
        canDoAtHome: backendExercise.canDoAtHome,
        requiresEquipment: backendExercise.requiresEquipment,

        // Metadata fields
        createdByProfessional: backendExercise.isFromVerifiedSource || false,
        createdBy: backendExercise.createdBy,
        published: true // Assume published if coming from public API
    };
};

/**
 * Converts backend difficulty enum to user-friendly string
 */
const transformDifficultyLevel = (difficulty: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED'): string => {
    switch (difficulty) {
        case 'BEGINNER': return 'Beginner';
        case 'INTERMEDIATE': return 'Intermediate';
        case 'ADVANCED': return 'Advanced';
        default: return 'Beginner';
    }
};

// ==================== SCHEDULED WORKOUT TRANSFORMATIONS ====================

/**
 * ✅ FIXED: Handle both individual exercises and workout plan responses with updated field names
 */
export const transformScheduledWorkoutToExercises = (
    scheduledWorkout: ScheduledWorkoutResponse
): ScheduledExercise[] => {

    console.log('🔍 Transforming workout:', scheduledWorkout);

    // ✅ SMART DETECTION: Determine exercise type from scheduled workout data
    const isCardio = !!(
        scheduledWorkout.targetDurationMinutes ||
        scheduledWorkout.targetDistanceKm ||
        scheduledWorkout.targetPace
    );
    const isIsometric = !!scheduledWorkout.holdDurationSeconds;

    if (scheduledWorkout.workoutPlan) {
        const workoutPlan = scheduledWorkout.workoutPlan;

        const exercise: Exercise = {
            id: workoutPlan.id,
            name: workoutPlan.name || 'Unnamed Workout Plan',
            exerciseName: workoutPlan.name,
            emoji: workoutPlan.exerciseCount === 1 ? '💪' : '📋',
            description: workoutPlan.description || 'Scheduled workout',
            difficultyLevel: (workoutPlan.difficulty || 'INTERMEDIATE') as DifficultyLevel,
            exerciseType: isCardio ? 'CARDIO' as ExerciseType : 'STRENGTH' as ExerciseType,

            // ✅ FIXED: Auto-determine from scheduled workout data
            isCardio: isCardio,
            isIsometric: isIsometric,

            exerciseTypeDisplay: workoutPlan.exerciseCount === 1 ? 'Exercise' : 'Workout Plan',
            estimatedDurationMinutes: workoutPlan.estimatedDurationMinutes || 15,
            estimatedCalories: Math.round((workoutPlan.estimatedDurationMinutes || 15) * 8),
            targetMuscleGroups: [],
            equipmentRequired: [],
            benefits: [],
            tips: [],
            videoUrl: null,
            averageRating: 0,
            totalRatings: 0,
            usageCount: 0,
            isPopular: false,
            isHighlyRated: false,
            canDoAtHome: true,
            requiresEquipment: false,
            createdByProfessional: false,
            createdBy: 'Platform',
            published: true
        };

        const scheduledExercise: ScheduledExercise = {
            id: scheduledWorkout.id.toString(),
            exerciseId: workoutPlan.id,
            exercise: exercise,
            scheduledDate: scheduledWorkout.scheduledDate,

            // ✅ ENHANCED: Smart defaults based on exercise type
            targetSets: (() => {
                if (isCardio) {
                    return scheduledWorkout.sets || 1; // Default to 1 for single-session cardio
                }
                return scheduledWorkout.sets || 3; // Default for strength/isometric
            })(),

            targetReps: (() => {
                if (isCardio) {
                    return scheduledWorkout.targetDurationMinutes || 20; // Show duration for cardio
                }
                return parseInt(scheduledWorkout.reps || '10'); // Convert string to number
            })(),

            targetWeight: scheduledWorkout.weight,
            targetWeightUnit: 'lbs', // Default to lbs for American users
            restSeconds: scheduledWorkout.restSeconds || (isCardio ? 0 : 60),
            targetRpe: scheduledWorkout.targetRpe,
            tempo: scheduledWorkout.tempo,

            // ✅ CARDIO FIELDS - Fixed field names
            targetDurationMinutes: scheduledWorkout.targetDurationMinutes,

            // ✅ FIXED: Convert from targetDistanceKm to targetDistance in miles
            targetDistance: (() => {
                if (scheduledWorkout.targetDistanceKm) {
                    // Backend sends km, convert to miles for American users
                    return Math.round(scheduledWorkout.targetDistanceKm * 0.621371 * 100) / 100;
                }
                return undefined; // No distance set
            })(),

            targetDistanceUnit: 'miles', // Default to miles for American users
            targetDistanceKm: scheduledWorkout.targetDistanceKm, // Keep for legacy support
            targetPace: scheduledWorkout.targetPace,

            // ✅ ISOMETRIC FIELDS
            holdDurationSeconds: scheduledWorkout.holdDurationSeconds,

            // ✅ FIXED: Use customNotes, not notes
            notes: scheduledWorkout.customNotes,
            completed: scheduledWorkout.status === 'COMPLETED',
            status: scheduledWorkout.status,
            createdAt: scheduledWorkout.createdAt,
            userId: scheduledWorkout.user?.id?.toString() || '1'
        };

        console.log('✅ FIXED: Transformed exercise with proper flags:', {
            exerciseName: exercise.name,
            isCardio: exercise.isCardio,
            isIsometric: exercise.isIsometric,
            targetSets: scheduledExercise.targetSets,
            targetReps: scheduledExercise.targetReps,
            targetDurationMinutes: scheduledExercise.targetDurationMinutes,
            targetDistance: scheduledExercise.targetDistance,
            targetDistanceUnit: scheduledExercise.targetDistanceUnit,
            notes: scheduledExercise.notes
        });

        return [scheduledExercise];
    }

    // ✅ FALLBACK: Handle individual exercises (this might be your case)
    console.warn('⚠️ ScheduledWorkout has no workoutPlan, using fallback:', scheduledWorkout.id);

    const exercise: Exercise = {
        id: 0,
        name: 'Individual Exercise', // Will be overridden by backend
        exerciseName: 'Individual Exercise',
        emoji: '💪',
        description: 'Individual scheduled exercise',
        difficultyLevel: 'INTERMEDIATE' as DifficultyLevel,
        exerciseType: isCardio ? 'CARDIO' as ExerciseType : 'STRENGTH' as ExerciseType,

        // ✅ FIXED: Smart detection from scheduled workout data
        isCardio: isCardio,
        isIsometric: isIsometric,

        exerciseTypeDisplay: 'Individual Exercise',
        estimatedDurationMinutes: scheduledWorkout.targetDurationMinutes || 15,
        estimatedCalories: 100,
        targetMuscleGroups: [],
        equipmentRequired: [],
        benefits: [],
        tips: [],
        videoUrl: null,
        averageRating: 0,
        totalRatings: 0,
        usageCount: 0,
        isPopular: false,
        isHighlyRated: false,
        canDoAtHome: true,
        requiresEquipment: false,
        createdByProfessional: false,
        createdBy: 'Platform',
        published: true
    };

    const scheduledExercise: ScheduledExercise = {
        id: scheduledWorkout.id.toString(),
        exerciseId: 0,
        exercise: exercise,
        scheduledDate: scheduledWorkout.scheduledDate,

        // ✅ SMART DEFAULTS based on detected exercise type
        targetSets: (() => {
            if (isCardio) {
                return scheduledWorkout.sets || 1; // Cardio usually 1 set unless interval
            }
            return scheduledWorkout.sets || 3; // Strength/isometric default to 3
        })(),

        targetReps: (() => {
            if (isCardio) {
                return scheduledWorkout.targetDurationMinutes || 20; // Show duration for cardio
            }
            return parseInt(scheduledWorkout.reps || '10');
        })(),

        targetWeight: scheduledWorkout.weight,
        targetWeightUnit: 'lbs',
        restSeconds: scheduledWorkout.restSeconds || (isCardio ? 0 : 60),
        targetRpe: scheduledWorkout.targetRpe,
        tempo: scheduledWorkout.tempo,

        // ✅ CARDIO FIELDS - Fixed field names
        targetDurationMinutes: scheduledWorkout.targetDurationMinutes,

        // ✅ FIXED: Convert from targetDistanceKm to targetDistance in miles
        targetDistance: scheduledWorkout.targetDistanceKm ?
            Math.round(scheduledWorkout.targetDistanceKm * 0.621371 * 100) / 100 :
            undefined,

        targetDistanceUnit: 'miles',
        targetDistanceKm: scheduledWorkout.targetDistanceKm,
        targetPace: scheduledWorkout.targetPace,

        // ✅ ISOMETRIC FIELDS
        holdDurationSeconds: scheduledWorkout.holdDurationSeconds,

        // ✅ FIXED: Use customNotes, not notes
        notes: scheduledWorkout.customNotes,
        completed: scheduledWorkout.status === 'COMPLETED',
        createdAt: scheduledWorkout.createdAt,
        userId: scheduledWorkout.user?.id?.toString() || '1'
    };

    console.log('✅ FIXED: Fallback exercise with smart detection:', {
        exerciseName: exercise.name,
        isCardio: exercise.isCardio,
        isIsometric: exercise.isIsometric,
        detectedFrom: {
            hasDuration: !!scheduledWorkout.targetDurationMinutes,
            hasDistance: !!scheduledWorkout.targetDistanceKm,
            hasPace: !!scheduledWorkout.targetPace,
            hasHold: !!scheduledWorkout.holdDurationSeconds
        }
    });

    return [scheduledExercise];
};

/**
 * ✅ UPDATED: Transform calendar response with better error handling
 */
export const transformScheduledWorkoutsToCalendarData = (
    scheduledWorkouts: ScheduledWorkoutResponse[] | any
): ScheduledExercise[] => {
    // ✅ Handle different response formats from backend
    let workoutsArray: ScheduledWorkoutResponse[] = [];

    if (Array.isArray(scheduledWorkouts)) {
        // Direct array response
        workoutsArray = scheduledWorkouts;
    } else if (scheduledWorkouts && typeof scheduledWorkouts === 'object') {
        // Handle CalendarViewResponse format
        if ('workoutsByDate' in scheduledWorkouts && typeof scheduledWorkouts.workoutsByDate === 'object') {
            console.log('🔍 DEBUG: Found workoutsByDate:', scheduledWorkouts.workoutsByDate);

            // Flatten the map of date -> workout arrays into a single array
            workoutsArray = Object.values(scheduledWorkouts.workoutsByDate)
                .flat() as ScheduledWorkoutResponse[];

            console.log('🔍 DEBUG: Flattened workouts:', workoutsArray);
        } else if ('scheduledWorkouts' in scheduledWorkouts && Array.isArray(scheduledWorkouts.scheduledWorkouts)) {
            workoutsArray = scheduledWorkouts.scheduledWorkouts;
        } else if ('content' in scheduledWorkouts && Array.isArray(scheduledWorkouts.content)) {
            workoutsArray = scheduledWorkouts.content;
        } else if ('data' in scheduledWorkouts && Array.isArray(scheduledWorkouts.data)) {
            workoutsArray = scheduledWorkouts.data;
        } else {
            console.warn('⚠️ Unexpected response format:', scheduledWorkouts);
            console.warn('⚠️ Available properties:', Object.keys(scheduledWorkouts));
            workoutsArray = [];
        }
    } else {
        console.warn('⚠️ Response is not an object or array:', scheduledWorkouts);
        workoutsArray = [];
    }

    // Transform each scheduled workout to scheduled exercises
    const allScheduledExercises = workoutsArray.flatMap(workout => {
        try {
            return transformScheduledWorkoutToExercises(workout);
        } catch (error) {
            console.error('❌ Failed to transform scheduled workout:', workout.id, error);
            return [];
        }
    });

    console.log(`✅ Transformed ${workoutsArray.length} scheduled workouts into ${allScheduledExercises.length} scheduled exercises`);
    return allScheduledExercises;
};

/**
 * ✅ FIXED: Transforms backend workout stats response into frontend format
 */
export const transformWorkoutStatsResponse = (apiStats: WorkoutStatsResponse): WorkoutStats => {
    return {
        totalWorkouts: apiStats.totalScheduledWorkouts || 0,
        completedWorkouts: apiStats.totalCompletedWorkouts || 0,
        completionRate: apiStats.overallCompletionRate || 0,
        weeklyGoal: 5, // Default weekly goal - you can make this configurable
        // ✅ FIXED: Your WorkoutStatsResponse doesn't have streak fields, so use defaults
        currentStreak: 0, // Not available in your backend response
        bestStreak: 0, // Not available in your backend response
        totalExercisesCompleted: apiStats.totalCompletedWorkouts || 0,
        averageWorkoutDuration: apiStats.averageDurationMinutes || 0
    };
};

// ==================== WORKOUT SESSION TRANSFORMATIONS ====================

/**
 * Transforms backend workout session into frontend workout session format
 * This handles the complex mapping between backend's performance-focused model
 * and frontend's set-based progression model
 */
export const transformWorkoutSessionResponse = (
    backendSession: WorkoutSessionResponse,
    performanceRecords: PerformanceResponse[] = []
): WorkoutSession => {
    // Group performance records by exercise
    const exercisePerformanceMap = groupPerformanceByExercise(performanceRecords);

    // For now, create placeholder exercises since we need workout plan details
    // In real implementation, you'll fetch the workout plan and its exercises
    const exercises: WorkoutExercise[] = Object.keys(exercisePerformanceMap).map(exerciseId => {
        const exercisePerformance = exercisePerformanceMap[exerciseId];
        const sets = transformPerformanceRecordsToSets(exercisePerformance);

        // Create placeholder scheduled exercise with updated field names
        const placeholderScheduledExercise: ScheduledExercise = {
            id: `session-${backendSession.id}-exercise-${exerciseId}`,
            exerciseId: parseInt(exerciseId),
            exercise: createPlaceholderExercise(parseInt(exerciseId)),
            scheduledDate: backendSession.date,
            targetSets: sets.length,                           // ✅ FIXED: targetSets
            targetReps: 10,                                    // ✅ FIXED: targetReps as number
            targetWeightUnit: 'lbs',                           // ✅ NEW: Weight unit
            completed: sets.every(set => set.completed),
            createdAt: backendSession.createdAt,
            userId: '1' // Would come from auth context
        };

        return {
            id: `workout-exercise-${exerciseId}`,
            scheduledExercise: placeholderScheduledExercise,
            sets: sets,
            completed: sets.every(set => set.completed),
            skipped: false,
            startedAt: sets.length > 0 ? sets[0].completedAt : undefined,
            completedAt: sets.every(set => set.completed) ? sets[sets.length - 1].completedAt : undefined
        };
    });

    return {
        id: backendSession.id.toString(),
        date: backendSession.date,
        exercises: exercises,
        status: determineWorkoutStatus(exercises, backendSession),
        startedAt: new Date(backendSession.createdAt),
        totalDurationMinutes: backendSession.totalDurationMinutes,
        currentExerciseIndex: findCurrentExerciseIndex(exercises),
        currentSetIndex: findCurrentSetIndex(exercises),
        notes: backendSession.notes
    };
};

/**
 * Groups performance records by exercise ID for easier processing
 */
const groupPerformanceByExercise = (records: PerformanceResponse[]): Record<string, PerformanceResponse[]> => {
    return records.reduce((acc, record) => {
        const exerciseId = record.exerciseId.toString();
        if (!acc[exerciseId]) acc[exerciseId] = [];
        acc[exerciseId].push(record);
        return acc;
    }, {} as Record<string, PerformanceResponse[]>);
};

/**
 * ✅ UPDATED: Transforms performance records into workout sets with updated field names
 */
const transformPerformanceRecordsToSets = (records: PerformanceResponse[]): WorkoutSet[] => {
    return records
        .sort((a, b) => a.setNumber - b.setNumber) // Ensure proper set order
        .map(record => ({
            id: record.id.toString(),
            setNumber: record.setNumber,
            targetReps: record.reps || 10,                     // ✅ FIXED: targetReps as number
            actualReps: record.reps,
            targetWeight: record.weight,
            actualWeight: record.weight,
            targetWeightUnit: 'lbs',                           // ✅ NEW: Weight unit default
            targetRpe: record.perceivedExertion,
            actualRpe: record.perceivedExertion,
            restSeconds: record.restSeconds,
            completed: true, // If there's a performance record, the set was completed
            notes: record.notes,
            completedAt: new Date(record.createdAt)
        }));
};

/**
 * Creates a placeholder exercise for transformation purposes
 * In real implementation, you'd fetch actual exercise details
 */
const createPlaceholderExercise = (exerciseId: number): Exercise => ({
    // Basic identification
    id: exerciseId,
    name: `Exercise ${exerciseId}`,
    exerciseName: `Exercise ${exerciseId}`, // Provide both naming conventions
    emoji: '💪',
    description: 'Exercise details to be loaded',

    // Exercise classification - use proper enum values
    exerciseType: 'STRENGTH' as ExerciseType,
    isCardio: false, // Default to strength training
    isIsometric: false,
    exerciseTypeDisplay: 'Strength Training',
    difficultyLevel: 'INTERMEDIATE' as DifficultyLevel,

    // Numerical fields with sensible defaults
    estimatedDurationMinutes: 15,
    estimatedCalories: 100,

    // Array fields - empty but defined
    targetMuscleGroups: [],
    equipmentRequired: [],
    benefits: [],
    tips: [],

    // Optional fields
    videoUrl: null,

    // Rating and popularity with neutral defaults
    averageRating: 0,
    totalRatings: 0,
    usageCount: 0,
    isPopular: false,
    isHighlyRated: false,

    // Feature flags with safe defaults
    canDoAtHome: true,
    requiresEquipment: false,

    // Metadata with platform defaults
    createdByProfessional: false,
    createdBy: 'Platform',
    published: true
});

/**
 * Determines workout status based on exercise completion
 */
const determineWorkoutStatus = (
    exercises: WorkoutExercise[],
    backendSession: WorkoutSessionResponse
): 'not_started' | 'in_progress' | 'paused' | 'completed' | 'cancelled' => {
    if (exercises.length === 0) return 'not_started';

    const completedExercises = exercises.filter(ex => ex.completed).length;
    const totalExercises = exercises.length;

    if (completedExercises === totalExercises) return 'completed';
    if (completedExercises > 0) return 'in_progress';
    return 'not_started';
};

/**
 * Finds the current exercise index for workout progression
 */
const findCurrentExerciseIndex = (exercises: WorkoutExercise[]): number => {
    const inProgressIndex = exercises.findIndex(ex => !ex.completed && !ex.skipped);
    return inProgressIndex >= 0 ? inProgressIndex : exercises.length - 1;
};

/**
 * Finds the current set index within the current exercise
 */
const findCurrentSetIndex = (exercises: WorkoutExercise[]): number => {
    const currentExerciseIndex = findCurrentExerciseIndex(exercises);
    if (currentExerciseIndex < 0) return 0;

    const currentExercise = exercises[currentExerciseIndex];
    const inProgressSetIndex = currentExercise.sets.findIndex(set => !set.completed);
    return inProgressSetIndex >= 0 ? inProgressSetIndex : currentExercise.sets.length - 1;
};

// ==================== REVERSE TRANSFORMATIONS (Frontend to Backend) ====================

/**
 * ✅ UPDATED: Transforms frontend workout set data into backend performance request with updated field names
 */
export const transformWorkoutSetToPerformanceRequest = (
    workoutSet: WorkoutSet,
    exerciseId: number,
    workoutSessionId: number
) => {
    return {
        workoutLogId: workoutSessionId, // Backend calls it workoutLogId
        exerciseId: exerciseId,
        setNumber: workoutSet.setNumber,
        reps: workoutSet.actualReps,
        weight: workoutSet.actualWeight,
        perceivedExertion: workoutSet.actualRpe,
        formRating: 3, // Default form rating - could be added to UI later
        restSeconds: workoutSet.restSeconds,
        notes: workoutSet.notes,
        achievementStatus: determineAchievementStatus(workoutSet)
    };
};

/**
 * ✅ UPDATED: Determines achievement status based on target vs actual performance
 */
const determineAchievementStatus = (workoutSet: WorkoutSet): 'NOT_SET' | 'EXCEEDED' | 'MET' | 'BELOW_TARGET' | 'PARTIAL' => {
    if (!workoutSet.targetReps || !workoutSet.actualReps) return 'NOT_SET';

    const targetReps = workoutSet.targetReps; // Now already a number
    const actualReps = workoutSet.actualReps;

    if (actualReps > targetReps) return 'EXCEEDED';
    if (actualReps === targetReps) return 'MET';
    if (actualReps >= targetReps * 0.75) return 'PARTIAL'; // 75% of target
    return 'BELOW_TARGET';
};

// ==================== UTILITY FUNCTIONS ====================

/**
 * Transforms ISO date string to JavaScript Date object
 */
export const parseBackendDate = (dateString: DateString | DateTimeString): Date => {
    return new Date(dateString);
};

/**
 * Transforms JavaScript Date to backend-expected ISO string
 */
export const formatDateForBackend = (date: Date): DateString => {
    return date.toISOString().split('T')[0]; // Returns YYYY-MM-DD format
};

/**
 * Transforms JavaScript Date to backend-expected ISO datetime string
 */
export const formatDateTimeForBackend = (date: Date): DateTimeString => {
    return date.toISOString(); // Returns full ISO datetime format
};

/**
 * ✅ NEW: Weight conversion utilities for American users
 */
export const convertWeight = (weight: number, fromUnit: 'kg' | 'lbs', toUnit: 'kg' | 'lbs'): number => {
    if (fromUnit === toUnit) return weight;
    if (fromUnit === 'lbs' && toUnit === 'kg') {
        return Math.round(weight * 0.453592 * 100) / 100;
    }
    if (fromUnit === 'kg' && toUnit === 'lbs') {
        return Math.round(weight * 2.20462 * 100) / 100;
    }
    return weight;
};

/**
 * ✅ NEW: Format weight with appropriate precision for display
 */
export const formatWeight = (weight: number, unit: 'kg' | 'lbs'): string => {
    if (unit === 'lbs') {
        return weight % 1 === 0 ? weight.toString() : weight.toFixed(1);
    } else {
        return weight % 1 === 0 ? weight.toString() : weight.toFixed(2);
    }
};

/**
 * Validates that required fields are present for transformation
 */
export const validateTransformationData = <T>(data: T, requiredFields: (keyof T)[]): boolean => {
    return requiredFields.every(field => data[field] !== undefined && data[field] !== null);
};

// ==================== DEBUGGING HELPERS ====================

/**
 * Logs transformation details for debugging integration issues
 */
export const logTransformation = (operation: string, input: any, output: any): void => {
    if (process.env.NODE_ENV === 'development') {
        console.group(`🔄 Transformation: ${operation}`);
        console.log('Input:', input);
        console.log('Output:', output);
        console.groupEnd();
    }
};

/**
 * Validates that transformation preserved essential data
 */
export const validateTransformationResult = <T>(result: T, originalId: number | string): T => {
    if (!result) {
        throw new Error(`Transformation failed: result is null/undefined for ID ${originalId}`);
    }
    return result;
};