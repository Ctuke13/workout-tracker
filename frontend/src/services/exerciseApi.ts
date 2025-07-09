// src/services/exerciseApi.ts - Typed API service for your backend

import {
    BackendExercise,
    GoalData,
    FiltersData,
    ExerciseFilters,
    Exercise,
    Goal,
    ExerciseApiClient
} from '../types/api';

class ExerciseApiService implements ExerciseApiClient {
    private readonly baseUrl = 'http://localhost:8080/api/exercises';

    // Helper method for making API calls with error handling
    private async fetchWithErrorHandling<T>(url: string): Promise<T> {
        try {
            const response = await fetch(url);

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            if (error instanceof Error) {
                throw new Error(`API Error: ${error.message}`);
            }
            throw new Error('Unknown API error occurred');
        }
    }

    // Build URL with query parameters
    private buildUrl(endpoint: string, params?: ExerciseFilters): string {
        const url = new URL(`${this.baseUrl}${endpoint}`);

        if (params) {
            Object.entries(params).forEach(([key, value]) => {
                if (value !== undefined && value !== null && value !== 'all' && value !== '') {
                    url.searchParams.append(key, value.toString());
                }
            });
        }

        return url.toString();
    }

    // Get all public exercises (with optional filters)
    async getPublicExercises(filters?: ExerciseFilters): Promise<BackendExercise[]> {
        const url = this.buildUrl('/public', filters);
        return this.fetchWithErrorHandling<BackendExercise[]>(url);
    }

    // Search exercises
    async searchExercises(query: string, filters?: ExerciseFilters): Promise<BackendExercise[]> {
        const searchFilters = { ...filters, q: query };
        const url = this.buildUrl('/public/search', searchFilters);
        return this.fetchWithErrorHandling<BackendExercise[]>(url);
    }

    // Get available goals with counts
    async getGoals(): Promise<GoalData[]> {
        const url = `${this.baseUrl}/goals`;
        return this.fetchWithErrorHandling<GoalData[]>(url);
    }

    // Get filter options (equipment, difficulties, etc.)
    async getFilters(): Promise<FiltersData> {
        const url = `${this.baseUrl}/public/filters`;
        return this.fetchWithErrorHandling<FiltersData>(url);
    }

    // Get single exercise by ID
    async getExerciseById(id: number): Promise<BackendExercise> {
        const url = `${this.baseUrl}/public/${id}`;
        return this.fetchWithErrorHandling<BackendExercise>(url);
    }

    // Get exercises based on current filters (handles both search and filter logic)
    async getFilteredExercises(filters: ExerciseFilters): Promise<BackendExercise[]> {
        if (filters.q?.trim()) {
            // Use search endpoint if there's a search query
            return this.searchExercises(filters.q.trim(), filters);
        } else {
            // Use regular filtered endpoint
            return this.getPublicExercises(filters);
        }
    }

