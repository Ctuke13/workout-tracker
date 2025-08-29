// =============================================================================
// CALENDAR AND SCHEDULING INTERFACES
// =============================================================================

import {WorkoutExercise} from "@/types/api";

export interface ScheduledExercise {
    id: string;
    exerciseId: number;
    exercise: Exercise;
    scheduledDate: string;

    // NEW: Add tracking mode and configuration for modal compatibility
    trackingMode?: 'strength' | 'cardio' | 'isometric';
    configuration?: ExerciseConfiguration;

    // Strength fields
    targetSets?: number;
    targetReps?: number;
    targetWeight?: number;
    targetWeightUnit?: 'kg' | 'lbs';
    restSeconds?: number;
    tempo?: string;
    targetRpe?: number;

    //  Cardio fields
    // ✅ UPDATED: Cardio fields with distance unit support
    targetDurationMinutes?: number;
    targetDistance?: number;
    targetDistanceUnit?: 'km' | 'miles';
    targetPace?: number;

    // Legacy support (deprecated)
    targetDistanceKm?: number;

    // Isometric fields
    holdDurationSeconds?: number;

    // Common fields
    notes?: string;
    completed: boolean;
    status?: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'SKIPPED' | 'RESCHEDULED';
    createdAt: string;
    userId: string;
}

export interface ScheduledExerciseWithResults extends ScheduledExercise {
    workoutResults?: WorkoutResults;
}

export interface CalendarDay {
    date: Date;
    dateString: string;
    isToday: boolean;
    isPast: boolean;
    exercises: ScheduledExercise[];
}

export interface CalendarEvent {
    id: string;
    title: string;
    date: Date;
    exercise: Exercise;
    completed: boolean;
}

export interface WorkoutStats {
    totalWorkouts: number;
    completedWorkouts: number;
    completionRate: number;
    weeklyGoal: number;
    currentStreak: number;
    bestStreak: number;
    totalExercisesCompleted: number;
    averageWorkoutDuration: number;
}

export interface WorkoutResults {
    // Basic completion info
    exerciseId: string;
    workoutSessionId?: string;
    scheduledExerciseId?: string;
    completedAt: string;
    totalDurationMinutes: number;

    // Performance rating
    performanceRating: 'EXCEEDED' | 'MET' | 'BELOW_TARGET' | 'PARTIAL';

    // ✅ CHANGE 1: Make sets array REQUIRED (remove the ? after sets)
    sets: {
        setNumber: number;
        targetReps: number;
        actualReps: number;
        targetWeight?: number;
        actualWeight?: number;
        targetWeightUnit: 'kg' | 'lbs';
        rpe?: number;
        formRating?: number;
        restSeconds?: number;
        setDurationSeconds?: number;
        completed: boolean;
        performanceVsTarget?: 'EXCEEDED' | 'MET' | 'BELOW_TARGET' | 'PARTIAL';
        notes?: string;
        cardioData?: {
            durationMinutes: number;
            distanceKm?: number;
            pace?: number;
            averageHeartRate?: number;
            caloriesBurned?: number;
        };
        isometricData?: {
            holdDurationSeconds: number;
            targetHoldSeconds: number;
        };
    }[]; // ← REQUIRED array (no ?)

    // Cardio specific (existing fields enhanced)
    actualDurationMinutes?: number;
    actualDistanceKm?: number;
    actualPace?: number;
    averageHeartRate?: number;
    caloriesBurned?: number;

    // Isometric specific (existing fields enhanced)
    actualHoldDurations?: number[];
    averageHoldTime?: number;
    longestHoldSeconds?: number;

    // Exercise type specific metrics (keep optional)
    strengthMetrics?: {
        totalVolume: number;
        averageRpe: number;
        maxWeight: number;
        totalReps: number;
        completionRate: number;
        strengthGains?: {
            previousMaxWeight?: number;
            weightIncrease?: number;
            previousTotalVolume?: number;
            volumeIncrease?: number;
        };
    };

    cardioMetrics?: {
        totalDurationMinutes: number;
        totalDistanceKm?: number;
        averagePace?: number;
        averageHeartRate?: number;
        totalCaloriesBurned?: number;
        cardioGains?: {
            previousBestPace?: number;
            paceImprovement?: number;
            previousLongestDuration?: number;
            enduranceImprovement?: number;
        };
    };

