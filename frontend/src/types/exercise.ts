// TypeScript Interfaces and Enums for Exercise System

// Enums matching your backend
export type ExerciseType = 'STRENGTH' | 'CARDIO' | 'FLEXIBILITY' | 'BALANCE' | 'PLYOMETRIC' | 'REHABILITATION' | 'SPORTS_SPECIFIC';
export type DifficultyLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
export type SortOption = 'relevance' | 'rating' | 'popularity' | 'duration' | 'calories' | 'newest';
export type FilterType = 'goal' | 'difficulty' | 'equipment' | 'exerciseType' | 'rating' | 'duration' | 'professional';

// Main Exercise interface based on your backend entity
export interface Exercise {
    id: number;
    name: string;
    emoji: string;
    description: string;
    exerciseType: ExerciseType;
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

// Exercise filters state
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