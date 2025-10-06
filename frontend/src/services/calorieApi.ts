import apiClient from './apiClient';

// ==================== TYPE DEFINITIONS ====================

export interface CaloriePreferences {
    calorieTrackingEnabled: boolean;
    preferredCalorieUnit: string;
    calorieGoalDaily?: number;
    calorieAdjustmentFactor: number;
}

export interface CalorieSessionBreakdown {
    sessionId: number;
    totalCaloriesCalculated?: number;
    actualCaloriesBurned?: number;
    userReportedCalories?: number;
    calculationStatus?: string;
    calorieUnit: string;
    formattedCalories?: string;
    exerciseBreakdown: ExerciseCalorieData[];
}

export interface ExerciseCalorieData {
    exerciseName: string;
    setNumber: number;
    calories?: number;
    metValue?: number;
    intensity?: string;
    perceivedExertion?: number;
}

export interface CalorieStats {
    totalCalories: number;
    workoutCount: number;
    averagePerWorkout: number;
    highestCalorieWorkout: number;
    dailyGoal?: number;
    goalProgress?: number;
}

export interface CalorieFeedback {
    accuracyRating?: number; // 1-5 stars
    userReportedCalories?: number;
}

// ==================== CALORIE API SERVICE ====================

class CalorieApiService {
    /**
     * Get detailed calorie breakdown for a workout session
     */
    async getSessionCalories(sessionId: number): Promise<CalorieSessionBreakdown> {
        try {
            const response = await apiClient.get<CalorieSessionBreakdown>(
                `/api/calories/session/${sessionId}`
            );
            return response;
        } catch (error) {
            console.error('Failed to fetch session calories:', error);
            throw new Error('Unable to load calorie data for this workout');
        }
    }

    /**
     * Recalculate calories for a workout session
     * Useful if user updates their weight or if exercise MET values change
     */
    async recalculateSessionCalories(sessionId: number): Promise<{
        sessionId: number;
        totalCalories: number;
        status: string
    }> {
        try {
            const response = await apiClient.post<{
                sessionId: number;
                totalCalories: number;
                status: string
            }>(
                `/api/calories/session/${sessionId}/recalculate`
            );
            return response;
        } catch (error) {
            console.error('Failed to recalculate calories:', error);
            throw new Error('Unable to recalculate calories');
        }
    }

    /**
     * Submit user feedback on calorie accuracy
     */
    async submitFeedback(sessionId: number, feedback: CalorieFeedback): Promise<{
        status: string;
        message: string;
    }> {
        try {
            const response = await apiClient.post<{
                status: string;
                message: string;
            }>(
                `/api/calories/session/${sessionId}/feedback`,
                feedback
            );
            return response;
        } catch (error) {
            console.error('Failed to submit calorie feedback:', error);
            throw new Error('Unable to submit feedback');
        }
    }

    /**
     * Get user's calorie tracking preferences
     */
    async getPreferences(): Promise<CaloriePreferences> {
        try {
            const response = await apiClient.get<CaloriePreferences>(
                '/api/calories/preferences'
            );

            // Ensure defaults for optional fields
            return {
                calorieTrackingEnabled: response.calorieTrackingEnabled ?? true,
                preferredCalorieUnit: response.preferredCalorieUnit || 'CALORIES',
                calorieGoalDaily: response.calorieGoalDaily,
                calorieAdjustmentFactor: response.calorieAdjustmentFactor ?? 1.0
            };
        } catch (error) {
            console.error('Failed to fetch calorie preferences:', error);
            // Return safe defaults on error
            return {
                calorieTrackingEnabled: true,
                preferredCalorieUnit: 'CALORIES',
                calorieAdjustmentFactor: 1.0
            };
        }
    }

    /**
     * Update user's calorie tracking preferences
     */
    async updatePreferences(preferences: Partial<CaloriePreferences>): Promise<{
        status: string;
    }> {
        try {
            // Validate adjustment factor before sending
            if (preferences.calorieAdjustmentFactor !== undefined) {
                const factor = preferences.calorieAdjustmentFactor;
                if (factor < 0.5 || factor > 1.5) {
                    throw new Error('Adjustment factor must be between 0.5 and 1.5');
                }
            }

            const response = await apiClient.put<{
                status: string;
            }>(
                '/api/calories/preferences',
                preferences
            );
            return response;
        } catch (error) {
            console.error('Failed to update calorie preferences:', error);
            throw error;
        }
    }

    /**
     * Get calorie statistics for a date range
     */
    async getStats(startDate: string, endDate: string): Promise<CalorieStats> {
        try {
            const response = await apiClient.get<CalorieStats>(
                `/api/calories/stats?startDate=${startDate}&endDate=${endDate}`
            );

            // Ensure all fields have safe defaults
            return {
                totalCalories: response.totalCalories || 0,
                workoutCount: response.workoutCount || 0,
                averagePerWorkout: response.averagePerWorkout || 0,
                highestCalorieWorkout: response.highestCalorieWorkout || 0,
                dailyGoal: response.dailyGoal,
                goalProgress: response.goalProgress
            };
        } catch (error) {
            console.error('Failed to fetch calorie stats:', error);
            // Return empty stats on error
            return {
                totalCalories: 0,
                workoutCount: 0,
                averagePerWorkout: 0,
                highestCalorieWorkout: 0
            };
        }
    }

    /**
     * Get calorie stats for the last 30 days
     */
    async getMonthlyStats(): Promise<CalorieStats> {
        const endDate = new Date().toISOString().split('T')[0];
        const startDate = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
            .toISOString().split('T')[0];

        return this.getStats(startDate, endDate);
    }

    /**
     * Get calorie stats for the current week
     */
    async getWeeklyStats(): Promise<CalorieStats> {
        const endDate = new Date().toISOString().split('T')[0];
        const startDate = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000)
            .toISOString().split('T')[0];

        return this.getStats(startDate, endDate);
    }

    /**
     * Format calories based on user's preferred unit
     */
    formatCalories(calories: number, unit: string): string {
        if (unit === 'KILOJOULES') {
            const kj = calories * 4.184;
            return `${kj.toFixed(1)} kJ`;
        }
        return `${calories} cal`;
    }

    /**
     * Get intensity description for display
     */
    getIntensityDescription(intensity?: string): string {
        switch (intensity) {
            case 'LIGHT':
                return 'Light intensity - conversational pace';
            case 'MODERATE':
                return 'Moderate intensity - challenging but sustainable';
            case 'VIGOROUS':
                return 'Vigorous intensity - hard effort';
            case 'CUSTOM':
                return 'Custom intensity';
            default:
                return 'Unknown intensity';
        }
    }

    /**
     * Get intensity color class for UI
     */
    getIntensityColorClass(intensity?: string): string {
        switch (intensity) {
            case 'LIGHT':
                return 'text-green-400 bg-green-500/20';
            case 'MODERATE':
                return 'text-yellow-400 bg-yellow-500/20';
            case 'VIGOROUS':
                return 'text-red-400 bg-red-500/20';
            case 'CUSTOM':
                return 'text-purple-400 bg-purple-500/20';
            default:
                return 'text-gray-400 bg-gray-500/20';
        }
    }
}

// Export singleton instance
export const calorieApi = new CalorieApiService();
export default calorieApi;