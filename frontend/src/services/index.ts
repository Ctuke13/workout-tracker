// services/index.ts - Updated to export real API services

// ==================== MOCK DATA (for development/testing) ====================
// export * from './mockData';

// ==================== REAL API SERVICES ====================
// These are the services your application is currently using
export * from './exerciseApi';
export * from './calendarApi';
export * from './workoutPlanApi';
export {progressApi} from './progressApi';

// ==================== DATA TRANSFORMATION SERVICES ====================
export * from './transformers';

// ==================== HTTP CLIENT & API CLIENT ====================
export * from './apiClient';

// ==================== ADDITIONAL API SERVICES ====================
// Uncomment these as you implement them
// export * from './userApi';
// export * from './authApi';
// export * from './analyticsApi';
// export * from './subscriptionApi';
// export * from './performanceApi';

// ==================== UTILITY SERVICES ====================
// Note: localStorage/sessionStorage are not supported in Claude.ai artifacts
// but you can uncomment these for your local development
// export * from './localStorage';
// export * from './sessionStorage';
// export * from './httpClient';
// export * from './errorHandler';

// ==================== CONFIGURATION ====================
// Uncomment these as you implement them
// export * from './config';
// export * from './constants';

// ==================== TYPE EXPORTS ====================
// Re-export commonly used types for convenience
export type {
    Exercise,
    ScheduledExercise,
    ExerciseConfiguration
} from '../types/exercise';

export type {
    ScheduledWorkoutResponse,
    WorkoutPlanScheduleRequest,
    WorkoutPlanInfo,
    BackendExercise
} from '../types/api';