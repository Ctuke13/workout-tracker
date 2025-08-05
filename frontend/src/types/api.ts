import {Exercise, ExerciseType, ScheduledExercise} from './exercise';

// ==================== AUTHENTICATION TYPES ====================

export interface LoginRequest {
    emailOrUsername: string;
    password: string;
}

export interface RegisterRequest {
    email: string;
    username: string;
    password: string;
    firstName: string;
    lastName: string;
    userType?: 'REGULAR' | 'PROFESSIONAL';
}

export interface JwtResponse {
    token: string;
    type: string; // "Bearer"
    id: number;
    username: string;
    email: string;
    firstName: string;
    lastName: string;
    userType: 'REGULAR' | 'PROFESSIONAL';
    isProfessional: boolean;
}

export interface ApiResponse<T = any> {
    data?: T;
    success: boolean;
    message?: string;
    timestamp?: string;
    status?: number;
    error?: string;
    path?: string;
}

// ==================== BACKEND EXERCISE TYPES (Direct from Spring Boot) ====================

export interface BackendExercise {
    id: number;
    name: string;  // Backend field name - matches ExerciseResponseDTO.name
    emoji: string | null;
    description: string;
    exerciseType: 'STRENGTH' | 'CARDIO' | 'FLEXIBILITY' | 'REHABILITATION' | 'SPORTS_SPECIFIC' | 'PLYOMETRIC' | 'BALANCE';
    exerciseTypeDisplay: string;  // "Strength Training", "Cardiovascular", etc.
    isCardio: boolean;
    isIsometric: boolean;
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
    // These are calculated by ExerciseResponseDTO.fromEntityForFrontend()
    duration?: string | null;      // "20 mins" format
    calories?: string | null;      // "400-600/hr" format  
    equipment?: string | null;     // "Dumbbells" format
    difficulty?: string | null;    // "Beginner" format
    goal?: string | null;          // "fat-burn" format
    goals?: string[] | null;       // ["fat-burn", "endurance"]
    hasVideo?: boolean | null;     // videoUrl != null
    rating?: string | null;        // "4.5 stars (120 reviews)"
}

// ==================== FRONTEND EXERCISE TYPES (UI-Optimized) ====================

// This represents how exercises appear in your React components after transformation
export type { Exercise } from './exercise';

// ==================== GOAL AND FILTER TYPES ====================

export interface GoalData {
    goal: string;  // "fat-burn", "muscle-building", "endurance", etc.
    count: number;
}

export interface Goal {
    id: string;    // goal code: "fat-burn", "muscle-building"
    name: string;  // display name: "Fat Burn", "Muscle Building"
    emoji: string; // "🔥", "💪"
    count: number;
}

export interface FiltersData {
    equipment: string[];      // ["None", "dumbbells", "barbell", "yoga_mat", ...]
    difficulties: string[];   // ["Beginner", "Intermediate", "Advanced"]
    exerciseTypes?: string[]; // Optional: exercise type options
    muscleGroups?: string[];  // Optional: muscle group options
}

// ==================== API FILTER PARAMETERS ====================

export interface ExerciseFilters {
    goal?: string;           // "fat-burn", "muscle-building", etc.
    difficulty?: string;     // "BEGINNER", "INTERMEDIATE", "ADVANCED" 
    equipment?: string;      // "dumbbells", "barbell", "None"
    exercise_type?: string;  // "STRENGTH", "CARDIO", etc.
    muscle_group?: string;   // "CHEST", "LEGS", etc.
    q?: string;             // search query
    page?: number;          // pagination
    size?: number;          // page size
    sort?: string;          // sort field
    direction?: 'asc' | 'desc'; // sort direction
}

// ==================== SCHEDULED WORKOUT TYPES ====================

export interface ScheduledWorkoutResponse {
    // =============================================================================
    // BASIC IDENTIFICATION & SCHEDULING
    // =============================================================================
    id: number;
    scheduledDate: string; // ISO date string "2025-01-20"
    status: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'SKIPPED' | 'RESCHEDULED';

