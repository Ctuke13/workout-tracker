// src/services/exerciseApi.ts - Fixed version with correct type imports
import {
    Exercise,
    ExerciseType,
    DifficultyLevel,
    ExerciseFilters, // ✅ FIXED: Import directly from exercise.ts (no more conflicts)
    Goal             // ✅ FIXED: Import directly from exercise.ts (no more conflicts)
} from '../types/exercise';
import {
    BackendExercise,
    ApiExerciseFilters, // ✅ FIXED: Use the renamed API-specific filters
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

    // Enhanced HTTP client method that can use either approach
    private async fetchWithErrorHandling<T>(endpoint: string, params?: any): Promise<T> {
        if (this.useAuthenticatedClient) {
            // Use your sophisticated API client with JWT auth and error handling
            return apiClient.get<T>(endpoint, { params });
        } else {
            // Fallback to the original fetch-based approach
            const url = this.buildUrl(endpoint, params);
            const response = await fetch(url);

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            return await response.json();
        }
    }

    // Keep your existing URL building logic (it's good!)
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

    // Enhanced public exercises method using your transformation layer
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

    // Enhanced search with your transformation layer
    async searchExercises(query: string, filters?: ExerciseFilters): Promise<Exercise[]> {
        const apiFilters = this.transformFiltersForBackend(filters);
        apiFilters.q = query;

        const backendExercises = await this.fetchWithErrorHandling<BackendExercise[]>(
            '/api/exercises/public/search',
            apiFilters
        );

        return backendExercises.map(transformBackendExerciseToFrontend);
    }

    // Enhanced single exercise fetch
    async getExerciseById(id: number): Promise<Exercise> {
        const backendExercise = await this.fetchWithErrorHandling<BackendExercise>(
            `/api/exercises/public/${id}`
        );

        const transformedExercise = transformBackendExerciseToFrontend(backendExercise);

        console.log(
            `✅ Fetched exercise "${transformedExercise.name}" ` +
            `(${transformedExercise.isCardio ? 'Cardio' : 'Strength'} exercise)`
        );

        return transformedExercise;
    }

    // ✅ FIXED: Transform frontend filters to backend API format
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

    // Keep your existing goal and filter methods, but enhance them with transformation
    async getGoals(): Promise<GoalData[]> {
        return this.fetchWithErrorHandling<GoalData[]>('/api/exercises/goals');
    }

    async getFilters(): Promise<FiltersData> {
        return this.fetchWithErrorHandling<FiltersData>('/api/exercises/public/filters');
    }

    // Keep your convenient utility methods
    async getFilteredExercises(filters: ExerciseFilters): Promise<Exercise[]> {
        if (filters.searchTerm?.trim()) {
            return this.searchExercises(filters.searchTerm.trim(), filters);
        } else {
            return this.getPublicExercises(filters);
        }
    }

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
}

// Export singleton instance
export const exerciseApi = new ExerciseApiService();