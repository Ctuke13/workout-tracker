export interface BackendExercise {
    id: number;
    name: string;  // Field is called "name" in response
    emoji: string | null;
    description: string;
    exerciseType: 'STRENGTH' | 'CARDIO' | 'FLEXIBILITY' | 'REHABILITATION' | 'SPORTS_SPECIFIC' | 'PLYOMETRIC' | 'BALANCE';
    exerciseTypeDisplay: string;  // "Strength Training", "Cardiovascular", etc.
    difficultyLevel: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
    difficultyDescription: string;  // "Beginner - No experience needed"
    estimatedDurationMinutes: number | null;
    estimatedCalories: number | null;
    targetMuscleGroups: string[];  // ["CARDIO"], ["FULL_BODY", "CORE"], etc.
    equipmentRequired: string[];   // ["dumbbells"], ["jump_rope"], []
    equipmentSummary: string;      // "No equipment needed", "dumbbells"
    benefits: string[];            // ["Burns calories fast", "Boosts metabolism"]
    tips: string[];                // ["Start with 30 sec work", "Focus on form"]
    videoUrl: string | null;
    usageCount: number;
    averageRating: number;
    totalRatings: number;
    isPopular: boolean;
    isHighlyRated: boolean;
    isFromVerifiedSource: boolean;
    canDoAtHome: boolean;
    requiresEquipment: boolean;
    createdBy: string;             // "Platform", "Professional"
    createdAt: string;             // ISO datetime string
    updatedAt: string;             // ISO datetime string

    // Frontend-specific fields (only populated by public endpoints)
    duration?: string | null;      // "20 mins" format (currently null)
    calories?: string | null;      // "400-600/hr" format (currently null)
    equipment?: string | null;     // "Dumbbells" format (currently null)
    difficulty?: string | null;    // "Beginner" format (currently null)
    goal?: string | null;          // "fat-burn" format (currently null)
    goals?: string[] | null;       // ["fat-burn", "endurance"] (currently null)
    hasVideo?: boolean | null;     // videoUrl != null (currently null)
    rating?: string | null;        // "4.5 stars (120 reviews)" (currently null)
}

export interface GoalData {
    goal: string;  // "fat-burn", "muscle-building", "endurance", etc.
    count: number;
}

export interface FiltersData {
    equipment: string[];      // ["None", "dumbbells", "barbell", "yoga_mat", ...]
    difficulties: string[];   // ["Beginner", "Intermediate", "Advanced"]
}

// Frontend transformed types (what your UI components use)
export interface Exercise {
    id: number;
    name: string;
    emoji: string;
    difficulty: string;           // "Beginner", "Intermediate", "Advanced"
    description: string;
    duration: string;             // "20 mins" format
    calories: string;             // "400-600/hr" format
    equipment: string;            // "Dumbbells" or "No Equipment"
    benefits: string[];
    tips: string[];
    videoUrl?: string | null;
    type: string;                 // exerciseType display name
    exerciseType: string;         // Raw enum value
    muscleGroups: string[];       // targetMuscleGroups
    rating: number;               // averageRating
    totalRatings: number;
    usageCount: number;
    isPopular: boolean;
    isHighlyRated: boolean;
    canDoAtHome: boolean;
    requiresEquipment: boolean;
    createdBy: string;
}

export interface Goal {
    id: string;
    name: string;
    emoji: string;
    count: number;
}

// API filter parameters
export interface ExerciseFilters {
    goal?: string;
    difficulty?: string;
    equipment?: string;
    exercise_type?: string;
    muscle_group?: string;
    q?: string; // search query
}

// UI state types
export interface ExercisePageState {
    exercises: Exercise[];
    goals: Goal[];
    equipmentOptions: string[];
    difficultyOptions: string[];
    exerciseTypeOptions: string[];
    muscleGroupOptions: string[];
    activeGoal: string;
    searchTerm: string;
    selectedEquipment: string;
    selectedDifficulty: string;
    selectedExerciseType: string;
    selectedMuscleGroup: string;
    expandedCard: number | null;
    loading: boolean;
    error: string | null;
    isMobileMenuOpen: boolean;
}

// API response wrapper (if your backend uses pagination/wrapper)
export interface ApiResponse<T> {
    data: T;
    success: boolean;
    message?: string;
}

// Exercise API client interface
export interface ExerciseApiClient {
    getPublicExercises: (filters?: ExerciseFilters) => Promise<BackendExercise[]>;
    getGoals: () => Promise<GoalData[]>;
    getFilters: () => Promise<FiltersData>;
    searchExercises: (query: string, filters?: ExerciseFilters) => Promise<BackendExercise[]>;
    getExerciseById: (id: number) => Promise<BackendExercise>;
}