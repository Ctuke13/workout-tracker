import apiClient from './apiClient';
import {ScheduledWorkoutResponse, WorkoutPlanInfo} from '../types/api';
import {WorkoutResults} from '../types/exercise';

export class CalendarApiService {
    // ==================== INDIVIDUAL EXERCISE SCHEDULING ====================

    /**
     * ✅ UPDATED: Schedule individual exercise using unified endpoint
     */
    async scheduleIndividualExercise(exerciseData: {
        exerciseId: number;
        scheduledDate: string;
        sets?: number;
        reps?: string;
        weight?: number;
        restSeconds?: number;
        targetRpe?: number;
        tempo?: string;
        targetDurationMinutes?: number;
        targetDistanceKm?: number;
        targetPace?: number;
        holdDurationSeconds?: number;
        notes?: string;
    }): Promise<ScheduledWorkoutResponse> {
        try {
            console.log('📅 API: Scheduling individual exercise:', exerciseData);

            // ✅ UPDATED: Use the unified ScheduledWorkoutController endpoint
            const response = await apiClient.post<ScheduledWorkoutResponse>('/api/calendar/schedule-exercise', exerciseData);

            console.log(`✅ Successfully scheduled exercise: ${exerciseData.exerciseId}`);
            return response;
        } catch (error) {
            console.error('❌ Failed to schedule individual exercise:', error);
            throw new Error('Failed to schedule exercise. Please try again.');
        }
    }

    // ==================== WORKOUT PLAN SCHEDULING ====================

    /**
     * ✅ UPDATED: Schedule a workout plan using unified endpoint
     */
    async scheduleWorkout(workoutData: {
        workoutPlanId: number;
        scheduledDate: string;
        customNotes?: string;
    }): Promise<ScheduledWorkoutResponse> {
        try {
            console.log('📅 API: Scheduling workout plan:', workoutData);

            // ✅ UPDATED: Use the unified ScheduledWorkoutController endpoint
            const response = await apiClient.post<ScheduledWorkoutResponse>('/api/calendar/schedule-workout-plan', workoutData);

            console.log(`✅ Successfully scheduled workout plan: ${workoutData.workoutPlanId}`);
            return response;
        } catch (error) {
            console.error('❌ Failed to schedule workout plan:', error);
            throw new Error('Failed to schedule workout plan. Please try again.');
        }
    }

    /**
     * Get available workout plans for scheduling
     */
    async getWorkoutPlans(): Promise<WorkoutPlanInfo[]> {
        try {
            console.log('📋 API: Getting available workout plans');

            // You'll need to add this endpoint to your backend
            const response = await apiClient.get<WorkoutPlanInfo[]>('/api/workout-plans/public');

            return response;
        } catch (error) {
            console.error('❌ Failed to fetch workout plans:', error);
            throw new Error('Failed to load workout plans');
        }
    }

    // ==================== CALENDAR DATA RETRIEVAL ====================