    isometricMetrics?: {
        totalHoldTimeSeconds: number;
        averageHoldTimeSeconds: number;
        longestHoldSeconds: number;
        completionRate: number;
        isometricGains?: {
            previousLongestHold?: number;
            holdTimeImprovement?: number;
            previousTotalHoldTime?: number;
            totalTimeImprovement?: number;
        };
    };

    // ✅ CHANGE 2: Make personalRecords array REQUIRED (remove the ?)
    personalRecords: {
        type: 'MAX_WEIGHT' | 'MAX_REPS' | 'LONGEST_HOLD' | 'FASTEST_PACE' | 'LONGEST_DISTANCE';
        exerciseId: number;
        exerciseName: string;
        previousValue?: number;
        newValue: number;
        unit: string;
        achievedAt: string;
    }[]; // ← REQUIRED array (no ?)

    // ✅ CHANGE 3: Make improvements array REQUIRED (remove the ?)
    improvements: {
        metric: 'volume' | 'weight' | 'reps' | 'pace' | 'duration' | 'hold_time';
        previousValue: number;
        currentValue: number;
        improvementPercentage: number;
        comparisonPeriod: 'last_workout' | 'last_week' | 'last_month';
    }[]; // ← REQUIRED array (no ?)

    // Enhanced session feedback (keep optional)
    notes?: string;
    workoutNotes?: string;
    mood?: 'ENERGETIC' | 'TIRED' | 'MOTIVATED' | 'FOCUSED' | 'STRESSED' | 'RELAXED' | 'PUMPED' | 'SLUGGISH';
    location?: 'HOME' | 'GYM' | 'OUTDOOR' | 'OFFICE';
    perceivedEffort?: number;
}

// =============================================================================
// CORE ENUMS AND TYPES
// =============================================================================

export type ExerciseType =
    'STRENGTH'
    | 'CARDIO'
    | 'FLEXIBILITY'
    | 'BALANCE'
    | 'PLYOMETRIC'
    | 'REHABILITATION'
    | 'SPORTS_SPECIFIC';
export type DifficultyLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
export type SortOption = 'relevance' | 'rating' | 'popularity' | 'duration' | 'calories' | 'newest';
export type FilterType =
    | 'goal'
    | 'difficulty'
    | 'equipment'
    | 'exerciseType'
    | 'rating'
    | 'duration'
    | 'professional'
    | 'trackingType'
    | 'search'
    | 'favorites'
    | 'muscleGroups';
export type WorkoutTrackingType = 'cardio' | 'isometric' | 'strength';
export type WorkoutSessionStatus = 'not_started' | 'in_progress' | 'paused' | 'completed' | 'cancelled';

export type WeightUnit = 'kg' | 'lbs';
export type DistanceUnit = 'km' | 'miles';

// =============================================================================
// EXERCISE CONFIGURATION INTERFACES - FIXED WITH DISCRIMINATED UNION
// =============================================================================

// Base interface with discriminator
interface BaseExerciseConfiguration {
    trackingMode: WorkoutTrackingType;
    notes?: string;
}

export interface CardioConfiguration extends BaseExerciseConfiguration {
    trackingMode: 'cardio';

    // Primary cardio fields (always shown)
    targetDurationMinutes: number;
    targetDistance?: number;
    targetDistanceUnit: 'km' | 'miles';
    targetPace?: number;

    // Session type configuration
    sessionType?: 'single_session' | 'interval_sets';
    targetSets?: number; // Only used for interval cardio
    restSeconds?: number; // Only used for interval cardio

    // Legacy support
    targetDistanceKm?: number;
}

export interface IsometricConfiguration extends BaseExerciseConfiguration {
    trackingMode: 'isometric';
    targetSets: number;
    holdDurationSeconds: number; // ✅ FIXED: Changed from targetHoldSeconds to holdDurationSeconds
    restSeconds: number;
}

export interface StrengthConfiguration extends BaseExerciseConfiguration {
    trackingMode: 'strength';
    targetSets: number;
    targetReps: number;
    targetWeight?: number;
    targetWeightUnit: 'kg' | 'lbs';
    restSeconds: number;
    targetRpe?: number;
    tempo?: string;
}

export interface CardioSessionType {
    type: 'single_session' | 'interval_sets';
    defaultSets: number;
    showSetsInConfig: boolean;
    description: string;
}

export type ExerciseConfiguration = CardioConfiguration | IsometricConfiguration | StrengthConfiguration;

// =============================================================================
// MAIN EXERCISE INTERFACE
// =============================================================================