    // =============================================================================
    // EXERCISE CONFIGURATION FIELDS
    // =============================================================================
    // Strength exercise fields
    sets?: number;
    reps?: string;
    weight?: number;
    restSeconds?: number;
    tempo?: string;
    targetRpe?: number;

    // Cardio exercise fields
    targetDurationMinutes?: number;
    targetDistanceKm?: number;
    targetPace?: number;

    // Isometric exercise fields
    holdDurationSeconds?: number;

    // =============================================================================
    // PROGRAM CONTEXT & SCHEDULING
    // =============================================================================
    weekNumber?: number; // Which week of the program
    dayOfWeek?: number; // 1=Monday, 7=Sunday
    estimatedDurationMinutes?: number;

    // =============================================================================
    // USER CUSTOMIZATIONS & NOTES
    // =============================================================================
    customNotes?: string;
    reminderTime?: string; // ISO datetime string

    // =============================================================================
    // COMPLETION & TRACKING
    // =============================================================================
    completedAt?: string; // ISO datetime string

    // =============================================================================
    // METADATA & AUDIT FIELDS
    // =============================================================================
    createdAt: string; // ISO datetime string
    updatedAt: string; // ISO datetime string
    createdByUserId?: number; // For coach-assigned workouts

    // =============================================================================
    // RELATED ENTITY INFORMATION
    // =============================================================================
    workoutPlan?: WorkoutPlanInfo;
    user?: UserInfo;
    program?: WorkoutProgramInfo; // Optional - only if part of program
    completedSession?: WorkoutSessionInfo; // Only if completed
}

// =============================================================================
// SUPPORTING INTERFACES FOR SCHEDULED WORKOUTS
// =============================================================================

export interface WorkoutPlanInfo {
    id: number;
    name: string;
    description?: string;
    difficulty: string; // BEGINNER, INTERMEDIATE, ADVANCED
    estimatedDurationMinutes?: number;
    exerciseCount: number;
    category?: string;
    imageUrl?: string;
    isPublic?: boolean;
}

export interface UserInfo {
    id: number;
    username: string;
    email?: string;
    firstName?: string;
    lastName?: string;
    subscriptionTier?: string; // FREE, PLUS, PRO
}

export interface WorkoutProgramInfo {
    id: number;
    name: string;
    description?: string;
    totalWeeks?: number;
    difficulty?: string;
    category?: string;
    imageUrl?: string;
    isActive?: boolean;
}

export interface WorkoutSessionInfo {
    id: number;
    startTime?: string; // ISO datetime string
    endTime?: string; // ISO datetime string
    actualDurationMinutes?: number;
    notes?: string;
    completed?: boolean;
}

// ==================== WORKOUT SESSION TYPES ====================

export interface WorkoutSessionRequest {
    workoutPlanId: number;
    date?: string; // ISO date string, defaults to today
    totalDurationMinutes?: number;
    estimatedCalories?: number;
    difficultyRating?: number; // 1-10
    overallEffort?: number; // 1.0-10.0
    mood?: 'ENERGETIC' | 'TIRED' | 'MOTIVATED' | 'FOCUSED' | 'STRESSED' | 'RELAXED' | 'PUMPED' | 'SLUGGISH';
    location?: 'HOME' | 'GYM' | 'PARK' | 'OFFICE' | 'HOTEL' | 'BEACH' | 'TRAIL' | 'STUDIO' | 'OTHER';
    notes?: string;
    scheduledWorkoutId?: number;
    programId?: number;
    weekNumber?: number;
    isShared?: boolean;
}