    /**
     *  Get scheduled workouts with proper query parameter handling
     */
    async getScheduledWorkouts(startDate: string, endDate: string): Promise<ScheduledWorkoutResponse[]> {
        try {
            console.log('📅 API: Getting scheduled workouts with exercise type debugging from', startDate, 'to', endDate);

            const response = await apiClient.get('/api/calendar', {
                startDate,
                endDate
            });

            console.log('🔍 DEBUG: Raw API response structure:', {
                type: typeof response,
                isArray: Array.isArray(response),
                keys: response && typeof response === 'object' ? Object.keys(response) : 'not an object',
                firstItem: Array.isArray(response) ? response[0] :
                    response && typeof response === 'object' ? Object.values(response)[0] : null
            });

            // Handle CalendarViewResponse format
            let workouts: ScheduledWorkoutResponse[] = [];

            if (Array.isArray(response)) {
                workouts = response;
            } else if (response && typeof response === 'object') {
                const calendarResponse = response as any;

                if (calendarResponse.workoutsByDate && typeof calendarResponse.workoutsByDate === 'object') {
                    console.log('🔍 DEBUG: Found workoutsByDate with dates:', Object.keys(calendarResponse.workoutsByDate));

                    // Flatten the map
                    workouts = Object.values(calendarResponse.workoutsByDate).flat() as ScheduledWorkoutResponse[];

                    console.log('🔍 DEBUG: Flattened to', workouts.length, 'workouts');
                } else if (calendarResponse.scheduledWorkouts && Array.isArray(calendarResponse.scheduledWorkouts)) {
                    workouts = calendarResponse.scheduledWorkouts;
                } else if (calendarResponse.data && Array.isArray(calendarResponse.data)) {
                    workouts = calendarResponse.data;
                } else if (calendarResponse.content && Array.isArray(calendarResponse.content)) {
                    workouts = calendarResponse.content;
                } else {
                    console.warn('⚠️ Unexpected response format. Available properties:', Object.keys(calendarResponse));
                    workouts = [];
                }
            }

            // 🔍 DEBUG: Log exercise type information for each workout
            workouts.forEach((workout, index) => {
                console.log(`🔍 DEBUG: Workout ${index + 1}:`, {
                    id: workout.id,
                    hasExercise: !!workout.exercise,
                    exerciseName: workout.exercise?.name,
                    exerciseType: workout.exercise?.exerciseType,
                    isCardio: workout.exercise?.isCardio,
                    isIsometric: workout.exercise?.isIsometric,
                    workoutPlanName: workout.workoutPlan?.name,
                    status: workout.status
                });

                // ✅ CRITICAL: Log if exercise data is missing
                if (!workout.exercise) {
                    console.warn(`⚠️ MISSING EXERCISE DATA for workout ${workout.id}:`, {
                        hasWorkoutPlan: !!workout.workoutPlan,
                        workoutPlanName: workout.workoutPlan?.name,
                        status: workout.status
                    });
                }
            });

            console.log(`✅ Successfully fetched ${workouts.length} scheduled workouts`);
            console.log(`🔍 Workouts with exercise data: ${workouts.filter(w => w.exercise).length}`);
            console.log(`⚠️ Workouts missing exercise data: ${workouts.filter(w => !w.exercise).length}`);

            return workouts;
        } catch (error) {
            console.error('❌ Failed to fetch scheduled workouts:', error);
            throw new Error('Failed to load calendar workouts');
        }
    }

    /**
     * ✅ UNIFIED: Get scheduled exercises for date range
     */
    async getScheduledExercises(startDate: string, endDate: string): Promise<ScheduledWorkoutResponse[]> {
        try {
            console.log('📅 API: Getting scheduled exercises from', startDate, 'to', endDate);

            // ✅ UNIFIED: Use the main exercises endpoint
            const response = await apiClient.get<ScheduledWorkoutResponse[]>('/api/calendar/exercises', {
                startDate,
                endDate
            });

            console.log(`✅ Successfully fetched ${response.length} scheduled exercises`);
            return response;
        } catch (error) {
            console.error('❌ Failed to fetch scheduled exercises:', error);
            throw new Error('Failed to load scheduled exercises');
        }
    }

    /**
     * ✅ UNIFIED: Get exercises for a specific date
     */
    async getExercisesForDate(date: string): Promise<ScheduledWorkoutResponse[]> {
        try {
            console.log('📅 API: Getting exercises for date:', date);

            // ✅ UNIFIED: This endpoint remains the same
            const response = await apiClient.get<ScheduledWorkoutResponse[]>(`/api/calendar/exercises/date/${date}`);

            console.log(`✅ Successfully fetched ${response.length} exercises for ${date}`);
            return response;
        } catch (error) {
            console.error('❌ Failed to fetch exercises for date:', error);
            throw new Error('Failed to load exercises for date');
        }
    }

    // ==================== WORKOUT MANAGEMENT ====================

    /**
     *  Update a scheduled exercise
     */
    async updateScheduledExercise(exerciseId: string, updates: any): Promise<ScheduledWorkoutResponse> {
        try {
            console.log('📅 API: Updating scheduled exercise:', exerciseId);

            //  This endpoint remains the same
            const response = await apiClient.put<ScheduledWorkoutResponse>(`/api/calendar/exercises/${exerciseId}`, updates);

            console.log(`✅ Successfully updated exercise: ${exerciseId}`);
            return response;
        } catch (error) {
            console.error('❌ Failed to update scheduled exercise:', error);
            throw new Error('Failed to update exercise');
        }
    }

    /**
     *  Delete a scheduled exercise
     */
    async deleteScheduledExercise(exerciseId: string): Promise<void> {
        try {
            console.log('📅 API: Deleting scheduled exercise:', exerciseId);

            //  This endpoint remains the same
            await apiClient.delete(`/api/calendar/exercises/${exerciseId}`);

            console.log(`✅ Successfully deleted exercise: ${exerciseId}`);
        } catch (error) {
            console.error('❌ Failed to delete scheduled exercise:', error);
            throw new Error('Failed to delete exercise');
        }
    }