export interface Exercise {
    id: number;
    name: string;
    exerciseName?: string; // Backward compatibility
    emoji: string;
    description: string;
    exerciseType: ExerciseType;
    isCardio: boolean; // Critical for workout tracking
    isIsometric: boolean; // Critical for workout tracking
    exerciseTypeDisplay: string;
    difficultyLevel: DifficultyLevel;
    estimatedDurationMinutes: number;
    estimatedCalories: number;
    targetMuscleGroups: string[];
    equipmentRequired: string[];
    benefits: string[];
    tips: string[];
    videoUrl: string | null;
    createdByProfessional: boolean;
    usageCount: number;
    averageRating: number;
    totalRatings: number;
    published: boolean;
    isPopular: boolean;
    isHighlyRated: boolean;
    canDoAtHome: boolean;
    requiresEquipment: boolean;
    createdBy: string;
    isFavorite?: boolean;

    // ✅ ADDED: Missing properties for modal compatibility
    type?: ExerciseType; // Alias for exerciseType
    primaryMuscleGroup?: string; // Primary muscle group
}

// =============================================================================
// WORKOUT TRACKING DATA STRUCTURES
// =============================================================================

// Individual set data for strength exercises
export interface StrengthSet {
    setNumber: number;
    targetReps: number;
    actualReps?: number;
    targetWeight?: number;
    actualWeight?: number;
    targetRpe?: number; // Rate of Perceived Exertion (1-10)
    actualRpe?: number;
    restSeconds?: number;
    tempo?: string; // e.g., "3-1-2-1" (eccentric-pause-concentric-pause)
    completed: boolean;
    notes?: string;
    completedAt?: Date;
}

// Individual set data for isometric exercises
export interface IsometricSet {
    setNumber: number;
    targetHoldSeconds: number;
    actualHoldSeconds?: number;
    restSeconds?: number;
    completed: boolean;
    notes?: string;
    completedAt?: Date;
}

// Cardio workout tracking data
export interface CardioWorkoutData {
    exerciseId: number;
    configuration: CardioConfiguration;
    startTime: Date;
    endTime?: Date;
    actualDurationMinutes?: number;
    actualDistanceKm?: number;
    averagePace?: number; // minutes per km
    maxHeartRate?: number;
    averageHeartRate?: number;
    caloriesBurned?: number;
    notes?: string;
    completed: boolean;
}

// Isometric workout tracking data
export interface IsometricWorkoutData {
    exerciseId: number;
    configuration: IsometricConfiguration;
    startTime: Date;
    endTime?: Date;
    completedSets: IsometricSet[];
    notes?: string;
    completed: boolean;
}

// Strength workout tracking data
export interface StrengthWorkoutData {
    exerciseId: number;
    configuration: StrengthConfiguration;
    startTime: Date;
    endTime?: Date;
    completedSets: StrengthSet[];
    notes?: string;
    completed: boolean;
}

// Unified workout data that handles all tracking modes
export interface UnifiedWorkoutData {
    exerciseId: number;
    exercise: Exercise;
    trackingMode: WorkoutTrackingType;
    startTime: Date;
    endTime?: Date;
    completed: boolean;
    notes?: string;

    // Mode-specific data (only one will be populated)
    cardioData?: CardioWorkoutData;
    isometricData?: IsometricWorkoutData;
    strengthData?: StrengthWorkoutData;
}

// =============================================================================
// WORKOUT SESSION INTERFACE
// =============================================================================

export interface WorkoutSession {
    id: string;
    date: string;
    exercises: WorkoutExercise[];
    status: 'not_started' | 'in_progress' | 'paused' | 'completed' | 'cancelled';
    startedAt?: Date;
    completedAt?: Date;
    totalDurationMinutes?: number;
    notes?: string;

    // Progress tracking
    currentExerciseIndex: number;
    currentSetIndex: number; // Only relevant for strength/isometric exercises

    // Session metadata
    estimatedCalories?: number;
    actualCalories?: number;
    mood?: string;
    location?: string;
}

// =============================================================================
// UI AND FILTERING INTERFACES
// =============================================================================

// Goal interface
export interface Goal {
    id: string;
    name: string;
    emoji: string;
    count: number;
}

// Exercise Type option for dropdowns
export interface ExerciseTypeOption {
    value: ExerciseType;
    display: string;
    emoji: string;
    label?: string;
    count?: number;
}

// Enhanced exercise type option with tracking information
export interface EnhancedExerciseTypeOption extends ExerciseTypeOption {
    trackingType: WorkoutTrackingType;
    description: string;
}

// Active filter interface
export interface ActiveFilter {
    type: FilterType;
    value: string;
    emoji?: string;
}

