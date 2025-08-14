import apiClient from './apiClient';
import { WorkoutPlanInfo, WorkoutPlanFilters } from '../types/api';

// ==================== API RESPONSE TYPES ====================

interface WorkoutPlanListResponse {
    workoutPlans: WorkoutPlanInfo[];
    totalCount: number;
    currentPage?: number;
    totalPages?: number;
    pageSize?: number;
    hasNext?: boolean;
    hasPrevious?: boolean;
    isFiltered?: boolean;
}

interface WorkoutPlanSearchResponse {
    results: WorkoutPlanInfo[];
    searchTerm?: string;
    totalResults: number;
    page?: number;
    size?: number;
    hasMore?: boolean;
    searchTimeMs?: number;
    searchType?: string;
}

export class WorkoutPlanApiService {

    // ==================== WORKOUT PLAN RETRIEVAL ====================

    /**
     * Get all public workout plans
     */
    async getPublicWorkoutPlans(filters?: WorkoutPlanFilters): Promise<WorkoutPlanInfo[]> {
        try {
            console.log('📋 API: Getting public workout plans from backend');

            // Your apiClient.get() already extracts data, so response IS the data
            const responseData = await apiClient.get<WorkoutPlanListResponse | WorkoutPlanInfo[]>('/api/workout-plans', filters);

            let plans: WorkoutPlanInfo[] = [];
            if (Array.isArray(responseData)) {
                // Direct array response
                plans = responseData;
            } else if (responseData && 'workoutPlans' in responseData) {
                // Wrapped response
                plans = responseData.workoutPlans || [];
            }

            console.log(`✅ Successfully fetched ${plans.length} workout plans from backend`);
            return plans.map(this.transformWorkoutPlan);
        } catch (error) {
            console.error('❌ Failed to fetch workout plans from backend:', error);
            throw new Error('Failed to load workout plans from backend');
        }
    }

    /**
     * Get accessible workout plans based on user subscription
     */
    async getAccessibleWorkoutPlans(): Promise<WorkoutPlanInfo[]> {
        try {
            console.log('📋 API: Getting accessible workout plans for user');

            const responseData = await apiClient.get<WorkoutPlanListResponse | WorkoutPlanInfo[]>('/api/workout-plans/accessible');

            let plans: WorkoutPlanInfo[] = [];
            if (Array.isArray(responseData)) {
                plans = responseData;
            } else if (responseData && 'workoutPlans' in responseData) {
                plans = responseData.workoutPlans || [];
            }

            console.log(`✅ Successfully fetched ${plans.length} accessible workout plans`);
            return plans.map(this.transformWorkoutPlan);
        } catch (error) {
            console.error('❌ Failed to fetch accessible workout plans:', error);
            throw new Error('Failed to load accessible workout plans');
        }
    }

    /**
     * Get workout plans by category
     */
    async getWorkoutPlansByCategory(category: string): Promise<WorkoutPlanInfo[]> {
        try {
            console.log('📋 API: Getting workout plans for category:', category);

            const responseData = await apiClient.get<WorkoutPlanListResponse | WorkoutPlanInfo[]>(`/api/workout-plans/category/${category}`);

            let plans: WorkoutPlanInfo[] = [];
            if (Array.isArray(responseData)) {
                plans = responseData;
            } else if (responseData && 'workoutPlans' in responseData) {
                plans = responseData.workoutPlans || [];
            }

            console.log(`✅ Successfully fetched ${plans.length} workout plans for ${category}`);
            return plans.map(this.transformWorkoutPlan);
        } catch (error) {
            console.error('❌ Failed to fetch workout plans by category:', error);
            throw new Error('Failed to load workout plans by category');
        }
    }

    /**
     * Get workout plans by difficulty
     */
    async getWorkoutPlansByDifficulty(difficulty: string): Promise<WorkoutPlanInfo[]> {
        try {
            console.log('📋 API: Getting workout plans for difficulty:', difficulty);

            const responseData = await apiClient.get<WorkoutPlanListResponse | WorkoutPlanInfo[]>(`/api/workout-plans/difficulty/${difficulty}`);

            let plans: WorkoutPlanInfo[] = [];
            if (Array.isArray(responseData)) {
                plans = responseData;
            } else if (responseData && 'workoutPlans' in responseData) {
                plans = responseData.workoutPlans || [];
            }

            console.log(`✅ Successfully fetched ${plans.length} workout plans for ${difficulty}`);
            return plans.map(this.transformWorkoutPlan);
        } catch (error) {
            console.error('❌ Failed to fetch workout plans by difficulty:', error);
            throw new Error('Failed to load workout plans by difficulty');
        }
    }

    /**
     * Get popular workout plans
     */
    async getPopularWorkoutPlans(limit: number = 10): Promise<WorkoutPlanInfo[]> {
        try {
            console.log('📋 API: Getting popular workout plans');

            const responseData = await apiClient.get<WorkoutPlanListResponse | WorkoutPlanInfo[]>('/api/workout-plans/popular', { limit });

            let plans: WorkoutPlanInfo[] = [];
            if (Array.isArray(responseData)) {
                plans = responseData;
            } else if (responseData && 'workoutPlans' in responseData) {
                plans = responseData.workoutPlans || [];
            }

            console.log(`✅ Successfully fetched ${plans.length} popular workout plans`);
            return plans.map(this.transformWorkoutPlan);
        } catch (error) {
            console.error('❌ Failed to fetch popular workout plans:', error);
            throw new Error('Failed to load popular workout plans');
        }
    }