    /**
     *  Mark exercise as completed
     */
    async markExerciseCompleted(
        exerciseId: string,
        completionData?: {
            completedAt?: string;
            totalDurationMinutes?: number;
            notes?: string;
            performanceRating?: 'EXCEEDED' | 'MET' | 'BELOW_TARGET' | 'PARTIAL';
        }
    ): Promise<ScheduledWorkoutResponse> {
        try {
            console.log('✅ API: Marking exercise as completed:', exerciseId);

            const payload = completionData ? {
                completedAt: completionData.completedAt || new Date().toISOString(),
                totalDurationMinutes: completionData.totalDurationMinutes,
                notes: completionData.notes,
                performanceRating: completionData.performanceRating || 'MET'
            } : {
                completedAt: new Date().toISOString(),
                performanceRating: 'MET'
            };

            const response = await apiClient.put<ScheduledWorkoutResponse>(
                `/api/calendar/exercises/${exerciseId}/complete`,
                payload
            );

            console.log(`✅ Successfully marked exercise ${exerciseId} as completed`);
            return response;
        } catch (error) {
            console.error('❌ Failed to mark exercise as completed:', error);
            throw new Error('Failed to mark exercise as completed');
        }
    }

    /**
     * Cancel a scheduled workout
     */
    async cancelScheduledWorkout(scheduledWorkoutId: number): Promise<void> {
        try {
            console.log('📅 API: Cancelling scheduled workout:', scheduledWorkoutId);

            // ✅ UNIFIED: This endpoint remains the same
            await apiClient.delete(`/api/calendar/${scheduledWorkoutId}`);

            console.log(`✅ Successfully cancelled workout: ${scheduledWorkoutId}`);
        } catch (error) {
            console.error('❌ Failed to cancel scheduled workout:', error);
            throw new Error('Failed to cancel workout');
        }
    }

    /**
     * Start a scheduled workout
     */
    async startWorkout(workoutId: string): Promise<ScheduledWorkoutResponse> {
        try {
            console.log('▶️ API: Starting workout:', workoutId);

            // ✅ UNIFIED: This endpoint remains the same
            const response = await apiClient.post<ScheduledWorkoutResponse>(`/api/calendar/${workoutId}/start`);

            console.log(`✅ Successfully started workout: ${workoutId}`);
            return response;
        } catch (error) {
            console.error('❌ Failed to start workout:', error);
            throw new Error('Failed to start workout');
        }
    }

    /**
     * Batch mark multiple exercises as completed
     */
    async batchMarkExercisesCompleted(exerciseCompletions: Array<{
        exerciseId: string;
        completedAt?: string;
        totalDurationMinutes?: number;
        notes?: string;
        performanceRating?: 'EXCEEDED' | 'MET' | 'BELOW_TARGET' | 'PARTIAL';
    }>): Promise<{ successful: string[]; failed: string[] }> {
        try {
            console.log('✅ API: Batch marking exercises as completed:', exerciseCompletions.length);

            const response = await apiClient.post<{ successful: string[]; failed: string[] }>(
                '/api/calendar/exercises/batch-complete',
                {completions: exerciseCompletions}
            );

            console.log(`✅ Batch completion: ${response.successful.length} successful, ${response.failed.length} failed`);
            return response;
        } catch (error) {
            console.error('❌ Failed to batch mark exercises as completed:', error);

            // Fallback: try to complete each exercise individually
            const results: { successful: string[]; failed: string[] } = {successful: [], failed: []};

            for (const completion of exerciseCompletions) {
                try {
                    await this.markExerciseCompleted(completion.exerciseId, {
                        completedAt: completion.completedAt,
                        totalDurationMinutes: completion.totalDurationMinutes,
                        notes: completion.notes,
                        performanceRating: completion.performanceRating
                    });
                    results.successful.push(completion.exerciseId);
                } catch (err) {
                    results.failed.push(completion.exerciseId);
                }
            }

            return results;
        }
    }

    /**
     * Save detailed workout results for performance tracking
     */
    async saveWorkoutResults(exerciseId: string, resultsData: {
        sets: Array<{
            setNumber: number;
            targetReps: number;
            actualReps: number;
            targetWeight?: number;
            actualWeight?: number;
            targetWeightUnit: 'kg' | 'lbs';
            rpe?: number;
            restSeconds?: number;
            completed: boolean;
            actualDurationMinutes?: number;
            actualHoldSeconds?: number;
        }>;
        totalDurationMinutes: number;
        caloriesBurned?: number;
        averageHeartRate?: number;
        notes?: string;
        performanceRating: 'EXCEEDED' | 'MET' | 'BELOW_TARGET' | 'PARTIAL';
    }): Promise<void> {
        try {
            console.log('💾 API: Saving detailed workout results:', exerciseId);

            await apiClient.post(`/api/calendar/exercises/${exerciseId}/results`, resultsData);

            console.log(`✅ Successfully saved workout results for exercise ${exerciseId}`);
        } catch (error) {
            console.error('❌ Failed to save workout results:', error);
            // Don't throw here - workout completion should succeed even if detailed results fail
            console.warn('⚠️ Workout marked as completed, but detailed results could not be saved');
        }
    }