// Exercise filters state
export interface ExerciseFilters {
    activeGoal: string;
    searchTerm: string;
    selectedEquipment: string;
    selectedDifficulty: string;
    selectedExerciseType: string;
    minRating: number;
    maxDuration: number;
    onlyFavorites: boolean;
    includeCompleted: boolean;
    muscleGroups: string[];
    availableEquipment: string[];
    fitnessLevel: string;
    sortBy: SortOption;
    showProfessionalOnly: boolean;
    trackingType?: WorkoutTrackingType | 'all';
    professionalOnly?: boolean;
    difficulty?: string;
    equipment?: string;
    exerciseType?: string;
}

// Sort option type
export interface SortOptionType {
    value: SortOption;
    label: string;
}

// =============================================================================
// API RESPONSE INTERFACES
// =============================================================================

export interface ExerciseApiResponse {
    exercises: Exercise[];
    totalCount: number;
    page: number;
    hasMore: boolean;
}

export interface GoalApiResponse {
    goals: Goal[];
}

export interface FilterOptionsResponse {
    equipmentOptions: string[];
    difficultyOptions: string[];
    exerciseTypeOptions: ExerciseTypeOption[];
}

// =============================================================================
// PERFORMANCE ANALYTICS INTERFACES
// =============================================================================

/**
 * Enhanced workout session summary
 */
export interface WorkoutSessionSummary {
    sessionId: string;
    date: string;
    totalDurationMinutes: number;
    totalExercises: number;
    completedExercises: number;
    completionPercentage: number;

    // Overall performance
    overallPerformanceRating: 'EXCELLENT' | 'GOOD' | 'AVERAGE' | 'NEEDS_IMPROVEMENT';
    totalVolume?: number; // For strength workouts
    totalCalories?: number; // For cardio workouts
    totalHoldTime?: number; // For isometric workouts

    // Achievements (using types from WorkoutResults)
    personalRecords: NonNullable<WorkoutResults['personalRecords']>;
    improvements: NonNullable<WorkoutResults['improvements']>;

    // Session feedback (using types from WorkoutResults)
    mood?: WorkoutResults['mood'];
    location?: WorkoutResults['location'];
    perceivedEffort?: number;
    sessionNotes?: string;

    // Exercise breakdown
    exercises: WorkoutResults[];
}

/**
 * Performance analytics for dashboard/progress tracking
 */
export interface PerformanceAnalytics {
    timeframe: 'week' | 'month' | 'quarter' | 'year';

    // Volume and frequency
    totalWorkouts: number;
    totalDurationMinutes: number;
    averageWorkoutDuration: number;
    workoutFrequency: number; // workouts per week

    // Performance trends
    strengthProgress?: {
        totalVolumeIncrease: number;
        averageWeightIncrease: number;
        strengthScore: number; // 1-100
    };

    cardioProgress?: {
        paceImprovement: number;
        enduranceIncrease: number;
        cardioScore: number; // 1-100
    };

    isometricProgress?: {
        holdTimeIncrease: number;
        stabilityScore: number; // 1-100
    };

    // Personal records achieved (using type from WorkoutResults)
    personalRecords: NonNullable<WorkoutResults['personalRecords']>;

    // Consistency metrics
    consistencyScore: number; // 1-100
    streaks: {
        current: number;
        longest: number;
    };
}

// =============================================================================
// PERFORMANCE UTILITY FUNCTIONS
// =============================================================================

/**
 * Calculate performance score (0-100) based on workout results
 */
export const calculateWorkoutPerformanceScore = (workoutResults: WorkoutResults): number => {
    let score = 0;
    let factors = 0;

    // Completion factor (40% of score)
    const completionRate = workoutResults.sets.filter(s => s.completed).length / workoutResults.sets.length;
    score += completionRate * 40;
    factors++;

    // Performance vs target factor (40% of score)
    const performanceScores = workoutResults.sets.map(set => {
        switch (set.performanceVsTarget) {
            case 'EXCEEDED':
                return 100;
            case 'MET':
                return 85;
            case 'PARTIAL':
                return 60;
            case 'BELOW_TARGET':
                return 30;
            default:
                return 50;
        }
    });
    const avgPerformanceScore = performanceScores.reduce((a, b) => a + b, 0) / performanceScores.length;
    score += (avgPerformanceScore / 100) * 40;
    factors++;

    // RPE factor (20% of score) - lower RPE for same performance is better
    if (workoutResults.strengthMetrics?.averageRpe) {
        const rpeScore = Math.max(0, 100 - (workoutResults.strengthMetrics.averageRpe * 8));
        score += (rpeScore / 100) * 20;
    } else {
        score += 15; // Default decent score if no RPE data
    }
    factors++;

    return Math.round(score / factors);
};

