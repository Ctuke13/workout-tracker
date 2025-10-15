import apiClient from './apiClient';

/**
 * Analytics API Service
 * Handles all analytics and insights endpoints
 */

// ==================== TYPES ====================

export interface TimePeriodSummary {
    period: string;
    startDate: string;
    endDate: string;
    workouts: number;
    minutes: number;
    volume: number;
    workoutChange: number;
    minutesChange: number;
    volumeChange: number;
    averageMinutesPerWorkout?: number;
}

export interface AllPeriodSummaries {
    week: TimePeriodSummary;
    month: TimePeriodSummary;
    year: TimePeriodSummary;
    allTime: TimePeriodSummary;
}

export interface PersonalRecord {
    type: string;
    exerciseName: string;
    exerciseId: number;
    value: number;
    reps?: number;
    weight?: number;
    date: string;
    unit: string;
}

export interface TopExercise {
    exerciseId: number;
    exerciseName: string;
    count: number;
    volume: number;
}

export interface ExerciseProgressionPoint {
    date: string;
    weight: number;
    reps: number;
    volume: number;
    setNumber: number;
}

// ==================== API CLIENT ====================

export const analyticsApi = {
    /**
     * Get summaries for all time periods (MOST EFFICIENT)
     */
    async getAllPeriodSummaries(): Promise<AllPeriodSummaries> {
        return apiClient.get<AllPeriodSummaries>('/api/analytics/summary/all');
    },

    /**
     * Get weekly summary
     */
    async getWeeklySummary(): Promise<TimePeriodSummary> {
        return apiClient.get<TimePeriodSummary>('/api/analytics/summary/week');
    },

    /**
     * Get monthly summary
     */
    async getMonthlySummary(): Promise<TimePeriodSummary> {
        return apiClient.get<TimePeriodSummary>('/api/analytics/summary/month');
    },

    /**
     * Get yearly summary
     */
    async getYearlySummary(): Promise<TimePeriodSummary> {
        return apiClient.get<TimePeriodSummary>('/api/analytics/summary/year');
    },

    /**
     * Get all-time summary
     */
    async getAllTimeSummary(): Promise<TimePeriodSummary> {
        return apiClient.get<TimePeriodSummary>('/api/analytics/summary/all-time');
    },

    /**
     * Get recent personal records
     */
    async getRecentPersonalRecords(days: number = 30): Promise<PersonalRecord[]> {
        return apiClient.get<PersonalRecord[]>(`/api/analytics/personal-records/recent?days=${days}`);
    },

    /**
     * Get all-time personal records
     */
    async getAllTimePersonalRecords(): Promise<PersonalRecord[]> {
        return apiClient.get<PersonalRecord[]>('/api/analytics/personal-records/all-time');
    },

    /**
     * Get top exercises for a time period
     */
    async getTopExercises(period: 'WEEK' | 'MONTH' | 'YEAR' | 'ALL_TIME' = 'WEEK', limit: number = 5): Promise<TopExercise[]> {
        return apiClient.get<TopExercise[]>(`/api/analytics/top-exercises?period=${period}&limit=${limit}`);
    },

    /**
     * Get exercise progression data
     */
    async getExerciseProgression(exerciseId: number, weeks: number = 12): Promise<ExerciseProgressionPoint[]> {
        return apiClient.get<ExerciseProgressionPoint[]>(`/api/analytics/exercise/${exerciseId}/progression?weeks=${weeks}`);
    }
};