    /**
     * ✅ NEW: Retry failed workout saves
     */
    async retryPendingWorkoutSaves(): Promise<void> {
        try {
            const pendingSaves = JSON.parse(localStorage.getItem('pendingWorkoutSaves') || '[]');

            if (pendingSaves.length === 0) {
                console.log('📱 No pending workout saves to retry');
                return;
            }

            console.log(`🔄 Retrying ${pendingSaves.length} pending workout saves...`);

            const completedRetries: any[] = [];
            const failedRetries: any[] = [];

            for (const pendingSave of pendingSaves) {
                try {
                    // Extract exercises that need to be marked as completed
                    const exerciseCompletions = pendingSave.workout.exercises
                        .filter((ex: any) => ex.completed) // ✅ FIXED: Added explicit type annotation
                        .map((ex: any) => ({ // ✅ FIXED: Added explicit type annotation
                            exerciseId: ex.scheduledExercise.id,
                            completedAt: ex.completedAt?.toISOString?.() || pendingSave.workout.completedAt?.toISOString?.(),
                            totalDurationMinutes: pendingSave.workout.totalDurationMinutes,
                            notes: ex.notes,
                            performanceRating: 'MET' as const
                        }));

                    const result = await this.batchMarkExercisesCompleted(exerciseCompletions);

                    if (result.failed.length === 0) {
                        completedRetries.push(pendingSave);
                        console.log(`✅ Successfully retried workout save: ${pendingSave.workout.id}`);
                    } else {
                        failedRetries.push({
                            ...pendingSave,
                            retryCount: (pendingSave.retryCount || 0) + 1
                        });
                        console.warn(`⚠️ Partial success for workout save: ${pendingSave.workout.id}`);
                    }
                } catch (error) {
                    failedRetries.push({
                        ...pendingSave,
                        retryCount: (pendingSave.retryCount || 0) + 1
                    });
                    console.error(`❌ Failed to retry workout save: ${pendingSave.workout.id}`, error);
                }
            }

            // Update localStorage with remaining failed retries (max 3 attempts)
            const remainingRetries = failedRetries.filter(retry => retry.retryCount < 3);
            localStorage.setItem('pendingWorkoutSaves', JSON.stringify(remainingRetries));

            console.log(`🔄 Retry completed: ${completedRetries.length} successful, ${remainingRetries.length} remaining`);

        } catch (error) {
            console.error('❌ Failed to process pending workout saves:', error);
        }
    }

    /**
     * Delete a workout
     */
    async deleteWorkout(workoutId: string): Promise<void> {
        try {
            console.log('🗑️ API: Deleting workout:', workoutId);

            // ✅ UNIFIED: This endpoint remains the same
            await apiClient.delete(`/api/calendar/${workoutId}`);

            console.log(`✅ Successfully deleted workout: ${workoutId}`);
        } catch (error) {
            console.error('❌ Failed to delete workout:', error);
            throw new Error('Failed to delete workout');
        }
    }

    /**
     * Get workout results for a completed exercise
     */
    async getWorkoutResults(exerciseId: string): Promise<WorkoutResults | null> {
        try {
            const response = await apiClient.get<WorkoutResults>(`/api/calendar/exercises/${exerciseId}/results`);
            return response;
        } catch (error) {
            console.error('Failed to fetch workout results:', error);
            return null;
        }
    }

    /**
     * Get workout results for multiple exercises
     */
    async getBatchWorkoutResults(exerciseIds: string[]): Promise<Record<string, WorkoutResults>> {
        try {
            console.log('🔍 RAW: Requesting batch results for:', exerciseIds);

            const response = await apiClient.post<Record<string, WorkoutResults>>('/api/calendar/results/batch', exerciseIds);

            // Add this debug logging
            console.log('🔍 RAW batch results from API:', response);
            console.log('🔍 First result structure:', Object.keys(response)[0] ? response[Object.keys(response)[0]] : 'No results');

            if (Object.keys(response).length > 0) {
                const firstResult = response[Object.keys(response)[0]];
                console.log('🔍 First result sets data:', firstResult?.sets);
                console.log('🔍 Sets array length:', firstResult?.sets?.length || 0);
                console.log('🔍 All fields in first result:', Object.keys(firstResult || {}));
            }

            return response;
        } catch (error) {
            console.error('Failed to fetch batch workout results:', error);
            return {};
        }
    }