export interface WorkoutSessionResponse {
    id: number;
    date: string; // ISO date string
    totalDurationMinutes?: number;
    estimatedCalories?: number;
    difficultyRating?: number;
    overallEffort?: number;
    mood?: string;
    location?: string;
    notes?: string;
    workoutPlanId: number;
    workoutPlanName: string;
    workoutPlanCategory?: string;
    programId?: number;
    programName?: string;
    weekNumber?: number;
    scheduledWorkoutId?: number;
    isShared: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface WorkoutStatsResponse {
    // ===== CORE TOTALS =====
    totalScheduledWorkouts: number;
    totalCompletedWorkouts: number;
    overallCompletionRate: number;  // 0-100 percentage
    currentStreekDays: number;
    longestStreakDays: number;
    averageDurationMinutes: number;

    // ===== WEEKLY STATS =====
    thisWeekScheduled: number;
    thisWeekCompleted: number;
    weeklyCompletionRate: number;
    completionRateThisWeek?: number;  // Alternative naming - your backend might use this

    // ===== MONTHLY STATS =====
    thisMonthScheduled: number;
    thisMonthCompleted: number;
    monthlyCompletionRate: number;
    completionRateThisMonth?: number;  // Alternative naming - your backend might use this

    // ===== BEHAVIORAL INSIGHTS =====
    favoriteExerciseType?: string;
    averageWorkoutsPerWeek?: number;
    mostActiveDay?: string;  // e.g., "Monday", "Tuesday", etc.

    // ===== PERFORMANCE METRICS =====
    totalCaloriesBurned?: number;
    totalDistanceKm?: number;  // For cardio tracking

    // ===== ADDITIONAL INSIGHTS =====
    averageCaloriesPerWorkout?: number;
    averageDistancePerCardioSession?: number;
    totalWorkoutTimeHours?: number;
    preferredWorkoutTimeOfDay?: string;  // e.g., "Morning", "Evening"

    // ===== GOAL TRACKING =====
    weeklyGoalAchievementRate?: number;  // Percentage of weeks where weekly goal was met
    consistencyScore?: number;  // Algorithm-based consistency rating (0-100)
}

// ==================== PERFORMANCE RECORD TYPES ====================

export interface PerformanceRequest {
    workoutLogId: number; // This maps to workoutSessionId in your backend
    exerciseId: number;
    setNumber: number;

    // Basic metrics
    reps?: number;
    weight?: number;

    // Cardio metrics
    durationMinutes?: number;
    durationSeconds?: number;
    distanceKm?: number;
    caloriesBurned?: number;

    // Advanced metrics (RPE scale, form rating)
    perceivedExertion?: number; // 1-10 RPE scale
    formRating?: number; // 1-10
    restSeconds?: number;
    tempo?: string; // "3-1-2-1"

    // Specialized metrics
    holdDurationSeconds?: number;
    balanceScore?: number;
    jumpHeightCm?: number;
    powerOutputWatts?: number;

    // Professional context
    assignedByTrainerId?: number;
    targetReps?: number;
    targetWeight?: number;
    achievementStatus?: 'NOT_SET' | 'EXCEEDED' | 'MET' | 'BELOW_TARGET' | 'PARTIAL';

    // Context
    notes?: string;
    equipmentUsed?: string;
    workoutEnvironment?: string;
}

export interface PerformanceResponse {
    id: number;
    workoutLogId: number;
    exerciseId: number;
    exerciseName: string;
    exerciseCategory: string;
    setNumber: number;

    // Basic metrics
    reps?: number;
    weight?: number;
    volume?: number; // Calculated: weight × reps

    // Cardio metrics with calculations
    durationMinutes?: number;
    durationSeconds?: number;
    totalDurationSeconds?: number;
    distanceKm?: number;
    caloriesBurned?: number;
    pace?: number; // Calculated: minutes per km
    speed?: number; // Calculated: km per hour

    // Advanced metrics
    perceivedExertion?: number;
    intensityLevel?: 'LOW' | 'MODERATE' | 'HIGH' | 'MAXIMUM';
    formRating?: number;
    restSeconds?: number;
    tempo?: string;

    // Performance analytics (calculated by backend)
    performanceScore?: number; // 0-100
    isPersonalRecord?: boolean;
    exceededTargets?: boolean;
    efficiencyPercentage?: number;

    // Context
    notes?: string;
    equipmentUsed?: string;
    workoutEnvironment?: string;