    // Fetch all initial data in parallel
    async getInitialData(): Promise<{
        exercises: BackendExercise[];
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

// Data transformation utilities
export class ExerciseDataTransformer {
    static transformExercise(exercise: BackendExercise): Exercise {
        return {
            id: exercise.id,
            name: exercise.name,  // Already correct field name
            emoji: exercise.emoji || '💪',
            difficulty: this.formatDifficulty(exercise.difficultyLevel),
            description: exercise.description,
            duration: this.formatDuration(exercise.estimatedDurationMinutes),
            calories: this.formatCalories(exercise.estimatedCalories),
            equipment: this.formatEquipment(exercise.equipmentRequired),
            benefits: exercise.benefits || [],
            tips: exercise.tips || [],
            videoUrl: exercise.videoUrl,
            type: exercise.exerciseTypeDisplay,
            exerciseType: exercise.exerciseType,
            muscleGroups: exercise.targetMuscleGroups || [],
            rating: exercise.averageRating || 0,
            totalRatings: exercise.totalRatings || 0,
            usageCount: exercise.usageCount || 0,
            isPopular: exercise.isPopular,
            isHighlyRated: exercise.isHighlyRated,
            canDoAtHome: exercise.canDoAtHome,
            requiresEquipment: exercise.requiresEquipment,
            createdBy: exercise.createdBy
        };
    }

    static transformGoals(goalsData: GoalData[], totalExercises: number): Goal[] {
        const baseGoals: Goal[] = [
            { id: 'all', name: 'All Goals', emoji: '🎯', count: totalExercises }
        ];

        const transformedGoals = goalsData.map(goal => ({
            id: goal.goal,
            name: this.formatGoalName(goal.goal),
            emoji: this.getGoalEmoji(goal.goal),
            count: goal.count
        }));

        return [...baseGoals, ...transformedGoals];
    }

    private static formatDuration(durationMinutes?: number | null): string {
        if (!durationMinutes) return "20 mins";

        if (durationMinutes <= 60) {
            return `${durationMinutes} mins`;
        }

        const hours = Math.floor(durationMinutes / 60);
        const mins = durationMinutes % 60;
        return `${hours}h${mins > 0 ? ` ${mins}m` : ''}`;
    }

    private static formatCalories(calories?: number | null): string {
        if (!calories) return "200-400/hr";

        const lower = Math.floor(calories * 0.8);
        const upper = Math.floor(calories * 1.2);
        return `${lower}-${upper}/hr`;
    }

    private static formatDifficulty(difficulty: string): string {
        switch (difficulty) {
            case 'BEGINNER':
                return 'Beginner';
            case 'INTERMEDIATE':
                return 'Intermediate';
            case 'ADVANCED':
                return 'Advanced';
            default:
                return 'Beginner';
        }
    }

    private static formatEquipment(equipmentList?: string[] | null): string {
        if (!equipmentList || equipmentList.length === 0) {
            return "No Equipment";
        }

        if (equipmentList.length === 1) {
            return this.formatSingleEquipment(equipmentList[0]);
        }

        return `${this.formatSingleEquipment(equipmentList[0])} (+more)`;
    }

    private static formatSingleEquipment(equipment: string): string {
        const equipmentMap: Record<string, string> = {
            'dumbbells': 'Dumbbells',
            'dumbbell': 'Dumbbells',
            'barbell': 'Barbell',
            'resistance_bands': 'Resistance Bands',
            'resistance_band': 'Resistance Bands',
            'kettlebell': 'Kettlebell',
            'yoga_mat': 'Yoga Mat',
            'jump_rope': 'Jump Rope',
            'pull_up_bar': 'Pull-up Bar',
            'exercise_bike': 'Exercise Bike',
            'rowing_machine': 'Rowing Machine',
            'foam_roller': 'Foam Roller',
            'cones': 'Training Cones',
            'bodyweight': 'No Equipment',
            'none': 'No Equipment'
        };
        return equipmentMap[equipment.toLowerCase()] || equipment;
    }

    private static formatGoalName(goal: string): string {
        const goalMap: Record<string, string> = {
            'fat-burn': 'Fat Burn',
            'muscle-building': 'Muscle Building',
            'endurance': 'Endurance',
            'flexibility': 'Flexibility',
            'sport-specific': 'Sport-Specific',
            'recovery': 'Recovery & Rehab'
        };
        return goalMap[goal.toLowerCase()] || goal;
    }

    private static getGoalEmoji(goal: string): string {
        const emojiMap: Record<string, string> = {
            'fat-burn': '🔥',
            'muscle-building': '💪',
            'endurance': '⚡',
            'flexibility': '🧘‍♀️',
            'sport-specific': '🎯',
            'recovery': '🛡️'
        };
        return emojiMap[goal.toLowerCase()] || '🎯';
    }
}

// Export singleton instance
export const exerciseApi = new ExerciseApiService();