import apiClient from './apiClient';
import {WorkoutResults} from '../types/exercise';
import {transformBackendPerformanceToWorkoutResults} from '../services/transformers';

// Additional types for performance analytics
export interface WorkoutSessionSummary {
    sessionId: string;
    date: string;
    totalWorkouts: number;
    totalDuration: number;
    averagePerformanceRating: string;
    workoutResults: WorkoutResults[];
}

export interface PerformanceAnalytics {
    totalWorkouts: number;
    totalVolume: number;
    averagePerformanceScore: number;
    personalRecordsCount: number;
    improvementTrend: 'IMPROVING' | 'STABLE' | 'DECLINING';
    consistencyScore: number;
    period: string;
}

class PerformanceApiService {
    /**
     * Transform backend data to ensure arrays are never undefined
     */
    private transformWorkoutResults(backendData: any): WorkoutResults {
        return transformBackendPerformanceToWorkoutResults(backendData);
    }

    /**
     * Get detailed performance results for a specific completed workout
     */
    async getWorkoutPerformance(scheduledExerciseId: string): Promise<WorkoutResults> {
        try {
            console.log('📊 Fetching performance data for exercise:', scheduledExerciseId);

            const response = await apiClient.get<any>(
                `/api/performance/workout/${scheduledExerciseId}`
            );

            return this.transformWorkoutResults(response);

        } catch (error) {
            console.error('❌ Failed to fetch workout performance:', error);
            throw new Error('Unable to load workout performance data');
        }
    }

    /**
     * Get performance data for multiple workouts in batch
     */
    async getBatchWorkoutPerformance(scheduledExerciseIds: string[]): Promise<Record<string, WorkoutResults>> {
        try {
            console.log('📊 Fetching batch performance data for exercises:', scheduledExerciseIds);

            const response = await apiClient.post<Record<string, any>>(
                '/api/performance/workout/batch',
                {scheduledExerciseIds}
            );

            // Transform each result to ensure arrays are never undefined
            const transformedResults: Record<string, WorkoutResults> = {};
            Object.entries(response).forEach(([key, value]) => {
                transformedResults[key] = this.transformWorkoutResults(value);
            });

            return transformedResults;

        } catch (error) {
            console.error('❌ Failed to fetch batch workout performance:', error);
            return {}; // Return empty object on error
        }
    }

    /**
     * Get workout session summary with all exercises
     */
    async getWorkoutSessionSummary(workoutSessionId: string): Promise<WorkoutSessionSummary> {
        try {
            const response = await apiClient.get<any>(
                `/api/performance/session/${workoutSessionId}`
            );

            // Transform all workout results in the session
            const transformedWorkoutResults = response.workoutResults?.map((result: any) =>
                this.transformWorkoutResults(result)
            ) || [];

            return {
                ...response,
                workoutResults: transformedWorkoutResults
            };

        } catch (error) {
            console.error('❌ Failed to fetch workout session summary:', error);
            throw new Error('Unable to load workout session data');
        }
    }

    /**
     * Get recent personal records
     */
    async getRecentPersonalRecords(limit: number = 10): Promise<any[]> {
        try {
            const response = await apiClient.get<any[]>(
                `/api/performance/records?limit=${limit}`
            );
            return response || [];

        } catch (error) {
            console.error('❌ Failed to fetch personal records:', error);
            return [];
        }
    }

    /**
     * Get performance analytics for dashboard
     */
    async getPerformanceAnalytics(timeframe: 'week' | 'month' | 'year' = 'month'): Promise<PerformanceAnalytics> {
        try {
            const response = await apiClient.get<PerformanceAnalytics>(
                `/api/performance/analytics?timeframe=${timeframe}`
            );

            return {
                totalWorkouts: response.totalWorkouts || 0,
                totalVolume: response.totalVolume || 0,
                averagePerformanceScore: response.averagePerformanceScore || 0,
                personalRecordsCount: response.personalRecordsCount || 0,
                improvementTrend: response.improvementTrend || 'STABLE',
                consistencyScore: response.consistencyScore || 0,
                period: response.period || timeframe,
            };

        } catch (error) {
            console.error('❌ Failed to fetch performance analytics:', error);
            return {
                totalWorkouts: 0,
                totalVolume: 0,
                averagePerformanceScore: 0,
                personalRecordsCount: 0,
                improvementTrend: 'STABLE',
                consistencyScore: 0,
                period: timeframe,
            };
        }
    }
}

// Export singleton instance
export const performanceApi = new PerformanceApiService();
export default performanceApi;