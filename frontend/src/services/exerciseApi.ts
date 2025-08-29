// src/services/exerciseApi.ts - Fixed version with correct type imports and complete functionality
import {
    Exercise,
    ExerciseType,
    DifficultyLevel,
    ExerciseFilters,
    Goal
} from '../types/exercise';
import {
    BackendExercise,
    ApiExerciseFilters,
    GoalData,
    FiltersData
} from '../types/api';
import {
    transformBackendExerciseToFrontend,
    logTransformation,
    validateTransformationResult
} from './transformers';
import apiClient from './apiClient';

class ExerciseApiService {
    private readonly useAuthenticatedClient = true; // Feature flag to control which HTTP client to use

    // ===================================================================
    // 🔧 PRIVATE UTILITY METHODS
    // ===================================================================

    /**
     * Build URL with query parameters for fetch-based requests
     */
    private buildUrl(endpoint: string, params?: any): string {
        const baseUrl = 'http://localhost:8080';
        const url = new URL(`${baseUrl}${endpoint}`);

        if (params) {
            Object.entries(params).forEach(([key, value]) => {
                if (value !== undefined && value !== null && value !== 'all' && value !== '') {
                    url.searchParams.append(key, value.toString());
                }
            });
        }

        return url.toString();
    }

    /**
     * Transform frontend filters to backend API format
     */
    private transformFiltersForBackend(filters?: ExerciseFilters): ApiExerciseFilters {
        if (!filters) return {};

        return {
            goal: filters.activeGoal !== 'all' ? filters.activeGoal : undefined,
            difficulty: filters.selectedDifficulty !== 'all' ? filters.selectedDifficulty.toUpperCase() as any : undefined,
            equipment: filters.selectedEquipment !== 'all' ? filters.selectedEquipment : undefined,
            exercise_type: filters.selectedExerciseType !== 'all' ? filters.selectedExerciseType.toUpperCase() as any : undefined,
            q: filters.searchTerm || undefined,
            page: 0,
            size: 50
        };
    }

    /**
     * ✅ FIXED: Create complete BackendExercise object with all required properties
     */
    private createCompleteBackendExercise(exerciseData: any): BackendExercise {
        return {
            // Core identification
            id: exerciseData.id,
            name: exerciseData.name || exerciseData.exerciseName || '',
            emoji: exerciseData.emoji || null,
            description: exerciseData.description || '',

            // Exercise type information
            exerciseType: exerciseData.exerciseType || 'STRENGTH',
            exerciseTypeDisplay: exerciseData.exerciseTypeDisplay || 'Strength Training',
            isCardio: exerciseData.isCardio || false,
            isIsometric: exerciseData.isIsometric || false,

            // Difficulty information
            difficultyLevel: exerciseData.difficultyLevel || 'BEGINNER',
            difficultyDescription: exerciseData.difficultyDescription || 'Beginner level',

            // Duration and calories
            estimatedDurationMinutes: exerciseData.estimatedDurationMinutes || null,
            estimatedCalories: exerciseData.estimatedCalories || null,

            // Equipment and muscles
            targetMuscleGroups: exerciseData.targetMuscleGroups || [],
            equipmentRequired: exerciseData.equipmentRequired || [],
            equipmentSummary: exerciseData.equipmentSummary || 'No equipment needed',

            // Additional information
            benefits: exerciseData.benefits || [],
            tips: exerciseData.tips || [],
            videoUrl: exerciseData.videoUrl || null,

            // Metrics and ratings
            usageCount: exerciseData.usageCount || 0,
            averageRating: exerciseData.averageRating || 0,
            totalRatings: exerciseData.totalRatings || 0,

            // Flags and characteristics
            isPopular: exerciseData.isPopular || false,
            isHighlyRated: exerciseData.isHighlyRated || false,
            isFromVerifiedSource: exerciseData.isFromVerifiedSource || false,
            canDoAtHome: exerciseData.canDoAtHome || true,
            requiresEquipment: exerciseData.requiresEquipment || false,

            // Creator and timestamps
            createdBy: exerciseData.createdBy || 'Platform',
            createdAt: exerciseData.createdAt || new Date().toISOString(),
            updatedAt: exerciseData.updatedAt || new Date().toISOString(),

            // Frontend-specific fields (optional)
            duration: exerciseData.duration || null,
            calories: exerciseData.calories || null,
            equipment: exerciseData.equipment || null,
            difficulty: exerciseData.difficulty || null,
            goal: exerciseData.goal || null,
            goals: exerciseData.goals || null,
            hasVideo: exerciseData.hasVideo || null,
            rating: exerciseData.rating || null
        };
    }