/**
 * Format workout results for display
 */
export const formatWorkoutResultsForDisplay = (workoutResults: WorkoutResults): {
    summary: string;
    highlights: string[];
    metrics: Array<{ label: string; value: string; trend?: 'up' | 'down' | 'neutral' }>;
} => {
    const summary = `Completed ${workoutResults.sets.filter(s => s.completed).length} of ${workoutResults.sets.length} sets`;

    const highlights: string[] = [];
    const metrics: Array<{ label: string; value: string; trend?: 'up' | 'down' | 'neutral' }> = [];

    // Add personal records to highlights
    if (workoutResults.personalRecords && workoutResults.personalRecords.length > 0) {
        highlights.push(`🏆 ${workoutResults.personalRecords.length} new personal record${workoutResults.personalRecords.length > 1 ? 's' : ''}!`);
    }

    // Add improvements to highlights
    if (workoutResults.improvements && workoutResults.improvements.length > 0) {
        highlights.push(`📈 Improved in ${workoutResults.improvements.length} metric${workoutResults.improvements.length > 1 ? 's' : ''}`);
    }

    // Add type-specific metrics
    if (workoutResults.strengthMetrics) {
        metrics.push(
            {label: 'Total Volume', value: `${workoutResults.strengthMetrics.totalVolume} lbs`},
            {label: 'Total Reps', value: workoutResults.strengthMetrics.totalReps.toString()},
            {label: 'Average RPE', value: `${workoutResults.strengthMetrics.averageRpe.toFixed(1)}/10`}
        );
    }

    if (workoutResults.cardioMetrics) {
        metrics.push(
            {label: 'Duration', value: `${workoutResults.cardioMetrics.totalDurationMinutes} min`}
        );

        if (workoutResults.cardioMetrics.totalDistanceKm) {
            metrics.push({
                label: 'Distance',
                value: `${(workoutResults.cardioMetrics.totalDistanceKm * 0.621371).toFixed(1)} mi`
            });
        }

        if (workoutResults.cardioMetrics.totalCaloriesBurned) {
            metrics.push({
                label: 'Calories',
                value: workoutResults.cardioMetrics.totalCaloriesBurned.toString()
            });
        }
    }

    if (workoutResults.isometricMetrics) {
        metrics.push(
            {label: 'Total Hold Time', value: `${workoutResults.isometricMetrics.totalHoldTimeSeconds}s`},
            {label: 'Best Hold', value: `${workoutResults.isometricMetrics.longestHoldSeconds}s`}
        );
    }

    return {summary, highlights, metrics};
};

/**
 * Get performance rating display info
 */
export const getPerformanceRatingInfo = (rating: WorkoutResults['performanceRating']) => {
    switch (rating) {
        case 'EXCEEDED':
            return {
                icon: '🚀',
                message: 'Crushed it!',
                color: 'text-green-600 bg-green-100 border-green-300',
                textColor: 'text-green-800'
            };
        case 'MET':
            return {
                icon: '🎯',
                message: 'Target achieved',
                color: 'text-blue-600 bg-blue-100 border-blue-300',
                textColor: 'text-blue-800'
            };
        case 'PARTIAL':
            return {
                icon: '⚡',
                message: 'Good effort',
                color: 'text-yellow-600 bg-yellow-100 border-yellow-300',
                textColor: 'text-yellow-800'
            };
        case 'BELOW_TARGET':
            return {
                icon: '💪',
                message: 'Keep pushing',
                color: 'text-red-600 bg-red-100 border-red-300',
                textColor: 'text-red-800'
            };
        default:
            return {
                icon: '✅',
                message: 'Completed',
                color: 'text-gray-600 bg-gray-100 border-gray-300',
                textColor: 'text-gray-800'
            };
    }
};

// =============================================================================
// HELPER FUNCTIONS
// =============================================================================

// Exercise name helpers
export const getExerciseName = (exercise: Exercise): string => {
    return exercise.exerciseName || exercise.name || 'Unknown Exercise';
};

export const getExerciseDisplayName = (exercise: Exercise): string => {
    return exercise.name || exercise.exerciseName || 'Unknown Exercise';
};

