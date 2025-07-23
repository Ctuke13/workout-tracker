// src/services/transformers.ts - Data Transformation Layer

import {
    // Backend types (what Spring Boot sends) - keep these from api.ts
    BackendExercise,
    ScheduledWorkoutResponse,
    WorkoutSessionResponse,
    PerformanceResponse,
    WorkoutPlanInfo,

    // Frontend types from API layer - only the ones that represent API contracts
    ScheduledExercise,
    WorkoutSession,
    WorkoutSet,
    WorkoutExercise,

    // Utility types
    DateString,
    DateTimeString
} from '../types/api';

// Import the authoritative Exercise interface from your domain model
import { Exercise, DifficultyLevel, ExerciseType } from '../types/exercise';
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

        // Rating and popularity fields - use exact field names from Exercise interface
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

/**
 * Formats duration from minutes to user-friendly string
 */
const formatDuration = (minutes: number | null | undefined): string => {
    if (minutes === null || minutes === undefined) return 'Variable';
    if (minutes < 60) return `${minutes} mins`;
    const hours = Math.floor(minutes / 60);
    const remainingMins = minutes % 60;
    return remainingMins > 0 ? `${hours}h ${remainingMins}m` : `${hours}h`;
};

/**
 * Formats calories from backend number to user-friendly range string
 */
const formatCalories = (calories: number | null | undefined): string => {
    if (calories === null || calories === undefined) return 'Varies';
    const lower = Math.round(calories * 0.8);
    const upper = Math.round(calories * 1.2);
    return `${lower}-${upper}/hr`;
};

/**
 * Formats equipment from backend arrays to user-friendly string
 */
const formatEquipment = (equipmentArray: string[], equipmentSummary: string): string => {
    // Use the backend's summary if available, otherwise format the array
    if (equipmentSummary && equipmentSummary !== '') return equipmentSummary;
    if (!equipmentArray || equipmentArray.length === 0) return 'No Equipment';
    return equipmentArray.map(eq => formatEquipmentName(eq)).join(', ');
};

/**
 * Converts backend equipment codes to user-friendly names
 */
const formatEquipmentName = (equipment: string): string => {
    const equipmentMap: Record<string, string> = {
        'dumbbells': 'Dumbbells',
        'barbell': 'Barbell',
        'kettlebell': 'Kettlebell',
        'resistance_bands': 'Resistance Bands',
        'yoga_mat': 'Yoga Mat',
        'jump_rope': 'Jump Rope',
        'pull_up_bar': 'Pull-up Bar',
        'bench': 'Bench',
        'cable_machine': 'Cable Machine',
        'treadmill': 'Treadmill',
        'stationary_bike': 'Stationary Bike',
        'foam_roller': 'Foam Roller'
    };
    return equipmentMap[equipment] || equipment.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
};

// ==================== SCHEDULED WORKOUT TRANSFORMATIONS ====================

/**
 * Transforms backend scheduled workout data into frontend scheduled exercises
 * This is complex because backend thinks in terms of "workout plans with multiple exercises"
 * while frontend thinks in terms of "individual scheduled exercises"
 */
export const transformScheduledWorkoutToExercises = (
    scheduledWorkout: ScheduledWorkoutResponse
): ScheduledExercise[] => {
    // If this scheduled workout doesn't have a workout plan, we can't transform it
    if (!scheduledWorkout.workoutPlan) {
        console.warn('ScheduledWorkout has no workoutPlan:', scheduledWorkout.id);
        return [];
    }

    // Extract exercises from the workout plan and create individual scheduled exercises
    // Note: This assumes your backend includes exercise details in WorkoutPlanInfo
    // If not, you'll need to fetch exercise details separately
    const workoutPlan = scheduledWorkout.workoutPlan;

    // For now, we'll create a placeholder since we need more backend API details
    // In the real implementation, you'll need exercise details from the workout plan
    const placeholderExercise: Exercise = {
        id: 0,
        name: `${workoutPlan.name} - Exercise`,
        exerciseName: `${workoutPlan.name} - Exercise`, // Add for backward compatibility
        emoji: '💪',
        description: workoutPlan.description || 'Part of workout plan',

        // Use the raw enum value, not the transformed string
        difficultyLevel: workoutPlan.difficulty, // This is already a DifficultyLevel enum

        // Exercise classification fields
        exerciseType: 'STRENGTH' as ExerciseType,
        isCardio: false, // Default assumption - could be derived from workoutPlan if it has this info
        exerciseTypeDisplay: 'Strength Training',

        // Numerical fields with defaults
        estimatedDurationMinutes: workoutPlan.estimatedDurationMinutes || 15,
        estimatedCalories: 100, // Default estimate

        // Array fields
        targetMuscleGroups: [],
        equipmentRequired: [],
        benefits: [],
        tips: [],

        // Optional fields
        videoUrl: null,

        // Rating and popularity fields
        averageRating: 0,
        totalRatings: 0,
        usageCount: 0,
        isPopular: false,
        isHighlyRated: false,

        // Feature flags
        canDoAtHome: true,
        requiresEquipment: false,

        // Metadata
        createdByProfessional: false,
        createdBy: 'Platform',
        published: true
    };

    // Create scheduled exercise representing this workout plan
    const scheduledExercise: ScheduledExercise = {
        id: scheduledWorkout.id.toString(),
        exerciseId: workoutPlan.id,
        exercise: placeholderExercise,
        scheduledDate: scheduledWorkout.scheduledDate,
        sets: 3, // Default - should come from workout plan configuration
        reps: '8-12', // Default - should come from workout plan configuration
        weight: undefined,
        restSeconds: 60, // Default
        notes: scheduledWorkout.customNotes,
        completed: scheduledWorkout.status === 'COMPLETED',
        createdAt: scheduledWorkout.createdAt,
        userId: scheduledWorkout.user.id.toString()
    };

    return [scheduledExercise];
};

/**
 * Transforms multiple scheduled workouts into the flat exercise list your calendar expects
 */
export const transformScheduledWorkoutsToCalendarData = (
    scheduledWorkouts: ScheduledWorkoutResponse[]
): ScheduledExercise[] => {
    return scheduledWorkouts.flatMap(workout => transformScheduledWorkoutToExercises(workout));
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

        // Create placeholder scheduled exercise
        const placeholderScheduledExercise: ScheduledExercise = {
            id: `session-${backendSession.id}-exercise-${exerciseId}`,
            exerciseId: parseInt(exerciseId),
            exercise: createPlaceholderExercise(parseInt(exerciseId)),
            scheduledDate: backendSession.date,
            sets: sets.length,
            reps: '8-12',
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
 * Transforms performance records into workout sets
 */
const transformPerformanceRecordsToSets = (records: PerformanceResponse[]): WorkoutSet[] => {
    return records
        .sort((a, b) => a.setNumber - b.setNumber) // Ensure proper set order
        .map(record => ({
            id: record.id.toString(),
            setNumber: record.setNumber,
            targetReps: record.reps?.toString() || '8-12',
            actualReps: record.reps,
            targetWeight: record.weight,
            actualWeight: record.weight,
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
 * Transforms frontend workout set data into backend performance request
 * This is used when users complete sets and we need to save to backend
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
 * Determines achievement status based on target vs actual performance
 */
const determineAchievementStatus = (workoutSet: WorkoutSet): 'NOT_SET' | 'EXCEEDED' | 'MET' | 'BELOW_TARGET' | 'PARTIAL' => {
    if (!workoutSet.targetReps || !workoutSet.actualReps) return 'NOT_SET';

    const targetReps = parseInt(workoutSet.targetReps.split('-')[0]); // Take lower bound of range
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