    // Audit
    createdAt: string;
    updatedAt: string;
}

// ==================== FRONTEND WORKOUT TYPES (Your Current Structure) ====================

// These represent your current frontend workout management structures
// We'll need to transform backend data into these for compatibility

export interface WorkoutSet {
    id: string;
    setNumber: number;
    targetReps: string;
    actualReps?: number;
    targetWeight?: number;
    actualWeight?: number;
    targetRpe?: number;
    actualRpe?: number;
    restSeconds?: number;
    completed: boolean;
    notes?: string;
    completedAt?: Date;
}

export interface WorkoutExercise {
    id: string;
    scheduledExercise: ScheduledExercise;
    sets: WorkoutSet[];
    completed: boolean;
    skipped: boolean;
    startedAt?: Date;
    completedAt?: Date;
    notes?: string;
}

export interface WorkoutSession {
    id: string;
    date: string;
    exercises: WorkoutExercise[];
    status: 'not_started' | 'in_progress' | 'paused' | 'completed' | 'cancelled';
    startedAt?: Date;
    completedAt?: Date;
    pausedAt?: Date;
    totalDurationMinutes?: number;
    currentExerciseIndex: number;
    currentSetIndex: number;
    notes?: string;
}

// ==================== REQUEST TYPES ====================

export interface ScheduledWorkoutRequest {
    workoutPlanId: number;
    scheduledDate: string; // ISO date string
    customNotes?: string;
    reminderTime?: string; // ISO datetime string
    programId?: number;
    weekNumber?: number;
    estimatedDurationMinutes?: number;
}

export interface RescheduleWorkoutRequest {
    newScheduledDate: string; // ISO date string
    reason?: string;
}

// ==================== UI STATE TYPES ====================

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

// ==================== PAGINATION TYPES ====================

export interface PageRequest {
    page: number;
    size: number;
    sort?: string[];
}

export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
    hasNext?: boolean;
    hasPrevious?: boolean;
}

// ==================== API CLIENT INTERFACES ====================

// Updated to match your current structure while supporting backend integration
export interface ExerciseApiClient {
    // Public exercise endpoints
    getPublicExercises: (filters?: ExerciseFilters) => Promise<BackendExercise[]>;
    searchExercises: (query: string, filters?: ExerciseFilters) => Promise<BackendExercise[]>;
    getExerciseById: (id: number) => Promise<BackendExercise>;

    // Goal and filter endpoints
    getGoals: () => Promise<GoalData[]>;
    getFilters: () => Promise<FiltersData>;

    // New backend endpoints for full integration
    getWorkoutPlans: () => Promise<WorkoutPlanInfo[]>;
    getWorkoutPlan: (id: number) => Promise<WorkoutPlanInfo>;
}

// Calendar API client interface
export interface CalendarApiClient {
    getCalendarView: (startDate: string, endDate: string) => Promise<ScheduledWorkoutResponse[]>;
    scheduleWorkout: (request: ScheduledWorkoutRequest) => Promise<ScheduledWorkoutResponse>;
    rescheduleWorkout: (id: number, request: RescheduleWorkoutRequest) => Promise<ScheduledWorkoutResponse>;
    cancelWorkout: (id: number) => Promise<void>;
    startWorkout: (id: number) => Promise<ScheduledWorkoutResponse>;
    getTodaysWorkouts: () => Promise<ScheduledWorkoutResponse[]>;
}

// Workout session API client interface
export interface WorkoutSessionApiClient {
    createSession: (request: WorkoutSessionRequest) => Promise<WorkoutSessionResponse>;
    updateSession: (id: number, request: Partial<WorkoutSessionRequest>) => Promise<WorkoutSessionResponse>;
    getSession: (id: number) => Promise<WorkoutSessionResponse>;
    completeSession: (id: number) => Promise<WorkoutSessionResponse>;
    deleteSession: (id: number) => Promise<void>;
}

// Performance API client interface
export interface PerformanceApiClient {
    recordPerformance: (request: PerformanceRequest) => Promise<PerformanceResponse>;
    updatePerformance: (id: number, request: Partial<PerformanceRequest>) => Promise<PerformanceResponse>;
    getPerformanceBySession: (sessionId: number) => Promise<PerformanceResponse[]>;
    getPerformanceByExercise: (exerciseId: number) => Promise<PerformanceResponse[]>;
    deletePerformance: (id: number) => Promise<void>;
}

