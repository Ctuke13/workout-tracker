import apiClient from './apiClient';
import {
    TimePeriod,
    mapPeriodToBackend,
    AllPeriodSummaries,
    TimePeriodSummary,
    PersonalRecord,
    TopExercise,
    ExerciseProgressionPoint,
    PerformanceTrackerResponse
} from '../types/analytics';

/**
 * Analytics API Service
 * Handles all analytics and insights endpoints
 */

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
    async getTopExercises(period: TimePeriod = 'WEEK', limit: number = 5): Promise<TopExercise[]> {
        const backendPeriod = mapPeriodToBackend(period);
        return apiClient.get<TopExercise[]>(`/api/analytics/top-exercises?period=${backendPeriod}&limit=${limit}`);
    },

    /**
     * Get exercise progression data
     */
    async getExerciseProgression(exerciseId: number, weeks: number = 12): Promise<ExerciseProgressionPoint[]> {
        return apiClient.get<ExerciseProgressionPoint[]>(`/api/analytics/exercise/${exerciseId}/progression?weeks=${weeks}`);
    },

    /**
     * Get performance tracker data for charts
     */
    async getPerformanceTrackerData(
        metric: string,
        period: string,
        exerciseId?: number | null // ✅ ADD THIS PARAMETER
    ): Promise<PerformanceTrackerResponse> {
        // ✅ Build query string with optional exerciseId
        let url = `/api/analytics/performance-tracker?metric=${metric}&period=${period}`;
        if (exerciseId) {
            url += `&exerciseId=${exerciseId}`;
        }

        const response = await apiClient.get<PerformanceTrackerResponse>(url);
        return response;
    }
};

// Re-export types for convenience
export type {
    AllPeriodSummaries,
    TimePeriodSummary,
    PersonalRecord,
    TopExercise,
    ExerciseProgressionPoint,
    PerformanceTrackerResponse
};