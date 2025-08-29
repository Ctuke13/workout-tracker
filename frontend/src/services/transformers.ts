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
    WorkoutSession,
    WorkoutResults,
    getWorkoutTrackingType
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
        case 'BEGINNER':
            return 'Beginner';
        case 'INTERMEDIATE':
            return 'Intermediate';
        case 'ADVANCED':
            return 'Advanced';
        default:
            return 'Beginner';
    }
};

// ==================== SCHEDULED WORKOUT TRANSFORMATIONS ====================

/**
 * ✅ FIXED: Handle both individual exercises and workout plan responses with updated field names
 */
export const transformScheduledWorkoutToExercises = (
    scheduledWorkout: ScheduledWorkoutResponse
): ScheduledExercise[] => {

    console.log('🔍 Transforming workout with REAL exercise data:', {
        id: scheduledWorkout.id,
        hasExercise: !!scheduledWorkout.exercise,
        hasWorkoutPlan: !!scheduledWorkout.workoutPlan
    });

    if (scheduledWorkout.workoutPlan) {
        // Handle workout plan workouts
        const workoutPlan = scheduledWorkout.workoutPlan;
        let exercise: Exercise;

        if (scheduledWorkout.exercise) {
            // ✅ USE REAL EXERCISE DATA from your database!
            exercise = transformBackendExerciseToFrontend(scheduledWorkout.exercise);
            console.log('✅ Using REAL exercise data from database:', {
                name: exercise.name,
                isCardio: exercise.isCardio,
                isIsometric: exercise.isIsometric,
                exerciseType: exercise.exerciseType
            });
        } else {
            // Fallback placeholder (should rarely happen now with V013 migration)
            console.warn('⚠️ No exercise data available, using placeholder');
            exercise = createSafeExercisePlaceholder(workoutPlan.id, workoutPlan.name || 'Workout Plan');
        }

        const scheduledExercise: ScheduledExercise = {
            id: scheduledWorkout.id.toString(),
            exerciseId: scheduledWorkout.exercise?.id || workoutPlan.id,
            exercise: exercise, // ✅ Now has correct isCardio/isIsometric flags!
            scheduledDate: scheduledWorkout.scheduledDate,

            // ✅ SMART CONFIGURATION based on ACTUAL exercise type
            targetSets: scheduledWorkout.targetSets || getDefaultSetsForExercise(exercise),
            targetReps: scheduledWorkout.targetReps ? parseInt(scheduledWorkout.targetReps) : getDefaultRepsForExercise(exercise),
            targetWeight: scheduledWorkout.targetWeight,
            targetWeightUnit: 'lbs', // Default to lbs for American users
            restSeconds: scheduledWorkout.restSeconds || getDefaultRestForExercise(exercise),
            targetRpe: scheduledWorkout.targetRpe,
            tempo: scheduledWorkout.tempo,

            // ✅ CARDIO FIELDS
            targetDurationMinutes: scheduledWorkout.targetDurationMinutes,
            targetDistance: scheduledWorkout.targetDistanceKm ?
                Math.round(scheduledWorkout.targetDistanceKm * 0.621371 * 100) / 100 : // Convert km to miles
                undefined,
            targetDistanceUnit: 'miles',
            targetDistanceKm: scheduledWorkout.targetDistanceKm,
            targetPace: scheduledWorkout.targetPace,

            // ✅ ISOMETRIC FIELDS
            holdDurationSeconds: scheduledWorkout.holdDurationSeconds,

            // Common fields
            notes: scheduledWorkout.customNotes,
            completed: scheduledWorkout.status === 'COMPLETED',
            status: scheduledWorkout.status,
            createdAt: scheduledWorkout.createdAt,
            userId: scheduledWorkout.user?.id?.toString() || '1'
        };

        console.log('✅ PERFECT: Exercise with correct type detection:', {
            exerciseName: exercise.name,
            isCardio: exercise.isCardio,
            isIsometric: exercise.isIsometric,
            exerciseType: exercise.exerciseType,
            workoutTrackingMode: getWorkoutTrackingType(exercise) // From exercise.ts
        });

        return [scheduledExercise];
    }

    // Handle individual exercises
    console.log('🔍 Processing individual exercise:', scheduledWorkout.id);

    let exercise: Exercise;

    if (scheduledWorkout.exercise) {
        // ✅ USE REAL EXERCISE DATA from your database!
        exercise = transformBackendExerciseToFrontend(scheduledWorkout.exercise);
        console.log('✅ Using REAL individual exercise data:', {
            name: exercise.name,
            isCardio: exercise.isCardio,
            isIsometric: exercise.isIsometric,
            exerciseType: exercise.exerciseType
        });
    } else {
        // True fallback
        console.warn('⚠️ No individual exercise data available');
        exercise = createSafeExercisePlaceholder(0, 'Individual Exercise');
    }

    const scheduledExercise: ScheduledExercise = {
        id: scheduledWorkout.id.toString(),
        exerciseId: scheduledWorkout.exercise?.id || 0,
        exercise: exercise, // ✅ Now has correct exercise type flags!
        scheduledDate: scheduledWorkout.scheduledDate,

        // Smart defaults based on actual exercise type
        targetSets: scheduledWorkout.targetSets || getDefaultSetsForExercise(exercise),
        targetReps: scheduledWorkout.targetReps ? parseInt(scheduledWorkout.targetReps) : getDefaultRepsForExercise(exercise),
        targetWeight: scheduledWorkout.targetWeight,
        targetWeightUnit: 'lbs',
        restSeconds: scheduledWorkout.restSeconds || getDefaultRestForExercise(exercise),
        targetRpe: scheduledWorkout.targetRpe,
        tempo: scheduledWorkout.tempo,

        // Cardio fields
        targetDurationMinutes: scheduledWorkout.targetDurationMinutes,
        targetDistance: scheduledWorkout.targetDistanceKm ?
            Math.round(scheduledWorkout.targetDistanceKm * 0.621371 * 100) / 100 :
            undefined,
        targetDistanceUnit: 'miles',
        targetDistanceKm: scheduledWorkout.targetDistanceKm,
        targetPace: scheduledWorkout.targetPace,

        // Isometric fields
        holdDurationSeconds: scheduledWorkout.holdDurationSeconds,

        notes: scheduledWorkout.customNotes,
        completed: scheduledWorkout.status === 'COMPLETED',
        createdAt: scheduledWorkout.createdAt,
        userId: scheduledWorkout.user?.id?.toString() || '1'
    };

    console.log('✅ PERFECT: Individual exercise with correct type detection:', {
        exerciseName: exercise.name,
        isCardio: exercise.isCardio,
        isIsometric: exercise.isIsometric,
        exerciseType: exercise.exerciseType
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
 * Transforms WorkoutExercise data into WorkoutResults format for performance tracking
 * This creates the performance data structure expected by the performance stats components
 */
export const transformToWorkoutResults = (
    exercise: WorkoutExercise,
    workoutSession: WorkoutSession
): WorkoutResults => {
    return {
        exerciseId: exercise.scheduledExercise.exerciseId.toString(),
        scheduledExerciseId: exercise.scheduledExercise.id,
        workoutSessionId: workoutSession.id,
        completedAt: (exercise.completedAt || workoutSession.completedAt || new Date()).toISOString(),
        totalDurationMinutes: calculateExerciseDuration(exercise),
        performanceRating: calculatePerformanceRating(exercise),

        // ✅ Transform sets to WorkoutResults format with required arrays
        sets: exercise.sets.map(set => ({
            setNumber: set.setNumber,
            targetReps: set.targetReps,
            actualReps: set.actualReps || 0,
            targetWeight: set.targetWeight,
            actualWeight: set.actualWeight,
            targetWeightUnit: set.targetWeightUnit || 'lbs',
            rpe: set.actualRpe,
            restSeconds: set.restSeconds,
            completed: set.completed,
            notes: set.notes,
            completedAt: set.completedAt?.toISOString(),
            performanceVsTarget: calculateSetPerformance(set),
        })),

        // ✅ ALWAYS provide empty arrays (backend will populate with actual data)
        personalRecords: [],
        improvements: [],

        // Optional workout-level data
        notes: exercise.notes,
        workoutNotes: workoutSession.notes,
        perceivedEffort: undefined, // Could be added to WorkoutSession later
        mood: undefined,           // Could be added to WorkoutSession later
        location: undefined,       // Could be added to WorkoutSession later
    };
};

/**
 * Creates a default WorkoutResults object with required arrays
 * Used as fallback when performance data is not available
 */
export const createDefaultWorkoutResults = (
    exerciseId: string,
    scheduledExerciseId: string
): WorkoutResults => {
    return {
        exerciseId,
        scheduledExerciseId,
        completedAt: new Date().toISOString(),
        totalDurationMinutes: 0,
        performanceRating: 'PARTIAL',

        // ✅ ALWAYS provide empty arrays - never undefined
        sets: [],
        personalRecords: [],
        improvements: [],

        // Optional fields
        notes: undefined,
        workoutNotes: undefined,
        mood: undefined,
        location: undefined,
        perceivedEffort: undefined,
    };
};

/**
 * Transforms backend performance data into frontend WorkoutResults format
 * Used when fetching completed workout performance from the API
 */
export const transformBackendPerformanceToWorkoutResults = (
    backendData: any
): WorkoutResults => {
    return {
        exerciseId: backendData.exerciseId,
        workoutSessionId: backendData.workoutSessionId,
        scheduledExerciseId: backendData.scheduledExerciseId,
        completedAt: backendData.completedAt,
        totalDurationMinutes: backendData.totalDurationMinutes,
        performanceRating: backendData.performanceRating,

        // ✅ ENSURE arrays are never null/undefined
        sets: backendData.sets || [],
        personalRecords: backendData.personalRecords || [],
        improvements: backendData.improvements || [],

        // Optional fields remain optional
        actualDurationMinutes: backendData.actualDurationMinutes,
        actualDistanceKm: backendData.actualDistanceKm,
        actualPace: backendData.actualPace,
        averageHeartRate: backendData.averageHeartRate,
        caloriesBurned: backendData.caloriesBurned,
        actualHoldDurations: backendData.actualHoldDurations,
        averageHoldTime: backendData.averageHoldTime,
        longestHoldSeconds: backendData.longestHoldSeconds,
        strengthMetrics: backendData.strengthMetrics,
        cardioMetrics: backendData.cardioMetrics,
        isometricMetrics: backendData.isometricMetrics,
        notes: backendData.notes,
        workoutNotes: backendData.workoutNotes,
        mood: backendData.mood,
        location: backendData.location,
        perceivedEffort: backendData.perceivedEffort,
    };
};

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


// ==================== PERFORMANCE CALCULATION HELPERS ====================

/**
 * Helper function to calculate exercise duration for performance tracking
 */
export function calculateExerciseDuration(exercise: WorkoutExercise): number {
    if (exercise.startedAt && exercise.completedAt) {
        return Math.round((exercise.completedAt.getTime() - exercise.startedAt.getTime()) / 1000 / 60);
    }

    // Estimate based on exercise type and sets
    const restTime = exercise.sets.reduce((total, set) => total + (set.restSeconds || 0), 0) / 60;
    const exerciseTime = exercise.scheduledExercise.exercise.estimatedDurationMinutes ||
        (exercise.scheduledExercise.exercise.isCardio ? 20 : exercise.sets.length * 2);

    return Math.round(exerciseTime + restTime);
}

/**
 * Helper function to calculate performance rating for an exercise
 */
export function calculatePerformanceRating(exercise: WorkoutExercise): 'EXCEEDED' | 'MET' | 'BELOW_TARGET' | 'PARTIAL' {
    const completedSets = exercise.sets.filter(set => set.completed);

    if (completedSets.length === 0) return 'PARTIAL';
    if (completedSets.length < exercise.sets.length) return 'PARTIAL';

    // ✅ Enhanced performance calculation
    let totalPerformanceScore = 0;
    let scoreCount = 0;

    completedSets.forEach(set => {
        // Rep performance score (0-2 scale: 0=below, 1=met, 2=exceeded)
        if (set.actualReps !== undefined && set.targetReps > 0) {
            const repRatio = set.actualReps / set.targetReps;
            if (repRatio >= 1.2) totalPerformanceScore += 2;      // Exceeded by 20%+
            else if (repRatio >= 1.0) totalPerformanceScore += 1; // Met target
            else if (repRatio >= 0.8) totalPerformanceScore += 0.5; // Close to target
            else totalPerformanceScore += 0;                      // Below target
            scoreCount++;
        }

        // Weight performance score (if applicable)
        if (set.actualWeight !== undefined && set.targetWeight !== undefined && set.targetWeight > 0) {
            const weightRatio = set.actualWeight / set.targetWeight;
            if (weightRatio >= 1.1) totalPerformanceScore += 2;      // Exceeded by 10%+
            else if (weightRatio >= 1.0) totalPerformanceScore += 1; // Met target
            else if (weightRatio >= 0.9) totalPerformanceScore += 0.5; // Close to target
            else totalPerformanceScore += 0;                         // Below target
            scoreCount++;
        }
    });

    if (scoreCount === 0) return 'MET'; // Default if no measurable metrics

    const averageScore = totalPerformanceScore / scoreCount;

    // Convert average score to rating
    if (averageScore >= 1.5) return 'EXCEEDED';   // Average exceeded performance
    if (averageScore >= 0.9) return 'MET';        // Average met performance
    if (averageScore >= 0.5) return 'BELOW_TARGET'; // Below but partial
    return 'PARTIAL';                              // Significantly below
}

/**
 * Helper function to calculate set performance vs target
 */
export function calculateSetPerformance(set: WorkoutSet): 'EXCEEDED' | 'MET' | 'BELOW_TARGET' | 'PARTIAL' {
    if (!set.completed) return 'PARTIAL';

    const repsMet = set.actualReps !== undefined && set.actualReps >= set.targetReps;
    const repsExceeded = set.actualReps !== undefined && set.actualReps > set.targetReps * 1.1;

    if (repsExceeded) return 'EXCEEDED';
    if (repsMet) return 'MET';
    if (set.actualReps !== undefined && set.actualReps >= set.targetReps * 0.8) return 'BELOW_TARGET';
    return 'PARTIAL';
}

/**
 * Calculate performance score (0-100) based on workout results
 */
export const calculateWorkoutPerformanceScore = (workoutResults: WorkoutResults): number => {
    let score = 0;
    let factors = 0;

    // Factor 1: Completion rate (40% weight)
    const completedSets = workoutResults.sets.filter(set => set.completed);
    const completionRate = workoutResults.sets.length > 0 ?
        (completedSets.length / workoutResults.sets.length) * 100 : 0;
    score += completionRate * 0.4;
    factors++;

    // Factor 2: Target achievement (35% weight)
    if (completedSets.length > 0) {
        const targetAchievement = completedSets.reduce((avg, set) => {
            const repsAchievement = set.targetReps > 0 ?
                Math.min((set.actualReps || 0) / set.targetReps, 1.2) * 100 : 100;
            return avg + repsAchievement;
        }, 0) / completedSets.length;

        score += targetAchievement * 0.35;
    } else {
        score += 50 * 0.35; // Partial credit if no sets completed
    }

    // Factor 3: Personal records bonus (15% weight)
    const prBonus = workoutResults.personalRecords.length > 0 ? 100 : 0;
    score += prBonus * 0.15;

    // Factor 4: Consistency with effort (10% weight)
    const effortScore = workoutResults.perceivedEffort ?
        (workoutResults.perceivedEffort / 10) * 100 : 70;
    score += effortScore * 0.1;

    return Math.round(Math.max(0, Math.min(100, score)));
};

/**
 * Format performance data for display
 */
export const formatPerformanceData = (workoutResults: WorkoutResults): {
    score: number;
    grade: string;
    summary: string;
    highlights: string[];
} => {
    const score = calculateWorkoutPerformanceScore(workoutResults);

    let grade = 'F';
    if (score >= 90) grade = 'A+';
    else if (score >= 85) grade = 'A';
    else if (score >= 80) grade = 'B+';
    else if (score >= 75) grade = 'B';
    else if (score >= 70) grade = 'C+';
    else if (score >= 65) grade = 'C';
    else if (score >= 60) grade = 'D';

    const highlights: string[] = [];

    if (workoutResults.personalRecords.length > 0) {
        highlights.push(`🏆 ${workoutResults.personalRecords.length} Personal Record${workoutResults.personalRecords.length > 1 ? 's' : ''}`);
    }

    if (workoutResults.improvements.length > 0) {
        highlights.push(`📈 ${workoutResults.improvements.length} Improvement${workoutResults.improvements.length > 1 ? 's' : ''}`);
    }

    const completedSets = workoutResults.sets.filter(set => set.completed);
    if (completedSets.length === workoutResults.sets.length && workoutResults.sets.length > 0) {
        highlights.push('✅ All sets completed');
    }

    const summary = `${grade} Performance (${score}/100)`;

    return {score, grade, summary, highlights};
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

// ==================== HELPER FUNCTIONS ====================

/**
 * ✅ NEW: Safe defaults based on exercise type
 */
const getDefaultSetsForExercise = (exercise: Exercise): number => {
    if (exercise.isCardio) {
        // Check if it's interval-style cardio
        const name = exercise.name.toLowerCase();
        const isInterval = name.includes('burpees') || name.includes('hiit') || name.includes('interval');
        return isInterval ? 4 : 1;
    }
    if (exercise.isIsometric) return 3;
    return 3; // Strength default
};

const getDefaultRepsForExercise = (exercise: Exercise): number => {
    if (exercise.isCardio) {
        return exercise.estimatedDurationMinutes || 20; // Duration for cardio
    }
    if (exercise.isIsometric) {
        return 30; // Hold duration in seconds
    }
    return 10; // Standard reps for strength
};

const getDefaultRestForExercise = (exercise: Exercise): number => {
    if (exercise.isCardio) return 0; // No rest for single cardio sessions
    if (exercise.isIsometric) return 60; // Standard rest for holds
    return 90; // Standard rest for strength
};

/**
 * ✅ NEW: Create safe exercise placeholders without guessing types
 */
const createSafeExercisePlaceholder = (id: number, name: string): Exercise => ({
    id: id,
    name: name,
    exerciseName: name,
    emoji: '💪',
    description: 'Exercise details to be loaded',
    exerciseType: 'STRENGTH' as ExerciseType,

    // ✅ SAFE DEFAULTS - Don't guess exercise types
    isCardio: false,
    isIsometric: false,

    exerciseTypeDisplay: 'Strength Training',
    difficultyLevel: 'INTERMEDIATE' as DifficultyLevel,
    estimatedDurationMinutes: 15,
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
});

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