    /**
     * Get trending workout plans
     */
    async getTrendingWorkoutPlans(limit: number = 10): Promise<WorkoutPlanInfo[]> {
        try {
            console.log('📋 API: Getting trending workout plans');

            const responseData = await apiClient.get<WorkoutPlanListResponse | WorkoutPlanInfo[]>('/api/workout-plans/trending', { limit });

            let plans: WorkoutPlanInfo[] = [];
            if (Array.isArray(responseData)) {
                plans = responseData;
            } else if (responseData && 'workoutPlans' in responseData) {
                plans = responseData.workoutPlans || [];
            }

            console.log(`✅ Successfully fetched ${plans.length} trending workout plans`);
            return plans.map(this.transformWorkoutPlan);
        } catch (error) {
            console.error('❌ Failed to fetch trending workout plans:', error);
            throw new Error('Failed to load trending workout plans');
        }
    }

    /**
     * Get highly rated workout plans
     */
    async getHighlyRatedWorkoutPlans(minRating: number = 4.0): Promise<WorkoutPlanInfo[]> {
        try {
            console.log('📋 API: Getting highly rated workout plans');

            const responseData = await apiClient.get<WorkoutPlanListResponse | WorkoutPlanInfo[]>('/api/workout-plans/highly-rated', { minRating });

            let plans: WorkoutPlanInfo[] = [];
            if (Array.isArray(responseData)) {
                plans = responseData;
            } else if (responseData && 'workoutPlans' in responseData) {
                plans = responseData.workoutPlans || [];
            }

            console.log(`✅ Successfully fetched ${plans.length} highly rated workout plans`);
            return plans.map(this.transformWorkoutPlan);
        } catch (error) {
            console.error('❌ Failed to fetch highly rated workout plans:', error);
            throw new Error('Failed to load highly rated workout plans');
        }
    }

    /**
     * Search workout plans
     */
    async searchWorkoutPlans(query: string, filters?: WorkoutPlanFilters): Promise<WorkoutPlanInfo[]> {
        try {
            console.log('📋 API: Searching workout plans with query:', query);

            const params = {
                q: query,
                ...filters
            };

            const responseData = await apiClient.get<WorkoutPlanSearchResponse | WorkoutPlanInfo[]>('/api/workout-plans/search', params);

            let plans: WorkoutPlanInfo[] = [];
            if (Array.isArray(responseData)) {
                plans = responseData;
            } else if (responseData && 'results' in responseData) {
                plans = responseData.results || [];
            }

            console.log(`✅ Successfully found ${plans.length} workout plans for "${query}"`);
            return plans.map(this.transformWorkoutPlan);
        } catch (error) {
            console.error('❌ Failed to search workout plans:', error);
            throw new Error('Failed to search workout plans');
        }
    }

    /**
     * Get a specific workout plan by ID
     */
    async getWorkoutPlanById(id: number): Promise<WorkoutPlanInfo> {
        try {
            console.log('📋 API: Getting workout plan details for ID:', id);

            const plan = await apiClient.get<WorkoutPlanInfo>(`/api/workout-plans/${id}`);

            console.log(`✅ Successfully fetched workout plan: ${plan.workoutName}`);
            return this.transformWorkoutPlan(plan);
        } catch (error) {
            console.error('❌ Failed to fetch workout plan details:', error);
            throw new Error('Failed to load workout plan details');
        }
    }

    /**
     * Transform backend WorkoutPlanResponse to frontend-friendly format
     */
    private transformWorkoutPlan(plan: WorkoutPlanInfo): WorkoutPlanInfo {
        return {
            ...plan,
            // Add frontend-friendly aliases
            name: plan.workoutName,
            description: plan.workoutDescription,
            category: plan.workoutCategory,
            difficulty: plan.difficultyLevel,
            // Parse comma-separated strings to arrays if needed
            targetMuscleGroups: plan.targetMuscleGroups || '',
            equipmentNeeded: plan.equipmentNeeded || '',
            // Ensure required fields have defaults
            timesUsed: plan.timesUsed || 0,
            averageRating: plan.averageRating || 0,
            estimatedDurationMinutes: plan.estimatedDurationMinutes || 30,
            // You might need to compute exerciseCount separately or add it to your backend
            exerciseCount: plan.exerciseCount || 5
        };
    }

    /**
     * Get workout plan statistics
     */
    async getWorkoutPlanStatistics(): Promise<Record<string, any>> {
        try {
            console.log('📋 API: Getting workout plan statistics');

            const statistics = await apiClient.get<Record<string, any>>('/api/workout-plans/statistics');

            console.log('✅ Successfully fetched workout plan statistics');
            return statistics;
        } catch (error) {
            console.error('❌ Failed to fetch workout plan statistics:', error);
            throw new Error('Failed to load workout plan statistics');
        }
    }