// ==================== ERROR TYPES ====================

export interface ErrorResponse {
    timestamp: string;
    status: number;
    error: string;
    message: string;
    path: string;
    validationErrors?: ValidationError[];
}

export interface ValidationError {
    field: string;
    message: string;
    rejectedValue: any;
}

// ==================== UTILITY TYPES ====================

export type DateString = string; // ISO date format "2025-01-20"
export type DateTimeString = string; // ISO datetime format "2025-01-20T14:30:00"

// Type guards for runtime type checking
export const isBackendExercise = (obj: any): obj is BackendExercise => {
    return obj && typeof obj.id === 'number' && typeof obj.name === 'string';
};

export const isScheduledWorkoutResponse = (obj: any): obj is ScheduledWorkoutResponse => {
    return obj && typeof obj.id === 'number' && typeof obj.scheduledDate === 'string';
};

export const isPerformanceResponse = (obj: any): obj is PerformanceResponse => {
    return obj && typeof obj.id === 'number' && typeof obj.exerciseId === 'number';
};

// ==================== API ENDPOINT CONSTANTS ====================

export const API_ENDPOINTS = {
    // Authentication
    AUTH: {
        LOGIN: '/api/auth/login',
        REGISTER: '/api/auth/register',
        REFRESH: '/api/auth/refresh-token',
        ME: '/api/auth/me',
        CHECK_EMAIL: '/api/auth/check-email',
        CHECK_USERNAME: '/api/auth/check-username',
        LOGOUT: '/api/auth/logout'
    },

    // Exercises (public endpoints)
    EXERCISES: {
        PUBLIC: '/api/exercises/public',
        PUBLIC_SEARCH: '/api/exercises/public/search',
        GOALS: '/api/exercises/goals',
        FILTERS: '/api/exercises/public/filters',
        BY_ID: (id: number) => `/api/exercises/public/${id}`
    },

    // Calendar & Scheduling
    CALENDAR: {
        BASE: '/api/calendar',
        EXERCISES: '/api/calendar/exercises',
        EXERCISE_BY_ID: (id: string) => `/api/calendar/exercises/${id}`,
        EXERCISE_COMPLETE: (id: string) => `/api/calendar/exercises/${id}/complete`,
        EXERCISE_BY_DATE: (date: string) => `/api/calendar/exercises/date/${date}`,
        TODAY: '/api/calendar/today',
        UPCOMING: '/api/calendar/upcoming',
        OVERDUE: '/api/calendar/overdue',
        SCHEDULE: '/api/calendar/schedule',
        RESCHEDULE: (id: number) => `/api/calendar/${id}/reschedule`,
        START: (id: number) => `/api/calendar/${id}/start`,
        DELETE: (id: number) => `/api/calendar/${id}`,
        STATS: '/api/calendar/stats',
        WORKOUT_PLANS: '/api/calendar/workout-plans'
    },

    // Workout Sessions
    WORKOUT_SESSIONS: {
        BASE: '/api/workout-sessions',
        BY_ID: (id: number) => `/api/workout-sessions/${id}`,
        RECENT: '/api/workout-sessions/recent',
        BY_DATE: (date: string) => `/api/workout-sessions/date/${date}`,
        TODAY: '/api/workout-sessions/today',
        START_SCHEDULED: (scheduledId: number) => `/api/workout-sessions/scheduled/${scheduledId}/start`
    },

    // Performance Records
    PERFORMANCE: {
        BASE: '/api/performance',
        BY_ID: (id: number) => `/api/performance/${id}`,
        BY_WORKOUT_SESSION: (sessionId: number) => `/api/performance/workout-session/${sessionId}`,
        BY_EXERCISE: (exerciseId: number) => `/api/performance/exercise/${exerciseId}`,
        ANALYTICS: '/api/performance/analytics'
    },

    // Health check
    HEALTH: '/api/health'
} as const;