// TypeScript Interfaces and Enums for Exercise System

// Enums matching your backend
export type ExerciseType = 'STRENGTH' | 'CARDIO' | 'FLEXIBILITY' | 'BALANCE' | 'PLYOMETRIC' | 'REHABILITATION' | 'SPORTS_SPECIFIC';
export type DifficultyLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
export type SortOption = 'relevance' | 'rating' | 'popularity' | 'duration' | 'calories' | 'newest';
export type FilterType = 'goal' | 'difficulty' | 'equipment' | 'exerciseType' | 'rating' | 'duration' | 'professional';

// NEW: Workout tracking type - determines which interface to show during workouts
export type WorkoutTrackingType = 'cardio' | 'strength';

// Main Exercise interface based on your backend entity
export interface Exercise {
    id: number;
    name: string;
    exerciseName?: string; // Backward compatibility
    emoji: string;
    description: string;
    exerciseType: ExerciseType;
    isCardio: boolean; // Critical for workout tracking
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
}

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

// Active filter interface
export interface ActiveFilter {
    type: FilterType;
    value: string;
    emoji?: string;
}

// Exercise filters state - ENHANCED with cardio filtering capability
export interface ExerciseFilters {
    activeGoal: string;
    searchTerm: string;
    selectedEquipment: string;
    selectedDifficulty: string;
    selectedExerciseType: string;
    minRating: number;
    maxDuration: number;
    sortBy: SortOption;
    showProfessionalOnly: boolean;
    exerciseType?: string;
    difficulty?: string;
    equipment?: string;
    professionalOnly?: boolean;
    // NEW: Filter by workout tracking type
    trackingType?: WorkoutTrackingType | 'all';  // 'cardio', 'strength', or 'all'
}

// API Response types (for future backend integration)
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

export interface SortOptionType {
    value: SortOption;
    label: string;
}

// NEW: Workout-specific interfaces that leverage the isCardio field

// Cardio exercise tracking data structure
export interface CardioWorkoutData {
    exerciseId: number;
    startTime: Date;
    endTime?: Date;
    durationMinutes?: number;
    durationSeconds?: number;
    distanceKm?: number;
    averagePace?: number;  // minutes per km
    maxHeartRate?: number;
    averageHeartRate?: number;
    caloriesBurned?: number;
    notes?: string;
    completed: boolean;
}

// Strength exercise tracking data structure
export interface StrengthWorkoutData {
    exerciseId: number;
    sets: WorkoutSet[];
    startTime: Date;
    endTime?: Date;
    notes?: string;
    completed: boolean;
}

// Individual set data for strength exercises
export interface WorkoutSet {
    setNumber: number;
    targetReps: number;
    actualReps?: number;
    targetWeight?: number;
    actualWeight?: number;
    targetRpe?: number;  // Rate of Perceived Exertion (1-10)
    actualRpe?: number;
    restSeconds?: number;
    tempo?: string;  // e.g., "3-1-2-1" (eccentric-pause-concentric-pause)
    completed: boolean;
    notes?: string;
    completedAt?: Date;
}

// Unified workout data that can handle both cardio and strength
export interface UnifiedWorkoutData {
    exerciseId: number;
    exercise: Exercise;  // Includes isCardio flag
    startTime: Date;
    endTime?: Date;
    completed: boolean;
    notes?: string;

    // Cardio-specific fields (populated when exercise.isCardio === true)
    cardioData?: CardioWorkoutData;

    // Strength-specific fields (populated when exercise.isCardio === false)
    strengthData?: StrengthWorkoutData;
}

export const getExerciseName = (exercise: Exercise): string => {
    return exercise.exerciseName || exercise.name || 'Unknown Exercise';
};

export const getExerciseDisplayName = (exercise: Exercise): string => {
    return exercise.name || exercise.exerciseName || 'Unknown Exercise';
};

// Type guards to help with type safety during workout tracking
export const isCardioExercise = (exercise: Exercise): boolean => {
    return exercise.isCardio === true;
};

export const isStrengthExercise = (exercise: Exercise): boolean => {
    return exercise.isCardio === false;
};

// Helper function to determine what type of workout interface to show
export const getWorkoutTrackingType = (exercise: Exercise): WorkoutTrackingType => {
    return exercise.isCardio ? 'cardio' : 'strength';
};

// Filter helper functions for exercise lists
export const filterExercisesByTrackingType = (
    exercises: Exercise[],
    trackingType: WorkoutTrackingType | 'all'
): Exercise[] => {
    if (trackingType === 'all') {
        return exercises;
    }

    if (trackingType === 'cardio') {
        return exercises.filter(isCardioExercise);
    }

    return exercises.filter(isStrengthExercise);
};

// Validation helpers to ensure data consistency
export const validateExerciseData = (exercise: Exercise): boolean => {
    // Ensure isCardio consistency with exerciseType
    const shouldBeCardio = exercise.exerciseType === 'CARDIO';

    if (exercise.isCardio !== shouldBeCardio) {
        console.warn(
            `Exercise "${exercise.name}" has inconsistent cardio flags: ` +
            `exerciseType="${exercise.exerciseType}" but isCardio=${exercise.isCardio}`
        );
        return false;
    }

    return true;
};

// Exercise creation helper that automatically sets isCardio based on exerciseType
export const createExercise = (exerciseData: Omit<Exercise, 'isCardio'>): Exercise => {
    return {
        ...exerciseData,
        // Automatically determine isCardio based on exerciseType
        isCardio: exerciseData.exerciseType === 'CARDIO'
    };
};

// NEW: Enhanced exercise type options that include tracking type information
export interface EnhancedExerciseTypeOption extends ExerciseTypeOption {
    trackingType: WorkoutTrackingType;  // Whether this type uses cardio or strength tracking
    description: string;  // User-friendly description of what this type involves
}

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
        trackingType: 'strength',
        description: 'Enhance flexibility with holds and stretches'
    },
    {
        value: 'BALANCE',
        display: 'Balance & Stability',
        emoji: '⚖️',
        trackingType: 'strength',
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

// Workout session status types
export type WorkoutSessionStatus = 'not_started' | 'in_progress' | 'paused' | 'completed' | 'cancelled';

// Complete workout session interface
export interface WorkoutSession {
    id: string;
    date: string;  // ISO date string
    exercises: UnifiedWorkoutData[];
    status: WorkoutSessionStatus;
    startTime?: Date;
    endTime?: Date;
    totalDurationMinutes?: number;
    notes?: string;

    // Progress tracking
    currentExerciseIndex: number;
    currentSetIndex: number;  // Only relevant for strength exercises

    // Session metadata
    estimatedCalories?: number;
    actualCalories?: number;
    mood?: string;
    location?: string;
}