    // ==================== ENHANCED DATA LOADING METHODS ====================

    /**
     * Get initial workout plan data for the selector - optimized for your backend
     */
    async getInitialWorkoutPlanData(): Promise<{
        allPlans: WorkoutPlanInfo[];
        freePlans: WorkoutPlanInfo[];
        popularPlans: WorkoutPlanInfo[];
        trendingPlans: WorkoutPlanInfo[];
        statistics: any;
    }> {
        try {
            console.log('📋 API: Loading initial workout plan data from backend...');

            // Use your accessible endpoint for better UX - it respects subscription
            const [accessiblePlans, popularPlans, trendingPlans, statistics] = await Promise.all([
                this.getAccessibleWorkoutPlans(),
                this.getPopularWorkoutPlans(8),
                this.getTrendingWorkoutPlans(6),
                this.getWorkoutPlanStatistics().catch(() => ({})) // Statistics optional
            ]);

            // Separate free plans from accessible plans
            const freePlans = accessiblePlans.filter(plan =>
                plan.subscriptionTierRequired === 'FREE'
            );

            console.log('✅ Successfully loaded initial workout plan data from backend');
            return {
                allPlans: accessiblePlans,
                freePlans,
                popularPlans,
                trendingPlans,
                statistics
            };
        } catch (error) {
            console.error('❌ Failed to load initial workout plan data from backend:', error);
            throw new Error('Failed to load workout plan data from backend');
        }
    }

    /**
     * Get filtered workout plans with your backend's advanced filtering
     */
    async getFilteredWorkoutPlans(filters: {
        searchTerm?: string;
        category?: string;
        difficulty?: string;
        workoutType?: string;
        equipment?: string;
        muscleGroup?: string;
    }): Promise<WorkoutPlanInfo[]> {
        try {
            console.log('📋 API: Getting filtered workout plans from backend:', filters);

            if (filters.searchTerm?.trim()) {
                // Use the search endpoint with filters
                return this.searchWorkoutPlans(filters.searchTerm.trim(), {
                    category: filters.category !== 'all' ? filters.category : undefined,
                    difficulty: filters.difficulty !== 'all' ? filters.difficulty as any : undefined,
                    workoutType: filters.workoutType !== 'all' ? filters.workoutType as any : undefined,
                    equipment: filters.equipment !== 'all' ? filters.equipment : undefined,
                    muscleGroup: filters.muscleGroup !== 'all' ? filters.muscleGroup : undefined
                });
            } else if (filters.category && filters.category !== 'all') {
                return this.getWorkoutPlansByCategory(filters.category);
            } else if (filters.difficulty && filters.difficulty !== 'all') {
                return this.getWorkoutPlansByDifficulty(filters.difficulty);
            } else {
                // Get all accessible plans
                return this.getAccessibleWorkoutPlans();
            }
        } catch (error) {
            console.error('❌ Failed to get filtered workout plans from backend:', error);
            throw new Error('Failed to filter workout plans');
        }
    }

    // ==================== WORKOUT PLAN SCHEDULING (REAL BACKEND) ====================

    /**
     * Schedule a workout plan using your backend API
     */
    async scheduleWorkoutPlan(request: {
        workoutPlanId: number;
        scheduledDate: string;
        customNotes?: string;
    }): Promise<any> {
        try {
            console.log('📋 API: Scheduling workout plan via backend:', request);

            const response = await apiClient.post<any>('/api/calendar/schedule-workout-plan', request);

            console.log('✅ Successfully scheduled workout plan via backend');
            return response;
        } catch (error) {
            console.error('❌ Failed to schedule workout plan via backend:', error);
            throw error; // Re-throw to preserve error details for proper handling
        }
    }

    /**
     * Schedule multiple exercises from a workout plan
     */
    async scheduleMultipleExercises(request: {
        workoutPlanId: number;
        scheduledDate: string;
        selectedExerciseIds?: number[];
        customNotes?: string;
    }): Promise<any[]> {
        try {
            console.log('📋 API: Scheduling multiple exercises from workout plan via backend:', request);

            const response = await apiClient.post<any[]>('/api/calendar/schedule-multiple-exercises', request);

            console.log('✅ Successfully scheduled multiple exercises via backend');
            return response;
        } catch (error) {
            console.error('❌ Failed to schedule multiple exercises via backend:', error);
            throw error; // Re-throw to preserve error details for proper handling
        }
    }

    // ==================== USER SUBSCRIPTION INFO ====================

    /**
     * Get user's subscription limits for workout plans
     */
    async getSubscriptionLimits(): Promise<Record<string, any>> {
        try {
            console.log('📋 API: Getting subscription limits from backend');

            const limits = await apiClient.get<Record<string, any>>('/api/workout-plans/subscription-limits');

            console.log('✅ Successfully fetched subscription limits from backend');
            return limits;
        } catch (error) {
            console.error('❌ Failed to fetch subscription limits from backend:', error);
            throw new Error('Failed to load subscription limits');
        }
    }
}

// Export singleton instance
export const workoutPlanApi = new WorkoutPlanApiService();