// Workout tracking mode helpers
export const getWorkoutTrackingType = (exercise: Exercise): WorkoutTrackingType => {
    if (exercise.isCardio) return 'cardio';
    if (exercise.isIsometric) return 'isometric';
    return 'strength';
};

export const getCardioSessionType = (exercise: Exercise): CardioSessionType => {
    const exerciseName = exercise.name.toLowerCase();
    const description = exercise.description?.toLowerCase() || '';

    // Keywords that indicate interval/set-based cardio
    const intervalKeywords = [
        'hiit', 'high intensity interval',
        'interval', 'intervals',
        'circuit', 'circuits',
        'tabata',
        'emom', 'every minute on the minute',
        'amrap', 'as many reps as possible',
        'rounds',
        'burpees', // Usually done in sets
        'mountain climbers', // Often done in timed sets
    ];

    const isIntervalCardio = intervalKeywords.some(keyword =>
        exerciseName.includes(keyword) || description.includes(keyword)
    );

    if (isIntervalCardio) {
        return {
            type: 'interval_sets',
            defaultSets: 4,
            showSetsInConfig: true,
            description: 'This exercise is typically done in multiple sets/rounds with rest periods'
        };
    }

    return {
        type: 'single_session',
        defaultSets: 1,
        showSetsInConfig: false,
        description: 'This exercise is typically done as one continuous session'
    };
};

export const isCardioExercise = (exercise: Exercise): boolean => {
    return exercise.isCardio === true;
};

export const isIsometricExercise = (exercise: Exercise): boolean => {
    return exercise.isIsometric === true;
};

export const isStrengthExercise = (exercise: Exercise): boolean => {
    return !exercise.isCardio && !exercise.isIsometric;
};

export const isCardioConfiguration = (config: ExerciseConfiguration): config is CardioConfiguration => {
    return config.trackingMode === 'cardio';
};

export const isIsometricConfiguration = (config: ExerciseConfiguration): config is IsometricConfiguration => {
    return config.trackingMode === 'isometric';
};

export const isStrengthConfiguration = (config: ExerciseConfiguration): config is StrengthConfiguration => {
    return config.trackingMode === 'strength';
};

// Filter helper functions
export const filterExercisesByTrackingType = (
    exercises: Exercise[],
    trackingType: WorkoutTrackingType | 'all'
): Exercise[] => {
    if (trackingType === 'all') {
        return exercises;
    }

    switch (trackingType) {
        case 'cardio':
            return exercises.filter(isCardioExercise);
        case 'isometric':
            return exercises.filter(isIsometricExercise);
        case 'strength':
            return exercises.filter(isStrengthExercise);
        default:
            return exercises;
    }
};

// Validation helpers
export const validateExerciseData = (exercise: Exercise): {
    isValid: boolean;
    issues: string[];
    recommendations: string[];
} => {
    const issues: string[] = [];
    const recommendations: string[] = [];

    // Check for conflicting flags
    if (exercise.isCardio && exercise.isIsometric) {
        issues.push('Exercise cannot be both cardio and isometric');
        recommendations.push('Choose either cardio OR isometric, not both');
    }

    // Check exerciseType consistency
    const shouldBeCardio = exercise.exerciseType === 'CARDIO';
    if (exercise.isCardio !== shouldBeCardio) {
        issues.push(`isCardio flag (${exercise.isCardio}) doesn't match exerciseType (${exercise.exerciseType})`);
        if (shouldBeCardio) {
            recommendations.push('Set isCardio to true for CARDIO exercises');
        } else {
            recommendations.push('Set isCardio to false for non-CARDIO exercises');
        }
    }

    // Check for missing names
    if (!exercise.name && !exercise.exerciseName) {
        issues.push('Exercise missing name');
        recommendations.push('Add a name or exerciseName property');
    }

    return {
        isValid: issues.length === 0,
        issues,
        recommendations
    };
};

// Exercise creation helper
export const createExercise = (exerciseData: Omit<Exercise, 'isCardio' | 'isIsometric'>): Exercise => {
    return {
        ...exerciseData,
        // Automatically determine isCardio based on exerciseType
        isCardio: exerciseData.exerciseType === 'CARDIO',
        // Default isIsometric to false (would need to be set manually for specific exercises)
        isIsometric: false
    };
};

// =============================================================================
// ✅ ADDED: MISSING UTILITY FUNCTIONS
// =============================================================================

// Time formatting utility
export const formatTime = (seconds: number): string => {
    if (seconds < 60) {
        return `${seconds}s`;
    }
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    if (remainingSeconds === 0) {
        return `${minutes}m`;
    }
    return `${minutes}m ${remainingSeconds}s`;
};