    /**
     * Enhanced HTTP client method to support different HTTP methods
     */
    private async fetchWithErrorHandling<T>(endpoint: string, params?: any, method: string = 'GET'): Promise<T> {
        if (this.useAuthenticatedClient) {
            // Use your sophisticated API client with JWT auth and error handling
            switch (method.toUpperCase()) {
                case 'GET':
                    return apiClient.get<T>(endpoint, params);
                case 'POST':
                    return apiClient.post<T>(endpoint, params);
                case 'DELETE':
                    // ✅ FIXED: Your apiClient.delete only accepts endpoint parameter
                    // For DELETE requests with data, we'll append to URL or use POST with different endpoint
                    if (params && Array.isArray(params)) {
                        // For bulk operations, send as POST to a bulk endpoint or append to URL
                        return apiClient.post<T>(`${endpoint}`, params);
                    } else {
                        return apiClient.delete<T>(endpoint);
                    }
                case 'PUT':
                    return apiClient.put<T>(endpoint, params);
                default:
                    return apiClient.get<T>(endpoint, params);
            }
        } else {
            // Fallback to the original fetch-based approach
            const url = method === 'GET' ? this.buildUrl(endpoint, params) : this.buildUrl(endpoint);

            const fetchOptions: RequestInit = {
                method,
                headers: {
                    'Content-Type': 'application/json',
                },
            };

            if (method !== 'GET' && params) {
                fetchOptions.body = JSON.stringify(params);
            }

            const response = await fetch(url, fetchOptions);

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            return await response.json();
        }
    }

    // ===================================================================
    // 🌐 PUBLIC EXERCISE ENDPOINTS
    // ===================================================================

    /**
     * Get public exercises with filtering
     */
    async getPublicExercises(filters?: ExerciseFilters): Promise<Exercise[]> {
        try {
            // Transform frontend filters to backend API format
            const apiFilters = this.transformFiltersForBackend(filters);

            const backendExercises = await this.fetchWithErrorHandling<BackendExercise[]>(
                '/api/exercises/public',
                apiFilters
            );

            // Use your sophisticated transformation layer
            const transformedExercises = backendExercises.map(backendExercise => {
                const frontendExercise = transformBackendExerciseToFrontend(backendExercise);
                logTransformation('BackendExercise -> Exercise', backendExercise, frontendExercise);
                return validateTransformationResult(frontendExercise, backendExercise.id);
            });

            console.log(
                `✅ Successfully fetched ${transformedExercises.length} exercises. ` +
                `Cardio: ${transformedExercises.filter(ex => ex.isCardio).length}, ` +
                `Isometric: ${transformedExercises.filter(ex => ex.isIsometric).length}, ` +
                `Strength: ${transformedExercises.filter(ex => !ex.isCardio && !ex.isIsometric).length}`
            );

            return transformedExercises;
        } catch (error) {
            console.error('Failed to fetch public exercises:', error);
            throw new Error('Unable to load exercises. Please try again.');
        }
    }

    /**
     * Search exercises with query and filters
     */
    async searchExercises(query: string, filters?: ExerciseFilters): Promise<Exercise[]> {
        try {
            const apiFilters = this.transformFiltersForBackend(filters);
            apiFilters.q = query;

            const backendExercises = await this.fetchWithErrorHandling<BackendExercise[]>(
                '/api/exercises/public/search',
                apiFilters
            );

            return backendExercises.map(transformBackendExerciseToFrontend);
        } catch (error) {
            console.error('Failed to search exercises:', error);
            throw new Error('Unable to search exercises. Please try again.');
        }
    }

    /**
     * Get single exercise by ID
     */
    async getExerciseById(id: number): Promise<Exercise> {
        try {
            const backendExercise = await this.fetchWithErrorHandling<BackendExercise>(
                `/api/exercises/${id}` // ✅ FIXED: Use correct endpoint
            );

            const transformedExercise = transformBackendExerciseToFrontend(backendExercise);

            console.log(
                `✅ Fetched exercise "${transformedExercise.name}" ` +
                `(${transformedExercise.isCardio ? 'Cardio' : 'Strength'} exercise)`
            );

            return transformedExercise;
        } catch (error) {
            console.error('Failed to fetch exercise by ID:', error);
            throw new Error('Unable to load exercise. Please try again.');
        }
    }

