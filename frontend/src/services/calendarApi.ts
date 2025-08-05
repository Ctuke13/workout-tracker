import apiClient from './apiClient';
import { ScheduledWorkoutResponse, WorkoutPlanInfo } from '../types/api';

export class CalendarApiService {
    // ==================== INDIVIDUAL EXERCISE SCHEDULING ====================

    /**
     * ✅ FIXED: Schedule individual exercise using correct endpoint
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

            // ✅ Use the CalendarController endpoint for individual exercises
            const response = await apiClient.post<ScheduledWorkoutResponse>('/api/calendar/exercises', exerciseData);

            console.log(`✅ Successfully scheduled exercise: ${exerciseData.exerciseId}`);
            return response;
        } catch (error) {
            console.error('❌ Failed to schedule individual exercise:', error);
            throw new Error('Failed to schedule exercise. Please try again.');
        }
    }

    // ==================== WORKOUT PLAN SCHEDULING ====================

    /**
     * Schedule a workout plan (existing method)
     */
    async scheduleWorkout(workoutData: {
        workoutPlanId: number;
        scheduledDate: string;
        customNotes?: string;
    }): Promise<ScheduledWorkoutResponse> {
        try {
            console.log('📅 API: Scheduling workout plan:', workoutData);

            // Use the ScheduledWorkoutController endpoint for workout plans
            const response = await apiClient.post<ScheduledWorkoutResponse>('/api/calendar/schedule', workoutData);

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
     * ✅ FIXED: Get scheduled workouts with proper query parameter handling
     */
    async getScheduledWorkouts(startDate: string, endDate: string): Promise<ScheduledWorkoutResponse[]> {
        try {
            console.log('📅 API: Getting scheduled workouts from', startDate, 'to', endDate);

            // ✅ FIXED: Use direct query parameters (not nested in params object)
            const response = await apiClient.get('/api/calendar', {
                startDate,
                endDate
            });

            // 🔍 DEBUG: Let's see what we're actually getting
            console.log('🔍 DEBUG: Raw API response:', response);
            console.log('🔍 DEBUG: Response type:', typeof response);
            console.log('🔍 DEBUG: Is array?', Array.isArray(response));

            // ✅ FIXED: Handle CalendarViewResponse format
            let workouts: ScheduledWorkoutResponse[] = [];

            if (Array.isArray(response)) {
                // Direct array response
                workouts = response;
            } else if (response && typeof response === 'object') {
                const calendarResponse = response as any;

                // Handle CalendarViewResponse format with workoutsByDate map
                if (calendarResponse.workoutsByDate && typeof calendarResponse.workoutsByDate === 'object') {
                    console.log('🔍 DEBUG: Found workoutsByDate:', calendarResponse.workoutsByDate);

                    // Flatten the map of date -> workout arrays into a single array
                    workouts = Object.values(calendarResponse.workoutsByDate)
                        .flat() as ScheduledWorkoutResponse[];

                    console.log('🔍 DEBUG: Flattened workouts:', workouts);
                } else if (calendarResponse.scheduledWorkouts && Array.isArray(calendarResponse.scheduledWorkouts)) {
                    workouts = calendarResponse.scheduledWorkouts;
                } else if (calendarResponse.data && Array.isArray(calendarResponse.data)) {
                    workouts = calendarResponse.data;
                } else if (calendarResponse.content && Array.isArray(calendarResponse.content)) {
                    workouts = calendarResponse.content;
                } else {
                    console.warn('⚠️ Unexpected response format:', calendarResponse);
                    console.warn('⚠️ Available properties:', Object.keys(calendarResponse));
                    workouts = [];
                }
            } else {
                console.warn('⚠️ Response is not an object or array:', response);
                workouts = [];
            }

            console.log(`✅ Successfully fetched ${workouts.length} scheduled workouts`);
            return workouts;
        } catch (error) {
            console.error('❌ Failed to fetch scheduled workouts:', error);
            throw new Error('Failed to load calendar workouts');
        }
    }

    /**
     * Get scheduled exercises for date range (alternative endpoint)
     */
    async getScheduledExercises(startDate: string, endDate: string): Promise<ScheduledWorkoutResponse[]> {
        try {
            console.log('📅 API: Getting scheduled exercises from', startDate, 'to', endDate);

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
     * Get exercises for a specific date
     */
    async getExercisesForDate(date: string): Promise<ScheduledWorkoutResponse[]> {
        try {
            console.log('📅 API: Getting exercises for date:', date);

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
     * Update a scheduled exercise
     */
    async updateScheduledExercise(exerciseId: string, updates: any): Promise<ScheduledWorkoutResponse> {
        try {
            console.log('📅 API: Updating scheduled exercise:', exerciseId);

            const response = await apiClient.put<ScheduledWorkoutResponse>(`/api/calendar/exercises/${exerciseId}`, updates);

            console.log(`✅ Successfully updated exercise: ${exerciseId}`);
            return response;
        } catch (error) {
            console.error('❌ Failed to update scheduled exercise:', error);
            throw new Error('Failed to update exercise');
        }
    }

    /**
     * Delete a scheduled exercise
     */
    async deleteScheduledExercise(exerciseId: string): Promise<void> {
        try {
            console.log('📅 API: Deleting scheduled exercise:', exerciseId);

            await apiClient.delete(`/api/calendar/exercises/${exerciseId}`);

            console.log(`✅ Successfully deleted exercise: ${exerciseId}`);
        } catch (error) {
            console.error('❌ Failed to delete scheduled exercise:', error);
            throw new Error('Failed to delete exercise');
        }
    }

    /**
     * Mark exercise as completed
     */
    async markExerciseCompleted(exerciseId: string): Promise<ScheduledWorkoutResponse> {
        try {
            console.log('✅ API: Marking exercise as completed:', exerciseId);

            const response = await apiClient.put<ScheduledWorkoutResponse>(`/api/calendar/exercises/${exerciseId}/complete`);

            console.log(`✅ Successfully marked exercise as completed: ${exerciseId}`);
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

            const response = await apiClient.post<ScheduledWorkoutResponse>(`/api/calendar/${workoutId}/start`);

            console.log(`✅ Successfully started workout: ${workoutId}`);
            return response;
        } catch (error) {
            console.error('❌ Failed to start workout:', error);
            throw new Error('Failed to start workout');
        }
    }

    /**
     * Delete a workout
     */
    async deleteWorkout(workoutId: string): Promise<void> {
        try {
            console.log('🗑️ API: Deleting workout:', workoutId);

            await apiClient.delete(`/api/calendar/${workoutId}`);

            console.log(`✅ Successfully deleted workout: ${workoutId}`);
        } catch (error) {
            console.error('❌ Failed to delete workout:', error);
            throw new Error('Failed to delete workout');
        }
    }

    // ==================== ANALYTICS & STATISTICS ====================

    /**
     * Get workout statistics
     */
    async getWorkoutStats(date?: string): Promise<any> {
        try {
            console.log('📊 API: Getting workout statistics', date ? `for ${date}` : '');

            const params = date ? { date } : {};
            const response = await apiClient.get('/api/calendar/stats', params);

            console.log('✅ Successfully fetched workout statistics');
            return response;
        } catch (error) {
            console.error('❌ Failed to fetch workout statistics:', error);
            throw new Error('Failed to load workout statistics');
        }
    }

    /**
     * Get today's workouts
     */
    async getTodaysWorkouts(): Promise<ScheduledWorkoutResponse[]> {
        try {
            console.log('📅 API: Getting today\'s workouts');

            const response = await apiClient.get<ScheduledWorkoutResponse[]>('/api/calendar/today');

            console.log(`✅ Successfully fetched ${response.length} workouts for today`);
            return response;
        } catch (error) {
            console.error('❌ Failed to fetch today\'s workouts:', error);
            throw new Error('Failed to load today\'s workouts');
        }
    }

    /**
     * Get upcoming workouts
     */
    async getUpcomingWorkouts(days: number = 7): Promise<ScheduledWorkoutResponse[]> {
        try {
            console.log(`📅 API: Getting upcoming workouts for next ${days} days`);

            const response = await apiClient.get<ScheduledWorkoutResponse[]>('/api/calendar/upcoming', { days });

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