// Weight conversion utilities
export const convertWeight = (weight: number, fromUnit: WeightUnit, toUnit: WeightUnit): number => {
    if (fromUnit === toUnit) return weight;
    if (fromUnit === 'lbs' && toUnit === 'kg') {
        return Math.round(weight * 0.453592 * 100) / 100;
    }
    if (fromUnit === 'kg' && toUnit === 'lbs') {
        return Math.round(weight * 2.20462 * 100) / 100;
    }
    return weight;
};

export const formatWeight = (weight: number, unit: WeightUnit): string => {
    if (weight % 1 === 0) {
        return `${weight} ${unit}`;
    }
    return `${weight.toFixed(1)} ${unit}`;
};

// Distance conversion utilities (already exist, keeping for completeness)
export const convertDistance = (distance: number, fromUnit: DistanceUnit, toUnit: DistanceUnit): number => {
    if (fromUnit === toUnit) return distance;
    if (fromUnit === 'miles' && toUnit === 'km') {
        return Math.round(distance * 1.60934 * 100) / 100;
    }
    if (fromUnit === 'km' && toUnit === 'miles') {
        return Math.round(distance * 0.621371 * 100) / 100;
    }
    return distance;
};

export const formatDistance = (distance: number, unit: DistanceUnit): string => {
    if (unit === 'miles') {
        return `${distance % 1 === 0 ? distance : distance.toFixed(1)} mi`;
    } else {
        return `${distance % 1 === 0 ? distance : distance.toFixed(1)} km`;
    }
};

// Distance presets function
export const getDistancePresets = (unit: DistanceUnit): number[] => {
    return DISTANCE_PRESETS[unit];
};

// =============================================================================
// CONSTANTS AND PREDEFINED DATA
// =============================================================================

// Enhanced exercise type options with tracking information
export const enhancedExerciseTypeOptions: EnhancedExerciseTypeOption[] = [
    {
        value: 'STRENGTH',
        display: 'Strength Training',
        emoji: '💪',
        trackingType: 'strength',
        description: 'Build muscle with sets, reps, and progressive weight'
    },
    {
        value: 'CARDIO',
        display: 'Cardiovascular',
        emoji: '❤️',
        trackingType: 'cardio',
        description: 'Improve endurance with time and distance tracking'
    },
    {
        value: 'FLEXIBILITY',
        display: 'Flexibility & Mobility',
        emoji: '🤸‍♀️',
        trackingType: 'isometric', // Most flexibility exercises are hold-based
        description: 'Enhance flexibility with holds and stretches'
    },
    {
        value: 'BALANCE',
        display: 'Balance & Stability',
        emoji: '⚖️',
        trackingType: 'isometric', // Balance exercises are typically hold-based
        description: 'Improve stability with timed holds and challenges'
    },
    {
        value: 'PLYOMETRIC',
        display: 'Plyometric & Power',
        emoji: '⚡',
        trackingType: 'strength',
        description: 'Build explosive power with dynamic movements'
    },
    {
        value: 'REHABILITATION',
        display: 'Rehabilitation',
        emoji: '🛡️',
        trackingType: 'strength',
        description: 'Support recovery with gentle, controlled movements'
    },
    {
        value: 'SPORTS_SPECIFIC',
        display: 'Sports Specific',
        emoji: '🏆',
        trackingType: 'strength',
        description: 'Develop sport skills with technique-focused practice'
    }
];

// ✅ UPDATED: Default configurations with proper field names
export const DEFAULT_CARDIO_CONFIG: CardioConfiguration = {
    trackingMode: 'cardio',
    targetDurationMinutes: 20,
    targetDistance: undefined,
    targetDistanceUnit: 'miles',
    targetPace: undefined,
    notes: ''
};

export const DEFAULT_ISOMETRIC_CONFIG: IsometricConfiguration = {
    trackingMode: 'isometric',
    targetSets: 3,
    holdDurationSeconds: 30, // ✅ FIXED: Using correct field name
    restSeconds: 60,
    notes: ''
};

export const DEFAULT_STRENGTH_CONFIG: StrengthConfiguration = {
    trackingMode: 'strength',
    targetSets: 3,
    targetReps: 10,
    targetWeight: undefined,
    targetWeightUnit: 'lbs',
    restSeconds: 90,
    targetRpe: 7,
    tempo: undefined,
    notes: ''
};