    /**
     * ✅ FIXED: Get enhanced exercises with favorite status (for authenticated users)
     */
    async getPublicExercisesWithFavorites(filters?: ExerciseFilters): Promise<Exercise[]> {
        try {
            const apiFilters = this.transformFiltersForBackend(filters);

            const enhancedExercises = await this.fetchWithErrorHandling<any[]>(
                '/api/exercises/public/enhanced',
                apiFilters
            );

            // Transform the enhanced response to Exercise format
            const transformedExercises = enhancedExercises.map(exerciseData => {
                // ✅ FIXED: Create complete BackendExercise object
                const backendExercise = this.createCompleteBackendExercise(exerciseData);
                const frontendExercise = transformBackendExerciseToFrontend(backendExercise);

                // Add the favorite status from the API response
                frontendExercise.isFavorite = exerciseData.isFavorite || false;

                return frontendExercise;
            });

            console.log(
                `✅ Successfully fetched ${transformedExercises.length} exercises with favorite status. ` +
                `Favorites: ${transformedExercises.filter(ex => ex.isFavorite).length}`
            );

            return transformedExercises;
        } catch (error) {
            console.error('Failed to fetch exercises with favorite status:', error);
            // Fallback to regular exercises without favorite status
            return this.getPublicExercises(filters);
        }
    }

    // ===================================================================
    // 🎯 FILTER & GOAL ENDPOINTS
    // ===================================================================

    /**
     * Get available fitness goals with counts
     */
    async getGoals(): Promise<GoalData[]> {
        try {
            return this.fetchWithErrorHandling<GoalData[]>('/api/exercises/goals');
        } catch (error) {
            console.error('Failed to fetch goals:', error);
            throw new Error('Unable to load fitness goals. Please try again.');
        }
    }

    /**
     * Get available filter options
     */
    async getFilters(): Promise<FiltersData> {
        try {
            return this.fetchWithErrorHandling<FiltersData>('/api/exercises/public/filters');
        } catch (error) {
            console.error('Failed to fetch filters:', error);
            throw new Error('Unable to load filter options. Please try again.');
        }
    }

    // ===================================================================
    // 🔄 UTILITY METHODS
    // ===================================================================

    /**
     * Get filtered exercises (with search support)
     */
    async getFilteredExercises(filters: ExerciseFilters): Promise<Exercise[]> {
        if (filters.searchTerm?.trim()) {
            return this.searchExercises(filters.searchTerm.trim(), filters);
        } else {
            return this.getPublicExercises(filters);
        }
    }

    /**
     * Get initial data for app loading
     */
    async getInitialData(): Promise<{
        exercises: Exercise[];
        goals: GoalData[];
        filters: FiltersData;
    }> {
        try {
            const [exercises, goals, filters] = await Promise.all([
                this.getPublicExercises(),
                this.getGoals(),
                this.getFilters()
            ]);

            return { exercises, goals, filters };
        } catch (error) {
            throw new Error(`Failed to load initial data: ${error instanceof Error ? error.message : 'Unknown error'}`);
        }
    }

    // ===================================================================
    // ⭐ FAVORITES API METHODS
    // ===================================================================

    /**
     * Get user's favorite exercises
     */
    async getFavoriteExercises(): Promise<Exercise[]> {
        try {
            const backendExercises = await this.fetchWithErrorHandling<BackendExercise[]>('/api/exercises/favorites');
            return backendExercises.map(transformBackendExerciseToFrontend);
        } catch (error) {
            console.error('Failed to fetch favorite exercises:', error);
            throw new Error('Unable to load favorite exercises. Please try again.');
        }
    }

    /**
     * Add exercise to favorites
     */
    async addToFavorites(exerciseId: number): Promise<{ success: boolean; isFavorite: boolean }> {
        try {
            const response = await this.fetchWithErrorHandling<{
                success: boolean;
                isFavorite: boolean;
                message: string;
            }>(`/api/exercises/favorites/${exerciseId}`, {}, 'POST');

            console.log(`✅ Added exercise ${exerciseId} to favorites`);
            return { success: response.success, isFavorite: response.isFavorite };
        } catch (error) {
            console.error('Failed to add exercise to favorites:', error);
            throw new Error('Unable to add exercise to favorites. Please try again.');
        }
    }

    /**
     * Remove exercise from favorites
     */
    async removeFromFavorites(exerciseId: number): Promise<{ success: boolean; isFavorite: boolean }> {
        try {
            const response = await this.fetchWithErrorHandling<{
                success: boolean;
                isFavorite: boolean;
                message: string;
            }>(`/api/exercises/favorites/${exerciseId}`, {}, 'DELETE');

            console.log(`✅ Removed exercise ${exerciseId} from favorites`);
            return { success: response.success, isFavorite: response.isFavorite };
        } catch (error) {
            console.error('Failed to remove exercise from favorites:', error);
            throw new Error('Unable to remove exercise from favorites. Please try again.');
        }
    }