    /**
     * ✅ NEW: Mark exercise as completed with full performance tracking
     */
    async markExerciseCompletedWithPerformance(
        exerciseId: string,
        completionData: {
            exerciseId: string;
            scheduledExerciseId: string;
            completedAt: string;
            totalDurationMinutes: number;
            sets: Array<{
                setNumber: number;
                targetReps: number;
                actualReps: number;
                targetWeight?: number;
                actualWeight?: number;
                targetWeightUnit: string;
                rpe?: number;
                restSeconds?: number;
                completed: boolean;
                actualDurationMinutes?: number;
                actualHoldSeconds?: number;
                notes?: string;
            }>;
            notes?: string;
            performanceRating: string;
            personalRecords: any[];
            improvements: any[];

            // Optional workout session data
            difficultyRating?: number;
            overallEffort?: number;
            mood?: string;
            location?: string;
            workoutFeedback?: string;
            performanceSummary?: string;

            // Optional cardio data
            distanceKm?: number;
            caloriesBurned?: number;
        }
    ): Promise<ScheduledWorkoutResponse> {
        try {
            console.log('✅ API: Marking exercise as completed with full performance tracking:', exerciseId);

            const response = await apiClient.post<ScheduledWorkoutResponse>(
                `/api/calendar/exercises/${exerciseId}/complete-with-performance`,
                completionData
            );

            console.log(`✅ Successfully marked exercise ${exerciseId} as completed with performance data`);
            return response;
        } catch (error) {
            console.error('❌ Failed to mark exercise as completed with performance:', error);

            // Fallback to simple completion if the enhanced endpoint fails
            console.log('🔄 Falling back to simple completion...');
            try {
                return await this.markExerciseCompleted(exerciseId, {
                    completedAt: completionData.completedAt,
                    totalDurationMinutes: completionData.totalDurationMinutes,
                    notes: completionData.notes,
                    performanceRating: completionData.performanceRating as any
                });
            } catch (fallbackError) {
                console.error('❌ Fallback completion also failed:', fallbackError);
                throw new Error('Failed to mark exercise as completed');
            }
        }
    }

    // ==================== ANALYTICS & STATISTICS ====================

    /**
     * ✅ UNIFIED: Get workout statistics
     */
    async getWorkoutStats(date?: string): Promise<any> {
        try {
            console.log('📊 API: Getting workout statistics', date ? `for ${date}` : '');

            // ✅ UNIFIED: This endpoint remains the same
            const params = date ? {date} : {};
            const response = await apiClient.get('/api/calendar/stats', params);

            console.log('✅ Successfully fetched workout statistics');
            return response;
        } catch (error) {
            console.error('❌ Failed to fetch workout statistics:', error);
            throw new Error('Failed to load workout statistics');
        }
    }

    /**
     * ✅ UNIFIED: Get today's workouts
     */
    async getTodaysWorkouts(): Promise<ScheduledWorkoutResponse[]> {
        try {
            console.log('📅 API: Getting today\'s workouts');

            // ✅ UNIFIED: This endpoint remains the same
            const response = await apiClient.get<ScheduledWorkoutResponse[]>('/api/calendar/today');

            console.log(`✅ Successfully fetched ${response.length} workouts for today`);
            return response;
        } catch (error) {
            console.error('❌ Failed to fetch today\'s workouts:', error);
            throw new Error('Failed to load today\'s workouts');
        }
    }

    /**
     * ✅ UNIFIED: Get upcoming workouts
     */
    async getUpcomingWorkouts(days: number = 7): Promise<ScheduledWorkoutResponse[]> {
        try {
            console.log(`📅 API: Getting upcoming workouts for next ${days} days`);

            // ✅ UNIFIED: This endpoint remains the same
            const response = await apiClient.get<ScheduledWorkoutResponse[]>('/api/calendar/upcoming', {days});

            console.log(`✅ Successfully fetched ${response.length} upcoming workouts`);
            return response;
        } catch (error) {
            console.error('❌ Failed to fetch upcoming workouts:', error);
            throw new Error('Failed to load upcoming workouts');
        }
    }
}

// Export singleton instance
export const calendarApi = new CalendarApiService();