export const getDefaultConfigForExercise = (exercise: Exercise): ExerciseConfiguration => {
    const trackingMode = getWorkoutTrackingType(exercise);

    switch (trackingMode) {
        case 'cardio': {
            const sessionType = getCardioSessionType(exercise);
            return {
                trackingMode: 'cardio',
                sessionType: sessionType.type,
                targetDurationMinutes: exercise.estimatedDurationMinutes || 20,
                targetDistance: undefined,
                targetDistanceUnit: 'miles', // American default
                targetPace: undefined,
                targetSets: sessionType.defaultSets,
                restSeconds: sessionType.type === 'interval_sets' ? 60 : undefined,
                notes: ''
            } as CardioConfiguration;
        }
        case 'isometric':
            return {...DEFAULT_ISOMETRIC_CONFIG};
        case 'strength':
        default:
            return {...DEFAULT_STRENGTH_CONFIG};
    }
};

/**
 * ✅ NEW: Debug exercise type for troubleshooting in CalendarPage
 */
export const debugExerciseType = (exercise: Exercise): void => {
    console.group(`🔍 Exercise Type Debug: "${exercise.name}"`);
    console.log('Basic Info:', {
        id: exercise.id,
        name: exercise.name,
        emoji: exercise.emoji
    });
    console.log('Type Flags:', {
        exerciseType: exercise.exerciseType,
        isCardio: exercise.isCardio,
        isIsometric: exercise.isIsometric
    });
    console.log('Computed Values:', {
        trackingType: getWorkoutTrackingType(exercise),
        interfaceColor: getExerciseInterfaceColor(exercise)
    });
    console.groupEnd();
};

/**
 * ✅ NEW: Get exercise interface color based on type
 */
export const getExerciseInterfaceColor = (exercise: Exercise): string => {
    const trackingType = getWorkoutTrackingType(exercise);

    switch (trackingType) {
        case 'cardio':
            return 'red'; // Red interface for cardio
        case 'isometric':
            return 'purple'; // Purple interface for isometric/holds
        case 'strength':
        default:
            return 'blue'; // Blue interface for strength training
    }
};

/**
 * ✅ NEW: Batch validate multiple exercises
 */
export const validateExerciseBatch = (exercises: Exercise[]): {
    valid: Exercise[];
    invalid: Exercise[];
    summary: {
        total: number;
        validCount: number;
        invalidCount: number;
        cardioCount: number;
        isometricCount: number;
        strengthCount: number;
    }
} => {
    const valid: Exercise[] = [];
    const invalid: Exercise[] = [];

    exercises.forEach(exercise => {
        const validation = validateExerciseData(exercise);
        if (validation.isValid) {
            valid.push(exercise);
        } else {
            invalid.push(exercise);
            console.warn(`⚠️ Invalid exercise: ${exercise.name}`, validation.issues);
        }
    });

    const cardioCount = valid.filter(ex => ex.isCardio).length;
    const isometricCount = valid.filter(ex => ex.isIsometric).length;
    const strengthCount = valid.filter(ex => !ex.isCardio && !ex.isIsometric).length;

    return {
        valid,
        invalid,
        summary: {
            total: exercises.length,
            validCount: valid.length,
            invalidCount: invalid.length,
            cardioCount,
            isometricCount,
            strengthCount
        }
    };
};

/**
 * ✅ NEW: Create exercise summary for debugging
 */
export const createExerciseSummary = (exercise: Exercise): {
    name: string;
    type: string;
    tracking: WorkoutTrackingType;
    interface: string;
    duration: string;
    valid: boolean;
} => {
    const validation = validateExerciseData(exercise);
    const trackingType = getWorkoutTrackingType(exercise);

    return {
        name: exercise.name || exercise.exerciseName || 'Unknown',
        type: exercise.exerciseTypeDisplay || exercise.exerciseType || 'Unknown',
        tracking: trackingType,
        interface: getExerciseInterfaceColor(exercise),
        duration: exercise.estimatedDurationMinutes ? `${exercise.estimatedDurationMinutes} min` : 'Varies',
        valid: validation.isValid
    };
};

export const DISTANCE_PRESETS = {
    miles: [0.5, 1, 1.5, 2, 3, 3.1, 6.2, 13.1, 26.2], // Including 5K, 10K, Half, Full marathon in miles
    km: [1, 2, 3, 5, 8, 10, 21.1, 42.2]             // Standard metric distances
};

export const PACE_PRESETS = {
    miles: [6, 7, 8, 9, 10, 11, 12], // minutes per mile
    km: [4, 5, 6, 7, 8]             // minutes per km
};