    /**
     * Toggle favorite status for an exercise
     */
    async toggleFavorite(exerciseId: number): Promise<{ success: boolean; isFavorite: boolean }> {
        try {
            const response = await this.fetchWithErrorHandling<{
                exerciseId: number;
                isFavorite: boolean;
                action: string;
                message: string;
            }>(`/api/exercises/favorites/${exerciseId}/toggle`, {}, 'POST');

            console.log(`✅ Toggled favorite status for exercise ${exerciseId}: ${response.isFavorite ? 'favorited' : 'unfavorited'}`);
            return { success: true, isFavorite: response.isFavorite };
        } catch (error) {
            console.error('Failed to toggle favorite status:', error);
            throw new Error('Unable to update favorite status. Please try again.');
        }
    }

    /**
     * Check if exercise is favorited
     */
    async getFavoriteStatus(exerciseId: number): Promise<boolean> {
        try {
            const response = await this.fetchWithErrorHandling<{
                exerciseId: number;
                isFavorite: boolean;
            }>(`/api/exercises/favorites/${exerciseId}/status`);

            return response.isFavorite;
        } catch (error) {
            console.error('Failed to check favorite status:', error);
            return false; // Default to not favorited on error
        }
    }

    /**
     * Get user's favorite exercise IDs (lightweight)
     */
    async getFavoriteExerciseIds(): Promise<Set<number>> {
        try {
            const favoriteIds = await this.fetchWithErrorHandling<number[]>('/api/exercises/favorites/ids');
            return new Set(favoriteIds);
        } catch (error) {
            console.error('Failed to fetch favorite exercise IDs:', error);
            return new Set(); // Return empty set on error
        }
    }

    /**
     * Check favorite status for multiple exercises
     */
    async checkMultipleFavoriteStatus(exerciseIds: number[]): Promise<Record<number, boolean>> {
        try {
            if (exerciseIds.length === 0) {
                return {};
            }

            return await this.fetchWithErrorHandling<Record<number, boolean>>(
                '/api/exercises/favorites/check',
                exerciseIds,
                'POST'
            );
        } catch (error) {
            console.error('Failed to check multiple favorite statuses:', error);
            // Return default false for all exercises on error
            return exerciseIds.reduce((acc, id) => {
                acc[id] = false;
                return acc;
            }, {} as Record<number, boolean>);
        }
    }

    /**
     * Get user's favorite count
     */
    async getFavoriteCount(): Promise<number> {
        try {
            const response = await this.fetchWithErrorHandling<{
                favoriteCount: number;
            }>('/api/exercises/favorites/count');

            return response.favoriteCount;
        } catch (error) {
            console.error('Failed to get favorite count:', error);
            return 0;
        }
    }

    // ===================================================================
    // 📦 BULK FAVORITES OPERATIONS
    // ===================================================================

    /**
     * Add multiple exercises to favorites
     */
    async addMultipleToFavorites(exerciseIds: number[]): Promise<{ success: boolean; addedCount: number }> {
        try {
            const response = await this.fetchWithErrorHandling<{
                success: boolean;
                addedCount: number;
                requestedCount: number;
                message: string;
            }>('/api/exercises/favorites/bulk/add', exerciseIds, 'POST');

            console.log(`✅ Added ${response.addedCount} exercises to favorites`);
            return { success: response.success, addedCount: response.addedCount };
        } catch (error) {
            console.error('Failed to add multiple exercises to favorites:', error);
            throw new Error('Unable to add exercises to favorites. Please try again.');
        }
    }

    /**
     * Remove multiple exercises from favorites
     */
    async removeMultipleFromFavorites(exerciseIds: number[]): Promise<{ success: boolean; removedCount: number }> {
        try {
            const response = await this.fetchWithErrorHandling<{
                success: boolean;
                removedCount: number;
                message: string;
            }>('/api/exercises/favorites/bulk/remove', exerciseIds, 'DELETE');

            console.log(`✅ Removed ${response.removedCount} exercises from favorites`);
            return { success: response.success, removedCount: response.removedCount };
        } catch (error) {
            console.error('Failed to remove multiple exercises from favorites:', error);
            throw new Error('Unable to remove exercises from favorites. Please try again.');
        }
    }

    /**
     * Clear all user favorites
     */
    async clearAllFavorites(): Promise<{ success: boolean; clearedCount: number }> {
        try {
            const response = await this.fetchWithErrorHandling<{
                success: boolean;
                clearedCount: number;
                message: string;
            }>('/api/exercises/favorites/clear', {}, 'DELETE');

            console.log(`✅ Cleared ${response.clearedCount} favorites`);
            return { success: response.success, clearedCount: response.clearedCount };
        } catch (error) {
            console.error('Failed to clear all favorites:', error);
            throw new Error('Unable to clear favorites. Please try again.');
        }
    }
}

// Export singleton instance
export const exerciseApi = new ExerciseApiService();