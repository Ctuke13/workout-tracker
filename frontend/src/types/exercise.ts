// =============================================================================
// CALENDAR AND SCHEDULING INTERFACES
// =============================================================================

export interface ScheduledExercise {
    id: string;
    exerciseId: number;
    exercise: Exercise;
    scheduledDate: string;

    // Strength fields
    sets?: number;
    reps?: string;
    weight?: number;
    restSeconds?: number;
    tempo?: string;
    targetRpe?: number;

    //  Cardio fields
    targetDurationMinutes?: number;
    targetDistanceKm?: number;
    targetPace?: number;

    // Isometric fields
    holdDurationSeconds?: number;

    // Common fields
    notes?: string;
    completed: boolean;
    status?: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'SKIPPED' | 'RESCHEDULED';
    createdAt: string;
    userId: string;
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

// =============================================================================
// CORE ENUMS AND TYPES
// =============================================================================

export type ExerciseType = 'STRENGTH' | 'CARDIO' | 'FLEXIBILITY' | 'BALANCE' | 'PLYOMETRIC' | 'REHABILITATION' | 'SPORTS_SPECIFIC';
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
    targetDurationMinutes: number;
    targetDistanceKm?: number;
    targetPace?: number; // minutes per km
}

export interface IsometricConfiguration extends BaseExerciseConfiguration {
    trackingMode: 'isometric';
    sets: number;
    holdDurationSeconds: number;
    restSeconds: number;
}

export interface StrengthConfiguration extends BaseExerciseConfiguration {
    trackingMode: 'strength';
    sets: number;
    reps: string;
    weight?: number;
    restSeconds: number;
    targetRpe?: number;
    tempo?: string;
}

// ✅ FIXED: Proper discriminated union type
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
    date: string; // ISO date string
    exercises: UnifiedWorkoutData[];
    status: WorkoutSessionStatus;
    startTime?: Date;
    endTime?: Date;
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

    // Ensure exercises can't be both cardio and isometric
    if (exercise.isCardio && exercise.isIsometric) {
        console.warn(
            `Exercise "${exercise.name}" cannot be both cardio and isometric`
        );
        return false;
    }

    return true;
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

// ✅ FIXED: Default configurations with proper tracking mode
export const DEFAULT_CARDIO_CONFIG: CardioConfiguration = {
    trackingMode: 'cardio',
    targetDurationMinutes: 20,
    targetDistanceKm: undefined,
    targetPace: undefined,
    notes: ''
};

export const DEFAULT_ISOMETRIC_CONFIG: IsometricConfiguration = {
    trackingMode: 'isometric',
    sets: 3,
    holdDurationSeconds: 30,
    restSeconds: 60,
    notes: ''
};

export const DEFAULT_STRENGTH_CONFIG: StrengthConfiguration = {
    trackingMode: 'strength',
    sets: 3,
    reps: '8-12',
    weight: undefined,
    restSeconds: 90,
    targetRpe: 7,
    tempo: undefined,
    notes: ''
};

// ✅ FIXED: Helper to get default config based on exercise
export const getDefaultConfigForExercise = (exercise: Exercise): ExerciseConfiguration => {
    const trackingMode = getWorkoutTrackingType(exercise);
    switch (trackingMode) {
        case 'cardio':
            return { ...DEFAULT_CARDIO_CONFIG };
        case 'isometric':
            return { ...DEFAULT_ISOMETRIC_CONFIG };
        case 'strength':
            return { ...DEFAULT_STRENGTH_CONFIG };
        default:
            return { ...DEFAULT_STRENGTH_CONFIG };
    }
};